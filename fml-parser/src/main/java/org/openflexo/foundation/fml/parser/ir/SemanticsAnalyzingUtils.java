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

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.parser.node.AManyCardinality;
import org.openflexo.foundation.fml.parser.node.AMaybeCardinality;
import org.openflexo.foundation.fml.parser.node.AOneCardinality;
import org.openflexo.foundation.fml.parser.node.ASomeCardinality;
import org.openflexo.foundation.fml.parser.node.PCardinality;
import org.openflexo.foundation.fml.parser.node.PExpression;

/**
 * This class implements some utils used in semantics analyzing context<br>
 * 
 * @author sylvain
 * 
 */
public class SemanticsAnalyzingUtils {

	static PropertyCardinality makeCardinality(PCardinality cardinality) {
		if (cardinality instanceof AMaybeCardinality) {
			return PropertyCardinality.ZeroOne;
		}
		else if (cardinality instanceof AOneCardinality) {
			return PropertyCardinality.One;
		}
		else if (cardinality instanceof AManyCardinality) {
			return PropertyCardinality.ZeroMany;
		}
		else if (cardinality instanceof ASomeCardinality) {
			return PropertyCardinality.OneMany;
		}
		return null;
	}

	static DataBinding<?> makeDataBinding(PExpression expression, Bindable owner, Type expectedType,
			BindingDefinitionType bindingDefinitionType) {
		return new DataBinding<>("TODO", owner, expectedType, bindingDefinitionType);
	}

}
