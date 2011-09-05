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

 package com.android.test.mykeystore;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.app.PendingIntent;


public class UserPresentReceiver extends BroadcastReceiver 
{
	public static final String TAG = "UserPresentReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		
		Log.v(TAG, "onReceive");
	
		if (Intent.ACTION_USER_PRESENT.equals(action)) 
		{
			PendingIntent pi = PendingIntent.getActivity(context, 0, 
				new Intent("com.android.test.mykeystore.TestMyKeyStoreActivity.USER_PRESENT"), 
				0);
			try
			{
				pi.send();
			}
			catch (Exception e)
			{
				Log.v(TAG, e.toString());
			}
		}
	}
}
