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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

public final class CodeVisitors {

	public static ClassVisitor tracer(ClassVisitor writer) {
		final StringWriter output = new StringWriter();
		return new TraceClassVisitor(writer, new PrintWriter(output)) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				System.err.println("----------------------------------------");
				System.err.println("Class code: ");
				System.err.println(output.toString());
			}
		};
	}

	public static ClassVisitor tracer() {
		return tracer(new EmptyVisitor());
	}

	private CodeVisitors() {
		// No instances.
	}
}
