package com.example.lokasiupdate4.dashboard

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface dashboardService {
    @GET("user_info.php")
    fun getUserInfo(@Query("id") id: String): Call<UserInfoResponse>
}