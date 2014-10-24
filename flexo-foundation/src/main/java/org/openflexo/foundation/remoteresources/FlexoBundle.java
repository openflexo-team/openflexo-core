package org.openflexo.foundation.remoteresources;

import java.net.URL;
import java.util.List;

import org.openflexo.toolbox.FlexoVersion;

public interface FlexoBundle {
	
	public enum FlexoBundleType { RESOURCE_CENTER, TECHNOLOGY_ADAPTER, MODULE };
	
	public FlexoBundleType getBundleType();
	
	public FlexoVersion getVersion();
	
	public List<URL> getURLs();

	public void addToURLs(URL url);

	public void removeFromURLs(URL url);
	
	public String getSimpleName();
	
	public List<FlexoBundle> getBundleDependencies();

}
