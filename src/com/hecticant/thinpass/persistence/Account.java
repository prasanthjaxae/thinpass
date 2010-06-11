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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * A container for user data saved in the application store.
 * 
 * @author Pedro Fonseca
 * @see Store
 */
public class Account implements Serializable {
	private static final long serialVersionUID = 583202560576552968L;

	private static final String TAG = "Account";
	
	private final long id;
	private String username;
	private byte[] description;
	private byte[] password;
	private Date modificationDate;
	
	/**
	 * 
	 * @param id
	 * @param username
	 * @param password
	 * @param description
	 */
	public Account(long id, String username, byte[] password, 
			byte[] description, String dateString) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.description = description;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.modificationDate = df.parse(dateString);
		} catch (ParseException e) {
			Log.w(TAG, e.getLocalizedMessage());
		}
	}

	/* Getters and setters. */
	public final String getUsername() {
		return username;
	}

	public final void setUsername(String username) {
		this.username = username;
	}

	public final byte[] getPassword() {
		return password;
	}

	public final void setPassword(byte[] password) {
		if (password == null || password.length == 0)
			throw new IllegalArgumentException();
		this.password = password;
	}

	public byte[] getDescription() {
		return description;
	}

	public void setDescription(byte[] description) {
		if (description == null)
			throw new IllegalArgumentException();
		this.description = description;
	}
	
	public final Date getModificationDate() {
		return modificationDate;
	}

	public final long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return String.format("<Account,%d,%s,PWD,DSC,%s>", id, username, 
				modificationDate);
	}
	
	@Override
	public int hashCode() {
		Long l = new Long(id);
		return l.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) { 
			return true; 
		}
		if (other instanceof Account) {
			Account a = (Account) other;
			return this.id == a.id;
		}
		return false;
	}
}
