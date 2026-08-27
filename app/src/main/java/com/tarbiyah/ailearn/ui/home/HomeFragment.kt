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
    }

    private fun setupUI() {
        val userName = "Rizki" // TODO: Load from SharedPreferences or ViewModel
        binding.tvUsername.text = userName

        // Set current date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        binding.tvDate.text = dateFormat.format(Date())

        // Set prayer info (placeholder — replace with actual prayer API)
        binding.tvPrayerName.text = "DZUHUR"
        binding.tvPrayerTime.text = "12:30 PM"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
