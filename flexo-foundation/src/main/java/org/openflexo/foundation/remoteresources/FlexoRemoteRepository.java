package org.openflexo.foundation.remoteresources;

import java.util.List;

import org.openflexo.toolbox.ImageIconResource;


public interface FlexoRemoteRepository {
	
	public boolean isPrivate();
	
	public String getName();
	
	public void setName(String name);

	public String getUri();
	
	public void setUri(String uri);
	
	public String getProvider();
	
	public List<FlexoBundle> getFlexoBundles();
	
	public ImageIconResource getIcon();

}
	
