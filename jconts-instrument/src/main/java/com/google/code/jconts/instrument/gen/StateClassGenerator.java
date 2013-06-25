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

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.code.jconts.instrument.context.MethodContext;
import com.google.code.jconts.instrument.util.TransformationContext;

/**
 * 
 * 
 */
public class StateClassGenerator {

	private final MethodContext info;

	public StateClassGenerator(MethodContext info) {
		this.info = info;
	}

	public void accept(TransformationContext context) {
		ClassVisitor cv = context.writer();

		final String name = info.stateClassName;

		cv.visit(Opcodes.V1_6, Opcodes.ACC_FINAL /*| Opcodes.ACC_SYNTHETIC*/, name,
				null, OBJECT_NAME, null);

		cv.visitSource(info.ownerSource, null);
		cv.visitOuterClass(info.owner, null, null);

		cv.visitField(0/*Opcodes.ACC_SYNTHETIC*/, CONTINUATION_FIELD,
				CONTINUATION_DESC, 'L' + CONTINUATION_NAME + '<'
						+ info.valueSignature + ">;", null);
		
		// Local variables state
		List<String> names = info.tracker.getFieldNames();
		List<Type> types = info.tracker.getFieldTypes();
		for (int i = 0; i < names.size(); ++i) {
			cv.visitField(0/*Opcodes.ACC_SYNTHETIC*/, names.get(i), types.get(i)
					.getDescriptor(), null, null);
		}

		// Return value variable
		cv.visitField(0/*Opcodes.ACC_SYNTHETIC*/, "result", OBJECT_DESC, null, null);
		cv.visitField(0/*Opcodes.ACC_SYNTHETIC*/, "exception", THROWABLE_DESC, null, null);

		// Generate constructor
		MethodVisitor mv = cv.visitMethod(0, CTOR_NAME, DEFAULT_CTOR_DESC,
				null, null);
		mv.visitCode();
		Label start = new Label();
		Label end = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(0, start);

		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_NAME, CTOR_NAME,
				DEFAULT_CTOR_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitLabel(end);

		mv.visitLocalVariable("this", 'L' + name + ';', null, start, end, 0);

		mv.visitMaxs(1, 1);
		mv.visitEnd();

		cv.visitEnd();
	}
}
