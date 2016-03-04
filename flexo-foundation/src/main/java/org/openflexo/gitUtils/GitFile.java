package org.openflexo.gitUtils;

import java.io.File;

import org.openflexo.toolbox.FlexoVersion;

public class GitFile {
	private FlexoVersion version;
	private File fileInRepository;
	public FlexoVersion getVersion() {
		return version;
	}
	public void setVersion(FlexoVersion version) {
		this.version = version;
	}
	public File getFileInRepository() {
		return fileInRepository;
	}
	public void setFileInRepository(File fileInRepository) {
		this.fileInRepository = fileInRepository;
	}
	
	public GitFile(FlexoVersion version, File fileInrepository){
		this.version=version;
		this.fileInRepository = fileInrepository;
	}
	
}
