package org.openflexo.foundation.fml;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Abstract implementation for a {@link CustomType} factory
 * 
 * @author sylvain
 * 
 * @param <T>
 */
public abstract class TechnologyAdapterTypeFactory<T extends CustomType, TA extends TechnologyAdapter<TA>>
		extends PropertyChangedSupportDefaultImplementation implements CustomTypeFactory<T> {

	private final TA technologyAdapter;

	public TechnologyAdapterTypeFactory(TA technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	public FlexoServiceManager getServiceManager() {
		return getTechnologyAdapter().getTechnologyAdapterService().getServiceManager();
	}

}
