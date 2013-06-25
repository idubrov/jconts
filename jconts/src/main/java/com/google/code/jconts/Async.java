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
package com.google.code.jconts;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.google.code.jconts.util.MultiWaitComputation;

public final class Async {

	public static <T> T await(final Computation<T> task) {
		throw new IllegalStateException(
				"This method should not be invoked directly!");
	}

	public static <T> Computation<T> areturn(final T value) {
		throw new IllegalStateException(
				"This method should not be invoked directly!");
	}

	public static Computation<Void> areturn() {
		return areturn(null);
	}

	public static Computation<List<Object>> multiAwait(Computation<?>... tasks) {
		return new MultiWaitComputation(tasks);
	}

	/**
	 * This method returns the result of the computation. Despite the
	 * {@link #await(Computation)} method, it waits for the computation to be
	 * finished, so it could be invoked from the non-instrumented code.
	 * <p>
	 * Instrumented methods should call {@link #await(Computation)} instead to
	 * wait asynchronously (without blocking).
	 * 
	 * @param <T>
	 * @param task
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static <T> T waitCompleted(final Computation<T> task)
			throws InterruptedException, ExecutionException {

		final Result<T> result = new Result<T>();
		final FutureTask<T> future = new FutureTask<T>(result);

		task.execute(new Continuation<T>() {
			public void invoke(T data) {
				result.setResult(data);
				future.run();
			};

			@Override
			public void setException(Throwable t) {
				result.setThrowable(t);
				future.run();
			}
		});

		return future.get();
	}

	static class Result<V> implements Callable<V> {
		private V result;
		private Throwable throwable;

		public void setResult(V result) {
			this.result = result;
		}

		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
		}

		@Override
		public V call() throws Exception {
			if (throwable != null) {
				throw throwable instanceof Exception ? (Exception) throwable
						: new InvocationTargetException(throwable);
			}
			return result;
		}
	}

	private Async() {
	}
}
