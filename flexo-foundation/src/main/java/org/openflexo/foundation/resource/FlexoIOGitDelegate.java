package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.factory.ModelFactory;

@ModelEntity
public interface FlexoIOGitDelegate extends FileFlexoIODelegate {

	public void setRepository(Repository repository);

	public void setLastCommidId(ObjectId objectId);

	public void setGitObjectId(ObjectId objectId);

	public Repository getRepository();

	public ObjectId getGitObjectId();
	
	public Git getGit();

	@Implementation
	public abstract class FlexoIOGitDelegateImpl extends FlexoIOStreamDelegateImpl<File> implements FlexoIOGitDelegate {

		private File file;
		private ObjectId gitObjectId;
		private ObjectId lastCommitId;
		private Repository repository;

		public static FlexoIOGitDelegate makeFlexoIOGitDelegate(String name, ModelFactory factory, File workTree,
				Repository repository) throws IOException {
			FlexoIOGitDelegate fileIODelegate = factory.newInstance(FlexoIOGitDelegate.class);

			
			fileIODelegate.setRepository(repository);

			// Create the file on the disk in the workTree of the Git Repository
			fileIODelegate.setFile(new File(workTree, name));

			try {
				fileIODelegate.getFile().createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Commit the ressource in the repo
			Git git = new Git(repository);
			try {
				System.out.println("Add ressource : "+name+" to Git Index of Repository : "+repository.getBranch() );
				DirCache inIndex = git.add().addFilepattern(name).call();
				fileIODelegate.setGitObjectId(inIndex.getEntry(name).getObjectId());

				System.out.println("Commit ressource : "+name+" in Repository : "+repository.getBranch() );
				RevCommit commit = git.commit().setMessage("Ressource ViewPoint committed").call();

			} catch (GitAPIException e) {
				e.printStackTrace();
			}
			git.close();

			// Set the gitRepository linked to this file
			
			return fileIODelegate;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public void setRepository(Repository repository) {
			this.repository = repository;
		}

		public ObjectId getGitObjectId(){
			return gitObjectId;
		}
			
		public void setGitObjectId(ObjectId gitObjectId) {
			this.gitObjectId = gitObjectId;
		}

		public void setLastCommitId(ObjectId lastCommitId) {
			this.lastCommitId = lastCommitId;
		}

		public Repository getRepository() {
			return repository;
		}

		@Override
		public File getSerializationArtefact() {
			return getFile();
		}

	}

}
