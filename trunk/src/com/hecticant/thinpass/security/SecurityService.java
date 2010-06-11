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

import java.util.Arrays;
import javax.crypto.SecretKey;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.hecticant.thinpass.persistence.StoreController;
import com.hecticant.thinpass.security.AuthenticationResult.AuthenticationStatus;

/**
 * This service provides authentication, confidentiality and key integrity (but 
 * not store-wide integrity). A <code>SecurityService</code> must comply with 
 * a security policy, exposed by the <code>PolicyProvider</code> interface.
 * <p>
 * The security service is explicitly started when a SetMasterPasswordActivity 
 * or an UnlockActivity is created, and explicitly stopped when the 
 * authentication fails and all attempts are exhausted.
 * 
 * @author Pedro Fonseca
 *
 */
public class SecurityService extends Service {
	private static final String TAG = "SecurityService";
	
	private static final String PREFERENCES = "secpreferences";
	
	public static final int LOCK_MESSAGE = 0x00008000;
	public static final String LOCK_ACTION = "Lock";
	
	public class LocalBinder extends Binder {
        public SecurityService getService() {
            return SecurityService.this;
        }
    }
	
	private final IBinder binder = new LocalBinder();
	
	private Handler lockTimeoutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOCK_MESSAGE) {
				lock();
				Intent intent = new Intent(LOCK_ACTION);
				SecurityService.this.sendBroadcast(intent);
			}
		}
	};
	
	private PolicyProvider policy;
	private StoreController storeController;
	private boolean locked;
	private byte[] key;
	private int attempts;
	
	public SecurityService() {
		this.locked = true;
		this.key = null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Context appContext = getApplicationContext();
		policy = new DefaultPolicyProvider(appContext);
		storeController = StoreController.getInstance(appContext);
		attempts = loadAttempts();
		
		Log.d(TAG, String.format("Created SecurityService<%s,%s,%d>", 
				policy, storeController, attempts));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "StartId " + startId + ",Flags " + flags + ": " + intent);
        return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying SecurityService");
		storeController.close();
		lock();
		super.onDestroy();
	}
	 
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public boolean addKey(char[] password, boolean unlock) {
		if (password == null || !policy.canAcceptPassword(password) 
			|| storeController.hasKey()) {
			return false;
		}
		
		// Generate and store the salt to be used with the new password.
		byte salt[] = CryptUtil.getSalt();
		storeController.setPasswordSalt(salt);
		
		// Generate a master symmetric key from the password and the salt using
		// key derivation.
		SecretKey master = CryptUtil.genMasterKey(password, salt);
		
		// Generate the random symmetric key that will be used to encrypt the 
		// user data. This key is encrypted by the master key and stored along
		// with a challenge for authentication/key verification purposes.
		SecretKey random = CryptUtil.genRandomKey();
		byte[] randomKeyBytes = random.getEncoded();
		byte[] challengeSalt = CryptUtil.getSalt();
		byte[] encryptedKey = CryptUtil.encrypt(master, randomKeyBytes);
		byte[] challenge = CryptUtil.hash(randomKeyBytes, challengeSalt);
		
		boolean didAddKey = storeController.storeKey(encryptedKey, 
				challengeSalt, challenge);
		
		if (didAddKey && unlock) {
			unlock(randomKeyBytes);
		}
		return didAddKey;
	}
	
	public AuthenticationResult authenticate(char[] password) {
		AuthenticationResult authResult = new AuthenticationResult();
		if (password == null) {
			return authResult;
		}
		if (!storeController.hasKey()) {
			authResult.status = AuthenticationStatus.NOKEY;
			return authResult;
		}
		
		// Authentication
		saveAttempts(++attempts);
		byte[] salt = storeController.passwordSalt();
		
		// Key types should have a clearSensitiveData() method...
		SecretKey mkey = CryptUtil.genMasterKey(password, salt);
		
		byte[] check = null;
		byte[] skey = CryptUtil.decrypt(mkey, storeController.key());
		if (skey != null) {
			check = CryptUtil.hash(skey, storeController.keySalt());
		} else {
			Log.w(TAG, "Decryption failed. Probably a bad key...");
		}
		
		if (check != null && Arrays.equals(check, storeController.challenge())){
			unlock(skey);
			authResult.status = AuthenticationStatus.AUTHENTICATED;
		} else if (policy.canRetryAuthentication(attempts)) {
			authResult.status = AuthenticationStatus.FAILED;
		} else if (policy.shouldWipeDatabase(attempts)) {
			storeController.obliterateStore();
			authResult.status = AuthenticationStatus.WIPED;
		} else {
			// TODO: Remove challenge and salt.
			authResult.status = AuthenticationStatus.LOCKED;
		}
		
		// Authenticating or locking out the accounts resets the attempt count.
		// In the second case the attempt count is reset because the next 
		// attempt will refer to a new principal.
		if (authResult.status != AuthenticationStatus.FAILED) {
			resetAttempts();
		}
		
		return authResult;
	}
	
	public final void unlock(byte[] key) {
		if (locked) {
			locked = false;
			this.key = key;
	
			// Start the lockout timer
			Message msg = new Message();
			msg.what = LOCK_MESSAGE;
			long delay = policy.lockTimeout() * 60 * 1000;
			lockTimeoutHandler.sendMessageDelayed(msg, delay);
		}
	}
	
	public final void lock() {
		if (!locked) {
			Arrays.fill(key, (byte) 0);
			locked = true;
		}
	}
	
	public final byte[] encrypt(byte[] clearText) {
		if (locked || clearText == null) {
			return null;
		}
		
		return CryptUtil.encrypt(key, clearText);
	}
	
	public final byte[] decrypt(byte[] cipheredText) {
		if (locked || cipheredText == null) {
			return null;
		}
		
		return CryptUtil.decrypt(key, cipheredText);	
	}
	
	private void resetAttempts() {
		attempts = 0;
		saveAttempts(0);
	}
	
	private void saveAttempts(int n) {
		SharedPreferences sp = getSharedPreferences(PREFERENCES, 0);
		sp.edit().putInt("attempts", n).commit();
	}
	
	private int loadAttempts() {
		SharedPreferences sp = getSharedPreferences(PREFERENCES, 0);
		return sp.getInt("attempts", 0);
	}
}
