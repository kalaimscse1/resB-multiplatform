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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import com.senraise.printer.SrPrinter

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
        
        // Mutex to ensure only one Bluetooth print job happens at a time across all instances
        private val bluetoothMutex = Mutex()
        
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
                            outputStream.write(formatLine(rLine, charWidth, bill, null, item, null))
                        }
                    }
                } else {
                    outputStream.write(formatLine(line, charWidth, bill, null, null, null))
                    i++
                }
            }
        }
        
        outputStream.write(ESC_CUT_PAPER)
        val data = outputStream.toByteArray()
        
        return sendDataToPrinter(target, mac = sessionManager.getBluetoothPrinter(), ipAddress = ipAddress, data = data)
    }

    /**
     * Print a KOT using a template from the database.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun printKotWithTemplate(kot: KOTRequest, target: String, ipAddress: String? = null): Boolean {
        val template = printTemplateDao.getDefaultTemplate("KOT") ?: return false
            
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
                    
                    for (item in kot.items) {
                        for (rLine in repeatableBlock) {
                            outputStream.write(formatLine(rLine, charWidth, null, kot, null, item))
                        }
                    }
                } else {
                    outputStream.write(formatLine(line, charWidth, null, kot, null, null))
                    i++
                }
            }
        }
        
        outputStream.write(ESC_CUT_PAPER)
        val data = outputStream.toByteArray()
        
        return sendDataToPrinter(target, mac = sessionManager.getBluetoothPrinter(), ipAddress = ipAddress, data = data)
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun sendDataToPrinter(target: String, mac: String?, ipAddress: String?, data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                when (target.uppercase()) {
                    "BT", "BLUETOOTH" -> {
                        if (mac != null) {
                            var success = false
                            printViaBluetoothMacSync(mac, data) { s, _ -> success = s }
                            success
                        } else false
                    }
                    "TCP" -> {
                        if (ipAddress != null) {
                            printViaTcpSync(ipAddress, 9100, data)
                        } else false
                    }
                    "USB" -> {
                        // Auto-detect connected USB printer — no UsbDevice arg needed
                        printViaUsbSync(context, data)
                    }
                    "INBUILT", "POS" -> {
                        printViaInBuilt(data)
                    }
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun formatLine(
        line: PrintTemplateLineEntity,
        charWidth: Int,
        bill: Bill?,
        kot: KOTRequest?,
        billItem: BillItem?,
        kotItem: KOTItem?
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
        val effectiveWidth = if (line.max_width_pct != null && line.max_width_pct > 0) {
            (charWidth * line.max_width_pct / 100)
        } else {
            charWidth
        }

        val columns = printTemplateDao.getColumnsForLineSync(line.line_id)
        if (columns.isEmpty()) {
            val text = resolveValue(line.field_key, bill, kot, billItem, kotItem, charWidth, line.display_text)
            bos.write(text.toByteArray(Charsets.UTF_8))
            bos.write(ESC_FEED_LINE)
        } else {
            val rowText = StringBuilder()
            
            for (column in columns.sortedBy { it.sort_order }) {
                val colWidth = (charWidth * column.width_pct / 100)
                val value = resolveValue(column.field_key, bill, kot, billItem, kotItem, colWidth)
                
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
    private fun resolveValue(
        key: String, 
        bill: Bill?, 
        kot: KOTRequest?, 
        billItem: BillItem?, 
        kotItem: KOTItem?, 
        charWidth: Int, 
        lineDisplayText: String? = null
    ): String {
        val profile = sessionManager.getRestaurantProfile()
        val settings = sessionManager.getGeneralSetting()
        
        val cleanKey = key.uppercase().trim()
        
        if (cleanKey == "TEXT" || cleanKey == "DISPLAY_TEXT" || cleanKey == "LABEL") {
            return lineDisplayText ?: ""
        }

        return when {
            // Business Details
            cleanKey == "BUSINESS NAME" || cleanKey == "RESTAURANT NAME" || cleanKey == "COMPANY VALUE" -> profile?.company_name ?: ""
            cleanKey == "BUSINESS ADDRESS" -> "${profile?.address1 ?: ""} ${profile?.address2 ?: ""}".trim()
            cleanKey == "ADDRESS1 VALUE" -> profile?.address1 ?: ""
            cleanKey == "ADDRESS2 VALUE" -> profile?.address2 ?: ""
            cleanKey == "PLACE VALUE" -> profile?.place ?: ""
            cleanKey == "PINCODE VALUE" -> profile?.pincode ?: ""
            cleanKey == "PHONE" || cleanKey == "CONTACT NO" || cleanKey == "BUSINESS PHONE" -> profile?.contact_no ?: ""
            cleanKey == "GST NO" || cleanKey == "TAX NO" || cleanKey == "BUSINESS GSTIN" -> profile?.tax_no ?: ""
            
            // Common Details
            cleanKey == "DATE VALUE" -> bill?.date ?: kot?.orderCreatedAt?.take(10) ?: ""
            cleanKey == "TIME VALUE" -> bill?.time ?: kot?.orderCreatedAt?.drop(11) ?: ""
            cleanKey == "TABLE VALUE" -> bill?.tableNo ?: kot?.tableNumber ?: ""
            
            // Bill Specific
            cleanKey == "BILL VALUE" -> bill?.billNo ?: ""
            cleanKey == "ORDER VALUE" -> bill?.orderNo ?: kot?.orderId ?: ""
            cleanKey == "COUNTER VALUE" -> bill?.counter ?: ""
            
            // KOT Specific
            cleanKey == "KOT" -> ("KOT-" + kot?.kotId?.toString())
            cleanKey == "WAITER VALUE" -> kot?.waiterName ?: ""
            cleanKey == "KOT TYPE" -> kot?.kottype?:""
            
            // Totals (Bill only)
            cleanKey == "SUB TOTAL" -> if (bill != null) String.format("%.2f", bill.subtotal) else ""
            cleanKey == "TOTAL" -> if (bill != null) String.format("%.2f", bill.total) else ""
            cleanKey == "DISCOUNT" -> if (bill != null) String.format("%.2f", bill.discount) else ""
            cleanKey == "TAX AMOUNT" -> bill?.items?.sumOf { it.taxAmount }?.let { String.format("%.2f", it) } ?: ""
            cleanKey == "RECEIVED AMT" -> if (bill != null) String.format("%.2f", bill.received_amt) else ""
            cleanKey == "PENDING AMT" -> if (bill != null) String.format("%.2f", bill.pending_amt) else ""
            
            // Customer Details (Bill only)
            cleanKey == "CUST NAME" -> bill?.custName ?: ""
            cleanKey == "CUST NO" -> bill?.custNo ?: ""
            cleanKey == "CUST ADDRESS" -> bill?.custAddress ?: ""
            cleanKey == "CUST GSTIN" -> bill?.custGstin ?: ""
            
            // Item Details (Body section)
            cleanKey == "ITEM VALUE"  -> billItem?.itemName ?: kotItem?.name ?: ""
            cleanKey == "QTY VALUE" -> billItem?.qty?.toString() ?: kotItem?.quantity?.toString() ?: ""
            cleanKey == "RATE" || cleanKey == "PRICE VALUE" -> billItem?.price?.let { String.format("%.2f", it) } ?: ""
            cleanKey == "AMOUNT" || cleanKey == "AMT VALUE" -> billItem?.amount?.let { String.format("%.2f", it) } ?: ""
            cleanKey == "SN" -> billItem?.sn?.toString() ?: ""
            cleanKey == "CATEGORY" -> kotItem?.category ?: ""
            cleanKey == "NOTES" || cleanKey == "ADDONS" -> kotItem?.addOn?.joinToString(", ") ?: ""
            
            // Footer & Utilities
            cleanKey == "FOOTER" || cleanKey == "BILL FOOTER" || cleanKey == "THANK VALUE" -> settings?.bill_footer ?: ""
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
    suspend fun printViaBluetoothMacSync(macAddress: String, data: ByteArray, onResult: (success: Boolean, message: String) -> Unit) {
        bluetoothMutex.withLock {
            withContext(Dispatchers.IO) {
                var socket: BluetoothSocket? = null
                try {
                    val adapter = BluetoothAdapter.getDefaultAdapter()
                    if (adapter == null || !adapter.isEnabled) {
                        withContext(Dispatchers.Main) { onResult(false,"❌ Bluetooth not available or disabled") }
                        return@withContext
                    }

                    val device: BluetoothDevice? = adapter.getRemoteDevice(macAddress)
                    if (device == null) {
                        withContext(Dispatchers.Main) { onResult(false,"❌ Device not found for MAC: $macAddress") }
                        return@withContext
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
                            return@withContext
                        }
                    }

                    val out = socket.outputStream
                    out.write(data)
                    out.flush()
                    
                    // Crucial: Wait for the printer to process the buffer before closing
                    delay(800)

                    withContext(Dispatchers.Main) {
                        onResult(true,"✅ Print successful on ${device.name} (${device.address})")
                    }
                } catch (e: SecurityException) {
                    withContext(Dispatchers.Main) { onResult(false,"❌ Permission denied: ${e.message}") }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) { onResult(false,"❌ I/O error: ${e.message}") }
                } finally {
                    try { socket?.close() } catch (_: IOException) {}
                    // Cooldown delay for the printer hardware
                    delay(300)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printViaBluetoothMac(macAddress: String, data: ByteArray, onResult: (success: Boolean, message: String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            printViaBluetoothMacSync(macAddress, data, onResult)
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

    private fun printViaTcpSync(
        ip: String,
        port: Int = 9100,
        data: ByteArray
    ): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 3000)
            val out: OutputStream = socket.getOutputStream()
            out.write(data)
            out.flush()
            out.close()
            socket.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Print to a specific USB device (existing API — scans for correct bulk-OUT endpoint).
     */
    @SuppressLint("ServiceCast")
    fun printViaUsb(context: Context, usbDevice: UsbDevice, data: ByteArray) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (!usbManager.hasPermission(usbDevice)) {
            requestUsbPermissionsIfNeeded(context)
            return
        }
        for (i in 0 until usbDevice.interfaceCount) {
            val usbInterface = usbDevice.getInterface(i)
            for (j in 0 until usbInterface.endpointCount) {
                val ep = usbInterface.getEndpoint(j)
                if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT
                ) {
                    val connection = usbManager.openDevice(usbDevice) ?: return
                    var claimed = false
                    try {
                        connection.claimInterface(usbInterface, true)
                        claimed = true
                        connection.bulkTransfer(ep, data, data.size, 3000)
                    } catch (_: Exception) {
                    } finally {
                        if (claimed) try { connection.releaseInterface(usbInterface) } catch (_: Exception) {}
                        connection.close()
                    }
                    return
                }
            }
        }
    }

    /**
     * Auto-detect the first connected USB printer and send data.
     * Called by repositories with (context, bytes) — no UsbDevice required.
     * Returns true if data was sent successfully.
     */
    @SuppressLint("ServiceCast")
    fun printViaUsb(context: Context, data: ByteArray): Boolean {
        return printViaUsbSync(context, data)
    }

    /**
     * Blocking USB auto-detect print. Finds the first device that has a bulk-OUT
     * endpoint (standard for ESC/POS thermal printers) and sends the data.
     * If a printer-like device is found but has no permission, requests it automatically.
     */
    @SuppressLint("ServiceCast")
    private fun printViaUsbSync(context: Context, data: ByteArray): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList

        if (deviceList.isEmpty()) return false

        var foundPrinterWithoutPermission = false
        for ((_, device) in deviceList) {
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                for (j in 0 until usbInterface.endpointCount) {
                    val ep = usbInterface.getEndpoint(j)
                    if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK &&
                        ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT
                    ) {
                        if (!usbManager.hasPermission(device)) {
                            foundPrinterWithoutPermission = true
                            continue
                        }
                        val connection = usbManager.openDevice(device) ?: continue
                        var claimed = false
                        return try {
                            connection.claimInterface(usbInterface, true)
                            claimed = true
                            val transferred = connection.bulkTransfer(ep, data, data.size, 3000)
                            transferred >= 0
                        } catch (_: Exception) {
                            false
                        } finally {
                            if (claimed) try { connection.releaseInterface(usbInterface) } catch (_: Exception) {}
                            connection.close()
                        }
                    }
                }
            }
        }

        if (foundPrinterWithoutPermission) {
            requestUsbPermissionsIfNeeded(context)
        }
        return false
    }

    /**
     * Request USB permission for all connected devices that look like printers.
     * Call this from the UI layer on startup or from the settings screen.
     */
    @SuppressLint("ServiceCast")
    fun requestUsbPermissionsIfNeeded(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val intent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            android.content.Intent("com.warriortech.resb.USB_PERMISSION"),
            android.app.PendingIntent.FLAG_IMMUTABLE
        )
        for ((_, device) in usbManager.deviceList) {
            if (!usbManager.hasPermission(device)) {
                usbManager.requestPermission(device, intent)
            }
        }
    }

    fun printViaInBuilt(data: ByteArray): Boolean {
        return try {
            SrPrinter.getInstance(context).printEpson(data)
            true
        } catch (e: Exception) {
            false
        }
    }
}
