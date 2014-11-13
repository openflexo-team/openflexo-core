package org.openflexo.rm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoaded;
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
	
	private List<ConflictedResourceSet> conflictedResourceSets;
	
	public ResourceConsistencyService() {
	}
	
	@Override
	public void initialize() {
		// Initialize current conflicted resource set
		conflictedResourceSets = getConflictedResourceSets();
		informOfConflictedResourceSets();
	}

	/**
	 * Receive a ntification that a new resource has changed.
	 * It send a message if a conflict exists or resource not complete
	 */
	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (notification instanceof ResourceUnregistered){
			ResourceUnregistered unRegistered = (ResourceUnregistered)notification;
			informOfConflictedResourceSets();
			if(unRegistered.newValue() instanceof ViewResourceImpl){
				ViewResourceImpl viewResource = (ViewResourceImpl)unRegistered.newValue();
				if(viewResource.getViewPointResource()==null){
					informOfViewPointMissing(viewResource);
				}
			}
		}else if(notification instanceof ResourceRegistered){
			ResourceRegistered registered = (ResourceRegistered)notification;
			informOfConflictedResourceSets();
			if(registered.newValue() instanceof ViewResourceImpl){
				ViewResourceImpl viewResource = (ViewResourceImpl)registered.newValue();
				if(viewResource.getViewPointResource()==null){
					informOfViewPointMissing(viewResource);
				}
			}
		}
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
			// If there is many resource with the same URI
			if(multipleResourcesWithSameURI(resource)){
				// If a set with this URI exists, add this resource to this set
				ConflictedResourceSet conflictedSet = getConflictedResourceSet(resource.getURI());
				// Otherwize create a new set
				if(conflictedSet==null){
					conflictedResourceSets.add(new ConflictedResourceSet(resource));
				}else if(!conflictedSet.getConflictedResources().contains(resource)){
					conflictedSet.getConflictedResources().add(resource);
				}
			}
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
	
	public void removeConflictedResourceSet(ConflictedResourceSet conflictedResourceSet){
		conflictedResourceSets.remove(conflictedResourceSet);
	}
	
	private void informOfConflictedResourceSets(){
		if(getNumberOfConflicts()>0){
			if(FlexoController.confirm(Integer.toString(getNumberOfConflicts()) + " URI conflicts have been found. Would you like to manage them?")){
				// Browse all set of conflicted resources
				List<ConflictedResourceSet> resourcesToRemove = new ArrayList<ConflictedResourceSet>();
				for(ConflictedResourceSet conflictedResourceSet : getConflictedResourceSets()){
					manageConflictedResourceSet(conflictedResourceSet);
					resourcesToRemove.add(conflictedResourceSet);
				}
				for(ConflictedResourceSet conflictedResourceSet :  resourcesToRemove){
					removeConflictedResourceSet(conflictedResourceSet);
				}
			}
		}
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