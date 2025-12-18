package com.warriortech.resb.model

data class User(
    val id: Long,
    val username: String,
    val password: String,  // Note: In a real app, this would be hashed and not stored in the model
    val name: String,
    val role: String,      // e.g., "manager", "waiter", "chef"
    val companyCode: String // Restaurant's unique company code for multi-tenant system
)

data class TblStaff(
    var staff_id: Long,
    var staff_name: String,
    var contact_no: String,
    var address: String,
    var user_name: String,
    var password: String,
    var role_id: Long,
    var role: String,
    var last_login: String,
    var is_block: Boolean,
    var counter_id: Long,
    var counter_name: String,
    var area_id: Long,
    var area_name: String,
    var commission: Double,
    var is_active: Long
)

data class staff(
    var staff_id: Long,
    var staff_name: String,
    var contact_no: String,
    var address: String,
    var user_name: String,
    var password: String,
    var role_id: Long,
    var role: String,
    var last_login: String,
    var is_block: Boolean,
    var counter_id: Long,
    var counter_name: String,
    var area_id: Long,
    var area_name: String,
    var is_active: Long
)

data class LoginRequest(
    val companyCode: String,
    val user_name: String,
    val password: String
)

data class TblStaffRequest(
    val staff_id: Long,
    val staff_name: String,
    val contact_no: String,
    val address: String,
    val user_name: String,
    val password: String,
    val role_id: Long,
    val last_login: String,
    val is_block: Boolean,
    val commission: Double,
    val counter_id: Long,
    val area_id: Long,
    val is_active: Long
)

data class TblCompanyMaster(
    var id: Long=0,
    var company_master_code:String,
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
    var install_date: String,
    var subscription_days:Long,
    var expiry_date:String,
    var is_block:Boolean=false,
    var is_active:Boolean=true
)
