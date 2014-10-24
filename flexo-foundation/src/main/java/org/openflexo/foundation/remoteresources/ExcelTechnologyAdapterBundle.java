package org.openflexo.foundation.remoteresources;

import java.net.MalformedURLException;
import java.net.URL;

import org.openflexo.toolbox.FlexoVersion;

public class ExcelTechnologyAdapterBundle extends DefaultFlexoBundle{

	public ExcelTechnologyAdapterBundle(String baseUri){
		try {
			String url1 = "openflexo-release/org/openflexo/excelconnector/1.7.0-beta/excelconnector-1.7.0-beta.jar";
			String url2 = "openflexo-release/org/openflexo/excelconnector-ui/1.7.0-beta/excelconnector-ui-1.7.0-beta.jar";
			getURLs().add(new URL(baseUri+url1));
			getURLs().add(new URL(baseUri+url2));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public FlexoBundleType getBundleType() {
		return FlexoBundleType.TECHNOLOGY_ADAPTER;
	}

	@Override
	public FlexoVersion getVersion() {
		return new FlexoVersion("1.7.0");
	}

	@Override
	public String getSimpleName() {
		return "Excel Technology Adaptor";
	}
	
}
