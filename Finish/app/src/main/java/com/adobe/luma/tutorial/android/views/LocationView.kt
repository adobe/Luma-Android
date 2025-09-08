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

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.MutableLiveData
import com.adobe.luma.tutorial.android.R
import com.adobe.luma.tutorial.android.models.Beacon
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.luma.tutorial.android.utils.Network
import com.adobe.marketing.mobile.places.PlacesPOI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationView() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationManager.mapCenter, locationManager.zoom)
    }
    var selectedBeacon by remember { mutableStateOf<Beacon?>(null) }
    var pois: List<PlacesPOI> by remember { mutableStateOf(listOf()) }
    val coroutineScope = rememberCoroutineScope()
    var shouldShowGeofenceDialog by remember { mutableStateOf(false) }
    var shouldShowBeacons by remember { mutableStateOf(false) }
    var shouldShowBeaconDetails by remember { mutableStateOf(false) }
    var selectedLocation: Location? = null
    var region: Geofence?
    var regionIdentifier = ""

    LaunchedEffect(locationManager.pointsOfInterest.size, locationManager.beacons.value?.size) {
        locationManager.startScanning()
        locationManager.beacons = MutableLiveData(Network.loadBeacons(context, ""))
        MobileSDK.shared.sendTrackScreenEvent("luma: content: android: us: en: location")
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(locationManager.mapCenter.latitude, locationManager.mapCenter.longitude),
            12.0f
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location", fontWeight = FontWeight.Bold, fontSize = 40.sp) }
            )
        },
        content = { paddingValues ->
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = {
                    selectedLocation = Location("current").apply {
                        latitude = it.latitude
                        longitude = it.longitude
                        accuracy = 100f
                    }
                }
            ) {
                locationManager.pointsOfInterest.forEach { poi ->
                    Circle(
                        center = LatLng(poi.latitude, poi.longitude),
                        radius = poi.radius.toDouble(),
                        fillColor = Color.Transparent,
                        strokeColor = Color.Red,
                        strokeWidth = 5.0f,
                        onClick = {
                            selectedLocation = Location(poi.name).apply {
                                latitude = poi.latitude
                                longitude = poi.longitude
                                accuracy = poi.radius.toFloat()
                            }
                            shouldShowGeofenceDialog = true
                        },
                        clickable = true,
                        visible = true
                    )
                }

                if (shouldShowBeacons) {
                    locationManager.beacons.value?.forEach { beacon ->
                        val latLng = locationManager.convertBeaconToLatLng(context, beacon)
                        if (latLng != null) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(latLng.latitude, latLng.longitude),
                                12.0f
                            )
                            Circle(
                                center = LatLng(latLng.latitude, latLng.longitude),
                                radius = 1000.0,
                                fillColor = Color.Transparent,
                                strokeColor = Color.Blue,
                                strokeWidth = 5.0f,
                                onClick = {
                                    selectedBeacon = beacon
                                    shouldShowBeaconDetails = true
                                },
                                clickable = true,
                                visible = true
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(paddingValues)) {
                Section(
                    title = { Text("Geofences") },
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        locationManager.setMapCenter()
                                        cameraPositionState.position =
                                            CameraPosition.fromLatLngZoom(
                                                LatLng(
                                                    locationManager.mapCenter.latitude,
                                                    locationManager.mapCenter.longitude
                                                ),
                                                12.0f
                                            )
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_location_circle_fill),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use and/or Simulate Geofences")
                        }
                    }
                )


//                Section(
//                    title = { Text("Beacons") },
//                    content = {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { shouldShowBeacons = true }
//                                .padding(16.dp)
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_sensor_tag_radiowaves_forward_fill),
//                                contentDescription = null
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Use and/or Simulate Beacons")
//                        }
//                    }
//                )

                if (shouldShowGeofenceDialog) {
                    Dialog(onDismissRequest = {
                        shouldShowGeofenceDialog = false
                    }) {
                        Card(
                            modifier = Modifier
                                .height(intrinsicSize = IntrinsicSize.Max),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Nearby POI",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        shouldShowGeofenceDialog = false
                                    },
                                    enabled = true,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }

                            selectedLocation?.let {
                                coroutineScope.launch {
                                    pois = MobileSDK.shared.getNearbyPointsOfInterest(it)
                                }
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {

                                    for (poi in pois) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        poi.identifier?.let {
                                            ListItem("Id", poi.identifier ?: "")
                                            regionIdentifier = poi.identifier
                                        }

                                        ListItem(
                                            "Longitude",
                                            selectedLocation?.longitude.toString()
                                        )
                                        ListItem("Latitude", selectedLocation?.latitude.toString())
                                        poi.name?.let {
                                            ListItem(
                                                "Name",
                                                poi.name ?: selectedLocation?.provider ?: ""
                                            )
                                        }
                                        poi.metadata?.get("street").let {
                                            ListItem("Street", poi.metadata["street"] ?: "")
                                        }
                                        poi.metadata?.get("city").let {
                                            ListItem("City", poi.metadata["city"] ?: "")
                                        }
                                        poi.metadata?.get("country").let {
                                            ListItem("Country", poi.metadata["country"] ?: "")
                                        }
                                        poi.metadata?.get("category").let {
                                            ListItem("Category", poi.metadata["category"] ?: "")
                                        }
                                        poi.metadata?.get("entryOrchestrationId").let {
                                            ListItem(
                                                "Entry Event Id",
                                                poi.metadata["entryOrchestrationId"] ?: ""
                                            )
                                        }
                                        poi.metadata?.get("exitOrchestrationId").let {
                                            ListItem(
                                                "Exit Event Id",
                                                poi.metadata["exitOrchestrationId"] ?: ""
                                            )
                                        }
                                    }

                                    region =
                                        Geofence.Builder()
                                            .setRequestId(regionIdentifier)
                                            .setCircularRegion(it.latitude, it.longitude, 100f)
                                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                            .build()

                                    Spacer(modifier = Modifier.weight(1f))
                                    Row {
                                        Button(
                                            onClick = {
                                                // Simulate geofence entry event
                                                coroutineScope.launch {
                                                    MobileSDK.shared.processGeofence(
                                                        region,
                                                        Geofence.GEOFENCE_TRANSITION_ENTER
                                                    )
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Entry")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                // Simulate geofence exit event
                                                coroutineScope.launch {
                                                    MobileSDK.shared.processGeofence(
                                                        region,
                                                        Geofence.GEOFENCE_TRANSITION_EXIT
                                                    )
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Exit")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (shouldShowBeaconDetails) {
                    AnimatedVisibility(visible = shouldShowBeaconDetails) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Selected Beacon") }
                                )
                            },
                            modifier = Modifier.height(250.dp)
                        ) { paddingValues ->
                            Column(modifier = Modifier.padding(paddingValues)) {
                                selectedBeacon?.let { beacon ->
                                    SelectionCell(
                                        beaconTitle = beacon.title,
                                        beaconUUID = beacon.uuid,
                                        beaconMajor = beacon.major,
                                        beaconMinor = beacon.minor,
                                        beaconIdentifier = beacon.identifier,
                                        beaconLocation = beacon.location,
                                        beaconCategory = beacon.category,
                                        beaconStatus = beacon.status,
                                        beaconSymbol = R.drawable.ic_beacon_symbol
                                    )
                                }
                                Button(onClick = { shouldShowBeaconDetails = false }) {
                                    Text(text = "Close beacon details")
                                }
                            }
                        }
                    }
                }
            }
        })
}

@Composable
fun ListItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Section(title: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column {
        title()
        content()
    }
}

@Composable
fun SelectionCell(
    beaconTitle: String,
    beaconUUID: String,
    beaconMajor: Int,
    beaconMinor: Int,
    beaconIdentifier: String,
    beaconLocation: String,
    beaconCategory: String,
    beaconStatus: String,
    beaconSymbol: Int
) {
    val scope = rememberCoroutineScope()

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(beaconTitle, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Column {
                    Text(beaconStatus, fontWeight = FontWeight.Bold)
                }
                Icon(
                    painter = painterResource(id = beaconSymbol),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column {
            Text("$beaconUUID|$beaconMajor|$beaconMinor")
            Text("$beaconIdentifier|$beaconCategory")
            Text(beaconLocation)
        }
        Row {
            Button(onClick = {
                scope.launch {
                    MobileSDK.shared.sendBeaconEvent(
                        eventType = "location.entry",
                        name = beaconTitle,
                        id = beaconIdentifier,
                        category = beaconCategory,
                        beaconMajor = beaconMajor.toDouble(),
                        beaconMinor = beaconMinor.toDouble()
                    )
                }
            }) {
                Text("Entry")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    MobileSDK.shared.sendBeaconEvent(
                        eventType = "location.exit",
                        name = beaconTitle,
                        id = beaconIdentifier,
                        category = beaconCategory,
                        beaconMajor = beaconMajor.toDouble(),
                        beaconMinor = beaconMinor.toDouble()
                    )
                }
            }) {
                Text("Exit")
            }
        }
    }
}