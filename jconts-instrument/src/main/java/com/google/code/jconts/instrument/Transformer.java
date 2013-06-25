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
package com.google.code.jconts.instrument;

import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.code.jconts.instrument.context.MethodContext;
import com.google.code.jconts.instrument.gen.AsyncClassAdapter;
import com.google.code.jconts.instrument.util.Config;
import com.google.code.jconts.instrument.util.TransformationContext;

/**
 * CPS transformer main.
 */
public final class Transformer {

	public static Map<String, byte[]> transformClass(byte[] source) {
		ClassReader reader = new ClassReader(source);

		// First, we look for the methods to transform
		Map<String, MethodContext> methods = AsyncMethodsAnalyzer
				.analyze(reader);
		if (methods.isEmpty()) {
			return null;
		}

		// Then, transform
		TransformationContext context = new TransformationContext();

		ClassVisitor writer = context.writer();
		if (Config.isCheckClasses()) {
			writer = new CheckClassAdapter(writer);
		}
		AsyncClassAdapter adapter = new AsyncClassAdapter(context, writer,
				methods);

		reader.accept(adapter, 0);
		return context.getCode();
	}
}
