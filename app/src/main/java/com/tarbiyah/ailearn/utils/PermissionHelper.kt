package com.tarbiyah.ailearn.utils

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tarbiyah.ailearn.R
import com.tarbiyah.ailearn.service.AppBlockerService

object PermissionHelper {

    // Daftar izin runtime yang diperlukan
    val REQUIRED_PERMISSIONS = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    /**
     * Cek apakah semua izin runtime sudah diberikan
     */
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Cek apakah Accessibility Service sudah aktif
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
            it.resolveInfo.serviceInfo.name == AppBlockerService::class.java.name
        }
    }

    /**
     * Buka Settings Aksesibilitas agar user mengaktifkan secara manual
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }

    /**
     * Tampilkan dialog penjelasan aksesibilitas sebelum diarahkan ke Settings
     */
    fun showAccessibilityExplanationDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setTitle("Izin Aksesibilitas Diperlukan")
            .setMessage(
                "Tarbiyah AI Learn membutuhkan izin Aksesibilitas untuk:\n\n" +
                "• Memblokir aplikasi lain saat waktu sholat\n" +
                "• Memblokir aplikasi lain saat sesi belajar aktif\n" +
                "• Memastikan kamu tetap fokus selama ujian mendadak\n\n" +
                "Langkah:\n" +
                "1. Ketuk OK untuk membuka Pengaturan Aksesibilitas\n" +
                "2. Cari 'Tarbiyah AI Learn'\n" +
                "3. Aktifkan layanannya"
            )
            .setPositiveButton("OK, Buka Pengaturan") { _, _ -> onConfirm() }
            .setNegativeButton("Nanti Saja") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    /**
     * Tampilkan dialog permintaan izin kamera (untuk Face ID / Tes Mendadak)
     */
    fun showCameraPermissionRationale(activity: AppCompatActivity, onConfirm: () -> Unit) {
        AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setTitle("Izin Kamera")
            .setMessage(
                "Kamera diperlukan untuk:\n\n" +
                "• Pemindaian wajah saat pendaftaran (Face ID)\n" +
                "• Pengawasan selama tes mendadak AI\n" +
                "• Memastikan tidak ada kecurangan saat ujian"
            )
            .setPositiveButton("Izinkan") { _, _ -> onConfirm() }
            .setNegativeButton("Tolak") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
