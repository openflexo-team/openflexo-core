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

package org.openflexo.foundation.fml.parser.fmlnodes;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AExpressionPropertyInnerConceptDecl;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 * 	expression_property_declaration =
 *       {identifier} visibility? type [identifier]:identifier is [base_identifier]:identifier [additional_identifiers]:additional_identifier* semi |
 *       {expression} visibility? type [identifier]:identifier is [expression_value]:expression semi;
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ExpressionPropertyNode extends FlexoPropertyNode<AExpressionPropertyInnerConceptDecl, ExpressionProperty<?>> {

	private static final Logger logger = Logger.getLogger(ExpressionPropertyNode.class.getPackage().getName());

	public ExpressionPropertyNode(AExpressionPropertyInnerConceptDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ExpressionPropertyNode(ExpressionProperty<?> property, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(property, analyzer);
	}

	@Override
	public ExpressionProperty<?> buildModelObjectFromAST(AExpressionPropertyInnerConceptDecl astNode) {
		ExpressionProperty<?> returned = getFactory().newExpressionProperty();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		try {
			returned.setName(astNode.getIdentifier().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getIdentifier().getText());
		}
		returned.setDeclaredType(TypeFactory.makeType(astNode.getType(), getSemanticsAnalyzer().getTypingSpace()));
		// returned.setExpression(makeBinding(astNode.getExpressionValue(), returned));

		DataBinding<Object> expression = ExpressionFactory.makeDataBinding(astNode.getExpressionValue(), returned,
				BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer(), this);
		returned.setExpression(expression);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getName(), SPACE), getNameFragment());
		append(staticContents("", "values", SPACE), getValuesFragment());
		append(dynamicContents(() -> getModelObject().getExpression().toString()), getExpressionFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getValuesFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwValues());
		}
		return null;
	}

	private RawSourceFragment getExpressionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpressionValue());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

}
