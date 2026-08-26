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
     * Kirim OTP ke Nomor WhatsApp Siswa via Backend Render
     */
    fun sendOtpToStudent(
        phone: String,
        studentName: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val otp = generateOtp()
        targetPhone = phone

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${Constants.WA_GATEWAY_URL}/api/send-otp")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    doInput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("phone", phone)
                    put("otp", otp)
                    put("name", studentName)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val responseJson = JSONObject(responseText)
                val isSuccess = responseJson.optBoolean("success", responseCode == 200)
                val msg = responseJson.optString("message", "Status: $responseCode")

                withContext(Dispatchers.Main) {
                    onResult(isSuccess, msg)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Fallback pesan jika backend render belum live atau offline
                    onResult(false, "Gagal terhubung ke server WhatsApp: ${e.localizedMessage ?: "Cek koneksi internet Anda"}")
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
     * Kirim Laporan Evaluasi Pembelajaran & Ibadah ke Nomor Orang Tua / Wali
     */
    fun sendReportToParent(
        parentPhone: String,
        studentName: String,
        reportContent: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${Constants.WA_GATEWAY_URL}/api/send-parent-report")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("parentPhone", parentPhone)
                    put("studentName", studentName)
                    put("reportContent", reportContent)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val responseJson = JSONObject(responseText)
                val isSuccess = responseJson.optBoolean("success", responseCode == 200)
                val msg = responseJson.optString("message", "Status: $responseCode")

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
