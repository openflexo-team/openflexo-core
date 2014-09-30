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

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.Validable;

/**
 * This is the ValidationModel for FML model (ViewPoint, VirtualModel, FlexoConcept, etc...)
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class ViewPointValidationModel extends FlexoValidationModel {

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as VirtualModel
	 * parts, and return a newly created ModelContext dedicated to FML model validation
	 * 
	 * @param taService
	 * @return
	 * @throws ModelDefinitionException
	 */
	private static ModelContext computeModelContext(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = (taService != null ? VirtualModelModelFactory.retrieveTechnologySpecificClasses(taService)
				: new ArrayList<Class<?>>());
		classes.add(ViewPoint.class);
		classes.add(VirtualModel.class);
		return ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes.size()]));
	}

	public ViewPointValidationModel(TechnologyAdapterService taService) throws ModelDefinitionException {
		super(computeModelContext(taService));

		/*registerRule(new FlexoConcept.FlexoConceptShouldHaveRoles());
		registerRule(new FlexoConcept.FlexoConceptShouldHaveEditionSchemes());
		registerRule(new FlexoConcept.FlexoConceptShouldHaveDeletionScheme());

		registerRule(new FlexoRole.FlexoRoleMustHaveAName());
		registerRule(new ClassRole.ClassRoleMustDefineAValidConceptClass());
		registerRule(new IndividualRole.IndividualFlexoRoleMustDefineAValidConceptClass());
		// registerRule(new DataPropertyStatementPatternRole.DataPropertyStatementPatternRoleMustDefineAValidProperty());
		// registerRule(new ObjectPropertyStatementPatternRole.ObjectPropertyStatementPatternRoleMustDefineAValidProperty());

		registerRule(new InspectorEntry.DataBindingIsRequiredAndMustBeValid());

		registerRule(new URIParameter.BaseURIBindingIsRequiredAndMustBeValid());

		registerRule(new EditionAction.ConditionalBindingMustBeValid());
		registerRule(new AssignableAction.AssignationBindingMustBeValid());

		registerRule(new AddIndividual.AddIndividualActionMustDefineAnOntologyClass());
		registerRule(new AddIndividual.URIBindingIsRequiredAndMustBeValid());

		registerRule(new DataPropertyAssertion.DataPropertyAssertionMustDefineAnOntologyProperty());
		registerRule(new DataPropertyAssertion.ValueBindingIsRequiredAndMustBeValid());
		registerRule(new ObjectPropertyAssertion.ObjectPropertyAssertionMustDefineAnOntologyProperty());
		registerRule(new ObjectPropertyAssertion.ObjectBindingIsRequiredAndMustBeValid());

		registerRule(new AddClass.AddClassActionMustDefineAnOntologyClass());
		registerRule(new AddClass.URIBindingIsRequiredAndMustBeValid());

		// registerRule(new AddStatement.SubjectIsRequiredAndMustBeValid());
		// registerRule(new AddObjectPropertyStatement.AddObjectPropertyStatementActionMustDefineAnObjectProperty());
		// registerRule(new AddObjectPropertyStatement.ObjectIsRequiredAndMustBeValid());
		// registerRule(new AddDataPropertyStatement.AddDataPropertyStatementActionMustDefineADataProperty());
		// registerRule(new AddDataPropertyStatement.ValueIsRequiredAndMustBeValid());


		// registerRule(new DeclareFlexoRole.AssignationBindingIsRequiredAndMustBeValid());
		registerRule(new DeclareFlexoRole.ObjectBindingIsRequiredAndMustBeValid());

		registerRule(new DeleteAction.ObjectToDeleteBindingIsRequiredAndMustBeValid());

		// registerRule(new GraphicalAction.GraphicalActionMustHaveASubject());
		// registerRule(new GraphicalAction.GraphicalActionMustDefineAValue());

		registerRule(new AddFlexoConceptInstance.VirtualModelInstanceBindingIsRequiredAndMustBeValid());
		registerRule(new AddFlexoConceptInstance.AddFlexoConceptInstanceMustAddressACreationScheme());
		registerRule(new AddFlexoConceptInstance.AddFlexoConceptInstanceParametersMustBeValid());

		registerRule(new ConditionalAction.ConditionBindingIsRequiredAndMustBeValid());
		registerRule(new IterationAction.IterationBindingIsRequiredAndMustBeValid());

		// Notify that the validation model is complete and that inheritance
		// computation could be performed
		update();*/
	}

	/**
	 * Return a boolean indicating if validation of supplied object must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	@Override
	protected boolean shouldNotifyValidation(Validable next) {
		return true;
	}

	/**
	 * Overrides fixAutomaticallyIfOneFixProposal
	 * 
	 * @see org.openflexo.model.validation.ValidationModel#fixAutomaticallyIfOneFixProposal()
	 */
	@Override
	public boolean fixAutomaticallyIfOneFixProposal() {
		return false;
	}

	@Override
	public String localizedForKey(String key) {
		return FlexoLocalization.localizedForKey(key);
	}
}
