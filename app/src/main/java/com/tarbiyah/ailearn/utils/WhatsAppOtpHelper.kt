package com.tarbiyah.ailearn.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

object WhatsAppOtpHelper {

    private var currentOtp: String? = null
    private var otpTimestamp: Long = 0
    private var targetPhone: String? = null
    private const val OTP_EXPIRY_MILLIS = 5 * 60 * 1000L // 5 Menit

    /**
     * Generate 6 Digit Angka OTP Acak
     */
    fun generateOtp(): String {
        val code = Random.nextInt(100000, 999999).toString()
        currentOtp = code
        otpTimestamp = System.currentTimeMillis()
        return code
    }

    /**
     * Kirim OTP ke Nomor WhatsApp Siswa Menggunakan API Fonnte
     */
    fun sendOtpToStudent(
        phone: String,
        studentName: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val otp = generateOtp()
        targetPhone = phone

        val messageText = """
            *Assalamu'alaikum Warahmatullahi Wabarakatuh* 🌙
            
            Halo *$studentName*, terima kasih telah mendaftar di *TARBIYAH: AI LEARN*.
            
            Berikut adalah Kode OTP verifikasi pendaftaran akun Anda:
            🔐 *$otp*
            
            _Kode ini bersifat rahasia dan berlaku selama 5 menit. Jangan berikan kode ini kepada siapa pun._
            
            *TARBIYAH AI LEARN*
            _Belajar Cerdas Berlandaskan Nilai-Nilai Islam_
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.fonnte.com/send")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", Constants.FONNTE_TOKEN)
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    doInput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("target", phone)
                    put("message", messageText)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val responseJson = JSONObject(responseText)
                
                // Fonnte mengembalikan { "status": true, ... }
                val isSuccess = responseJson.optBoolean("status", responseCode == 200)
                val msg = responseJson.optString("detail", "Berhasil kirim OTP")

                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        onResult(true, "OTP WhatsApp berhasil dikirim.")
                    } else {
                        onResult(false, "Gagal mengirim: $msg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Gagal terhubung ke Fonnte: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Verifikasi kode OTP yang dimasukkan siswa
     */
    fun verifyOtp(inputCode: String): Pair<Boolean, String> {
        val otp = currentOtp
        if (otp == null) {
            return Pair(false, "Silakan minta kode OTP terlebih dahulu.")
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - otpTimestamp > OTP_EXPIRY_MILLIS) {
            currentOtp = null
            return Pair(false, "Kode OTP sudah kedaluwarsa (lebih dari 5 menit). Silakan kirim ulang.")
        }

        return if (inputCode.trim() == otp) {
            currentOtp = null // Reset setelah berhasil verifikasi
            Pair(true, "Nomor WhatsApp siswa berhasil diverifikasi!")
        } else {
            Pair(false, "Kode OTP salah. Silakan periksa kembali pesan WhatsApp Anda.")
        }
    }

    /**
     * Kirim Laporan Evaluasi Pembelajaran & Ibadah ke Nomor Orang Tua / Wali via Fonnte
     */
    fun sendReportToParent(
        parentPhone: String,
        studentName: String,
        reportContent: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val messageText = """
            *Assalamu'alaikum Warahmatullahi Wabarakatuh* 🌿
            _Laporan Pembelajaran & Ibadah Harian (Tarbiyah AI Learn)_
            
            Yth. Bapak/Ibu Wali dari *$studentName*,
            
            Berikut adalah ringkasan evaluasi perkembangan belajar dan ibadah ananda:
            $reportContent
            
            Semoga ananda senantiasa istiqomah dalam menuntut ilmu dan berakhlakul karimah.
            
            *Tarbiyah AI Learn Bot*
            _Sistem Pendamping Belajar Madrasah Digital_
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.fonnte.com/send")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", Constants.FONNTE_TOKEN)
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("target", parentPhone)
                    put("message", messageText)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val responseJson = JSONObject(responseText)
                val isSuccess = responseJson.optBoolean("status", responseCode == 200)
                val msg = responseJson.optString("detail", "Berhasil kirim laporan")

                withContext(Dispatchers.Main) {
                    onResult(isSuccess, msg)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Gagal mengirim laporan ke wali: ${e.localizedMessage}")
                }
            }
        }
    }
}
