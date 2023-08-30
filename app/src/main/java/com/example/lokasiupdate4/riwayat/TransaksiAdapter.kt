package com.example.lokasiupdate4.riwayat

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.lokasiupdate4.R
import java.text.NumberFormat
import java.util.*

class TransaksiAdapter(private val context: Context) : RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

    private var transactionList: List<TransaksiModel> = emptyList()

    inner class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tokoNameTextView: TextView = itemView.findViewById(R.id.tokoNameTextView)
        private val salesNameTextView: TextView = itemView.findViewById(R.id.salesNameTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        private val totalPriceTextView: TextView = itemView.findViewById(R.id.totalPriceTextView)
        private val waktuTextView: TextView = itemView.findViewById(R.id.waktuTextView)
        private val fotoImageView: ImageView = itemView.findViewById(R.id.fotoImageView)

        init {
            // Set click listener on the CardView
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val transaksi = transactionList[position]
                    showPhotoPopup(transaksi.photoUrl)
                }
            }
        }

        fun bind(transaksi: TransaksiModel) {
            tokoNameTextView.text = transaksi.tokoName
            salesNameTextView.text = transaksi.salesName
            quantityTextView.text = transaksi.quantity.toString()
            totalPriceTextView.text = formatRupiah(transaksi.totalPrice)
            waktuTextView.text = transaksi.waktu

            // Load and display photo
            var fotoUrl = transaksi.photoUrl ?: ""
            fotoUrl = "http://192.168.43.20/presensi_karyawan_fix2/public/storage/transaksi/" +
                    fotoUrl.replace("http://localhost/presensi_karyawan_fix2/public/storage/transaksi/", "")

            if (!fotoUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(fotoUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_img)
                    .into(fotoImageView)
            } else {
                // Set default image if no photo available
                fotoImageView.setImageResource(R.drawable.placeholder_image)
            }
        }

        private fun showPhotoPopup(photoUrl: String?) {
            if (photoUrl.isNullOrEmpty()) {
                // If no photo available, show a message
                Toast.makeText(context, "No photo available", Toast.LENGTH_SHORT).show()
            } else {
                // Show the photo in a dialog
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_foto_pengiriman, null)
                val fotoImageView = dialogView.findViewById<ImageView>(R.id.dialogFotoImageView)

                // Create and show the dialog
                val dialogBuilder = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                val dialog = dialogBuilder.create()
                dialog.show()

                // Modify the photo URL if necessary
                var fotoUrlModified = photoUrl
                if (photoUrl.startsWith("http://localhost")) {
                    fotoUrlModified = photoUrl.replace("http://localhost", "http://192.168.43.20")
                }

                // Load and display the photo using Glide or any other image loading library
                Glide.with(context)
                    .load(fotoUrlModified)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_img)
                    .into(fotoImageView)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_transaksi, parent, false)
        return TransaksiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val transaksi = transactionList[position]
        holder.bind(transaksi)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun setTransactionList(transactions: List<TransaksiModel>) {
        transactionList = transactions
        notifyDataSetChanged()
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(amount)
    }
}
