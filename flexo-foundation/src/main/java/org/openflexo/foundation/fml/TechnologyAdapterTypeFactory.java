package org.openflexo.foundation.fml;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public abstract class TechnologyAdapterTypeFactory<T extends CustomType> implements CustomTypeFactory<T> {

	private final TechnologyAdapter technologyAdapter;

	public TechnologyAdapterTypeFactory(TechnologyAdapter technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	public FlexoServiceManager getServiceManager() {
		return getTechnologyAdapter().getTechnologyAdapterService().getServiceManager();
	}

}