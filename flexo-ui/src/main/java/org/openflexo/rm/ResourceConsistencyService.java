package org.openflexo.rm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.ResourceRegistered;
import org.openflexo.foundation.resource.ResourceUnregistered;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.view.rm.ViewResourceImpl;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * This service performs an analysis of existing resources and provides to the user tools to 
 * make resources to be consistent.
 * @author Vincent
 *
 */
public class ResourceConsistencyService extends FlexoServiceImpl {
	
	// The whole set of conflicts
	private List<ConflictedResourceSet> conflictedResourceSets;
	
	private List<ViewResource> viewsWithoutViewpoint;
	
	public ResourceConsistencyService() {
	}
	
	@Override
	public void initialize() {
		// Initialize current conflicted resource set
		conflictedResourceSets = getConflictedResourceSets();
		// Inform the user of conflicts
		informOfConflictedResourceSets(conflictedResourceSets);
		
		viewsWithoutViewpoint = new ArrayList<ViewResource>();
	}
	
	/**
	 * Receive a ntification that a new resource has changed.
	 * It send a message if a conflict exists or resource not complete
	 */
	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		// A resource is unregistered or registered
		if (notification instanceof ResourceUnregistered){
			ResourceUnregistered unRegistered = (ResourceUnregistered)notification;
			checkProblems(unRegistered.newValue());
		}else if(notification instanceof ResourceRegistered){
			ResourceRegistered registered = (ResourceRegistered)notification;
			checkProblems(registered.newValue());
		}
	}
	
	private void checkProblems(Object value){
		if(value instanceof FlexoResource<?>){
			informOfURIConflict((FlexoResource<?>) value);
		}
		if(value instanceof ViewResourceImpl){
			informOfViewpointMissing((ViewResourceImpl) value);
		}
	}
	
	private void informOfURIConflict(FlexoResource<?> resource){
		ConflictedResourceSet conflict = getConflictedResourceSet(resource);
		if(conflict!=null){
			informOfConflictedResourceSet(conflict);
		}
	}
	
	/**
	 * Infor of a viewpoint missing for a view. Inform once.
	 * @param viewResource
	 */
	private void informOfViewpointMissing(ViewResourceImpl viewResource){
		if(viewResource.getViewPointResource()==null && !viewsWithoutViewpoint.contains(viewResource)){
			informOfViewPointMissing(viewResource);
			viewsWithoutViewpoint.add(viewResource);
		}
	}
	
	private ConflictedResourceSet getConflictedResourceSet(FlexoResource<?> resource){
		// If there is many resource with the same URI
		if(multipleResourcesWithSameURI(resource)){
			// If a set with this URI exists, add this resource to this set
			ConflictedResourceSet conflictedSet = getConflictedResourceSet(resource.getURI());
			// Otherwize create a new set
			if(conflictedSet==null){
				conflictedSet = new ConflictedResourceSet(getResources(resource.getURI()));
				conflictedResourceSets.add(conflictedSet);
			}else if(!conflictedSet.getConflictedResources().contains(resource)){
				conflictedSet.getConflictedResources().add(resource);
			}
			return conflictedSet;
		}
		return null;
	}
	
	/**
	 * Get the set of conflicted sets
	 * @return
	 */
	public List<ConflictedResourceSet> getConflictedResourceSets(){
		if(conflictedResourceSets == null){
			conflictedResourceSets = new ArrayList<ConflictedResourceSet>();
		}
		// Browse all resources
		for(FlexoResource<?> resource : getResourceManager().getRegisteredResources()){
			getConflictedResourceSet(resource);
		}
		return conflictedResourceSets;
	}
	
	/**
	 * Get a conflicted set of resource, which share the same URI
	 * @param uri
	 * @return
	 */
	private ConflictedResourceSet getConflictedResourceSet(String uri){
		for(ConflictedResourceSet conflictedResourceSet : conflictedResourceSets){
			if(conflictedResourceSet.getCommonUri().equals(uri)){
				return conflictedResourceSet;
			}
		}
		return null;
	}
	
	/**
	 * Inform that a viewpoint is missing
	 * @param resource
	 */
	private void informOfViewPointMissing(ViewResourceImpl resource){
		if(resource!=null){
			FlexoController.notify(
					 "<html> "
					+ "<h4>Viewpoint resources is missing!</h4>"
						+ "View " + resource.getURI() +" requires \nViewpoint: " + resource.viewpointURI
					+ "\nPlease add resources in resource centers and restart Openflexo");
		}
	}
	
	//TODO
	/*public void removeConflictedResourceSet(ConflictedResourceSet conflictedResourceSet){
		conflictedResourceSets.remove(conflictedResourceSet);
	}*/
	
	private void informOfConflictedResourceSets(List<ConflictedResourceSet> conflicts){
		if(conflicts.size()>0){
			StringBuilder conflictsInformation = new StringBuilder();
			for(ConflictedResourceSet conflict : conflicts){
				conflictsInformation.append("URI : " + conflict.getCommonUri() +" is owned by \n");
				for(FlexoResource<?> fr : conflict.getConflictedResources()){
					conflictsInformation.append(fr.getRelativePath());
				}
			}
			FlexoController.notify(Integer.toString(conflicts.size()) + " URI conflicts have been found:\n" +
					conflictsInformation.toString());
		}
	}
	
	private void informOfConflictedResourceSet(ConflictedResourceSet conflict){
		StringBuilder sb = new StringBuilder();
		sb.append("URI : " + conflict.getCommonUri() +" is owned by \n");
		for(FlexoResource<?> fr : conflict.getConflictedResources()){
			if(fr.getFlexoIODelegate() instanceof FileFlexoIODelegate){
				sb.append(((FileFlexoIODelegate)fr.getFlexoIODelegate()).getFile().getAbsolutePath());
			}else{
				sb.append(fr.getFlexoIODelegate().toString());
			}
			sb.append("\n");
		}
		FlexoController.notify(" URI conflicts have been found:\n" + sb.toString());
	}
	
	/**
	 * Get a set of resources with a uri
	 * @param resourceURI
	 * @return
	 */
	private List<FlexoResource<?>> getResources(String resourceURI) {
		List<FlexoResource<?>> flexoResources = new ArrayList<FlexoResource<?>>();
		if (StringUtils.isEmpty(resourceURI)) {
			return null;
		}
		for (FlexoResource<?> r : getResourceManager().getRegisteredResources()) {
			if (resourceURI.equals(r.getURI())) {
				flexoResources.add(r);
			}
		}
		return flexoResources;
	}
	
	public boolean multipleResourcesWithSameURI(FlexoResource<?> resource){
		if(getResources(resource.getURI()).size()>1){
			return true;
		}
		return false;
	}
	
	private void manageConflictedResourceSet(ConflictedResourceSet resources){
		ConflictedResourceEditorDialog.showResourceConsistencyEditorDialog(resources,this, FlexoFrame.getActiveFrame());
	}
	
	private void manageMissingResources(MissingFlexoResource missingResource){
		ResourceMissingEditorDialog.showResourceMissingEditorDialog(missingResource, this, FlexoFrame.getActiveFrame());
	}
	
	public ResourceManager getResourceManager(){
		return getServiceManager().getResourceManager();
	}
	
	public int getNumberOfConflicts(){
		return conflictedResourceSets.size();
	}
	
}