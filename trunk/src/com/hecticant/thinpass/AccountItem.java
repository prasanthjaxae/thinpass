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

import java.io.Serializable;

/**
 * A container for unencrypted account information.
 * 
 * @author Pedro Fonseca
 * @see persistence.Account
 */
public class AccountItem implements Serializable {
	private static final long serialVersionUID = 3554386735974851360L;
	
	@SuppressWarnings("unused")
	private final long accountId;
	
	private String description;
	private String username;
	private String password;
	
	public AccountItem(long accountId, String description, String username, 
			String password) 
	{
		this.accountId = accountId;
		this.description = description;
		this.username = username;
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
