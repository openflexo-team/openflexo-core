/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ObjectNode;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * @author sylvain
 * 
 */
public class DataBindingNode extends ObjectNode<Node, DataBinding<?>, FMLCompilationUnitSemanticsAnalyzer> {

	private Bindable bindable;
	private BindingDefinitionType bindingDefinitionType;
	private Type expectedType;

	public DataBindingNode(Node astNode, Bindable bindable, BindingDefinitionType bindingDefinitionType, Type expectedType,
			FMLSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
		this.bindable = bindable;
		this.bindingDefinitionType = bindingDefinitionType;
		this.expectedType = expectedType;
		// buildModelObjectFromAST() was already called, but too early (bindable not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public DataBindingNode(DataBinding<?> dataBinding, FMLSemanticsAnalyzer analyzer) {
		super(dataBinding, analyzer);
	}

	@Override
	public DataBinding<?> buildModelObjectFromAST(Node astNode) {
		if (bindable != null && expectedType != null && bindingDefinitionType != null) {
			return new DataBinding(bindable, expectedType, bindingDefinitionType);
		}
		return null;
	}

	@Override
	public DataBindingNode deserialize() {
		return this;
	}

	@Override
	public void finalizeDeserialization() {
		if (getModelObject() != null) {
			if (!getModelObject().isValid()) {
				// No reason to throw from now, this will be detected later during model validation
				// throwIssue(getModelObject().invalidBindingReason(), getFragment());
			}
		}
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
	}

}
