/*
 * Copyright 2010 Pedro Fonseca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hecticant.thinpass.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * Provides symmetric key cryptography and hashing.
 * 
 * @author Pedro Fonseca
 */
public class CryptUtil {
	private static final String TAG = "CryptUtil";
	private static final int KEYSIZE = 128;
		
	private static SecureRandom rd;
	static {
		try {
			rd = SecureRandom.getInstance("SHA1PRNG");
		} 
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getLocalizedMessage());
			throw new RuntimeException("Certainly a random error", e);
		}
	}
	
	private static final byte[] defaultIV = { 127, 24, 123, 23, 93, 7, 15, 0, 
												9, 4, 8, 15, 16, 23, 42, 1}; 
	
	public static SecretKey genMasterKey(char[] pwd, byte[] salt) {
		SecretKeyFactory kf;
		PBEKeySpec ks;
		SecretKey sk;
		try {
			// Android does not support PBKDF2WithHmacSHA1 but BouncyCastle
			// is included. For a bit of archaeology see
			// 	http://www.google.com/codesearch/p?hl=en#atE6BTe41-M
			//		/libcore/security/src/main/java/org/bouncycastle/jce
			//		/provider/BouncyCastleProvider.java
			kf = SecretKeyFactory.getInstance("PBEWITHSHAAND128BITAES-CBC-BC");
			ks = new PBEKeySpec(pwd, salt, 1000, KEYSIZE);
			sk = kf.generateSecret(ks);
			ks.clearPassword();
		} 
		catch (GeneralSecurityException e) {
			Log.e(TAG, e.getLocalizedMessage());
			throw new IllegalStateException("Error generating secret key", e);
		}

		return sk;
	}
	
	public static SecretKey genRandomKey() {
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(KEYSIZE, rd);
		} catch (GeneralSecurityException e) {
			Log.e(TAG, e.getLocalizedMessage());
			throw new IllegalStateException("Error generating secret key", e);
		}
		
		return kg.generateKey();
	}
	
	public static byte[] encrypt(byte[] key, byte[] clearText) {
		return encrypt(keyFromBytes(key), clearText);
	}
	
	public static byte[] decrypt(byte[] key, byte[] encryptedText) {
		return decrypt(keyFromBytes(key), encryptedText);
	}
	
	public static byte[] encrypt(SecretKey key, byte[] clearText) {
		return transform(Cipher.ENCRYPT_MODE, key, clearText);
	}
	
	public static byte[] decrypt(SecretKey key, byte[] encryptedText) {
		return transform(Cipher.DECRYPT_MODE, key, encryptedText);
	}
	
	protected static SecretKey keyFromBytes(byte[] keyBytes) {
		SecretKeySpec sks = new SecretKeySpec(keyBytes, "AES");
		return sks;
	}
	
	private static byte[] transform(int mode, SecretKey key, byte[] text) {
		if (key == null || text == null) {
			throw new NullPointerException();
		}
			
		byte[] transformedText = null;
		
		try {
			// CBC is used here because the implementation has some trouble
			// with ECB (thus the default IV).
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec sks = new SecretKeySpec(key.getEncoded(), "AES");
			IvParameterSpec ivs = new IvParameterSpec(defaultIV);
			c.init(mode, sks, ivs, rd);
			transformedText = c.doFinal(text);
		} 
		catch (GeneralSecurityException e) {
			Log.e(TAG, "transform: " + Log.getStackTraceString(e));
		}
		
		return transformedText;
	}
	
	/**
	 * Indispensable for reading slashdot.
	 * 
	 * @return
	 */
	public static byte[] getSalt() {
		byte[] salt = new byte[16];
		rd.nextBytes(salt);
		return salt;
	}
	
	public static byte[] hash(byte[] text, byte[] salt) {	
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} 
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "hash: " + e.getLocalizedMessage());
		}
			
		md.update(text);
		md.update(salt);
		
		return md.digest();
	} 
	
}
