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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelObjectActorReference;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;

@ModelEntity
@ImplementationClass(FlexoConceptInstanceRole.FlexoConceptInstanceRoleImpl.class)
@XMLElement
@FML("FlexoConceptInstanceRole")
public interface FlexoConceptInstanceRole extends FlexoRole<FlexoConceptInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	public CreationScheme getCreationScheme();

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public FMLRTModelSlot getVirtualModelModelSlot();

	public static abstract class FlexoConceptInstanceRoleImpl extends FlexoRoleImpl<FlexoConceptInstance>
			implements FlexoConceptInstanceRole {

		private static final Logger logger = Logger.getLogger(FlexoConceptInstanceRole.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private String _flexoConceptTypeURI;

		public FlexoConceptInstanceRoleImpl() {
			super();
			// logger.severe("############# Created FlexoConceptInstanceRole " + Integer.toHexString(hashCode()) +
			// " model version="
			// + builder.getModelVersion() + " file=" + builder.resource.getFile().getAbsolutePath());
		}

		/*@Override
		public void finalizeDeserialization(Object builder) {
			super.finalizeDeserialization(builder);
			logger.severe("############# Finalized FlexoConceptInstanceRole " + Integer.toHexString(hashCode()) + toString());
		}*/

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as FlexoConceptInstance conformTo " + getTypeDescription() + ";", context);
			return out.toString();
		}

		@Override
		public Type getType() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getInstanceType();
			}
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}

		@Override
		public String getTypeDescription() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getName();
			}
			return "FlexoConcept";
		}

		/*@Override
		public boolean getIsPrimaryRole() {
			return false;
		}
		
		@Override
		public void setIsPrimaryRole(boolean isPrimary) {
			// Not relevant
		}*/

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getFlexoConcept();
			}
			if (flexoConceptType == null && _flexoConceptTypeURI != null && getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(_flexoConceptTypeURI);
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				this.flexoConceptType = flexoConceptType;
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
					setCreationScheme(null);
				}
				notifyResultingTypeChanged();
				/*if (getFlexoConcept() != null) {
					for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
						s.updateBindingModels();
					}
				}*/
			}
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			if (flexoConceptType == null && _flexoConceptTypeURI != null && getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(_flexoConceptTypeURI);
			}
		}

		@Override
		public String _getCreationSchemeURI() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getURI();
			}
			return _creationSchemeURI;
		}

		@Override
		public void _setCreationSchemeURI(String uri) {
			if (getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(uri);
				/*for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
					s.updateBindingModels();
				}*/
			}
			_creationSchemeURI = uri;
		}

		@Override
		public String _getFlexoConceptTypeURI() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getURI();
			}
			return _flexoConceptTypeURI;
		}

		@Override
		public void _setFlexoConceptTypeURI(String uri) {
			if (getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(uri);
			}
			_flexoConceptTypeURI = uri;
		}

		@Override
		public CreationScheme getCreationScheme() {
			if (creationScheme == null && _creationSchemeURI != null && getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(_creationSchemeURI);
			}
			return creationScheme;
		}

		public void setCreationScheme(CreationScheme creationScheme) {
			this.creationScheme = creationScheme;
			if (creationScheme != null) {
				_creationSchemeURI = creationScheme.getURI();
			}
		}

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Reference;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public ModelObjectActorReference<FlexoConceptInstance> makeActorReference(FlexoConceptInstance object, FlexoConceptInstance epi) {
			VirtualModelInstanceModelFactory factory = epi.getFactory();
			ModelObjectActorReference<FlexoConceptInstance> returned = factory.newInstance(ModelObjectActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(epi);
			returned.setModellingElement(object);
			return returned;
		}

		/*@Override
		public FMLRTModelSlot getModelSlot() {
			FMLRTModelSlot returned = (FMLRTModelSlot) super.getModelSlot();
			return returned;
		}*/

		@Override
		public FMLRTModelSlot getVirtualModelModelSlot() {
			return (FMLRTModelSlot) getModelSlot();
		}

		public void setVirtualModelModelSlot(FMLRTModelSlot modelSlot) {
			setModelSlot(modelSlot);
		}

	}

	@DefineValidationRule
	public static class MustHaveAConceptType extends ValidationRule<MustHaveAConceptType, FlexoConceptInstanceRole> {

		public MustHaveAConceptType() {
			super(FlexoConceptInstanceRole.class, "FlexoConceptInstanceRole_should_have_a_type");
		}

		@Override
		public ValidationIssue<MustHaveAConceptType, FlexoConceptInstanceRole> applyValidation(FlexoConceptInstanceRole aRole) {
			FlexoConcept fc = aRole.getFlexoConceptType();
			if (fc == null) {
				return new ValidationWarning<MustHaveAConceptType, FlexoConceptInstanceRole>(this, aRole,
						"FlexoConceptInstanceRole_should_have_a_type");

			}
			return null;
		}

	}

}
