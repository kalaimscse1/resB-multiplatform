package com.warriortech.resb.model

import java.time.LocalDate

data class Registration(
    val id: Long,
    val company_master_code: String,
    val company_name: String,
    val owner_name: String,
    val address1: String,
    val address2: String,
    val place: String,
    val pincode: String,
    val contact_no: String,
    val mail_id: String,
    val country: String,
    val state: String,
    val year: String,
    val database_name: String,
    val order_plan: String,
    val install_date: String,
    val subscription_days: Long,
    val expiry_date: String,
    val is_block: Boolean,
    val is_active: Boolean
)

data class RegistrationRequest(
    val company_master_code: String,
    val company_name: String,
    val owner_name: String,
    val address1: String,
    val address2: String,
    val place: String,
    val pincode: String,
    val contact_no: String,
    val mail_id: String,
    val country: String,
    val state: String,
    val year: String,
    val database_name: String,
    val order_plan: String,
    val install_date: String,
    val subscription_days: Long,
    val expiry_date: String,
    val is_block: Boolean = false
)

data class RegistrationResponse(
    val success: Boolean,
    val message: String,
    val data: Registration?
)
