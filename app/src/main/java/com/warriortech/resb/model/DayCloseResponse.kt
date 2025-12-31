package com.warriortech.resb.model

import java.time.LocalDateTime

data class DayCloseResponse(
    var slno: Long=0,
    var s_date:String,
    var is_closed:Boolean=false,
    var login_time :String="",
    var staff:TblStaff,
    var opening_float:Double=0.0,
    var total_sales:Double=0.0,
    var discount_total:Double=0.0,
    var taxable_amt:Double=0.0,
    var service_charge:Double=0.0,
    var net_sales:Double=0.0,
    var cash_excepted:Double=0.0,
    var cash_counted:Double=0.0,
    var created_by:String="",
    var created_at: LocalDateTime = LocalDateTime.now()
)