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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hecticant.thinpass.security.SecurityService;

/**
 * All classes that require access to the {@link SecurityService} must 
 * subclass <code>SecureActivity</code>. This provides a default implementation 
 * for binding and connecting to the service. 
 * 
 * @author Pedro Fonseca
 */
public abstract class SecureActivity extends Activity {
	protected static final String TAG = "SecureActivity";
	
	protected SecurityService secService;
	
	protected boolean isBound;
	
	/* (non-javadoc) @see android.app.Service#LocalServiceSample */
	protected ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
	    	// The service and activities are running on the same process.
			secService = ((SecurityService.LocalBinder)service).getService();
			Log.d(TAG, String.format("%s connected to %s. Locked? %s",
					toString(), secService.toString(), 
					new Boolean(secService.isLocked())));
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        secService = null;
	        Log.d(TAG, toString() + "disconnected from service");
	    }
	};
	
	void doBindService() {
		// Some SecureActivity subclasses are contained in tabs, so we need to 
		// use the application context. See Android issue 2483 for more info.
	    isBound = getApplicationContext().bindService(
	    		new Intent(this, SecurityService.class), connection, 
	    		Context.BIND_AUTO_CREATE);
	    if (isBound) {
	    	Log.i(TAG, "SecurityService bound."); 
	    }
	}

	void doUnbindService() {
	    if (isBound) {
	        // Detach our existing connection.
	    	getApplicationContext().unbindService(connection);
	        isBound = false;
	        Log.i(TAG, "SecurityService unbound.");
	    }
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, String.format("Destroying %s", toString()));
		doUnbindService();
		super.onDestroy();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, String.format("Creating %s", toString()));
        super.onCreate(savedInstanceState);
        doBindService();
    }
}
