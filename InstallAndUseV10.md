# Installation version 1.0 #

This apply to release 1.0

## Create your keystore ##

Release 1.0 don't let you create key directly, you must create first a keystore and put it on your sdcard. The keystore must be named **"MyKeyStore.bks"** and must be **"BKS"** type.

---

Have a look on http://www.bouncycastle.org/specifications.html for keystore creation and management on your computer.

## Configure ##

Run MyKeystore application on your phone and configure passphrase (to the passphrase of your keystore set on creation) in menu preference.

## Use intents ##

```
"com.android.keystore.MyKeyStoreActivity.SIGN"
"com.android.keystore.MyKeyStoreActivity.VERIFY"
"com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PUBKEY"
"com.android.keystore.MyKeyStoreActivity.ENCRYPT_WITH_PRIVKEY"
"com.android.keystore.MyKeyStoreActivity.DECRYPT_WITH_PUBKEY"
"com.android.keystore.MyKeyStoreActivity.DECRYPT_WITH_PRIVKEY"
```

## Parameters ##

For each intent you can provide this parameters:

| "ALIAS"    | is the name of certificat/key to use in keystore                 |
|:-----------|:-----------------------------------------------------------------|
| "PASSWORD" | is necessary if you use private key (not set for certificat use) |
| "ALGO"     | specify witch algorithm you want to use                          |
| "DATA"     | byte array of data to deal with                                  |

## What can you do with this ##

You can sign message or verify message signature that you receive (usefull for example for application command received by SMS, don't know if somebody project to do this...)

You can encrypt data mail to send to serveur and decrypt it.

More...