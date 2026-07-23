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

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavController) {
    val context = LocalContext.current
    var showBadgeForUser by remember { mutableStateOf(false) }
    var showConfigAlert by remember { mutableStateOf(false) }
    var showLoginSheet by remember { mutableStateOf(false) }
    MobileSDK.shared.GetIdentities()

    if (showConfigAlert) {
        AlertDialog(
            onDismissRequest = { showConfigAlert = false },
            title = { Text("App Needs Configuration!") },
            text = { Text("Go to Config to configure the app…") },
            confirmButton = {
                Button(onClick = { showConfigAlert = false }) {
                    Text("OK")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        // Track view screen
        MobileCore.trackState("luma: content: android: us: en: home", null)

        // Get attributes

        // Ask status of consents

    }

    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = {
                showLoginSheet = true
            },
            content = {
                if (showBadgeForUser === true) {
                    Icon(
                        painterResource(id = R.drawable.ic_person_badge),
                        contentDescription = "Login icon"
                    )
                }
                else {
                    Icon(
                        painterResource(id = R.drawable.ic_person),
                        contentDescription = "Login icon"
                    )
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 40.sp) }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    colors = CardColors(
                        Color.White,
                        Color.Black,
                        Color.Transparent,
                        Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(MobileSDK.shared.brandLogo.value),
                        contentDescription = "brandLogo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxHeight(0.2f)
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                    )

                    Text(
                        "Welcome to the...",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        MobileSDK.shared.brandName.value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Android Sample App!\n",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Showing how to use the",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Adobe Experience Platform Mobile SDK…",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Identities",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Card(
                    colors = CardColors(
                        Color.White,
                        Color.Black,
                        Color.Transparent,
                        Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    IdentityRow(
                        label = "ECID:",
                        value = MobileSDK.shared.ecid.value,
                        context = context
                    )
                    IdentityRow(
                        label = "Email:",
                        value = MobileSDK.shared.currentEmailId.value,
                        context = context
                    )
                    IdentityRow(
                        label = "CRM ID:",
                        value = MobileSDK.shared.currentCRMId.value,
                        context = context
                    )
                }
            }

            if (showLoginSheet) {
                LoginSheet { showLoginSheet = false }
            }
        }
    )
}

@Composable
fun IdentityRow(label: String, value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        ClickableText(
            text = AnnotatedString(value.ifEmpty { "not available" }),
            onClick = {
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                // Copy to clipboard logic
            },
            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
        )
    }
}