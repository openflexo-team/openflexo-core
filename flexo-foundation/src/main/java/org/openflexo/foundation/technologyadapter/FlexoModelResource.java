/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;

/**
 * A {@link FlexoModelResource} is a {@link FlexoResource} specific to a technology and storing a model conform to a metamodel
 * 
 * @see FlexoMetaModelResource
 * @author sylvain
 * 
 * @param <M>
 *            type of model being handled as resource data
 * @param <MM>
 *            type of metamodel
 * @param <TA>
 *            type of {@link TechnologyAdapter} handling this conforming pattern
 */
@ModelEntity(isAbstract = true)
public interface FlexoModelResource<M extends FlexoModel<M, MM> & TechnologyObject<TA>, MM extends FlexoMetaModel<MM> & TechnologyObject<TAMM>, TA extends TechnologyAdapter, TAMM extends TechnologyAdapter>
		extends TechnologyAdapterResource<M, TA> {

	public static final String META_MODEL_RESOURCE = "metaModelResource";

	@Getter(value = META_MODEL_RESOURCE, ignoreType = true)
	public FlexoMetaModelResource<M, MM, TAMM> getMetaModelResource();

	@Setter(META_MODEL_RESOURCE)
	public void setMetaModelResource(FlexoMetaModelResource<M, MM, TAMM> aMetaModelResource);

	/**
	 * Return the model this resource is storing (same as {@link #getModel()}
	 * 
	 * @return
	 */
	public M getModelData();

	/**
	 * Return the model this resource is storing (same as {@link #getModelData()}
	 * 
	 * @return
	 */
	public M getModel();

	/**
	 * Return flag indicating in this model resource appear to conform to supplied meta-model resource. Assertion is performed, that
	 * execution of this method should not cause any of both resource to be loaded (lazy evalution method). If strong checking is required,
	 * prefer to use {@link #isConformTo(FlexoMetaModelResource)} method.
	 * 
	 * 
	 * @param aMetaModelResource
	 * @return
	 */
	// public boolean appearToConformTo(FlexoMetaModelResource<M, MM> aMetaModelResource);

	/**
	 * Return flag indicating in this model resource appear to conform to supplied meta-model resource.<br>
	 * As strong checking is required, the loading of the resources might be necessary
	 * 
	 * 
	 * @param aMetaModelResource
	 * @return
	 */
	// public boolean isConformTo(FlexoMetaModelResource<M, MM> aMetaModelResource);

}
