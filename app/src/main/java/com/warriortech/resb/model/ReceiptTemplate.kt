package com.warriortech.resb.model

data class ReceiptTemplate(
    val id: String,
    val name: String,
    val type: ReceiptType,
    val headerSettings: HeaderSettings,
    val bodySettings: BodySettings,
    val footerSettings: FooterSettings,
    val paperSettings: PaperSettings,
    val isDefault: Boolean = false
)

data class HeaderSettings(
    val showLogo: Boolean = true,
    val businessName: String = "",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val fontSize: Int = 16,
    val fontWeight: FontWeights = FontWeights.BOLD,
    val textAlign: TextAligns = TextAligns.CENTER
)

data class BodySettings(
    val showItemDetails: Boolean = true,
    val showQuantity: Boolean = true,
    val showPrice: Boolean = true,
    val showTotal: Boolean = true,
    val fontSize: Int = 12,
    val fontWeight: FontWeights = FontWeights.NORMAL,
    val showBorders: Boolean = true,
    val lineSpacing: Int = 2
)

data class FooterSettings(
    val showThankYou: Boolean = true,
    val customMessage: String = "",
    val showDateTime: Boolean = true,
    val fontSize: Int = 10,
    val fontWeight: FontWeights = FontWeights.NORMAL,
    val textAlign: TextAligns = TextAligns.CENTER
)

data class PaperSettings(
    val paperSize: PaperSize = PaperSize.RECEIPT_80MM,
    val margins: Margins = Margins(5, 5, 5, 5),
    val characterWidth: Int = 32
)

data class Margins(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int
)

enum class ReceiptType {
    KOT,
    BILL
}

enum class FontWeights {
    NORMAL,
    BOLD,
    LIGHT
}

enum class TextAligns {
    LEFT,
    CENTER,
    RIGHT
}

enum class PaperSize(val width: Int, val displayName: String) {
    RECEIPT_58MM(32, "58mm Receipt"),
    RECEIPT_80MM(48, "80mm Receipt"),
    A4(72, "A4 Paper")
}
