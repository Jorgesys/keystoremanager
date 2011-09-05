/*
 * Copyright (C) 2010 keystoremanager authors
 *
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

import android.app.Dialog;

import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.os.*;

import java.security.*;
import java.io.*;
import java.util.*;

public class GetPasswordDialog extends Dialog
{
	
	private Context context;
	
/* 	public final static String INTENT_ACTION_MAIN = "com.android.keystore.GetPasswordActivity.MAIN";
	
	private Intent intent = null;
	private String action = null;
	
	private OnClickListener bPassphraseOkListener = new OnClickListener() {
		public void onClick(View v) {
			EditText entry = (EditText) findViewById(R.id.entry);
					
			if (intent != null && action != null) 
			{
				if (action.equals(INTENT_ACTION_MAIN)) 
				{
					intent.putExtra("RESULTAT", entry.getText().toString());
					setResult(RESULT_OK, intent);
				}
			}
			dismiss();
		}
	};

	private OnClickListener bPassphraseCancelListener = new OnClickListener() {
		public void onClick(View v) {
			if (intent != null)
			{
				setResult(RESULT_CANCELED , intent);
			}
			dismiss();
		}
	};
 */

	public GetPasswordDialog(Context context) 
	{
		super(context);
		this.context = context;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
	}
}
