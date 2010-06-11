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

package com.hecticant.thinpass.persistence;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * A <code>Store</code> that uses a SQLite database to save persistent user
 * and application data.
 * 
 * @author Pedro Fonseca
 */
public class DefaultStore implements Store {
	private static final String TAG = "Store";
	private static final String DB_NAME = "PassApp";
	private static final String ACC_TABLE = "Accounts";
	private static final String APP_TABLE = "AppData";
	
    private static final String CREATE_DATA =
        "CREATE TABLE IF NOT EXISTS " + ACC_TABLE + " ("
            + "_id INTEGER PRIMARY KEY, "
            + "username TEXT, "
            + "password BLOB NOT NULL, "
            + "description BLOB NOT NULL, "
            + "modification_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
            + "reserved1 TEXT DEFAULT NULL, " 
            + "reserved2 BLOB DEFAULT NULL);";
    
    private static final String CREATE_KV =
    	"CREATE TABLE IF NOT EXISTS " + APP_TABLE + " ("
    		+ "key TEXT PRIMARY KEY, value BLOB);";
        	
    private SQLiteDatabase db;
	
    private File dbPath;
    
	public DefaultStore(Object context) {
		if (!(context instanceof Context))
			throw new IllegalArgumentException();
			
		Context ctx = (Context) context;
		try { 
			this.db = ctx.openOrCreateDatabase(DB_NAME, 0, null);
			db.execSQL(CREATE_DATA);
			db.execSQL(CREATE_KV);
			
			this.dbPath = ctx.getDatabasePath(DB_NAME);
		} 
		catch (SQLException e) {
			Log.e(TAG, e.getLocalizedMessage());
			throw new IllegalStateException("Cannot access data store", e);
		}
	}

	public long countAccounts() {
		long count = -1;
		Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + ACC_TABLE, null);
		
		if (c.moveToFirst()) {
			count = c.getLong(0);
		}
		c.close();
		return count;
	}
	
	public List<Account> accounts() {
		checkState();
	    Cursor c = db.rawQuery("SELECT * FROM " + ACC_TABLE 
	    		+ " ORDER BY _id ASC" , null);
	    List<Account> list = getAccountSet(c);
	    c.close();
	    return list;
	}
	
	public List<Account> accountsInRange(long offset, long limit) {
		checkState();
		Cursor c = db.rawQuery("SELECT * FROM " + ACC_TABLE 
	    		+ " ORDER BY _id ASC LIMIT " + limit + " OFFSET " + offset, 
	    		null);
	    List<Account> list = getAccountSet(c);
	    c.close();
	    return list;
	}
		
	public void addAccount(String username, byte[] password, byte[] description) 
	{
		checkState();
		try {
			ContentValues values = new ContentValues();
			values.put("username", username);
			values.put("password", password);
			values.put("description", description);
			
			long rowId = db.insertOrThrow(ACC_TABLE, null, values);
			Log.d(TAG, "Account added to the database. Row ID: " + rowId);
		}
		catch (SQLException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	public void updateAccount(Account acc) {
		checkState();
		try {
			ContentValues values = new ContentValues();
			values.put("_id", acc.getId());
			values.put("username", acc.getUsername());
			values.put("password", acc.getPassword());
			values.put("description", acc.getDescription());
			
			long rowId = db.replaceOrThrow(ACC_TABLE, null, values);
			Log.d(TAG, String.format("Account updated (%d) %s", rowId, acc));
		}
		catch (SQLException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	public byte[] valueForKey(String key) {
		checkState();
		Cursor c = db.rawQuery("SELECT value FROM " + APP_TABLE 
				+ " WHERE key = ?", new String[] { key });
		
		byte[] value = null;
		if (c.moveToFirst()) {
			value = c.getBlob(0);
		}
		c.close();
		return value;
	}
	
	public void setValueForKey(String key, byte[] value, boolean replace) {
		if (key == null || value == null) {
			Log.d(TAG, "Tried to insert null key or value");
			return;
		}
		checkState();
		try {
			ContentValues values = new ContentValues();
			values.put("key", key);
			values.put("value", value);
			if (replace) {
				db.replaceOrThrow(APP_TABLE, null, values);
			} else {
				db.insertOrThrow(APP_TABLE, null, values); 
			}
		}
		catch (SQLException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	public void obliterate() {
		checkState();
		try {
			db.execSQL("DROP TABLE " + ACC_TABLE + ";");
			db.execSQL("DROP TABLE " + APP_TABLE + ";");
		} 
		catch (SQLException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
		
		db.close();
		dbPath.delete();
	}
		
	public void close() {
		db.close();
	}
	
	SQLiteDatabase getRawStore() {
		return db;
	}
	
	private Account nextAccount(Cursor c) {
		Account a = null;
		boolean hasNext = !c.isClosed() && c.moveToNext();
		if (hasNext) {
			a = new Account(c.getLong(0), c.getString(1), c.getBlob(2), 
					c.getBlob(3), c.getString(4));
			Log.d(TAG, a.toString());
		}
		return a;
	}
	
	private List<Account> getAccountSet(Cursor c) {
		List<Account> accounts = new ArrayList<Account>();
		
		Account a;
		while ((a = nextAccount(c)) != null) {
			accounts.add(a);
		}
		return accounts;
	}
	
	private void reopen() throws SQLiteException {
		db = SQLiteDatabase.openDatabase(dbPath.getPath(), null, 
				SQLiteDatabase.OPEN_READWRITE);
	}
	
	void checkState() {
		if (db == null || !db.isOpen()) {
			try {
				reopen(); //TODO: check read/write mode
			} 
			catch (SQLiteException e) {
				Log.e(TAG, e.getLocalizedMessage());
				throw new IllegalStateException("Cannot access data store", e);
			}
		}
	}
}
