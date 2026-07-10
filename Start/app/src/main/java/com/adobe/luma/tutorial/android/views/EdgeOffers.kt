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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.ContentItem
import com.adobe.luma.tutorial.android.models.Decision
import com.adobe.luma.tutorial.android.models.OfferItem
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.marketing.mobile.optimize.DecisionScope
import com.adobe.marketing.mobile.optimize.Optimize
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Composable
fun EdgeOffersView(decision: Decision, navController: NavController) {
    val currentEcid = MobileSDK.shared.ecid.value
    var offersOD by remember { mutableStateOf(listOf<OfferItem>()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // recompose the view when the number of received offers changes


    Text(
        text = "Decision: ${decision.name}".toUpperCase(Locale.current),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 30.dp)
    )
    Card(
        colors = CardColors(
            Color.White,
            Color.Black,
            Color.Transparent,
            Color.Transparent
        ),
        modifier = Modifier
            .height(350.dp)
            .width(350.dp)
            .padding(horizontal = 20.dp)
    ) {
        Column {
            if (offersOD.isEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.aep_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = true) {
                            offersOD = emptyList()
                            scope.launch {
                                updatePropositionsOD(
                                    currentEcid,
                                    decision.activityId,
                                    decision.placementId,
                                    decision.itemCount
                                )
                                offersOD = onPropositionsUpdateOD(
                                    decision.activityId,
                                    decision.placementId,
                                    decision.itemCount
                                )
                            }
                        }
                )
            } else {
                LazyColumn {
                    items(offersOD) { offerItem ->
                        Column(modifier = Modifier.clickable(enabled = true) {
                            showInfoSheet = true
                        }) {
                            val painter: Painter =
                                rememberAsyncImagePainter(offerItem.content.image)
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                            )
                            Text(
                                text = offerItem.content.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text = offerItem.content.text,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                            // show an info icon under the last offer displayed
                            if (offersOD.indexOf(offerItem) == offersOD.size - 1) {
                                IconButton(
                                    onClick = { showInfoSheet = true },
                                    enabled = true,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        tint = Color.Blue,
                                        contentDescription = "Info"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Text(
        text = "${offersOD.size} offer(s) returned for this decision…".toUpperCase(Locale.current),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 30.dp)
    )
    Spacer(modifier = Modifier.height(20.dp))

    if (showInfoSheet) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showInfoSheet = false },
            title = { Text("Info") },
            text = {
                Text(
                    """
                    PARAMETERS FOR DECISION
                    activityId: ${decision.activityId}
                    placementId: ${decision.placementId}
                    itemCount: ${decision.itemCount}
                    
                    RESPONSE (offer objects)
                    $offersOD
                    """.trimIndent(),
                    fontSize = 10.sp,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoSheet = false }) {
                    Text("OK")
                }
            }
        )
    }
}

suspend fun updatePropositionsOD(
    ecid: String,
    activityId: String,
    placementId: String,
    itemCount: Int
) {
    MobileSDK.shared.updatePropositionsOD(ecid, activityId, placementId, itemCount)
}

fun onPropositionsUpdateOD(
    activityId: String,
    placementId: String,
    itemCount: Int
): List<OfferItem> {
    val offersOD = arrayListOf<OfferItem>()
    val decisionScope = DecisionScope(activityId, placementId, itemCount)
    val latch = CountDownLatch(1)
    Optimize.onPropositionsUpdate { propositions ->
        propositions[decisionScope]?.let { optimizeProposition ->
            for (offer in optimizeProposition.offers) {
                val contentJson = JSONObject(offer.content)
                offersOD.add(
                    OfferItem(
                        offer,
                        content = ContentItem(
                            title = contentJson.get("title") as String,
                            text = contentJson.get("text") as String,
                            image = contentJson.get("image") as String
                        )
                    )
                )
            }
        }
        latch.countDown()
    }
    latch.await(2000, TimeUnit.MILLISECONDS)
    return offersOD
}