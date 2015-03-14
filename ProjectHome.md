The goal is to provide a central keystore on sdcard to use for sign/verify/encrypt/decrypt in other application via intent access.


# Getting started #

You can have a look to test provide examples of use at
http://code.google.com/p/keystoremanager/source/browse/#svn/trunk/test

Request for signing datas:
```

Intent new_intent;
		
new_intent = new Intent("com.android.keystore.MyKeyStoreActivity.SIGN");
new_intent.putExtra("ALIAS", "test");
new_intent.putExtra("PASSWORD", "test");
new_intent.putExtra("ALGO", "MD5withRSA");
new_intent.putExtra("DATA", texte.getBytes());
startActivityForResult(new_intent, TEST_SIGN);

```

For now this suppose to have a keystore named MyKeyStore.bks on sdcard
with private key alias "test" and password "test".


# Related projects #

  * seek-for-android http://code.google.com/p/seek-for-android/


## Last News ##

**27/04/2010**

MyKeyStore-1.1.apk released, with support for RSA/DSA keys on-board generation.

**16/04/2010**

MyKeyStore-1.0.apk seems badly signed, if you download this before the 2010/04/16 you should download it again to correctly install.

MyKeyStore.bks provided for testing use keystore password "demodemo" end key test password "testtest".
