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

import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hecticant.thinpass.R;
import com.hecticant.thinpass.persistence.Account;
import com.hecticant.thinpass.persistence.StoreController;

/**
 * Activity to create and update Accounts. This activity requires the <code>
 * SecurityService</code> to encrypt the description and password fields
 * before saving them in the data store.
 * <p>
 * Callers must specify one of two actions when creating this activity:
 * <ul>
 * <li>CREATE_ACCOUNT
 * <li>UPDATE_ACCOUNT
 * </ul> 
 * 
 * @author Pedro Fonseca
 */
public class EditAccountActivity extends LockableActivity 
	implements OnClickListener {
	
	public static final String CREATE_ACCOUNT = "Create"; 
	public static final String UPDATE_ACCOUNT = "Update";
	
	private static final int DIALOG_SUCCESS_ID = 0;
	private static final int DIALOG_FAILURE_ID = 1;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editaccount);
        
        Button button = (Button)findViewById(R.id.SaveAccInfoButton);
        button.setOnClickListener(this);
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		String message;
		if (id == DIALOG_SUCCESS_ID) {
			message = "Account saved";
		} else {
			message = "Cannot save the account";
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		                removeDialog(id);
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}
	
	public void onClick(View v) {
		EditText description = (EditText) findViewById(R.id.EditAccDesc);
		EditText username = (EditText) findViewById(R.id.EditAccUser);
		EditText password = (EditText) findViewById(R.id.EditAccPass);
		
		if (description.length() == 0 || password.length() == 0) {
			// TODO: notify the user or set a listener to change the button
			// enabled state accordingly.
			return;
		}
		
		byte[] descBytes;
		byte[] passBytes;
		try {
			descBytes = description.getText().toString().getBytes("UTF-16LE");
			passBytes = password.getText().toString().getBytes("UTF-16LE");	
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "UTF-16LE is unsuported, using the default encoding");
			descBytes = description.getText().toString().getBytes();
			passBytes = password.getText().toString().getBytes();
		}
		
		// TODO: handle failures.
		byte[] encDesc = secService.encrypt(descBytes);
		byte[] encPass = secService.encrypt(passBytes);
		
		StoreController sc = StoreController.getInstance(null);
		String action = getIntent().getAction();
		Account acc = null;
		
		if (action.equals(CREATE_ACCOUNT)) {
			acc = sc.addAccount(username.getText().toString(), encPass, 
				encDesc);
		} else if (action.equals(UPDATE_ACCOUNT)) {
			acc = (Account) getIntent().getSerializableExtra("Account");
			acc.setDescription(encDesc);
			acc.setPassword(encPass);
			acc.setUsername(username.getText().toString());
			sc.updateAccount(acc);
		}
		
		if (acc != null) {
			description.getText().clear();
			username.getText().clear();
			password.getText().clear();
			showDialog(DIALOG_SUCCESS_ID);
		} else {
			showDialog(DIALOG_FAILURE_ID);
		}
		
		// Force the keyboard to hide.
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
	}
}
