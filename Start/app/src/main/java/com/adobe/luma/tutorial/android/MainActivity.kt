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

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.adobe.luma.tutorial.android.views.AppNavigation
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "MainActivity"

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                Log.d(
                    LOG_TAG,
                    "Notification permission granted"
                )
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d(
                    LOG_TAG,
                    "Notification Permission: Not granted"
                )
                showNotificationPermissionRationale()
            } else {
                // Directly ask for the permission
                Log.d(
                    LOG_TAG,
                    "Requesting notification permission"
                )
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            Log.d(
                LOG_TAG,
                "Notification permission granted"
            )
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Log.d(
                LOG_TAG,
                "Notification permission granted"
            )
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationPermissionRationale()
                } else {
                    Log.d(
                        LOG_TAG,
                        "Grant notification permission from settings"
                    )
                    Toast.makeText(
                        this,
                        "Grant notification permission from settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Grant notification permission")
            .setMessage("Notification permission is required to show notifications")
            .setPositiveButton("Ok") { dialog: DialogInterface?, which: Int ->
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation(navController = rememberNavController())
        }


        // register push notification
        askNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", token ?: "Token is null")
            } else {
                Log.w("FCM Token", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val deeplink = intent.dataString
        (application as LumaApplication).handleDeeplink(deeplink)
    }
}