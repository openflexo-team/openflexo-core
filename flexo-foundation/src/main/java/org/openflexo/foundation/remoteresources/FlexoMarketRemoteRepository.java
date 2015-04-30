/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.remoteresources;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ImageIconResource;


public class FlexoMarketRemoteRepository extends FlexoRemoteRepositoryImpl{

	private final String name;
	
	private String uri;
	
	private static String provider = "OPENFLEXO";
	
	private List<FlexoBundle> flexoBundles;
	
	public final ImageIconResource FLEXO_MARKET16x16_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Market/flexo_market_16x16.png"));
	
	public FlexoMarketRemoteRepository(String name, String uri){
		super(name,uri);
		this.name = name;
		this.uri = "https://maven.openflexo.org/artifactory/";
		initialize();
	}
	private void initialize(){
		
		// TODO Retrieve from the remote repositories the bundles
		/*URL url;
		try {
			url = new URL(uri+"api/storage/openflexo-deps-local?list&deep=1&listFolders=1&mdTimestamps=1");
			String result = HTTPFileUtils.getURL(url, "",true);
			System.out.println(result);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		getFlexoBundles().add(new ExcelTechnologyAdapterBundle(uri));
		getFlexoBundles().add(new TestViewPointBundle(uri));
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUri() {
		return uri;
	}

	public void setURI(String anURI) {
		this.uri = anURI;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public String getProvider() {
		return provider;
	}

	@Override
	public List<FlexoBundle> getFlexoBundles() {
		if(flexoBundles == null){
			flexoBundles = new ArrayList<FlexoBundle>();
		}
		return flexoBundles;
	}

	@Override
	public ImageIconResource getIcon() {
		return FLEXO_MARKET16x16_ICON;
	}
	

}
	
