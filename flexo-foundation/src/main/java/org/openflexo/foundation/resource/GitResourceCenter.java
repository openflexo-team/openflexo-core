/**
 * 
 * Copyright (c) 2015-2016, Openflexo
 * 
 * This file is part of Openflexo-Core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */
package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.openflexo.foundation.resource.DirectoryBasedGitIODelegate.DirectoryBasedGitIODelegateImpl;
import org.openflexo.foundation.resource.GitIODelegate.GitIODelegateImpl;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

public class GitResourceCenter extends FileSystemBasedResourceCenter {

	protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());

	private IODelegateFactory<File> gitIODelegateFactory;

	/**
	 * For a first impl, consider that one GitResourceCenter holds one gitRepository
	 */

	private Repository gitRepository;
	private Git git;
	// .git directory
	private File gitDir;

	public GitResourceCenter(File resourceCenterDirectory, File gitRepository, FlexoResourceCenterService rcService)
			throws IllegalStateException, IOException, GitAPIException {
		super(resourceCenterDirectory, rcService);
		initializeRepositoryGit(gitRepository);
		gitIODelegateFactory = new GitIODelegateFactory();
	}

	/**
	 * needed for sub-classing and steel accessing to JGit Tooling
	 * 
	 * @return
	 */
	public Git getGit() {
		return git;
	}

	@Override
	protected boolean isToBeIgnored(File file) {
		if (super.isToBeIgnored(file)) {
			return true;
		}
		if (file.isDirectory() && file.getName().equals(".git")) {
			return true;
		}
		if (FileUtils.isFileContainedIn(file, gitDir)) {
			return true;
		}
		return false;
	}

	public Repository getGitRepository() {
		return gitRepository;
	}

	public IODelegateFactory<File> getGitIODelegateFactory() {
		return gitIODelegateFactory;
	}

	public void initializeRepositoryGit(File gitFile) throws IOException, IllegalStateException, GitAPIException {
		gitDir = new File(gitFile.getAbsolutePath(), ".git");
		if (!gitDir.exists()) {
			Git.init().setDirectory(gitFile).call();
		}

		// Our GitRepository is of type file
		gitRepository = FileRepositoryBuilder.create(gitDir);
		System.out.println("Created a new repository at " + gitRepository.getDirectory());
		// Where files are checked
		System.out.println("Working tree : " + gitRepository.getWorkTree());
		// Where index is checked
		System.out.println("Index File : " + gitRepository.getIndexFile());
		git = new Git(gitRepository);

		logger.info("New Git Repository Created");
	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		return getAllResources();
	}

	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress) throws Exception {
		// TODO Not yet implemented
	}

	@Override
	public void update() throws IOException {
		// TODO Not yet implemented
	}

	@Override
	public GitIODelegate makeFlexoIODelegate(File serializationArtefact, FlexoResourceFactory<?, ?, ?> resourceFactory) throws IOException {
		return GitIODelegateImpl.makeFlexoIOGitDelegate(serializationArtefact, resourceFactory, getGitRepository());
	}

	@Override
	public FlexoIODelegate<File> makeDirectoryBasedFlexoIODelegate(File serializationArtefact, String directoryExtension,
			String fileExtension, FlexoResourceFactory<?, ?, ?> resourceFactory) {
		String baseName = serializationArtefact.getName().substring(0,
				serializationArtefact.getName().length() - directoryExtension.length());
		return DirectoryBasedGitIODelegateImpl.makeDirectoryBasedFlexoIOGitDelegate(serializationArtefact.getParentFile(), baseName,
				directoryExtension, fileExtension, resourceFactory);
	}

	@Override
	public <R extends FlexoResource<?>> RepositoryFolder<R, File> getRepositoryFolder(FlexoIODelegate<File> ioDelegate,
			ResourceRepository<R, File> resourceRepository) {

		File candidateFile = null;
		if (ioDelegate instanceof DirectoryBasedGitIODelegate) {
			candidateFile = ((DirectoryBasedGitIODelegate) ioDelegate).getDirectory();
		}
		else if (ioDelegate instanceof GitIODelegate) {
			candidateFile = ((GitIODelegate) ioDelegate).getFile();
		}

		if (candidateFile != null) {
			try {
				RepositoryFolder<R, File> returned = resourceRepository.getParentRepositoryFolder(candidateFile, true);
				return returned;
			} catch (IOException e) {
				e.printStackTrace();
				return resourceRepository.getRootFolder();
			}
		}
		else {
			logger.warning("Could not retrieve File for ioDelegate=" + ioDelegate);
			return null;
		}

	}

	/**
	 * Compute and return a default URI for supplied resource<br>
	 * If resource does not provide URI support, this might be delegated to the {@link FlexoResourceCenter} through this method
	 * 
	 * @param resource
	 * @return
	 */
	/*@Override
	public String getDefaultResourceURI(FlexoResource<?> resource) {
		String returned = super.getDefaultResourceURI(resource);
		System.out.println("Pour " + resource.getName() + " uri=" + returned);
	
		if (resource instanceof TechnologyAdapterResource) {
			System.out.println("hop");
			TechnologyAdapter ta = ((TechnologyAdapterResource<?, ?>) resource).getTechnologyAdapter();
			System.out.println("ta=" + ta);
	
			if (ta != null) {
				System.out.println("globalRC=" + ta.getGlobalRepository(this));
				ResourceRepositoryImpl repository = ta.getGlobalRepository(this);
				// for (ResourceRepositoryImpl repository : getRegistedRepositories(ta)) {
				System.out.println("repo:" + repository);
				if (repository.containsResource(resource)) {
					System.out.println("yes");
					String path = "";
					RepositoryFolder f = repository.getRepositoryFolder(resource);
	
					while (f != null && !f.isRootFolder()) {
						path = f.getName() + File.separator + path;
						f = f.getParentFolder();
					}
					String defaultBaseURI = getDefaultBaseURI();
					if (defaultBaseURI.endsWith(File.separator) || defaultBaseURI.endsWith("/")) {
						return getDefaultBaseURI() + path.replace(File.separator, "/") + resource.getName();
					}
					else {
						return getDefaultBaseURI() + "/" + path.replace(File.separator, "/") + resource.getName();
					}
				}
			}
			// }
		}
	
		return null;
	}*/

	/*@Override
	public void setDefaultBaseURI(String defaultBaseURI) {
		System.out.println("ah, je sette le defaultBaseURI a " + defaultBaseURI);
		super.setDefaultBaseURI(defaultBaseURI);
		System.out.println("get=" + getDefaultBaseURI());
	}*/

	@Override
	public String getDisplayableName() {
		return getDefaultBaseURI();
	}

}
