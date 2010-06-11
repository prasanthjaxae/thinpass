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
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hecticant.thinpass.R;
import com.hecticant.thinpass.persistence.Account;
import com.hecticant.thinpass.persistence.StoreController;

/**
 * An activity that fetches and displays in a <code>ListView</code> all accounts
 * from the data store.
 * 
 * @author Pedro Fonseca
 */
public class ListAccountsActivity extends LockableActivity {

	private ListView listView;
		
	private List<Account> accounts;
	
	private List<AccountItem> accountItems;
	
	// Used to cancel any active ListAccountsTask when onDestroy is called.
	@SuppressWarnings("unchecked")
	private AsyncTask listTask;
	
	public ListAccountsActivity() {
		accounts = new ArrayList<Account>();
		accountItems = new ArrayList<AccountItem>();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	listView = new ListView(this);
    	setContentView(listView);
    	
    	listView.setAdapter(new ArrayAdapter<AccountItem>(this, 
    		R.layout.listitem, accountItems) 
    		{
    			LayoutInflater inflater = (LayoutInflater) getSystemService(
    				Context.LAYOUT_INFLATER_SERVICE);
    		
        		@Override
        		public View getView(int position, View convertView, 
        				ViewGroup parent) 
        		{
        			View row;
	        		       		
	        		if (convertView == null) {
	        			row = this.inflater.inflate(R.layout.listitem, null);
	        		} else {
	        			row = convertView;
	        		}
	        		
	        		AccountItem ai = (AccountItem) getItem(position);
	        		
	        		TextView tv = (TextView)row.findViewById(R.id.ListItemDesc);
	        		tv.setText(ai.getDescription());
	        		tv = (TextView)row.findViewById(R.id.ListItemUser);
	        		tv.setText(ai.getUsername());
	        		tv = (TextView)row.findViewById(R.id.ListItemPass);
	        		tv.setText(ai.getPassword());
	        		
    				return row;
        		}
    		}
    	);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// Start filling the list on a background thread every time this 
    	// activity gets back to the top of the stack. This is tolerable if the 
    	// list is small; a better solution would be to have EditAccountActivity
    	// broadcast changes and update the list incrementally after populating 
    	// it when onCreate is called.
    	listTask = new ListAccountsTask().execute();
    }
    
    @Override
	protected void onDestroy() {
    	if (listTask != null) {
    		listTask.cancel(true);
    	}
    	accounts.clear();
    	accountItems.clear();
	    super.onDestroy();
	}
    
    /**
     * This method fetches all stored account information to create <code>
     * AccountItem</code>s, which have the account information in clear text. 
     * The <code>accounts</code> and <code>accountItem</code> lists are 
     * populated by this method.
     * <p>
     * This implementation is quite naïve as it fetches all accounts on the 
     * store in one call. Moreover, it is potentially time consuming and should
     * be executed on a separate thread.
     */
    private void populateAccounts() {
    	StoreController sc = StoreController.getInstance(getApplicationContext());
    	accounts.clear();
    	accountItems.clear();
    	
    	if (secService == null || secService.isLocked()) {
    		Log.w(TAG, 
    		"Called populateAccounts with the Security Service locked or null");
    		return;
    	}
    		
    	accounts.addAll(sc.accounts());
    	
    	for (Account acc: accounts) {
    		// Decrypt the description and password fields to create the 
    		// corresponding AccountItem.
    		byte[] encrypted = acc.getDescription();
    		byte[] clear = secService.decrypt(encrypted);
    		
    		// If decryption fails the password manager may be locked, in which
    		// case populateAccounts should have never been called; or there 
    		// was some GeneralSecurityException that was handled in the layers 
    		// below. The data model forbids null values on the Account fields 
    		// that are decrypted here. 
    		if (clear == null) {
    			// TODO: Make the App exit gracefully...
    			throw new IllegalStateException();
    		}
    		
    		String desc;
			try {
				desc = new String(clear, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				Log.w(TAG, "UTF-16LE is unsuported, using the default encoding");
				desc = new String(clear);
			}
    		
    		encrypted = acc.getPassword();
    		clear = secService.decrypt(encrypted);
    		if (clear == null ) {
    			throw new IllegalStateException();
    		}
    		
    		String pass;
			try {
				pass = new String(clear, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				Log.w(TAG, "UTF-16LE is unsuported, using the default encoding");
				pass = new String(clear);
			}
    		
    		AccountItem ai = new AccountItem(acc.getId(), desc, 
    				acc.getUsername(), pass);
    		accountItems.add(ai);
    		
    		// Notify the list view that the data has changed. This must be done
    		// on the UI thread.
        	Runnable r = new Runnable() {
        		@SuppressWarnings("unchecked")
				public void run() {
        			ArrayAdapter<AccountItem> adapter = 
        				(ArrayAdapter<AccountItem>) listView.getAdapter();
        			adapter.notifyDataSetChanged();
        	    }
        	};
        	listView.post(r);
    	}
    	
    	if (accounts.size() != accountItems.size()) {
    		throw new IllegalStateException("Account/AccountItems mismatch");
    	}
    }
    
    private class ListAccountsTask extends AsyncTask<Void,Void,Void> {
		protected Void doInBackground(Void... params) {
			// The SecurityService may be bound but not yet available, that is,
			// onServiceConnected has not been called yet.
			final long MAX = 5000;
			long current = 500;
			while (secService == null && current < MAX) {
				try {
					synchronized(this) {
						wait(current);
						current *= 1.5;
					}
				} 
				catch (InterruptedException e) {}
			}
			populateAccounts();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			listTask = null;
		}
    }
}
