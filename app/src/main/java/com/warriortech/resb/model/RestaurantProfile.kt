package com.warriortech.resb.model

data class RestaurantProfile(
    var company_code: String,
    var company_name: String,
    var owner_name: String,
    var address1: String,
    var address2: String,
    var place: String,
    var pincode: String,
    var contact_no: String,
    var mail_id: String,
    var country: String,
    var state: String,
    var currency: String,
    var tax_no: String,
    var decimal_point: Long,
    var upi_id: String,
    var upi_name: String,
)
