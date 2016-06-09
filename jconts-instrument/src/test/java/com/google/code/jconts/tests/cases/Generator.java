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

import com.google.code.jconts.Computation;
import com.google.code.jconts.Continuation;
import com.google.code.jconts.util.EmptyContinuation;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Generator<T> implements Iterable<T> {

	private T data;
	private Continuation<? super Void> next;
	private boolean finished = false;
	private BlockingQueue<Continuation<? super Void>> queue = new ArrayBlockingQueue<Continuation<? super Void>>(
			1);

	public Generator() {
		next = new EmptyContinuation<Void>() {
			public void invoke(Void data) {
				generate().execute(new EmptyContinuation<Void>() {
					@Override
					public void invoke(Void data) {
						finished = true;
						// Make hasNext exit the take().
						queue.offer(new EmptyContinuation<Void>());
					}
				});
			};
		};
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				if (finished) {
					return false;
				}
				next.invoke(null);
				try {
					next = queue.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return !finished;
			}

			@Override
			public T next() {
				return data;
			}

			@Override
			public void remove() {
			}
		};
	}

	protected abstract Computation<Void> generate();

	public Computation<Void> yield(final T data) {
		return new Computation<Void>() {
			@Override
			public void execute(Continuation<? super Void> c) {
				Generator.this.data = data;
				Generator.this.queue.offer(c);
			}
		};
	}
}
