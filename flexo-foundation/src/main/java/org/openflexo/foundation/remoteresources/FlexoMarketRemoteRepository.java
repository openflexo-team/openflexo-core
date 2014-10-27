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
	
