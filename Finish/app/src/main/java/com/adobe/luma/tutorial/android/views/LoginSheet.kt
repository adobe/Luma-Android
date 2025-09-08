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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.MobileSDK
import java.util.*

@Composable
fun LoginSheet(onDismiss: () -> Unit) {
    var ldap by remember { mutableStateOf("testUser") }
    var emailDomain by remember { mutableStateOf("gmail.com") }
    var disableLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        disableLogin =
            MobileSDK.shared.currentEmailId.value != "testUser@gmail.com" && MobileSDK.shared.currentEmailId.value.isValidEmail()

        // Send track screen event
        MobileSDK.shared.sendTrackScreenEvent("luma: content: android: us: en: login")
    }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Card(
            modifier = Modifier
                .height(intrinsicSize = IntrinsicSize.Max),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Create Identity",
                fontSize = 24.sp
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = "Identities",
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
            if (disableLogin) {
                Text(
                    "You are identified with email address ${MobileSDK.shared.currentEmailId.value}",
                    fontSize = 12.sp
                )
                Text(
                    "You are identified with CRM ID ${MobileSDK.shared.currentCRMId.value}",
                    fontSize = 12.sp
                )
                Row {
                    Button(onClick = {
                        // Remove identities
                        MobileSDK.shared.removeIdentities(
                            MobileSDK.shared.currentEmailId.value,
                            MobileSDK.shared.currentCRMId.value
                        )
                        onDismiss()
                    }) {
                        Text("Logout")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            } else {
                TextField(
                    value = MobileSDK.shared.currentEmailId.value,
                    onValueChange = { MobileSDK.shared.currentEmailId.value = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = MobileSDK.shared.currentCRMId.value,
                    onValueChange = { MobileSDK.shared.currentCRMId.value = it },
                    label = { Text("CRM ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    Button(onClick = {
                        val dateString = Date().formatDate()
                        val randomNumberString = String.format("%02d", (1..99).random())
                        MobileSDK.shared.currentCRMId.value =
                            UUID.randomUUID().toString().replace("-", "")
                                .lowercase(Locale.getDefault())
                        MobileSDK.shared.currentEmailId.value =
                            "$ldap+$dateString-$randomNumberString@$emailDomain"
                    }) {
                        Text("Generate Random Email")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Update identities
                            MobileSDK.shared.updateIdentities(
                                MobileSDK.shared.currentEmailId.value,
                                MobileSDK.shared.currentCRMId.value
                            )
                            // Send app interaction event
                            MobileSDK.shared.sendAppInteractionEvent("login")

                            onDismiss()
                        },
                        enabled = MobileSDK.shared.currentEmailId.value.isValidEmail()
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}


fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun Date.formatDate(): String {
    val format = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return format.format(this)
}