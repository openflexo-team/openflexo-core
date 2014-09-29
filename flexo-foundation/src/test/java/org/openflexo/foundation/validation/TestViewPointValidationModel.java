package org.openflexo.foundation.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointValidationModel;
import org.openflexo.foundation.viewpoint.editionaction.AssignableAction;
import org.openflexo.foundation.viewpoint.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.model.exceptions.ModelDefinitionException;
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
				.getModelEntity(org.openflexo.foundation.viewpoint.CloningScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoBehaviourObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.technologyadapter.TypeAwareModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.DeleteAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.IntegerInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.CreateFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.AbstractCreationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.IndividualParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ListParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AbstractAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptInstanceRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConcept.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AssignationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.ExecutionAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptBehaviouralFacet.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddIndividual.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.SelectFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.CheckboxInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptStructuralFacet.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.TextAreaInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ViewPoint.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.LocalizedEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.TechnologyObjectParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.DeletionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.FloatInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.VirtualModel.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.ObjectPropertyAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AssignableAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ActionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddClass.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.SelectIndividual.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FloatParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.resource.ResourceData.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.AbstractActionScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.DataPropertyAssertion.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.PropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.DeleteFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.VirtualModelModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.ProcedureAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.DropDownParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.DataPropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ObjectPropertyParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.TextFieldParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.RemoveFromListAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddToListAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.FetchRequestCondition.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptConstraint.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoBehaviourParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.InspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.NamedViewPointObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.PropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.FlexoConceptInspector.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.CheckboxParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.ControlStructureAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.MatchingCriteria.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.PrimitiveRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.IterationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.FlexoProperty.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.ConditionalAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.ObjectPropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.IntegerParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.URIParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.DataPropertyInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.TextAreaParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ViewPointObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoBehaviour.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity(org.openflexo.foundation.FlexoObject.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.OntologicObjectRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.FetchRequest.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoBehaviourParameters.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.DeclareFlexoRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.DeleteFlexoConceptInstanceParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.SynchronizationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.EditionAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ClassParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ActionContainer.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.InnerModelSlotParameter.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.FetchRequestIterationAction.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.AddConcept.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.IndividualInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.LocalizedDictionary.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.FlexoRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.ClassInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.CreationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.inspector.TextFieldInspectorEntry.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.technologyadapter.ModelSlot.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.editionaction.MatchFlexoConceptInstance.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.NavigationScheme.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.ClassRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.IndividualRole.class) != null);
		assertTrue(validationModel.getValidationModelFactory().getModelContext()
				.getModelEntity(org.openflexo.foundation.viewpoint.PropertyRole.class) != null);

		assertEquals(validationModel.getSize(), validationModel.getSortedClasses().size());

	}

	@Test
	@TestOrder(2)
	public void testViewPointValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(ViewPoint.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getSize());
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
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getSize());
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
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getSize());
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
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getSize());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(FlexoRole.FlexoRoleMustHaveAName.class));
	}

	@Test
	@TestOrder(6)
	public void testDeclareFlexoRoleValidationRules() throws ModelDefinitionException {
		ValidationRuleSet<?> ruleSet = validationModel.getRuleSet(DeclareFlexoRole.class);
		assertNotNull(ruleSet);
		System.out.println("ruleSet=" + ruleSet + " size=" + ruleSet.getSize());
		for (ValidationRule r : ruleSet.getRules()) {
			System.out.println("> " + r);
		}
		assertTrue(ruleSet.containsRuleClass(DeclareFlexoRole.ObjectBindingIsRequiredAndMustBeValid.class));
		assertTrue(ruleSet.containsRuleClass(AssignableAction.AssignationBindingMustBeValidOrVariable.class));
		assertTrue(ruleSet.containsRuleClass(EditionAction.ConditionalBindingMustBeValid.class));
	}
}
