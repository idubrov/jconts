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

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Empty implementation of {@link SignatureVisitor}
 */
public class SignatureAdapter implements SignatureVisitor {

	@Override
	public void visitFormalTypeParameter(String name) {
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterface() {
		return this;
	}

	@Override
	public SignatureVisitor visitParameterType() {
		return this;
	}

	@Override
	public SignatureVisitor visitReturnType() {
		return this;
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		return this;
	}

	@Override
	public void visitBaseType(char descriptor) {
	}

	@Override
	public void visitTypeVariable(String name) {
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return this;
	}

	@Override
	public void visitClassType(String name) {
	}

	@Override
	public void visitInnerClassType(String name) {
	}

	@Override
	public void visitTypeArgument() {
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		return this;
	}

	@Override
	public void visitEnd() {
	}
}
