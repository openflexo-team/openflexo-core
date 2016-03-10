package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.openflexo.gitUtils.GitIODelegateFactory;
import org.openflexo.gitUtils.IODelegateFactory;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

public class GitResourceCenter extends  FileSystemBasedResourceCenter   {
	
	public GitResourceCenter(File resourceCenterDirectory,File gitRepository) throws IllegalStateException, IOException, GitAPIException {
		super(resourceCenterDirectory);
		initializeRepositoryGit(gitRepository);
		gitIODelegateFactory = new GitIODelegateFactory();
	}
	

	protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());
	
	private IODelegateFactory<File> gitIODelegateFactory;
	
	/**
	 * For a first impl, consider that one GitResourceCenter holds one gitRepository
	 */
	
	private Repository gitRepository; 
	private Git git;
	
	public Repository getGitRepository() {
		return gitRepository;
	}
	
	public IODelegateFactory<File> getGitIODelegateFactory() {
		return gitIODelegateFactory;
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
