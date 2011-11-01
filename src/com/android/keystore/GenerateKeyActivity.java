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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.lang.Integer;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;
import java.security.Signature;

import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import java.io.ByteArrayInputStream;

import java.util.Date;
import java.text.SimpleDateFormat;

class Asn1Tag 
{
	public byte tag;
	public Object value;

	//define some asn1 tags
	public static final byte RAW_DATA = 0;
	
	public static final byte SEQUENCE = 0x30;
	public static final byte OPTIONAL = (byte)0xa0;
	public static final byte INTEGER = 0x02;
	public static final byte OBJECT_ID = 0x06;
	public static final byte SET = 0x31;
	public static final byte UTC_TIME = 0x17;
	public static final byte PRINTABLE_STRING = 0x13;
	public static final byte NULL = 0x05;
	public static final byte BIT_STRING = 0x03;
	
	public static final byte[] RSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x01 }; // 1.2.840.113549.1.1.1

	public static final byte[] sha1RSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0x86, (byte)0xf7, (byte)0x0d, (byte)0x01, (byte)0x01, (byte)0x05 }; // 1.2.840.113549.1.1.5 

	public static final byte[] sha1DSA = { (byte)0x2a, (byte)0x86, (byte)0x48,
		(byte)0xce, (byte)0x38, (byte)0x04, (byte)0x03 }; // 1.2.840.10040.4.3 

	public static final byte[] country = { (byte)0x55, (byte)0x04, (byte)0x06 }; // 2.5.4.6 Pays/région (C)
	public static final byte[] commonName = { (byte)0x55, (byte)0x04, (byte)0x03 }; // 2.5.4.3 Nom commun (CN)
	public static final byte[] state = { (byte)0x55, (byte)0x04, (byte)0x08 }; // 2.5.4.8 Département ou province (S)
	public static final byte[] locality = { (byte)0x55, (byte)0x04, (byte)0x07 }; // 2.5.4.7 Ville (L)
	public static final byte[] organization = { (byte)0x55, (byte)0x04, (byte)0x0a }; // 2.5.4.10 Organisation (O)
	public static final byte[] organizationUnit = { (byte)0x55, (byte)0x04, (byte)0x0b }; // 2.5.4.11 Unité d'organisation Unit (OU)

	public Asn1Tag(byte tag, Object value)
	{
		this.tag = tag;
		this.value = value;
	}

	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}
	
	public static String byteToHex(byte[] buf) 
	{ 
			String s;
			StringBuffer strBuffer = new StringBuffer();
			
			for (int i = 0; i < buf.length; i++) {
					s = Integer.toHexString(unsignedByteToInt(buf[i]));
					if(s.length() < 2)
						strBuffer.append('0');
					strBuffer.append(s);
			  }
			  return strBuffer.toString();
	}

	private static byte[] asn1_add(byte[] in, byte type, byte[] bd)
	{
		byte[] tag;
		
		// ugly hack but...
		if(type == BIT_STRING)
		{
			byte[] t = new byte[bd.length+1];
			t[0] = 0;
			System.arraycopy(bd, 0, t, 1, bd.length);
			bd = t;
		}
		else if(type == RAW_DATA)
		{
			return append(in,bd);
		}
		
		if(bd.length > 255)
		{
			// TAG 82 XX XX 
			tag = new byte[4];
			tag[0] = type;
			tag[1] = (byte)0x82;
			tag[2] = (byte)((bd.length/256)&0xff);
			tag[3] = (byte)((bd.length%256)&0xff);
		}
		else if(bd.length > 127)
		{
			// TAG 81 XX  
			tag = new byte[3];
			tag[0] = type;
			tag[1] = (byte)0x81;
			tag[2] = (byte)((bd.length%256)&0xff);
		}
		else
		{
			// TAG 81 XX  
			tag = new byte[2];
			tag[0] = type;
			tag[1] = (byte)((bd.length%256)&0xff);
		}
		byte[] out = new byte[in.length + tag.length + bd.length];
		
		System.arraycopy(in, 0, out, 0, in.length);
		System.arraycopy(tag, 0, out, in.length, tag.length);
		System.arraycopy(bd, 0, out, in.length+tag.length, bd.length);
		
		return out;
	}

	public static byte[] append(byte[] in, byte[] to_add)
	{
		byte[] r = new byte[in.length+to_add.length];
		
		System.arraycopy(in, 0, r, 0, in.length);
		System.arraycopy(to_add, 0, r, in.length, to_add.length);
		
		return r;
	}
	
	public static byte[] asn1_add(byte[] in, Asn1Tag t)
	{
		return asn1_add(in, t.tag, t.value);
	}
	
	public static byte[] asn1_add(byte[] in, byte tag, Object obj)
	{
		if(obj instanceof byte[])
		{
			in = asn1_add(in, tag, (byte[])obj);
		}
		else if(obj instanceof Asn1Tag[])
		{
			byte[] temp = new byte[0];
			Asn1Tag[] asn1array = (Asn1Tag[])obj;
			for(int i = 0; i < asn1array.length; i++)
			{
				temp = asn1_add(temp, asn1array[i].tag, asn1array[i].value);
			}
			in = asn1_add(in, tag, temp);
		}
		else
		{
			Asn1Tag t = (Asn1Tag)obj;
			in = asn1_add(in, t.tag, t.value);
		}
		
		return in;
	}
}
	
public class GenerateKeyActivity extends Activity
{
	private static final String TAG = "GenerateKeyActivity";

	private KeyStore keystore = null;
	
	private String keytype = "RSA";
	
	private Certificate buildCert(PublicKey publickey, PrivateKey privatekey) throws Exception
	{
		byte[] r = new byte[0];
		long validite = 0;
		Signature sign;
		byte[] asn1TagEncrypt;
		
		EditText et;
		
		et = (EditText) findViewById(R.id.commonName);
		String cn = et.getText().toString();
		et = (EditText) findViewById(R.id.organizationalUnit);
		String ou = et.getText().toString();
		et = (EditText) findViewById(R.id.organization);
		String o = et.getText().toString();
		et = (EditText) findViewById(R.id.locality);
		String l = et.getText().toString();
		et = (EditText) findViewById(R.id.state);
		String s = et.getText().toString();
		et = (EditText) findViewById(R.id.country);
		String c = et.getText().toString();

		try
		{
			et = (EditText) findViewById(R.id.validity);
			validite = Integer.parseInt(et.getText().toString());
		}
		catch(Exception e)
		{
			validite = 365;
			Log.v(TAG, e.toString());
		}
		
		if(keytype.equals("RSA"))
		{
			sign = Signature.getInstance("1.2.840.113549.1.1.5"); // "SHA1WithRSAEncryption"
			asn1TagEncrypt =  Asn1Tag.sha1RSA;
		}
		else //if(keytype.equals("DSA"))
		{
			sign = Signature.getInstance("1.2.840.10040.4.3");
			asn1TagEncrypt =  Asn1Tag.sha1DSA;
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddhhmmss'Z'");
		Date beginDate = new Date();
		Date endDate = new Date(beginDate.getTime()+(validite*24*3600*1000));
		
		Log.v(TAG, "beginDate=" + beginDate.getTime() + ", validite=" + (validite*24*3600*1000) + ", endDate=" + endDate.getTime());
		Log.v(TAG, formatter.format(beginDate) + " à " + formatter.format(endDate));
		
		Asn1Tag[] asn1tag = new Asn1Tag[] {
				new Asn1Tag(Asn1Tag.OPTIONAL, 
					new Asn1Tag[] {
						new Asn1Tag(Asn1Tag.INTEGER, new byte[] { (byte)0x02 })
					}
				),
				new Asn1Tag(Asn1Tag.INTEGER, new byte[] { (byte)0x01 }),
				new Asn1Tag(Asn1Tag.SEQUENCE,
					new Asn1Tag[] {
						new Asn1Tag(Asn1Tag.OBJECT_ID, asn1TagEncrypt),
						new Asn1Tag(Asn1Tag.NULL, new byte[0])
					}
				),
				new Asn1Tag(Asn1Tag.SEQUENCE,
					new Asn1Tag[] {
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.commonName),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, cn.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.organizationUnit),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, ou.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.organization),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, o.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.locality),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, l.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.state),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, s.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.country),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, c.getBytes())
									}
								)
							}
						)
					}
				),
				new Asn1Tag(Asn1Tag.SEQUENCE,
					new Asn1Tag[] {
						new Asn1Tag(Asn1Tag.UTC_TIME, formatter.format(beginDate).getBytes()),
						new Asn1Tag(Asn1Tag.UTC_TIME, formatter.format(endDate).getBytes())
					}
				),
				new Asn1Tag(Asn1Tag.SEQUENCE,
					new Asn1Tag[] {
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.commonName),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, cn.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.organizationUnit),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, ou.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.organization),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, o.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.locality),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, l.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.state),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, s.getBytes())
									}
								)
							}
						),
						new Asn1Tag(Asn1Tag.SET,
							new Asn1Tag[] {
								new Asn1Tag(Asn1Tag.SEQUENCE,
									new Asn1Tag[] {
										new Asn1Tag(Asn1Tag.OBJECT_ID, Asn1Tag.country),
										new Asn1Tag(Asn1Tag.PRINTABLE_STRING, c.getBytes())
									}
								)
							}
						)
					}
				),
				new Asn1Tag(Asn1Tag.RAW_DATA, publickey.getEncoded())
			};
		
		r = Asn1Tag.asn1_add(r, Asn1Tag.SEQUENCE, asn1tag);

		//Signature sign = Signature.getInstance("SHA1WithRSAEncryption");
		sign.initSign(privatekey);
		sign.update(r);
			
		r = Asn1Tag.asn1_add(new byte[0], Asn1Tag.SEQUENCE, 
				new Asn1Tag[] { 
					new Asn1Tag(Asn1Tag.RAW_DATA, r),
					new Asn1Tag(Asn1Tag.SEQUENCE,
						new Asn1Tag[] {
							new Asn1Tag(Asn1Tag.OBJECT_ID, asn1TagEncrypt),
							new Asn1Tag(Asn1Tag.NULL, new byte[0])
						}
					),
					new Asn1Tag(Asn1Tag.BIT_STRING,
						new Asn1Tag[] {
							new Asn1Tag(Asn1Tag.INTEGER, sign.sign())
						}
					)
				}
			);
			
		CertificateFactory cf = CertificateFactory.getInstance("x509");
		return cf.generateCertificate(new ByteArrayInputStream(r));
	}

	private class OKListener implements OnClickListener {
		public void onClick(View v) {
			
			try
			{
				EditText etAlias = (EditText) findViewById(R.id.alias);
				EditText etSize = (EditText) findViewById(R.id.size);
				EditText etPassword = (EditText) findViewById(R.id.keypassword);
				EditText etConfirmPassword = (EditText) findViewById(R.id.confirm_keypassword);
				
				String alias = etAlias.getText().toString();
				String size = etSize.getText().toString();
				String password = etPassword.getText().toString();
				String confirmPassword = etConfirmPassword.getText().toString();
				
				if(confirmPassword.equals(password) != true)
				{
					Toast.makeText(getApplicationContext(), 
						getString(R.string.failure_confirm_password),
						Toast.LENGTH_SHORT).show();
					return;
				}
				
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keytype);
				
				Log.v(TAG, "alias="+alias+", size="+size+", keytype="+keytype);
				
				keyGen.initialize(Integer.parseInt(size));
				KeyPair keyPair = keyGen.genKeyPair();
				PrivateKey privateKey = keyPair.getPrivate();
				PublicKey  publicKey  = keyPair.getPublic();

				Certificate cert = buildCert(publicKey, privateKey);
				
				keystore.setKeyEntry(alias, privateKey, password.toCharArray(), new Certificate[] { cert } );
				
				MyKeyStoreActivity.saveKeyStore();
				
				Toast.makeText(getApplicationContext(), 
					getString(R.string.successful_generate_key),
					Toast.LENGTH_SHORT).show();
				
			}
			catch(Exception e)
			{
				Toast.makeText(getApplicationContext(), 
					getString(R.string.failure_generate_key),
					Toast.LENGTH_SHORT).show();

				Log.v(TAG, e.toString());
			}
			
			finish();
		}
	}

	private class CancelListener implements OnClickListener {
		public void onClick(View v) {
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.generate_key);
		setTitle(R.string.generate_key);

		Spinner keytypeSpinner  = (Spinner)findViewById(R.id.keytype_spinner);
		
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.keytypes, android.R.layout.simple_spinner_item); 
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keytypeSpinner.setAdapter(adapter);

		// FIXME:
		//keytype = getString(R.array.keytypes)[0];

		keytypeSpinner.setOnItemSelectedListener (new OnItemSelectedListener () {
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				keytype = parent.getItemAtPosition(position).toString();
			}
			
			public void onNothingSelected(AdapterView parent) {
			  keytype = "RSA";
			}
		});

		Button buttonOK = (Button) findViewById(R.id.ok);
		buttonOK.setOnClickListener(new OKListener());

		Button buttonCancel = (Button) findViewById(R.id.cancel);
		buttonCancel.setOnClickListener(new CancelListener());
		
		keystore = MyKeyStoreActivity.getKeyStore();
	}

}
