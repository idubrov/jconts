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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.code.jconts.instrument.context.MethodContext;

/**
 * Class visitor that looks for the methods marked by CPS annotation.
 * 
 * @author Ivan Dubrov
 * 
 */
public class AsyncMethodsAnalyzer extends EmptyVisitor {

	private Map<String, MethodContext> methods;
	private String owner;
	private String ownerSource;

	public AsyncMethodsAnalyzer() {
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.owner = name;
	}

	@Override
	public void visitSource(String source, String debug) {
		super.visitSource(source, debug);
		this.ownerSource = source;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String methodName,
			final String methodDesc, final String signature,
			final String[] exceptions) {
		return new EmptyVisitor() {
			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				// FIXME: Validate return type!
				if (desc.equals(Constants.IS_ASYNC_ANNOTATION_DESC)) {
					if (methods == null) {
						methods = new HashMap<String, MethodContext>();
					}
					methods.put(MethodContext.keyOf(methodName, methodDesc),
							new MethodContext(owner, ownerSource, access,
									methodName, methodDesc, signature,
									exceptions));
				}
				return null;
			}
		};
	}

	public static Map<String, MethodContext> analyze(ClassReader reader) {
		AsyncMethodsAnalyzer lookup = new AsyncMethodsAnalyzer();
		reader.accept(lookup, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
		return lookup.getAsyncMethods();
	}

	public Map<String, MethodContext> getAsyncMethods() {
		return methods != null ? methods : Collections
				.<String, MethodContext> emptyMap();
	}
}
