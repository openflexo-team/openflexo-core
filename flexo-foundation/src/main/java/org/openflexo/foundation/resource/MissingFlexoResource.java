package org.openflexo.foundation.resource;


public class MissingFlexoResource {

	private String infos;
	private FlexoResource<?> resource;

	public MissingFlexoResource(String infos,
			FlexoResource<?> resource) {
		super();
		this.infos = infos;
		this.resource = resource;
	}
	public FlexoResource<?> getResource() {
		return resource;
	}
	public void setResource(FlexoResource<?> resource) {
		this.resource = resource;
	}
	
	public String getInfos() {
		return infos;
	}

	public void setInfos(String infos) {
		this.infos = infos;
	}
	
}
