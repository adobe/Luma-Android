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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.CodeBasedContent
import com.adobe.luma.tutorial.android.models.DecisioningOffer
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.messaging.Proposition
import com.adobe.marketing.mobile.messaging.Surface
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Displays the code-based decisioning offers returned for a single Messaging surface.
 *
 * @param surface The Messaging [Surface] to fetch propositions for
 * @param surfaceName A human-readable name for the surface, shown as a header
 */
@Composable
fun EdgeDecisioningOffersView(surface: Surface, surfaceName: String) {
    var offers by remember { mutableStateOf(listOf<DecisioningOffer>()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var propositionInfo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        MobileSDK.shared.updatePropositionsForSurfaces(listOf(surface))
        // Give the Messaging extension time to retrieve the propositions from the Edge
        delay(500)
        val result = fetchPropositionsForSurface(surface)
        offers = result.offers
        propositionInfo = result.info
    }

    Text(
        text = "Surface " + surfaceName,
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
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column {
            if (offers.isEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.aep_logo),
                    contentDescription = "Reload offers",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = true) {
                            offers = emptyList()
                            scope.launch {
                                MobileSDK.shared.updatePropositionsForSurfaces(listOf(surface))
                                delay(500)
                                val result = fetchPropositionsForSurface(surface)
                                offers = result.offers
                                propositionInfo = result.info
                            }
                        }
                )
            } else {
                Column {
                    offers.forEach { offer ->
                        Column(modifier = Modifier.clickable(enabled = true) {
                            showInfoSheet = true
                        }) {
                            offer.image?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = offer.title,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }
                            offer.title?.let { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                            offer.text?.let { text ->
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                            // show an info icon under the last offer displayed
                            if (offers.indexOf(offer) == offers.size - 1) {
                                IconButton(
                                    onClick = { showInfoSheet = true },
                                    enabled = true,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        tint = Color.Blue,
                                        contentDescription = "Offer information"
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
        text = "${offers.size} offer(s) returned for surface…",
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
                    SURFACE PARAMETERS
                    Surface URI: ${surface.uri}

                    PROPOSITION RESPONSE
                    $propositionInfo
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

/** Result of fetching propositions for a surface: the parsed offers and a debug info string. */
data class DecisioningResult(val offers: List<DecisioningOffer>, val info: String)

/**
 * Retrieves the propositions cached by the Messaging extension for [surface] and parses any
 * code-based content into [DecisioningOffer]s. Blocks briefly while awaiting the SDK callback.
 */
suspend fun fetchPropositionsForSurface(surface: Surface): DecisioningResult =
    withContext(Dispatchers.IO) {
        val allOffers = arrayListOf<DecisioningOffer>()
        val infoText = StringBuilder()
        val latch = CountDownLatch(1)

        Messaging.getPropositionsForSurfaces(listOf(surface)) { propositionsMap ->
            propositionsMap[surface]?.let { propositions ->
                for (proposition in propositions) {
                    infoText.append("Proposition ID: ${proposition.uniqueId}\n")
                    infoText.append("Scope: ${proposition.scope}\n")
                    infoText.append("Items: ${proposition.items.size}\n")
                    allOffers.addAll(parseCodeBasedOffers(proposition, infoText))
                }
            }
            latch.countDown()
        }
        latch.await(2000, TimeUnit.MILLISECONDS)
        Log.i("EdgeDecisioningOffersView", "Total offers to display: ${allOffers.size}")
        DecisioningResult(allOffers, infoText.toString())
    }

/** Parses the JSON code-based content of a proposition's items into [DecisioningOffer]s. */
private fun parseCodeBasedOffers(
    proposition: Proposition,
    infoText: StringBuilder
): List<DecisioningOffer> {
    val gson = Gson()
    val offers = arrayListOf<DecisioningOffer>()
    for (item in proposition.items) {
        val contentMap = item.jsonContentMap ?: continue
        try {
            val content = gson.fromJson(gson.toJson(contentMap), CodeBasedContent::class.java)
            content.offers?.let { parsedOffers ->
                // Generate a stable id for any offer that did not include one
                val withIds = parsedOffers.map { offer ->
                    if (offer.id == null) offer.copy(id = UUID.randomUUID().toString()) else offer
                }
                offers.addAll(withIds)
                infoText.append("Offers: ${withIds.size}\n\n")
                Log.i("EdgeDecisioningOffersView", "Parsed ${withIds.size} offer(s)")
            }
        } catch (e: Exception) {
            Log.e("EdgeDecisioningOffersView", "Failed to parse content: ${e.localizedMessage}")
        }
    }
    return offers
}
