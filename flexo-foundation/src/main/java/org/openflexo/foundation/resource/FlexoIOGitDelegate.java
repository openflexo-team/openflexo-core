package org.openflexo.foundation.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.openflexo.gitUtils.SerializationArtefactKind;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.toolbox.FlexoVersion;
/**
 * Linked with a flexo resource, it makes it persistent. Contains all versions this resource has.
 * @author kvermeul
 *
 */
@ModelEntity
public interface FlexoIOGitDelegate extends FileFlexoIODelegate {

	public void setRepository(Repository repository);

	public Repository getRepository();

	// Key ==Versions and value == commit
	public Map<FlexoVersion, ObjectId> getGitCommitIds();

	public void setGitCommitIds(Map<FlexoVersion, ObjectId> gitCommitIds);

	public File getDirectory();
	public void setDirectory(File file);

	
	public Git getGit();

	/**
	 * Create a file in the working tree of the repository and commit it
	 * @param resource
	 * @param artefactType TODO
	 */
	//public void createAndSaveIO(FlexoResource<?> resource, SerializationArtefactKind artefactType);
	
	
	/**
	 * Update and write the version in the .version file linked to the resource
	 * @param resource
	 * @param id
	 * @throws IOException
	 */
	public void writeVersion(FlexoResource<?> resource, ObjectId id) throws IOException;

	@Implementation
	public abstract class FlexoIOGitDelegateImpl extends FlexoIOStreamDelegateImpl<File> implements FlexoIOGitDelegate {

		private File file;
		private File resourceVersionFile;
		private Map<FlexoVersion, ObjectId> gitCommitIds;
		private File directory;
		
		
		@Override
		public void createAndSaveIO(FlexoResource<?> resource, SerializationArtefactKind artefactType) {
			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			Repository repository = resourceCenter.getGitRepository();
			// Create the file on the disk in the workTree of the Git Repository
			
			String directoryPath = artefactType.getPath();
			File pathFile = null;
			if( directoryPath==null){
				pathFile = repository.getWorkTree();
			}
			else{
				pathFile = new File(directoryPath);
			}
			
			if(artefactType.equals(SerializationArtefactKind.FILE)){				
				setFile(new File(pathFile, resource.getName()));
				try {
					getFile().createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(artefactType.equals(SerializationArtefactKind.DIRECTORY)){
				String directorySuffix = artefactType.getDirectorySuffix();
				String coreFileSuffix = artefactType.getCoreFileSuffix();
				
				
				setDirectory(new File(pathFile, resource.getName()+directorySuffix));
				getDirectory().mkdir();
				setFile(new File(getDirectory(), resource.getName()+coreFileSuffix));
				try {
					getFile().createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			save(resource);
		}

		@Override
		public void save(FlexoResource<?> resource) {
			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			Repository repository = resourceCenter.getGitRepository();

			// Commit the ressource in the repo
			Git git = new Git(repository);
			try {
				resourceCenter.getGitSaveStrategy().executeStrategy(git, repository, resource);
			} catch (IncorrectObjectTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			git.close();
		}

		public void writeVersion(FlexoResource<?> resource, ObjectId id) throws IOException{
			if(resourceVersionFile==null){
				resourceVersionFile = new File(((GitResourceCenter)resource.getResourceCenter()).getGitRepository().getWorkTree(),resource.getName()+".version");
				resourceVersionFile.createNewFile();
				System.out.println("Absolute Path : "+ resourceVersionFile.getAbsolutePath());
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(resourceVersionFile.getAbsolutePath(),true)));
			writer.println(resource.getVersion()+","+id.getName());
			writer.flush();
			writer.close();
		}
		
		public void setVersion(FlexoResource<?> resource){
			Repository gitRepository = ((GitResourceCenter) resource.getResourceCenter()).getGitRepository();
			FileTreeIterator iter = new FileTreeIterator(gitRepository);
			while (!iter.eof()){
				
			}
		}
		
		public void setDirectory(File directory){
			this.directory = directory;
		}
		
		public File getDirectory(){
			return directory;
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

		public Map<FlexoVersion, ObjectId> getGitCommitIds() {
			return gitCommitIds;
		}

		public void setGitCommitIds(Map<FlexoVersion, ObjectId> gitCommitIds) {
			this.gitCommitIds = gitCommitIds;
		}

	}

}
