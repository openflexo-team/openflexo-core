/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.rm;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.javareflect.JavaNewInstanceMethodPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.pamela.model.PamelaVisitor;
import org.openflexo.toolbox.JavaUtils;

@Deprecated
@FMLMigration
public class XMLToFMLConverter {

	private FMLCompilationUnit compilationUnit;

	private List<AddClassInstance> addClassInstanceActions = new ArrayList<>();
	private List<AddToListAction<?>> addToListActions = new ArrayList<>();
	private List<RemoveFromListAction<?>> removeFromListActions = new ArrayList<>();
	private List<AddFlexoConceptInstance<?>> addFlexoConceptInstanceActions = new ArrayList<>();
	private List<AddVirtualModelInstance> addVirtualModelInstanceActions = new ArrayList<>();
	private List<CreateTopLevelVirtualModelInstance> createTopLevelVirtualModelInstanceActions = new ArrayList<>();

	public XMLToFMLConverter(FMLCompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public void convert() {

		System.out.println("convertFromXMLToFML for " + compilationUnit);

		System.out.println("In " + compilationUnit.getVirtualModel());
		for (FlexoConcept flexoConcept : compilationUnit.getVirtualModel().getFlexoConcepts()) {
			System.out.println(" > " + flexoConcept + " container: " + flexoConcept.getContainerFlexoConcept() + " parent: "
					+ flexoConcept.getParentFlexoConcepts());
		}
		// System.exit(-1);

		compilationUnit.accept(new PamelaVisitor() {
			@Override
			public void visit(Object object) {
				if (object instanceof CreationScheme) {
					CreationScheme cs = (CreationScheme) object;
					if (cs.getName().equals("_create")) {
						cs.setAnonymous(true);
					}
				}
				if (object instanceof DeletionScheme) {
					DeletionScheme ds = (DeletionScheme) object;
					if (ds.getName().equals("_delete")) {
						ds.setAnonymous(true);
					}
				}
			}
		});

		compilationUnit.accept(new PamelaVisitor() {
			@Override
			public void visit(Object object) {
				if (object instanceof FlexoBehaviourParameter) {
					FlexoBehaviourParameter behaviourParameter = (FlexoBehaviourParameter) object;
					if (StringUtils.isNotEmpty(behaviourParameter.getName())) {
						if (startsWithUpperCase(behaviourParameter.getName())) {
							try {
								behaviourParameter.setName(convertToLowerCase(behaviourParameter.getName()));
							} catch (InvalidNameException e) {
								// Should not happen
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		compilationUnit.accept(new PamelaVisitor() {
			@Override
			public void visit(Object object) {
				// System.out.println("> visit " + object);
				if (object instanceof FetchRequestCondition) {
					FetchRequestCondition condition = (FetchRequestCondition) object;
					convertConditionalDataBinding(condition.getCondition());
				}
				if (object instanceof ConditionalAction) {
					ConditionalAction condition = (ConditionalAction) object;
					convertConditionalDataBinding(condition.getCondition());
				}
				if (object instanceof ExpressionAction) {
					ExpressionAction expression = (ExpressionAction) object;
					convertExpressionDataBinding(expression.getExpression());
				}
				if (object instanceof AssignationAction) {
					convertAssignationAction((AssignationAction) object);
				}
			}
		});

		compilationUnit.accept(new PamelaVisitor() {
			@Override
			public void visit(Object object) {
				// System.out.println("> visit " + object);
				if (object instanceof AddClassInstance) {
					addClassInstanceActions.add((AddClassInstance) object);
				}
				if (object instanceof AddToListAction) {
					addToListActions.add((AddToListAction) object);
				}
				if (object instanceof RemoveFromListAction) {
					removeFromListActions.add((RemoveFromListAction) object);
				}
				if (object instanceof AddFlexoConceptInstance) {
					addFlexoConceptInstanceActions.add((AddFlexoConceptInstance) object);
				}
				if (object instanceof AddVirtualModelInstance) {
					addVirtualModelInstanceActions.add((AddVirtualModelInstance) object);
				}
				if (object instanceof CreateTopLevelVirtualModelInstance) {
					createTopLevelVirtualModelInstanceActions.add((CreateTopLevelVirtualModelInstance) object);
				}
				if (object instanceof AbstractActionScheme) {
					AbstractActionScheme behaviour = (AbstractActionScheme) object;
					if (behaviour.getDeclaredType() == null) {
						Type analyzedType = behaviour.getAnalyzedReturnType();
						behaviour.setDeclaredType(analyzedType);
					}
				}
			}
		});

		for (AddClassInstance addClassInstance : addClassInstanceActions) {
			migrateAddClassInstance(addClassInstance);
		}

		for (AddToListAction addToListAction : addToListActions) {
			migrateAddToListAction(addToListAction);
		}

		for (RemoveFromListAction addToListAction : removeFromListActions) {
			migrateRemoveFromListAction(addToListAction);
		}

		for (AddFlexoConceptInstance addFlexoConceptInstance : addFlexoConceptInstanceActions) {
			migrateAddFlexoConceptInstanceAction(addFlexoConceptInstance);
		}

		for (AddVirtualModelInstance addVirtualModelInstance : addVirtualModelInstanceActions) {
			migrateAddVirtualModelInstanceAction(addVirtualModelInstance);
		}

		for (CreateTopLevelVirtualModelInstance createTopLevelVirtualModelInstance : createTopLevelVirtualModelInstanceActions) {
			migrateCreateTopLevelVirtualModelInstanceAction(createTopLevelVirtualModelInstance);
		}

		compilationUnit.accept(new PamelaVisitor() {
			@Override
			public void visit(Object object) {
				// System.out.println("> visit " + object);
				if (object instanceof FlexoProperty) {
					FlexoProperty property = (FlexoProperty) object;
					String name = property.getName();
					if (!name.startsWith("_")) {
						try {
							property.setName(JavaUtils.getVariableName(name));
						} catch (InvalidNameException e) {
							// Should not happen
							e.printStackTrace();
						}
					}
				}
				if (object instanceof FlexoBehaviour) {
					FlexoBehaviour behaviour = (FlexoBehaviour) object;
					if (StringUtils.isNotEmpty(behaviour.getName())) {
						if (startsWithUpperCase(behaviour.getName())) {
							try {
								behaviour.setName(convertToLowerCase(behaviour.getName()));
							} catch (InvalidNameException e) {
								// Should not happen
								e.printStackTrace();
							}
						}
					}
				}
				if (object instanceof FlexoConcept) {
					FlexoConcept concept = (FlexoConcept) object;
					if (StringUtils.isNotEmpty(concept.getName())) {
						if (startsWithLowerCase(concept.getName())) {
							try {
								concept.setName(convertToUpperCase(concept.getName()));
							} catch (InvalidNameException e) {
								// Should not happen
								e.printStackTrace();
							}
						}
					}
				}
				if (object instanceof FlexoEnumValue) {
					FlexoEnumValue enumValue = (FlexoEnumValue) object;
					try {
						enumValue.setName(JavaUtils.getConstantJavaName(enumValue.getName()));
					} catch (InvalidNameException e) {
						// Should not happen
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void migrateAddClassInstance(AddClassInstance addClassInstance) {

		ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();

		JavaNewInstanceMethodPathElement pathElement = (JavaNewInstanceMethodPathElement) compilationUnit.getVirtualModel()
				.getBindingFactory()
				.makeNewInstancePathElement(addClassInstance.getType(), null, null, addClassInstance.getParameters(), expAction);

		DataBinding expression = new DataBinding(expAction, addClassInstance.getType(), BindingDefinitionType.GET);
		BindingPath bv = new BindingPath(Collections.singletonList(pathElement), expAction, FMLPrettyPrinter.getInstance());
		expression.setExpression(bv);

		expAction.setExpression(expression);
		addClassInstance.replaceWith(expAction);

	}

	private void migrateAddFlexoConceptInstanceAction(AddFlexoConceptInstance<?> addConceptInstance) {

		System.out.println("type: " + addConceptInstance.getFlexoConceptType());
		System.out.println("args: " + addConceptInstance.getParameters());
		System.out.println("container: " + addConceptInstance.getContainer());
		System.out.println("creationScheme: " + addConceptInstance.getCreationScheme());

		ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();

		List<DataBinding<?>> args = new ArrayList<>();
		for (AddFlexoConceptInstanceParameter parameter : addConceptInstance.getParameters()) {
			args.add(parameter.getValue());
		}

		System.out.println("BindingFactory: " + compilationUnit.getVirtualModel().getBindingFactory());

		CreationSchemePathElement pathElement = (CreationSchemePathElement) compilationUnit.getVirtualModel().getBindingFactory()
				.makeNewInstancePathElement(addConceptInstance.getFlexoConceptType().getInstanceType(), /*getParentPathElement()*/null,
						addConceptInstance.getCreationScheme().getName(), args, expAction);
		System.out.println("creationScheme:" + pathElement.getCreationScheme());

		DataBinding expression = new DataBinding(expAction, addConceptInstance.getFlexoConceptType().getInstanceType(),
				BindingDefinitionType.GET);

		if (addConceptInstance.getContainer() != null && addConceptInstance.getContainer().isSet()
				&& addConceptInstance.getContainer().isValid()
				&& addConceptInstance.getContainer().getExpression() instanceof BindingPath) {
			// pathElement.setContainer(addConceptInstance.getContainer());
			BindingPath containerBindingValue = (BindingPath) addConceptInstance.getContainer().getExpression();
			pathElement.setParent(containerBindingValue.getLastBindingPathElement());
			containerBindingValue.setOwner(expAction);
			containerBindingValue.addBindingPathElement(pathElement);
			expression.setExpression(containerBindingValue);
		}
		else {
			BindingPath bv = new BindingPath(Collections.singletonList(pathElement), expAction, FMLPrettyPrinter.getInstance());
			expression.setExpression(bv);
		}

		expAction.setExpression(expression);
		addConceptInstance.replaceWith(expAction);

	}

	private void migrateAddVirtualModelInstanceAction(AddVirtualModelInstance addVirtualModelInstance) {

		System.out.println("type: " + addVirtualModelInstance.getFlexoConceptType());
		System.out.println("args: " + addVirtualModelInstance.getParameters());
		System.out.println("container: " + addVirtualModelInstance.getContainer());
		System.out.println("receiver: " + addVirtualModelInstance.getReceiver());
		System.out.println("creationScheme: " + addVirtualModelInstance.getCreationScheme());

		ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();

		List<DataBinding<?>> args = new ArrayList<>();
		for (AddFlexoConceptInstanceParameter parameter : addVirtualModelInstance.getParameters()) {
			args.add(parameter.getValue());
		}

		System.out.println("BindingFactory: " + compilationUnit.getVirtualModel().getBindingFactory());

		CreationSchemePathElement pathElement = (CreationSchemePathElement) compilationUnit.getVirtualModel().getBindingFactory()
				.makeNewInstancePathElement(addVirtualModelInstance.getFlexoConceptType().getInstanceType(), /*getParentPathElement()*/null,
						addVirtualModelInstance.getCreationScheme().getName(), args, expAction);
		System.out.println("creationScheme:" + pathElement.getCreationScheme());

		if (addVirtualModelInstance.getVirtualModelInstanceName() != null
				&& addVirtualModelInstance.getVirtualModelInstanceName().isSet()) {
			pathElement.setVirtualModelInstanceName(addVirtualModelInstance.getVirtualModelInstanceName());
		}

		DataBinding expression = new DataBinding(expAction, addVirtualModelInstance.getFlexoConceptType().getInstanceType(),
				BindingDefinitionType.GET);

		if (addVirtualModelInstance.getReceiver() != null && addVirtualModelInstance.getReceiver().isSet()
				&& addVirtualModelInstance.getReceiver().isValid()
				&& addVirtualModelInstance.getReceiver().getExpression() instanceof BindingPath) {
			// pathElement.setContainer(addConceptInstance.getContainer());
			BindingPath containerBindingValue = (BindingPath) addVirtualModelInstance.getReceiver().getExpression();
			pathElement.setParent(containerBindingValue.getLastBindingPathElement());
			containerBindingValue.setOwner(expAction);
			containerBindingValue.addBindingPathElement(pathElement);
			expression.setExpression(containerBindingValue);
		}
		else {
			BindingPath bv = new BindingPath(Collections.singletonList(pathElement), expAction, FMLPrettyPrinter.getInstance());
			expression.setExpression(bv);
		}

		expAction.setExpression(expression);
		addVirtualModelInstance.replaceWith(expAction);

	}

	private void migrateCreateTopLevelVirtualModelInstanceAction(CreateTopLevelVirtualModelInstance createTopLevelVirtualModelInstance) {

		System.out.println("type: " + createTopLevelVirtualModelInstance.getFlexoConceptType());
		System.out.println("args: " + createTopLevelVirtualModelInstance.getParameters());
		System.out.println("resourceURI: " + createTopLevelVirtualModelInstance.getResourceURI());
		System.out.println("relativePath: " + createTopLevelVirtualModelInstance.getDynamicRelativePath());
		System.out.println("resourceCenter: " + createTopLevelVirtualModelInstance.getResourceCenter());
		System.out.println("creationScheme: " + createTopLevelVirtualModelInstance.getCreationScheme());

		ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();

		List<DataBinding<?>> args = new ArrayList<>();
		for (AddFlexoConceptInstanceParameter parameter : createTopLevelVirtualModelInstance.getParameters()) {
			args.add(parameter.getValue());
		}

		System.out.println("BindingFactory: " + compilationUnit.getVirtualModel().getBindingFactory());

		CreationSchemePathElement pathElement = (CreationSchemePathElement) compilationUnit.getVirtualModel().getBindingFactory()
				.makeNewInstancePathElement(createTopLevelVirtualModelInstance.getFlexoConceptType().getInstanceType(),
						/*getParentPathElement()*/null, createTopLevelVirtualModelInstance.getCreationScheme().getName(), args, expAction);
		System.out.println("creationScheme:" + pathElement.getCreationScheme());

		if (createTopLevelVirtualModelInstance.getVirtualModelInstanceName() != null
				&& createTopLevelVirtualModelInstance.getVirtualModelInstanceName().isSet()) {
			pathElement.setVirtualModelInstanceName(createTopLevelVirtualModelInstance.getVirtualModelInstanceName());
		}
		if (createTopLevelVirtualModelInstance.getResourceURI() != null && createTopLevelVirtualModelInstance.getResourceURI().isSet()) {
			pathElement.setResourceURI(createTopLevelVirtualModelInstance.getResourceURI());
		}
		if (createTopLevelVirtualModelInstance.getDynamicRelativePath() != null
				&& createTopLevelVirtualModelInstance.getDynamicRelativePath().isSet()) {
			pathElement.setDynamicRelativePath(createTopLevelVirtualModelInstance.getDynamicRelativePath());
		}
		if (createTopLevelVirtualModelInstance.getResourceCenter() != null
				&& createTopLevelVirtualModelInstance.getResourceCenter().isSet()) {
			pathElement.setResourceCenter(createTopLevelVirtualModelInstance.getResourceCenter());
		}

		DataBinding expression = new DataBinding(expAction, createTopLevelVirtualModelInstance.getFlexoConceptType().getInstanceType(),
				BindingDefinitionType.GET);
		BindingPath bv = new BindingPath(Collections.singletonList(pathElement), expAction, FMLPrettyPrinter.getInstance());
		expression.setExpression(bv);

		expAction.setExpression(expression);
		createTopLevelVirtualModelInstance.replaceWith(expAction);

	}

	private void migrateAddToListAction(AddToListAction addToListAction) {

		DataBinding<List> list = addToListAction.getList();

		FMLControlGraph replacingAction = null;

		list.revalidate();

		if (list.getExpression() instanceof BindingPath) {

			BindingPath listBindingValue = (BindingPath) list.getExpression();

			if (addToListAction.getAssignableAction() instanceof ExpressionAction) {
				// The most simple case
				DataBinding<Object> objectToAdd = ((ExpressionAction) addToListAction.getAssignableAction()).getExpression();

				ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();
				objectToAdd.setOwner(expAction);
				list.setOwner(expAction);
				SimpleMethodPathElement<?> addPathElement = compilationUnit.getVirtualModel().getBindingFactory()
						.makeSimpleMethodPathElement(listBindingValue.getLastBindingPathElement(), "add",
								Collections.singletonList(objectToAdd), expAction);
				// pathElement.setBindingPathElementOwner(this);

				listBindingValue.addBindingPathElement(addPathElement);

				DataBinding expression = new DataBinding(expAction, Void.TYPE, BindingDefinitionType.GET);
				expression.setOwner(expAction);
				expression.setExpression(listBindingValue);
				expAction.setExpression(expression);
				replacingAction = expAction;
			}
			else {
				AssignableAction assignableAction = addToListAction.getAssignableAction();
				addToListAction.setAssignableAction(null);
				Sequence sequence = compilationUnit.getFMLModelFactory().newSequence();
				DeclarationAction<?> declarationAction = compilationUnit.getFMLModelFactory().newDeclarationAction();
				declarationAction.setVariableName("item");
				declarationAction.setDeclaredType(assignableAction.getAssignableType());
				declarationAction.setAssignableAction(assignableAction);
				sequence.setControlGraph1(declarationAction);

				ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();
				SimpleMethodPathElement<?> addPathElement = compilationUnit.getVirtualModel().getBindingFactory()
						.makeSimpleMethodPathElement(listBindingValue.getLastBindingPathElement(), "add",
								Collections.singletonList(new DataBinding("item")), expAction);
				// pathElement.setBindingPathElementOwner(this);

				listBindingValue.addBindingPathElement(addPathElement);
				DataBinding expression = new DataBinding(expAction, Void.TYPE, BindingDefinitionType.GET);
				expression.setExpression(listBindingValue);
				expAction.setExpression(expression);

				sequence.setControlGraph2(expAction);

				replacingAction = sequence;
			}
		}
		else {
			System.out.println("Unexpected list=" + list);
			System.out.println("valid: " + list.isValid());
			System.out.println("reason: " + list.invalidBindingReason());
			System.out.println("expression " + list.getExpression());
			if (list.getExpression() != null) {
				System.out.println("of " + list.getExpression().getClass());
			}
			// System.exit(-1);
		}

		if (replacingAction != null) {
			addToListAction.replaceWith(replacingAction);
		}

	}

	private void migrateRemoveFromListAction(RemoveFromListAction removeFromListAction) {

		DataBinding<List> list = removeFromListAction.getList();

		FMLControlGraph replacingAction = null;

		list.revalidate();

		if (list.getExpression() instanceof BindingPath) {

			BindingPath listBindingValue = (BindingPath) list.getExpression();

			// The most simple case
			DataBinding<Object> objectToRemove = removeFromListAction.getValue();

			ExpressionAction<?> expAction = compilationUnit.getFMLModelFactory().newExpressionAction();
			objectToRemove.setOwner(expAction);
			list.setOwner(expAction);

			SimpleMethodPathElement<?> addPathElement = compilationUnit.getVirtualModel().getBindingFactory().makeSimpleMethodPathElement(
					listBindingValue.getLastBindingPathElement(), "remove", Collections.singletonList(objectToRemove), expAction);
			// pathElement.setBindingPathElementOwner(this);

			listBindingValue.addBindingPathElement(addPathElement);

			DataBinding expression = new DataBinding(expAction, Void.TYPE, BindingDefinitionType.GET);
			expression.setExpression(listBindingValue);
			expAction.setExpression(expression);
			replacingAction = expAction;
		}
		else {
			System.out.println("Unexpected list=" + list);
			System.out.println("valid: " + list.isValid());
			System.out.println("reason: " + list.invalidBindingReason());
			System.out.println("expression " + list.getExpression());
			if (list.getExpression() != null) {
				System.out.println("of " + list.getExpression().getClass());
			}
			// System.exit(-1);
		}

		if (replacingAction != null) {
			removeFromListAction.replaceWith(replacingAction);
		}

	}

	private void convertConditionalDataBinding(DataBinding<Boolean> condition) {
		if (!condition.isValid()) {
			String oldExp = condition.toString();
			if (oldExp.contains("=")) {
				String newExpressionAsString = oldExp.replace("=", "==");
				condition.setUnparsedBinding(newExpressionAsString);
				condition.revalidate();
				System.out.println("Replaced " + oldExp + " by " + condition + " valid: " + condition.isValid()
						+ (!condition.isValid() ? " reason: " + condition.invalidBindingReason() : ""));
			}
			if (oldExp.contains("&")) {
				String newExpressionAsString = oldExp.replace("&", "&&");
				condition.setUnparsedBinding(newExpressionAsString);
				condition.revalidate();
				System.out.println("Replaced " + oldExp + " by " + condition + " valid: " + condition.isValid()
						+ (!condition.isValid() ? " reason: " + condition.invalidBindingReason() : ""));
			}
			if (oldExp.contains("|")) {
				String newExpressionAsString = oldExp.replace("|", "||");
				condition.setUnparsedBinding(newExpressionAsString);
				condition.revalidate();
				System.out.println("Replaced " + oldExp + " by " + condition + " valid: " + condition.isValid()
						+ (!condition.isValid() ? " reason: " + condition.invalidBindingReason() : ""));
			}
		}
	}

	private void convertExpressionDataBinding(DataBinding<?> expression) {
		if (!expression.isValid()) {
			String oldExp = expression.toString();
			if (oldExp.contains("super.create")) {
				String newExpressionAsString = oldExp.replace("super.create(", "super(");
				expression.setUnparsedBinding(newExpressionAsString);
				expression.revalidate();
				System.out.println("Replaced " + oldExp + " by " + expression + " valid: " + expression.isValid()
						+ (!expression.isValid() ? " reason: " + expression.invalidBindingReason() : ""));
			}
			if (oldExp.startsWith("parameters.")) {
				String remaining = oldExp.substring(11);
				if (startsWithUpperCase(remaining)) {
					String newExpressionAsString = "parameters." + convertToLowerCase(remaining);
					expression.setUnparsedBinding(newExpressionAsString);
					expression.revalidate();
					System.out.println("Replaced " + oldExp + " by " + expression + " valid: " + expression.isValid()
							+ (!expression.isValid() ? " reason: " + expression.invalidBindingReason() : ""));
				}
			}
		}
	}

	private void convertAssignationAction(AssignationAction<?> assignationAction) {
		DataBinding<?> assignation = assignationAction.getAssignation();
		convertAssignationActionDataBinding(assignation);
	}

	private void convertAssignationActionDataBinding(DataBinding<?> assignation) {
		String initialValue = assignation.toString();
		if (startsWithUpperCase(initialValue)) {
			String newValue = convertToLowerCase(initialValue);
			assignation.setUnparsedBinding(newValue);
			assignation.revalidate();
			System.out.println("Replaced " + initialValue + " by " + newValue + " valid: " + assignation.isValid()
					+ (!assignation.isValid() ? " reason: " + assignation.invalidBindingReason() : ""));
		}
	}

	private boolean startsWithUpperCase(String value) {
		Character c = value.charAt(0);
		return Character.isUpperCase(c);
	}

	private String convertToLowerCase(String initialValue) {
		Character c = initialValue.charAt(0);
		if (Character.isUpperCase(c)) {
			return "" + Character.toLowerCase(c) + initialValue.substring(1);
		}
		return initialValue;
	}

	private boolean startsWithLowerCase(String value) {
		Character c = value.charAt(0);
		return Character.isLowerCase(c);
	}

	private String convertToUpperCase(String initialValue) {
		Character c = initialValue.charAt(0);
		if (Character.isLowerCase(c)) {
			return "" + Character.toUpperCase(c) + initialValue.substring(1);
		}
		return initialValue;
	}

}
