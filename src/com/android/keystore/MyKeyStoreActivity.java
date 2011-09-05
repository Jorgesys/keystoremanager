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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.net.Uri;

import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;

import java.security.*;
import java.io.*;
import java.util.*;

import javax.crypto.Cipher;


// interface to request reload of view with new updated keystore
interface UpdateListener {
	public void update();
}

class KeyStoreElement 
{
	public String name;
	public boolean certificat;
	
	public KeyStoreElement(String name, boolean certificat) 
	{
		this.name = name;
		this.certificat = certificat; 
	}

	public KeyStoreElement(String name) 
	{
		this(name, false);
	}
 
	public boolean isCertificat() 
	{
		return certificat;
	}
}

class KeyStoreElementAdapter extends ArrayAdapter<KeyStoreElement> 
{

	private ArrayList<KeyStoreElement> items;
	private Context context;

	public KeyStoreElementAdapter(Context context, int textViewResourceId, ArrayList<KeyStoreElement> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.itemview, null);
		}

		KeyStoreElement item = items.get(position);
		if (item!= null) {
			ImageView icone = (ImageView) view.findViewById(R.id.icone);
			if (icone != null) 
			{
				if(item.isCertificat())
				{
					icone.setImageDrawable(getContext().getResources().getDrawable(R.drawable.certificat_icone_4129_32));
				}
				else
				{
					icone.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cle_verrouillage_mot_passe_icone_5560_32));
				}
			}
			TextView texte = (TextView) view.findViewById(R.id.texte);
			if (texte != null) 
				texte.setText(item.name);
		}

		return view;
	}
}

public class MyKeyStoreActivity extends ListActivity
{
	private static final String TAG = "MyKeyStoreActivity";

	public static final String MYKEYSTORENAME = "MyKeyStore.bks";
	
	// Menus
	private static final int SETTINGS_ID = 1;
	private static final int ADD_ID = SETTINGS_ID + 1;
	private static final int HELP_ID = ADD_ID + 1;
	private static final int ABOUT_ID = HELP_ID + 1;
	private static final int QUIT_ID = ABOUT_ID + 1;

	// Dialogs
	private static final int ADD_DIALOG = 0;
	private String[] itemsAdd;
	
	private static final int ITEM_DIALOG = ADD_DIALOG + 1;
	private String[] itemsItem;

	private static final int PASSWORD_DIALOG = ITEM_DIALOG + 1;

	// Intents
	public static final String ACTION_SIGN = "com.android.keystore.MyKeyStoreActivity.SIGN";
	public static final String ACTION_VERIFY = "com.android.keystore.MyKeyStoreActivity.VERIFY";
	public static final String ACTION_ENCRYPT_WITH_PUBKEY = "com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PUBKEY";
	public static final String ACTION_ENCRYPT_WITH_PRIVKEY = "com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PRIVKEY";
	public static final String ACTION_DECRYPT_WITH_PUBKEY = "com.android.keystore.MyKeyStoreActivity.DECRYPT_WITH_PUBKEY";
	public static final String ACTION_DECRYPT_WITH_PRIVKEY = "com.android.keystore.MyKeyStoreActivity.DECRYPT_WITH_PRIVKEY";

	private ArrayList<KeyStoreElement> arraylist;

	// Current element selected
	private KeyStoreElement selectedElement;
	
	private static KeyStore keystore;
	private static String passphrase;
	
	private EditText input;
	
	private class UpdateView implements UpdateListener {
		@Override
		public void update() {
			initView();
		}
	}
	
	public static KeyStore getKeyStore() { return keystore; }
	
	public static boolean saveKeyStore() 
	{
		java.io.FileOutputStream fos = null;
		try
		{
	
			fos = new java.io.FileOutputStream(
				new File(Environment.getExternalStorageDirectory(),
					MYKEYSTORENAME));
			keystore.store(fos, passphrase.toCharArray());
			fos.close();
			return true;
		}
		catch(Exception e)
		{
			Log.v(TAG, e.toString());
		}

		return false;
	}
	
	private void loadKeyStore() throws Exception
	{
		java.io.FileInputStream fis = null;

		keystore = KeyStore.getInstance("BKS");
		Log.v(TAG, "getDefaultType() = " + keystore.getDefaultType());
		try
		{
	
			fis = new java.io.FileInputStream(
				new File(Environment.getExternalStorageDirectory(),
					MYKEYSTORENAME));
			keystore.load(fis, passphrase.toCharArray());
		}
		catch(FileNotFoundException fnfe)
		{
			Log.v(TAG, fnfe.toString());
			keystore.load(null, passphrase.toCharArray());
		}
		finally
		{
			if (fis != null) 
			{
				fis.close();
			}
		}
	}
	
	private void initView() 
	{
		if(keystore != null)
		{
			try 
			{
				Enumeration<String> e = keystore.aliases();
				arraylist = new ArrayList();
				
				while(e.hasMoreElements()) 
				{
					String s = e.nextElement();
					arraylist.add(new KeyStoreElement(s,keystore.isCertificateEntry(s)));
					
				}
				
				setListAdapter(new KeyStoreElementAdapter(this,
					R.layout.itemview, arraylist));
				
			}
			catch(KeyStoreException kse)
			{
				Log.v(TAG, kse.toString());
			}
			
			ListView lv = getListView();

			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
						selectedElement = arraylist.get(position);
						showDialog(ITEM_DIALOG);
				}
			});
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(prefs.getBoolean(PreferencesActivity.KEY_SAUVE_PASSEPHRASE, false) == true)
		{
			passphrase = prefs.getString(PreferencesActivity.KEY_PASSEPHRASE, "");

			try
			{
				loadKeyStore();
			}
			catch(Exception e)
			{
				Toast.makeText(getApplicationContext(), 
					getString(R.string.failure_loading_keystore),
					Toast.LENGTH_SHORT).show();
			}
			
			//initView();

		}
		else
		{
			showDialog(PASSWORD_DIALOG);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		String dataString = intent == null ? null : intent.getDataString();

		if (intent != null && action != null) 
		{
			if(action.equals(ACTION_SIGN)) 
			{
				try
				{
					Signature sign = Signature.getInstance(intent.getStringExtra("ALGO"));
					Key key = keystore.getKey(intent.getStringExtra("ALIAS"), 
							intent.getStringExtra("PASSWORD").toCharArray());
					sign.initSign((PrivateKey)key);
					
					sign.update(intent.getByteArrayExtra("DATA"));
					
					intent.putExtra("SIGNATURE", sign.sign());
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_sign),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
			else if(action.equals(ACTION_VERIFY))
			{
				try
				{
					Signature sign = Signature.getInstance(intent.getStringExtra("ALGO"));
					java.security.cert.Certificate cert = keystore.getCertificate(intent.getStringExtra("ALIAS"));
					
					sign.initVerify(cert);
					
					sign.update(intent.getByteArrayExtra("DATA"));
					
					intent.putExtra("VERIFY", sign.verify(intent.getByteArrayExtra("SIGNATURE")));
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_verify),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
			else if(action.equals(ACTION_ENCRYPT_WITH_PUBKEY))
			{
				try
				{
					Cipher cipher = Cipher.getInstance(intent.getStringExtra("ALGO"));

					String alias = intent.getStringExtra("ALIAS");
					
					java.security.cert.Certificate cert = keystore.getCertificate(alias);
					cipher.init(Cipher.ENCRYPT_MODE, cert);
					
					intent.putExtra("ENCRYPTED", cipher.doFinal(intent.getByteArrayExtra("DATA")));
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_encrypt),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
			else if(action.equals(ACTION_ENCRYPT_WITH_PRIVKEY))
			{
				try
				{
					Cipher cipher = Cipher.getInstance(intent.getStringExtra("ALGO"));

					String alias = intent.getStringExtra("ALIAS");
					
					Key key = keystore.getKey(alias,  intent.getStringExtra("PASSWORD").toCharArray());
					cipher.init(Cipher.ENCRYPT_MODE, key);
					
					intent.putExtra("ENCRYPTED", cipher.doFinal(intent.getByteArrayExtra("DATA")));
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_encrypt),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
			else if(action.equals(ACTION_DECRYPT_WITH_PUBKEY))
			{
				try
				{
					Cipher cipher = Cipher.getInstance(intent.getStringExtra("ALGO"));

					String alias = intent.getStringExtra("ALIAS");
					
					java.security.cert.Certificate cert = keystore.getCertificate(alias);
					cipher.init(Cipher.DECRYPT_MODE, cert);

					intent.putExtra("DECRYPTED", cipher.doFinal(intent.getByteArrayExtra("DATA")));
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_decrypt),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
			else if(action.equals(ACTION_DECRYPT_WITH_PRIVKEY))
			{
				try
				{
					Cipher cipher = Cipher.getInstance(intent.getStringExtra("ALGO"));

					String alias = intent.getStringExtra("ALIAS");
					
					Key key = keystore.getKey(alias,  intent.getStringExtra("PASSWORD").toCharArray());
					cipher.init(Cipher.DECRYPT_MODE, key);

					intent.putExtra("DECRYPTED", cipher.doFinal(intent.getByteArrayExtra("DATA")));
					setResult(RESULT_OK, intent);
				}
				catch(Exception e) 
				{
					setResult(RESULT_CANCELED, intent);
					Toast.makeText(getApplicationContext(), 
						getString(R.string.abort_decrypt),
						Toast.LENGTH_SHORT).show();
					Log.v(TAG, e.toString());
				}
				finish();
			}
		}
		
		findViewById(R.id.progress_bar).setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
	
		initView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SETTINGS_ID, 0, R.string.preferences)
			.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, ADD_ID, 0, R.string.add)
			.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, HELP_ID, 0, R.string.aide)
			.setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, ABOUT_ID, 0, R.string.apropos)
			.setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, QUIT_ID, 0, R.string.quitter)
			.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	private final DialogInterface.OnClickListener aboutListener =
		new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.urlweb)));
			startActivity(intent);
		}
	};

	private final DialogInterface.OnClickListener addListener =
		new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			if(itemsAdd[i].equals(getString(R.string.generate_key)))
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setClassName(getApplicationContext(), GenerateKeyActivity.class.getName());
				startActivity(intent);
			}
		}
	};

	private final DialogInterface.OnClickListener itemListener =
		new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			if(itemsItem[i].equals(getString(R.string.info)))
			{
				try
				{
					java.security.cert.Certificate cert = keystore.getCertificate(selectedElement.name);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					intent.setClassName(getApplicationContext(), TextViewActivity.class.getName());
					intent.putExtra("TEXT", cert.toString());
					startActivity(intent);
				}
				catch(Exception e)
				{
					Log.v(TAG, e.toString());
				}
				
			}
			else if(itemsItem[i].equals(getString(R.string.delete)))
			{
				try
				{
					keystore.deleteEntry(selectedElement.name);
					saveKeyStore();
				}
				catch(Exception e)
				{
					Log.v(TAG, e.toString());
				}
				initView();
			}
		}
	};

	/* Handles dialogs */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch(id) {
			case ADD_DIALOG:
				itemsAdd = new String[] { /*getString(R.string.add_certificat),*/
					/* getString(R.string.add_key), */
					getString(R.string.generate_key), 
					getString(R.string.abandon) };
				builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.app_name));
				builder.setItems(itemsAdd, addListener);
				dialog = builder.create();
				break;
			case ITEM_DIALOG:
				itemsItem = new String[] { getString(R.string.info),
					getString(R.string.delete), 
					getString(R.string.abandon) };
				builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.app_name));
				builder.setItems(itemsItem, itemListener);
				dialog = builder.create();
				break;
			case PASSWORD_DIALOG:
				builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.password));
				input = new EditText(this);
				input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
				input.setTransformationMethod(new android.text.method.PasswordTransformationMethod());

				builder.setView(input);
				
				builder.setPositiveButton(getString(R.string.valider), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						passphrase = input.getText().toString();
						
						try
						{
							loadKeyStore();
						}
						catch(Exception e)
						{
							Toast.makeText(getApplicationContext(), 
								getString(R.string.failure_loading_keystore),
								Toast.LENGTH_SHORT).show();
						}
						
						initView();

					}
				});
				builder.setNegativeButton(getString(R.string.abandon), null);
				dialog = builder.create();

				break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case SETTINGS_ID:
				intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setClassName(this, PreferencesActivity.class.getName());
				startActivity(intent);
				return true;
			case ADD_ID:
				showDialog(ADD_DIALOG);
				return true;
			case HELP_ID:
				intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setClassName(this, WebViewActivity.class.getName());
				intent.setData(Uri.parse("file:///android_asset/html/help/index.html"));
				startActivity(intent);
				return true;
			case ABOUT_ID:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.app_name));
				builder.setMessage(getString(R.string.apropos) + "\n\n" + getString(R.string.urlweb));
				//builder.setIcon(R.drawable.keystoremanager_icon);
				builder.setPositiveButton(R.string.ouvrir_navigateur, aboutListener);
				builder.setNegativeButton(R.string.abandon, null);
				builder.show();
				return true;
			case QUIT_ID:
				finish();
				return true;
			default:
				return false;
		}
	}

}
