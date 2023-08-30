package com.example.lokasiupdate4.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.lokasiupdate4.ApiClient
import com.example.lokasiupdate4.LoginActivity
import com.example.lokasiupdate4.MapsActivity
import com.example.lokasiupdate4.R
import com.example.lokasiupdate4.gaji.GajiActivity
import com.example.lokasiupdate4.login.UserModel
import com.example.lokasiupdate4.riwayat.RiwayatTransaksiActivity
import com.example.lokasiupdate4.riwayat.TransaksiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*


class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvOrderCount: TextView


    private val updateInterval: Long = 2000 // Interval pembaruan total price dalam milidetik
    private val handler = Handler()
    private var isActivityRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //activity dashboard lokasi
        val btn_lokasi = findViewById<Button>(R.id.button_maps)
        btn_lokasi.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)

            // Memulai Activity kedua
            startActivity(intent)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }

        val btn_riwayat = findViewById<Button>(R.id.button_riwayat)
        btn_riwayat.setOnClickListener {
            val intent = Intent(this, RiwayatTransaksiActivity::class.java)

            // Memulai Activity kedua
            startActivity(intent)
        }

        // Memanggil WhatsApp
        val btnWhatsApp = findViewById<Button>(R.id.button_wa)
        btnWhatsApp.setOnClickListener {
            val phoneNumber = "+6282331018479" // Nomor telepon yang dituju
            openWhatsApp(phoneNumber)
        }

        //gaji
        val btnGaji = findViewById<Button>(R.id.button_gaji)
        btnGaji.setOnClickListener {
            val intent = Intent(this, GajiActivity::class.java)

            // Memulai Activity kedua
            startActivity(intent)
        }

        tvUserName = findViewById(R.id.tv_welcome)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        tvOrderCount = findViewById(R.id.tvOrderCount)




        // Mengambil user_id dari session
        val sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")


        // Panggil API Endpoint untuk mendapatkan data profil sales
        if (userId != null) {

            ApiClient.getinfo().getUserInfo(userId)
                .enqueue(object : Callback<UserInfoResponse> {
                    override fun onResponse(
                        call: Call<UserInfoResponse>,
                        response: Response<UserInfoResponse>
                    ) {
                        if (response.isSuccessful) {
                            val userModel = response.body()
                            if (userModel?.status == "success") {
                                // Mendapatkan data profil sales
                                val userName = userModel.name

                                // Tampilkan pesan selamat datang dengan nama sales
                                val welcomeMessage = "$userName!"
                                tvUserName.text = welcomeMessage
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })




        }



    }

    private fun logout() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Logout")
        alertDialogBuilder.setMessage("Apakah Anda yakin ingin logout?")
        alertDialogBuilder.setPositiveButton("Ya") { dialog, which ->
            // Hapus sesi login
            clearLoginSession()

            // Pindah ke LoginActivity
            val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialogBuilder.setNegativeButton("Batal", null)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun clearLoginSession() {
        val sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        startTotalPriceUpdate()
    }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun startTotalPriceUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                val sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("user_id", "")

                if (userId != null) {
                    fetchTotalPrice(userId)
                }

                if (isActivityRunning) {
                    handler.postDelayed(this, updateInterval)
                }
            }
        })
    }

    // Get Data Total Transaksi Sales Realtime
    private fun fetchTotalPrice(userId: String) {
        // Panggil API Endpoint untuk mendapatkan total price
        ApiClient.gettotaltransaksi().getTotalPriceByUserId(userId)
            .enqueue(object : Callback<TotalPriceResponse> {
                override fun onResponse(
                    call: Call<TotalPriceResponse>,
                    response: Response<TotalPriceResponse>
                ) {
                    if (response.isSuccessful) {
                        val totalPriceResponse = response.body()
                        if (totalPriceResponse?.status == "success") {
                            val totalPrice = totalPriceResponse.total

                            val localeID = Locale("in", "ID")
                            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
                            val totalTransaksiRupiah = formatRupiah.format(totalPrice)

                            // Tampilkan total price
                            val totalPriceMessage = "$totalTransaksiRupiah"
                            tvTotalPrice.text = totalPriceMessage

                        }
                    }
                }

                override fun onFailure(call: Call<TotalPriceResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to retrieve total price",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        ApiClient.gettotaltransaksi().getOrderCountByUserId(userId)
            .enqueue(object : Callback<OrderCountResponse> {
                override fun onResponse(
                    call: Call<OrderCountResponse>,
                    response: Response<OrderCountResponse>
                ) {
                    if (response.isSuccessful) {
                        val orderCountResponse = response.body()
                        if (orderCountResponse?.status == "success") {
                            val orderCount = orderCountResponse.count
                            val orderCountMessage = "$orderCount Pengiriman"
                            tvOrderCount.text = orderCountMessage
                        }
                    }
                }

                override fun onFailure(call: Call<OrderCountResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to retrieve order count",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }


    private fun openWhatsApp(number: String) {
        val message = "Tolong Aktifkan Marker Lokasi Saya"
        val url = "https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
    }

    // ...

