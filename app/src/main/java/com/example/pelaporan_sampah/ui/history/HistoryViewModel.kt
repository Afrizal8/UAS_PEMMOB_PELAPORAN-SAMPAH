package com.example.pelaporan_sampah.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Riwayat Laporan"
    }
    val text: LiveData<String> = _text
}