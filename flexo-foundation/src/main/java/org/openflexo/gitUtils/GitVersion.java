package org.openflexo.gitUtils;

import org.eclipse.jgit.lib.Repository;
import org.openflexo.toolbox.FlexoVersion;

public class GitVersion {
	private FlexoVersion flexoVersion;
	private Repository repository;

	public GitVersion(){
		
	}
	
	
	public GitVersion(FlexoVersion version, Repository repo){
		flexoVersion = version;
		repository = repo;
	}
	
	public FlexoVersion getFlexoVersion() {
		return flexoVersion;
	}

	public void setFlexoVersion(FlexoVersion flexoVersion) {
		this.flexoVersion = flexoVersion;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEquals = false;
		GitVersion versionToCompare = null;
		try{
			versionToCompare = (GitVersion) obj;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		if (versionToCompare.getFlexoVersion().equals(flexoVersion)&&versionToCompare.getRepository().getDirectory().getAbsolutePath().equals(repository.getDirectory().getAbsolutePath())){
			isEquals = true;
		}
		
		return isEquals;
	}

}
