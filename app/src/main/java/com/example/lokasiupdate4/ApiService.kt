package com.example.lokasiupdate4

import com.example.lokasiupdate4.login.UserModel
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("get_lokasi.php")
    fun getMarkers(@Query("user_id") userId: String?): Call<List<MarkerData>>

    @FormUrlEncoded
    @POST("login_service.php") // Sesuaikan dengan nama file PHP
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserModel>
}