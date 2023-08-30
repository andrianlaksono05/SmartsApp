package com.example.lokasiupdate4.gaji

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GajiService {
    @GET("get_gaji.php")
    fun getGaji(@Query("user_id") userId: String?): Call<List<GajiModel>>

    // Ambil Data transaksi berdasarkan tanggal
    @GET("get_gaji_filter.php")
    fun getGajiByBulan(
        @Query("user_id") userId: String?,
        @Query("bulan") selectedBulan: String?,
        @Query("tahun") selectedTahun: String,
    ): Call<List<GajiModel>>
}