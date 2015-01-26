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

package org.openflexo.foundation.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointValidationModel;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationRuleSet;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test ViewPoint validation model
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestViewPointValidationModel extends OpenflexoTestCase {

	static ViewPointValidationModel validationModel;

	@Test
	@TestOrder(1)
	public void testCreateViewPointValidationModel() throws ModelDefinitionException {

		instanciateTestServiceManager();
		validationModel = new ViewPointValidationModel(serviceManager.getTechnologyAdapterService());
		System.out.println("class number= " + validationModel.getValidationModelFactory().getModelContext().getEntityCount());

		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.technologyadapter.FreeModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.CloningScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoBehaviourObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.technologyadapter.TypeAwareModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.DeleteAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.IntegerInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.CreateFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.AbstractCreationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.IndividualParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ListParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AbstractAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptInstanceRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConcept.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AssignationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptBehaviouralFacet.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AddIndividual.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.CheckboxInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptStructuralFacet.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.TextAreaInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ViewPoint.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ViewPointLocalizedEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.TechnologyObjectParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.DeletionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.FloatInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.VirtualModel.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.ObjectPropertyAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AssignableAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ActionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AddClass.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.SelectIndividual.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FloatParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.resource.ResourceData.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.AbstractActionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.DataPropertyAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.PropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.FMLRTModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.DropDownParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.DataPropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ObjectPropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.TextFieldParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.RemoveFromListAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AddToListAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.FetchRequestCondition.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptConstraint.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoBehaviourParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.InspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.PropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.FlexoConceptInspector.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.CheckboxParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.controlgraph.ControlStructureAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.PrimitiveRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.controlgraph.IterationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.FlexoProperty.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.controlgraph.ConditionalAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.ObjectPropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.IntegerParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.URIParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.DataPropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.TextAreaParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FMLObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoBehaviour.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity(org.openflexo.foundation.FlexoObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.OntologicObjectRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.FetchRequest.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.SynchronizationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.EditionAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ClassParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ActionContainer.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.InnerModelSlotParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.controlgraph.FetchRequestIterationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.editionaction.AddConcept.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.IndividualInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ViewPointLocalizedDictionary.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.FlexoRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.ClassInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.CreationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.inspector.TextFieldInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.technologyadapter.ModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.NavigationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.ClassRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.IndividualRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.fml.PropertyRole.class) != null);

	}

	@Test
	@TestOrder(2)
	public void testViewPointValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(ViewPoint.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getRulesCount());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(ViewPoint.ViewPointMustHaveAName.class));
		assertTrue(ruleSet.containsRuleClass(ViewPoint.ViewPointURIMustBeValid.class));
	}

	@Test
	@TestOrder(3)
	public void testFlexoConceptValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(FlexoConcept.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getRulesCount());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(FlexoConcept.FlexoConceptShouldHaveDeletionScheme.class));
		assertTrue(ruleSet.containsRuleClass(FlexoConcept.FlexoConceptShouldHaveFlexoBehaviours.class));
		assertTrue(ruleSet.containsRuleClass(FlexoConcept.FlexoConceptShouldHaveRoles.class));
	}

	@Test
	@TestOrder(4)
	public void testFlexoRoleValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(FlexoRole.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getRulesCount());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(FlexoRole.FlexoRoleMustHaveAName.class));
	}

	@Test
	@TestOrder(5)
	public void testModelSlotValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(ModelSlot.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getRulesCount());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(FlexoRole.FlexoRoleMustHaveAName.class));
	}

	@Test
	@TestOrder(6)
	public void testAssignationActionValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(AssignationAction.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getRulesCount());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(AssignationAction.AssignationBindingIsRequiredAndMustBeValid.class));
		// assertTrue(ruleSet.containsRuleClass(EditionAction.ConditionalBindingMustBeValid.class));
	}
}
