package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.toolbox.FlexoVersion;

@ModelEntity
public interface FlexoIOGitDelegate extends FileFlexoIODelegate {

	public void setRepository(Repository repository);

	public Repository getRepository();

	public List<ObjectId> getGitObjectIds();

	public void setGitObjectIds(List<ObjectId> gitObjectIds);

	// Key ==Versions and value == commit
	public Map<FlexoVersion,ObjectId> getGitCommitIds();

	public void setGitCommitIds(Map<FlexoVersion,ObjectId> gitCommitIds);
	
	public Git getGit();
	
	public void createAndSaveIO(FlexoResource<?> resource);

	@Implementation
	public abstract class FlexoIOGitDelegateImpl extends FlexoIOStreamDelegateImpl<File> implements FlexoIOGitDelegate {

		private File file;
		private List<ObjectId> gitObjectIds;
		private Map<FlexoVersion,ObjectId> gitCommitIds;

		public void createAndSaveIO(FlexoResource<?> resource) {
			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			Repository repository = resourceCenter.getGitRepository();
			// Create the file on the disk in the workTree of the Git Repository
			setFile(new File(repository.getWorkTree(), resource.getName()));
			try {
				getFile().createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			save(resource);
		}

		@Override
		public void save(FlexoResource<?> resource) {
			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			Repository repository = resourceCenter.getGitRepository();
			
			// Commit the ressource in the repo
			Git git = new Git(repository);
			if(resource.getVersion()==null){
				resource.setVersion(new FlexoVersion("0.1"));
			}
			else{
				resource.setVersion(FlexoVersion.versionByIncrementing(resource.getVersion(), 0, 1, 0));
			}
			resourceCenter.getGitSaveStrategy().executeStrategy(git, repository, resource);
			git.close();
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		@Override
		public File getSerializationArtefact() {
			return getFile();
		}

		public List<ObjectId> getGitObjectIds() {
			return gitObjectIds;
		}

		public void setGitObjectIds(List<ObjectId> gitObjectIds) {
			this.gitObjectIds = gitObjectIds;
		}

		public Map<FlexoVersion,ObjectId> getGitCommitIds() {
			return gitCommitIds;
		}

		public void setGitCommitIds(Map<FlexoVersion,ObjectId> gitCommitIds) {
			this.gitCommitIds = gitCommitIds;
		}

	}

}
