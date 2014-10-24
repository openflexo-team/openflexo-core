package org.openflexo.foundation.remoteresources;

import java.net.MalformedURLException;
import java.net.URL;

import org.openflexo.toolbox.FlexoVersion;

public class TestViewPointBundle extends DefaultFlexoBundle{

	public TestViewPointBundle(String baseUri){
		try {
			String url1 = baseUri+"openflexo-deps-local/org/openflexo/testdiagram_vp/1.1/testdiagram_vp-1.1.jar";
			getURLs().add(new URL(url1));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public FlexoBundleType getBundleType() {
		return FlexoBundleType.RESOURCE_CENTER;
	}

	@Override
	public FlexoVersion getVersion() {
		return new FlexoVersion("1.1");
	}
	
	@Override
	public String getSimpleName() {
		return "Viewpoint sample";
	}
	
}
