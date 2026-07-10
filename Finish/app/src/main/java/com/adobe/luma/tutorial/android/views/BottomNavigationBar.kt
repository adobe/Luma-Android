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

import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.MobileSDK

@Composable
fun BottomNavigationBar(setSelectedView: (String) -> Unit) {
    var showGeofences by remember { mutableStateOf(false) }
    var showBeacons by remember { mutableStateOf(false) }
    var showProducts by remember { mutableStateOf(false) }
    var showPersonalisation by remember { mutableStateOf(false) }
    // Reactive read: recomposes once the configuration is loaded into the shared SDK state
    val showDecisioning = MobileSDK.shared.showDecisioning.value

    LaunchedEffect(Unit) {
        if (MobileSDK.shared.trackingEnabled == TrackingStatus.AUTHORIZED) {
            showGeofences = true
            showBeacons = true
            showPersonalisation = true
        }
        showProducts = true
    }

    BottomAppBar(
        content = {
            BottomNavigationItem(
                selected = true,
                onClick = { setSelectedView("Home") },
                label = { Text("Home") },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_home),
                        contentDescription = null
                    )
                },
                selectedContentColor = Color.Blue
            )
            if (showProducts) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { setSelectedView("Products") },
                    label = { Text("Products") },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_cart),
                            contentDescription = null
                        )
                    },
                    selectedContentColor = Color.Blue
                )
            }
            if (showPersonalisation && !showDecisioning) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { setSelectedView("Personalisation") },
                    label = { Text("Personalisation", fontSize = 8.sp) },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_personalization),
                            contentDescription = null
                        )
                    },
                    selectedContentColor = Color.Blue
                )
            }
            if (showPersonalisation && showDecisioning) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { setSelectedView("Decisioning") },
                    label = { Text("Personalisation", fontSize = 8.sp) },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_personalization),
                            contentDescription = null
                        )
                    },
                    selectedContentColor = Color.Blue
                )
            }
            if (showBeacons && showGeofences) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { setSelectedView("Location") },
                    label = { Text("Location") },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_location),
                            contentDescription = null
                        )
                    },
                    selectedContentColor = Color.Blue
                )
            }
            BottomNavigationItem(
                selected = false,
                onClick = { setSelectedView("Settings") },
                label = { Text("Settings") },
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_settings),
                        contentDescription = null
                    )
                },
                selectedContentColor = Color.Blue
            )
        }
    )
}