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
package com.google.code.jconts.instrument.gen;

import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.code.jconts.instrument.context.MethodContext;
import com.google.code.jconts.instrument.util.TransformationContext;

/**
 * Class adapter that transforms methods marked by <code>@IsAsync</code>
 * annotation.
 * 
 */
public class AsyncClassAdapter extends ClassAdapter {

	private final TransformationContext context;
	private final Map<String, MethodContext> methods;

	private String name;

	public AsyncClassAdapter(TransformationContext context, ClassVisitor cv,
			Map<String, MethodContext> methods) {
		super(cv);
		this.context = context;
		this.methods = methods;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);

		this.name = name;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		final MethodContext info = methods.get(MethodContext.keyOf(name, desc));
		if (info == null) {
			return cv.visitMethod(access, name, desc, signature, exceptions);
		}

		// method will create computation and return it
		new TramplineMethodGenerator(info).accept(cv);

		// implementation of Computation<>
		new ComputationClassGenerator(info).accept(context);

		// implementation of Continuation<>
		new ContinuationClassGenerator(info).accept(context);

		// Rename our method and transform it
		MethodVisitor mv = cv.visitMethod(
				access | Opcodes.ACC_FINAL /*| Opcodes.ACC_SYNTHETIC*/,
				name + "$async",
				Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
						info.stateType, Type.INT_TYPE }), null, exceptions);

		return new AsyncMethodAdapter(info, mv) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				// Now we have all information about used locals to generate
				// state class body
				new StateClassGenerator(info).accept(context);
			}
		};
	}

	@Override
	public void visitEnd() {
		// All transformed classes must be inner classes
		for (Map.Entry<String, byte[]> entry : context.getCode().entrySet()) {
			String inner = entry.getKey();
			if (!inner.equals(name)) {
				String simpleName = null;
				// No need to check length, inner name could not be the same as
				// name (see check above)
				if (inner.startsWith(name)
						&& inner.charAt(name.length()) == '$') {
					simpleName = inner.substring(name.length() + 1);
				}
				cv.visitInnerClass(inner, name, simpleName, 0);

			}
		}
		super.visitEnd();
	}
}
