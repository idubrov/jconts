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
package com.google.code.jconts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.code.jconts.Computation;
import com.google.code.jconts.Continuation;

public class MultiWaitComputation implements Computation<List<Object>> {

	private volatile int count;
	private final Computation<?>[] tasks;
	private final List<Object> results;
	private AtomicBoolean finished = new AtomicBoolean();

	public MultiWaitComputation(Computation<?>... tasks) {
		this.count = tasks.length;
		this.results = new ArrayList<Object>();
		this.tasks = tasks;
		for (int i = 0; i < tasks.length; ++i) {
			results.add(null);
		}
	}

	@Override
	public void execute(final Continuation<? super List<Object>> finish) {
		for (int i = 0; i < tasks.length; ++i) {
			final int index = i;
			tasks[i].execute(new Continuation<Object>() {
				@Override
				public void invoke(Object data) {
					// FIXME: thread safety...
					results.set(index, data);
					count--;
					if (count == 0) {
						if (finished.compareAndSet(false, true)) {
							finish.invoke(results);
						}
					}
				}

				@Override
				public void setException(Throwable t) {
					if (finished.compareAndSet(false, true)) {
						finish.setException(t);
					}
				}
			});
		}
	}

}
