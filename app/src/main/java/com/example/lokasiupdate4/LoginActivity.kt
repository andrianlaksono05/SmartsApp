package com.example.lokasiupdate4

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.lokasiupdate4.ApiClient
import com.example.lokasiupdate4.dashboard.DashboardActivity
import com.example.lokasiupdate4.login.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    // Nama Shared Preferences untuk session login
    private val PREF_NAME = "login_session"
    private val KEY_USERNAME = "email"
    private val KEY_USER_ID = "user_id" // Tambahkan key untuk menyimpan user_id

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // Cek apakah pengguna sudah login sebelumnya
        if (isLoggedIn()) {
            startDashboardActivity()
            finish()
            return
        }

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString()
            val password = etPassword.text.toString()

            // Panggil fungsi login dari UserService di ApiClient
            ApiClient.getUserService().loginUser(email, password)
                .enqueue(object : Callback<UserModel> {
                    override fun onResponse(
                        call: Call<UserModel>,
                        response: Response<UserModel>
                    ) {
                        if (response.isSuccessful) {
                            val userModel = response.body()
                            if (userModel?.status == "success") {
                                // Login sukses
                                Toast.makeText(
                                    this@LoginActivity,
                                    userModel.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Simpan username dan user_id ke Shared Preferences sebagai session login
                                saveLoginSession(email, userModel.userId.toString())

                                // Pindah ke DashboardActivity
                                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Login gagal
                                Toast.makeText(
                                    this@LoginActivity,
                                    userModel?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Response tidak sukses
                            Toast.makeText(
                                this@LoginActivity,
                                "Error: " + response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UserModel>, t: Throwable) {
                        // Gagal melakukan request
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        return !username.isNullOrEmpty() && !userId.isNullOrEmpty()
    }

    private fun startDashboardActivity() {
        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
        startActivity(intent)
    }

    // Fungsi untuk menyimpan username dan user_id ke Shared Preferences sebagai session login
    private fun saveLoginSession(email: String, userId: String) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, email)
        editor.putString(KEY_USER_ID, userId) // Simpan user_id ke Shared Preferences
        editor.apply()
    }
}
