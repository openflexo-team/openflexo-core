package org.openflexo.gitUtils;

public class SerializationArtefactFile implements SerializationArtefactKind {
	private String absolutePath;

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
}
