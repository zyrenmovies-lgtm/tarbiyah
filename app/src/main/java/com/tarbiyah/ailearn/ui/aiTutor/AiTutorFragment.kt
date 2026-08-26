package com.tarbiyah.ailearn.ui.aiTutor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tarbiyah.ailearn.databinding.FragmentAiTutorBinding

class AiTutorFragment : Fragment() {

    private var _binding: FragmentAiTutorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardStartStudy.setOnClickListener {
            // TODO: Navigate to study session
            Toast.makeText(requireContext(), "Memulai sesi belajar AI...", Toast.LENGTH_SHORT).show()
        }

        binding.cardQuickTest.setOnClickListener {
            // TODO: Launch quiz with camera lock
            Toast.makeText(requireContext(), "Tes mendadak AI - kamera akan diaktifkan", Toast.LENGTH_SHORT).show()
        }

        binding.cardTahfidz.setOnClickListener {
            // TODO: Open Tahfidz voice session
            Toast.makeText(requireContext(), "Asisten Tahfidz - izin mikrofon diperlukan", Toast.LENGTH_SHORT).show()
        }

        binding.cardFocusRoom.setOnClickListener {
            // TODO: Enter virtual focus room
            Toast.makeText(requireContext(), "Bergabung ke Ruang Fokus Bersama", Toast.LENGTH_SHORT).show()
        }

        binding.cardCatchUpTutor.setOnClickListener {
            // TODO: Start catch-up mode
            Toast.makeText(requireContext(), "Mode Kejar Target - AI menyusun kurikulum...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
