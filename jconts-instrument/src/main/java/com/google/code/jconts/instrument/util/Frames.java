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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class Frames {

	public static Object toFrameType(Type type) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.SHORT:
		case Type.INT:
			return Opcodes.INTEGER;
		case Type.FLOAT:
			return Opcodes.FLOAT;
		case Type.LONG:
			return Opcodes.LONG;
		case Type.DOUBLE:
			return Opcodes.DOUBLE;
		case Type.ARRAY:
			return type.getDescriptor();
			// case Type.OBJECT:
		default:
			return type.getInternalName();
		}
	}
	
	public static Type fromFrameType(final Object type) {
		if (type instanceof String) {
			return Type.getObjectType((String) type);
		} else if (type == Opcodes.INTEGER) {
			return Type.INT_TYPE;
		} else if (type == Opcodes.FLOAT) {
			return Type.FLOAT_TYPE;
		} else if (type == Opcodes.LONG) {
			return Type.LONG_TYPE;
		} else if (type == Opcodes.DOUBLE) {
			return Type.DOUBLE_TYPE;
		}
		throw new IllegalArgumentException("Unexpected local type: " + type);
	}

	private Frames() {
		// No instances.
	}
}
