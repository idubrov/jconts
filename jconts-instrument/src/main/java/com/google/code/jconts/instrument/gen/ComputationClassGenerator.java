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
import org.objectweb.asm.signature.SignatureReader;
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
public class ComputationClassGenerator {

	private final MethodContext info;
	private final String stateDesc;
	private final String signature;

	public ComputationClassGenerator(MethodContext info) {
		this.info = info;
		this.stateDesc = info.stateType.getDescriptor();
		this.signature = null;
	}

	public void accept(TransformationContext context) {
		ClassVisitor cv = context.writer();

		// extends Object implements Computation<ValueType>
		SignatureWriter sign = new SignatureWriter();
		SignatureVisitor supsign = sign.visitSuperclass();
		supsign.visitClassType(OBJECT_NAME);
		supsign.visitEnd();
		SignatureVisitor iface = sign.visitInterface();
		iface.visitClassType(COMPUTATION_NAME);
		SignatureVisitor argsign = iface.visitTypeArgument('=');
		new SignatureReader(info.valueSignature).acceptType(argsign);
		argsign.visitEnd();

		cv.visit(Opcodes.V1_6, Opcodes.ACC_FINAL, info.computationClassName,
				sign.toString(), OBJECT_NAME, new String[] { COMPUTATION_NAME });

		cv.visitSource(info.ownerSource, null);
		cv.visitInnerClass(info.stateClassName, info.owner,
				info.stateSimpleName, 0);
		cv.visitInnerClass(info.computationClassName, info.owner,
				info.computationSimpleName, 0);

		cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "state",
				stateDesc, null, null);

		generateConstructor(cv);
		generateExecute(cv);

		cv.visitEnd();
	}

	private void generateConstructor(ClassVisitor cv) {
		final String name = info.computationClassName;
		final Type outerType = Type.getObjectType(info.owner);

		// Constructor have form either <init>(OuterClass this$0, State state)
		// or <init>(State state)
		String ctorDesc;
		if (info.isStatic()) {
			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE,
					new Type[] { info.stateType });
		} else {
			cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "this$0",
					'L' + info.owner + ';', null, null);

			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
					outerType, info.stateType });
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

		mv.visitMaxs(2, 2 + info.thisOffset);
		mv.visitEnd();
	}

	private void generateExecute(ClassVisitor cv) {
		final String name = info.computationClassName;
		final Type outerType = Type.getObjectType(info.owner);

		// Generate execute(Continuation<T> cont);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_FINAL
				| Opcodes.ACC_PUBLIC, COMPUTATION_EXECUTE_NAME,
				COMPUTATION_EXECUTE_DESC, executeSignature(), null);
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

		// Load state field
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, name, "state", stateDesc);

		// state.continuation = continuation
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, name, "state", stateDesc);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitFieldInsn(Opcodes.PUTFIELD, info.stateClassName,
				CONTINUATION_FIELD, CONTINUATION_DESC);

		// Initial state (0)
		mv.visitIntInsn(Opcodes.BIPUSH, 0);
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

		SignatureWriter sign = new SignatureWriter();
		contSignature(sign);
		mv.visitLocalVariable("continuation", CONTINUATION_DESC,
				sign.toString(), start, end, 1 + info.thisOffset);

		mv.visitMaxs(3 + info.thisOffset, 2 + info.thisOffset);
		mv.visitEnd();
	}

	private void contSignature(SignatureVisitor sign) {
		sign.visitClassType(CONTINUATION_NAME);
		SignatureVisitor argsv = sign.visitTypeArgument('-');
		new SignatureReader(info.valueSignature).acceptType(argsv);
		sign.visitEnd();

	}

	private String executeSignature() {
		SignatureWriter sw = new SignatureWriter();
		contSignature(sw.visitParameterType());
		sw.visitReturnType().visitBaseType('V');
		return sw.toString();
	}
}
