package com.tarbiyah.ailearn.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.tarbiyah.ailearn.utils.Constants

/**
 * AppBlockerService - Accessibility Service untuk memblokir aplikasi lain
 * saat waktu belajar atau waktu sholat aktif.
 *
 * STUB: Implementasi penuh memerlukan integrasi dengan scheduler
 * dan kondisi lock yang ditentukan oleh server/Firebase.
 */
class AppBlockerService : AccessibilityService() {

    private var isBlockingActive = false
    private var allowedPackage = Constants.APP_PACKAGE_NAME

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isBlockingActive) return

        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = it.packageName?.toString() ?: return

                // If another app is opened and it's not our app, redirect back
                if (packageName != allowedPackage &&
                    packageName != "com.android.systemui" &&
                    packageName != "com.android.launcher") {
                    redirectToApp()
                }
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service is connected and ready
    }

    private fun redirectToApp() {
        val intent = packageManager.getLaunchIntentForPackage(allowedPackage)
        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(this)
        }
    }

    fun activateBlocking() {
        isBlockingActive = true
    }

    fun deactivateBlocking() {
        isBlockingActive = false
    }
}
