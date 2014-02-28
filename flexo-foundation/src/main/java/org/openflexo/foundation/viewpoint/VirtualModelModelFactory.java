/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.viewpoint;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.technologyadapter.DeclareEditionAction;
import org.openflexo.foundation.technologyadapter.DeclareEditionActions;
import org.openflexo.foundation.technologyadapter.DeclareFetchRequest;
import org.openflexo.foundation.technologyadapter.DeclareFetchRequests;
import org.openflexo.foundation.technologyadapter.DeclarePatternRole;
import org.openflexo.foundation.technologyadapter.DeclarePatternRoles;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.editionaction.AddToListAction;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.ConditionalAction;
import org.openflexo.foundation.viewpoint.editionaction.CreateFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.editionaction.DataPropertyAssertion;
import org.openflexo.foundation.viewpoint.editionaction.DeleteAction;
import org.openflexo.foundation.viewpoint.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.editionaction.ExecutionAction;
import org.openflexo.foundation.viewpoint.editionaction.FetchRequestCondition;
import org.openflexo.foundation.viewpoint.editionaction.FetchRequestIterationAction;
import org.openflexo.foundation.viewpoint.editionaction.IterationAction;
import org.openflexo.foundation.viewpoint.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.editionaction.MatchingCriteria;
import org.openflexo.foundation.viewpoint.editionaction.ObjectPropertyAssertion;
import org.openflexo.foundation.viewpoint.editionaction.RemoveFromListAction;
import org.openflexo.foundation.viewpoint.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.FlexoConceptStructuralFacet;
import org.openflexo.foundation.viewpoint.inspector.CheckboxInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.ClassInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.DataPropertyInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.FlexoConceptInspector;
import org.openflexo.foundation.viewpoint.inspector.IndividualInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.IntegerInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.ObjectPropertyInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.PropertyInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.TextAreaInspectorEntry;
import org.openflexo.foundation.viewpoint.inspector.TextFieldInspectorEntry;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathFileConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle VirtualModel models<br>
 * One instance is declared for a {@link VirtualModelResource}
 * 
 * @author sylvain
 * 
 */
public class VirtualModelModelFactory extends ModelFactory {

	// TODO: the factory should be instantiated and managed by the TechnologyAdapterService, which should react to the registering
	// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
	public VirtualModelModelFactory(TechnologyAdapterService taService) throws ModelDefinitionException {
		super(computeModelContext(taService));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
	}

	// TODO: the factory should be instantiated and managed by the TechnologyAdapterService, which should react to the registering
	// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
	public VirtualModelModelFactory(TechnologyAdapterService taService, VirtualModelResource virtualModelResource)
			throws ModelDefinitionException {
		super(computeModelContext(taService));
		addConverter(new RelativePathFileConverter(virtualModelResource.getDirectory()));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
	}

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as VirtualModel
	 * parts, and return a newly created ModelContext dedicated to {@link VirtualModel} manipulations
	 * 
	 * @param taService
	 * @return
	 * @throws ModelDefinitionException
	 */
	private static ModelContext computeModelContext(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(VirtualModel.class);
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			for (Class<?> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				classes.add(modelSlotClass);
				DeclarePatternRoles prDeclarations = modelSlotClass.getAnnotation(DeclarePatternRoles.class);
				if (prDeclarations != null) {
					for (DeclarePatternRole prDeclaration : prDeclarations.value()) {
						classes.add(prDeclaration.flexoRoleClass());
					}
				}
				DeclareEditionActions eaDeclarations = modelSlotClass.getAnnotation(DeclareEditionActions.class);
				if (eaDeclarations != null) {
					for (DeclareEditionAction eaDeclaration : eaDeclarations.value()) {
						classes.add(eaDeclaration.editionActionClass());
					}
				}
				DeclareFetchRequests frDeclarations = modelSlotClass.getAnnotation(DeclareFetchRequests.class);
				if (frDeclarations != null) {
					for (DeclareFetchRequest frDeclaration : frDeclarations.value()) {
						classes.add(frDeclaration.fetchRequestClass());
					}
				}
			}
		}

		return ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes.size()]));
	}

	public VirtualModel newVirtualModel() {
		return newInstance(VirtualModel.class);
	}

	public SynchronizationScheme newSynchronizationScheme() {
		return newInstance(SynchronizationScheme.class);
	}

	public FlexoConceptConstraint newFlexoConceptConstraint() {
		return newInstance(FlexoConceptConstraint.class);
	}

	public CreationScheme newCreationScheme() {
		return newInstance(CreationScheme.class);
	}

	public CloningScheme newCloningScheme() {
		return newInstance(CloningScheme.class);
	}

	public ActionScheme newActionScheme() {
		return newInstance(ActionScheme.class);
	}

	public NavigationScheme newNavigationScheme() {
		return newInstance(NavigationScheme.class);
	}

	public DeletionScheme newDeletionScheme() {
		return newInstance(DeletionScheme.class);
	}

	public AddFlexoConceptInstanceParameter newAddFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		AddFlexoConceptInstanceParameter returned = newInstance(AddFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public DeleteFlexoConceptInstanceParameter newDeleteFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		DeleteFlexoConceptInstanceParameter returned = newInstance(DeleteFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public DataPropertyAssertion newDataPropertyAssertion() {
		return newInstance(DataPropertyAssertion.class);
	}

	public ObjectPropertyAssertion newObjectPropertyAssertion() {
		return newInstance(ObjectPropertyAssertion.class);
	}

	public FlexoConceptBehaviouralFacet newFlexoConceptBehaviouralFacet(FlexoConcept flexoConcept) {
		FlexoConceptBehaviouralFacet returned = newInstance(FlexoConceptBehaviouralFacet.class);
		returned.setFlexoConcept(flexoConcept);
		return returned;
	}

	public FlexoConceptStructuralFacet newFlexoConceptStructuralFacet(FlexoConcept flexoConcept) {
		FlexoConceptStructuralFacet returned = newInstance(FlexoConceptStructuralFacet.class);
		returned.setFlexoConcept(flexoConcept);
		return returned;
	}

	public URIParameter newURIParameter() {
		return newInstance(URIParameter.class);
	}

	public TextFieldParameter newTextFieldParameter() {
		return newInstance(TextFieldParameter.class);
	}

	public TextAreaParameter newTextAreaParameter() {
		return newInstance(TextAreaParameter.class);
	}

	public IntegerParameter newIntegerParameter() {
		return newInstance(IntegerParameter.class);
	}

	public CheckboxParameter newCheckboxParameter() {
		return newInstance(CheckboxParameter.class);
	}

	public DropDownParameter newDropDownParameter() {
		return newInstance(DropDownParameter.class);
	}

	public IndividualParameter newIndividualParameter() {
		return newInstance(IndividualParameter.class);
	}

	public ClassParameter newClassParameter() {
		return newInstance(ClassParameter.class);
	}

	public PropertyParameter newPropertyParameter() {
		return newInstance(PropertyParameter.class);
	}

	public ObjectPropertyParameter newObjectPropertyParameter() {
		return newInstance(ObjectPropertyParameter.class);
	}

	public DataPropertyParameter newDataPropertyParameter() {
		return newInstance(DataPropertyParameter.class);
	}

	public TechnologyObjectParameter newTechnologyObjectParameter() {
		return newInstance(TechnologyObjectParameter.class);
	}

	public ListParameter newListParameter() {
		return newInstance(ListParameter.class);
	}

	public FlexoConceptInstanceParameter newFlexoConceptInstanceParameter() {
		return newInstance(FlexoConceptInstanceParameter.class);
	}

	public FlexoBehaviourParameters newFlexoBehaviourParameters(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameters returned = newInstance(FlexoBehaviourParameters.class);
		returned.setFlexoBehaviour(flexoBehaviour);
		return returned;
	}

	public FetchRequestCondition newFetchRequestCondition() {
		return newInstance(FetchRequestCondition.class);
	}

	public FlexoConceptInspector newFlexoConceptInspector(FlexoConcept ep) {
		FlexoConceptInspector returned = newInstance(FlexoConceptInspector.class);
		returned.setFlexoConcept(ep);
		return returned;
	}

	public TextFieldInspectorEntry newTextFieldInspectorEntry() {
		return newInstance(TextFieldInspectorEntry.class);
	}

	public TextAreaInspectorEntry newTextAreaInspectorEntry() {
		return newInstance(TextAreaInspectorEntry.class);
	}

	public IntegerInspectorEntry newIntegerInspectorEntry() {
		return newInstance(IntegerInspectorEntry.class);
	}

	public CheckboxInspectorEntry newCheckboxInspectorEntry() {
		return newInstance(CheckboxInspectorEntry.class);
	}

	public IndividualInspectorEntry newIndividualInspectorEntry() {
		return newInstance(IndividualInspectorEntry.class);
	}

	public ClassInspectorEntry newClassInspectorEntry() {
		return newInstance(ClassInspectorEntry.class);
	}

	public PropertyInspectorEntry newPropertyInspectorEntry() {
		return newInstance(PropertyInspectorEntry.class);
	}

	public ObjectPropertyInspectorEntry newObjectPropertyInspectorEntry() {
		return newInstance(ObjectPropertyInspectorEntry.class);
	}

	public DataPropertyInspectorEntry newDataPropertyInspectorEntry() {
		return newInstance(DataPropertyInspectorEntry.class);
	}

	public CreateFlexoConceptInstanceParameter newCreateFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		CreateFlexoConceptInstanceParameter returned = newInstance(CreateFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public MatchingCriteria newMatchingCriteria(FlexoRole pr) {
		MatchingCriteria returned = newInstance(MatchingCriteria.class);
		returned.setFlexoRole(pr);
		return returned;
	}

	public FlexoConceptInstanceRole newFlexoConceptInstanceRole() {
		return newInstance(FlexoConceptInstanceRole.class);
	}

	public AddFlexoConceptInstance newAddFlexoConceptInstance() {
		return newInstance(AddFlexoConceptInstance.class);
	}

	public SelectFlexoConceptInstance newSelectFlexoConceptInstance() {
		return newInstance(SelectFlexoConceptInstance.class);
	}

	public FlexoConcept newFlexoConcept() {
		return newInstance(FlexoConcept.class);
	}

	public DeleteAction newDeleteAction() {
		return newInstance(DeleteAction.class);
	}

	public MatchFlexoConceptInstance newMatchFlexoConceptInstance() {
		return newInstance(MatchFlexoConceptInstance.class);
	}

	public org.openflexo.foundation.viewpoint.editionaction.DeclarePatternRole newDeclarePatternRole() {
		return newInstance(org.openflexo.foundation.viewpoint.editionaction.DeclarePatternRole.class);
	}

	public ExecutionAction newExecutionAction() {
		return newInstance(ExecutionAction.class);
	}

	public RemoveFromListAction newRemoveFromListAction() {
		return newInstance(RemoveFromListAction.class);
	}

	public AddToListAction newAddToListAction() {
		return newInstance(AddToListAction.class);
	}

	public AssignationAction newAssignationAction() {
		return newInstance(AssignationAction.class);
	}

	public ConditionalAction newConditionalAction() {
		return newInstance(ConditionalAction.class);
	}

	public IterationAction newIterationAction() {
		return newInstance(IterationAction.class);
	}

	public FetchRequestIterationAction newFetchRequestIterationAction() {
		return newInstance(FetchRequestIterationAction.class);
	}

}
