package org.openflexo.foundation.remoteresources;

import java.util.List;

import org.openflexo.toolbox.ImageIconResource;


public interface FlexoRemoteRepository {
	
	public boolean isPrivate();
	
	public String getName();

	public String getURI();
	
	public String getProvider();
	
	public List<FlexoBundle> getFlexoBundles();
	
	public ImageIconResource getIcon();

}
	
