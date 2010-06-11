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

public interface PolicyProvider {
	
	boolean canAcceptPassword(char[] password);

	boolean canRetryAuthentication(int attemptCount);

	int authenticationAttemptsLeft(int attemptCount);

	/**
	 * 
	 * @param attemptCount the number of preceding authentication attempts.
	 * @return the delay, in seconds, before the next attempt.
	 */
	float delayForAttempt(int attemptCount);

	/**
	 * Defines the application behavior if authentication fails, which happens 
	 * if the user exceeds the number of attempts allowed to unlock the password 
	 * manager.  
	 * 
	 * @param attemptCount 	the number of times the user failed to authenticate.
	 * @return 	a <code>boolean</code> indicating if the the database must 
	 * 			be wiped after authentication fails <code>attemptCount</code>
	 * 			times.
	 * @see #authenticationAttemptsLeft(int)
	 */
	boolean shouldWipeDatabase(int attemptCount);

	/**
	 * Returns the maximum time interval during which the password manager
	 * should remain unlocked.
	 * 
	 * @return a time interval in minutes.
	 */
	int lockTimeout();

}