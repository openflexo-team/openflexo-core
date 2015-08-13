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
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fml.rt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
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

	public FlexoModelResource<M, MM, ?, ?> createProjectSpecificEmptyModel(FlexoProject project, String filename, String modelUri,
			FlexoMetaModelResource<M, MM, ?> metaModelResource);

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
	public String generateUniqueURI(TypeAwareModelSlotInstance msInstance, String proposedName);

	/**
	 * Return a new String (the simple name) uniquely identifying a new object in related technology, according to the conventions of
	 * related technology
	 * 
	 * @param msInstance
	 * @param proposedName
	 * @return
	 */
	public String generateUniqueURIName(TypeAwareModelSlotInstance msInstance, String proposedName);

	public String generateUniqueURIName(TypeAwareModelSlotInstance msInstance, String proposedName, String uriPrefix);

	public static abstract class TypeAwareModelSlotImpl<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>>
			extends ModelSlotImpl<M> implements TypeAwareModelSlot<M, MM> {

		private static final Logger logger = Logger.getLogger(TypeAwareModelSlot.class.getPackage().getName());

		private FlexoMetaModelResource<M, MM, ?> metaModelResource;
		private String metaModelURI;

		/**
		 * Instanciate a new model slot instance configuration for this model slot
		 */
		@Override
		public abstract ModelSlotInstanceConfiguration<? extends TypeAwareModelSlot<M, MM>, M> createConfiguration(
				CreateVirtualModelInstance action);

		/**
		 * Return a new String (full URI) uniquely identifying a new object in related technology, according to the conventions of related
		 * technology
		 * 
		 * @param msInstance
		 * @param proposedName
		 * @return
		 */
		@Override
		public String generateUniqueURI(TypeAwareModelSlotInstance msInstance, String proposedName) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return null;
			}
			return msInstance.getModelURI() + "#" + generateUniqueURIName(msInstance, proposedName);
		}

		/**
		 * Return a new String (the simple name) uniquely identifying a new object in related technology, according to the conventions of
		 * related technology
		 * 
		 * @param msInstance
		 * @param proposedName
		 * @return
		 */
		@Override
		public String generateUniqueURIName(TypeAwareModelSlotInstance msInstance, String proposedName) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return proposedName;
			}
			return generateUniqueURIName(msInstance, proposedName, msInstance.getModelURI() + "#");
		}

		@Override
		public String generateUniqueURIName(TypeAwareModelSlotInstance msInstance, String proposedName, String uriPrefix) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return proposedName;
			}
			String baseName = JavaUtils.getClassName(proposedName);
			boolean unique = false;
			int testThis = 0;
			while (!unique) {
				unique = msInstance.getResourceData().getObject(uriPrefix + baseName) == null;
				if (!unique) {
					testThis++;
					baseName = proposedName + testThis;
				}
			}
			return baseName;
		}

		@Override
		public abstract FlexoModelResource<M, MM, ?, ?> createProjectSpecificEmptyModel(FlexoProject project, String filename,
				String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

		@Override
		public abstract FlexoModelResource<M, MM, ?, ?> createSharedEmptyModel(FlexoResourceCenter<?> resourceCenter, String relativePath,
				String filename, String modelUri, FlexoMetaModelResource<M, MM, ?> metaModelResource);

		@Override
		public FlexoMetaModelResource<M, MM, ?> getMetaModelResource() {
			if (metaModelResource == null && StringUtils.isNotEmpty(metaModelURI) && getInformationSpace() != null) {
				metaModelResource = (FlexoMetaModelResource<M, MM, ?>) getInformationSpace().getMetaModelWithURI(metaModelURI,
						getModelSlotTechnologyAdapter());
				logger.info("Looked-up " + metaModelResource + " for " + metaModelURI);
			}
			// Temporary hack to lookup parent slot (to be refactored)
			/*
			 * if (metaModelResource == null && getVirtualModel() != null &&
			 * getViewPoint() != null) { if
			 * (getViewPoint().getModelSlot(getName()) != null) { return
			 * ((TypeSafeModelSlot)
			 * getViewPoint().getModelSlot(getName())).getMetaModelResource(); }
			 * }
			 */
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

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " type=" + getClass().getSimpleName() + " conformTo=\"" + getMetaModelURI() + "\""
					+ " required=" + getIsRequired() + " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
		}

		@Override
		public final String getURIForObject(ModelSlotInstance msInstance, Object o) {
			return getURIForObject((TypeAwareModelSlotInstance<M, MM, ? extends TypeAwareModelSlot<M, MM>>) msInstance, o);
		}

		@Override
		public final Object retrieveObjectWithURI(ModelSlotInstance msInstance, String objectURI) {
			return retrieveObjectWithURI((TypeAwareModelSlotInstance<M, MM, ? extends TypeAwareModelSlot<M, MM>>) msInstance, objectURI);
		}

		public abstract String getURIForObject(TypeAwareModelSlotInstance<M, MM, ? extends TypeAwareModelSlot<M, MM>> msInstance, Object o);

		public abstract Object retrieveObjectWithURI(TypeAwareModelSlotInstance<M, MM, ? extends TypeAwareModelSlot<M, MM>> msInstance,
				String objectURI);

		/**
		 * Return class of models this slot gives access to
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public final Class<? extends FlexoModel<?, ?>> getModelClass() {
			return (Class<? extends FlexoModel<?, ?>>) TypeUtils.getTypeArguments(getClass(), TypeAwareModelSlot.class).get(
					TypeAwareModelSlot.class.getTypeParameters()[0]);
		}

		/**
		 * Return class of models this slot gives access to
		 * 
		 * @return
		 */
		@Override
		@SuppressWarnings("unchecked")
		public final Class<? extends FlexoMetaModel<?>> getMetaModelClass() {
			return (Class<? extends FlexoMetaModel<?>>) TypeUtils.getTypeArguments(getClass(), TypeAwareModelSlot.class).get(
					TypeAwareModelSlot.class.getTypeParameters()[1]);
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

	}
}
