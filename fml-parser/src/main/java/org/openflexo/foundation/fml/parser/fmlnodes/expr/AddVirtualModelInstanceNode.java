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
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * A PathElement representing a new VirtualModel instance
 * 
 * Handle both {@link ASimpleNewInstance} or {@link AFullQualifiedNewInstance}
 * 
 * @author sylvain
 * 
 */
public class AddVirtualModelInstanceNode extends AbstractAddFlexoConceptInstanceNode {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddVirtualModelInstanceNode.class.getPackage().getName());

	public AddVirtualModelInstanceNode(ASimpleNewInstance astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent,
			Bindable bindable) {
		super(astNode, analyzer, parent, bindable);
	}

	public AddVirtualModelInstanceNode(AFullQualifiedNewInstance astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent,
			Bindable bindable) {
		super(astNode, analyzer, parent, bindable);
	}

	public AddVirtualModelInstanceNode(CreationSchemePathElement action, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(action, analyzer, bindable);
	}

	/*@Override
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
				else {
					// TODO
					getModelObject().setCreationScheme((CreationScheme) flexoConceptType.getFlexoBehaviour(creationSchemeName));
				}
	
				if (getModelObject().getCreationScheme() != null) {
	
					int requiredArgumentsCount = getModelObject().getCreationScheme().getParameters().size();
	
					int index = 0;
					for (P2PPNode<?, ?> p2ppNode : getChildren()) {
						if (p2ppNode instanceof BehaviourCallArgumentNode) {
							BehaviourCallArgumentNode callArgNode = (BehaviourCallArgumentNode) p2ppNode;
							AddFlexoConceptInstanceParameter argument = (AddFlexoConceptInstanceParameter) ((BehaviourCallArgumentNode) p2ppNode)
									.getModelObject();
							if (index < getModelObject().getCreationScheme().getParameters().size()) {
								FlexoBehaviourParameter parameter = getModelObject().getCreationScheme().getParameters().get(index);
								argument.setParam(parameter);
								if (!TypeUtils.isTypeAssignableFrom(parameter.getType(), argument.getValue().getAnalyzedType(), true)) {
									throwIssue("Invalid type " + argument.getValue().getAnalyzedType() + " (expected: "
											+ parameter.getType() + ")", callArgNode.getLastParsedFragment());
								}
							}
							else {
								throwIssue("Invalid argument " + argument.getValue(), callArgNode.getLastParsedFragment());
							}
							index++;
						}
					}
	
					if (index != requiredArgumentsCount) {
						throwIssue("Invalid number of arguments ", getArgsFragment());
					}
				}
				else {
					throwIssue("No creation scheme for concept " + flexoConceptType.getName(), getConceptNameFragment());
				}
			}
			else {
				throwIssue("Unknown concept " + getConceptName(), getConceptNameFragment());
			}
	
		}
	}*/

	/*private void handleArguments(PArgumentList argumentList, AddVirtualModelInstance modelObject) {
		if (argumentList instanceof AManyArgumentList) {
			AManyArgumentList l = (AManyArgumentList) argumentList;
			handleArguments(l.getArgumentList(), modelObject);
			handleArgument(l.getExpression(), modelObject);
		}
		else if (argumentList instanceof AOneArgumentList) {
			handleArgument(((AOneArgumentList) argumentList).getExpression(), modelObject);
		}
	}
	
	private void handleArgument(PExpression expression, AddVirtualModelInstance modelObject) {
		BehaviourCallArgumentNode callArgNode = new BehaviourCallArgumentNode(expression, getanalyzer());
		addToChildren(callArgNode);
		callArgNode.deserialize();
	}*/

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (getModelObject().getType() instanceof VirtualModelInstanceType) {
			VirtualModel typeVirtualModel = ((VirtualModelInstanceType) getModelObject().getType()).getVirtualModel();
			if (typeVirtualModel == null) {
				throwIssue("Cannot find VirtualModel " + getModelObject().getType(), getTypeFragment());
			}
			else {
				if (typeVirtualModel.getCreationSchemes().size() == 0) {
					throwIssue("Cannot find any CreationScheme for VirtualModel " + getModelObject().getType(), getTypeFragment());
				}
				else if (typeVirtualModel.getCreationSchemes().size() == 1) {
					getModelObject().setFunction(typeVirtualModel.getCreationSchemes().get(0));
					// System.out.println("Set constructor to " + getModelObject().getFunction());
				}
				else {
					throwIssue("Ambigous CreationScheme for FlexoConcept " + getModelObject().getType(), getTypeFragment());
				}
			}
		}
		else {
			throwIssue("Type does not address any FlexoConcept", getTypeFragment());
		}

	}

	@Override
	public CreationSchemePathElement buildModelObjectFromAST(Node astNode) {

		if (readyToBuildModelObject()) {

			// System.out.println("astNode = " + astNode);
			// System.exit(-1);

			Type type = null;
			if (astNode instanceof ASimpleNewInstance) {
				handleArguments(((ASimpleNewInstance) astNode).getArgumentList());
				type = TypeFactory.makeType(((ASimpleNewInstance) astNode).getType(), getSemanticsAnalyzer().getTypingSpace());
			}
			else if (astNode instanceof AFullQualifiedNewInstance) {
				handleArguments(((AFullQualifiedNewInstance) astNode).getArgumentList());
				type = TypeFactory.makeType(((AFullQualifiedNewInstance) astNode).getConceptName(), getSemanticsAnalyzer().getTypingSpace());
			}

			CreationSchemePathElement pathElement = (CreationSchemePathElement) getBindingFactory().makeNewInstancePathElement(type,
					getParentPathElement(), null, getArguments(), getBindable());

			decodeFMLProperties(getFMLParameters(), pathElement);

			pathElement.setBindingPathElementOwner(this);
			return pathElement;
			/*NewVirtualModelInstanceBindingPathElement returned = new NewVirtualModelInstanceBindingPathElement(
					(VirtualModelInstanceType) type, null, // default constructor,
					getArguments());
			return returned;*/
		}
		return null;

		/*AddVirtualModelInstance returned = getFactory().newAddVirtualModelInstance();
		// System.out.println(">>>>>> New FCI " + astNode);
		
		Type type = TypeFactory.makeType(getConceptNameNode(), getanalyzer().getTypingSpace());
		if (type instanceof FlexoConceptInstanceType) {
			conceptType = (FlexoConceptInstanceType) type;
		}
		else {
			throwIssue("Unexpected type " + getText(getConceptNameNode()), getConceptNameFragment());
		}
		// conceptType = getTypeFactory().lookupConceptNamed(getConceptName(), getFragment(getConceptNameNode()));
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
		
		// Tricky area: we have to set model object now, otherwise NPE is raised during handleArguments()
		setModelObject(returned);
		
		if (astNode instanceof AFmlInstanceCreationFmlActionExp) {
			handleArguments(((AFmlInstanceCreationFmlActionExp) astNode).getArgumentList(), returned);
		}
		else if (astNode instanceof AJavaInstanceCreationFmlActionExp) {
			handleArguments(((AJavaInstanceCreationFmlActionExp) astNode).getArgumentList(), returned);
		}
		
		return returned;*/

	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		/*when(() -> isContainerFullQualified())
				.thenAppend(dynamicContents(() -> getModelObject().getContainer().toString()), getContainerFragment())
				.thenAppend(staticContents("."), getContainerDotFragment());
		append(staticContents("", "new", SPACE), getNewFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getFlexoConceptType())), getConceptNameFragment());
		when(() -> isFullQualified()).thenAppend(staticContents("::"), getColonColonFragment()).thenAppend(
				dynamicContents(() -> serializeFlexoBehaviour(getModelObject().getCreationScheme())), getConstructorNameFragment());
		append(staticContents("("), getLParFragment());
		append(childrenContents("", "", () -> getModelObject().getParameters(), ",", "", Indentation.DoNotIndent,
				AddFlexoConceptInstanceParameter.class));
		append(staticContents(")"), getRParFragment());
		
		when(() -> hasFMLProperties()).thenAppend(staticContents(SPACE, "with", SPACE), getFMLParametersWithFragment())
				.thenAppend(staticContents("("), getFMLParametersLParFragment())
				.thenAppend(childrenContents("", "", () -> getModelObject().getFMLPropertyValues(getFactory()), ", ", "",
						Indentation.DoNotIndent, FMLPropertyValue.class))
				.thenAppend(staticContents(")"), getFMLParametersRParFragment());
		
		// Append semi only when required
		when(() -> requiresSemi()).thenAppend(staticContents(";"), getSemiFragment());*/
		// @formatter:on
	}

	protected boolean hasFMLProperties() {
		if (getFMLParametersClause() != null) {
			return true;
		}
		if (getModelObject() != null) {
			return getModelObject().hasFMLProperties(getFactory());
		}
		return false;
	}

}
