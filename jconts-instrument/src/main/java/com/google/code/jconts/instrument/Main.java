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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;

public class Main {

	/**
	 * Packages we always ignore during the instrumentation.
	 */
	private static final String[] PREFIXES = { "java/", "javax/", "sun/" };
	private static Method DEFINE_CLASS_METHOD;

	/**
	 * Stand alone transformation entry point.
	 */
	public static void main(String[] args) throws Exception {
		for (String clazz : args) {
			System.out.print("Transforming file: ");
			System.out.println(clazz);

			byte[] bytecode = readFile(clazz);

			Map<String, byte[]> classes = Transformer.transformClass(bytecode);
			for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
				String file = entry.getKey() + ".class";
				
				System.out.print("Writing file: ");
				System.out.println(file);
				writeFile(file, entry.getValue());
			}
		}
	}

	/**
	 * Intsrumentation agent entry point.
	 */
	public static void premain(String agentArgs, Instrumentation inst)
			throws Exception {

		DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod(
				"defineClass", String.class, byte[].class, Integer.TYPE,
				Integer.TYPE);
		DEFINE_CLASS_METHOD.setAccessible(true);

		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className,
					Class<?> classBeingRedefined,
					ProtectionDomain protectionDomain, byte[] classfileBuffer)
					throws IllegalClassFormatException {

				if (ignore(className)) {
					return null;
				}

				final Map<String, byte[]> classes;
				try {
					classes = Transformer.transformClass(classfileBuffer);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				// No transformation check
				if (classes == null) {
					return null;
				}

				byte[] primary = classes.remove(className);
				try {
					declareExtra(loader, classes);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				return primary;
			}
		});
	}

	private static void declareExtra(ClassLoader loader,
			Map<String, byte[]> classes) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		// Explicitly declare extra classes
		for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
			DEFINE_CLASS_METHOD.invoke(loader,
					entry.getKey().replace('/', '.'), entry.getValue(), 0,
					entry.getValue().length);
		}
	}

	private static boolean ignore(String className) {
		for (String p : PREFIXES) {
			if (className.startsWith(p)) {
				return true;
			}
		}
		return false;
	}

	private static byte[] readFile(String name) throws IOException {
		FileInputStream in = new FileInputStream(name);
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream(
					in.available());

			byte[] buf = new byte[1024];
			int count;
			while ((count = in.read(buf)) != -1) {
				bout.write(buf, 0, count);
			}
			return bout.toByteArray();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// Ignore.
			}
		}
	}

	private static void writeFile(String name, byte[] data) throws IOException {
		FileOutputStream out = new FileOutputStream(name);
		try {
			out.write(data);
		} finally {
			out.close();
		}
	}
}
