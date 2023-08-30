package com.example.lokasiupdate4.dashboard

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("name")
    val name: String
)


