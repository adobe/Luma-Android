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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.Product
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.luma.tutorial.android.utils.Network
import com.adobe.luma.tutorial.android.utils.ProductLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsView(navController: NavController) {
    var configLocation by remember { mutableStateOf("") }
    var products by remember { mutableStateOf(listOf<Product>()) }
    val context = LocalContext.current

    val groupedProducts = products.groupBy { it.category }
    val featuredProducts = products.filter { it.featured == true }.shuffled()
    val categories = groupedProducts.keys.sorted().reversed()

    LaunchedEffect(Unit) {
        Network.loadGeneral(context, configLocation)
        products = ProductLoader.loadProducts(context, configLocation)
        Log.i("ProductsView", "Loaded ${products.size} products...")
    }

    DisposableEffect(Unit) {
        MobileSDK.shared.sendTrackScreenEvent("luma: content: android: us: en: products")
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products", fontWeight = FontWeight.Bold, fontSize = 40.sp) }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn {
                    item {
                        Text(
                            text = "★ Featured",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(start = 40.dp)
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
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(8.dp)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                item {
                                    FeaturedProducts(
                                        products = featuredProducts,
                                        navController = navController
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    items(categories) { category ->
                        Text(
                            text = category.replace(":", " ‣ "),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(start = 40.dp)
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
                            for (product in groupedProducts[category] ?: emptyList()) {
                                ProductRow(product = product, navController = navController)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    )
}

@Composable
fun FeaturedProducts(products: List<Product>, navController: NavController) {
    for (product in products) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clickable {
                    navController.currentBackStackEntry?.savedStateHandle?.set("sku", product.sku)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "selectedView",
                        "Product"
                    )
                    navController.navigate(Routes.ContentView.route)
                }) {
            AsyncImage(
                model = product.imageURL,
                contentDescription = product.name,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .weight(1f, true)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                product.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(.20f, false)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProductRow(product: Product, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                navController.currentBackStackEntry?.savedStateHandle?.set("sku", product.sku)
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "selectedView",
                    "Product"
                )
                navController.navigate(Routes.ContentView.route)
            }
    ) {
        AsyncImage(
            model = product.imageURL,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(5.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            product.name, modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp)
        )
        if (product.featured == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star_fill),
                contentDescription = "Featured",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
