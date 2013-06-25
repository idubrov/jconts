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

import static com.google.code.jconts.Async.*;

import org.junit.Test;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.IsAsync;
import com.google.code.jconts.tests.AsyncSleep;

/**
 * Simple asynchronous methods.
 */
public class GeneratorIT extends Generator<String> {

	@Test
	public void test() throws InterruptedException {
		for (String data : this) {
			System.out.println("Data: " + data);
		}
	}

	@IsAsync
	public Computation<Void> generate() {
		for (int i = 0; i < 10; ++i) {
			await(AsyncSleep.sleep(100L));
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			await(yield("Value " + i));
		}
		return Async.areturn();
	}

}
