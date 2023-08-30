package com.example.lokasiupdate4.dashboard

import com.google.gson.annotations.SerializedName

data class TotalPriceResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("total")
    val total: Double
)
