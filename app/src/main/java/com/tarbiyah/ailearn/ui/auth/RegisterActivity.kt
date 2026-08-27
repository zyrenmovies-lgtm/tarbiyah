package com.tarbiyah.ailearn.ui.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tarbiyah.ailearn.MainActivity
import com.tarbiyah.ailearn.R
import com.tarbiyah.ailearn.databinding.ActivityRegisterBinding
import com.tarbiyah.ailearn.databinding.FragmentRegisterStep1Binding
import com.tarbiyah.ailearn.databinding.FragmentRegisterStep2Binding
import com.tarbiyah.ailearn.databinding.FragmentRegisterStep3Binding
import com.tarbiyah.ailearn.databinding.FragmentRegisterStep4Binding
import com.tarbiyah.ailearn.utils.WhatsAppOtpHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var currentStep = 0
    private val totalSteps = 4

    // Data registrasi & status verifikasi
    var studentName: String = ""
    var studentPhone: String = ""
    var parentPhone: String = ""
    var isStudentPhoneVerified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        val adapter = RegisterPagerAdapter(this)
        binding.vpRegister.adapter = adapter
        binding.vpRegister.isUserInputEnabled = false
        updateStepIndicator(0)
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            // Validasi Step 2: Nomor WhatsApp Siswa Wajib Terverifikasi
            if (currentStep == 1 && !isStudentPhoneVerified) {
                Toast.makeText(
                    this,
                    "⚠️ Silakan verifikasi nomor WhatsApp siswa dengan OTP terlebih dahulu!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (currentStep < totalSteps - 1) {
                currentStep++
                binding.vpRegister.currentItem = currentStep
                updateStepIndicator(currentStep)
                updateButtons()
            } else {
                // Final step - submit registration
                performRegistration()
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                binding.vpRegister.currentItem = currentStep
                updateStepIndicator(currentStep)
                updateButtons()
            }
        }

        binding.tvGoLogin.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun updateButtons() {
        binding.btnBack.visibility = if (currentStep > 0) View.VISIBLE else View.GONE
        binding.btnNext.text = if (currentStep == totalSteps - 1) {
            getString(R.string.btn_finish)
        } else {
            getString(R.string.btn_next)
        }
    }

    private fun updateStepIndicator(step: Int) {
        val stepViews = listOf(binding.step1, binding.step2, binding.step3, binding.step4)
        val lineViews = listOf(binding.line1, binding.line2, binding.line3)

        stepViews.forEachIndexed { index, view ->
            if (index <= step) {
                view.setBackgroundResource(R.drawable.bg_button_minimal)
            } else {
                view.setBackgroundResource(R.drawable.bg_input_minimal)
            }
        }

        lineViews.forEachIndexed { index, view ->
            view.setBackgroundColor(
                if (index < step) getColor(R.color.gold_primary)
                else getColor(R.color.black_divider)
            )
        }
    }

    private fun performRegistration() {
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Mendaftar..."

        binding.root.postDelayed({
            Toast.makeText(this, "Pendaftaran berhasil! Selamat datang di Tarbiyah.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1500)
    }

    inner class RegisterPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = totalSteps
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> RegisterStep1Fragment()
            1 -> RegisterStep2Fragment()
            2 -> RegisterStep3Fragment()
            3 -> RegisterStep4Fragment()
            else -> RegisterStep1Fragment()
        }
    }
}

// ============================
// STEP 1: Data Pribadi
// ============================
class RegisterStep1Fragment : Fragment() {
    private var _binding: FragmentRegisterStep1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etFullName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                (activity as? RegisterActivity)?.studentName = binding.etFullName.text.toString().trim()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ============================
// STEP 2: Data Akademik & OTP WhatsApp
// ============================
class RegisterStep2Fragment : Fragment() {
    private var _binding: FragmentRegisterStep2Binding? = null
    private val binding get() = _binding!!
    private var resendTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupOtpFlow()
    }

    private fun setupDropdowns() {
        val levels = arrayOf("RA (Raudlatul Athfal)", "MI (Madrasah Ibtidaiyah)", "MTs (Madrasah Tsanawiyah)", "MA (Madrasah Aliyah)")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.spinnerLevel.setAdapter(levelAdapter)
        binding.spinnerLevel.isClickable = true

        binding.spinnerLevel.setOnItemClickListener { _, _, position, _ ->
            val grades = when (position) {
                0 -> arrayOf("TK A", "TK B")
                1 -> arrayOf("Kelas 1", "Kelas 2", "Kelas 3", "Kelas 4", "Kelas 5", "Kelas 6")
                2, 3 -> arrayOf("Kelas 1 / X", "Kelas 2 / XI", "Kelas 3 / XII")
                else -> arrayOf()
            }
            val gradeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, grades)
            binding.spinnerGrade.setAdapter(gradeAdapter)
            binding.spinnerGrade.text.clear()
        }
    }

    private fun setupOtpFlow() {
        binding.btnSendOtpStudent.setOnClickListener {
            val phone = binding.etStudentPhone.text.toString().trim()
            if (phone.isEmpty() || phone.length < 9) {
                binding.etStudentPhone.error = "Masukkan nomor WhatsApp yang valid"
                return@setOnClickListener
            }
            binding.etStudentPhone.error = null

            val registerActivity = activity as? RegisterActivity
            val studentName = registerActivity?.studentName?.ifEmpty { "Siswa Tarbiyah" } ?: "Siswa Tarbiyah"

            binding.btnSendOtpStudent.isEnabled = false
            binding.btnSendOtpStudent.text = "Mengirim..."

            WhatsAppOtpHelper.sendOtpToStudent(phone, studentName) { success, message ->
                binding.btnSendOtpStudent.isEnabled = true
                binding.btnSendOtpStudent.text = "Kirim OTP"

                if (success) {
                    Toast.makeText(requireContext(), "OTP terkirim ke WhatsApp $phone", Toast.LENGTH_SHORT).show()
                } else {
                    // Berikan info fallback jika server belum dihubungkan / demo mode
                    val demoCode = WhatsAppOtpHelper.generateOtp()
                    Toast.makeText(
                        requireContext(),
                        "Mode Simulasi (Server Offline). Kode OTP Anda: $demoCode",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Tampilkan Dialog Input OTP
                showOtpVerificationDialog(phone)
            }
        }

        binding.etParentContact.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                (activity as? RegisterActivity)?.parentPhone = binding.etParentContact.text.toString().trim()
            }
        }
    }

    private fun showOtpVerificationDialog(phoneNumber: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_otp_verification)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        val tvDestInfo = dialog.findViewById<TextView>(R.id.tv_otp_dest_info)
        val etOtpCode = dialog.findViewById<EditText>(R.id.et_otp_code)
        val tvResend = dialog.findViewById<TextView>(R.id.tv_resend_otp)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel_otp)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm_otp)

        tvDestInfo.text = "Kode OTP 6 digit telah dikirimkan ke WhatsApp:\n$phoneNumber"

        // Timer 60 detik
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvResend.text = "Kirim ulang kode dalam ${millisUntilFinished / 1000}s"
                tvResend.isEnabled = false
            }
            override fun onFinish() {
                tvResend.text = "Kirim Ulang Kode OTP"
                tvResend.setTextColor(requireContext().getColor(R.color.gold_primary))
                tvResend.isEnabled = true
            }
        }.start()

        tvResend.setOnClickListener {
            val studentName = (activity as? RegisterActivity)?.studentName ?: "Siswa"
            WhatsAppOtpHelper.sendOtpToStudent(phoneNumber, studentName) { _, _ ->
                Toast.makeText(requireContext(), "Kode OTP baru telah dikirim!", Toast.LENGTH_SHORT).show()
                resendTimer?.start()
            }
        }

        btnCancel.setOnClickListener {
            resendTimer?.cancel()
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val inputCode = etOtpCode.text.toString().trim()
            if (inputCode.length != 6) {
                Toast.makeText(requireContext(), "Harap masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (isValid, message) = WhatsAppOtpHelper.verifyOtp(inputCode)
            if (isValid) {
                resendTimer?.cancel()
                dialog.dismiss()

                // Update Status Terverifikasi di Step 2
                val regActivity = activity as? RegisterActivity
                regActivity?.isStudentPhoneVerified = true
                regActivity?.studentPhone = phoneNumber

                binding.tvOtpStatus.text = "✅ Terverifikasi (WhatsApp Siswa Aktif)"
                binding.tvOtpStatus.setTextColor(requireContext().getColor(R.color.green_accent))
                binding.btnSendOtpStudent.visibility = View.GONE
                binding.etStudentPhone.isEnabled = false

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendTimer?.cancel()
        _binding = null
    }
}

// ============================
// STEP 3: Domisili
// ============================
class RegisterStep3Fragment : Fragment() {
    private var _binding: FragmentRegisterStep3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCascadingDropdowns()
    }

    private fun setupCascadingDropdowns() {
        val provinces = arrayOf("Riau", "DKI Jakarta", "Jawa Barat", "Jawa Tengah", "Jawa Timur", "Sumatera Utara", "Sumatera Selatan")
        val provAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, provinces)
        binding.spinnerProvince.setAdapter(provAdapter)

        binding.spinnerProvince.setOnItemClickListener { _, _, position, _ ->
            val cities = when (position) {
                0 -> arrayOf("Kabupaten Indragiri Hilir", "Kabupaten Indragiri Hulu", "Kabupaten Kampar", "Kota Pekanbaru", "Kota Dumai")
                1 -> arrayOf("Jakarta Pusat", "Jakarta Utara", "Jakarta Selatan", "Jakarta Timur", "Jakarta Barat")
                else -> arrayOf("Kabupaten/Kota 1", "Kabupaten/Kota 2", "Kabupaten/Kota 3")
            }
            val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
            binding.spinnerCity.setAdapter(cityAdapter)
            binding.spinnerCity.text.clear()
            binding.spinnerDistrict.text.clear()
            binding.spinnerVillage.text.clear()
        }

        binding.spinnerCity.setOnItemClickListener { _, _, position, _ ->
            val districts = when (position) {
                0 -> arrayOf("Tembilahan", "Tembilahan Hulu", "Kateman", "Gaung", "Enok")
                else -> arrayOf("Kecamatan 1", "Kecamatan 2", "Kecamatan 3")
            }
            val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
            binding.spinnerDistrict.setAdapter(districtAdapter)
            binding.spinnerDistrict.text.clear()
            binding.spinnerVillage.text.clear()
        }

        binding.spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
            val villages = when (position) {
                0 -> arrayOf("Tembilahan Kota", "Tembilahan Hilir", "Pekan Arba")
                else -> arrayOf("Kelurahan/Desa 1", "Kelurahan/Desa 2", "Kelurahan/Desa 3")
            }
            val villageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, villages)
            binding.spinnerVillage.setAdapter(villageAdapter)
            binding.spinnerVillage.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ============================
// STEP 4: Verifikasi & GPS
// ============================
class RegisterStep4Fragment : Fragment() {
    private var _binding: FragmentRegisterStep4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnDetectGps.setOnClickListener {
            binding.tvGpsStatus.text = "Mendeteksi lokasi..."
            binding.btnDetectGps.isEnabled = false

            // Simulasi deteksi GPS
            binding.root.postDelayed({
                binding.tvGpsStatus.text = "Terdeteksi: -0.3412, 103.1592 (Tembilahan, Riau)"
                binding.tvGpsStatus.setTextColor(requireContext().getColor(R.color.green_accent))
                binding.btnDetectGps.isEnabled = true
                binding.btnDetectGps.text = "Lokasi Terdeteksi"
            }, 1500)
        }

        binding.btnScanFace.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Face ID segera hadir", Toast.LENGTH_SHORT).show()
            binding.tvFaceStatus.text = "Wajah berhasil dipindai (demo)"
            binding.tvFaceStatus.setTextColor(requireContext().getColor(R.color.green_accent))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
