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
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;

public class TextViewActivity extends Activity 
{
	private static final String LOG_TAG = "TextViewActivity";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.textview);

		Button closeButton = (Button) findViewById(R.id.close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		
		if (intent != null && action != null) 
		{
			if (action.equals(Intent.ACTION_VIEW)) 
			{
				String text = intent.getStringExtra("TEXT");
				if(text != null)
				{
					TextView tv = (TextView) findViewById(R.id.mytext);
					tv.setText(text);
				}
			}
		}
	}
}
