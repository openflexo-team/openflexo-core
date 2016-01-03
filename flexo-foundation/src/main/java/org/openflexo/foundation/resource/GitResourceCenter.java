package org.openflexo.foundation.resource;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProperty;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.ViewPointRepository;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate.DirectoryBasedFlexoIODelegateImpl;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.validation.Validable;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

public class GitResourceCenter extends  FileSystemBasedResourceCenter   {
	
	public GitResourceCenter(File resourceCenterDirectory,File gitRepository) throws IllegalStateException, IOException, GitAPIException {
		super(resourceCenterDirectory);
		initializeRepositoryGit(gitRepository);
	}
	

	protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());
	
	private FileRepositoryBuilder builder = new FileRepositoryBuilder();
	

	/**
	 * For a first impl, consider that one GitResourceCenter holds one gitRepository
	 */
	
	private Repository gitRepository; 
	private Git git;
	
	public Repository getGitRepository() {
		return gitRepository;
	}
	
	public void initializeRepositoryGit(File gitFile) throws IOException, IllegalStateException, GitAPIException {
		File gitDir = new File(gitFile.getAbsolutePath(), ".git");
		Git.init().setDirectory(gitFile).call();
		
		//Our GitRepository is of type file
		gitRepository = FileRepositoryBuilder.create(gitDir);
		System.out.println("Created a new repository at " + gitRepository.getDirectory());
		//Where files are checked
		System.out.println("Working tree : "+ gitRepository.getWorkTree());
		//Where index is checked
		System.out.println("Index File : "+gitRepository.getIndexFile());
		git = new Git(gitRepository);
		
		logger.info("New Git Repository Created");
	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDefaultBaseURI() {
		// TODO Auto-generated method stub
		return null;
	}	

	
}
