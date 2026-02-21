package com.avinashpatil.app.dishacomputer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for DISHA Computer app.
 * The @HiltAndroidApp annotation triggers Hilt's code generation and
 * serves as the application-level dependency container.
 */
@HiltAndroidApp
class DishaComputerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}