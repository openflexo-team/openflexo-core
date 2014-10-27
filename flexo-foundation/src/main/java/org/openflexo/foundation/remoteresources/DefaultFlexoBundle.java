package org.openflexo.foundation.remoteresources;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.toolbox.FlexoVersion;

public class DefaultFlexoBundle implements FlexoBundle{
	
	private List<URL> urls;
	
	private String simpleName;
	
	private FlexoBundleType bundleType;
	
	public List<URL> getURLs() {
		if(urls == null ){
			urls = new ArrayList<URL>();
		}
		return urls;
	}
	
	public DefaultFlexoBundle(){
		
	}

	public void addToURLs(URL url) {
		getURLs().add(url);
	}

	public void removeFromURLs(URL url) {
		getURLs().remove(url);
	}
	
	public List<FlexoBundle> getBundleDependencies(){
		return null;
	}

	@Override
	public FlexoBundleType getBundleType() {
		return bundleType;
	}

	@Override
	public FlexoVersion getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public void setBundleType(FlexoBundleType type) {
		this.bundleType = type;
	}

	@Override
	public void setSimpleName(String name) {
		this.simpleName = name;
	}

}
