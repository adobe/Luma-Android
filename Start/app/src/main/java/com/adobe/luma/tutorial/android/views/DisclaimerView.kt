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

package com.adobe.luma.tutorial.android.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.adobe.luma.tutorial.android.models.MobileSDK

@Composable
fun DisclaimerView(navController: NavController) {
    val context = LocalContext.current
    var continueButtonEnabled by remember { mutableStateOf(false) }
    var showPersonalizationWarning by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { status: Boolean ->
        // Add consent based on authorization
        if (status) {
            showPersonalizationWarning = false

            // Set consent to yes

        } else {
            Toast.makeText(
                context,
                "You will not receive offers and location tracking will be disabled.",
                Toast.LENGTH_LONG
            ).show()
            showPersonalizationWarning = true

            // Set consent to no

        }
        continueButtonEnabled = true
    }

    LaunchedEffect(Unit) {
        // Load general configuration
        MobileSDK.shared.loadGeneral(context, "")
        // Track view screen
        MobileSDK.shared.sendTrackScreenEvent("luma: content: android: us: en: disclaimer")
        if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            continueButtonEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(MobileSDK.shared.brandLogo.value),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        Text(
            buildAnnotatedString {
                append("Welcome to the ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(MobileSDK.shared.brandName.value)
                }
                append(
                    " Android Sample App," +
                            "\nshowing how to use the Adobe Experience Platform Mobile SDK…"
                )
            },
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Spacer(modifier = Modifier.size(75.dp))


        if (!showPersonalizationWarning) {
            Text(
                text = "This app is to illustrate how to use the Adobe Experience Platform Mobile SDK in an Android application. " +
                        "\nPress the \"Show permission dialog\" button to give your tracking preference. " +
                        "\nSelect \"While using the app\" to allow location tracking and collect events which will enable personalization (offers, push notification messages) in the app. " +
                        "\nSelect \"Don't allow\" if you do not want the app to track your activity and collect events; you will not receive personalized offers and/or messages.",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        Button(
            onClick = {
                when (PackageManager.PERMISSION_GRANTED) {
                    context.checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) -> {
                        showPersonalizationWarning = false
                        continueButtonEnabled = true
                        MobileSDK.shared.updateConsent("y")
                    }

                    else -> {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            },
            enabled = MobileSDK.shared.trackingEnabled == TrackingStatus.NOT_DETERMINED || MobileSDK.shared.trackingEnabled == TrackingStatus.DENIED
        ) {
            Text("Show permission dialog")
        }
        if (showPersonalizationWarning) {
            Text(
                text = "Location permission has been denied. If it is denied more than once, then the device settings must be entered to manually enable location tracking." +
                        "The \"Open device settings\" below can be used to navigate to the device settings to do so.",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        Text(
            text = "If testing on Android Q or newer, you must enable \"Allow all the time\" in the device settings for the Luma app" +
                    " to allow location tracking.",
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = {
            val permissionsToRequest: String =
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            launcher.launch(permissionsToRequest)
        }) {
            Text("Open device settings")
        }
        Button(enabled = continueButtonEnabled, onClick = {
            when (PackageManager.PERMISSION_GRANTED) {
                context.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    MobileSDK.shared.updateTrackingStatus(TrackingStatus.AUTHORIZED)
                }
            }
            Toast.makeText(
                context,
                "Tracking status: ${MobileSDK.shared.trackingEnabled}",
                Toast.LENGTH_LONG
            ).show()
            navController.navigate(Routes.ContentView.route)
        }) {
            Text("Continue")
        }
    }

}


enum class TrackingStatus {
    AUTHORIZED, DENIED, NOT_DETERMINED
}