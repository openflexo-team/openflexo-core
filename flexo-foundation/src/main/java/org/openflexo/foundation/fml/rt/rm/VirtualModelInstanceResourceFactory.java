/*
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.fml.rt.rm;

import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of PamelaResourceFactory for {@link ViewPointResource}
 * 
 * @author sylvain
 *
 */
public class VirtualModelInstanceResourceFactory
		extends AbstractVirtualModelInstanceResourceFactory<VirtualModelInstance, VirtualModel, VirtualModelInstanceResource> {

	private static final Logger logger = Logger.getLogger(VirtualModelInstanceResourceFactory.class.getPackage().getName());

	public static final String VIRTUAL_MODEL_INSTANCE_SUFFIX = ".vmxml";

	public VirtualModelInstanceResourceFactory() throws ModelDefinitionException {
		super(VirtualModelInstanceResource.class);
	}

	@Override
	public VirtualModelInstance makeEmptyResourceData(VirtualModelInstanceResource resource) {
		return resource.getFactory().newInstance(VirtualModelInstance.class);
	}

	@Override
	public VirtualModelInstanceModelFactory makeResourceDataFactory(VirtualModelInstanceResource resource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new VirtualModelInstanceModelFactory(resource,
				technologyContextManager.getTechnologyAdapter().getServiceManager().getEditingContext(),
				technologyContextManager.getTechnologyAdapter().getServiceManager().getTechnologyAdapterService());
	}

	public <I> VirtualModelInstanceResource makeVirtualModelInstanceResource(String name, VirtualModel virtualModel,
			ViewResource viewResource, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createEntry(
				(name.endsWith(VIRTUAL_MODEL_INSTANCE_SUFFIX) ? name : (name + VIRTUAL_MODEL_INSTANCE_SUFFIX)),
				resourceCenter.getContainer((I) viewResource.getFlexoIODelegate().getSerializationArtefact()));

		VirtualModelInstanceResource returned = initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager,
				name, viewResource.getURI() + "/"
						+ (name.endsWith(VIRTUAL_MODEL_INSTANCE_SUFFIX) ? name : (name + VIRTUAL_MODEL_INSTANCE_SUFFIX)));

		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			VirtualModelInstance resourceData = makeEmptyResourceData(returned);
			resourceData.setVirtualModel(virtualModel);
			resourceData.setResource(returned);
			returned.setResourceData(resourceData);
			returned.setModified(true);
			returned.save(null);
		}

		// Finally add to contents of viewResource
		viewResource.addToContents(returned);
		viewResource.notifyContentsAdded(returned);

		return returned;
	}

	public <I> VirtualModelInstanceResource retrieveVirtualModelInstanceResource(I serializationArtefact,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, ViewResource viewResource)
			throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		VirtualModelInstanceResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);
		returned.setURI(viewResource.getURI() + "/" + name);
		returned.setServiceManager(resourceCenter.getServiceManager());

		viewResource.addToContents(returned);
		viewResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter, technologyContextManager);

		return returned;
	}

	@Override
	protected <I> VirtualModelInstanceResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, String name, String uri)
			throws ModelDefinitionException {
		VirtualModelInstanceResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter,
				technologyContextManager, name, uri);
		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_RT_VERSION);
		return returned;
	}

	@Override
	protected <I> VirtualModelInstanceResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException, IOException {
		VirtualModelInstanceResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter,
				technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName;
		if (artefactName.endsWith(VIRTUAL_MODEL_INSTANCE_SUFFIX)) {
			baseName = artefactName.substring(0, artefactName.length() - VIRTUAL_MODEL_INSTANCE_SUFFIX.length());
		}
		returned.initName(baseName);

		VirtualModelInstanceInfo vmiInfo = findVirtualModelInstanceInfo(returned, resourceCenter);
		if (vmiInfo != null) {
			returned.setURI(vmiInfo.uri);
			if (StringUtils.isNotEmpty(vmiInfo.version)) {
				returned.setVersion(new FlexoVersion(vmiInfo.version));
			}
			else {
				returned.setVersion(INITIAL_REVISION);
			}
			if (StringUtils.isNotEmpty(vmiInfo.modelVersion)) {
				returned.setModelVersion(new FlexoVersion(vmiInfo.modelVersion));
			}
			else {
				returned.setModelVersion(CURRENT_FML_RT_VERSION);
			}
			if (StringUtils.isNotEmpty(vmiInfo.virtualModelURI)) {
				VirtualModelResource vmrsc = null;
				FlexoServiceManager sm = technologyContextManager.getServiceManager();
				vmrsc = sm.getViewPointLibrary().getVirtualModelResource(vmiInfo.virtualModelURI);
				if (vmrsc == null) {
					// In this case, serialize URI of virtualmodel, to give a chance to find it later
					returned.setVirtualModelURI(vmiInfo.virtualModelURI);
					logger.warning("Could not find virtual model " + vmiInfo.virtualModelURI);
				}
				else {
					returned.setVirtualModelResource(vmrsc);
				}
			}
		}
		else {
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			returned.setVersion(INITIAL_REVISION);
			returned.setModelVersion(CURRENT_FML_RT_VERSION);
		}

		return returned;

	}

	protected static class VirtualModelInstanceInfo {
		public String virtualModelURI;
		public String name;
		public String uri;
		public String version;
		public String modelVersion;
	}

	private <I> VirtualModelInstanceInfo findVirtualModelInstanceInfo(VirtualModelInstanceResource resource,
			FlexoResourceCenter<I> resourceCenter) {

		VirtualModelInstanceInfo returned = new VirtualModelInstanceInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getFlexoIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}
		if (xmlRootElementInfo.getName().equals("VirtualModelInstance")) {
			returned.name = xmlRootElementInfo.getAttribute("name");
			returned.uri = xmlRootElementInfo.getAttribute("uri");
			returned.virtualModelURI = xmlRootElementInfo.getAttribute("virtualModelURI");
			returned.version = xmlRootElementInfo.getAttribute("version");
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
		}
		return returned;
	}

	/*protected <I> VirtualModelInstanceInfo findVirtualModelInstanceInfo(I serializationArtefact) {
		Document document;
	
		if (serializationArtefact instanceof File) {
			try {
				File virtualModelInstanceFile = (File) serializationArtefact;
				logger.fine("Try to find infos for " + virtualModelInstanceFile);
	
				String baseName = virtualModelInstanceFile.getName().substring(0,
						virtualModelInstanceFile.getName().length() - VIRTUAL_MODEL_INSTANCE_SUFFIX.length());
	
				if (virtualModelInstanceFile.exists()) {
					document = XMLUtils.readXMLFile(virtualModelInstanceFile);
					Element root = XMLUtils.getElement(document, "VirtualModelInstance");
					if (root != null) {
						VirtualModelInstanceInfo returned = new VirtualModelInstanceInfo();
						returned.name = baseName;
						Iterator<Attribute> it = root.getAttributes().iterator();
						while (it.hasNext()) {
							Attribute at = it.next();
							if (at.getName().equals("uri")) {
								logger.fine("Returned " + at.getValue());
								returned.uri = at.getValue();
							}
							else if (at.getName().equals("virtualModelURI")) {
								logger.fine("Returned " + at.getValue());
								returned.virtualModelURI = at.getValue();
							}
							else if (at.getName().equals("version")) {
								logger.fine("Returned " + at.getValue());
								returned.version = at.getValue();
							}
							else if (at.getName().equals("modelVersion")) {
								logger.fine("Returned " + at.getValue());
								returned.modelVersion = at.getValue();
							}
						}
						return returned;
					}
				}
				else {
					logger.warning("Cannot find file: " + virtualModelInstanceFile.getAbsolutePath());
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.fine("Returned null");
		return null;
	}*/

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.canRead(serializationArtefact)
				&& resourceCenter.retrieveName(serializationArtefact).endsWith(VIRTUAL_MODEL_INSTANCE_SUFFIX)) {
			return true;
		}
		return false;
	}

	/*public static VirtualModelInstanceResource makeVirtualModelInstanceResource(String name, VirtualModel virtualModel, View view) {
	
		System.out.println("Et hop, on cree une nouvelle vmi " + name + " pour " + view);
		System.out.println("thread=" + Thread.currentThread());
		System.out.println("EDT=" + SwingUtilities.isEventDispatchThread());
	
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(FileFlexoIODelegate.class, VirtualModelInstanceResource.class));
			VirtualModelInstanceResourceImpl returned = (VirtualModelInstanceResourceImpl) factory
					.newInstance(VirtualModelInstanceResource.class);
			String baseName = name;
	
			FileFlexoIODelegate delegate = (FileFlexoIODelegate) ((ViewResource) view.getResource()).getFlexoIODelegate();
	
			File xmlFile = new File(delegate.getFile().getParentFile(), baseName + VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX);
			returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.setProject(view.getProject());
			returned.setFactory(new VirtualModelInstanceModelFactory(returned, view.getProject().getServiceManager().getEditingContext(),
					view.getProject().getServiceManager().getTechnologyAdapterService()));
			returned.initName(name);
			// returned.setURI(view.getResource().getURI() + "/" + baseName);
			//System.out.println(">>>>>>>>>>> virtualModel=" + virtualModel);
			returned.setVirtualModelResource((VirtualModelResource) virtualModel.getResource());
			returned.setResourceCenter(view.getProject());
			returned.setServiceManager(view.getProject().getServiceManager());
	
			view.getResource().addToContents(returned);
			view.getResource().notifyContentsAdded(returned);
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static VirtualModelInstanceResource retrieveVirtualModelInstanceResource(File virtualModelInstanceFile,
			ViewResource viewResource) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(FileFlexoIODelegate.class, VirtualModelInstanceResource.class));
			VirtualModelInstanceResourceImpl returned = (VirtualModelInstanceResourceImpl) factory
					.newInstance(VirtualModelInstanceResource.class);
			String baseName = virtualModelInstanceFile.getName().substring(0,
					virtualModelInstanceFile.getName().length() - VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX.length());
	
			FileFlexoIODelegate delegate = (FileFlexoIODelegate) (viewResource.getFlexoIODelegate());
	
			File xmlFile = new File(delegate.getFile().getParentFile(), baseName + VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX);
			FileFlexoIODelegate fileIODelegate = factory.newInstance(FileFlexoIODelegate.class);
			returned.setFlexoIODelegate(fileIODelegate);
			fileIODelegate.setFile(xmlFile);
			returned.setProject(viewResource.getProject());
			returned.setFactory(
					new VirtualModelInstanceModelFactory(returned, viewResource.getProject().getServiceManager().getEditingContext(),
							viewResource.getProject().getServiceManager().getTechnologyAdapterService()));
			returned.initName(baseName);
			// returned.setURI(viewResource.getURI() + "/" + baseName);
			VirtualModelInstanceInfo vmiInfo = findVirtualModelInstanceInfo(xmlFile, "VirtualModelInstance");
			if (vmiInfo == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
	
			if (StringUtils.isNotEmpty(vmiInfo.virtualModelURI)) {
				if (viewResource != null && viewResource.getViewPoint() != null
						&& viewResource.getViewPoint().getVirtualModelNamed(vmiInfo.virtualModelURI) != null) {
					returned.setVirtualModelResource(
							(VirtualModelResource) viewResource.getViewPoint().getVirtualModelNamed(vmiInfo.virtualModelURI).getResource());
				}
			}
			viewResource.addToContents(returned);
			viewResource.notifyContentsAdded(returned);
			returned.setResourceCenter(viewResource.getProject());
			returned.setServiceManager(viewResource.getProject().getServiceManager());
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}*/

}
