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

import static com.google.code.jconts.instrument.Constants.*;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import com.google.code.jconts.instrument.context.MethodContext;
import com.google.code.jconts.instrument.util.TransformationContext;

/**
 * Particular case of continuation: computation. Basically, it's interface and
 * behaviour is similar to continuation , but with little differences:
 * <ul>
 * <li>Computation does not have setException method.
 * <li>It's execute method receives the continuation and not the result of
 * operation execution.
 * </ul>
 * It is similar in that it delegates back to the outer class passing as a
 * parameter.
 */
public class ContinuationClassGenerator {

	private final MethodContext info;
	private final String stateDesc;
	private final String signature;

	public ContinuationClassGenerator(MethodContext info) {
		this.info = info;
		this.stateDesc = info.stateType.getDescriptor();

		SignatureWriter sign = new SignatureWriter();

		// Name<T> implements Continuation<T>
		sign.visitFormalTypeParameter("T");
		SignatureVisitor v = sign.visitClassBound();
		v.visitClassType(OBJECT_NAME);
		v.visitEnd();
		v = sign.visitSuperclass();
		v.visitClassType(OBJECT_NAME);
		v.visitEnd();
		v = sign.visitInterface();
		v.visitClassType(CONTINUATION_NAME);
		v.visitTypeArgument('=').visitTypeVariable("T");
		v.visitEnd();
		this.signature = sign.toString();
	}

	public void accept(TransformationContext context) {
		ClassVisitor cv = context.writer();

		cv.visit(Opcodes.V1_6, Opcodes.ACC_FINAL, info.continuationClassName,
				signature, OBJECT_NAME, new String[] { CONTINUATION_NAME });

		cv.visitSource(info.ownerSource, null);
		cv.visitInnerClass(info.stateClassName, info.owner,
				info.stateSimpleName, 0);
		cv.visitInnerClass(info.continuationClassName, info.owner,
				info.continuationSimpleName, 0);

		cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "state",
				stateDesc, null, null);

		cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "index", "I",
				null, null);

		generateConstructor(cv);
		generateExecute(cv, true);
		generateExecute(cv, false);

		cv.visitEnd();
	}

	private void generateConstructor(ClassVisitor cv) {
		final String name = info.continuationClassName;
		final Type outerType = Type.getObjectType(info.owner);

		// Constructor have form either <init>(OuterClass this$0, State state,
		// int index)
		// or <init>(State state, int index)
		String ctorDesc;
		if (info.isStatic()) {
			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
					info.stateType, Type.INT_TYPE });
		} else {
			cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "this$0",
					'L' + info.owner + ';', null, null);

			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
					outerType, info.stateType, Type.INT_TYPE });
		}

		// Generate constructor
		MethodVisitor mv = cv.visitMethod(0, CTOR_NAME, ctorDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		Label end = new Label();
		mv.visitLabel(start);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_NAME, CTOR_NAME,
				DEFAULT_CTOR_DESC);

		// Save state field
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1 + info.thisOffset);
		mv.visitFieldInsn(Opcodes.PUTFIELD, name, "state", stateDesc);

		// Save index field
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 2 + info.thisOffset);
		mv.visitFieldInsn(Opcodes.PUTFIELD, name, "index", "I");

		// Save outer this
		if (!info.isStatic()) {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, name, "this$0",
					outerType.getDescriptor());
		}
		mv.visitInsn(Opcodes.RETURN);
		mv.visitLabel(end);

		mv.visitLocalVariable("this", 'L' + name + ';', signature, start, end,
				0);
		if (!info.isStatic()) {
			mv.visitLocalVariable("this$0", outerType.getDescriptor(), null,
					start, end, 1);
		}
		mv.visitLocalVariable("state", stateDesc, null, start, end,
				1 + info.thisOffset);
		mv.visitLocalVariable("index", "I", null, start, end,
				2 + info.thisOffset);

		mv.visitMaxs(2, 3 + info.thisOffset);
		mv.visitEnd();
	}

	private void generateExecute(ClassVisitor cv, boolean execute) {
		final String name = info.continuationClassName;
		final Type outerType = Type.getObjectType(info.owner);

		// Generate invoke(T result);
		String signature = null;
		if (execute) {
			SignatureWriter sign = new SignatureWriter();
			sign.visitParameterType().visitTypeVariable("T");
			sign.visitReturnType().visitBaseType('V'); // void
			signature = sign.toString();
		}
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_FINAL
				| Opcodes.ACC_PUBLIC, execute ? CONTINUATION_INVOKE_NAME
				: CONTINUATION_SET_EXCEPTION_NAME,
				execute ? CONTINUATION_INVOKE_DESC
						: CONTINUATION_SET_EXCEPTION_DESC, signature, null);
		mv.visitCode();
		Label start = new Label();
		Label end = new Label();
		mv.visitLabel(start);

		// Load outer this
		if (!info.isStatic()) {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, name, "this$0",
					outerType.getDescriptor());
		}

		// state.result = result or state.exception = throwable
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, name, "state", stateDesc);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitFieldInsn(Opcodes.PUTFIELD, info.stateClassName,
				execute ? "result" : "exception", execute ? OBJECT_DESC
						: THROWABLE_DESC);

		// Load state field
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, name, "state", stateDesc);

		// Continue from this index or index+1 (for exception)
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, name, "index", "I");
		if (!execute) {
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitInsn(Opcodes.IADD);
		}

		mv.visitMethodInsn(info.isStatic() ? Opcodes.INVOKESTATIC
				: Opcodes.INVOKEVIRTUAL, info.owner, info.name + "$async", Type
				.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
						info.stateType, Type.INT_TYPE }));

		mv.visitInsn(Opcodes.RETURN);
		mv.visitLabel(end);

		mv.visitLocalVariable("this", 'L' + name + ';', signature, start, end,
				0);
		if (!info.isStatic()) {
			mv.visitLocalVariable("this$0", outerType.getDescriptor(), null,
					start, end, 1);
		}
		mv.visitLocalVariable("result", OBJECT_DESC, "TT;", start, end,
				1 + info.thisOffset);

		mv.visitMaxs(3 + info.thisOffset + (execute ? 0 : 1),
				2 + info.thisOffset);
		mv.visitEnd();
	}

}
