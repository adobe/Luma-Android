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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.PlatformLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.Product
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.luma.tutorial.android.utils.ProductLoader
import kotlinx.coroutines.launch
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductView(productId: String, navController: NavController) {
    val product = ProductLoader.loadedProductMap[productId] ?: Product.example
    var showAddToCartDialog by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showSaveForLaterDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Send productViews commerce experience event

    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Row {
                        IconButton(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "selectedView",
                                "Products"
                            )
                            navController.navigate(Routes.ContentView.route)
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back"
                            )
                        }
                        if (product.name.length >= 20) {
                            Text("")
                        } else if (product.name.length >= 15) {
                            Text(
                                "Back",
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Left,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            Text(
                                "Products",
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Justify,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (MobileSDK.shared.trackingEnabled == TrackingStatus.AUTHORIZED) {
                            scope.launch {
                                // Send saveForLater commerce experience event

                            }
                        }
                        showSaveForLaterDialog = true
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_heart),
                            contentDescription = "Save for later"
                        )
                    }
                    IconButton(onClick = {
                        if (MobileSDK.shared.trackingEnabled == TrackingStatus.AUTHORIZED) {
                            scope.launch {
                                // Send productListAdds commerce experience event

                            }
                        }
                        showAddToCartDialog = true
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_cart),
                            contentDescription = "Add to cart"
                        )
                    }
                    IconButton(onClick = {
                        if (MobileSDK.shared.trackingEnabled == TrackingStatus.AUTHORIZED) {
                            scope.launch {
                                // Send purchase commerce experience event


                                // Update attributes


                            }
                        }
                        showPurchaseDialog = true
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_creditcard),
                            contentDescription = "Purchase"
                        )
                    }
                }
            )
        }, content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .height(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val painter: Painter = rememberAsyncImagePainter(product.imageURL)
                Image(
                    painter = painter,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .clipToBounds()
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.category.replace(":", " ‣ "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    if (product.color.isNotEmpty()) {
                        Icon(
                            painterResource(id = R.drawable.ic_filled_square),
                            contentDescription = "Filled square icon",
                            tint = product.color.toColor()
                        )
                    } else {
                        // place a transparent icon so the row alignment is consistent
                        Icon(
                            painterResource(id = R.drawable.ic_filled_square),
                            contentDescription = "Filled square icon",
                            tint = Color.Transparent
                        )
                    }
                    val numberFormat = NumberFormat.getCurrencyInstance(PlatformLocale.getDefault())
                    Text(
                        text = "${numberFormat.currency?.symbol}${
                            String.format(
                                PlatformLocale.getDefault(),
                                "%.2f",
                                product.price
                            )
                        }",
                        fontWeight = FontWeight.Bold
                    )
                    if (product.size.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp)
                                .fillMaxWidth()
                                .background(color = Color.DarkGray, shape = RectangleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when (product.size) {
                                "xl" -> {
                                    Text(
                                        "XL",
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }

                                "l" -> {
                                    Text(
                                        "L",
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }

                                "s" -> {
                                    Text(
                                        "S",
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }

                                "xs" -> {
                                    Text(
                                        "XS",
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }

                                else -> {
                                    Text(
                                        product.size,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // place a transparent box so the row alignment is consistent
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp)
                                .fillMaxWidth()
                                .background(color = Color.Transparent, shape = RectangleShape)
                        )
                    }
                }

            }

            if (showAddToCartDialog) {
                AlertDialog(
                    onDismissRequest = { showAddToCartDialog = false },
                    title = { Text("Added to basket") },
                    text = { Text("The selected item is added to your basket…") },
                    confirmButton = {
                        Button(onClick = { showAddToCartDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showPurchaseDialog) {
                AlertDialog(
                    onDismissRequest = { showPurchaseDialog = false },
                    title = { Text("Purchases") },
                    text = { Text("The selected item is purchased…") },
                    confirmButton = {
                        Button(onClick = { showPurchaseDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showSaveForLaterDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveForLaterDialog = false },
                    title = { Text("Saved for later") },
                    text = { Text("The selected item is saved to your wishlist…") },
                    confirmButton = {
                        Button(onClick = { showSaveForLaterDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        })
}

private fun String.toColor(): Color {
    return when (this) {
        "red" -> Color.Red
        "green" -> Color.Green
        "blue" -> Color.Blue
        "yellow" -> Color.Yellow
        "purple" -> Color.Magenta
        "orange" -> Color(0xFFFFA500)
        "black" -> Color.Black
        "white" -> Color.White
        "gray" -> Color.Gray
        "cyan" -> Color.Cyan
        "pink" -> Color(0xFFFFC0CB)
        "brown" -> Color(0xFFA52A2A)
        "magenta" -> Color.Magenta
        "teal" -> Color(0xFF008080)
        "olive" -> Color(0xFF808000)
        "navy" -> Color(0xFF000080)
        "maroon" -> Color(0xFF800000)
        "lime" -> Color(0xFF00FF00)
        "silver" -> Color(0xFFC0C0C0)
        "darkgray" -> Color(0xFFA9A9A9)
        "lightgray" -> Color(0xFFD3D3D3)
        else -> Color.Gray
    }
}