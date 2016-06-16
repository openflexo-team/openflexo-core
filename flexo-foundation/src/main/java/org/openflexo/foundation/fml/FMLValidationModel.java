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

package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.Validable;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * This is the ValidationModel for FML model (ViewPoint, VirtualModel, FlexoConcept, etc...)
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class FMLValidationModel extends FlexoValidationModel {

	private static Resource fibValidationLocalizedDelegate = ResourceLocator.locateResource("FlexoLocalization/MLValidation");
	private static LocalizedDelegate VALIDATION_LOCALIZATION = new LocalizedDelegateImpl(fibValidationLocalizedDelegate, null,
			true, true);

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as VirtualModel
	 * parts, and return a newly created ModelContext dedicated to FML model validation
	 * 
	 * @param taService
	 * @return
	 * @throws ModelDefinitionException
	 */
	private static ModelContext computeModelContext(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = (taService != null ? FMLModelFactory.retrieveTechnologySpecificClasses(taService)
				: new ArrayList<Class<?>>());
		classes.add(ViewPoint.class);
		classes.add(VirtualModel.class);
		return ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes.size()]));
	}

	public FMLValidationModel(TechnologyAdapterService taService) throws ModelDefinitionException {
		super(computeModelContext(taService), VALIDATION_LOCALIZATION);

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

}
