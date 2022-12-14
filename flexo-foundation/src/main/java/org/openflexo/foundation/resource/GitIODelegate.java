package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openflexo.foundation.resource.GitIODelegate.GitIODelegateImpl;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.factory.PamelaModelFactory;

@ModelEntity
@Imports({ @Import(DirectoryBasedGitIODelegate.class) })
@ImplementationClass(GitIODelegateImpl.class)
public interface GitIODelegate extends FileIODelegate {

	public void setRepository(Repository repository);

	public void setLastCommitId(ObjectId objectId);

	public void setGitObjectId(ObjectId objectId);

	public Repository getRepository();

	public ObjectId getGitObjectId();

	public Git getGit();

	public abstract class GitIODelegateImpl extends FileIODelegateImpl implements GitIODelegate {

		// private static final FileSystemResourceLocatorImpl FS_RESOURCE_LOCATOR = new FileSystemResourceLocatorImpl();

		private File file;
		private ObjectId gitObjectId;
		private ObjectId lastCommitId;
		private Repository repository;
		// private Git git;

		public static GitIODelegate makeFlexoIOGitDelegate(File file, PamelaModelFactory factory, /*File workTree, */ Repository repository) {
			GitIODelegate fileIODelegate = factory.newInstance(GitIODelegate.class);
			// Set the gitRepository linked to this file
			fileIODelegate.setRepository(repository);
			fileIODelegate.setFile(file/*new File(repository.getWorkTree(), name)*/);
			// ((FlexoIOGitDelegateImpl) fileIODelegate).git = new Git(repository);
			return fileIODelegate;
		}

		@Override
		public void save(FlexoResource<?> resource) {
			System.out.println("******* GIT: Saving resource " + resource);

			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			try (Repository repository = resourceCenter.getGitRepository()) {
				// Create the file on the disk in the workTree of the Git Repository
				// setFile(new File(repository.getWorkTree(), resource.getName()));

				/*try {
					getFile().createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
				// Commit the ressource in the repo
				try (Git git = new Git(repository)) {
					System.out.println("Add ressource : " + resource.getName() + " to Git Index of Repository : " + repository.getBranch());
					DirCache inIndex = git.add().addFilepattern(resource.getName()).call();
					System.out.println("inIndex=" + inIndex);
					DirCacheEntry entry = inIndex.getEntry(resource.getName());
					System.out.println("entry=" + entry);
					if (entry != null) {
						setGitObjectId(inIndex.getEntry(resource.getName()).getObjectId());
					}

					System.out.println("Commit ressource : " + resource.getName() + " in Repository : " + repository.getBranch());
					// Unused RevCommit commit =
					git.commit().setMessage("Ressource ViewPoint committed").call();

				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public File getFile() {
			return file;
		}

		@Override
		public void setFile(File file) {
			this.file = file;
		}

		@Override
		public void setRepository(Repository repository) {
			this.repository = repository;
		}

		@Override
		public ObjectId getGitObjectId() {
			return gitObjectId;
		}

		@Override
		public void setGitObjectId(ObjectId gitObjectId) {
			this.gitObjectId = gitObjectId;
		}

		@Override
		public void setLastCommitId(ObjectId lastCommitId) {
			this.lastCommitId = lastCommitId;
		}

		@Override
		public Repository getRepository() {
			return repository;
		}

		@Override
		public File getSerializationArtefact() {
			return getFile();
		}

		@Override
		public boolean delete(boolean deleteFile) {
			/*if (hasWritePermission()) {
				if (getFile() != null && getFile().exists() && deleteFile) {
					getFlexoResource().getServiceManager().getResourceManager().addToFilesToDelete(getFile());
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Will delete file " + getFile().getAbsolutePath() + " upon next save of RM");
					}
				}
				return true;
			}
			else {
				logger.warning("Delete requested for READ-ONLY file resource " + this);
				return false;
			}*/
			return false;
		}

		@Override
		public Git getGit() {
			// return git;
			return null;
		}

		/*@Override
		public FileResourceImpl locateResourceRelativeToParentPath(String relativePathName) {
			File currentFile = new File(getSerializationArtefact().getParentFile(), relativePathName);
			if (currentFile.exists()) {
				return FS_RESOURCE_LOCATOR.retrieveResource(currentFile);
			}
			return null;
		}*/

	}

}
