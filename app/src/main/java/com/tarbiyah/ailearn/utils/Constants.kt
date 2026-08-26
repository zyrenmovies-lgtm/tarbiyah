package com.tarbiyah.ailearn.utils

object Constants {
    const val APP_PACKAGE_NAME = "com.tarbiyah.ailearn"

    // Fonnte API Token untuk kirim OTP WhatsApp
    const val FONNTE_TOKEN = "GfK6cqom3hxbhHv7YxCJ"

    // Firebase Database Paths
    const val DB_USERS = "users"
    const val DB_FEED = "feed"
    const val DB_POINTS = "points"
    const val DB_JOURNALS = "journals"
    const val DB_PRAYER_SCHEDULE = "prayer_schedule"
    const val DB_STUDY_SESSIONS = "study_sessions"

    // SharedPreferences Keys
    const val PREF_NAME = "tarbiyah_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_FULL_NAME = "full_name"
    const val PREF_EDUCATION_LEVEL = "education_level"
    const val PREF_GRADE = "grade"
    const val PREF_SCHOOL = "school"
    const val PREF_LATITUDE = "latitude"
    const val PREF_LONGITUDE = "longitude"
    const val PREF_STUDENT_PHONE = "student_phone"
    const val PREF_PARENT_PHONE = "parent_phone"
    const val PREF_IS_PHONE_VERIFIED = "is_phone_verified"
    const val PREF_IS_LOGGED_IN = "is_logged_in"

    // Prayer Times
    val PRAYER_NAMES = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")

    // Education Levels
    val EDUCATION_LEVELS = listOf(
        "RA (Raudlatul Athfal)",
        "MI (Madrasah Ibtidaiyah)",
        "MTs (Madrasah Tsanawiyah)",
        "MA (Madrasah Aliyah)"
    )

    // Provinces (sample)
    val PROVINCES = listOf(
        "Aceh", "Sumatera Utara", "Sumatera Barat", "Riau", "Kepulauan Riau",
        "Jambi", "Sumatera Selatan", "Bangka Belitung", "Bengkulu", "Lampung",
        "DKI Jakarta", "Jawa Barat", "Banten", "Jawa Tengah", "DI Yogyakarta",
        "Jawa Timur", "Bali", "Nusa Tenggara Barat", "Nusa Tenggara Timur",
        "Kalimantan Barat", "Kalimantan Tengah", "Kalimantan Selatan",
        "Kalimantan Timur", "Kalimantan Utara", "Sulawesi Utara", "Gorontalo",
        "Sulawesi Tengah", "Sulawesi Barat", "Sulawesi Selatan", "Sulawesi Tenggara",
        "Maluku", "Maluku Utara", "Papua", "Papua Barat"
    )

    // Point values
    const val POINTS_PRAYER = 100
    const val POINTS_DHUHA = 50
    const val POINTS_TILAWAH = 30
    const val POINTS_STUDY_SESSION = 80
    const val POINTS_STUDY_COMPLETE = 200
    const val POINTS_SEDEKAH = 150

    const val PENALTY_TOXIC = -200
    const val PENALTY_NO_SALAM = -10
    const val PENALTY_QUIT_EXAM = -100
}
