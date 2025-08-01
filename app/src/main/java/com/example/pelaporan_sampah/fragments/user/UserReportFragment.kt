package com.example.pelaporan_sampah.fragments.user

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.pelaporan_sampah.databinding.FragmentUserReportBinding
import com.example.pelaporan_sampah.models.Report
import com.example.pelaporan_sampah.repository.ReportRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class UserReportFragment : Fragment() {

    private var _binding: FragmentUserReportBinding? = null
    private val binding get() = _binding!!

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentAddress: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var reportRepository: ReportRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (isGranted) {
                ambilLokasiSaatIni()
            } else {
                Toast.makeText(requireContext(), "Permission lokasi diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        setupJenisSampahDropdown()
        setupClickListeners()
    }

    private fun initializeComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        reportRepository = ReportRepository()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupJenisSampahDropdown() {
        val jenisSampah = arrayOf(
            "Sampah Organik",
            "Sampah Plastik",
            "Sampah Kertas",
            "Sampah Kaca",
            "Sampah Elektronik",
            "Sampah Berbahaya",
            "Lainnya"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jenisSampah)
        binding.actvJenisSampah.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnAmbilLokasi.setOnClickListener {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        binding.btnKirimLaporan.setOnClickListener {
            kirimLaporan()
        }
    }

    private fun ambilLokasiSaatIni() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        binding.btnAmbilLokasi.isEnabled = false
        binding.tvLokasiStatus.text = "Mengambil lokasi..."

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            binding.btnAmbilLokasi.isEnabled = true

            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude

                lifecycleScope.launch {
                    currentAddress = getAddressFromLocationAsync(currentLatitude!!, currentLongitude!!)
                    binding.tvLokasiStatus.text = "Lokasi berhasil diambil"
                    binding.tvKoordinat.text = "Lat: $currentLatitude, Long: $currentLongitude"
                    binding.layoutKoordinat.visibility = View.VISIBLE

                    Toast.makeText(requireContext(), "Lokasi berhasil diambil", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.tvLokasiStatus.text = "Gagal mengambil lokasi"
                Toast.makeText(requireContext(), "Gagal mengambil lokasi. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            binding.btnAmbilLokasi.isEnabled = true
            binding.tvLokasiStatus.text = "Gagal mengambil lokasi"
            Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLocationAsync(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Alamat tidak ditemukan"
                } else {
                    "Alamat tidak ditemukan"
                }
            } catch (e: Exception) {
                "Gagal mendapatkan alamat"
            }
        }
    }

    private fun kirimLaporan() {
        val jenisSampah = binding.actvJenisSampah.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        if (validateInput(jenisSampah, deskripsi)) {
            showLoading(true)

            val report = Report(
                jenisSampah = jenisSampah,
                deskripsi = deskripsi,
                latitude = currentLatitude!!,
                longitude = currentLongitude!!,
                alamat = currentAddress,
                status = "Menunggu"
            )

            lifecycleScope.launch {
                try {
                    val result = reportRepository.addReport(report)
                    showLoading(false)

                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()
                        clearForm()
                    } else {
                        val error = result.exceptionOrNull()
                        Toast.makeText(requireContext(), "Gagal mengirim laporan: ${error?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(jenisSampah: String, deskripsi: String): Boolean {
        return when {
            auth.currentUser == null -> {
                Toast.makeText(requireContext(), "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
                false
            }
            jenisSampah.isEmpty() -> {
                Toast.makeText(requireContext(), "Pilih jenis sampah", Toast.LENGTH_SHORT).show()
                false
            }
            deskripsi.isEmpty() -> {
                binding.etDeskripsi.error = "Deskripsi tidak boleh kosong"
                binding.etDeskripsi.requestFocus()
                false
            }
            currentLatitude == null || currentLongitude == null -> {
                Toast.makeText(requireContext(), "Ambil lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnKirimLaporan.isEnabled = !isLoading
        binding.btnAmbilLokasi.isEnabled = !isLoading
    }

    private fun clearForm() {
        binding.actvJenisSampah.text.clear()
        binding.etDeskripsi.text?.clear()
        binding.tvLokasiStatus.text = "Tekan tombol untuk ambil lokasi"
        binding.layoutKoordinat.visibility = View.GONE
        currentLatitude = null
        currentLongitude = null
        currentAddress = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
