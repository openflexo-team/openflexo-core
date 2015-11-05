package org.openflexo.foundation.fml;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Abstract implementation for a {@link CustomType} factory
 * 
 * @author sylvain
 * 
 * @param <T>
 */
public abstract class TechnologyAdapterTypeFactory<T extends CustomType> extends PropertyChangedSupportDefaultImplementation implements
		CustomTypeFactory<T> {

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

	private Resource fibComponentResource = null;

	public Resource getFibComponentResource() {
		if (fibComponentResource == null) {
			Class<?> current = getClass();
			while (fibComponentResource == null && current != null) {
				if (current.getAnnotation(FIBPanel.class) != null) {
					System.out.println("Found annotation " + getClass().getAnnotation(FIBPanel.class));
					String fibPanelName = current.getAnnotation(FIBPanel.class).value();
					fibComponentResource = ResourceLocator.locateResource(fibPanelName);
				}
				current = current.getSuperclass();
			}
		}
		return fibComponentResource;
	}

}