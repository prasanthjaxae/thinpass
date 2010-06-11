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

import android.content.Context;
import android.content.res.Resources;

import com.hecticant.thinpass.R;

/**
 * A <code>PolicyProvider</code> that obtains its policy from the bundled
 * policy.xml file.
 * 
 * @author Pedro Fonseca
 */
public class DefaultPolicyProvider implements PolicyProvider {

	private Resources resources; 
	
	public DefaultPolicyProvider(Context context) {
		this.resources = context.getResources();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean canAcceptPassword(char[] password) {
		return password.length
			>= resources.getInteger(R.integer.min_password_length);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean canRetryAuthentication(int attemptCount) {
		return authenticationAttemptsLeft(attemptCount) > 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int authenticationAttemptsLeft(int attemptCount) {
		return resources.getInteger(R.integer.auth_attempts) - attemptCount;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final float delayForAttempt(int attemptCount) {
		float delay = resources.getInteger(R.integer.delay_base);
		float factor = resources.getInteger(R.integer.delay_factor);
		for (int i = 1; i < attemptCount; ++i) {
			delay *= factor; 
		}
		return delay;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean shouldWipeDatabase(int attemptCount) {
		return !canRetryAuthentication(attemptCount) 
			&& resources.getBoolean(R.bool.wipe_database);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int lockTimeout() {
		return resources.getInteger(R.integer.lock_timeout);
	}
}
