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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.code.jconts.instrument.context.MethodContext;

/**
 * This class replaces original asynchronous method with one that will create
 * instance of <code>com.google.code.jconts.Computation</code> and return it.
 */
public class TramplineMethodGenerator {

	private final MethodContext info;

	public TramplineMethodGenerator(MethodContext info) {
		this.info = info;
	}

	public void accept(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(info.access, info.name, info.desc,
				info.signature, info.exceptions);
		mv.visitCode();
		mv.visitTypeInsn(Opcodes.NEW, info.computationClassName);
		mv.visitInsn(Opcodes.DUP);

		// "this' for new Computation(this, state)
		if (!info.isStatic()) {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
		}

		// new State()
		mv.visitTypeInsn(Opcodes.NEW, info.stateClassName);
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, info.stateClassName,
				CTOR_NAME, DEFAULT_CTOR_DESC);

		// state.varX = argX
		String[] names = info.entryLocalsVars;
		for (int i = 0; i < info.entryLocals.length; ++i) {
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(info.entryLocals[i].getOpcode(Opcodes.ILOAD), i
					+ info.thisOffset);
			mv.visitFieldInsn(Opcodes.PUTFIELD, info.stateClassName, names[i],
					info.entryLocals[i].getDescriptor());
		}

		// new Computation(this, state);
		String ctorDesc;
		if (info.isStatic()) {
			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE,
					new Type[] { info.stateType });
		} else {
			ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
					Type.getObjectType(info.owner), info.stateType });
		}
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, info.computationClassName,
				CTOR_NAME, ctorDesc);

		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(info.isStatic() ? 5 : 6,
				info.isStatic() ? info.entryLocals.length
						: info.entryLocals.length + 1);
		mv.visitEnd();
	}
}
