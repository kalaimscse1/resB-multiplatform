package com.warriortech.resb.model

data class TblUnitConversionRequest(
    var unit_conv_id: Long = 0,
    var unit_id: Long,
    var base_item_id: Long,
    var consume_item_id: Long,
    var conversion_no: Double,
    var is_active: Boolean = true
)

data class TblUnitConversionResponse(
    var unit_conv_id: Long,
    var unit: TblUnit,
    var base_item: TblMenuItemResponse,
    var consume_item: TblMenuItemResponse,
    var conversion_no: Double,
    var is_active: Boolean
)


data class TblTmpItemMasterRequest(
    var tmp_item_master_id: Long=0,
    var order_id:String,
    var item_id:Long,
    var tmp_qty:Double,
    var is_active:Long=1
)

data class TblTmpItemMasterResponse(
    var tmp_item_master_id: Long=0,
    var order_id:String,
    var item:TblMenuItemResponse,
    var tmp_qty:Double,
    var is_active:Long=1
)