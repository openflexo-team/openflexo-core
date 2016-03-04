package org.openflexo.gitUtils;

public enum SerializationArtefactKind {
	FILE, DIRECTORY;
	
	private String name;
	
	private String directorySuffix;
	
	private String coreFileSuffix;

	private String directoryPath;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public String getPath() {
		return directoryPath;
	}

	public void setPath(String path) {
		this.directoryPath = path;
	}
	
}
