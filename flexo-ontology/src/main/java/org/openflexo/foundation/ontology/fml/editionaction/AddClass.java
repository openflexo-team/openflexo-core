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
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.SubClassOfClass;
import org.openflexo.foundation.ontology.fml.ClassRole;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;

@ModelEntity(isAbstract = true)
@ImplementationClass(AddClass.AddClassImpl.class)
public abstract interface AddClass<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyClass<TA>, TA extends TechnologyAdapter<TA>>
		extends AddConcept<MS, M, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CLASS_NAME_KEY = "className";
	@PropertyIdentifier(type = String.class)
	public static final String ONTOLOGY_CLASS_URI_KEY = "ontologyClassURI";
	@PropertyIdentifier(type = IFlexoOntologyClass.class)
	public static final String ONTOLOGY_CLASS_KEY = "ontologyClass";
	// @PropertyIdentifier(type = TypeAwareModelSlot.class)
	// public static final String MODEL_SLOT_KEY = "modelSlot";

	@Getter(value = CLASS_NAME_KEY)
	@XMLAttribute(xmlTag = "newClassName")
	@FMLAttribute(value = CLASS_NAME_KEY, required = true)
	public DataBinding<String> getClassName();

	@Setter(CLASS_NAME_KEY)
	public void setClassName(DataBinding<String> className);

	@Getter(value = ONTOLOGY_CLASS_URI_KEY)
	@XMLAttribute
	public String _getOntologyClassURI();

	@Setter(ONTOLOGY_CLASS_URI_KEY)
	public void _setOntologyClassURI(String ontologyClassURI);

	// TODO: a DataBinding will be better !
	@Getter(value = ONTOLOGY_CLASS_KEY, ignoreType = true)
	@FMLAttribute(value = ONTOLOGY_CLASS_KEY, required = false)
	public T getOntologyClass();

	@Setter(ONTOLOGY_CLASS_KEY)
	public void setOntologyClass(T ontologyClass);

	/*@Override
	@Getter(value = MODEL_SLOT_KEY)
	@XMLAttribute
	public MS getModelSlot();
	
	@Override
	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(MS modelSlot);*/

	public static abstract class AddClassImpl<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyClass<TA>, TA extends TechnologyAdapter<TA>>
			extends AddConceptImpl<MS, M, T> implements AddClass<MS, M, T, TA> {

		private static final Logger logger = Logger.getLogger(AddClass.class.getPackage().getName());

		private String ontologyClassURI = null;

		private DataBinding<String> className;

		public AddClassImpl() {
			super();
		}

		@Override
		public ClassRole<T> getAssignedFlexoProperty() {
			FlexoProperty<?> superFlexoRole = super.getAssignedFlexoProperty();
			if (superFlexoRole instanceof ClassRole) {
				return (ClassRole) superFlexoRole;
			}
			else if (superFlexoRole != null) {
				// logger.warning("Unexpected pattern property of type " +
				// superPatternRole.getClass().getSimpleName());
				return null;
			}
			return null;
		}

		@Override
		public T getOntologyClass() {
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return (T) FlexoOntologyVirtualModelNature.getOntologyClass(ontologyClassURI, getOwningVirtualModel());
			}
			if (getAssignedFlexoProperty() != null) {
				return getAssignedFlexoProperty().getOntologicType();
			}
			return null;
		}

		public abstract Class<T> getOntologyClassClass();

		@Override
		public void setOntologyClass(T ontologyClass) {
			if (ontologyClass != null) {
				ClassRole<T> cr = getAssignedFlexoProperty();
				if (cr != null) {
					if (cr.getOntologicType().isSuperConceptOf(ontologyClass)) {
						ontologyClassURI = ontologyClass.getURI();
					}
					else {
						cr.setOntologicType(ontologyClass);
					}
				}
				else {
					ontologyClassURI = ontologyClass.getURI();
				}
			}
			else {
				ontologyClassURI = null;
			}
		}

		@Override
		public String _getOntologyClassURI() {
			if (getOntologyClass() != null) {
				if (getAssignedFlexoProperty() != null && getAssignedFlexoProperty().getOntologicType() == getOntologyClass()) {
					// No need to store an overriding type, just use default
					// provided by pattern property
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
		public DataBinding<String> getClassName() {
			if (className == null) {
				className = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				className.setBindingName("className");
			}
			return className;
		}

		@Override
		public void setClassName(DataBinding<String> className) {
			if (className != null) {
				className.setOwner(this);
				className.setDeclaredType(String.class);
				className.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				className.setBindingName("className");
			}
			this.className = className;
		}

		@Override
		public Type getAssignableType() {
			if (getOntologyClass() == null) {
				return IFlexoOntologyClass.class;
			}
			return SubClassOfClass.getSubClassOfClass(getOntologyClass());
		}

	}

	@DefineValidationRule
	public static class AddClassActionShouldDefineAnOntologyClass
			extends ValidationRule<AddClassActionShouldDefineAnOntologyClass, AddClass<?, ?, ?, ?>> {
		public AddClassActionShouldDefineAnOntologyClass() {
			super(AddClass.class, "add_individual_action_should_define_an_ontology_class");
		}

		@Override
		public ValidationIssue<AddClassActionShouldDefineAnOntologyClass, AddClass<?, ?, ?, ?>> applyValidation(
				AddClass<?, ?, ?, ?> action) {
			if (action.getOntologyClass() == null && action.getOwner() instanceof AssignationAction) {
				Vector<FixProposal<AddClassActionShouldDefineAnOntologyClass, AddClass<?, ?, ?, ?>>> v = new Vector<>();
				for (ClassRole<?> pr : action.getFlexoConcept().getAccessibleProperties(ClassRole.class)) {
					v.add(new SetsFlexoRole(pr));
				}
				return new ValidationWarning<>(this, action, "add_individual_action_does_not_define_any_ontology_class", v);
			}
			return null;
		}

		protected static class SetsFlexoRole extends FixProposal<AddClassActionShouldDefineAnOntologyClass, AddClass<?, ?, ?, ?>> {

			private final ClassRole<?> flexoRole;

			public SetsFlexoRole(ClassRole<?> flexoRole) {
				super("assign_action_to_flexo_role_($flexoRole.flexoRoleName)");
				this.flexoRole = flexoRole;
			}

			public ClassRole<?> getFlexoRole() {
				return flexoRole;
			}

			@Override
			protected void fixAction() {
				AddClass<?, ?, ?, ?> action = getValidable();
				((AssignationAction<?>) action.getOwner()).setAssignation(new DataBinding<>(flexoRole.getRoleName()));
			}

		}
	}

	@DefineValidationRule
	public static class URIBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AddClass> {
		public URIBindingIsRequiredAndMustBeValid() {
			super("'uri'_binding_is_required_and_must_be_valid", AddClass.class);
		}

		@Override
		public DataBinding<String> getBinding(AddClass object) {
			return object.getClassName();
		}

	}

}
