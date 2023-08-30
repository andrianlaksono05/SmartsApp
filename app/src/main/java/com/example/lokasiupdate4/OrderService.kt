package com.example.lokasiupdate4

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OrderService {
    @POST("order.php")
    fun submitOrder(@Body order: Order): Call<Void>

    @Multipart
    @POST("upload_image.php")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        ): Call<Void>
}



//    @POST("ordercoba.php")
//    fun submitOrder(@Body order: Order): Call<Void>
//
//    @Multipart
//    @POST("ordercoba.php")
//    fun uploadImage(
//        @Part image: MultipartBody.Part,
//        ): Call<Void>
