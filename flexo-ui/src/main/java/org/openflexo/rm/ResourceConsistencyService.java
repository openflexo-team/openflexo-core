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
	
	// List<FlexoResource<?>> resourcesNotComplete;
	
	@Override
	public void initialize() {
		// Initialize current conflicted resource set
		conflictedResourceSets = getConflictedResourceSets();
		informOfConflictedResourceSets();
		//resourcesNotComplete = getResourceNotComplete();
		//updateResourcesNotComplete();
	}
	
	/*public List<FlexoResource<?>> getResourceNotComplete(){
		resourcesNotComplete = new ArrayList<FlexoResource<?>>();
		// Browse all resources
		for(FlexoResource<?> resource : getResourceManager().getRegisteredResources()){
			if(resource.getMissingInformations()!=null && resource.getMissingInformations().size()>0 && !resourcesNotComplete.contains(resource)){
				resourcesNotComplete.add(resource);
			}
		}
		return resourcesNotComplete;
	}*/
	

	/**
	 * Receive a ntification that a new resource has changed.
	 * It send a message if a conflict exists or resource not complete
	 */
	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (notification instanceof DataModification 
				&&  ((DataModification)notification).newValue() instanceof FlexoResource<?>){
			informOfConflictedResourceSets();
			informOfResourcesIncomplete((FlexoResource<?>) ((DataModification)notification).newValue());
		}else if (notification instanceof DataModification 
					&&  ((DataModification)notification).newValue() instanceof ResourceData){
			ResourceData data = (ResourceData) ((DataModification)notification).newValue();
				informOfConflictedResourceSets();
				informOfResourcesIncomplete(data.getResource());
				
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
	 * Inform that some resources are incomplete
	 * @param resource
	 */
	private void informOfResourcesIncomplete(FlexoResource<?> resource){
		if(resource!=null && resource.getMissingInformations()!=null 
				&& resource.getMissingInformations().size()>0){
			FlexoController.notify(
					 "<html> "
					+ "<h3>Resources missings!</h3>"
						+ prepareMissingResourcesMessage(resource)
					+ "<h4> Please add resources in resource centers and restart Openflexo</h4> "
					+ "</html>");
		}
	}
	
	private String prepareMissingResourcesMessage(FlexoResource<?> resource){
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>" + resource.getURI() +" requires " );
		for(MissingFlexoResource missingResource : resource.getMissingInformations()){
			//manageMissingResources(missingResource);
			sb.append("<li>"+ missingResource.getInfos() +"</li>");
		}
		sb.append("</ul>");
		return sb.toString();
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