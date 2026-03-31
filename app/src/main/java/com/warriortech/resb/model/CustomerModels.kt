package com.warriortech.resb.model

data class Customer(
    val customer_id: Long,
    val customer_name: String,
    val customer_phone: String,
    val customer_email: String? = null,
    val customer_address: String? = null,
    val created_date: String,
    val is_active: Boolean = true
)

data class CustomerSearchResponse(
    val success: Boolean,
    val message: String,
    val data: List<Customer>
)

data class CreateCustomerRequest(
    val customer_name: String,
    val customer_phone: String,
    val customer_email: String? = null,
    val customer_address: String? = null
)

data class CreateCustomerResponse(
    val success: Boolean,
    val message: String,
    val data: Customer?
)


data class TblCustomer(
    var customer_id: Long,
    var customer_name: String,
    var contact_no: String,
    var address: String,
    var email_address: String,
    var gst_no: String,
    var igst_status: Boolean,
    var is_active: Long
)

data class TblCustomerInfoResponse(
    var customer_info_id: Long = 0,
    var customer: TblCustomer,
    var address: String,
    var contact_no: String,
    var is_active: Boolean = true
)

data class TblCustomerInfoRequest(
    var customer_info_id: Long = 0,
    var customer_id: Long,
    var address: String,
    var contact_no: String,
    var is_active: Boolean = true
)