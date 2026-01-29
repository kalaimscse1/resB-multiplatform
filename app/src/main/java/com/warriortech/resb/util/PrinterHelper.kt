package com.warriortech.resb.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.warriortech.resb.model.*
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.warriortech.resb.data.local.dao.PrintTemplateDao
import com.warriortech.resb.data.local.entity.PrintTemplateColumnEntity
import com.warriortech.resb.data.local.entity.PrintTemplateEntity
import com.warriortech.resb.data.local.entity.PrintTemplateLineEntity
import com.warriortech.resb.data.local.entity.PrintTemplateSectionEntity
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Helper class for handling communication with physical printer hardware.
 */
class PrinterHelper(
    private val context: Context,
    private val printTemplateDao: PrintTemplateDao,
    private val sessionManager: SessionManager
) {

    companion object {
        private const val TAG = "PrinterHelper"
        
        // ESC/POS Commands
        private val ESC_ALIGN_LEFT = byteArrayOf(0x1b, 0x61, 0x00)
        private val ESC_ALIGN_CENTER = byteArrayOf(0x1b, 0x61, 0x01)
        private val ESC_ALIGN_RIGHT = byteArrayOf(0x1b, 0x61, 0x02)
        private val ESC_BOLD_ON = byteArrayOf(0x1b, 0x45, 0x01)
        private val ESC_BOLD_OFF = byteArrayOf(0x1b, 0x45, 0x00)
        private val ESC_UNDERLINE_ON = byteArrayOf(0x1b, 0x2d, 0x01)
        private val ESC_UNDERLINE_OFF = byteArrayOf(0x1b, 0x2d, 0x00)
        private val ESC_INIT = byteArrayOf(0x1b, 0x40)
        private val ESC_FEED_LINE = byteArrayOf(0x0a)
        private val ESC_CUT_PAPER = byteArrayOf(0x1d, 0x56, 0x41, 0x10)
    }

    /**
     * Connect to the printer.
     */
    fun connectPrinter(): Boolean {
        return true
    }

    /**
     * Print a bill using a template from the database.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun printBillWithTemplate(bill: Bill, templateType: String = "BILL", target: String, ipAddress: String? = null): Boolean {
        val template = printTemplateDao.getDefaultTemplate(templateType) ?: return false
            
        val sections = printTemplateDao.getSectionsForTemplateSync(template.template_id)
        
        val outputStream = ByteArrayOutputStream()
        outputStream.write(ESC_INIT)
        
        val charWidth = if (template.paper_width_mm == 58) 32 else 48

        for (section in sections) {
            val lines = printTemplateDao.getLinesForSectionSync(section.section_id)
            
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                if (line.is_repeatable && section.section_type.uppercase() == "BODY") {
                    val start = i
                    while (i < lines.size && lines[i].is_repeatable) {
                        i++
                    }
                    val repeatableBlock = lines.subList(start, i)
                    
                    for (item in bill.items) {
                        for (rLine in repeatableBlock) {
                            outputStream.write(formatLine(rLine, charWidth, bill, item))
                        }
                    }
                } else {
                    outputStream.write(formatLine(line, charWidth, bill, null))
                    i++
                }
            }
//            outputStream.write(ESC_FEED_LINE)
        }
        
        outputStream.write(ESC_CUT_PAPER)
        val data = outputStream.toByteArray()
        
        return withContext(Dispatchers.IO) {
            try {
                if (target == "BLUETOOTH") {
                    var success = false
                    val mac = sessionManager.getBluetoothPrinter()
                    if (mac != null) {
                        printViaBluetoothMac(mac, data) { s, _ -> success = s }
                        success
                    } else false
                } else if (target == "TCP" && ipAddress != null) {
                    printViaTcp(ipAddress, 9100, data) { _, _ -> }
                    true 
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun formatLine(
        line: PrintTemplateLineEntity,
        charWidth: Int,
        bill: Bill,
        item: BillItem?
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        
        // 1. Font Name (ESC M n)
        when (line.font_name?.uppercase()) {
            "FONT_B", "FONTB", "SMALL" -> bos.write(byteArrayOf(0x1b, 0x4d, 0x01))
            else -> bos.write(byteArrayOf(0x1b, 0x4d, 0x00))
        }

        // 2. Font Size (GS ! n)
        val sizeInt = line.font_size.toIntOrNull() ?: 12
        val n = when {
            sizeInt >= 24 -> 0x11.toByte() // Double height & width
            sizeInt >= 16 -> 0x01.toByte() // Double height
            else -> 0x00.toByte()          // Normal
        }
        bos.write(byteArrayOf(0x1d, 0x21, n))

        // 3. Font Styles
        if (line.is_bold) bos.write(ESC_BOLD_ON) else bos.write(ESC_BOLD_OFF)
        if (line.is_underline) bos.write(ESC_UNDERLINE_ON) else bos.write(ESC_UNDERLINE_OFF)
        
        // 4. Alignment
        when (line.align_type.uppercase()) {
            "CENTER" -> bos.write(ESC_ALIGN_CENTER)
            "RIGHT" -> bos.write(ESC_ALIGN_RIGHT)
            else -> bos.write(ESC_ALIGN_LEFT)
        }

        // 5. Max Width Pct
        val effectiveWidth = if (line.max_width_pct != null && line.max_width_pct!! > 0) {
            (charWidth * line.max_width_pct!! / 100)
        } else {
            charWidth
        }

        val columns = printTemplateDao.getColumnsForLineSync(line.line_id)
        if (columns.isEmpty()) {
            val text = resolveValue(line.field_key, bill, item, effectiveWidth, line.display_text)
            bos.write(text.toByteArray(Charsets.UTF_8))
            bos.write(ESC_FEED_LINE)
        } else {
            val rowText = StringBuilder()
            
            for (column in columns.sortedBy { it.sort_order }) {
                val colWidth = (effectiveWidth * column.width_pct / 100)
                val value = resolveValue(column.field_key, bill, item, colWidth)
                
                val formattedValue = when (column.align_type.uppercase()) {
                    "RIGHT" -> value.padStart(colWidth)
                    "CENTER" -> {
                        val padding = (colWidth - value.length) / 2
                        " ".repeat(maxOf(0, padding)) + value.padEnd(colWidth - padding)
                    }
                    else -> value.padEnd(colWidth)
                }
                rowText.append(formattedValue.take(colWidth))
            }
            bos.write(rowText.toString().toByteArray(Charsets.UTF_8))
            bos.write(ESC_FEED_LINE)
        }
        
        // Reset styles
        bos.write(byteArrayOf(0x1d, 0x21, 0x00))
        bos.write(byteArrayOf(0x1b, 0x4d, 0x00))
        bos.write(ESC_BOLD_OFF)
        bos.write(ESC_UNDERLINE_OFF)
        
        return bos.toByteArray()
    }

    @SuppressLint("DefaultLocale")
    private fun resolveValue(key: String, bill: Bill, item: BillItem?, charWidth: Int, lineDisplayText: String? = null): String {
        val profile = sessionManager.getRestaurantProfile()
        val settings = sessionManager.getGeneralSetting()
        
        val cleanKey = key.uppercase().trim().replace(" ", "_")
        
        if (cleanKey == "TEXT" || cleanKey == "DISPLAY_TEXT" || cleanKey == "LABEL") {
            return lineDisplayText ?: ""
        }

        return when {
            // Business Details
            cleanKey == "BUSINESS_NAME" || cleanKey == "RESTAURANT_NAME" || cleanKey == "COMPANY_VALUE" -> profile?.company_name ?: ""
            cleanKey == "BUSINESS_ADDRESS" || cleanKey == "ADDRESS" -> "${profile?.address1 ?: ""} ${profile?.address2 ?: ""}".trim()
            cleanKey == "ADDRESS1" || cleanKey == "ADDRESS1_VALUE" -> profile?.address1 ?: ""
            cleanKey == "ADDRESS2" || cleanKey == "ADDRESS2_VALUE" -> profile?.address2 ?: ""
            cleanKey == "PLACE" || cleanKey == "PLACE_VALUE" -> profile?.place ?: ""
            cleanKey == "PINCODE" || cleanKey == "PINCODE_VALUE" -> profile?.pincode ?: ""
            cleanKey == "PHONE" || cleanKey == "CONTACT_NO" || cleanKey == "BUSINESS_PHONE" -> profile?.contact_no ?: ""
            cleanKey == "GST_NO" || cleanKey == "TAX_NO" || cleanKey == "BUSINESS_GSTIN" -> profile?.tax_no ?: ""
            
            // Bill Details
            cleanKey == "BILL_VALUE" -> bill.billNo
            cleanKey == "DATE_VALUE" -> bill.date
            cleanKey == "TIME_VALUE" -> bill.time
            cleanKey == "ORDER_VALUE" -> bill.orderNo
            cleanKey == "TABLE_VALUE" -> bill.tableNo
            cleanKey == "COUNTER_VALUE" -> bill.counter
            
            // Totals
            cleanKey == "SUB_TOTAL" -> String.format("%.2f", bill.subtotal)
            cleanKey == "TOTAL" -> String.format("%.2f", bill.total)
            cleanKey == "DISCOUNT" -> String.format("%.2f", bill.discount)
            cleanKey == "TAX_AMOUNT" -> String.format("%.2f", bill.items.sumOf { it.taxAmount })
            cleanKey == "RECEIVED_AMT" -> String.format("%.2f", bill.received_amt)
            cleanKey == "PENDING_AMT" -> String.format("%.2f", bill.pending_amt)
            
            // Customer Details
            cleanKey == "CUST_NAME" -> bill.custName
            cleanKey == "CUST_NO" -> bill.custNo
            cleanKey == "CUST_ADDRESS" -> bill.custAddress
            cleanKey == "CUST_GSTIN" -> bill.custGstin
            
            // Item Details (for Body section)
            cleanKey == "ITEM_VALUE" -> item?.itemName ?: ""
            cleanKey == "QTY_VALUE" -> item?.qty?.toString() ?: ""
            cleanKey == "RATE" || cleanKey == "PRICE_VALUE" -> String.format("%.2f", item?.price ?: 0.0)
            cleanKey == "AMOUNT" || cleanKey == "AMT_VALUE" -> String.format("%.2f", item?.amount ?: 0.0)
            cleanKey == "SN"  -> item?.sn?.toString() ?: ""
            
            // Footer & Utilities
            cleanKey == "FOOTER" || cleanKey == "BILL_FOOTER" || cleanKey == "THANK_VALUE" -> settings?.bill_footer ?: ""
            cleanKey == "SEPARATOR" -> "-".repeat(charWidth)
            cleanKey == "DOUBLE_SEPARATOR" -> "=".repeat(charWidth)
            
            key.startsWith("FIXED:") -> key.removePrefix("FIXED:")
            
            else -> lineDisplayText ?: key 
        }
    }

    /**
     * Print a KOT ticket to the kitchen printer with template.
     */
    fun printKot(kotData: KotData, template: ReceiptTemplate): Boolean {
        return true
    }

    /**
     * Print a KOT ticket to the kitchen printer (without template).
     */
    fun printKot(kotData: KOTRequest): Boolean {
        return true
    }

    /**
     * Print a bill with template.
     */
    fun printBill(billData: Bill, template: ReceiptTemplate): Boolean {
        return true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printViaBluetoothMac(macAddress: String, data: ByteArray, onResult: (success: Boolean, message: String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var socket: BluetoothSocket? = null
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter == null || !adapter.isEnabled) {
                    withContext(Dispatchers.Main) { onResult(false,"❌ Bluetooth not available or disabled") }
                    return@launch
                }

                val device: BluetoothDevice? = adapter.getRemoteDevice(macAddress)
                if (device == null) {
                    withContext(Dispatchers.Main) { onResult(false,"❌ Device not found for MAC: $macAddress") }
                    return@launch
                }

                if (adapter.isDiscovering) adapter.cancelDiscovery()

                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                socket = device.createRfcommSocketToServiceRecord(uuid)

                try {
                    socket.connect()
                } catch (e: IOException) {
                    try {
                        val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        socket = m.invoke(device, 1) as BluetoothSocket
                        socket.connect()
                    } catch (fallback: Exception) {
                        withContext(Dispatchers.Main) { onResult(false,"❌ Connection failed: ${fallback.message}") }
                        return@launch
                    }
                }

                socket.outputStream.use { out ->
                    out.write(data)
                    out.flush()
                }

                withContext(Dispatchers.Main) {
                    onResult(true,"✅ Print successful on ${device.name} (${device.address})")
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) { onResult(false,"❌ Permission denied: ${e.message}") }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onResult(false,"❌ I/O error: ${e.message}") }
            } finally {
                try { socket?.close() } catch (_: IOException) {}
            }
        }
    }

    fun printViaTcp(
        ip: String,
        port: Int = 9100,
        data: ByteArray,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        Thread {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 3000)
                val out: OutputStream = socket.getOutputStream()
                out.write(data)
                out.flush()
                out.close()
                socket.close()

                Handler(Looper.getMainLooper()).post {
                    onResult(true, "✅ Print sent to printer at $ip:$port")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    onResult(false, "❌ Print failed: ${e.message}")
                }
            }
        }.start()
    }

    @SuppressLint("ServiceCast")
    fun printViaUsb(context: Context, usbDevice: UsbDevice, data: ByteArray) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbInterface = usbDevice.getInterface(0)
        val endpoint = usbInterface.getEndpoint(0)
        val connection = usbManager.openDevice(usbDevice)

        connection?.claimInterface(usbInterface, true)
        connection?.bulkTransfer(endpoint, data, data.size, 1000)
        connection?.releaseInterface(usbInterface)
        connection?.close()
    }
}
