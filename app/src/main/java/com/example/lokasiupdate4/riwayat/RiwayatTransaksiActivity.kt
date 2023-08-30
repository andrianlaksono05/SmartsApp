package com.example.lokasiupdate4.riwayat

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lokasiupdate4.ApiClient
import com.example.lokasiupdate4.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransaksiAdapter
    private lateinit var btnFilter: Button
    private lateinit var datePicker: DatePicker

    // Nama Shared Preferences untuk session login
    private val PREF_NAME = "login_session"
    private val KEY_USER_ID = "user_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_transaksi)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransaksiAdapter(this)
        recyclerView.adapter = adapter

        btnFilter = findViewById(R.id.btnFilter)

        btnFilter.setOnClickListener {
            showDatePickerDialog()
        }

        fetchTransaksiData()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Tangkap tanggal yang dipilih
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                // Format tanggal menjadi format yang sesuai dengan kebutuhan
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

                // Lakukan filter berdasarkan tanggal yang dipilih
                filterByDate(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun fetchTransaksiData() {
        // Dapatkan user_id dari Shared Preferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(KEY_USER_ID, "")

        val call = ApiClient.getTransaksi().getTransactions(userId)

        call.enqueue(object : Callback<List<TransaksiModel>> {
            override fun onResponse(
                call: Call<List<TransaksiModel>>,
                response: Response<List<TransaksiModel>>
            ) {
                if (response.isSuccessful) {
                    val transactionList = response.body()
                    if (transactionList != null) {
                        adapter.setTransactionList(transactionList)


                    }
                } else {
                    // Handle error response
                }
            }

            override fun onFailure(call: Call<List<TransaksiModel>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun filterByDate(date: String) {
        // Lakukan pemanggilan API dengan filter tanggal
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(KEY_USER_ID, "")

        val call = ApiClient.getTransaksi().getTransactionsByDate(userId, date)

        call.enqueue(object : Callback<List<TransaksiModel>> {
            override fun onResponse(
                call: Call<List<TransaksiModel>>,
                response: Response<List<TransaksiModel>>
            ) {
                if (response.isSuccessful) {
                    val transactionList = response.body()
                    if (transactionList != null) {
                        adapter.setTransactionList(transactionList)
                    }
                } else {
                    // Handle error response
                }
            }

            override fun onFailure(call: Call<List<TransaksiModel>>, t: Throwable) {
                // Handle failure
            }
        })
    }
}

