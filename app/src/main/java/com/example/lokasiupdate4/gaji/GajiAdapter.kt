package com.example.lokasiupdate4.gaji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lokasiupdate4.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GajiAdapter(private val gajiList: List<GajiModel>) : BaseAdapter() {

    override fun getCount(): Int {
        return gajiList.size
    }

    override fun getItem(position: Int): Any {
        return gajiList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val gaji = gajiList[position]

        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.item_gaji, parent, false)

        val gajiPokokTextView: TextView = view.findViewById(R.id.gaji_pokok)
        val intensifKunjunganTextView: TextView = view.findViewById(R.id.intensif_kunjungan)
        val bonusPenjualanTextView: TextView = view.findViewById(R.id.bonus_penjualan)
        val gajiTotalTextView: TextView = view.findViewById(R.id.gaji_total)
        val bulanTextView: TextView = view.findViewById(R.id.bulan)
        val tahunTextView: TextView = view.findViewById(R.id.tahun)


        val gajiPokokFormatted = formatRupiah(gaji.gajiPokok)
        gajiPokokTextView.text = gajiPokokFormatted

        val kunjunganrupiah = formatRupiah(gaji.intensifKunjungan)
        intensifKunjunganTextView.text = kunjunganrupiah

        val bonusrupiah = formatRupiah(gaji.bonusPenjualan)
        bonusPenjualanTextView.text = bonusrupiah

        val gajirupiah = formatRupiah(gaji.gajiTotal)
        gajiTotalTextView.text = gajirupiah

        bulanTextView.text = getNamaBulan(gaji.bulan)
        tahunTextView.text = gaji.tahun

        return view
    }

    private fun formatRupiah(amount: String): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(amount.toLong())
    }

    private fun getNamaBulan(bulan: String): String {
        val monthIndex = bulan.toInt() - 1 // Mengurangi 1 karena indeks bulan dimulai dari 0
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthIndex)
        return monthFormat.format(calendar.time)
    }
}



