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
import android.content.Intent;
import android.os.Bundle;

import com.hecticant.thinpass.persistence.StoreController;

/**
 * The entry point for the application. This is just a wrapper to create the
 * appropriate activity after checking the application data. If a master 
 * password is set a {@link UnlockActivity} is started; otherwise a 
 * {@link SetMasterPasswordActivity}.
 * 
 * @author Pedro Fonseca
 */
public class PasswordManager extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	        
    	if (hasKey()) {
    		startActivity(new Intent(PasswordManager.this, 
    				UnlockActivity.class)
    				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    	} else {
    		startActivity(new Intent(PasswordManager.this, 
    				SetMasterPasswordActivity.class)
    				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    	}
    }
    
    private boolean hasKey() {
    	StoreController sc = StoreController.getInstance(getApplicationContext());
    	return sc.hasKey();
    }
}