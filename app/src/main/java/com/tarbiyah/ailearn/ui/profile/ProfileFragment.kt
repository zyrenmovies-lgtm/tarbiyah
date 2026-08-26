package com.tarbiyah.ailearn.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tarbiyah.ailearn.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Load placeholder data (replace with Firebase later)
        binding.tvProfileName.text = "Muhammad Rizki"
        binding.tvProfileUsername.text = "@rizki_tarbiyah"
        binding.chipLevel.text = "MA - Kelas X"
        binding.chipPahala.text = "1.250 Poin"
        binding.tvFollowers.text = "128"
        binding.tvFollowing.text = "56"
        binding.tvStudyDays.text = "42"

        // Journal checkboxes (persisted state demo)
        binding.cbTilawah.isChecked = true
        binding.cbDhuha.isChecked = false
        binding.cbSholat5.isChecked = false
        binding.progressSholat.progress = 60 // 3 of 5 prayers done
    }

    private fun setupListeners() {
        binding.cbTilawah.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "MasyaAllah! Tilawah hari ini dicatat.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cbDhuha.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Alhamdulillah! Dhuha dicatat. +50 Poin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cbSholat5.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Sholat 5 waktu dicatat. +100 Poin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAiEval.setOnClickListener {
            Toast.makeText(requireContext(), "Evaluasi AI Mingguan sedang diproses...", Toast.LENGTH_SHORT).show()
        }

        binding.itemReminderSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan Reminder - segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.itemBotSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan Laporan Bot - segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.itemLogout.setOnClickListener {
            // TODO: Firebase signOut + navigate to LoginActivity
            Toast.makeText(requireContext(), "Keluar dari akun...", Toast.LENGTH_SHORT).show()
        }

        binding.ivProfileAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "Ganti foto profil - segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
