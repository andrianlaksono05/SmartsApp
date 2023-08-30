package com.example.lokasiupdate4

data class MarkerData(
    val id: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val snippet: String,
    var isCheckIn: Boolean,
    val user_id: Any
)
