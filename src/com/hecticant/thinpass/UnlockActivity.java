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

import java.util.Arrays;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hecticant.thinpass.R;
import com.hecticant.thinpass.security.AuthenticationResult;
import com.hecticant.thinpass.security.SecurityService;

public final class UnlockActivity extends SecureActivity 
	implements OnClickListener {
		
	private ProgressDialog dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	startService(new Intent(this, SecurityService.class));
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.unlock);
        
        Button button = (Button)findViewById(R.id.ok);
        button.setOnClickListener(this);
    }
    
	public void onClick(View v) {
		EditText et1 = (EditText)findViewById(R.id.entry);
        char[] password = new char[et1.length()];
        et1.getText().getChars(0, et1.length(), password, 0);
        et1.getText().clear();
        
        // Force the keyboard to hide.
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et1.getWindowToken(), 0);
        
        new UnlockTask().execute(password);
	}
	
	private void presentErrorDialog(String message, final boolean restart) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		                if (restart) {
		                	finish();
		                }
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private class UnlockTask extends AsyncTask<char[],Void,AuthenticationResult> {
		protected AuthenticationResult doInBackground(char[]... params) {
			AuthenticationResult ar = secService.authenticate(params[0]);
			Arrays.fill(params[0], (char)0);
			return ar;
		}
		
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(UnlockActivity.this, "", 
                    "Authenticating. Please wait...", true);
		}
		
		@Override
		protected void onPostExecute(AuthenticationResult result) {
			dialog.cancel();
			dialog = null;
			
			Log.i(TAG, "Authentication status: " + result.getStatus().toString());
			
			String message = "";
			switch (result.getStatus()) {
        		case AUTHENTICATED:
        			Log.i(TAG, "Authenticated");
        			startActivity(new Intent(UnlockActivity.this, 
        					MenuActivity.class));
        			break;
        
        		case FAILED:
        			message = "Wrong password";
        			presentErrorDialog(message, false);
        			break;
        		
        		case WIPED:
        			message = "Database deleted. ";
        		// FALLTHROUGH
        		case LOCKED:
        			message = message + "Application Locked ";
        			presentErrorDialog(message, true);
        			
        			// Explicitly stop the SecurityService if the user locks 
        			// out the application.
        			secService.stopSelf();
        			break;
        		
        		default:
        			message = String.format("Authentication error (%d)", 
        					result.getStatus());
        			presentErrorDialog(message, false);
        			break;
			}    
		}
    }
}
