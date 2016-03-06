package org.openflexo.gitUtils;

public class SerializationArtefactDirectory implements SerializationArtefactKind{
	
	private String directorySuffix;
	
	private String coreFileSuffix;
		
	private String absolutePath;
	
	public String getDirectorySuffix() {
		return directorySuffix;
	}

	public void setDirectorySuffix(String directorySuffix) {
		this.directorySuffix = directorySuffix;
	}

	public String getCoreFileSuffix() {
		return coreFileSuffix;
	}

	public void setCoreFileSuffix(String coreFileSuffix) {
		this.coreFileSuffix = coreFileSuffix;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
}
