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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.code.jconts.instrument.context.MethodContext;
import com.google.code.jconts.instrument.util.Frames;
import com.google.code.jconts.instrument.util.IntroduceLocalsAdapter;

/**
 * This adapter enables transformation of the methods marked by IsAsync
 * annotations.
 * <p>
 * FIXME: invoke introduce local adapter before this adapter...
 */
public class AsyncMethodAdapter extends MethodAdapter {

	private final MethodContext info;

	private final Label dispatchLabel = new Label();
	private final Label tryLabel = new Label();
	private final Label catchLabel = new Label();

	private final List<Label> dispatchTable = new ArrayList<Label>();
	private final List<Type> locals = new ArrayList<Type>();
	private boolean prologGenerated = false;

	/**
	 * Reference to the destination method writer. It is used when we need to
	 * use local variables, added by <code>IntroduceLocalAdapter</code>, because
	 * it renumbers all variable access.
	 */
	private final MethodVisitor target;

	public AsyncMethodAdapter(MethodContext info, MethodVisitor mv) {
		super(new IntroduceLocalsAdapter(mv, info.isStatic(), info.stateType,
				Type.INT_TYPE));

		this.info = info;
		this.target = mv;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		checkProlog();
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		checkProlog();
		super.visitIincInsn(var, increment);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		checkProlog();
		super.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		checkProlog();
		super.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		checkProlog();
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		checkProlog();
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		checkProlog();
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {
		checkProlog();
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		checkProlog();
		super.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		checkProlog();
		super.visitVarInsn(opcode, var);
	}

	private void checkProlog() {
		// Generate only once (at the beginning)
		if (prologGenerated) {
			return;
		}
		prologGenerated = true;

		// try...catch around the whole body. Should be added last (after all
		// other try..catch blocks).
		// Actually, we are making an assumption that reader will emit
		// visitTryCatchBlock BEFORE generating any bytecode.
		// We cannot call visitTryCatchBlock at the end since it should
		// be called before all its labels are visited (which we generate
		// in this block).
		mv.visitTryCatchBlock(tryLabel, dispatchLabel, catchLabel, null);

		// Goto to table switch dispatcher
		mv.visitJumpInsn(Opcodes.GOTO, dispatchLabel);

		// Wrap the whole original body with try...catch that will invoke
		// cont.setException(Throwable t)
		mv.visitLabel(tryLabel);

		// Restore the frame as it was before changing method arguments
		Type[] locs = info.entryLocals;
		Object[] frame = new Object[info.isStatic() ? locs.length
				: locs.length + 1];

		// Frame
		if (!info.isStatic()) {
			frame[0] = info.owner;
		}

		int off = info.isStatic() ? 0 : 1;
		for (int i = 0; i < locs.length; ++i) {
			frame[off + i] = Frames.toFrameType(locs[i]);
		}

		mv.visitFrame(Opcodes.F_FULL, frame.length, frame, 0, new Object[0]);
		Label label = new Label();
		dispatchTable.add(label);
		mv.visitLabel(label);

		if (!info.isStatic()) {
			locals.add(Type.getObjectType(info.owner));
		}
		Collections.addAll(locals, locs);

		// Restore the original method arguments
		// argX = state.varX
		String[] names = info.entryLocalsVars;
		for (int i = 0; i < info.entryLocals.length; ++i) {
			// We go directly to target, introduced var used
			target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
			mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName, names[i],
					info.entryLocals[i].getDescriptor());
			mv.visitVarInsn(info.entryLocals[i].getOpcode(Opcodes.ISTORE),
					info.isStatic() ? i : i + 1);

		}
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] locs, int nStack,
			Object[] stack) {
		if (type == Opcodes.F_NEW) {
			throw new IllegalArgumentException(
					"Expanded frames are not supported!");
		}

		mv.visitFrame(type, nLocal, locs, nStack, stack);

		if (type == Opcodes.F_APPEND) {
			for (int i = 0; i < nLocal; ++i) {
				locals.add(Frames.fromFrameType(locs[i]));
			}
		} else if (type == Opcodes.F_CHOP) {
			for (int i = 0; i < nLocal; ++i) {
				locals.remove(locals.size() - 1);
			}
		} else if (type == Opcodes.F_FULL) {
			locals.clear();
			for (int i = 0; i < nLocal; ++i) {
				locals.add(Frames.fromFrameType(locs[i]));
			}
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		checkProlog();

		if (opcode == Opcodes.INVOKESTATIC && ASYNC_NAME.equals(owner)
				&& ARETURN_NAME.equals(name)) {

			if (ARETURN_VOID_DESC.equals(desc)) {
				mv.visitInsn(Opcodes.ACONST_NULL);
			}

			// state variable
			target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
			mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName,
					CONTINUATION_FIELD, CONTINUATION_DESC);
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, CONTINUATION_NAME,
					CONTINUATION_INVOKE_NAME, CONTINUATION_INVOKE_DESC);

			// Will be dropped while replacing ARETURN with RETURN.
			// FIXME: Should verify this value is NOT used.
			mv.visitInsn(Opcodes.ACONST_NULL);
			return;
		}
		if (opcode == Opcodes.INVOKESTATIC && ASYNC_NAME.equals(owner)
				&& AWAIT_NAME.equals(name) && AWAIT_DESC.equals(desc)) {

			// Computation<T> is on stack

			// FIXME: ...
			// if (stack.size() != 1) {
			// throw new IllegalStateException(
			// "Stack preserving is not supported!");
			// }

			int index = dispatchTable.size();

			// Save state
			List<Type> l = new ArrayList<Type>(locals);
			if (!info.isStatic()) {
				l.remove(0);
			}

			// state.varX = locX
			String[] vars = info.tracker.stateFields(l.toArray(new Type[0]));
			for (int i = 0; i < vars.length; ++i) {
				// state variable
				target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
				mv.visitVarInsn(l.get(i).getOpcode(Opcodes.ILOAD), i
						+ info.thisOffset);
				mv.visitFieldInsn(Opcodes.PUTFIELD, info.stateClassName,
						vars[i], l.get(i).getDescriptor());
			}

			// Create instance of continuation
			// new Continuation([this, ]state, index);
			mv.visitTypeInsn(Opcodes.NEW, info.continuationClassName);
			mv.visitInsn(Opcodes.DUP);

			// "this' for new Continuation([this, ]state, index)
			if (!info.isStatic()) {
				mv.visitVarInsn(Opcodes.ALOAD, 0);
			}

			// state and index
			target.visitVarInsn(Opcodes.ALOAD, 0 + info.thisOffset);
			mv.visitIntInsn(Opcodes.BIPUSH, index);

			String ctorDesc;
			if (info.isStatic()) {
				ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
						info.stateType, Type.INT_TYPE });
			} else {
				ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
						Type.getObjectType(info.owner), info.stateType,
						Type.INT_TYPE });
			}
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					info.continuationClassName, CTOR_NAME, ctorDesc);

			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, COMPUTATION_NAME,
					COMPUTATION_EXECUTE_NAME, COMPUTATION_EXECUTE_DESC);
			super.visitInsn(Opcodes.RETURN);

			// Restore state
			// mv.visitFrame(Opcodes.F_SAME, 0, new Object[0], 0, new
			// Object[0]);
			Label label = new Label();

			int invokeIndex = dispatchTable.size();
			dispatchTable.add(label); // for invoke
			dispatchTable.add(label); // for setException
			mv.visitLabel(label);
			for (int i = 0; i < vars.length; ++i) {
				// state variable
				target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
				mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName,
						vars[i], l.get(i).getDescriptor());
				mv.visitVarInsn(l.get(i).getOpcode(Opcodes.ISTORE), i
						+ info.thisOffset);
			}

			// if (index == invokeIndex) goto invokeLabel;
			Label invokeLabel = new Label();
			target.visitVarInsn(Opcodes.ILOAD, 1 + info.thisOffset);
			mv.visitIntInsn(Opcodes.BIPUSH, invokeIndex);
			mv.visitJumpInsn(Opcodes.IF_ICMPEQ, invokeLabel);

			// Throw exception
			target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
			mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName,
					"exception", THROWABLE_DESC);
			mv.visitInsn(Opcodes.ATHROW);

			// Push result value
			// invokeLabel:
			mv.visitLabel(invokeLabel);
			target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
			mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName, "result",
					OBJECT_DESC);
			return;
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitInsn(int opcode) {
		checkProlog();

		if (opcode == Opcodes.ARETURN) {
			super.visitInsn(Opcodes.POP);
			super.visitInsn(Opcodes.RETURN);
		} else {
			super.visitInsn(opcode);
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// Table switch at the end of the method
		mv.visitLabel(dispatchLabel);

		Label dflt = new Label();

		// Load index
		target.visitVarInsn(Opcodes.ILOAD, 1 + info.thisOffset);
		int[] keys = new int[dispatchTable.size()];
		for (int i = 0; i < keys.length; ++i) {
			keys[i] = i;
		}
		mv.visitLookupSwitchInsn(dflt, keys,
				dispatchTable.toArray(new Label[0]));

		// FIXME: ...throw exception
		mv.visitLabel(dflt);
		mv.visitInsn(Opcodes.RETURN);

		// catch block
		mv.visitLabel(catchLabel);

		// invoke Continuation#setException(Throwable t)
		target.visitVarInsn(Opcodes.ALOAD, info.isStatic() ? 0 : 1);
		mv.visitFieldInsn(Opcodes.GETFIELD, info.stateClassName,
				CONTINUATION_FIELD, CONTINUATION_DESC);
		mv.visitInsn(Opcodes.SWAP);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, CONTINUATION_NAME,
				CONTINUATION_SET_EXCEPTION_NAME,
				CONTINUATION_SET_EXCEPTION_DESC);
		mv.visitInsn(Opcodes.RETURN);

		// FIXME: evaluate properly
		super.visitMaxs(maxStack + 4 + info.thisOffset, maxLocals + 2);
	}
}