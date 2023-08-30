package com.example.lokasiupdate4.riwayat

import com.example.lokasiupdate4.PhotoData

data class TransaksiModel(
    val id_order: String,
    val tokoName: String,
    val salesName: String,
    val quantity: Int,
    val totalPrice: Double,
    val waktu: String,
    val photoUrl: String?
)
