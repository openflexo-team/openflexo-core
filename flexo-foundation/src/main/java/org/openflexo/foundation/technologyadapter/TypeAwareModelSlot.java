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

package org.openflexo.foundation.technologyadapter;

import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Implementation of a ModelSlot in a given technology implementing model conformance.<br>
 * This model slot provides a symbolic access to a model conform to a meta-model (basic conformance contract). <br>
 * 
 * @see FlexoModel
 * @see FlexoMetaModel
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TypeAwareModelSlot.TypeAwareModelSlotImpl.class)
public interface TypeAwareModelSlot<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>>
		extends ModelSlot<M> {

	@PropertyIdentifier(type = String.class)
	public static final String META_MODEL_URI_KEY = "metaModelURI";

	@Getter(value = META_MODEL_URI_KEY)
	@XMLAttribute
	public String getMetaModelURI();

	@Setter(META_MODEL_URI_KEY)
	public void setMetaModelURI(String metaModelURI);

	public FlexoMetaModelResource<M, MM, ?> getMetaModelResource();

	public void setMetaModelResource(FlexoMetaModelResource<M, MM, ?> metaModelResource);

	public Class<? extends FlexoMetaModel<?>> getMetaModelClass();

	public FlexoModelResource<M, MM, ?, ?> createProjectSpecificEmptyModel(FlexoResourceCenter<?> rc, String filename, String relativePath,
			String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

	public FlexoModelResource<M, MM, ?, ?> createSharedEmptyModel(FlexoResourceCenter<?> resourceCenter, String relativePath,
			String filename, String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

	/**
	 * Return a new String (full URI) uniquely identifying a new object in related technology, according to the conventions of related
	 * technology
	 * 
	 * @param msInstance
	 * @param proposedName
	 * @return
	 */
	public String generateUniqueURI(M model, String proposedName);

	/**
	 * Return a new String (the simple name) uniquely identifying a new object in related technology, according to the conventions of
	 * related technology
	 * 
	 * @param msInstance
	 * @param proposedName
	 * @return
	 */
	public String generateUniqueURIName(M model, String proposedName);

	public String generateUniqueURIName(M model, String proposedName, String uriPrefix);

	public boolean isMetaModelRequired();

	public static abstract class TypeAwareModelSlotImpl<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>>
			extends ModelSlotImpl<M> implements TypeAwareModelSlot<M, MM> {

		private static final Logger logger = Logger.getLogger(TypeAwareModelSlot.class.getPackage().getName());

		private FlexoMetaModelResource<M, MM, ?> metaModelResource;
		private String metaModelURI;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public TypeAwareModelSlotInstance<M, MM, ?> makeActorReference(M object, FlexoConceptInstance fci) {

			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			TypeAwareModelSlotInstance returned = factory.newInstance(TypeAwareModelSlotInstance.class);
			returned.setModelSlot(this);
			returned.setFlexoConceptInstance(fci);
			returned.setAccessedResourceData(object);
			return returned;
		}

		/**
		 * Return a new String (full URI) uniquely identifying a new object in related technology, according to the conventions of related
		 * technology
		 * 
		 * @param model
		 * @param proposedName
		 * @return
		 */
		@Override
		public String generateUniqueURI(M model, String proposedName) {
			if (model == null) {
				return null;
			}
			return model.getURI() + "#" + generateUniqueURIName(model, proposedName);
		}

		/**
		 * Return a new String (the simple name) uniquely identifying a new object in related technology, according to the conventions of
		 * related technology
		 * 
		 * @param model
		 * @param proposedName
		 * @return
		 */
		@Override
		public String generateUniqueURIName(M model, String proposedName) {
			if (model == null) {
				return null;
			}
			return generateUniqueURIName(model, proposedName, model.getURI() + "#");
		}

		@Override
		public String generateUniqueURIName(M model, String proposedName, String uriPrefix) {
			if (model == null) {
				return null;
			}
			String baseName = JavaUtils.getClassName(proposedName);
			boolean unique = false;
			int testThis = 0;
			while (!unique) {
				unique = model.getObject(uriPrefix + baseName) == null;
				if (!unique) {
					testThis++;
					baseName = proposedName + testThis;
				}
			}
			return baseName;
		}

		@Override
		public abstract FlexoModelResource<M, MM, ?, ?> createProjectSpecificEmptyModel(FlexoResourceCenter<?> rc, String filename,
				String relativePath, String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

		@Override
		public abstract FlexoModelResource<M, MM, ?, ?> createSharedEmptyModel(FlexoResourceCenter<?> resourceCenter, String relativePath,
				String filename, String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

		@Override
		public FlexoMetaModelResource<M, MM, ?> getMetaModelResource() {
			if (metaModelResource == null && StringUtils.isNotEmpty(metaModelURI) && getServiceManager() != null
					&& getServiceManager().getResourceManager() != null) {
				metaModelResource = (FlexoMetaModelResource<M, MM, ?>) getServiceManager().getResourceManager()
						.getMetaModelWithURI(metaModelURI, getModelSlotTechnologyAdapter());
				logger.info("Looked-up " + metaModelResource + " for " + metaModelURI);
			}
			return metaModelResource;
		}

		@Override
		public void setMetaModelResource(FlexoMetaModelResource<M, MM, ?> metaModelResource) {
			this.metaModelResource = metaModelResource;
		}

		@Override
		public String getMetaModelURI() {
			if (metaModelResource != null) {
				return metaModelResource.getURI();
			}
			return metaModelURI;
		}

		@Override
		public void setMetaModelURI(String metaModelURI) {
			this.metaModelURI = metaModelURI;
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " type=" + getClass().getSimpleName() + " conformTo=\"" + getMetaModelURI() + "\""
					+ " required=" + getIsRequired() + " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
		}*/

		/*@Override
		protected String getFMLAnnotation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("@" + getImplementedInterface().getSimpleName() + "(metamodel=" + '"' + getMetaModelURI() + '"' + ",cardinality="
					+ getCardinality() + ",readOnly=" + isReadOnly() + ")", context);
			if (isKey()) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				out.append("@Key", context);
			}
			return out.toString();
		}*/

		@Override
		public abstract String getURIForObject(M model, Object o);

		@Override
		public abstract Object retrieveObjectWithURI(M model, String objectURI);

		/**
		 * Return class of models this slot gives access to
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public final Class<? extends FlexoModel<?, ?>> getModelClass() {
			return (Class<? extends FlexoModel<?, ?>>) TypeUtils.getTypeArguments(getClass(), TypeAwareModelSlot.class)
					.get(TypeAwareModelSlot.class.getTypeParameters()[0]);
		}

		/**
		 * Return class of models this slot gives access to
		 * 
		 * @return
		 */
		@Override
		@SuppressWarnings("unchecked")
		public final Class<? extends FlexoMetaModel<?>> getMetaModelClass() {
			return (Class<? extends FlexoMetaModel<?>>) TypeUtils.getTypeArguments(getClass(), TypeAwareModelSlot.class)
					.get(TypeAwareModelSlot.class.getTypeParameters()[1]);
		}

		/**
		 * Return flag indicating if this model slot implements a strict meta-modelling contract (return true if and only if a model in this
		 * technology can be conform to only one metamodel). Otherwise, this is simple metamodelling (a model is conform to exactely one
		 * metamodel)
		 * 
		 * @return
		 */
		public abstract boolean isStrictMetaModelling();

		@Override
		public String getModelSlotDescription() {
			return "Model conform to " + getMetaModelURI();
		}

		@Override
		public boolean isMetaModelRequired() {
			return true;
		}

	}

	@DefineValidationRule
	@SuppressWarnings({ "rawtypes" })
	public static class TypeAwareModelSlotMustAddressAValidMetaModel
			extends ValidationRule<TypeAwareModelSlotMustAddressAValidMetaModel, TypeAwareModelSlot> {
		public TypeAwareModelSlotMustAddressAValidMetaModel() {
			super(TypeAwareModelSlot.class, "ModelSlot_($validable.name)_must_address_a_valid_meta_model");
		}

		@Override
		public ValidationIssue<TypeAwareModelSlotMustAddressAValidMetaModel, TypeAwareModelSlot> applyValidation(
				TypeAwareModelSlot modelSlot) {

			if (modelSlot.getMetaModelResource() == null) {
				if (modelSlot.isMetaModelRequired()) {
					return new ValidationError<>(this, modelSlot, "ModelSlot_($validable.name)_doesn't_define_any_meta_model");
				}
				else {
					return new ValidationWarning<>(this, modelSlot, "ModelSlot_($validable.name)_doesn't_define_any_meta_model");
				}
			}

			return null;
		}
	}

}
