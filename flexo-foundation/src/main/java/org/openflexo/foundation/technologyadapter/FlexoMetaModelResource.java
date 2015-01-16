package org.openflexo.foundation.technologyadapter;

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.model.annotations.ModelEntity;

/**
 * A {@link FlexoMetaModelResource} is a {@link FlexoResource} specific to a technology and storing a metamodel
 * 
 * @see FlexoModelResource
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
public interface FlexoMetaModelResource<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<TA>, TA extends TechnologyAdapter>
		extends TechnologyAdapterResource<MM, TA> {

	public MM getMetaModelData();
}