package com.example.lokasiupdate4.gaji

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import com.example.lokasiupdate4.ApiClient
import com.example.lokasiupdate4.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class GajiActivity : AppCompatActivity() {
    private lateinit var gajiAdapter: GajiAdapter

    // Nama Shared Preferences untuk session login
    private val PREF_NAME = "login_session"
    private val KEY_USER_ID = "user_id"

    private var selectedBulan: String? = null
    private var selectedTahun: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gaji)

        // Inisialisasi komponen tampilan
        val gajiContainer: LinearLayout = findViewById(R.id.gajiContainer)
        val filterButton: Button = findViewById(R.id.filterButton)


        // Dapatkan user_id dari Shared Preferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(KEY_USER_ID, "")

        // Mengambil data gaji dari API menggunakan Retrofit
        loadGajiData(userId)

        filterButton.setOnClickListener {
            showMonthYearPickerDialog(userId)
        }
    }

    private fun loadGajiData(userId: String?) {
        val call = ApiClient.getTotalGaji().getGaji(userId!!)
        val gajiContainer: LinearLayout = findViewById(R.id.gajiContainer)

        call.enqueue(object : Callback<List<GajiModel>> {
            override fun onResponse(call: Call<List<GajiModel>>, response: Response<List<GajiModel>>) {
                if (response.isSuccessful) {
                    val gajiList = response.body()
                    if (gajiList != null && gajiList.isNotEmpty()) {
                        // Buat adapter dan tambahkan data gaji pada komponen tampilan
                        gajiAdapter = GajiAdapter(gajiList)
                        for (i in 0 until gajiAdapter.count) {
                            val view = gajiAdapter.getView(i, null, gajiContainer)
                            gajiContainer.addView(view)
                        }
                    }
                } else {
                    // Tidak dapat mengambil data gaji, tangani kesalahan di sini
                }
            }

            override fun onFailure(call: Call<List<GajiModel>>, t: Throwable) {
                // Terjadi kesalahan jaringan, tangani kesalahan di sini
            }
        })
    }


    private fun filterGajiData(userId: String, bulan: String, tahun: String) {
        val call = ApiClient.getTotalGaji().getGajiByBulan(userId, bulan, tahun)
        val gajiContainer: LinearLayout = findViewById(R.id.gajiContainer)

        call.enqueue(object : Callback<List<GajiModel>> {
            override fun onResponse(call: Call<List<GajiModel>>, response: Response<List<GajiModel>>) {
                if (response.isSuccessful) {
                    val gajiList = response.body()
                    if (gajiList != null && gajiList.isNotEmpty()) {
                        // Hapus data gaji sebelumnya dari komponen tampilan
                        gajiContainer.removeAllViews()

                        // Buat adapter dan tambahkan data gaji pada komponen tampilan
                        val gajiAdapter = GajiAdapter(gajiList)
                        for (i in 0 until gajiAdapter.count) {
                            val view = gajiAdapter.getView(i, null, gajiContainer)
                            gajiContainer.addView(view)
                        }
                    } else {
                        Toast.makeText(this@GajiActivity, "No data found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GajiActivity, "Failed to get data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<GajiModel>>, t: Throwable) {
                Toast.makeText(this@GajiActivity, "Error: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showMonthYearPickerDialog(userId: String?) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Inisialisasi komponen tampilan
        val gajiContainer: LinearLayout = findViewById(R.id.gajiContainer)

        val yearPickerValues = Array(10) { currentYear - 5 + it }
        val monthPickerValues = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null)
        dialogBuilder.setView(dialogView)

        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)

        yearPicker.minValue = yearPickerValues.first()
        yearPicker.maxValue = yearPickerValues.last()
        yearPicker.value = currentYear

        monthPicker.minValue = 0
        monthPicker.maxValue = monthPickerValues.size - 1
        monthPicker.displayedValues = monthPickerValues
        monthPicker.value = currentMonth

        dialogBuilder.setTitle("Select Month and Year")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            val selectedYear = yearPicker.value
            val selectedMonth = monthPicker.value + 1 // Tambahkan 1 karena bulan dimulai dari 0

            selectedBulan = selectedMonth.toString()// Format bulan menjadi 2 digit (misalnya: 01, 02, dst.)
            selectedTahun = selectedYear.toString()

            // Mengambil data gaji dari API menggunakan Retrofit dengan filter bulan
            if (userId != null) {
                filterGajiData(userId, selectedBulan!!, selectedTahun!!)
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

}
