/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.rm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceImpl;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.ResourceRegistered;
import org.openflexo.foundation.resource.ResourceUnregistered;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * This service performs an analysis of existing resources and provides to the user tools to make resources to be consistent.
 * 
 * @author Vincent
 *
 */
public class ResourceConsistencyService extends FlexoServiceImpl {

	// The whole set of conflicts
	private List<ConflictedResourceSet> conflictedResourceSets;

	private List<AbstractVirtualModelInstanceResource<?, ?>> vmiWithoutVM;

	private int skip = 1;

	private final String[] options1 = { "Don't show this message again", "Show conflicted resources" };
	private final String[] options2 = { "Don't show this message again", "Close message" };

	public ResourceConsistencyService() {
	}

	@Override
	public String getServiceName() {
		return "ResourceConsistencyService";
	}

	@Override
	public void initialize() {
		// Initialize current conflicted resource set
		conflictedResourceSets = getConflictedResourceSets();
		// Inform the user of conflicts
		informOfConflictedResourceSets(conflictedResourceSets);

		vmiWithoutVM = new ArrayList<>();

		status = Status.Started;
	}

	/**
	 * Receive a notification that a new resource has changed. It send a message if a conflict exists or resource not complete
	 */
	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		// A resource is unregistered or registered
		if (notification instanceof ResourceUnregistered) {
			ResourceUnregistered unRegistered = (ResourceUnregistered) notification;
			checkProblems(unRegistered.newValue());
		}
		else if (notification instanceof ResourceRegistered) {
			ResourceRegistered registered = (ResourceRegistered) notification;
			checkProblems(registered.newValue());
		}
	}

	private void checkProblems(Object value) {
		if (value instanceof FlexoResource<?>) {
			informOfURIConflict((FlexoResource<?>) value);
		}
		if (value instanceof FMLRTVirtualModelInstanceResourceImpl) {
			informOfViewpointMissing((FMLRTVirtualModelInstanceResourceImpl) value);
		}
	}

	private void informOfURIConflict(FlexoResource<?> resource) {
		ConflictedResourceSet conflict = getConflictedResourceSet(resource);
		if (conflict != null) {
			informOfConflictedResourceSet(conflict);
		}
	}

	/**
	 * Infor of a viewpoint missing for a view. Inform once.
	 * 
	 * @param viewResource
	 */
	private void informOfViewpointMissing(FMLRTVirtualModelInstanceResourceImpl viewResource) {
		if (viewResource.getVirtualModelResource() == null && !vmiWithoutVM.contains(viewResource)) {
			informOfViewPointMissing(viewResource);
			vmiWithoutVM.add(viewResource);
		}
	}

	private ConflictedResourceSet getConflictedResourceSet(FlexoResource<?> resource) {
		// If there is many resource with the same URI
		if (multipleResourcesWithSameURI(resource)) {
			// If a set with this URI exists, add this resource to this set
			ConflictedResourceSet conflictedSet = getConflictedResourceSet(resource.getURI());
			// Otherwize create a new set
			if (conflictedSet == null) {
				conflictedSet = new ConflictedResourceSet(getResources(resource.getURI()));
				conflictedResourceSets.add(conflictedSet);
			}
			else if (!conflictedSet.getConflictedResources().contains(resource)) {
				conflictedSet.getConflictedResources().add(resource);
			}
			return conflictedSet;
		}
		return null;
	}

	/**
	 * Get the set of conflicted sets
	 * 
	 * @return
	 */
	public List<ConflictedResourceSet> getConflictedResourceSets() {
		if (conflictedResourceSets == null) {
			conflictedResourceSets = new ArrayList<>();
		}
		// Browse all resources
		for (FlexoResource<?> resource : getResourceManager().getRegisteredResources()) {
			getConflictedResourceSet(resource);
		}
		return conflictedResourceSets;
	}

	/**
	 * Get a conflicted set of resource, which share the same URI
	 * 
	 * @param uri
	 * @return
	 */
	private ConflictedResourceSet getConflictedResourceSet(String uri) {
		for (ConflictedResourceSet conflictedResourceSet : conflictedResourceSets) {
			if (conflictedResourceSet.getCommonUri().equals(uri)) {
				return conflictedResourceSet;
			}
		}
		return null;
	}

	/**
	 * Inform that a viewpoint is missing
	 * 
	 * @param resource
	 */
	private static void informOfViewPointMissing(FMLRTVirtualModelInstanceResourceImpl resource) {
		if (resource != null) {
			Thread.dumpStack();
			FlexoController.notify("<html> " + "<h3>VirtualModel resource is missing!</h3>"
					+ "<p>FMLRTVirtualModelInstance <font color=\"red\">" + resource.getURI() + "</font><br>requires VirtualModel: "
					+ resource.getVirtualModelURI() + "<br>Please add resources in resource centers and restart Openflexo</html>");
		}
	}

	private void informOfConflictedResourceSets(List<ConflictedResourceSet> conflicts) {
		if (conflicts.size() > 0) {
			StringBuilder conflictsInformation = new StringBuilder();
			for (ConflictedResourceSet conflict : conflicts) {
				conflictsInformation.append(informationMessageForConflictSet(conflict));
			}
			if (skip != 0) {
				skip = FlexoController.selectOption(
						"<html> <h3>" + Integer.toString(conflicts.size()) + " URI conflicts have been found:</h3></br></html>", options1,
						"OK");
				if (skip != 0) {
					skip = FlexoController.selectOption("<html> <h3>" + Integer.toString(conflicts.size())
							+ " URI conflicts have been found:</h3></br>" + conflictsInformation.toString() + "</html>", options2, "OK");
				}
			}
		}
	}

	private void informOfConflictedResourceSet(ConflictedResourceSet conflict) {
		if (skip != 0) {
			System.out.println("Conflict: " + informationMessageForConflictSet(conflict));
			/*skip = FlexoController.selectOption(
					"<html> <h3> URI conflicts have been found:</h3></br>" + informationMessageForConflictSet(conflict) + "</html>",
					options2, "OK");*/
		}
	}

	private static String informationMessageForConflictSet(ConflictedResourceSet conflict) {
		Thread.dumpStack();
		StringBuilder conflictsInformation = new StringBuilder();
		conflictsInformation.append("<p> <font color=\"red\"> URI : " + conflict.getCommonUri() + " is owned by </font><br/>");
		for (FlexoResource<?> fr : conflict.getConflictedResources()) {

			if (fr.getIODelegate() instanceof FileIODelegate) {
				conflictsInformation.append(((FileIODelegate) fr.getIODelegate()).getFile());
			}
			else {
				conflictsInformation.append(fr.getIODelegate().getSerializationArtefact().toString());
			}
			conflictsInformation.append("<br/>");
		}
		conflictsInformation.append("</p>");
		return conflictsInformation.toString();
	}

	/**
	 * Get a set of resources with a uri
	 * 
	 * @param resourceURI
	 * @return
	 */
	private List<FlexoResource<?>> getResources(String resourceURI) {
		List<FlexoResource<?>> flexoResources = new ArrayList<>();
		if (StringUtils.isEmpty(resourceURI)) {
			return null;
		}
		for (FlexoResource<?> r : new ArrayList<>(getResourceManager().getRegisteredResources())) {
			if (resourceURI.equals(r.getURI())) {
				flexoResources.add(r);
			}
		}
		return flexoResources;
	}

	public boolean multipleResourcesWithSameURI(FlexoResource<?> resource) {
		String checkedURI = resource.getURI();
		if (checkedURI != null) {
			List<FlexoResource<?>> rscWithSameUri = getResources(checkedURI);
			if (rscWithSameUri.size() > 1) {
				boolean returned = false;
				// check it is not a resource that has been registered several times
				for (FlexoResource<?> r : rscWithSameUri) {
					returned = returned || (r == resource);
				}
				return returned;
			}
		}
		return false;
	}

	private void manageConflictedResourceSet(ConflictedResourceSet resources, ApplicationContext applicationContext) {
		ConflictedResourceEditorDialog.showResourceConsistencyEditorDialog(resources, this, applicationContext,
				FlexoFrame.getActiveFrame());
	}

	private void manageMissingResources(MissingFlexoResource missingResource, ApplicationContext applicationContext) {
		ResourceMissingEditorDialog.showResourceMissingEditorDialog(missingResource, this, applicationContext, FlexoFrame.getActiveFrame());
	}

	public ResourceManager getResourceManager() {
		return getServiceManager().getResourceManager();
	}

	public int getNumberOfConflicts() {
		return conflictedResourceSets.size();
	}

}
