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

package com.hecticant.thinpass.security;

public class AuthenticationResult {

	public enum AuthenticationStatus {
		/**
		 * Some error occurred while authenticating.
		 */
		UNDEFINED,
		
		/**
		 * The master key cannot be verified.
		 */
		NOKEY,
		
		/**
		 * The authentication was successful and the password manager is now
		 * unlocked.
		 */
		AUTHENTICATED, 
		
		/**
		 * The authentication failed. 
		 */ 
		FAILED, 
		
		/**
		 * The authentication failed. The allowed number of attempts was 
		 * exceeded and the information needed to authenticate and recover 
		 * the encryption key was deleted. 
		 */
		LOCKED, 
		
		/**
		 * The authentication failed. The allowed number of attempts was 
		 * exceeded and the database was obliterated, that is, all user 
		 * and persistent application data was deleted.
		 */
		WIPED 
	}
	
	AuthenticationStatus status;
	String message;
	
	AuthenticationResult() {
		this.status = AuthenticationStatus.UNDEFINED;
	}
	
	public AuthenticationResult(AuthenticationStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public AuthenticationStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
