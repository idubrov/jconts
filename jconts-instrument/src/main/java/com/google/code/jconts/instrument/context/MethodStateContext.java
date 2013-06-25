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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;

/**
 * The purpose of this class is to track which variables are required for
 * storing the state of the asynchronous method when await is invoked.
 * <p>
 * Currently, each invocation of <code>await</code> uses separate set of fields.
 */
public class MethodStateContext {
	private int counter;

	private List<String> fieldNames = new ArrayList<String>();
	private List<Type> fieldTypes = new ArrayList<Type>();

	public String[] stateFields(Type... types) {
		String[] result = new String[types.length];
		
		// Try to reuse existing field.
		Set<Integer> used = new HashSet<Integer>();
		outer: for (int i = 0; i < types.length; ++i) {
			for (int field = 0; field < fieldNames.size(); ++field) {
				if (types[i].equals(fieldTypes.get(field)) && used.add(field)) {
					result[i] = fieldNames.get(field);
					continue outer;
				}
			}
			
			// Not found, add new one
			String field = "var" + counter++;
			result[i] = field;
			fieldNames.add(field);
			fieldTypes.add(types[i]);
		}
		return result;
	}
	
	public List<String> getFieldNames() {
		return fieldNames;
	}

	public List<Type> getFieldTypes() {
		return fieldTypes;
	}
}
