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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFmlInstanceCreationFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AJavaInstanceCreationFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.toolbox.StringUtils;

/**
 * 
 * Handle both {@link AFmlInstanceCreationFmlActionExp} or {@link AJavaInstanceCreationFmlActionExp}
 * 
 * @author sylvain
 * 
 */
public class AddFlexoConceptInstanceNode extends AssignableActionNode<PFmlActionExp, AddFlexoConceptInstance<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddFlexoConceptInstanceNode.class.getPackage().getName());

	public AddFlexoConceptInstanceNode(PFmlActionExp astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public AddFlexoConceptInstanceNode(AddFlexoConceptInstance<?> action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	/*protected FMLControlGraph getSimpleControlGraph(PAssignmentExpression expression) {
		if (expression ins)
	}*/

	private FlexoConceptInstanceType conceptType;
	private String creationSchemeName;

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (conceptType != null && conceptType.isResolved()) {
			FlexoConcept flexoConceptType = conceptType.getFlexoConcept();
			if (flexoConceptType != null) {
				getModelObject().setFlexoConceptType(flexoConceptType);
				if (flexoConceptType.getCreationSchemes().size() == 0) {
					// No constructor: !! problem
					throwIssue("No constructor for concept " + flexoConceptType, getConceptNameFragment());
				}
				else if (flexoConceptType.getCreationSchemes().size() == 1) {
					getModelObject().setCreationScheme(flexoConceptType.getCreationSchemes().get(0));
				}
				else /* flexoConceptType.getCreationSchemes().size() > 1 */ {
					// TODO
					getModelObject().setCreationScheme((CreationScheme) flexoConceptType.getFlexoBehaviour(creationSchemeName));
				}

				if (getModelObject().getCreationScheme() != null) {
					int index = 0;
					for (FlexoBehaviourParameter flexoBehaviourParameter : getModelObject().getCreationScheme().getParameters()) {
						AddFlexoConceptInstanceParameter arg = getModelObject().getParameter(flexoBehaviourParameter);
						if (index < args.size()) {
							arg.setValue((DataBinding) args.get(index));
							index++;
						}
						else {
							throwIssue("Missing argument value for parameter " + flexoBehaviourParameter, getConceptNameFragment());
							break;
						}
					}
				}
			}
			else {
				throwIssue("Unknown concept " + getConceptName(), getConceptNameFragment());
			}

		}
	}

	private void handleArguments(PArgumentList argumentList, AddFlexoConceptInstance<?> modelObject) {
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

	private void handleArgument(PExpression expression, AddFlexoConceptInstance<?> modelObject) {
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
	public AddFlexoConceptInstance<?> buildModelObjectFromAST(PFmlActionExp astNode) {
		AddFlexoConceptInstance<?> returned = getFactory().newAddFlexoConceptInstance();
		// System.out.println(">>>>>> New FCI " + astNode);

		conceptType = getTypeFactory().lookupConceptNamed(getConceptName(), getFragment(getConceptNameNode()));
		if (astNode instanceof AFmlInstanceCreationFmlActionExp) {
			creationSchemeName = ((AFmlInstanceCreationFmlActionExp) astNode).getConstructorName().getText();
		}

		// System.out.println("conceptType:" + conceptType);

		if (getContainmentClause() != null) {
			String container = makeFullQualifiedIdentifier(getContainmentClause().getCompositeIdent());
			returned.setContainer(new DataBinding<>(container));
		}
		else {
			// returned.setContainer(new DataBinding<>(FlexoConceptBindingModel.THIS_PROPERTY));
		}
		returned.setReceiver(new DataBinding<>(FlexoConceptBindingModel.THIS_PROPERTY));

		if (astNode instanceof AFmlInstanceCreationFmlActionExp) {
			handleArguments(((AFmlInstanceCreationFmlActionExp) astNode).getArgumentList(), returned);
		}
		else if (astNode instanceof AJavaInstanceCreationFmlActionExp) {
			handleArguments(((AJavaInstanceCreationFmlActionExp) astNode).getArgumentList(), returned);
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
		append(dynamicContents(() -> serializeType(getModelObject().getFlexoConceptType())), getConceptNameFragment());
		when(() -> isFullQualified())
			.thenAppend(staticContents("::"), getColonColonFragment())
			.thenAppend(dynamicContents(() -> serializeFlexoBehaviour(getModelObject().getCreationScheme())), getConstructorNameFragment());
		append(staticContents("("), getLParFragment());
		append(childrenContents("", "", () -> getModelObject().getParameters(), ",", "", Indentation.DoNotIndent,
				AddFlexoConceptInstanceParameter.class));
		append(staticContents(")"), getRParFragment());
		// Append semi only when required
		when(() -> requiresSemi()).thenAppend(staticContents(";"), getSemiFragment());
		// @formatter:on	
	}

	private boolean isFullQualified() {
		if (getModelObject() != null && getModelObject().getFlexoConceptType() != null) {
			return getModelObject().getFlexoConceptType().getCreationSchemes().size() > 1;
		}
		else {
			return getASTNode() != null && getASTNode() instanceof AFmlInstanceCreationFmlActionExp;
		}
	}

	private boolean isContainerFullQualified() {
		if (getModelObject() != null) {
			return StringUtils.isNotEmpty(getModelObject().getContainer().toString())
					&& !FlexoConceptBindingModel.THIS_PROPERTY.equals(getModelObject().getContainer().toString());
		}
		else {
			return getContainmentClause() != null;
		}
	}

	private ANewContainmentClause getContainmentClause() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return (ANewContainmentClause) ((AFmlInstanceCreationFmlActionExp) getASTNode()).getNewContainmentClause();
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return (ANewContainmentClause) ((AJavaInstanceCreationFmlActionExp) getASTNode()).getNewContainmentClause();
		}
		return null;
	}

	private RawSourceFragment getContainerFragment() {
		if (getContainmentClause() != null) {
			return getFragment(getContainmentClause().getCompositeIdent());
		}
		return null;
	}

	private RawSourceFragment getContainerDotFragment() {
		if (getContainmentClause() != null) {
			return getFragment(getContainmentClause().getDot());
		}
		return null;
	}

	private RawSourceFragment getNewFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getKwNew());
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return getFragment(((AJavaInstanceCreationFmlActionExp) getASTNode()).getKwNew());
		}
		return null;
	}

	private String getConceptName() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return ((AFmlInstanceCreationFmlActionExp) getASTNode()).getConceptName().getText();
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return getText(((AJavaInstanceCreationFmlActionExp) getASTNode()).getCompositeIdent());
		}
		return null;
	}

	private Node getConceptNameNode() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return ((AFmlInstanceCreationFmlActionExp) getASTNode()).getConceptName();
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return ((AJavaInstanceCreationFmlActionExp) getASTNode()).getCompositeIdent();
		}
		return null;
	}

	private RawSourceFragment getConceptNameFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getConceptName());
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return getFragment(((AJavaInstanceCreationFmlActionExp) getASTNode()).getCompositeIdent());
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getColonColon());
		}
		return null;
	}

	private RawSourceFragment getConstructorNameFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getConstructorName());
		}
		return null;
	}

	private RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return getFragment(((AJavaInstanceCreationFmlActionExp) getASTNode()).getLPar());
		}
		return null;
	}

	private RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return getFragment(((AFmlInstanceCreationFmlActionExp) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return getFragment(((AJavaInstanceCreationFmlActionExp) getASTNode()).getRPar());
		}
		return null;
	}

}
