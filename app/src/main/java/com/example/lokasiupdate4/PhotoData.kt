package com.example.lokasiupdate4

import com.google.gson.annotations.SerializedName
import java.io.File

data class PhotoData(
    @SerializedName("image")
    val image: File
)

