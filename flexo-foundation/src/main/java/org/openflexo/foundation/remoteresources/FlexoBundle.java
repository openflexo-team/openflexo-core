package org.openflexo.foundation.remoteresources;

import java.net.URL;
import java.util.List;

import org.openflexo.toolbox.FlexoVersion;

public interface FlexoBundle {
	
	public enum FlexoBundleType { RESOURCE_CENTER, TECHNOLOGY_ADAPTER, MODULE };
	
	public FlexoBundleType getBundleType();
	
	public void setBundleType(FlexoBundleType type);
	
	public FlexoVersion getVersion();
	
	public List<URL> getURLs();

	public void addToURLs(URL url);

	public void removeFromURLs(URL url);
	
	public String getSimpleName();
	
	public void setSimpleName(String name);
	
	public List<FlexoBundle> getBundleDependencies();
}
