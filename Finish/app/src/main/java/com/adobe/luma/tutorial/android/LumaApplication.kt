/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.luma.tutorial.android

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.luma.tutorial.android.utils.SettingsBundleHelper
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.Places
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.UserProfile
import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.optimize.Optimize
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.FirebaseApp


class LumaApplication : Application() {
    private var environmentFileId = "b5cbd1a1220e/1857ef6cacb5/launch-2594f26b23cd-development"

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        MobileCore.setLogLevel(LoggingMode.ERROR)
        MobileCore.setApplication(this)

        // Define extensions
        val extensions = listOf(
            Identity.EXTENSION,
            Lifecycle.EXTENSION,
            Signal.EXTENSION,
            Edge.EXTENSION,
            Consent.EXTENSION,
            UserProfile.EXTENSION,
            Places.EXTENSION,
            Messaging.EXTENSION,
            Optimize.EXTENSION,
            Assurance.EXTENSION
        )


        // Register extensions
        MobileCore.registerExtensions(extensions) {
            // Use the environment file id assigned to this application via Adobe Experience Platform Data Collection
            Log.i("Luma", "Using mobile config: $environmentFileId")
            MobileCore.configureWithAppID(environmentFileId)

            // set this to true when testing on your device, default is false.
            //MobileCore.updateConfiguration(mapOf("messaging.useSandbox" to true))

            // assume unknown, adapt to your needs.
            MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN)
        }

        // only start lifecycle if the application is not in the background
        // see LumaActivityLifecycleCallbacks.onActivityResumed
        registerActivityLifecycleCallbacks(LumaActivityLifecycleCallbacks())

        // update version and build
        Log.i("Luma", "Updating version and build number...")
        val settingsBundleHelper = SettingsBundleHelper(this)
        settingsBundleHelper.setVersionAndBuildNumber()

        createNotificationChannel()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            // Log and toast
            if (task.isSuccessful) {
                // Get new FCM registration token
                val token = task.result
                Log.i("Luma", "Android Firebase token :: $token")

                // Send push token to Mobile SDK
                MobileCore.setPushIdentifier(token)

                // Store the push token
                MobileSDK.shared.deviceToken.value = token
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Luma Channel"
            val descriptionText = "Luma Notification Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("LUMA_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun handleDeeplink(deeplink: String?) {
        // Called when the app in background is opened with a deep link.
        if (deeplink.isNullOrEmpty()) {
            Log.w("Luma", "Deeplink is null or empty")
            return
        }

        Log.i("Luma", "Handling deeplink: $deeplink")
        Assurance.startSession(deeplink)
    }

    fun scheduleNotification() {
        val builder = NotificationCompat.Builder(this, "LUMA_CHANNEL_ID")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Notification Title")
            .setContentText("This is an example of how to create a notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    // App lifecycle callbacks
    class LumaActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.i("Luma", "onActivityCreated: " + activity.localClassName)
        }

        override fun onActivityStarted(activity: Activity) {
            Log.i("Luma", "onActivityStarted: " + activity.localClassName)
        }

        override fun onActivityResumed(activity: Activity) {
            Log.i("Luma", "onActivityResumed: " + activity.localClassName)
            // When in foreground start lifecycle data collection
            MobileCore.lifecycleStart(null)
        }

        override fun onActivityPaused(activity: Activity) {
            Log.i("Luma", "onActivityPaused: " + activity.localClassName)
            // When in background pause lifecycle data collection
            MobileCore.lifecyclePause()
        }

        override fun onActivityStopped(activity: Activity) {
            Log.i("Luma", "onActivityStopped: " + activity.localClassName)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            Log.i("Luma", "onActivitySaveInstanceState: " + activity.localClassName)
        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.i("Luma", "onActivityDestroyed: " + activity.localClassName)
        }
    }
}
