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
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.Date;
import java.text.DateFormat;


public class TestMyKeyStoreActivity extends Activity
{

	private static final String TAG = "TestMyKeyStoreActivity";

	public static final String ACTION_USER_PRESENT = "com.android.test.mykeystore.TestMyKeyStoreActivity.USER_PRESENT";
	
	private static final int TEST_SIGN = 0;
	private static final int TEST_VERIFY = TEST_SIGN + 1;
	private static final int TEST_ENCRYPT = TEST_VERIFY + 1;
	private static final int TEST_DECRYPT = TEST_ENCRYPT + 1;
	private static final int TEST_SIGN_LOG = TEST_DECRYPT + 1;
	private static final int TEST_ENCRYPT_SEND = TEST_SIGN_LOG + 1;

	private static final String texte = "Hello world!!!";
	private byte[] signature = null;
	private byte[] encrypted = null;
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		if (requestCode == TEST_SIGN) 
		{
			if (resultCode == RESULT_OK) 
			{
				signature = intent.getByteArrayExtra("SIGNATURE");
				
				try
				{
					String result = "Signature=" + Base64.encodeBytes(signature);
					Toast.makeText(getApplicationContext(), 
						result,
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, result);
					
					Intent new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.VERIFY");
					new_intent.putExtra("ALIAS", "test");
					new_intent.putExtra("ALGO", "MD5withRSA");
					new_intent.putExtra("DATA", texte.getBytes());
					new_intent.putExtra("SIGNATURE", signature);
					startActivityForResult(new_intent, TEST_VERIFY);
				}
				catch(Exception e)
				{
					Toast.makeText(getApplicationContext(), 
						"Erreur signature donnees",
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == TEST_VERIFY) 
		{
			if (resultCode == RESULT_OK) 
			{
				boolean result = intent.getBooleanExtra("VERIFY", false);
				String str = "verify result = " + (result == true ? "true":"false");
				Toast.makeText(getApplicationContext(), 
					str,
					Toast.LENGTH_SHORT).show();
				Log.v(TAG, str);
				
				Intent new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PRIVKEY");
				new_intent.putExtra("ALIAS", "test");
				new_intent.putExtra("PASSWORD", "testtest");
				new_intent.putExtra("ALGO", "RSA/NONE/PKCS1Padding");
				new_intent.putExtra("DATA", texte.getBytes());
				startActivityForResult(new_intent, TEST_ENCRYPT);
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == TEST_ENCRYPT) 
		{
			if (resultCode == RESULT_OK) 
			{
				encrypted = intent.getByteArrayExtra("ENCRYPTED");
		
				if(encrypted == null)
				{
					Toast.makeText(getApplicationContext(), 
						"!!! encrypted = null !!!",
						Toast.LENGTH_SHORT).show();
					return;
				}
		
				String str = "encryp result = " + Base64.encodeBytes(encrypted);
				Toast.makeText(getApplicationContext(), 
					str,
					Toast.LENGTH_SHORT).show();
				Log.v(TAG, str);
				
				Intent new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.DECRYPT_WITH_PUBKEY");
				new_intent.putExtra("ALIAS", "test");
				new_intent.putExtra("ALGO", "RSA/NONE/PKCS1Padding");
				new_intent.putExtra("DATA", encrypted);
				startActivityForResult(new_intent, TEST_DECRYPT);
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == TEST_DECRYPT) 
		{
			if (resultCode == RESULT_OK) 
			{
				byte result[] = intent.getByteArrayExtra("DECRYPTED");
				
				if(result == null)
				{
					Toast.makeText(getApplicationContext(), 
						"!!! result = null !!!",
						Toast.LENGTH_SHORT).show();
					return;
				}
				
				String decrypted = new String(result);
					
				Toast.makeText(getApplicationContext(), 
					"texte decrypter = " + decrypted,
					Toast.LENGTH_SHORT).show();
				Log.v(TAG, decrypted);
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == TEST_SIGN_LOG) 
		{
			if (resultCode == RESULT_OK) 
			{
				try
				{
					Log.v(TAG, Base64.encodeBytes(intent.getByteArrayExtra("SIGNATURE")));
				}
				catch(Exception e)
				{
					Log.v(TAG, e.toString());
				}
					
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}

			finish();
		}
		else if (requestCode == TEST_ENCRYPT_SEND) 
		{
			if (resultCode == RESULT_OK) 
			{
				encrypted = intent.getByteArrayExtra("ENCRYPTED");
		
				if(encrypted == null)
				{
					Toast.makeText(getApplicationContext(), 
						"!!! encrypted = null !!!",
						Toast.LENGTH_SHORT).show();
					return;
				}
		
				String str = "--- BEGIN CRYPTED TEXT ---\n" + Base64.encodeBytes(encrypted) + "\n--- END CRYPTED TEXT ---";
				
				Log.v(TAG, "action send");
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, "Sujet");
				i.putExtra(Intent.EXTRA_TEXT, str);
				startActivity(Intent.createChooser(i, "Titre:"));	

				
			} 
			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), 
					"ABANDON !!!",
					Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button testButton = (Button) findViewById(R.id.test);
		testButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent new_intent;
				new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.SIGN");
				new_intent.putExtra("ALIAS", "test");
				new_intent.putExtra("PASSWORD", "testtest");
				new_intent.putExtra("ALGO", "MD5withRSA");
				new_intent.putExtra("DATA", texte.getBytes());
				startActivityForResult(new_intent, TEST_SIGN);
			}
		});

		Button emailButton = (Button) findViewById(R.id.email);
		emailButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent new_intent;
				new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PUBKEY");
				new_intent.putExtra("ALIAS", "test");
				new_intent.putExtra("ALGO", "RSA/NONE/PKCS1Padding");
				new_intent.putExtra("DATA", "bla bla bla".getBytes());
				startActivityForResult(new_intent, TEST_ENCRYPT_SEND);
				return;
			}
		});

	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		String dataString = intent == null ? null : intent.getDataString();

		if (intent != null && action != null) 
		{
			if(action.equals(ACTION_USER_PRESENT)) 
			{
				Date d = new Date();
				String str = "log user present at " + DateFormat.getDateInstance().format(d);

				Log.v(TAG, str);
				
				Intent new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.SIGN");
				new_intent.putExtra("ALIAS", "test");
				new_intent.putExtra("PASSWORD", "test");
				new_intent.putExtra("ALGO", "MD5withRSA");
				new_intent.putExtra("DATA", str.getBytes());
				startActivityForResult(new_intent, TEST_SIGN_LOG);
				finish();
			}
		}
	}
}
