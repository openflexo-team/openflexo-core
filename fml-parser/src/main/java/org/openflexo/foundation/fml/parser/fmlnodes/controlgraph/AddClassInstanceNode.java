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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AJavaInstanceCreationFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *    {java_instance_creation} new_containment_clause? kw_new composite_ident type_arguments_or_diamond? l_par argument_list? r_par
 * </pre>
 * 
 * Handle {@link AJavaInstanceCreationFmlActionExp}
 * 
 * @author sylvain
 * 
 */
public class AddClassInstanceNode extends AssignableActionNode<AJavaInstanceCreationFmlActionExp, AddClassInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddClassInstanceNode.class.getPackage().getName());

	public AddClassInstanceNode(AJavaInstanceCreationFmlActionExp astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public AddClassInstanceNode(AddClassInstance action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	/*protected FMLControlGraph getSimpleControlGraph(PAssignmentExpression expression) {
		if (expression ins)
	}*/

	// private FlexoConceptInstanceType conceptType;
	// private String creationSchemeName;

	/*@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		//for (DataBinding arg : args) {
		//	getModelObject().addToParameters(arg);
		//}
	}*/

	private void handleArguments(PArgumentList argumentList, AddClassInstance modelObject) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleArguments(l.getArgumentList(), modelObject);
			handleArgument(l.getExpression(), modelObject);
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleArgument(((AOneArgumentList) argumentList).getExpression(), modelObject);
		}
	}

	private List<DataBinding<?>> args;

	private void handleArgument(PExpression expression, AddClassInstance modelObject) {
		DataBinding<?> argValue = ExpressionFactory.makeExpression(expression, getAnalyser(), modelObject);

		if (args == null) {
			args = new ArrayList<>();
		}

		args.add(argValue);

		/*ControlGraphNode<?, ?> assignableActionNode = ControlGraphFactory.makeControlGraphNode(astNode.getRight(), getAnalyser());
		if (assignableActionNode != null) {
			if (assignableActionNode.getModelObject() instanceof AssignableAction) {
				returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
				addToChildren(assignableActionNode);
			}
			else {
				System.err.println("Unexpected " + assignableActionNode.getModelObject());
				Thread.dumpStack();
			}
		}*/

	}

	@Override
	public AddClassInstance buildModelObjectFromAST(AJavaInstanceCreationFmlActionExp astNode) {
		AddClassInstance returned = getFactory().newAddClassInstance();

		Type type = getTypeFactory().makeType(astNode.getCompositeIdent(), astNode.getTypeArgumentsOrDiamond());
		returned.setType(type);

		handleArguments(astNode.getArgumentList(), returned);
		for (DataBinding arg : args) {
			returned.addToParameters(arg);
		}

		return returned;

	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		when(() -> isContainerFullQualified())
		.thenAppend(dynamicContents(() -> getModelObject().getContainer().toString()), getContainerFragment())
		.thenAppend(staticContents("."), getContainerDotFragment());
		append(staticContents("", "new", SPACE), getNewFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
		append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> serializeArguments(getModelObject().getParameters())), getArgumentsFragment());
		append(staticContents(")"), getRParFragment());
		// @formatter:on	

	}

	private String serializeArguments(List<DataBinding<?>> arguments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			sb.append((i > 0 ? "," : "") + arguments.get(i).toString());
		}
		return sb.toString();
	}

	private boolean isContainerFullQualified() {
		if (getModelObject() != null) {
			return getModelObject().getContainer().isSet();
		}
		else {
			return getASTNode().getNewContainmentClause() != null;
		}
	}

	private RawSourceFragment getContainerFragment() {
		if (getASTNode() != null && getASTNode().getNewContainmentClause() != null) {
			return getFragment(getASTNode().getNewContainmentClause());
		}
		return null;
	}

	private RawSourceFragment getContainerDotFragment() {
		if (getASTNode() != null && getASTNode().getNewContainmentClause() != null) {
			return getFragment(((ANewContainmentClause) getASTNode().getNewContainmentClause()).getDot());
		}
		return null;
	}

	private RawSourceFragment getNewFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwNew());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {

		if (getASTNode() != null) {
			if (getASTNode().getTypeArgumentsOrDiamond() != null) {
				return getFragment(getASTNode().getCompositeIdent(), getASTNode().getTypeArgumentsOrDiamond());
			}
			else {
				return getFragment(getASTNode().getCompositeIdent());
			}
		}
		return null;
	}

	private RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}

	private RawSourceFragment getArgumentsFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getArgumentList());
		}
		return null;
	}

	private RawSourceFragment getRParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRPar());
		}
		return null;
	}

}
