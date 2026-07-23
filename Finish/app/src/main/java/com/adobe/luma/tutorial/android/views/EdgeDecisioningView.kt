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

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.marketing.mobile.messaging.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeDecisioningView(navController: NavController) {
    val context = LocalContext.current
    // Surfaces are derived from the configured decisioning surface.
    // Add more entries here as needed for different decisioning placements.
    var surfaces by remember { mutableStateOf(listOf<Pair<Surface, String>>()) }

    LaunchedEffect(Unit) {
        MobileSDK.shared.loadGeneral(context, "")
        val surfacePath = MobileSDK.shared.decisioningSurface.value
        if (surfacePath.isNotEmpty()) {
            surfaces = listOf(Surface(surfacePath) to "Offers Location")
        }
        Log.i("EdgeDecisioningView", "Loaded ${surfaces.size} decisioning surface(s)…")
    }

    DisposableEffect(Unit) {
        MobileSDK.shared.sendTrackScreenEvent("luma: content: android: us: en: decisioningEdge")
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalisation", fontWeight = FontWeight.Bold, fontSize = 40.sp) }
            )
        },
        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn {
                    items(surfaces) { (surface, name) ->
                        EdgeDecisioningOffersView(surface = surface, surfaceName = name)
                    }
                }
            }
        })
}
