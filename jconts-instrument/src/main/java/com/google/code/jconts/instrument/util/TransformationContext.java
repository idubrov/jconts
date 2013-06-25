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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Data class that collects all generated classes as well as the transformed
 * code for the class transformer was invoked for.
 */
public class TransformationContext {

	private final Map<String, byte[]> code = new HashMap<String, byte[]>();

	public ClassVisitor writer() {
		ClassVisitor cv = new ClassWriter(0) {

			private String thisName;

			@Override
			public void visit(int version, int access, String name,
					String signature, String superName, String[] interfaces) {
				super.visit(version, access, name, signature, superName,
						interfaces);
				this.thisName = name;
			}

			@Override
			public void visitEnd() {
				super.visitEnd();

				code.put(thisName, toByteArray());
			}
		};

		if (Config.isTraceClasses()) {
			cv = CodeVisitors.tracer(cv);
		}
		return cv;
	}

	/**
	 * Map of the generated classes. Key is the internal name of the class (like
	 * <code>java/lang/Object</code>), value is byte array containing the
	 * bytecode.
	 * 
	 * @return map of the generated classes
	 */
	public Map<String, byte[]> getCode() {
		return code;
	}
}
