package org.openflexo.foundation.fml;

import org.openflexo.antar.binding.CustomType;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Represents a {@link CustomType} in a given technology, returned by {@link #getSpecificTechnologyAdapter()} method
 * 
 * @author sylvain
 * 
 */
public interface TechnologySpecificCustomType<TA extends TechnologyAdapter> extends CustomType {

	public TA getSpecificTechnologyAdapter();
}
