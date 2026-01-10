package com.warriortech.resb.model

data class TblAuditingRequest(
    var slno: Long=0,
    var id:Long,
    var modify_date:String,
    var modify_time:String,
    var groups:String,
    var counter_id:Long,
    var user_id:Long,
    var created_date:String,
    var member:String,
    var member_id:String,
    var narration:String,
    var credit:Double,
    var debit:Double
)

data class TblAuditingResponse(
    var slno: Long=0,
    var id:Long,
    var modify_date:String,
    var modify_time:String,
    var groups:String,
    var counter:TblCounter,
    var user:TblStaff,
    var created_date:String,
    var member:String,
    var member_id:String,
    var narration:String,
    var credit:Double,
    var debit:Double
)