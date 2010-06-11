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

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.hecticant.thinpass.R;
import com.hecticant.thinpass.security.SecurityService;

public final class SetMasterPasswordActivity extends SecureActivity 
	implements OnClickListener {
	
	private boolean canSetPassword;
	
	/**
	 * A {@link TextWatcher} to handle password confirmation. The button to 
	 * set the password is disabled until this TextWatcher verifies that the 
	 * characters entered in the password and confirm password fields are 
	 * identical. The verification is performed whenever the 
	 * <code>afterTextChanged</code> callback is triggered. 
	 */
	private final TextWatcher textWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			// s === ((EditText)findViewById(R.id.ConfirmPassword)).getText()
			Editable s0 = ((EditText)findViewById(R.id.Password)).getText();
			
			int passwordLength = s0.length();
			if (passwordLength == 0 || s.length() != passwordLength) { 
				// If the EditText fields are reset canSetPassword resets too
				canSetPassword = false;
			} else {
				char[] s0Text = new char[passwordLength];
				char[] sText = new char[passwordLength];
				s0.getChars(0, passwordLength, s0Text, 0);
		        s.getChars(0, passwordLength, sText, 0);
		        canSetPassword = Arrays.equals(s0Text, sText);
		        
		        Arrays.fill(s0Text, (char)0);
	        	Arrays.fill(sText, (char)0);
			}
			
			Button b0 = (Button) findViewById(R.id.SetPassButton);
			b0.setEnabled(canSetPassword);
		}
		
		public void beforeTextChanged(CharSequence s, int start, int count, 
				int after) { /* NOT IMPLEMENTED */ }
		public void onTextChanged(CharSequence s, int start, int before, 
				int count) { /* NOT IMPLEMENTED */ }
	};
	
	public void onClick(View v) {
		EditText et1 = (EditText)findViewById(R.id.Password);
        int passwordLength = et1.length();
        char[] password = new char[passwordLength];
        et1.getText().getChars(0, passwordLength, password, 0);
        
        // Cleanup
        et1.getText().clear();   
        ((EditText)findViewById(R.id.CheckPass)).getText().clear();
        
        if (secService.addKey(password, true)) {
        	Arrays.fill(password, (char)0);
        	startActivity(new Intent(this, MenuActivity.class));
        } else {
        	// Do something to warn the user
        }
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	startService(new Intent(this, SecurityService.class));
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.setpassword);
        
        EditText confirmPasswordField = (EditText)findViewById(R.id.CheckPass);
        confirmPasswordField.addTextChangedListener(textWatcher);
        
        Button button = (Button)findViewById(R.id.SetPassButton);
        button.setOnClickListener(this);
        button.setEnabled(false);
    }
}
