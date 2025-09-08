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

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import com.adobe.luma.tutorial.android.models.MobileSDK
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.services.ServiceProvider

class WebViewModel : ViewModel() {
    lateinit var webView: WebView

    init {
        ServiceProvider.getInstance().appContextService.applicationContext?.let {
            webView = WebView(it)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }

    }

    fun loadUrl() {
        try {
            var urlVariables: String

            // Handle web view

        } catch (e: Exception) {
            MobileSDK.shared.logInfo("TermsOfServiceSheet - loadUrl: Error with WebView: ${e.localizedMessage}")
        }
    }

    private fun getHtmlFileUrl(fileName: String): String {
        return "file:///android_asset/$fileName"
    }
}