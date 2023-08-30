package com.example.lokasiupdate4.login

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiServiceLogin {
    @FormUrlEncoded
    @POST("login_service.php") // Sesuaikan dengan nama file PHP yang Anda berikan
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserModel>
}