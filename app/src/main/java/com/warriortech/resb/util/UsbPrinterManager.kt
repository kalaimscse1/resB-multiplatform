package com.warriortech.resb.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ACTION_USB_PERMISSION = "com.warriortech.resb.USB_PERMISSION"

/**
 * Manages USB printer device discovery, permission requests, and printing.
 * Does NOT affect existing Bluetooth/TCP/POS print paths.
 */
class UsbPrinterManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _devices = MutableStateFlow<List<UsbDevice>>(emptyList())
    val devices: StateFlow<List<UsbDevice>> = _devices.asStateFlow()

    private val _permissionResult = MutableStateFlow<Pair<UsbDevice, Boolean>?>(null)
    val permissionResult: StateFlow<Pair<UsbDevice, Boolean>?> = _permissionResult.asStateFlow()

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (device != null) {
                    _permissionResult.value = device to granted
                    refreshDevices()
                }
            }
        }
    }

    fun registerReceiver() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(permissionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        refreshDevices()
    }

    fun unregisterReceiver() {
        try { context.unregisterReceiver(permissionReceiver) } catch (_: Exception) {}
    }

    /** Refresh the list of connected USB devices that have a bulk-OUT endpoint (printer-like). */
    fun refreshDevices() {
        _devices.value = usbManager.deviceList.values
            .filter { device -> hasBulkOutEndpoint(device) }
    }

    fun hasPermission(device: UsbDevice) = usbManager.hasPermission(device)

    fun requestPermission(device: UsbDevice) {
        val pi = PendingIntent.getBroadcast(
            context, 0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pi)
    }

    /**
     * Send raw ESC/POS bytes to the given USB device.
     * Returns true on success.
     */
    @SuppressLint("ServiceCast")
    fun print(device: UsbDevice, data: ByteArray): Boolean {
        if (!usbManager.hasPermission(device)) return false
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            for (j in 0 until usbInterface.endpointCount) {
                val ep = usbInterface.getEndpoint(j)
                if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    ep.direction == UsbConstants.USB_DIR_OUT
                ) {
                    val connection = usbManager.openDevice(device) ?: return false
                    return try {
                        connection.claimInterface(usbInterface, true)
                        val result = connection.bulkTransfer(ep, data, data.size, 3000)
                        connection.releaseInterface(usbInterface)
                        result >= 0
                    } catch (_: Exception) {
                        false
                    } finally {
                        connection.close()
                    }
                }
            }
        }
        return false
    }

    private fun hasBulkOutEndpoint(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    ep.direction == UsbConstants.USB_DIR_OUT
                ) return true
            }
        }
        return false
    }

    companion object {
        fun deviceLabel(device: UsbDevice): String {
            val name = device.productName?.takeIf { it.isNotBlank() }
                ?: device.manufacturerName?.takeIf { it.isNotBlank() }
                ?: "USB Device"
            return "$name (${device.vendorId}:${device.productId})"
        }
    }
}
