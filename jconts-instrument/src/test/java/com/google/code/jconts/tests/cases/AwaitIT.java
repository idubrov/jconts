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

import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.Continuation;
import com.google.code.jconts.IsAsync;

/**
 * Simple asynchronous methods.
 */
public class AwaitIT {

	@Test
	public void test() throws Exception {
		Async.waitCompleted(Async.multiAwait(executeSleep("1: "),
				executeSleep("2: ")));
	}

	@IsAsync
	public Computation<Void> executeSleep(String data) {
		for (int i = 0; i < 3; i++) {
			data = Async.await(longOperation(data));
			System.out.println("Processed: " + data);
		}

		System.out.println("After processing data: " + data);
		return Async.areturn();
	}

	public static Computation<String> longOperation(final String data) {
		return new Computation<String>() {
			@Override
			public void execute(final Continuation<? super String> c) {
				Timer timer = new Timer(true);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						c.invoke(data + " processed!");
					}
				}, 1000);
			}
		};
	}

}
