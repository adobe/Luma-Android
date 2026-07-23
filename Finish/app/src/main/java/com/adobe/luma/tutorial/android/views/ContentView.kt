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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.adobe.luma.tutorial.android.models.MobileSDK
import kotlinx.coroutines.launch

/**
 * ContentView composable is the main view of the application.
 * It contains the BottomNavigationBar composable and the content of the currently selected view.
 *
 * @param navController NavController to handle navigation
 */
@Composable
fun ContentView(navController: NavController) {
    val context = LocalContext.current
    var configLocation by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var selectedView by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            MobileSDK.shared.loadGeneral(context, configLocation)
        }
    }

    selectedView =
        navController.previousBackStackEntry?.savedStateHandle?.get("selectedView") ?: "Home"

    Scaffold(
        bottomBar = {
            BottomNavigationBar(setSelectedView = {
                selectedView = it
            })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedView) {
                    "Home" -> {
                        HomeView(navController)
                    }

                    "Products" -> {
                        ProductsView(navController)
                    }

                    "Product" -> {
                        val productSku: String =
                            navController.previousBackStackEntry?.savedStateHandle?.get("sku") ?: ""
                        ProductView(productSku, navController)
                    }

                    "Personalisation" -> {
                        EdgePersonalisationView(navController)
                    }

                    "Decisioning" -> {
                        EdgeDecisioningView(navController)
                    }

                    "Location" -> {
                        LocationView()
                    }

                    "Settings" -> {
                        ConfigView(navController)
                    }
                }
            }
        })
}