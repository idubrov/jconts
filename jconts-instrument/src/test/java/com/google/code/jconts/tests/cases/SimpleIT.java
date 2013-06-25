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
package com.google.code.jconts.tests.cases;

import org.junit.Assert;
import org.junit.Test;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.IsAsync;

/**
 * Simple asynchronous methods.
 */
public class SimpleIT {

	@Test
	public void testStatic() throws Exception {
		Assert.assertEquals("value",
				Async.waitCompleted(executeStatic("value")));
	}

	@IsAsync
	public static Computation<String> executeStatic(String param) {
		return Async.areturn(param);
	}

	@Test
	public void testInstance() throws Exception {
		Assert.assertEquals("value",
				Async.waitCompleted(executeInstance("value")));
	}

	@IsAsync
	public Computation<String> executeInstance(String param) {
		return Async.areturn(param);
	}

	@Test
	public void testConditionals() throws Exception {
		// Conditionals
		Assert.assertEquals("Hello!",
				Async.waitCompleted(executeCondition("Hello!")));
		Assert.assertEquals("No!",
				Async.waitCompleted(executeCondition("Bye!")));
	}

	@IsAsync
	public Computation<String> executeCondition(String parameter) {
		String result;
		if (parameter.startsWith("H")) {
			result = parameter;
		} else {
			result = "No!";
		}
		return Async.areturn(result);
	}

	@Test
	public void testCycles() throws Exception {
		// Cycles
		Assert.assertEquals("Hello!Hello!Hello!",
				Async.waitCompleted(executeCycles(3, "Hello!")));
	}

	@IsAsync
	public Computation<String> executeCycles(int count, String parameter) {
		String result = "";
		for (int i = 0; i < count; ++i) {
			result += parameter;
		}
		return Async.areturn(result);
	}

	@Test
	public void testMultipleReturns() throws Exception {
		// Multiple returns
		Assert.assertEquals("Hello!",
				Async.waitCompleted(executeMultipleReturn("Hello!")));
		Assert.assertEquals("No!",
				Async.waitCompleted(executeMultipleReturn("Bye!")));
		Assert.assertEquals("Hello!10",
				Async.waitCompleted(executeMultipleReturn2("Hello!")));
		Assert.assertEquals("No!No!",
				Async.waitCompleted(executeMultipleReturn2("Bye!")));
	}

	@IsAsync
	public Computation<String> executeMultipleReturn(String parameter) {
		if (parameter.startsWith("H")) {
			return Async.areturn(parameter);
		} else {
			return Async.areturn("No!");
		}
	}

	@IsAsync
	public Computation<String> executeMultipleReturn2(String parameter) {
		if (parameter.startsWith("H")) {
			int pos = 0;
			pos = pos + 10;
			return Async.areturn(parameter + pos);
		} else {
			String some = "No!";
			some = some + some;
			return Async.areturn(some);
		}
	}
}
