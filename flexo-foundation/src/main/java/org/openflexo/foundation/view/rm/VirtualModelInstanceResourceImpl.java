package org.openflexo.foundation.view.rm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.foundation.view.FreeModelSlotInstance;
import org.openflexo.foundation.view.ModelSlotInstance;
import org.openflexo.foundation.view.TypeAwareModelSlotInstance;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.view.VirtualModelModelSlotInstance;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;

/**
 * Default implementation for {@link VirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class VirtualModelInstanceResourceImpl extends PamelaResourceImpl<VirtualModelInstance, VirtualModelInstanceModelFactory>
		implements VirtualModelInstanceResource, AccessibleProxyObject {

	static final Logger logger = Logger.getLogger(VirtualModelInstanceResourceImpl.class.getPackage().getName());

	/*private static VirtualModelInstanceModelFactory VIRTUAL_MODEL_INSTANCE_FACTORY;

	static {
		try {
			VIRTUAL_MODEL_INSTANCE_FACTORY = new VirtualModelInstanceModelFactory();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}*/

	public static VirtualModelInstanceResource makeVirtualModelInstanceResource(String name, VirtualModel virtualModel, View view) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext( 
					FileFlexoIODelegate.class,VirtualModelInstanceResource.class));
			VirtualModelInstanceResourceImpl returned = (VirtualModelInstanceResourceImpl) factory
					.newInstance(VirtualModelInstanceResource.class);
			String baseName = name;
			
			FileFlexoIODelegate delegate = (FileFlexoIODelegate)((ViewResource) view.getResource()).getFlexoIODelegate();
			
			File xmlFile = new File(delegate.getFile().getParentFile(), baseName
					+ VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX);
			returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.setProject(view.getProject());
			returned.setFactory(new VirtualModelInstanceModelFactory(returned, view.getProject().getServiceManager().getEditingContext(),
					view.getProject().getServiceManager().getTechnologyAdapterService()));
			returned.setName(name);
			returned.setURI(view.getResource().getURI() + "/" + baseName);
			returned.setVirtualModelResource((VirtualModelResource) virtualModel.getResource());

			

			view.getResource().addToContents(returned);
			view.getResource().notifyContentsAdded(returned);
			returned.setServiceManager(view.getProject().getServiceManager());
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static VirtualModelInstanceResource retrieveVirtualModelInstanceResource(File virtualModelInstanceFile, ViewResource viewResource) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext( 
					FileFlexoIODelegate.class,VirtualModelInstanceResource.class));
			VirtualModelInstanceResourceImpl returned = (VirtualModelInstanceResourceImpl) factory
					.newInstance(VirtualModelInstanceResource.class);
			String baseName = virtualModelInstanceFile.getName().substring(0,
					virtualModelInstanceFile.getName().length() - VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX.length());
			

			FileFlexoIODelegate delegate = (FileFlexoIODelegate)(viewResource.getFlexoIODelegate());
			
			
			File xmlFile = new File(delegate.getFile().getParentFile(), baseName + VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX);
			FileFlexoIODelegate fileIODelegate = factory.newInstance(FileFlexoIODelegate.class) ;
			returned.setFlexoIODelegate(fileIODelegate);
			fileIODelegate.setFile(xmlFile);
			returned.setProject(viewResource.getProject());
			returned.setFactory(new VirtualModelInstanceModelFactory(returned, viewResource.getProject().getServiceManager()
					.getEditingContext(), viewResource.getProject().getServiceManager().getTechnologyAdapterService()));
			returned.setName(baseName);
			returned.setURI(viewResource.getURI() + "/" + baseName);
			VirtualModelInstanceInfo vmiInfo = findVirtualModelInstanceInfo(xmlFile, "VirtualModelInstance");
			if (vmiInfo == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
			
			if (StringUtils.isNotEmpty(vmiInfo.virtualModelURI)) {
				if (viewResource != null && viewResource.getViewPoint() != null
						&& viewResource.getViewPoint().getVirtualModelNamed(vmiInfo.virtualModelURI) != null) {
					returned.setVirtualModelResource((VirtualModelResource) viewResource.getViewPoint()
							.getVirtualModelNamed(vmiInfo.virtualModelURI).getResource());
				}
			}
			viewResource.addToContents(returned);
			viewResource.notifyContentsAdded(returned);
			returned.setServiceManager(viewResource.getProject().getServiceManager());
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public VirtualModelInstance getVirtualModelInstance() {
		try {
			return getResourceData(null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<VirtualModelInstance> getResourceDataClass() {
		return VirtualModelInstance.class;
	}

	protected static class VirtualModelInstanceInfo {
		public String virtualModelURI;
		public String name;
	}

	protected static VirtualModelInstanceInfo findVirtualModelInstanceInfo(File virtualModelInstanceFile, String searchedRootXMLTag) {
		Document document;
		try {
			logger.fine("Try to find infos for " + virtualModelInstanceFile);

			String baseName = virtualModelInstanceFile.getName().substring(0,
					virtualModelInstanceFile.getName().length() - VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX.length());

			if (virtualModelInstanceFile.exists()) {
				document = XMLUtils.readXMLFile(virtualModelInstanceFile);
				Element root = XMLUtils.getElement(document, searchedRootXMLTag);
				if (root != null) {
					VirtualModelInstanceInfo returned = new VirtualModelInstanceInfo();
					returned.name = baseName;
					Iterator<Attribute> it = root.getAttributes().iterator();
					while (it.hasNext()) {
						Attribute at = it.next();
						if (at.getName().equals("virtualModelURI")) {
							logger.fine("Returned " + at.getValue());
							returned.virtualModelURI = at.getValue();
						}
					}
					return returned;
				}
			} else {
				logger.warning("Cannot find file: " + virtualModelInstanceFile.getAbsolutePath());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}

	@Override
	public VirtualModelInstance loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException,
			InvalidXMLException, InconsistentDataException, InvalidModelDefinitionException {
		VirtualModelInstance returned = super.loadResourceData(progress);
		// We notify a deserialization start on ViewPoint AND VirtualModel, to avoid addToVirtualModel() and setViewPoint() to notify
		// UndoManager
		boolean containerWasDeserializing = getContainer().isDeserializing();
		if (!containerWasDeserializing) {
			getContainer().startDeserializing();
		}
		startDeserializing();
		getContainer().getView().addToVirtualModelInstances(returned);
		returned.clearIsModified();
		/*if (returned.isSynchronizable()) {
			returned.synchronize(null);
		}*/
		// And, we notify a deserialization stop
		stopDeserializing();
		if (!containerWasDeserializing) {
			getContainer().stopDeserializing();
		}

		/*if (!getContainer().isDeserializing()) {
			if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
				getLoadedResourceData().synchronize(null);
			}
		}*/

		// CAUTION: entering HACKING area
		/*SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
					getLoadedResourceData().synchronize(null);
				}
			}
		});*/

		return returned;
	}

	@Override
	public void setLoading(boolean isLoading) {
		super.setLoading(isLoading);
		// Just after the loading occurs, apply synchronization.
		if (!isLoading()) {
			if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
				getLoadedResourceData().synchronize(null);
			}
		}
	}

	@Override
	public VirtualModelTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(VirtualModelTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public ViewResource getContainer() {
		return (ViewResource) performSuperGetter(CONTAINER);
	}

	@Override
	public List<MissingFlexoResource> getMissingInformations() {
		List<MissingFlexoResource> missingResources = new ArrayList<MissingFlexoResource>();
		if(isLoaded() && getVirtualModelInstance()!=null){
				
			for(ModelSlotInstance msi : getVirtualModelInstance().getModelSlotInstances()){
				if(msi.getResource()==null && msi instanceof TypeAwareModelSlotInstance){
					TypeAwareModelSlotInstance taMsi = (TypeAwareModelSlotInstance)msi;
					missingResources.add(new MissingFlexoResource(taMsi.getModelURI(),this)) ;
				} else if(msi.getResource()==null && msi instanceof FreeModelSlotInstance){
					FreeModelSlotInstance fMsi = (FreeModelSlotInstance)msi;
						missingResources.add(new MissingFlexoResource(fMsi.getResourceURI(),this)) ;
				} else if(msi.getResource()==null && msi instanceof VirtualModelModelSlotInstance){
					VirtualModelModelSlotInstance vmMsi = (VirtualModelModelSlotInstance)msi;
					missingResources.add(new MissingFlexoResource(vmMsi.getVirtualModelInstanceURI(),this)) ;
				}
			}	
		}
		
		return missingResources;
	}
}
