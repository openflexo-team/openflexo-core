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
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.openflexo.foundation.resource.DirectoryBasedGitIODelegate.DirectoryBasedGitIODelegateImpl;
import org.openflexo.foundation.resource.GitIODelegate.GitIODelegateImpl;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;

@ModelEntity
@ImplementationClass(GitResourceCenter.GitResourceCenterImpl.class)
public interface GitResourceCenter extends FileSystemBasedResourceCenter {

	public static GitResourceCenter instanciateNewGitResourceCenter(File resourceCenterDirectory, File gitRepository,
			FlexoResourceCenterService rcService) throws IllegalStateException, IOException, GitAPIException {
		GitResourceCenterImpl.logger.info("Instanciate ResourceCenter from " + resourceCenterDirectory.getAbsolutePath());
		PamelaModelFactory factory;
		try {
			factory = new PamelaModelFactory(GitResourceCenter.class);
			GitResourceCenter gitResourceCenter = factory.newInstance(GitResourceCenter.class);
			gitResourceCenter.setBaseArtefact(resourceCenterDirectory);
			gitResourceCenter.setFlexoResourceCenterService(rcService);
			gitResourceCenter.initializeRepositoryGit(gitRepository);
			gitResourceCenter.update();
			gitResourceCenter.startDirectoryWatching();
			return gitResourceCenter;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void initializeRepositoryGit(File gitFile) throws IOException, IllegalStateException, GitAPIException;

	public Repository getGitRepository();

	public IODelegateFactory<File> getGitIODelegateFactory();

	public static abstract class GitResourceCenterImpl extends FileSystemBasedResourceCenterImpl implements GitResourceCenter {
		protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());

		private IODelegateFactory<File> gitIODelegateFactory;

		/**
		 * For a first impl, consider that one GitResourceCenter holds one gitRepository
		 */

		private Repository gitRepository;
		private Git git;
		// .git directory
		private File gitDir;

		public GitResourceCenterImpl(/*File resourceCenterDirectory, File gitRepository, FlexoResourceCenterService rcService*/)
				throws IllegalStateException {
			super();
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

		@Override
		public Repository getGitRepository() {
			return gitRepository;
		}

		@Override
		public IODelegateFactory<File> getGitIODelegateFactory() {
			return gitIODelegateFactory;
		}

		@Override
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
		public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion) throws Exception {
			// TODO Not yet implemented
		}

		@Override
		public void update() throws IOException {
			// TODO Not yet implemented
		}

		@Override
		public GitIODelegate makeFlexoIODelegate(File serializationArtefact, FlexoResourceFactory<?, ?> resourceFactory)
				throws IOException {
			return GitIODelegateImpl.makeFlexoIOGitDelegate(serializationArtefact, resourceFactory, getGitRepository());
		}

		@Override
		public FlexoIODelegate<File> makeDirectoryBasedFlexoIODelegate(File serializationArtefact, String directoryExtension,
				String fileExtension, FlexoResourceFactory<?, ?> resourceFactory) {
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
			logger.warning("Could not retrieve File for ioDelegate=" + ioDelegate);
			return null;
		}
	}
}
