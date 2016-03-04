package org.openflexo.gitUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.GitResourceCenter;
import org.openflexo.toolbox.FlexoVersion;


/**
 * Represents the current workspace of the user, with which resources he wants to use and which version
 * @author kvermeul
 *
 */
public class Workspace {
	Map<FlexoResource<?>, GitVersion> resourcesOnWorking = new HashMap<>();
	
	List<GitResourceCenter> gitResourceCenterList;
	
	public Map<FlexoResource<?>, GitVersion> getResourcesOnWorking() {
		return resourcesOnWorking;
	}
	
	public List<GitResourceCenter> getGitResourceCenterList() {
		return gitResourceCenterList;
	}

	public void setGitResourceCenterList(List<GitResourceCenter> gitResourceCenterList) {
		this.gitResourceCenterList = gitResourceCenterList;
	}
	
	/**
	 * Change the current workspace by checking out the different repositories where the asked resources are located.
	 * @param resourcesToCheckout
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public void checkoutResources(Map<String, FlexoVersion> resourcesToCheckout) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, MissingObjectException, IncorrectObjectTypeException, GitAPIException, IOException{
		resourcesOnWorking.clear();
		
		Map<FlexoResource<?>,GitVersion> returned = new HashMap<>();
		
		for (GitResourceCenter gitResourceCenter : gitResourceCenterList) {
			Map<FlexoResource<?>,FlexoVersion> resourceFoundInGitResourceCenter = new HashMap<>();
			for (FlexoResource<?> resource : gitResourceCenter.getAllResources()) {
				if (resourcesToCheckout.containsKey(resource.getName())){
					resourceFoundInGitResourceCenter.put(resource, resourcesToCheckout.get(resource.getName()));
				}
			}
			returned.putAll(gitResourceCenter.checkoutPick(resourceFoundInGitResourceCenter,null));
		}
		resourcesOnWorking = returned;
	}
	
	
	/**
	 * Commit in the different local repositories the content of the workspace
	 * @param resourcesToSave
	 */
	public void commitWorkspace(Map<FlexoResource<?>, GitVersion> resourcesToSave){
		for (GitResourceCenter gitResourceCenter : gitResourceCenterList) {
			for (FlexoResource<?> resource : gitResourceCenter.getAllResources()) {
				if (resourcesToSave.containsKey(resource)){
					resource.getFlexoIODelegate().save(resource);
				}
			}
		}
	}
	
	
	/**
	 * Push the workspace to remote repositories
	 */
	public void pushWorkspace (){
		for (GitResourceCenter gitResourceCenter : gitResourceCenterList) {
			gitResourceCenter.pushLocalRepository();
		}
	}

	
	/**
	 * Commit the actual state of the workspace in the repository that holds the workspace state file
	 * @param repository
	 */
	public void  saveWorkspace(Repository repository){
		
	}
	
}
