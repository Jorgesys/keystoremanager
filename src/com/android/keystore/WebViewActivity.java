/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keystore;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.content.Intent;
import android.net.Uri;

public class WebViewActivity extends Activity 
{

	private static final String LOG_TAG = "WebViewActivity";
	private static final String defaultUrl = "file:///android_asset/html/help/index.html";
	
	private WebView webView;

	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		webView = new WebView(this);
		setContentView(webView);

		WebSettings webSettings = webView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(false);
		webSettings.setSupportZoom(false);

		webView.setWebChromeClient(new WebChromeClient());
		
		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		
		String url = defaultUrl;
		if (intent != null && action != null) 
		{
			if (action.equals(Intent.ACTION_VIEW)) 
			{
				Uri uri = intent.getData();
				if(uri != null)
				{
					url = uri.toString();
				}
			}
		}
		
		/* test:
		String summary = "<html><body>You scored <b>192 points.</body></html>";
		webView.loadData(summary, "text/html", "utf-8");
		*/
		webView.loadUrl(defaultUrl);
	}
}
