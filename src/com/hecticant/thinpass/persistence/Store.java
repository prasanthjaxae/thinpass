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

import java.util.List;

/**
 * This interface defines operations on the application backing store. 
 * It allows insertion and retrieval of user and application data.
 * 
 * @author Pedro Fonseca
 */
public interface Store {

	long countAccounts();
	
	List<Account> accountsInRange(long offset, long limit);
	
	void addAccount(String username, byte[] password, byte[] description);
	
	void updateAccount(Account acc);
	
	byte[] valueForKey(String key);
	
	/**
	 * Adds a new value to the application data store.
	 * 
	 * @param key		
	 * @param value		
	 * @param replace	if a value that is mapped can be replaced.
	 */
	void setValueForKey(String key, byte[] value, boolean replace);
	
	/**
	 * Irrevocably destroys the store.
	 */
	void obliterate();
	
	/**
	 * Closes the database backing the store.
	 */
	void close();
}
