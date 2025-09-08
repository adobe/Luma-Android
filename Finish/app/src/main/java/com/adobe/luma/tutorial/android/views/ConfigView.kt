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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.MobileSDK
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigView(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var environmentFileId by remember { mutableStateOf("b5cbd1a1220e/1857ef6cacb5/launch-2594f26b23cd-development") }
    var configLocation by remember { mutableStateOf("") }
    val testPushEventType by remember { mutableStateOf("application.test") }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showConfigSections by remember { mutableStateOf(true) }
    var showTermsOfServiceSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 40.sp) },
                actions = {
                    IconButton(onClick = {
                        environmentFileId =
                            "2a518741ab24/ec01f7dc7ed6/launch-384206a7fc37-development"
                        configLocation = ""
                        scope.launch {
                            MobileSDK.shared.loadGeneral(context, configLocation)
                        }
                        showRestartDialog = true
                    }, content = {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = "Settings"
                        )
                    }
                    )
                })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (showConfigSections) {
                    val clickCount = remember { mutableStateOf(0) }
                    Column(modifier = Modifier.clickable {
                        clickCount.value += 1
                        if (clickCount.value >= 4) {
                            showConfigSections = false
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "AEP Data Collection",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 25.dp)
                            )
                        }
                        ConfigurationFromAppId(
                            environmentFileId = environmentFileId,
                            onConfigClick = { showRestartDialog = true }
                        )
                        ConfigurationFromPath(
                            configLocation = configLocation,
                            onConfigLocationChange = { configLocation = it },
                            onConfigClick = {
                                scope.launch {
                                    MobileSDK.shared.loadGeneral(context, configLocation)
                                }
                                showRestartDialog = true
                            }
                        )
                    }
                }

                if (MobileSDK.shared.currentEmailId.value.isNotEmpty()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Test",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 25.dp)
                            )
                        }
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
                            TestSection(
                                onInAppMessageClick = {
                                    // Setting parameters and calling function to send in-app message
                                    MobileSDK.shared.sendTrackAction(
                                        "in-app",
                                        mapOf("showMessage" to "true")
                                    )
                                },
                                onPushNotificationClick = {
                                    // Setting parameters and calling function to send push notification
                                    val eventType = testPushEventType
                                    val applicationId = context.packageName
                                    scope.launch {
                                        MobileSDK.shared.sendTestPushEvent(
                                            applicationId,
                                            eventType
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Application",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 25.dp)
                    )
                }
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
                    ApplicationSection(
                        onTermsOfUseClick = { showTermsOfServiceSheet = true },
                        onAppSettingsClick = {
                            val intent =
                                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        }
                    )
                }

                if (showRestartDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestartDialog = false },
                        title = { Text("App Needs Restart!") },
                        text = { Text("Restart the app to pick up the new configuration…") },
                        confirmButton = {
                            Button(onClick = { showRestartDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                if (showTermsOfServiceSheet) {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        TermsOfServiceSheet(onDismiss = { showTermsOfServiceSheet = false })
                    }
                }
            }
        }
    )
}

@Composable
fun ConfigurationFromAppId(
    environmentFileId: String,
    onConfigClick: () -> Unit
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
        Row {
            Text(
                environmentFileId,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            Button(
                onClick = onConfigClick,
                enabled = environmentFileId.isNotEmpty()
            ) {
                Text("Config")
            }
        }
    }
    val environmentFileIdMessage: String = if (environmentFileId.isEmpty()) {
        "Provide a valid environment file id from your AEP Data Collection mobile property"
    } else {
        "Environment file id for your mobile property in Adobe Experience Platform Data Collection…"
    }
    Text(
        environmentFileIdMessage,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ConfigurationFromPath(
    configLocation: String,
    onConfigLocationChange: (String) -> Unit,
    onConfigClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Text(
        "Configuration Location",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 25.dp)
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
            .padding(horizontal = 20.dp)
    ) {
        Row {
            OutlinedTextField(
                value = configLocation,
                onValueChange = onConfigLocationChange,
                label = { Text("Path") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier
                    .weight(1.0f)
                    .padding(horizontal = 8.dp)
            )
            Button(
                onClick = onConfigClick,
                modifier = Modifier
                    .padding(vertical = 12.dp)
            ) {
                Text("Config")
            }
        }
        val tenant = MobileSDK.shared.tenant.value
        val sandbox = MobileSDK.shared.sandbox.value

        if (tenant.isNotEmpty() && sandbox.isNotEmpty()) {
            ConfigurationDetails(
                onExpandChange = { isExpanded = it },
                isExpanded = isExpanded,
                tenant = tenant,
                sandbox = sandbox
            )
        } else {
            Text(
                "Configuration Details Missing",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    val configText: String = if (configLocation.isEmpty()) {
        "App is using internal configuration files (general, products, (i)beacons, geofences))"
    } else {
        "App is using remote configuration files (general, products, (i)beacons, geofences)…"
    }
    Text(
        configText,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ConfigurationDetails(
    onExpandChange: (Boolean) -> Unit,
    isExpanded: Boolean,
    tenant: String,
    sandbox: String
) {
    Text(
        "Configuration Details (Click to view)",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.clickable { onExpandChange(!isExpanded) })
    if (isExpanded) {
        val brandName = MobileSDK.shared.brandName.value
        val ldap = MobileSDK.shared.ldap.value
        val emailDomain = MobileSDK.shared.emailDomain.value
        val tms = MobileSDK.shared.tms.value
        val currentDeviceToken = MobileSDK.shared.deviceToken.value
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
            Row {
                Text("Brand: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(brandName, textAlign = TextAlign.End)
            }
            Row {
                Text("LDAP: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(ldap, textAlign = TextAlign.End)
            }
            Row {
                Text("Email Domain: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(emailDomain, textAlign = TextAlign.End)
            }
            Row {
                Text("TMS: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(tms, textAlign = TextAlign.End)
            }
            Row {
                Text("Tenant: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(tenant, textAlign = TextAlign.End)
            }
            Row {
                Text("Sandbox: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(sandbox, textAlign = TextAlign.End)
            }
            Row {
                Text("Device Token: ", textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    currentDeviceToken,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}

@Composable
fun TestSection(
    onInAppMessageClick: () -> Unit,
    onPushNotificationClick: () -> Unit
) {
    Row {
        TestProfileToggle()
    }
    Row {
        Button(onClick = onInAppMessageClick, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("In-App Message")
        }
        Button(onClick = onPushNotificationClick) {
            Text(" Push Notification")
        }
    }
}

@Composable
fun TestProfileToggle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Test Profile")
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = false,
            onCheckedChange = { value ->
                MobileSDK.shared.sendTrackAction(
                    action = "updateProfile",
                    data = mapOf(
                        "ecid" to MobileSDK.shared.ecid.value,
                        "testProfile" to value.toString()
                    )
                )
            },
            enabled = true
        )
    }

}


@Composable
fun ApplicationSection(
    onTermsOfUseClick: () -> Unit,
    onAppSettingsClick: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Terms of Use")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onTermsOfUseClick) {
                Text("View...")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tracking is allowed…")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onAppSettingsClick) {
                Text("App Settings…")
            }
        }
    }
}