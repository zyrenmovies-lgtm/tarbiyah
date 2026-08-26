# Tarbiyah AI Learn - WhatsApp Gateway (Pairing Code)

Microservice backend ringan (~40MB RAM) untuk mengirimkan **Kode OTP WhatsApp Siswa** dan **Laporan Belajar Bot AI Orang Tua** tanpa perlu scan QR (menggunakan **Pairing Code 8 Digit**).

---

## 🚀 Panduan Deploy Gratis ke Render.com (100% Free)

1. **Buka Render**: Masuk ke akun Anda di [https://render.com](https://render.com).
2. **Buat Web Service Baru**:
   - Klik tombol **"New +"** (di kanan atas) > pilih **"Web Service"**.
   - Pilih repositori GitHub Anda: `Tarbiyah-AiLearning`.
3. **Konfigurasi Pengaturan**:
   - **Name**: `tarbiyah-wa-gateway`
   - **Root Directory**: `backend/tarbiyah-wa-gateway` *(Sangat penting)*
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Instance Type**: `Free` (Gratis).
4. **Klik "Deploy Web Service"**:
   - Tunggu 1-2 menit hingga statusnya berubah menjadi **Live**.
   - Catat URL Render Anda (contoh: `https://tarbiyah-wa-gateway.onrender.com`).
5. **Hubungkan WhatsApp (Pairing Code)**:
   - Buka URL Render Anda di browser.
   - Masukkan nomor WhatsApp pengirim bot (contoh: `08123456789`).
   - Klik **"Minta Kode Pairing"**. Kode 8 digit akan muncul (misal: `ABCD-1234`).
   - Buka WhatsApp di HP Anda > **Perangkat Tertaut** > **Tautkan Perangkat** > **Tautkan dengan nomor telepon saja** > Masukkan kode tersebut.
   - Selesai! WhatsApp Anda kini aktif 24/7 di cloud.

---

## 📡 Dokumentasi Endpoint REST API

### 1. Cek Status Bot
`GET /api/status`
**Response**:
```json
{
  "success": true,
  "connected": true,
  "botNumber": "628123456789"
}
```

### 2. Kirim OTP WhatsApp Siswa
`POST /api/send-otp`
**Body**:
```json
{
  "phone": "081298765432",
  "otp": "482910",
  "name": "Ahmad"
}
```

### 3. Kirim Laporan Evaluasi AI ke Orang Tua / Wali
`POST /api/send-parent-report`
**Body**:
```json
{
  "parentPhone": "081211122233",
  "studentName": "Ahmad",
  "reportContent": "- Hafalan Surah Al-Mulk: Lancar (Ayat 1-15)\n- Sholat Dhuha: Sudah Check-In\n- Modul Matematika: Selesai 85%"
}
```
