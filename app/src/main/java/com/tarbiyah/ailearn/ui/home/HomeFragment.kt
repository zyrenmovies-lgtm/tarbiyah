package com.tarbiyah.ailearn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tarbiyah.ailearn.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set username (placeholder — replace with actual user data from prefs/Firebase)
        val userName = "Rizki" // TODO: Load from SharedPreferences or ViewModel
        binding.tvUsername.text = "$userName!"

        // Set current date in Indonesian
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvDate.text = dateFormat.format(Date())

        // Set study progress (placeholder)
        binding.tvStudyPercent.text = "75%"
        binding.progressStudy.progress = 75

        // Set pahala count
        binding.tvPahalaCount.text = "1.250"

        // Set catch-up status
        binding.tvCatchUpStatus.text = "2 modul Matematika tertinggal"

        // Set prayer info (placeholder — replace with actual prayer API)
        binding.tvPrayerName.text = "Dzuhur"
        binding.tvPrayerTime.text = "12:30"
        binding.tvPrayerCountdown.text = "dalam 1 jam 23 menit"
        binding.progressPrayer.progress = 60
    }

    private fun setupClickListeners() {
        binding.cardStudyProgress.setOnClickListener {
            // TODO: Navigate to detailed study progress
        }

        binding.cardPahala.setOnClickListener {
            // TODO: Navigate to pahala/points detail
        }

        binding.cardCatchUp.setOnClickListener {
            // TODO: Start catch-up mode
        }

        binding.cardPrayer.setOnClickListener {
            // TODO: Show full prayer schedule
        }

        binding.cardReminderSedekah.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Pengingat Sedekah", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.cardReminderSholat.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Pengingat Sholat", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.cardReminderBelajar.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Pengingat Belajar", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.cardReminderLainnya.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Pengingat Lainnya", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnCatchUp.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Mode Kejar Target dimulai!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
