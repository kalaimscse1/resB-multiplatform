package com.warriortech.resb.model

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)