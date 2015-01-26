/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.ontology;

public enum BuiltInDataType implements W3URIDefinitions {
	// TODO: see http://www.w3.org/TR/xmlschema-2/ to complete list

	String {
		@Override
		public Class<?> getAccessedType() {
			return String.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_STRING_DATATYPE_URI;
		}

	},
	Integer {
		@Override
		public Class<?> getAccessedType() {
			return Integer.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_INTEGER_DATATYPE_URI;
		}

	},
	Int {
		@Override
		public Class<?> getAccessedType() {
			return Integer.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_INT_DATATYPE_URI;
		}

	},
	Short {
		@Override
		public Class<?> getAccessedType() {
			return Short.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_SHORT_DATATYPE_URI;
		}

	},
	Long {
		@Override
		public Class<?> getAccessedType() {
			return Long.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_LONG_DATATYPE_URI;
		}

	},
	Byte {
		@Override
		public Class<?> getAccessedType() {
			return Byte.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_BYTE_DATATYPE_URI;
		}

	},
	Float {
		@Override
		public Class<?> getAccessedType() {
			return Float.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_FLOAT_DATATYPE_URI;
		}

	},
	Double {
		@Override
		public Class<?> getAccessedType() {
			return Double.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_DOUBLE_DATATYPE_URI;
		}

	},
	Boolean {
		@Override
		public Class<?> getAccessedType() {
			return Boolean.class;
		}

		@Override
		public java.lang.String getURI() {
			return W3_BOOLEAN_DATATYPE_URI;
		}

	};

	public abstract Class<?> getAccessedType();

	public abstract String getURI();

	public static BuiltInDataType fromURI(String uri) {
		for (BuiltInDataType dt : values()) {
			if (dt.getURI().equals(uri)) {
				return dt;
			}
		}
		return null;
	}

	public static BuiltInDataType fromType(Class<?> aClass) {
		for (BuiltInDataType dt : values()) {
			if (dt.getAccessedType().equals(aClass)) {
				return dt;
			}
		}
		return null;
	}
}
