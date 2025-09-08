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

package com.adobe.luma.tutorial.android.models

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.adobe.luma.tutorial.android.utils.Network
import com.adobe.luma.tutorial.android.views.LocationManager
import com.adobe.luma.tutorial.android.views.TrackingStatus
import com.adobe.luma.tutorial.android.xdm.Application
import com.adobe.luma.tutorial.android.xdm.TestPushPayload
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Places
import com.adobe.marketing.mobile.UserProfile
import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.mobile.edge.identity.AuthenticatedState
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.identity.IdentityItem
import com.adobe.marketing.mobile.edge.identity.IdentityMap
import com.adobe.marketing.mobile.optimize.AEPOptimizeError
import com.adobe.marketing.mobile.optimize.AdobeCallbackWithOptimizeError
import com.adobe.marketing.mobile.optimize.DecisionScope
import com.adobe.marketing.mobile.optimize.Optimize
import com.adobe.marketing.mobile.optimize.OptimizeProposition
import com.adobe.marketing.mobile.places.PlacesPOI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


class MobileSDK : ViewModel() {

    var ecid = mutableStateOf("")
    var currentEmailId = mutableStateOf("testUser@gmail.com")
    var currentCRMId = mutableStateOf("112ca06ed53d3db37e4cea49cc45b71e")
    var deviceToken = mutableStateOf("")
    var tenant = mutableStateOf("")
    var sandbox = mutableStateOf("")
    var showProducts = mutableStateOf(true)
    var showPersonalisation = mutableStateOf(true)
    var showGeofences = mutableStateOf(true)
    var showBeacons = mutableStateOf(true)
    var testPushEventType = mutableStateOf("application.test")
    var testPushOrchestrationId = mutableStateOf("")
    var brandName = mutableStateOf("Luma")
    var brandLogo = mutableStateOf("https://contentviewer.s3.amazonaws.com/helium/luma-logo01.png")
    var productsType = mutableStateOf("ProductLoader")
    var productsSystemImage = mutableStateOf("cart")
    var currency = mutableStateOf("$")
    var targetLocation = mutableStateOf("")
    var ldap = mutableStateOf("")
    var emailDomain = mutableStateOf("adobetest.com")
    var tms = mutableStateOf("")
    var longitude = mutableStateOf(0.0)
    var latitude = mutableStateOf(0.0)
    var zoom = mutableStateOf(2.0)
    var trackingEnabled = TrackingStatus.NOT_DETERMINED
        private set
    var nearbyPois: MutableList<PlacesPOI> = mutableStateListOf()

    companion object {
        val shared = MobileSDK()
    }

    suspend fun loadGeneral(context: Context, configLocation: String) {
        withContext(Dispatchers.IO) {
            val general = Network.loadGeneral(context, configLocation)
            tenant.value = general.config.tenant
            sandbox.value = general.config.sandbox
            showProducts.value = general.config.showProducts
            showPersonalisation.value = general.config.showPersonalisation
            showBeacons.value = general.config.showBeacons
            showGeofences.value = general.config.showGeofences
            brandName.value = general.customer.name
            brandLogo.value = general.customer.logo
            productsType.value = general.customer.productsType
            productsSystemImage.value = general.customer.productsSystemImage
            currency.value = general.customer.currency
            testPushEventType.value = general.testPush.eventType
            targetLocation.value = general.target.location
            ldap.value = general.config.ldap
            emailDomain.value = general.config.emailDomain ?: "adobetest.com"
            tms.value = general.config.tms
            longitude.value = general.map.longitude
            latitude.value = general.map.latitude
            zoom.value = general.map.zoom
        }
    }

    fun updateTrackingStatus(status: TrackingStatus) {
        trackingEnabled = status
    }

    fun updateConsent(value: String) {
        // Update consent
        val collectConsent = mapOf("collect" to mapOf("val" to value))
        val currentConsents = mapOf("consents" to collectConsent)
        Consent.update(currentConsents)
        MobileCore.updateConfiguration(currentConsents)
    }

    fun getConsents() {
        // Get consents
        Consent.getConsents { callback ->
            if (callback != null) {
                val jsonStr = JSONObject(callback).toString(4)
                Log.i("MobileSDK", "Consent getConsents: $jsonStr")
            }
        }
    }

    fun logInfo(message: String) {
        com.adobe.marketing.mobile.services.Log.debug("LumaApp", "MobileSDKLogInfo", message)
    }

    fun sendAppInteractionEvent(actionName: String) {
        // Set up a data map, create an experience event and send the event.
        val xdmData = mapOf(
            "eventType" to "application.interaction",
            tenant.value to mapOf(
                "appInformation" to mapOf(
                    "appInteraction" to mapOf(
                        "name" to actionName,
                        "appAction" to mapOf("value" to 1)
                    )
                )
            )
        )
        val appInteractionEvent = ExperienceEvent.Builder().setXdmSchema(xdmData).build()
        Edge.sendEvent(appInteractionEvent, null)
    }

    fun sendTrackScreenEvent(stateName: String) {
        // Set up a data map, create an experience event and send the event.
        val xdmData = mapOf(
            "eventType" to "application.scene",
            tenant.value to mapOf(
                "appInformation" to mapOf(
                    "appStateDetails" to mapOf(
                        "screenType" to "App",
                        "screenName" to stateName,
                        "screenView" to mapOf("value" to 1)
                    )
                )
            )
        )
        val trackScreenEvent = ExperienceEvent.Builder().setXdmSchema(xdmData).build()
        Edge.sendEvent(trackScreenEvent, null)
    }

    fun sendCommerceExperienceEvent(commerceEventType: String, product: Product) {
        // Set up a data map, create an experience event and send the event.
        val xdmData = mapOf(
            "eventType" to "commerce.$commerceEventType",
            "commerce" to mapOf(commerceEventType to mapOf("value" to 1)),
            "productListItems" to listOf(
                mapOf(
                    "name" to product.name,
                    "priceTotal" to product.price,
                    "SKU" to product.sku
                )
            )
        )
        val commerceExperienceEvent = ExperienceEvent.Builder().setXdmSchema(xdmData).build()
        Edge.sendEvent(commerceExperienceEvent, null)
    }

    fun updateIdentities(emailAddress: String, crmId: String) {
        // Set up identity map, add identities to map and update identities
        val identityMap = IdentityMap()

        val emailIdentity = IdentityItem(emailAddress, AuthenticatedState.AUTHENTICATED, true)
        val crmIdentity = IdentityItem(crmId, AuthenticatedState.AUTHENTICATED, true)
        identityMap.addItem(emailIdentity, "Email")
        identityMap.addItem(crmIdentity, "lumaCRMId")

        Identity.updateIdentities(identityMap)
    }

    fun removeIdentities(emailAddress: String, crmId: String) {
        // Remove identities and reset email and CRM Id to their defaults
        Identity.removeIdentity(IdentityItem(emailAddress), "Email")
        Identity.removeIdentity(IdentityItem(crmId), "lumaCRMId")
        currentEmailId.value = "testUser@gmail.com"
        currentCRMId.value = "112ca06ed53d3db37e4cea49cc45b71e"
    }


    @Composable
    fun GetIdentities() {
        Identity.getExperienceCloudId { retrievedEcid ->
            ecid.value = retrievedEcid
        }
        Identity.getIdentities { identityMap ->
            identityMap?.getIdentityItemsForNamespace("email")?.lastOrNull()?.id?.let {
                currentEmailId.value = it
            }
        }
        Log.d("GetEcid", "getIdentities: $ecid")
    }


    fun updateUserAttribute(attributeName: String, attributeValue: String) {
        // Create a profile map, add attributes to the map and update profile using the map
        val profileMap = mapOf(attributeName to attributeValue)
        UserProfile.updateUserAttributes(profileMap)
    }

    suspend fun sendTestPushEvent(applicationId: String, eventType: String) {
        // Create payload and send experience event
        val testPushPayload = TestPushPayload(

            Application(applicationId),
            eventType
        )
        sendExperienceEvent(testPushPayload.asMap())
    }

    private suspend fun sendExperienceEvent(xdm: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            val experienceEvent = ExperienceEvent.Builder()
                .setXdmSchema(xdm)
                .build()
            Edge.sendEvent(experienceEvent) { handles ->
                handles.forEach { handle ->
                    handle.payload?.let {
                        Log.i("MobileSDK", "sendExperienceEvent: Handle type: ${handle.type}")
                    }
                }
            }
        }
    }

    fun sendTrackAction(action: String, data: Map<String, String>?) {
        // Send trackAction Event
        MobileCore.trackAction(action, data)
    }

    suspend fun updatePropositionsAT(ecid: String, location: String) {
        // set up the XDM dictionary, define decision scope and call update proposition API
        withContext(Dispatchers.IO) {
            val ecidMap = mapOf("ECID" to mapOf("id" to ecid, "primary" to true))
            val identityMap = mapOf("identityMap" to ecidMap)
            val xdmData = mapOf("xdm" to identityMap)
            val decisionScope = DecisionScope(location)
            Optimize.clearCachedPropositions()
            Optimize.updatePropositions(listOf(decisionScope), xdmData, null, object :
                    AdobeCallbackWithOptimizeError<MutableMap<DecisionScope?, OptimizeProposition?>?> {
                    override fun fail(optimizeError: AEPOptimizeError?) {
                        val responseError = optimizeError
                        Log.i("MobileSDK", "updatePropositionsAT error: ${responseError}")
                    }
                    override fun call(propositionsMap: MutableMap<DecisionScope?, OptimizeProposition?>?) {
                        val responseMap = propositionsMap
                        Log.i("MobileSDK", "updatePropositionsOD call: ${responseMap}")
                    }
                })
        }
    }

    suspend fun updatePropositionsOD(
        ecid: String,
        activityId: String,
        placementId: String,
        itemCount: Int
    ) {
        // set up the XDM dictionary, define decision scope and call update proposition API
        withContext(Dispatchers.IO) {
            val ecidMap = mapOf("ECID" to mapOf("id" to ecid, "primary" to true))
            val identityMap = mapOf("identityMap" to ecidMap)
            val xdmData = mapOf("xdm" to identityMap)
            val decisionScope = DecisionScope(activityId, placementId, itemCount)
            Optimize.clearCachedPropositions()
            Optimize.updatePropositions(listOf(decisionScope), xdmData, null, object :
                AdobeCallbackWithOptimizeError<MutableMap<DecisionScope?, OptimizeProposition?>?> {
                override fun fail(optimizeError: AEPOptimizeError?) {
                    val responseError = optimizeError
                    Log.i("MobileSDK", "updatePropositionsOD error: ${responseError}")
                }
                override fun call(propositionsMap: MutableMap<DecisionScope?, OptimizeProposition?>?) {
                    val responseMap = propositionsMap
                    Log.i("MobileSDK", "updatePropositionsOD call: ${responseMap}")
                }
            })
        }
    }

    suspend fun processGeofence(geofence: Geofence?, transitionType: Int) {
        withContext(Dispatchers.IO) {
            geofence?.let {
                // Process geolocation event
                Places.processGeofence(geofence, transitionType)
            }
        }
    }

    suspend fun getNearbyPointsOfInterest(location: Location): List<PlacesPOI> {
        return withContext(Dispatchers.IO) {
            if (nearbyPois.isNotEmpty()) {
                nearbyPois
            } else {
                val latch = java.util.concurrent.CountDownLatch(1)
                location.let {
                    Places.getNearbyPointsOfInterest(
                        it,
                        200, { pointsOfInterests ->
                            nearbyPois = pointsOfInterests
                        }) { error ->
                        error?.let {
                            Log.i("MobileSDK", "Places error: $it")
                        }
                        latch.countDown()
                    }
                }
                latch.await(1, TimeUnit.SECONDS)
                nearbyPois
            }
        }
    }

    suspend fun sendBeaconEvent(
        eventType: String,
        name: String,
        id: String,
        category: String,
        beaconMajor: Double,
        beaconMinor: Double
    ) {
        withContext(Dispatchers.IO) {
            val beaconEventPayload = BeaconInteractionPayload(
                PlaceContext(
                    POIinteraction(
                        PoiDetail(
                            name,
                            id,
                            "beacon",
                            category,
                            BeaconInteractionDetails(beaconMajor, beaconMinor)
                        ),
                        PoiEntries(if (eventType == "location.entry") 1.0 else 0.0),
                        PoiExits(if (eventType == "location.exit") 1.0 else 0.0)
                    )
                ),
                eventType
            )
            sendExperienceEvent(beaconEventPayload.toMap())
        }
    }

    suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://ims-na1.adobelogin.com/ims/token/v3")
            val postData =
                "grant_type=client_credentials&client_id=<clientid>&client_secret=<clientsecret>&scope=<scopes>"
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true
            connection.outputStream.write(postData.toByteArray())

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val accessToken = jsonResponse.getString("access_token")
                Log.i("MobileSDK", "getAccessToken: $accessToken")
                accessToken
            } else {
                Log.e(
                    "MobileSDK",
                    "getAccessToken: Failed with response code ${connection.responseCode}"
                )
                ""
            }
        }
    }
}