package org.openflexo.foundation.technologyadapter;

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;

/**
 * A {@link TechnologyAdapterResource} is a {@link FlexoResource} specific to a technology
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity(isAbstract = true)
public interface TechnologyAdapterResource<RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter> extends
		FlexoResource<RD> {
	public static final String TECHNOLOGY_ADAPTER = "technologyAdapter";

	@Getter(value = TECHNOLOGY_ADAPTER, ignoreType = true)
	public TA getTechnologyAdapter();

	@Setter(TECHNOLOGY_ADAPTER)
	public void setTechnologyAdapter(TA technologyAdapter);
}