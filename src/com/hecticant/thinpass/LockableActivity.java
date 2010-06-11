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

package com.hecticant.thinpass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.hecticant.thinpass.security.SecurityService;

public abstract class LockableActivity extends SecureActivity {
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) { 
	    	Log.i(TAG, "Received LOCK broadcast");
	    	  
	    	startActivity(new Intent(LockableActivity.this, 
      			UnlockActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	    }
	};
	
	private IntentFilter intentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	       
	    intentFilter = new IntentFilter();
	    intentFilter.addAction(SecurityService.LOCK_ACTION);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(broadcastReceiver, intentFilter);
	}
	
	@Override 
	protected void onStop() {
		super.onStop();
		unregisterReceiver(broadcastReceiver);
	}
}
