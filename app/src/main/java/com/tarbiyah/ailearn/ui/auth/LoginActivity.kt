package com.tarbiyah.ailearn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tarbiyah.ailearn.MainActivity
import com.tarbiyah.ailearn.databinding.ActivityLoginBinding
import com.tarbiyah.ailearn.utils.PermissionHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Launcher untuk meminta beberapa izin sekaligus
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            // Setelah izin runtime selesai, cek aksesibilitas
            checkAccessibilityPermission()
        } else {
            // Beberapa izin ditolak — tetap lanjut, fitur tertentu tidak akan berfungsi
            checkAccessibilityPermission()
            Toast.makeText(
                this,
                "Beberapa izin ditolak. Fitur kamera/lokasi mungkin tidak berfungsi.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Minta izin saat pertama kali buka
        requestRuntimePermissions()
        setupListeners()
    }

    // ========================
    // PERMISSION FLOW
    // ========================

    private fun requestRuntimePermissions() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            permissionLauncher.launch(PermissionHelper.REQUIRED_PERMISSIONS)
        } else {
            // Semua izin sudah ada, langsung cek aksesibilitas
            checkAccessibilityPermission()
        }
    }

    private fun checkAccessibilityPermission() {
        if (!PermissionHelper.isAccessibilityServiceEnabled(this)) {
            // Tampilkan dialog penjelasan aksesibilitas
            PermissionHelper.showAccessibilityExplanationDialog(this) {
                PermissionHelper.openAccessibilitySettings(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cek ulang saat kembali dari Settings aksesibilitas
        // (tidak tampilkan dialog lagi jika sudah diaktifkan)
    }

    // ========================
    // AUTH LOGIC
    // ========================

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur pemulihan kata sandi segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.etEmail.error = "Email atau username tidak boleh kosong"
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Kata sandi tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "Kata sandi minimal 6 karakter"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        // TODO: Integrate Firebase Auth
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Memuat..."

        binding.root.postDelayed({
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Masuk"

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1200)
    }
}
