package com.warriortech.resb.model

data class TblBranchRequest(
    var branch_code:String,
    var companyMasterCode:String,
    var company_name:String,
    var owner_name:String,
    var address1:String,
    var address2:String,
    var place:String,
    var pincode:String,
    var contact_no:String,
    var mail_id:String,
    var country:String,
    var state:String,
    var year:String,
    var database_name:String,
    var order_plan:String,
    var install_date:String,
    var subscription_days:Long,
    var expiry_date:String,
    var is_block:Boolean = false
)

data class TblBranchResponse(
    var branch_code:String,
    var companyMaster:TblCompanyMaster,
    var company_name:String,
    var owner_name:String,
    var address1:String,
    var address2:String,
    var place:String,
    var pincode:String,
    var contact_no:String,
    var mail_id:String,
    var country:String,
    var state:String,
    var year:String,
    var database_name:String,
    var order_plan:String,
    var install_date:String,
    var subscription_days:Long,
    var expiry_date:String,
    var is_block:Boolean
)