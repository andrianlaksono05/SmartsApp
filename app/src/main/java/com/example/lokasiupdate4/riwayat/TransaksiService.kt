package com.example.lokasiupdate4.riwayat

import com.example.lokasiupdate4.dashboard.OrderCountResponse
import com.example.lokasiupdate4.dashboard.TotalPriceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TransaksiService {

    //Ambil Data rekap transaksi
    @GET("riwayat_transaksi.php")
    fun getTransactions(@Query("user_id") userId: String?): Call<List<TransaksiModel>>

    //ambil data total omset dari order
    @GET("get_total.php")
    fun getTotalPriceByUserId(@Query("user_id") userId: String): Call<TotalPriceResponse>

    // ambil data jumlah order yang dilakukan sales
    @GET("get_total_order.php")
    fun getOrderCountByUserId(
        @Query("user_id") userId: String
    ): Call<OrderCountResponse>

    // Ambil Data transaksi berdasarkan tanggal
    @GET("filter_riwayat_transaksi.php")
    fun getTransactionsByDate(
        @Query("user_id") userId: String?,
        @Query("tanggal") tanggal: String?
    ): Call<List<TransaksiModel>>

}