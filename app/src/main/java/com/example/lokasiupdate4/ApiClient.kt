package com.example.lokasiupdate4

import com.example.lokasiupdate4.dashboard.dashboardService
import com.example.lokasiupdate4.gaji.GajiService
import com.example.lokasiupdate4.login.ApiServiceLogin
import com.example.lokasiupdate4.riwayat.TransaksiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object ApiClient {
    private const val BASE_URL = "http://192.168.43.20/tes_retrofit/"

    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getOrderService(): OrderService {
        return getClient().create(OrderService::class.java)
    }

    fun getUserService(): ApiService {
        return getClient().create(ApiService::class.java)
    }

    fun getTransaksi(): TransaksiService{
        return getClient().create(TransaksiService::class.java)
    }

    fun getinfo(): dashboardService{
        return getClient().create(dashboardService::class.java)
    }

    fun gettotaltransaksi(): TransaksiService{
        return getClient().create(TransaksiService::class.java)
    }

    fun getTotalGaji(): GajiService{
        return getClient().create(GajiService::class.java)
    }


}

