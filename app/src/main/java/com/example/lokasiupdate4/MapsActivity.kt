package com.example.lokasiupdate4

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var map: GoogleMap
    private lateinit var markerList: RecyclerView
    private val RADIUS: Double = 100.0 // jarak maksimal dalam meter


    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CAMERA_PERMISSION = 100

    private lateinit var photoPreview: ImageView
    private lateinit var markerIconCheckedIn: BitmapDescriptor
    private var isCheckInSubmitted: Boolean = false

    private val CAMERA_REQUEST_CODE = 123

//    private var salesPhotoBitmap: Bitmap? = null


    // Nama Shared Preferences untuk session login
    private val PREF_NAME = "login_session"
    private val KEY_USER_ID = "user_id"

    private var userId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Dapatkan user_id dari Shared Preferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(KEY_USER_ID, "")




        val btnRefresh = findViewById<Button>(R.id.btn_refresh)
        btnRefresh.setOnClickListener {
            refreshMap()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }


        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        markerList = findViewById(R.id.rvmaps)
        markerList.layoutManager = LinearLayoutManager(this)



         //Panggil API untuk mendapatkan data marker
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.getMarkers(userId).enqueue(object : Callback<List<MarkerData>> {
            override fun onResponse(
                call: Call<List<MarkerData>>,
                response: Response<List<MarkerData>>
            ) {
                val markers = response.body()
                if (markers != null) {
                    // Tampilkan marker pada peta
                    for (marker in markers) {
                        val latLng = LatLng(marker.latitude, marker.longitude)
                        map.addMarker(MarkerOptions().position(latLng).title(marker.title))
                    }

                    // Tampilkan data marker pada RecyclerView
                    markerList.adapter = MarkerAdapter(markers)
                }
            }

            override fun onFailure(call: Call<List<MarkerData>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })



    }


    private fun showCheckInDialog(markerData: MarkerData) {
        val latLng = LatLng(markerData.latitude, markerData.longitude)
        if (isNearby(latLng)) {
            if (isCheckInSubmitted()) {
                Toast.makeText(this, "Anda sudah melakukan check-in", Toast.LENGTH_SHORT).show()
                return
            }

            val builder = AlertDialog.Builder(this)

            builder.setTitle("Check-in")
                .setMessage("Apakah Anda ingin melakukan check-in di ${markerData.title}?")
                .setCancelable(false)
                .setPositiveButton("Ya") { dialog, which ->
                    val tokoName = markerData.title
                    val salesName = markerData.snippet

                    val alertDialogBuilder = AlertDialog.Builder(this)
                    val view = layoutInflater.inflate(R.layout.order_form, null)

                    view.findViewById<TextView>(R.id.toko_name).text = tokoName
                    view.findViewById<TextView>(R.id.sales_name).text = salesName

                    val quantityBarang = view.findViewById<EditText>(R.id.quantity_barang)
                    val subtotalHasil = view.findViewById<TextView>(R.id.subtotal_hasil)

                    quantityBarang.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val hargaBarang = 10000 // Harga barang per unit
                            val jumlahBarang = s?.toString()?.toIntOrNull() ?: 0
                            val subtotal = hargaBarang * jumlahBarang

                            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            subtotalHasil.text = formatRupiah.format(subtotal)
                        }
                    })

                    alertDialogBuilder.setView(view)
                        .setCancelable(false)
                        .setPositiveButton("Order") { dialog, which ->
                            val quantity = quantityBarang.text.toString().toIntOrNull()

                            if (quantity == null || quantity <= 0) {
                                Toast.makeText(this@MapsActivity, "Quantity barang tidak valid", Toast.LENGTH_SHORT).show()
                            } else {
                                val totalPrice = quantity * 10000 // Harga barang per packs

                                // Ambil user_id dari user yang melakukan login
                                val userId = getUserId() // mendapatkan user_id
                                val userIdValue = userId?.toIntOrNull() ?: 0
                                val order = Order(tokoName, salesName, quantity, totalPrice, userIdValue)

                                // Cek izin kamera
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                                } else {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                                }

                                val orderService = ApiClient.getOrderService()
                                val call = orderService.submitOrder(order)

                                call.enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        if (response.isSuccessful) {
                                            setCheckInSubmitted()
                                            Toast.makeText(this@MapsActivity, "Order berhasil dikirim", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@MapsActivity, "Order gagal dikirim", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(this@MapsActivity, "Order gagal dikirim: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                        .setNegativeButton("Batal") { dialog, which ->
                            dialog.dismiss()
                        }

                    alertDialogBuilder.create().show()
                }
                .setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }
            builder.create().show()
        } else {
            Toast.makeText(this, "Anda belum berada dalam radius dengan ${markerData.title}", Toast.LENGTH_SHORT).show()
        }
    }






    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Foto berhasil ditangkap
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Skala foto ke ukuran yang diinginkan
            val scaledImageBitmap = scaleBitmap(imageBitmap, 1920, 1080)

            // Konversi foto ke byte array dengan kualitas tinggi
            val outputStream = ByteArrayOutputStream()
            scaledImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageByteArray = outputStream.toByteArray()

            // Simpan foto ke file temporari
            val tempFile = File.createTempFile("image", ".jpg", applicationContext.cacheDir)
            tempFile.writeBytes(imageByteArray)

            // Buat objek PhotoData dengan foto yang disimpan di file temporari
            val photoData = PhotoData(tempFile)

            // Kirim foto ke server
            val orderService = ApiClient.getOrderService()
            val photoRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), tempFile)
            val photoPart = MultipartBody.Part.createFormData("image", tempFile.name, photoRequestBody)
            val call = orderService.uploadImage(photoPart)

            call.enqueue(object : Callback<Void>  {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // Foto berhasil dikirim ke server
                        Toast.makeText(this@MapsActivity, "Foto berhasil dikirim ke server", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MapsActivity, "Gagal mengirim foto ke server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MapsActivity, "Gagal mengirim foto ke server: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        var newWidth = maxWidth
        var newHeight = maxHeight

        if (aspectRatio > 1) {
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newWidth = (newHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getUserId(): String? {
        val sharedPreferences = getSharedPreferences("login_session", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_id", null)
    }









    // Simpan sesi check in
    private fun isCheckInSubmitted(): Boolean {


        val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isCheckInSubmitted", false)



    }



    private fun setCheckInSubmitted() {
        val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isCheckInSubmitted", true).apply()

    }



    private fun refreshMap() {
        // Hapus semua marker pada peta
        map.clear()

        // Dapatkan user_id dari Shared Preferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(KEY_USER_ID, "")

        // Panggil API untuk mendapatkan data marker yang terbaru
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.getMarkers(userId).enqueue(object : Callback<List<MarkerData>> {
            override fun onResponse(
                call: Call<List<MarkerData>>,
                response: Response<List<MarkerData>>
            ) {
                val markers = response.body()
                if (markers != null) {
                    // Tampilkan marker pada peta
                    for (marker in markers) {
                        val latLng = LatLng(marker.latitude, marker.longitude)
                        map.addMarker(MarkerOptions().position(latLng).title(marker.title))
                    }
                    // Tampilkan data marker pada RecyclerView
                    markerList.adapter = MarkerAdapter(markers)
                }
            }

            override fun onFailure(call: Call<List<MarkerData>>, t: Throwable) {
                Toast.makeText(
                    this@MapsActivity,
                    "Error refreshing map: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        map.setOnInfoWindowClickListener { marker ->
            // Cari data marker yang sesuai dengan marker yang di-klik
            val clickedMarkerData = markerList.adapter
                ?.let { adapter -> adapter as MarkerAdapter }
                ?.markers
                ?.find { it.title == marker.title }

            // Tampilkan dialog konfirmasi check-in jika data marker ditemukan
            if (clickedMarkerData != null) {
                showCheckInDialog(clickedMarkerData)
            }
        }

    }


    private fun checkIn(markerData: MarkerData) {

        // Tampilkan pesan bahwa check-in berhasil
        Toast.makeText(this, "Check-in berhasil dilakukan di ${markerData.title}", Toast.LENGTH_SHORT).show()

//        val builder = AlertDialog.Builder(this)
//        builder.setView(R.layout.order_form)
//
//        val alertDialog = builder.create()
//
//        alertDialog.show()
    }

    //RUMUS RADIUS
    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val R = 6371 // radius bumi dalam km
        val dLat = Math.toRadians(latLng2.latitude - latLng1.latitude)
        val dLon = Math.toRadians(latLng2.longitude - latLng1.longitude)
        val lat1 = Math.toRadians(latLng1.latitude)
        val lat2 = Math.toRadians(latLng2.latitude)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val distance = R * c // jarak dalam km
        return distance * 1000 // jarak dalam meter
    }

    private fun isNearby(latLng: LatLng): Boolean {

        val myLocation = map.myLocation
        if (myLocation != null) {
            val myLatLng = LatLng(myLocation.latitude, myLocation.longitude)
            val distance = calculateDistance(myLatLng, latLng)
            return distance <= RADIUS
        }
        return false
    }


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_IMAGE_CAPTURE = 1
    }




}