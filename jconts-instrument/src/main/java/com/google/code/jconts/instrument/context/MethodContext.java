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
package com.google.code.jconts.instrument.context;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import com.google.code.jconts.instrument.util.SignatureAdapter;

/**
 * This class contains different information about the asynchronous method being
 * processed.
 */
public final class MethodContext {
	/** Class method belongs to */
	public final String owner;
	/** Source file of the class method belongs toF */
	public final String ownerSource;
	/** Name of the method */
	public final String name;
	/** Method descriptor */
	public final String desc;
	/** Method access flags */
	public final int access;
	/** Method signature */
	public final String signature;
	/** Method exceptions */
	public final String[] exceptions;
	/**
	 * 0 for static method, 1 for instance method. Used to simplify
	 * calculations.
	 */
	public final int thisOffset;

	/**
	 * Signature of the asynchronous method return value (type argument of the
	 * <code>Computation&lt;T&gt;</code>.
	 */
	public final String valueSignature;
	/**
	 * Types of the locals at the entrance of the method (excluding
	 * <code>this</code>)
	 */
	public final Type[] entryLocals;
	/** Name of the fields of the state class for saving the entry locals */
	public final String[] entryLocalsVars;

	/** Object to track which field do we need to generate in state class */
	public final MethodStateContext tracker = new MethodStateContext();

	/** Internal name of the state class */
	public final String stateClassName;
	public final String stateSimpleName;
	/** Type of the state class */
	public final Type stateType;

	/** Name of the Computation&lt;T&gt; implementor */
	public final String computationClassName;
	public final String computationSimpleName;
	/** Name of the Continuation&lt;T&gt; implementor */
	public final String continuationClassName;
	public final String continuationSimpleName;

	public MethodContext(String owner, String ownerSource, int access,
			String name, String desc, String signature, String[] exceptions) {
		this.owner = owner;
		this.ownerSource = ownerSource;
		this.access = access;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.exceptions = exceptions;

		this.thisOffset = isStatic() ? 0 : 1;

		this.valueSignature = valueSignature();
		this.entryLocals = Type.getArgumentTypes(desc);
		this.entryLocalsVars = tracker.stateFields(entryLocals);

		this.stateSimpleName = name + "_State";
		this.stateClassName = owner + '$' + stateSimpleName;		
		this.stateType = Type.getObjectType(stateClassName);

		this.computationSimpleName = name + "_Computation";
		this.computationClassName = owner + '$' + computationSimpleName;
		
		this.continuationSimpleName = name + "_Continuation";
		this.continuationClassName = owner + '$' + continuationSimpleName;
	}

	/**
	 * If we are transforming a static method.
	 * 
	 * @return
	 */
	public boolean isStatic() {
		return (access & Opcodes.ACC_STATIC) != 0;
	}

	public static String keyOf(String name, String desc) {
		return name + '#' + desc;
	}

	/**
	 * This method assumes that return type of the method is
	 * <code>com.google.code.jconts.Computation&lt;T&gt;</code>. It extracts the type
	 * argument <code>T</code> and returns it signature.
	 */
	private String valueSignature() {
		final SignatureWriter sign = new SignatureWriter();
		SignatureAdapter adaptor = new SignatureAdapter() {
			public SignatureVisitor visitReturnType() {
				return new SignatureAdapter() {
					@Override
					public SignatureVisitor visitTypeArgument(char wildcard) {
						return sign;
					}

					// FIXME: All other are error!
				};
			};
		};
		new SignatureReader(signature).accept(adaptor);
		return sign.toString();
	}

	@Override
	public String toString() {
		return name + desc;
	}
}
