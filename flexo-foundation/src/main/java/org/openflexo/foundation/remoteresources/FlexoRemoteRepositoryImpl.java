package org.openflexo.foundation.remoteresources;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.toolbox.ImageIconResource;


public class FlexoRemoteRepositoryImpl implements FlexoRemoteRepository{

	private String name;
	
	private String uri;
	
	private List<FlexoBundle> flexoBundles;
	
	public FlexoRemoteRepositoryImpl(String name, String uri){
		this.name = name;
		this.uri = uri;
		initialize();
	}
	private void initialize(){
		flexoBundles = new ArrayList<FlexoBundle>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUri() {
		return uri;
	}

	public void setUri(String anUri) {
		this.uri = anUri;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public String getProvider() {
		return "NoProvider";
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
		return null;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	

}
	
