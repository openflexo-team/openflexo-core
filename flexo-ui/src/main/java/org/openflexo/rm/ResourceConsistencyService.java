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
	
	private int skip =1;
	
	private String[] options1={"Don't show this message again", "Show conflicted resources"}; 
	private String[] options2={"Don't show this message again", "Close message"};
	
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
					+ "<h3>Viewpoint resources is missing!</h3>"
						+ "<p>View <font color=\"red\">" + resource.getURI() +" requires Viewpoint: " + resource.viewpointURI
					+ "</br>Please add resources in resource centers and restart Openflexo</html>");
		}
	}

	private void informOfConflictedResourceSets(List<ConflictedResourceSet> conflicts){
		if(conflicts.size()>0){
			StringBuilder conflictsInformation = new StringBuilder();
			for(ConflictedResourceSet conflict : conflicts){
				conflictsInformation.append(informationMessageForConflictSet(conflict));
			}
			if(skip!=0){
				skip = FlexoController.selectOption("<html> <h3>" +Integer.toString(conflicts.size()) + " URI conflicts have been found:</h3></br></html>", options1,"OK");
				if(skip!=0){
					skip = FlexoController.selectOption("<html> <h3>" +Integer.toString(conflicts.size()) + " URI conflicts have been found:</h3></br>" +
							conflictsInformation.toString()+ "</html>", options2,"OK");
				}
			}
		}
	}
	
	private void informOfConflictedResourceSet(ConflictedResourceSet conflict){
		if(skip!=0){
			skip=FlexoController.selectOption("<html> <h3> URI conflicts have been found:</h3></br>" +
					informationMessageForConflictSet(conflict)+ "</html>", options2,"OK");
		}
	}
	
	private String informationMessageForConflictSet(ConflictedResourceSet conflict){
		StringBuilder conflictsInformation = new StringBuilder();
		conflictsInformation.append("<p> <font color=\"red\"> URI : " + conflict.getCommonUri() +" is owned by </font><br/>");
		for(FlexoResource<?> fr : conflict.getConflictedResources()){
			
			if(fr.getFlexoIODelegate() instanceof FileFlexoIODelegate){
				conflictsInformation.append(((FileFlexoIODelegate)fr.getFlexoIODelegate()).getFile());
			}else{
				conflictsInformation.append(fr.getFlexoIODelegate().getSerializationArtefact().toString());
			}
			conflictsInformation.append("<br/>");
		}
		conflictsInformation.append("</p>");
		return conflictsInformation.toString();
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
		if(resource.getURI()!=null && getResources(resource.getURI()).size()>1){
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