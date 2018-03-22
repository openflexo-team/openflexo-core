/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.ir;

import org.openflexo.foundation.fml.parser.node.AIdentifierDotIdentifier;
import org.openflexo.foundation.fml.parser.node.ATailDotIdentifier;
import org.openflexo.foundation.fml.parser.node.PDotIdentifier;

/**
 * This class implements some utils to convert node to String<br>
 * 
 * @author sylvain
 * 
 */
public class TextAnalyzingUtils {

	static String asText(PDotIdentifier dotIdentifier) {
		PDotIdentifier current = dotIdentifier;
		String returned = null;
		while (current != null) {
			String identifier = null;
			if (current instanceof AIdentifierDotIdentifier) {
				identifier = ((AIdentifierDotIdentifier) current).getAllowedIdentifierInBinding().toString().trim();
				current = null;
			}
			else if (current instanceof ATailDotIdentifier) {
				identifier = ((ATailDotIdentifier) current).getAllowedIdentifierInBinding().toString().trim();
				current = ((ATailDotIdentifier) current).getDotIdentifier();
			}
			returned = (returned != null ? returned + "." : "") + identifier;
		}
		return returned;
	}

}
