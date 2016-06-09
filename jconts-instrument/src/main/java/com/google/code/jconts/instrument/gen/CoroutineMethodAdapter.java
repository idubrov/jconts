/**
 * Copyright (C) 2011 Ivan Dubrov
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.jconts.instrument.gen;

import com.google.code.jconts.instrument.Constants;
import com.google.code.jconts.instrument.context.MethodContext;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static com.google.code.jconts.instrument.Constants.*;

public class CoroutineMethodAdapter extends MethodAdapter {

  public CoroutineMethodAdapter(MethodContext info, MethodVisitor mv) {
    super(mv);
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    super.visitMethodInsn(opcode, owner, name, desc);

    if (opcode == Opcodes.INVOKESPECIAL) {
      return;
    }

    if (Type.getReturnType(desc) == Type.VOID_TYPE) {
      super.visitMethodInsn(Opcodes.INVOKESTATIC, COROUTINES_NAME, COROUTINES_YIELD_NAME, COROUTINES_YIELD_DESC);
      super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ASYNC_NAME, AWAIT_NAME, AWAIT_DESC);
      super.visitInsn(Opcodes.POP);
    }
  }

  @Override
  public void visitMaxs(int maxStack, int maxLocals) {
    super.visitMaxs(maxStack + 1, maxLocals);
  }
}