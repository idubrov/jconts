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

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.IsAsync;

/**
 * Simple asynchronous methods.
 */
public class ExceptionIT {

	@Test(expected = CustomException.class)
	public void testSimpleException() throws Exception {
		try {
			Async.waitCompleted(executeSimpleException());
		} catch (ExecutionException ex) {
			throw (Exception) ex.getCause();
		}
	}

	@IsAsync
	public Computation<Void> executeSimpleException() throws CustomException {
		throw new CustomException();
	}

	@Test(expected = CustomException.class)
	public void testComplexException() throws Exception {
		try {
			Async.waitCompleted(executeComplexException());
		} catch (ExecutionException ex) {
			throw (Exception) ex.getCause();
		}
	}

	@IsAsync
	public Computation<Void> executeComplexException() throws CustomException {
		Async.await(executeSimpleException());
		return Async.areturn();
	}

	@Test
	public void testCatchException() throws Exception {
		Assert.assertTrue(Async.waitCompleted(executeCatchException()));
	}

	@IsAsync
	public Computation<Boolean> executeCatchException() {
		try {
			Async.await(executeSimpleException());
		} catch (CustomException ex) {
			return Async.areturn(true);
		}
		return Async.areturn(false);
	}

	static class CustomException extends Exception {
	}
}
