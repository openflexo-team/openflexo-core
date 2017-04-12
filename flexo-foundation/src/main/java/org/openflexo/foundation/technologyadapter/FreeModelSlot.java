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

import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FreeModelSlotInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.toolbox.JavaUtils;

/**
 * Implementation of a ModelSlot in a given technology allowing to plug any data source<br>
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FreeModelSlot.FreeModelSlotImpl.class)
public abstract interface FreeModelSlot<RD extends ResourceData<RD> & TechnologyObject<?>> extends ModelSlot<RD> {

	public TechnologyAdapterResource<RD, ?> createProjectSpecificEmptyResource(View view, String filename, String modelUri);

	public TechnologyAdapterResource<RD, ?> createSharedEmptyResource(FlexoResourceCenter<?> resourceCenter, String relativePath,
			String filename, String modelUri);

	public static abstract class FreeModelSlotImpl<RD extends ResourceData<RD> & TechnologyObject<?>> extends ModelSlotImpl<RD>
			implements FreeModelSlot<RD> {

		private static final Logger logger = Logger.getLogger(FreeModelSlot.class.getPackage().getName());

		/**
		 * Instanciate a new model slot instance configuration for this model slot
		 */
		@Override
		public abstract ModelSlotInstanceConfiguration<? extends FreeModelSlot<RD>, RD> createConfiguration(FlexoConceptInstance fci,
				FlexoResourceCenter<?> rc);

		/**
		 * Instantiate a new IndividualRole
		 * 
		 * @param ontClass
		 * @return
		 */
		/*public IndividualRole<?> makeIndividualRole(IFlexoOntologyClass ontClass) {
			Class<? extends IndividualRole> individualPRClass = getFlexoRoleClass(IndividualRole.class);
			IndividualRole<?> returned = makeFlexoRole(individualPRClass);
			returned.setOntologicType(ontClass);
			return returned;
		}*/

		/**
		 * Return a new String (full URI) uniquely identifying a new object in related technology, according to the conventions of related
		 * technology
		 * 
		 * @param msInstance
		 * @param proposedName
		 * @return
		 */
		public String generateUniqueURI(FreeModelSlotInstance msInstance, String proposedName) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return null;
			}
			return msInstance.getResourceURI() + "#" + generateUniqueURIName(msInstance, proposedName);
		}

		/**
		 * Return a new String (the simple name) uniquely identifying a new object in related technology, according to the conventions of
		 * related technology
		 * 
		 * @param msInstance
		 * @param proposedName
		 * @return
		 */
		public String generateUniqueURIName(FreeModelSlotInstance msInstance, String proposedName) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return proposedName;
			}
			return generateUniqueURIName(msInstance, proposedName, msInstance.getResourceURI() + "#");
		}

		public String generateUniqueURIName(FreeModelSlotInstance msInstance, String proposedName, String uriPrefix) {
			if (msInstance == null || msInstance.getResourceData() == null) {
				return proposedName;
			}
			String baseName = JavaUtils.getClassName(proposedName);
			/*
			 * boolean unique = false; int testThis = 0; while (!unique) {
			 * unique = msInstance.getResourceData().getObject(uriPrefix +
			 * baseName) == null; if (!unique) { testThis++; baseName =
			 * proposedName + testThis; } }
			 */
			return baseName;
		}

		@Override
		public abstract TechnologyAdapterResource<RD, ?> createProjectSpecificEmptyResource(View view, String filename, String modelUri);

		@Override
		public abstract TechnologyAdapterResource<RD, ?> createSharedEmptyResource(FlexoResourceCenter<?> resourceCenter,
				String relativePath, String filename, String modelUri);

		@Override
		public final String getURIForObject(ModelSlotInstance msInstance, Object o) {
			return getURIForObject((FreeModelSlotInstance<RD, ? extends FreeModelSlot<RD>>) msInstance, o);
		}

		@Override
		public final Object retrieveObjectWithURI(ModelSlotInstance msInstance, String objectURI) {
			return retrieveObjectWithURI((FreeModelSlotInstance<RD, ? extends FreeModelSlot<RD>>) msInstance, objectURI);
		}

		public abstract String getURIForObject(FreeModelSlotInstance<RD, ? extends FreeModelSlot<RD>> msInstance, Object o);

		public abstract Object retrieveObjectWithURI(FreeModelSlotInstance<RD, ? extends FreeModelSlot<RD>> msInstance, String objectURI);

		@Override
		public String getTypeDescription() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
