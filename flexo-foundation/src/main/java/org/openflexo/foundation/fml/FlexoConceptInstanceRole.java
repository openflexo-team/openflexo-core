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

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.DefaultFlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(FlexoConceptInstanceRole.FlexoConceptInstanceRoleImpl.class)
@XMLElement
@FML("ConceptInstance")
public interface FlexoConceptInstanceRole extends FlexoRole<FlexoConceptInstance> {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_TYPE_KEY = "flexoConceptType";
	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	@Deprecated
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	@Deprecated
	public void _setCreationSchemeURI(String creationSchemeURI);

	@Deprecated
	public CreationScheme getCreationScheme();

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	/**
	 * This binding define the {@link FMLRTVirtualModelInstance} where addressed {@link FlexoConceptInstance} "lives"
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	@FMLAttribute(value = VIRTUAL_MODEL_INSTANCE_KEY, required = false)
	public DataBinding<VirtualModelInstance<?, ?>> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> virtualModelInstance);

	/**
	 * Return type of VirtualModel where this role may access to a FlexoConceptInstance<br>
	 * This data is infered from eventual analyzed type of FMLRTVirtualModelInstance binding
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModelType();

	/*public FMLRTModelSlot<?, ?> getVirtualModelModelSlot();
	
	public void setVirtualModelModelSlot(FMLRTModelSlot<?, ?> modelSlot);*/

	@Override
	public FlexoConceptInstanceType buildType(String serializedType);

	public static abstract class FlexoConceptInstanceRoleImpl extends FlexoRoleImpl<FlexoConceptInstance>
			implements FlexoConceptInstanceRole {

		private static final Logger logger = Logger.getLogger(FlexoConceptInstanceRole.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private String _flexoConceptTypeURI;

		@Deprecated
		private CreationScheme creationScheme;
		@Deprecated
		private String _creationSchemeURI;

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
			if (flexoConceptType == null && _getFlexoConceptTypeURI() != null && getVirtualModelLibrary() != null) {
				flexoConceptType = getVirtualModelLibrary().getFlexoConcept(_getFlexoConceptTypeURI(), false);
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, null, type);
				notifyResultingTypeChanged();
			}
			/*System.out.println("On me demande le type, je renvoie: " + flexoConceptType);
			System.out.println("uri=" + _getFlexoConceptTypeURI());
			System.out.println("getVirtualModelLibrary()" + getVirtualModelLibrary());
			System.out.println("type: " + type + " resolved=" + type.isResolved());
			System.out.println("concept: " + type.getFlexoConcept());*/
			if (!type.isResolved()) {
				type.resolve();
			}
			/*System.out.println("- type: " + type + " resolved=" + type.isResolved());
			System.out.println("- factory=" + type.getCustomTypeFactory());
			System.out.println("- concept: " + type.getFlexoConcept());*/
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				Type oldType = getType();
				String oldValue = _getFlexoConceptTypeURI();
				FlexoConcept oldConceptType = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				/*if (flexoConceptType != null) {
					type = flexoConceptType.getInstanceType();
				}*/
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
					setCreationScheme(null);
				}
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_URI_KEY, oldValue, _getFlexoConceptTypeURI());
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, oldConceptType, getFlexoConceptType());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();
			}
		}

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			if (flexoConceptType == null && _flexoConceptTypeURI != null && getVirtualModelLibrary() != null) {
				flexoConceptType = getVirtualModelLibrary().getFlexoConcept(_flexoConceptTypeURI, true);
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, null, type);
				notifyResultingTypeChanged();
			}
		}

		@Override
		@Deprecated
		public String _getCreationSchemeURI() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getURI();
			}
			return _creationSchemeURI;
		}

		@Override
		@Deprecated
		public void _setCreationSchemeURI(String uri) {
			if (getVirtualModelLibrary() != null) {
				creationScheme = (CreationScheme) getVirtualModelLibrary().getFlexoBehaviour(uri, true);
				/*for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
					s.updateBindingModels();
				}*/
			}
			_creationSchemeURI = uri;
		}

		@Override
		public String _getFlexoConceptTypeURI() {
			if (flexoConceptType != null) {
				return flexoConceptType.getURI();
			}
			if (type != null) {
				if (type.isResolved() && type.getFlexoConcept() != null) {
					return type.getFlexoConcept().getURI();
				}
				else {
					return type.getConceptURI();
				}
			}
			return _flexoConceptTypeURI;
		}

		@Override
		public void _setFlexoConceptTypeURI(String uri) {
			if ((uri == null && _getFlexoConceptTypeURI() != null) || (uri != null && !uri.equals(_getFlexoConceptTypeURI()))) {
				Type oldType = getType();
				String oldValue = _getFlexoConceptTypeURI();
				FlexoConcept oldConceptType = getFlexoConceptType();
				this._flexoConceptTypeURI = uri;
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_URI_KEY, oldValue, uri);
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, oldConceptType, getFlexoConceptType());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();
			}

			if (getDeclaringCompilationUnit() != null) {
				flexoConceptType = getDeclaringCompilationUnit().getFlexoConcept(uri);
			}
			_flexoConceptTypeURI = uri;
		}

		@Override
		@Deprecated
		public CreationScheme getCreationScheme() {
			if (creationScheme == null && _creationSchemeURI != null && getVirtualModelLibrary() != null) {
				creationScheme = (CreationScheme) getVirtualModelLibrary().getFlexoBehaviour(_creationSchemeURI, true);
			}
			return creationScheme;
		}

		@Deprecated
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

		/**
		 * Instanciate run-time-level object encoding reference to object (see {@link ActorReference})
		 * 
		 * @param object
		 *            the object which are pointing to
		 * @param fci
		 *            the {@link FlexoConceptInstance} where this {@link ActorReference} is defined
		 * 
		 */
		@Override
		public ActorReference<? extends FlexoConceptInstance> makeActorReference(FlexoConceptInstance object, FlexoConceptInstance fci) {
			/*AbstractVirtualModelInstanceModelFactory<?> factory = epi.getFactory();
			ModelObjectActorReference<FlexoConceptInstance> returned = factory.newInstance(ModelObjectActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(epi);
			returned.setModellingElement(object);
			return returned;*/
			return object.makeActorReference(this, fci);
		}

		private DataBinding<VirtualModelInstance<?, ?>> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<>(this, VirtualModelInstance.class, DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName("virtualModelInstance");
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName("virtualModelInstance");
				aVirtualModelInstance.setDeclaredType(VirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			if (this.virtualModelInstance != aVirtualModelInstance) {
				this.virtualModelInstance = aVirtualModelInstance;
				this.getPropertyChangeSupport().firePropertyChange("virtualModelInstance", this.virtualModelInstance,
						aVirtualModelInstance);
			}
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getVirtualModelInstance()) {
				getPropertyChangeSupport().firePropertyChange("virtualModelType", null, getVirtualModelType());
				// System.out.println("getVirtualModelInstance() changed");
				// System.out.println("getFlexoConceptType()=" + getFlexoConceptType());
				// System.out.println("getVirtualModelType()=" + getVirtualModelType());
				if (getFlexoConceptType() != null && getFlexoConceptType().getOwner() != null
						&& !getFlexoConceptType().getOwner().isAssignableFrom(getVirtualModelType())) {
					// If existing concept type is not defined in a VirtualModel compatible with the virtual model type accessed by the
					// binding, then nullify existing concept
					setFlexoConceptType(null);
				}

			}
		}

		/**
		 * Return type of VirtualModel where this role may access to a FlexoConceptInstance<br>
		 * This data is infered from eventual analyzed type of FMLRTVirtualModelInstance binding
		 * 
		 * @return
		 */
		@Override
		public VirtualModel getVirtualModelType() {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().isSet() && getVirtualModelInstance().isValid()) {
				Type type = getVirtualModelInstance().getAnalyzedType();
				if (type instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) type).getVirtualModel();
				}
			}
			return null;
		}

		@Override
		public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
			return FMLRTTechnologyAdapter.class;
		}

		// Flag used to avoid stack overflow
		private boolean isHandlingRequiredImports = false;

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			if (isHandlingRequiredImports) {
				return;
			}
			isHandlingRequiredImports = true;
			// super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				compilationUnit.ensureUse(FMLRTVirtualModelInstanceModelSlot.class);
				if (getFlexoConceptType() != null) {
					compilationUnit.ensureResourceImport(getFlexoConceptType().getDeclaringCompilationUnit());
				}
			}
			isHandlingRequiredImports = false;
		}

		@Override
		public FlexoConceptInstanceType buildType(String serializedType) {
			return new FlexoConceptInstanceType(serializedType, getFlexoConceptInstanceTypeFactory());
		}

		private FlexoConceptInstanceType type = FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;

		@Override
		public Type getType() {

			if (flexoConceptType == null) {
				if (type == FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE
						&& StringUtils.isNotEmpty(_getFlexoConceptTypeURI()) && getTechnologyAdapter() != null
						&& getTechnologyAdapter().getFlexoConceptInstanceTypeFactory() != null) {
					type = getTechnologyAdapter().getFlexoConceptInstanceTypeFactory().makeCustomType(_getFlexoConceptTypeURI());
					// getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, null, getFlexoConceptType());
					getPropertyChangeSupport().firePropertyChange(TYPE_KEY, null, type);
					notifyResultingTypeChanged();
				}
				return type;
			}
			return getFlexoConceptType().getInstanceType();
		}

		@Override
		public String getTypeDescription() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getName();
			}
			return "FlexoConcept";
		}

		@Override
		public void setType(Type type) {
			if (type instanceof FlexoConceptInstanceType) {
				Type oldType = getType();
				String oldValue = _getFlexoConceptTypeURI();
				FlexoConcept oldConceptType = getFlexoConceptType();
				flexoConceptType = null;
				_flexoConceptTypeURI = null;
				this.type = (FlexoConceptInstanceType) type;
				/*if (((FlexoConceptInstanceType) type).isResolved()) {
					setFlexoConceptType(((FlexoConceptInstanceType) type).getFlexoConcept());
				}*/
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_URI_KEY, oldValue, _getFlexoConceptTypeURI());
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_TYPE_KEY, oldConceptType, getFlexoConceptType());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();
			}
			else {
				logger.warning("Unexpected type: " + type);
			}
		}

		private FlexoConceptInstanceTypeFactory customTypeFactory;

		/**
		 * Retrieve an internal factory which will be used to resolve type of {@link FlexoConceptInstance}
		 * 
		 * @return
		 */
		private FlexoConceptInstanceTypeFactory getFlexoConceptInstanceTypeFactory() {
			if (customTypeFactory == null) {
				customTypeFactory = new DefaultFlexoConceptInstanceTypeFactory(getTechnologyAdapter()) {
					@Override
					public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
						if (getDeclaringCompilationUnit() != null) {
							for (ElementImportDeclaration elementImportDeclaration : getDeclaringCompilationUnit().getElementImports()) {
								if (elementImportDeclaration.isReferencedObjectLoaded()) {
									if (elementImportDeclaration.getReferencedObject() instanceof FMLCompilationUnit) {
										FMLCompilationUnit referencedCompilationUnit = (FMLCompilationUnit) elementImportDeclaration
												.getReferencedObject();
										if (referencedCompilationUnit.getVirtualModel() != null) {
											FlexoConcept flexoConcept = referencedCompilationUnit.getVirtualModel()
													.getFlexoConcept(typeToResolve.getConceptURI());
											if (flexoConcept != null) {
												if (typeToResolve == type) {
													FlexoConceptInstanceRoleImpl.this.setFlexoConceptType(flexoConcept);
												}
												return flexoConcept;
											}
										}
									}
								}
							}
						}
						return null;

					}
				};
			}
			return customTypeFactory;
		}

	}

	@DefineValidationRule
	public static class VirtualModelInstanceIsRecommandedAndShouldBeValid
			extends BindingIsRecommandedAndShouldBeValid<FlexoConceptInstanceRole> {
		public VirtualModelInstanceIsRecommandedAndShouldBeValid() {
			super("'virtual_model_instance'_binding_is_recommanded_and_should_be_valid", FlexoConceptInstanceRole.class);
		}

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getBinding(FlexoConceptInstanceRole object) {
			return object.getVirtualModelInstance();
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
				return new ValidationWarning<>(this, aRole, "FlexoConceptInstanceRole_should_have_a_type");

			}
			return null;
		}

	}

}
