/**
 * Copyright (C) 2011 Ivan Dubrov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.jconts.instrument.util;

/**
 * Configuration for the agent. Two system properties are defined:
 * <ul>
 * <li><code>jconts.trace</code>, set to <code>true</code> to output the
 * contents of transformed/generated classes. <code>false</code> by default.
 * <li><code>jconts.check</code>, set to <code>true</code> to run checker on the
 * transformed/generated classes. <code>false</code> by default.
 * </ul>
 */
public final class Config {

	private static final String TRACE_CLASSES = "jconts.trace";
	private static final String CHECK_CLASSES = "jconts.check";

	public static boolean isTraceClasses() {
		return Boolean.getBoolean(TRACE_CLASSES);
	}

	public static boolean isCheckClasses() {
		return Boolean.getBoolean(CHECK_CLASSES);
	}
}
