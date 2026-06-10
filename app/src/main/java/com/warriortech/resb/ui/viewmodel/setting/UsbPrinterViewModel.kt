package com.warriortech.resb.ui.viewmodel.setting

import android.app.Application
import android.hardware.usb.UsbDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.util.UsbPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsbPrinterViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val usbManager = UsbPrinterManager(application)

    val devices: StateFlow<List<UsbDevice>> = usbManager.devices
    val permissionResult: StateFlow<Pair<UsbDevice, Boolean>?> = usbManager.permissionResult

    private val _printResult = MutableStateFlow<String?>(null)
    val printResult: StateFlow<String?> = _printResult.asStateFlow()

    init {
        usbManager.registerReceiver()
    }

    override fun onCleared() {
        super.onCleared()
        usbManager.unregisterReceiver()
    }

    fun refresh() = usbManager.refreshDevices()

    fun requestPermission(device: UsbDevice) = usbManager.requestPermission(device)

    fun hasPermission(device: UsbDevice) = usbManager.hasPermission(device)

    fun testPrint(device: UsbDevice) {
        viewModelScope.launch {
            val testData = buildTestPrintData()
            val success = usbManager.print(device, testData)
            _printResult.value = if (success) "✅ Test print sent to ${UsbPrinterManager.deviceLabel(device)}"
                                  else "❌ Print failed — check USB connection and permission"
        }
    }

    fun clearResult() { _printResult.value = null }

    private fun buildTestPrintData(): ByteArray {
        val esc = 0x1b.toByte()
        val init = byteArrayOf(esc, 0x40)                          // ESC @ — init
        val center = byteArrayOf(esc, 0x61, 0x01)                  // ESC a 1 — center
        val bold = byteArrayOf(esc, 0x45, 0x01)                    // ESC E 1 — bold on
        val boldOff = byteArrayOf(esc, 0x45, 0x00)
        val feed = byteArrayOf(0x0a)
        val cut = byteArrayOf(0x1d, 0x56, 0x41, 0x10)             // GS V A — cut

        return init +
            center + bold + "USB PRINTER TEST\n".toByteArray() + boldOff +
            "----------------------------\n".toByteArray() +
            "USB printing is working!\n".toByteArray() +
            "resAPP POS System\n".toByteArray() +
            "----------------------------\n".toByteArray() +
            feed + feed + cut
    }
}

private operator fun ByteArray.plus(other: ByteArray): ByteArray {
    val result = ByteArray(this.size + other.size)
    this.copyInto(result)
    other.copyInto(result, this.size)
    return result
}
