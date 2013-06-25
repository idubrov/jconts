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
package com.google.code.jconts.instrument;

import org.objectweb.asm.Type;

public final class Constants {

	public static String OBJECT_NAME = "java/lang/Object";
	public static String OBJECT_DESC = 'L' + OBJECT_NAME + ';';
	public static Type OBJECT_TYPE = Type.getType(OBJECT_DESC);
	
	public static String THROWABLE_NAME = "java/lang/Throwable";
	public static String THROWABLE_DESC = 'L' + THROWABLE_NAME + ';';

	public static String IS_ASYNC_ANNOTATION_DESC = "Lcom/google/code/jconts/IsAsync;";
	public static Type IS_ASYNC_ANNOTATION_TYPE = Type
			.getType(IS_ASYNC_ANNOTATION_DESC);

	public static String ASYNC_NAME = "com/google/code/jconts/Async";
	public static String ASYNC_DESC = 'L' + ASYNC_NAME + ';';

	public static Type ASYNC_TYPE = Type.getType(ASYNC_DESC);

	public static String COMPUTATION_NAME = "com/google/code/jconts/Computation";
	public static String COMPUTATION_DESC = 'L' + COMPUTATION_NAME + ';';

	public static String CONTINUATION_NAME = "com/google/code/jconts/Continuation";
	public static String CONTINUATION_DESC = 'L' + CONTINUATION_NAME + ';';
	public static Type CONTINUATION_TYPE = Type.getType(CONTINUATION_DESC);

	public static String CONTINUATION_INVOKE_NAME = "invoke";
	public static String CONTINUATION_INVOKE_DESC = "(Ljava/lang/Object;)V";
	
	public static String CONTINUATION_SET_EXCEPTION_NAME = "setException";
	public static String CONTINUATION_SET_EXCEPTION_DESC = "(Ljava/lang/Throwable;)V";
	
	public static String CONTINUATION_FIELD = "continuation";

	public static String COMPUTATION_EXECUTE_NAME = "execute";
	public static String COMPUTATION_EXECUTE_DESC = '(' + CONTINUATION_DESC
			+ ")V";

	public static String CTOR_NAME = "<init>";
	public static String DEFAULT_CTOR_DESC = "()V";

	public static String ARETURN_NAME = "areturn";
	public static String ARETURN_VALUE_DESC = "(Ljava/lang/Object;)Lcom/google/code/jconts/Computation;";
	public static String ARETURN_VOID_DESC = "()Lcom/google/code/jconts/Computation;";

	public static String AWAIT_NAME = "await";
	public static String AWAIT_DESC = "(Lcom/google/code/jconts/Computation;)Ljava/lang/Object;";

	private Constants() {
		// No instances.
	}
}
