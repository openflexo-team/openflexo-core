/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

public interface W3URIDefinitions {

	public static final String W3_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	public static final String W3_URI = W3_NAMESPACE;
	// TODO check if correct, namespace doesn't have same format restrictions as URI

	public static final String W3_ANYTYPE_URI = W3_URI + "#anyType";

	public static final String W3_STRING_DATATYPE_URI = W3_URI + "#string";

	public static final String W3_INTEGER_DATATYPE_URI = W3_URI + "#integer";

	public static final String W3_INT_DATATYPE_URI = W3_URI + "#int";

	public static final String W3_SHORT_DATATYPE_URI = W3_URI + "#short";

	public static final String W3_LONG_DATATYPE_URI = W3_URI + "#long";

	public static final String W3_BYTE_DATATYPE_URI = W3_URI + "#byte";

	public static final String W3_FLOAT_DATATYPE_URI = W3_URI + "#float";

	public static final String W3_DOUBLE_DATATYPE_URI = W3_URI + "#double";

	public static final String W3_BOOLEAN_DATATYPE_URI = W3_URI + "#boolean";

}
