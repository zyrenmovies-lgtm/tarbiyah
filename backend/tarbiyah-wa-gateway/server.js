const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const pino = require('pino');
const {
    default: makeWASocket,
    useMultiFileAuthState,
    DisconnectReason,
    fetchLatestBaileysVersion,
    makeInMemoryStore
} = require('@whiskeysockets/baileys');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

let sock = null;
let isConnected = false;
let currentPairingCode = null;
let botNumber = null;
const sessionDir = path.join(__dirname, 'session');

// Helper to format phone number to international WhatsApp JID (628xxx@s.whatsapp.net)
function formatPhoneToJid(phone) {
    let clean = phone.replace(/[^0-9]/g, '');
    if (clean.startsWith('0')) {
        clean = '62' + clean.slice(1);
    } else if (clean.startsWith('+62')) {
        clean = '62' + clean.slice(3);
    } else if (!clean.startsWith('62') && clean.length > 5) {
        clean = '62' + clean;
    }
    return clean + '@s.whatsapp.net';
}

function cleanPhoneNumber(phone) {
    let clean = phone.replace(/[^0-9]/g, '');
    if (clean.startsWith('0')) {
        clean = '62' + clean.slice(1);
    } else if (clean.startsWith('+62')) {
        clean = '62' + clean.slice(3);
    } else if (!clean.startsWith('62') && clean.length > 5) {
        clean = '62' + clean;
    }
    return clean;
}

// Inisialisasi WhatsApp Socket
async function startWhatsApp(pairingPhone = null) {
    const { state, saveCreds } = await useMultiFileAuthState(sessionDir);
    const { version } = await fetchLatestBaileysVersion();

    sock = makeWASocket({
        version,
        logger: pino({ level: 'silent' }),
        printQRInTerminal: false,
        auth: state,
        browser: ['Ubuntu', 'Chrome', '20.0.04']
    });

    sock.ev.on('creds.update', saveCreds);

    sock.ev.on('connection.update', async (update) => {
        const { connection, lastDisconnect } = update;

        if (connection === 'close') {
            isConnected = false;
            const shouldReconnect = (lastDisconnect?.error)?.output?.statusCode !== DisconnectReason.loggedOut;
            console.log('Koneksi terputus karena:', lastDisconnect?.error, 'Mencoba konek ulang:', shouldReconnect);
            if (shouldReconnect) {
                setTimeout(() => startWhatsApp(), 3000);
            }
        } else if (connection === 'open') {
            isConnected = true;
            botNumber = sock.user?.id ? sock.user.id.split(':')[0] : 'Aktif';
            console.log('✅ WhatsApp Bot Tarbiyah berhasil tersambung sebagai:', botNumber);
        }
    });

    if (pairingPhone && !sock.authState.creds.registered) {
        setTimeout(async () => {
            try {
                const cleanPhone = cleanPhoneNumber(pairingPhone);
                const code = await sock.requestPairingCode(cleanPhone);
                currentPairingCode = code;
                console.log(`🔑 Pairing Code untuk ${cleanPhone}: ${code}`);
            } catch (err) {
                console.error('Gagal meminta pairing code:', err);
                currentPairingCode = null;
            }
        }, 3000);
    }

    return sock;
}

// Start bot on startup if session already exists
if (fs.existsSync(sessionDir)) {
    startWhatsApp();
}

// ==========================================
// REST API ENDPOINTS
// ==========================================

// 1. Cek Status Bot
app.get('/api/status', (req, res) => {
    res.json({
        success: true,
        connected: isConnected,
        botNumber: botNumber,
        pairingCode: currentPairingCode
    });
});

// 2. Request Pairing Code
app.post('/api/pair', async (req, res) => {
    const { phone } = req.body;
    if (!phone) {
        return res.status(400).json({ success: false, message: 'Nomor WhatsApp wajib diisi' });
    }

    try {
        await startWhatsApp(phone);
        
        // Wait for pairing code to generate (max 5 seconds)
        let tries = 0;
        const checkInterval = setInterval(() => {
            tries++;
            if (currentPairingCode) {
                clearInterval(checkInterval);
                return res.json({
                    success: true,
                    pairingCode: currentPairingCode,
                    message: 'Kode pairing berhasil dibuat. Masukkan kode ini di WhatsApp Anda.'
                });
            }
            if (tries >= 10) {
                clearInterval(checkInterval);
                return res.status(500).json({
                    success: false,
                    message: 'Waktu tunggu habis. Pastikan nomor benar dan coba lagi.'
                });
            }
        }, 500);

    } catch (error) {
        console.error('Error saat request pairing:', error);
        res.status(500).json({ success: false, message: error.message });
    }
});

// 3. Kirim OTP ke Siswa
app.post('/api/send-otp', async (req, res) => {
    const { phone, otp, name } = req.body;

    if (!phone || !otp) {
        return res.status(400).json({ success: false, message: 'Nomor HP dan OTP wajib diisi' });
    }

    if (!isConnected || !sock) {
        return res.status(503).json({
            success: false,
            message: 'Bot WhatsApp belum tersambung. Silakan lakukan pairing terlebih dahulu di dashboard web.'
        });
    }

    try {
        const jid = formatPhoneToJid(phone);
        const displayName = name || 'Siswa';

        const messageText = 
`*Assalamu'alaikum Warahmatullahi Wabarakatuh* 🌙

Halo *${displayName}*, terima kasih telah mendaftar di *TARBIYAH: AI LEARN*.

Berikut adalah Kode OTP verifikasi pendaftaran akun Anda:
🔐 *${otp}*

_Kode ini bersifat rahasia dan berlaku selama 5 menit. Jangan berikan kode ini kepada siapa pun._

*TARBIYAH AI LEARN*
_Belajar Cerdas Berlandaskan Nilai-Nilai Islam_`;

        await sock.sendMessage(jid, { text: messageText });

        res.json({
            success: true,
            message: 'OTP WhatsApp berhasil dikirim ke nomor siswa',
            target: phone
        });
    } catch (error) {
        console.error('Gagal mengirim OTP:', error);
        res.status(500).json({ success: false, message: 'Gagal mengirim pesan: ' + error.message });
    }
});

// 4. Kirim Laporan AI ke Nomor Orang Tua / Wali
app.post('/api/send-parent-report', async (req, res) => {
    const { parentPhone, studentName, reportContent } = req.body;

    if (!parentPhone || !reportContent) {
        return res.status(400).json({ success: false, message: 'Nomor Wali dan Konten Laporan wajib diisi' });
    }

    if (!isConnected || !sock) {
        return res.status(503).json({
            success: false,
            message: 'Bot WhatsApp belum tersambung.'
        });
    }

    try {
        const jid = formatPhoneToJid(parentPhone);
        const name = studentName || 'Ananda';

        const messageText = 
`*Assalamu'alaikum Warahmatullahi Wabarakatuh* 🌿
_Laporan Pembelajaran & Ibadah Harian (Tarbiyah AI Learn)_

Yth. Bapak/Ibu Wali dari *${name}*,

Berikut adalah ringkasan evaluasi perkembangan belajar dan ibadah ananda:
${reportContent}

Semoga ananda senantiasa istiqomah dalam menuntut ilmu dan berakhlakul karimah.

*Tarbiyah AI Learn Bot*
_Sistem Pendamping Belajar Madrasah Digital_`;

        await sock.sendMessage(jid, { text: messageText });

        res.json({
            success: true,
            message: 'Laporan AI berhasil dikirim ke WhatsApp Orang Tua',
            target: parentPhone
        });
    } catch (error) {
        console.error('Gagal mengirim laporan orang tua:', error);
        res.status(500).json({ success: false, message: 'Gagal mengirim laporan: ' + error.message });
    }
});

// ==========================================
// WEB UI DASHBOARD FOR PAIRING
// ==========================================
app.get('/', (req, res) => {
    res.send(`
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tarbiyah AI Learn - WhatsApp Gateway</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Outfit', sans-serif; }
        body {
            background: linear-gradient(135deg, #0B0E0D 0%, #131A17 50%, #093028 100%);
            color: #F8F9FA;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: rgba(27, 36, 32, 0.85);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(212, 175, 55, 0.35);
            border-radius: 20px;
            padding: 36px;
            width: 100%;
            max-width: 460px;
            box-shadow: 0 16px 40px rgba(0,0,0,0.5);
            text-align: center;
        }
        .logo { font-size: 26px; font-weight: 700; color: #D4AF37; letter-spacing: 2px; }
        .subtitle { font-size: 13px; color: #8FCE00; letter-spacing: 3px; font-weight: 600; margin-bottom: 24px; }
        .status-badge {
            display: inline-block;
            padding: 6px 16px;
            border-radius: 50px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 24px;
        }
        .status-connected { background: rgba(76, 175, 80, 0.2); color: #81C784; border: 1px solid #4CAF50; }
        .status-disconnected { background: rgba(229, 57, 53, 0.2); color: #E57373; border: 1px solid #E53935; }
        .input-group { margin-bottom: 18px; text-align: left; }
        label { font-size: 13px; color: #D4AF37; margin-bottom: 6px; display: block; }
        input {
            width: 100%;
            padding: 14px;
            background: #0B0E0D;
            border: 1px solid rgba(212, 175, 55, 0.4);
            border-radius: 12px;
            color: #FFF;
            font-size: 15px;
            outline: none;
        }
        input:focus { border-color: #D4AF37; box-shadow: 0 0 10px rgba(212, 175, 55, 0.3); }
        button {
            width: 100%;
            padding: 14px;
            background: linear-gradient(90deg, #D4AF37, #E5C07B);
            border: none;
            border-radius: 12px;
            color: #0B0E0D;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: 0.2s;
            margin-top: 6px;
        }
        button:hover { opacity: 0.9; transform: translateY(-1px); }
        .code-box {
            margin-top: 24px;
            padding: 18px;
            background: #0B0E0D;
            border: 2px dashed #D4AF37;
            border-radius: 14px;
            display: none;
        }
        .pairing-code { font-size: 28px; font-weight: 800; color: #D4AF37; letter-spacing: 4px; }
        .steps { text-align: left; margin-top: 20px; font-size: 12px; color: #B0BEC5; line-height: 1.6; }
        .steps ol { padding-left: 18px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="logo">TARBIYAH</div>
        <div class="subtitle">WHATSAPP GATEWAY</div>
        
        <div id="statusBadge" class="status-badge status-disconnected">Mengecek Koneksi...</div>

        <div id="pairForm">
            <div class="input-group">
                <label>Nomor WhatsApp Pengirim (Bot):</label>
                <input type="tel" id="phoneNumber" placeholder="Contoh: 08123456789 atau 628123456789" required />
            </div>
            <button id="btnPair" onclick="requestPair()">Minta Kode Pairing</button>
        </div>

        <div id="codeBox" class="code-box">
            <div style="font-size: 12px; color: #8FCE00; margin-bottom: 4px;">KODE PAIRING WHATSAPP:</div>
            <div id="codeDisplay" class="pairing-code">----</div>
            <div class="steps">
                <b>Cara Menghubungkan:</b>
                <ol>
                    <li>Buka WhatsApp di HP Anda.</li>
                    <li>Ketuk <b>Perangkat Tertaut</b> > <b>Tautkan Perangkat</b>.</li>
                    <li>Pilih <b>Tautkan dengan nomor telepon saja</b>.</li>
                    <li>Masukkan kode di atas sebelum 2 menit.</li>
                </ol>
            </div>
        </div>
    </div>

    <script>
        async function checkStatus() {
            try {
                const res = await fetch('/api/status');
                const data = await res.json();
                const badge = document.getElementById('statusBadge');
                if (data.connected) {
                    badge.className = 'status-badge status-connected';
                    badge.innerHTML = '🟢 WhatsApp Terhubung (' + (data.botNumber || 'Aktif') + ')';
                    document.getElementById('pairForm').style.display = 'none';
                    document.getElementById('codeBox').style.display = 'none';
                } else {
                    badge.className = 'status-badge status-disconnected';
                    badge.innerHTML = '🔴 Belum Terhubung (Perlu Pairing)';
                    document.getElementById('pairForm').style.display = 'block';
                }
            } catch (e) {
                console.error(e);
            }
        }

        async function requestPair() {
            const phone = document.getElementById('phoneNumber').value.trim();
            if (!phone) return alert('Silakan masukkan nomor WhatsApp Anda!');
            
            const btn = document.getElementById('btnPair');
            btn.disabled = true;
            btn.innerText = 'Meminta Kode...';

            try {
                const res = await fetch('/api/pair', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ phone })
                });
                const data = await res.json();
                if (data.success && data.pairingCode) {
                    document.getElementById('codeBox').style.display = 'block';
                    document.getElementById('codeDisplay').innerText = data.pairingCode;
                } else {
                    alert(data.message || 'Gagal membuat kode pairing');
                }
            } catch (e) {
                alert('Terjadi kesalahan: ' + e.message);
            } finally {
                btn.disabled = false;
                btn.innerText = 'Minta Kode Pairing';
            }
        }

        checkStatus();
        setInterval(checkStatus, 5000);
    </script>
</body>
</html>
    `);
});

app.listen(PORT, () => {
    console.log(`🚀 Tarbiyah WhatsApp Gateway berjalan di port ${PORT}`);
});
