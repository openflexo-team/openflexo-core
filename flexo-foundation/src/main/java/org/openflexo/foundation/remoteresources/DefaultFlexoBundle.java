package org.openflexo.foundation.remoteresources;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultFlexoBundle implements FlexoBundle{
	
	private List<URL> urls;
	
	public List<URL> getURLs() {
		if(urls == null ){
			urls = new ArrayList<URL>();
		}
		return urls;
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

}
