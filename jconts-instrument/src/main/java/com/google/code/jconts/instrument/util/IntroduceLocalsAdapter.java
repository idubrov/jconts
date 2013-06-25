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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Introduces local variables of given types at the beginning of the frame
 * (after "this" local for instance methods).
 * <p>
 * Since adding at arbitrary place requires extra bookkeeping, this class
 * supports introducing variables at the beginning only.
 */
public class IntroduceLocalsAdapter extends MethodAdapter {
	private final int shift;
	private final Object[] extraLocals;
	private final boolean isStatic;

	public IntroduceLocalsAdapter(MethodVisitor mv, boolean isStatic,
			Type... locals) {
		super(mv);

		this.isStatic = isStatic;

		// double & long could take two.
		int shift = 0;
		for (Type t : locals) {
			shift += t.getSize();
		}
		this.shift = shift;
		this.extraLocals = new Object[locals.length];
		for (int i = 0; i < locals.length; ++i) {
			extraLocals[i] = Frames.toFrameType(locals[i]);
		}
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// Shift all indexes after "this"
		if (isStatic || var > 0) {
			var += shift;
		}
		super.visitVarInsn(opcode, var);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		// Shift all indexes after "this"
		if (isStatic || var > 0) {
			var += shift;
		}
		super.visitIincInsn(var, increment);
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {
		if (type == Opcodes.F_FULL) {
			// FIXME:.... utils...
			Object[] newLocal = new Object[local.length + extraLocals.length];

			int off = 0;
			if (!isStatic) {
				newLocal[0] = local[0];
				off = 1;
			}
			System.arraycopy(extraLocals, 0, newLocal, off, extraLocals.length);
			System.arraycopy(local, off, newLocal, extraLocals.length + off,
					local.length - off);

			super.visitFrame(Opcodes.F_FULL, newLocal.length, newLocal, nStack,
					stack);
		} else {
			super.visitFrame(type, nLocal, local, nStack, stack);
		}
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		// Shift all indexes after "this"
		if (isStatic || index > 0) {
			index += shift;
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals + extraLocals.length);
	}
}
