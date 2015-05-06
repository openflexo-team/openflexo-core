/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.util.logging.Logger;

import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoRoleWizard extends AbstractCreateFlexoPropertyWizard<CreateFlexoRole> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoRoleWizard.class.getPackage().getName());

	private static final String NO_ROLE_TYPE = FlexoLocalization.localizedForKey("please_choose_a_role_type");
	private static final String NO_FLEXO_CONCEPT_TYPE = FlexoLocalization.localizedForKey("please_choose_type_of_flexo_concept");
	private static final String NO_PRIMITIVE_TYPE = FlexoLocalization.localizedForKey("please_choose_primitive_type");
	private static final String NO_INDIVIDUAL_TYPE = FlexoLocalization.localizedForKey("please_choose_individual_type");

	public CreateFlexoRoleWizard(CreateFlexoRole action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribeFlexoRole makeDescriptionStep() {
		return new DescribeFlexoRole();
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_flexo_role");
	}

	@Override
	public DescribeFlexoRole getDescribeProperty() {
		return (DescribeFlexoRole) super.getDescribeProperty();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeFlexoRole.fib")
	public class DescribeFlexoRole extends DescribeProperty {

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_flexo_role");
		}

		@Override
		public CreateFlexoRole getAction() {
			return super.getAction();
		}

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getFlexoRoleClass() == null) {
				setIssueMessage(NO_ROLE_TYPE, IssueMessageType.ERROR);
				return false;
			} else if (getAction().isFlexoConceptInstance() && getFlexoConceptInstanceType() == null) {
				setIssueMessage(NO_FLEXO_CONCEPT_TYPE, IssueMessageType.ERROR);
				return false;
			} else if (getAction().isPrimitive() && getPrimitiveType() == null) {
				setIssueMessage(NO_PRIMITIVE_TYPE, IssueMessageType.ERROR);
				return false;
			} else if (getAction().isIndividual() && getIndividualType() == null) {
				setIssueMessage(NO_INDIVIDUAL_TYPE, IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getRoleName() {
			return getPropertyName();
		}

		public void setRoleName(String roleName) {
			setPropertyName(roleName);
			getPropertyChangeSupport().firePropertyChange("roleName", null, roleName);
			checkValidity();
		}

		public PropertyCardinality getCardinality() {
			return getAction().getCardinality();
		}

		public void setCardinality(PropertyCardinality roleCardinality) {
			if (roleCardinality != getCardinality()) {
				PropertyCardinality oldValue = getCardinality();
				getAction().setCardinality(roleCardinality);
				getPropertyChangeSupport().firePropertyChange("cardinality", oldValue, roleCardinality);
			}
		}

		public ModelSlot<?> getModelSlot() {
			return getAction().getModelSlot();
		}

		public void setModelSlot(ModelSlot<?> modelSlot) {
			if (getModelSlot() != modelSlot) {
				ModelSlot<?> oldValue = getModelSlot();
				getAction().setModelSlot(modelSlot);
				getPropertyChangeSupport().firePropertyChange("modelSlot", oldValue, modelSlot);
				getPropertyChangeSupport().firePropertyChange("adressedFlexoMetaModel", null, getAdressedFlexoMetaModel());
				getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getModelSlot());
				getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
				checkValidity();
			}
		}

		public Class<? extends FlexoRole> getFlexoRoleClass() {
			return getAction().getFlexoRoleClass();
		}

		public void setFlexoRoleClass(Class<? extends FlexoRole> flexoRoleClass) {
			if (getFlexoRoleClass() != flexoRoleClass) {
				Class<? extends FlexoRole> oldValue = getFlexoRoleClass();
				getAction().setFlexoRoleClass(flexoRoleClass);
				getPropertyChangeSupport().firePropertyChange("flexoRoleClass", oldValue, flexoRoleClass);
				getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
				checkValidity();
			}
		}

		public FlexoMetaModel<?> getAdressedFlexoMetaModel() {
			return getAction().getAdressedFlexoMetaModel();
		}

		public IFlexoOntologyClass getIndividualType() {
			return getAction().getIndividualType();
		}

		public void setIndividualType(IFlexoOntologyClass individualType) {
			if (getIndividualType() != individualType) {
				IFlexoOntologyClass oldValue = getIndividualType();
				getAction().setIndividualType(individualType);
				getPropertyChangeSupport().firePropertyChange("individualType", oldValue, individualType);
				checkValidity();
			}
		}

		public FlexoConcept getFlexoConceptInstanceType() {
			return getAction().getFlexoConceptInstanceType();
		}

		public void setFlexoConceptInstanceType(FlexoConcept flexoConceptInstanceType) {
			if (getFlexoConceptInstanceType() != flexoConceptInstanceType) {
				FlexoConcept oldValue = getFlexoConceptInstanceType();
				getAction().setFlexoConceptInstanceType(flexoConceptInstanceType);
				getPropertyChangeSupport().firePropertyChange("flexoConceptInstanceType", oldValue, flexoConceptInstanceType);
				checkValidity();
			}
		}

		public PrimitiveType getPrimitiveType() {
			return getAction().getPrimitiveType();
		}

		public void setPrimitiveType(PrimitiveType primitiveType) {
			if (getPrimitiveType() != primitiveType) {
				PrimitiveType oldValue = getPrimitiveType();
				getAction().setPrimitiveType(primitiveType);
				getPropertyChangeSupport().firePropertyChange("primitiveType", oldValue, primitiveType);
				checkValidity();
			}
		}

		public boolean isUseModelSlot() {
			return getAction().isUseModelSlot();
		}

		public void setUseModelSlot(boolean useModelSlot) {
			if (isUseModelSlot() != useModelSlot) {
				boolean oldValue = isUseModelSlot();
				getAction().setUseModelSlot(useModelSlot);
				getPropertyChangeSupport().firePropertyChange("useModelSlot", oldValue, useModelSlot);
				getPropertyChangeSupport().firePropertyChange("modelSlot", null, getModelSlot());
				getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getFlexoRoleClass());
				getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
			}
		}

	}

}
