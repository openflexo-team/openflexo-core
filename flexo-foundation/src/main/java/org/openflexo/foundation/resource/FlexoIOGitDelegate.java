package org.openflexo.foundation.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openflexo.gitUtils.SerializationArtefactDirectory;
import org.openflexo.gitUtils.SerializationArtefactFile;
import org.openflexo.gitUtils.SerializationArtefactKind;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.toolbox.FlexoVersion;

import com.google.common.collect.Lists;
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

	public SerializationArtefactKind getSerializationArtefactKind();

	public boolean isSerialized();
	
	public void setSerializationArtefactKind(SerializationArtefactKind serializationArtefactKind);
	
	public File getResourceVersionFile();

	public void setResourceVersionFile(File resourceVersionFile);
	
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

	/**
	 * Get the highest version available for increment the versionning
	 * @return
	 */
	public FlexoVersion getHighestVersion();

	
	@Implementation
	public abstract class FlexoIOGitDelegateImpl extends FlexoIOStreamDelegateImpl<File> implements FlexoIOGitDelegate {

		
		private boolean isSerialized;
		private File file;
		private File resourceVersionFile;
		

		private Map<FlexoVersion, ObjectId> gitCommitIds;
		private File directory;
		
		private SerializationArtefactKind serializationArtefactKind;
		

		
		
		@Override
		public void save(FlexoResource<?> resource) {

			GitResourceCenter resourceCenter = (GitResourceCenter) resource.getResourceCenter();
			Repository repository = resourceCenter.getGitRepository();
			
			//Create the file linked with the resource if the file does not exist
			if(!isSerialized){				
				
				if(serializationArtefactKind instanceof SerializationArtefactFile ){				
					setFile(new File(((SerializationArtefactFile) serializationArtefactKind).getAbsolutePath()));
					try {
						getFile().createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if(serializationArtefactKind instanceof SerializationArtefactDirectory){
					SerializationArtefactDirectory sad = (SerializationArtefactDirectory) serializationArtefactKind;
					String directorySuffix = sad.getDirectorySuffix();
					String coreFileSuffix = sad.getCoreFileSuffix();
					
					
					setDirectory(new File(sad.getAbsolutePath(), resource.getName()+directorySuffix));
					getDirectory().mkdir();
					setFile(new File(getDirectory(), resource.getName()+coreFileSuffix));
					try {
						getFile().createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				isSerialized = true;
			}
			
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
				if(serializationArtefactKind instanceof SerializationArtefactFile  ){
					SerializationArtefactFile saf = (SerializationArtefactFile) serializationArtefactKind;
					resourceVersionFile = new File(saf.getAbsolutePath()+".version");
					resourceVersionFile.createNewFile();					
				}
				else if(serializationArtefactKind instanceof SerializationArtefactDirectory ){
					SerializationArtefactDirectory sad = (SerializationArtefactDirectory) serializationArtefactKind;
					resourceVersionFile = new File(sad.getAbsolutePath()+".version");
					resourceVersionFile.createNewFile();
				}
				System.out.println("Absolute Path version File: "+ resourceVersionFile.getAbsolutePath());
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(resourceVersionFile.getAbsolutePath(),true)));
			writer.println(resource.getVersion()+","+id.getName());
			writer.flush();
			writer.close();
		}
		
		public FlexoVersion getHighestVersion() {
			FlexoVersion highestVersion=null;
			for (FlexoVersion versionAvailable : gitCommitIds.keySet()) {
				if( highestVersion==null){
					highestVersion=versionAvailable;
				}
				else {
					if(versionAvailable.isGreaterThan(highestVersion)){
						highestVersion =versionAvailable;
					}
					
				}
			}
			return highestVersion;
		}

		
		@Override 
		public List<FlexoVersion> getAvailableVersions(){
			return Lists.newArrayList(gitCommitIds.keySet());
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

		public SerializationArtefactKind getSerializationArtefactKind() {
			return serializationArtefactKind;
		}

		public void setSerializationArtefactKind(SerializationArtefactKind serializationArtefactKind) {
			this.serializationArtefactKind = serializationArtefactKind;
		}

		public boolean isSerialized() {
			return isSerialized;
		}
		public File getResourceVersionFile() {
			return resourceVersionFile;
		}

		public void setResourceVersionFile(File resourceVersionFile) {
			this.resourceVersionFile = resourceVersionFile;
		}
	}


}
