package com.warriortech.resb.model

data class TblUnitConversionRequest(
    var unit_conv_id: Long = 0,
    var unit_id: Long,
    var item_id: Long,
    var conversion_no: Long,
    var is_active: Boolean = true
)

data class TblUnitConversionResponse(
    var unit_conv_id: Long,
    var unit: TblUnit,
    var item: TblMenuItemResponse,
    var conversion_no: Long,
    var is_active: Boolean
)
