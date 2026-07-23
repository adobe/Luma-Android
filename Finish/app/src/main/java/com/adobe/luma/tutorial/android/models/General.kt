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

import com.google.gson.annotations.SerializedName

data class General(
    @SerializedName("config") val config: Config,
    @SerializedName("customer") val customer: Customer,
    @SerializedName("testPush") val testPush: TestPush,
    @SerializedName("target") val target: Target,
    @SerializedName("decisioning") val decisioning: Decisioning,
    @SerializedName("map") val map: AppMap
) {
    companion object {
        val example = General(
            config = Config(
                tenant = "",
                sandbox = "",
                showProducts = true,
                showPersonalisation = true,
                showDecisioning = false,
                showGeofences = true,
                showBeacons = true,
                ldap = "",
                tms = "",
                emailDomain = ""
            ),
            customer = Customer(
                name = "",
                logo = "",
                productsType = "Products",
                productsSystemImage = "cart",
                currency = "$"
            ),
            testPush = TestPush(
                name = "",
                eventType = ""
            ),
            target = Target(
                location = ""
            ),
            decisioning = Decisioning(
                surface = ""
            ),
            map = AppMap(
                longitude = 0.0,
                latitude = 0.0,
                zoom = 0.0
            )
        )
    }
}

data class Config(
    @SerializedName("tenant") val tenant: String,
    @SerializedName("sandbox") val sandbox: String,
    @SerializedName("showProducts") val showProducts: Boolean,
    @SerializedName("showPersonalisation") val showPersonalisation: Boolean,
    @SerializedName("showDecisioning") val showDecisioning: Boolean,
    @SerializedName("showGeofences") val showGeofences: Boolean,
    @SerializedName("showBeacons") val showBeacons: Boolean,
    @SerializedName("ldap") val ldap: String,
    @SerializedName("tms") val tms: String,
    @SerializedName("emailDomain") val emailDomain: String?
) {
    companion object {
        val example = Config(
            tenant = "",
            sandbox = "",
            showProducts = true,
            showPersonalisation = true,
            showDecisioning = false,
            showGeofences = true,
            showBeacons = true,
            ldap = "",
            tms = "",
            emailDomain = "adobetest.com"
        )
    }
}

data class Customer(
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("productsType") val productsType: String,
    @SerializedName("productsSystemImage") val productsSystemImage: String,
    @SerializedName("currency") val currency: String
)

data class Target(
    @SerializedName("location") val location: String
)

data class Decisioning(
    @SerializedName("surface") val surface: String
)

data class AppMap(
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("zoom") val zoom: Double
)

data class TestPush(
    @SerializedName("name") val name: String,
    @SerializedName("eventType") val eventType: String
)