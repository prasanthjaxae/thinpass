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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.hecticant.thinpass.R;

public class MenuActivity extends TabActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabmenu);
	        
	    TabHost tabHost = getTabHost(); 
	    TabHost.TabSpec spec;  
	    Intent intent;  
	
	    // Add the List Accounts tab
	    intent = new Intent().setClass(this, ListAccountsActivity.class);
	    spec = tabHost.newTabSpec("accounts").setIndicator("Accounts")
	    	.setContent(intent);
	    tabHost.addTab(spec);
	
	    // Add the Add New Account tab
	    intent = new Intent().setClass(this, EditAccountActivity.class);
	    intent.setAction(EditAccountActivity.CREATE_ACCOUNT);
	    spec = tabHost.newTabSpec("add").setIndicator("Add")
	    	.setContent(intent);
	    tabHost.addTab(spec);
	
	    tabHost.setCurrentTab(0);
	}
}
