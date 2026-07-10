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

// Model for parsing code-based experience content with decisioning offers
data class CodeBasedContent(
    @SerializedName("version") val version: String?,
    // Maps to "offers" (plural) in the JSON response from the AJO template
    @SerializedName("offers") val offers: List<DecisioningOffer>?
)

data class DecisioningOffer(
    // Optional in the response; a stable id is generated on parse when missing
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("actionUrl") val actionUrl: String?
)
