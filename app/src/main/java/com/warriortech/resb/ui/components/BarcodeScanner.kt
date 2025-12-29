package com.warriortech.resb.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Color

@Composable
fun BarcodeScannerButton(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit,
    onError: (String) -> Unit = {},
    tint: Color = Color.White
) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        .build()

    val scanner = GmsBarcodeScanning.getClient(options)

    IconButton(
        onClick = {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        onBarcodeScanned(rawValue)
                    } else {
                        onError("Barcode is empty")
                    }
                }
                .addOnCanceledListener {
                    onError("Scan cancelled")
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Scan failed")
                }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "Scan Barcode",
            tint = tint
        )
    }
}
