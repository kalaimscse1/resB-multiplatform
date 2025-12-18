package com.warriortech.resb.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("staff")
    val user: TblStaff
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)