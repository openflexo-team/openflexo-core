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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.ontology.fml.IndividualRole;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

@FIBPanel("Fib/FML/AddIndividualPanel.fib")
@ModelEntity(isAbstract = true)
@ImplementationClass(AddIndividual.AddIndividualImpl.class)
public abstract interface AddIndividual<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyIndividual<?>>
		extends AddConcept<MS, M, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String INDIVIDUAL_NAME_KEY = "individualName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DYNAMIC_TYPE_KEY = "dynamicType";
	@PropertyIdentifier(type = Vector.class)
	public static final String DATA_ASSERTIONS_KEY = "dataAssertions";
	@PropertyIdentifier(type = Vector.class)
	public static final String OBJECT_ASSERTIONS_KEY = "objectAssertions";
	@PropertyIdentifier(type = String.class)
	public static final String ONTOLOGY_CLASS_URI_KEY = "ontologyClassURI";
	// @PropertyIdentifier(type = TypeAwareModelSlot.class)
	// public static final String MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = INDIVIDUAL_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getIndividualName();

	@Setter(INDIVIDUAL_NAME_KEY)
	public void setIndividualName(DataBinding<String> individualName);

	@Getter(value = DYNAMIC_TYPE_KEY)
	@XMLAttribute
	public DataBinding<IFlexoOntologyClass<?>> getDynamicType();

	@Setter(DYNAMIC_TYPE_KEY)
	public void setDynamicType(DataBinding<IFlexoOntologyClass<?>> dynamicType);

	@Getter(value = DATA_ASSERTIONS_KEY, cardinality = Cardinality.LIST, inverse = DataPropertyAssertion.ACTION_KEY)
	@XMLElement(xmlTag = "DataPropertyAssertion")
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<DataPropertyAssertion> getDataAssertions();

	@Setter(DATA_ASSERTIONS_KEY)
	public void setDataAssertions(List<DataPropertyAssertion> dataAssertions);

	@Adder(DATA_ASSERTIONS_KEY)
	public void addToDataAssertions(DataPropertyAssertion aDataAssertion);

	@Remover(DATA_ASSERTIONS_KEY)
	public void removeFromDataAssertions(DataPropertyAssertion aDataAssertion);

	@Getter(value = OBJECT_ASSERTIONS_KEY, cardinality = Cardinality.LIST, inverse = ObjectPropertyAssertion.ACTION_KEY)
	@XMLElement(xmlTag = "ObjectPropertyAssertion")
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ObjectPropertyAssertion> getObjectAssertions();

	@Setter(OBJECT_ASSERTIONS_KEY)
	public void setObjectAssertions(List<ObjectPropertyAssertion> objectAssertions);

	@Adder(OBJECT_ASSERTIONS_KEY)
	public void addToObjectAssertions(ObjectPropertyAssertion aObjectAssertion);

	@Remover(OBJECT_ASSERTIONS_KEY)
	public void removeFromObjectAssertions(ObjectPropertyAssertion aObjectAssertion);

	@Getter(value = ONTOLOGY_CLASS_URI_KEY)
	@XMLAttribute
	public String _getOntologyClassURI();

	@Setter(ONTOLOGY_CLASS_URI_KEY)
	public void _setOntologyClassURI(String ontologyClassURI);

	public IFlexoOntologyClass<?> getOntologyClass();

	public void setOntologyClass(IFlexoOntologyClass<?> ontologyClass);

	/*@Override
	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement
	public MS getModelSlot();
	
	@Override
	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);*/

	public ObjectPropertyAssertion createObjectPropertyAssertion();

	public ObjectPropertyAssertion deleteObjectPropertyAssertion(ObjectPropertyAssertion assertion);

	public DataPropertyAssertion createDataPropertyAssertion();

	public DataPropertyAssertion deleteDataPropertyAssertion(DataPropertyAssertion assertion);

	public static abstract class AddIndividualImpl<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyIndividual<?>>
			extends AddConceptImpl<MS, M, T> implements AddIndividual<MS, M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(AddIndividual.class.getPackage().getName());

		protected String ontologyClassURI = null;

		private DataBinding<String> individualName;

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}
			out.append(getClass().getSimpleName() + (getOntologyClass() != null ? " conformTo " + getOntologyClass().getName() : "")
					+ " from " + (getModelSlot() != null ? getModelSlot().getName() : "") + " {" + StringUtils.LINE_SEPARATOR, context);
			out.append(getAssertionsFMLRepresentation(context), context);
			out.append("}", context);
			if (getAssignation().isSet()) {
				out.append(")", context);
			}
			return out.toString();
		}*/

		protected String getAssertionsFMLRepresentation(FMLRepresentationContext context) {
			if (getDataAssertions().size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (DataPropertyAssertion a : getDataAssertions()) {
					if (a.getOntologyProperty() != null) {
						sb.append("  " + a.getOntologyProperty().getName() + " = " + a.getValue().toString() + ";"
								+ StringUtils.LINE_SEPARATOR);
					}
				}
				return sb.toString();
			}
			if (getObjectAssertions().size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (ObjectPropertyAssertion a : getObjectAssertions()) {
					sb.append(
							"  " + a.getOntologyProperty().getName() + " = " + a.getObject().toString() + ";" + StringUtils.LINE_SEPARATOR);
				}
				return sb.toString();
			}
			return null;
		}

		public abstract Class<T> getOntologyIndividualClass();

		@Override
		public IndividualRole<T> getAssignedFlexoProperty() {
			return (IndividualRole<T>) super.getAssignedFlexoProperty();
		}

		public IFlexoOntologyClass<?> getType() {
			return getOntologyClass();
		}

		public void setType(IFlexoOntologyClass<?> type) {
			setOntologyClass(type);
		}

		@Override
		public IFlexoOntologyClass getOntologyClass() {
			if (getAssignedFlexoProperty() != null) {
				return getAssignedFlexoProperty().getOntologicType();
			}
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return FlexoOntologyVirtualModelNature.getOntologyClass(ontologyClassURI, getOwningVirtualModel());
			}
			return null;
		}

		@Override
		public void setOntologyClass(IFlexoOntologyClass ontologyClass) {
			if (ontologyClass != null) {
				if (getAssignedFlexoProperty() instanceof IndividualRole) {
					if (getAssignedFlexoProperty().getOntologicType() != null) {
						if (getAssignedFlexoProperty().getOntologicType().isSuperConceptOf(ontologyClass)) {
						}
						else {
							getAssignedFlexoProperty().setOntologicType(ontologyClass);
						}
					}
					else {
						getAssignedFlexoProperty().setOntologicType(ontologyClass);
					}
				}
				ontologyClassURI = ontologyClass.getURI();
			}
			else {
				ontologyClassURI = null;
			}
			// System.out.println("ontologyClassURI=" + ontologyClassURI);
		}

		@Override
		public String _getOntologyClassURI() {
			if (getOntologyClass() != null) {
				if (getAssignedFlexoProperty() instanceof IndividualRole
						&& getAssignedFlexoProperty().getOntologicType() == getOntologyClass()) {
					// No need to store an overriding type, just use default provided by pattern property
					return null;
				}
				return getOntologyClass().getURI();
			}
			return ontologyClassURI;
		}

		@Override
		public void _setOntologyClassURI(String ontologyClassURI) {
			this.ontologyClassURI = ontologyClassURI;
		}

		@Override
		public DataPropertyAssertion createDataPropertyAssertion() {
			DataPropertyAssertion newDataPropertyAssertion = getFMLModelFactory().newInstance(DataPropertyAssertion.class);
			addToDataAssertions(newDataPropertyAssertion);
			return newDataPropertyAssertion;
		}

		@Override
		public DataPropertyAssertion deleteDataPropertyAssertion(DataPropertyAssertion assertion) {
			removeFromDataAssertions(assertion);
			assertion.delete();
			return assertion;
		}

		@Override
		public ObjectPropertyAssertion createObjectPropertyAssertion() {
			ObjectPropertyAssertion newObjectPropertyAssertion = getFMLModelFactory().newInstance(ObjectPropertyAssertion.class);
			addToObjectAssertions(newObjectPropertyAssertion);
			return newObjectPropertyAssertion;
		}

		@Override
		public ObjectPropertyAssertion deleteObjectPropertyAssertion(ObjectPropertyAssertion assertion) {
			removeFromObjectAssertions(assertion);
			assertion.delete();
			return assertion;
		}

		@Override
		public DataBinding<String> getIndividualName() {
			if (individualName == null) {
				individualName = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				individualName.setBindingName("individualName");
			}
			return individualName;
		}

		@Override
		public void setIndividualName(DataBinding<String> individualName) {
			if (individualName != null) {
				individualName.setOwner(this);
				individualName.setDeclaredType(String.class);
				individualName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				individualName.setBindingName("individualName");
			}
			this.individualName = individualName;
		}

		@Override
		public Type getAssignableType() {
			if (getOntologyClass() == null) {
				return IFlexoOntologyIndividual.class;
			}
			return IndividualOfClass.getIndividualOfClass(getOntologyClass());
		}

	}

	@DefineValidationRule
	public static class AddIndividualActionMustDefineAnOntologyClass
			extends ValidationRule<AddIndividualActionMustDefineAnOntologyClass, AddIndividual> {
		public AddIndividualActionMustDefineAnOntologyClass() {
			super(AddIndividual.class, "add_individual_action_must_define_an_ontology_class");
		}

		@Override
		public ValidationIssue<AddIndividualActionMustDefineAnOntologyClass, AddIndividual> applyValidation(AddIndividual action) {
			if ((action.getDynamicType() == null || !action.getDynamicType().isSet()) && action.getOntologyClass() == null
					&& action.getOwner() instanceof AssignationAction) {
				Vector<FixProposal<AddIndividualActionMustDefineAnOntologyClass, AddIndividual>> v = new Vector<>();
				for (IndividualRole<?> pr : action.getFlexoConcept().getAccessibleProperties(IndividualRole.class)) {
					v.add(new SetsFlexoRole(pr));
				}
				return new ValidationError<>(this, action, "add_individual_action_does_not_define_any_ontology_class", v);
			}
			return null;
		}

		protected static class SetsFlexoRole extends FixProposal<AddIndividualActionMustDefineAnOntologyClass, AddIndividual> {

			private final IndividualRole<?> flexoRole;

			public SetsFlexoRole(IndividualRole<?> flexoRole) {
				super("assign_action_to_flexo_role" + " " + flexoRole.getRoleName());
				this.flexoRole = flexoRole;
			}

			public IndividualRole<?> getFlexoRole() {
				return flexoRole;
			}

			@Override
			protected void fixAction() {
				AddIndividual<?, ?, ?> action = getValidable();
				((AssignationAction) action.getOwner()).setAssignation(new DataBinding<>(flexoRole.getRoleName()));
			}

		}
	}

}
