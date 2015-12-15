/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt.rm;

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
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.ViewModelFactory;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate.DirectoryBasedFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResourceDefinition;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.RequiredResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SomeResources;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;

/**
 * Default implementation for {@link ViewResource}
 * 
 * 
 * @author Sylvain
 * 
 */
@FlexoResourceDefinition( /* This is the resource specification*/
resourceDataClass = View.class, /* ResourceData class which is handled by this resource */
contains = { /* Defines the resources which may be embeddded in this resource */
@SomeResources(resourceType = ViewResource.class, pattern = "*.view"),
		@SomeResources(resourceType = VirtualModelInstanceResource.class, pattern = "*.vmxml") }, require = { /* Defines the resources which are required for this resource */
@RequiredResource(resourceType = ViewPointResource.class, value = ViewResource.VIEWPOINT_RESOURCE) })
public abstract class ViewResourceImpl extends AbstractVirtualModelInstanceResourceImpl<View, ViewPoint> implements ViewResource {

	static final Logger logger = Logger.getLogger(ViewResourceImpl.class.getPackage().getName());

	public static ViewResource makeViewResource(String name, RepositoryFolder<ViewResource> folder, ViewPoint viewPoint,
			ViewLibrary viewLibrary) {
		ViewResource returned = makeViewResourceInDirectory(name, folder.getFile(), viewPoint, viewLibrary);
		returned.setURI(viewLibrary.getProject().getURI() + "/" + returned.getName());
		viewLibrary.registerResource(returned, folder);
		return returned;
	}

	public static ViewResource makeSubViewResource(String name, ViewResource container, ViewPoint viewPoint, ViewLibrary viewLibrary) {

		System.out.println("Et hop, on cree une nouvelle subview " + name + " pour " + container);
		System.out.println("container.getFlexoIODelegate()=" + container.getFlexoIODelegate());

		if (container.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
			ViewResource returned = makeViewResourceInDirectory(name, ((FileFlexoIODelegate) container.getFlexoIODelegate()).getFile()
					.getParentFile(), viewPoint, viewLibrary);
			returned.setURI(container.getURI() + "/" + returned.getName());

			System.out.println("***************** COUCOU on ajoute la nouvelle SubView dans la View");
			System.out.println("loaded=" + container.isLoaded());
			System.out.println("***************** HOP1");
			viewLibrary.registerResource(returned, container);
			System.out.println("***************** HOP2");
			return returned;
		} else {
			// TODO !!!
		}
		return null;
	}

	private static ViewResource makeViewResourceInDirectory(String name, File directory, ViewPoint viewPoint, ViewLibrary viewLibrary) {
		try {
			// File viewDirectory = new File(folder.getFile(), name + ViewResource.VIEW_SUFFIX);
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class,
					ViewResource.class));
			ViewResourceImpl returned = (ViewResourceImpl) factory.newInstance(ViewResource.class);
			// String baseName = name;
			// File xmlFile = new File(viewDirectory, baseName + ".xml");
			returned.initName(name);

			System.out.println("Je suis dans la vue " + directory);
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(directory, VIEW_SUFFIX,
					CORE_FILE_SUFFIX, returned, factory));
			System.out.println("Je sauve la nouvelle sous-vue en " + ((FileFlexoIODelegate) returned.getFlexoIODelegate()).getFile());

			returned.setProject(viewLibrary.getProject());
			returned.setVersion(new FlexoVersion("1.0"));
			// returned.setURI(viewLibrary.getProject().getURI() + "/" + name);
			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.setViewLibrary(viewLibrary);
			returned.setViewPointResource((ViewPointResource) viewPoint.getResource());
			returned.setFactory(new ViewModelFactory(returned, viewLibrary.getServiceManager().getEditingContext(), viewLibrary
					.getServiceManager().getTechnologyAdapterService()));

			returned.setResourceCenter(viewLibrary.getProject());
			returned.setServiceManager(viewLibrary.getServiceManager());

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ViewResource retrieveViewResource(File viewDirectory, RepositoryFolder<ViewResource> folder, ViewLibrary viewLibrary) {

		ViewResource returned = retrieveViewResourceFromDirectory(viewDirectory, viewLibrary);
		returned.setURI(viewLibrary.getProject().getURI() + "/" + returned.getName());
		viewLibrary.registerResource(returned, folder);
		return returned;
	}

	public static ViewResource retrieveSubViewResource(File viewDirectory, ViewResource container, ViewLibrary viewLibrary) {

		ViewResource returned = retrieveViewResourceFromDirectory(viewDirectory, viewLibrary);
		returned.setURI(container.getURI() + "/" + returned.getName());
		viewLibrary.registerResource(returned, container);
		return returned;
	}

	private static ViewResource retrieveViewResourceFromDirectory(File viewDirectory, ViewLibrary viewLibrary) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class,
					ViewResource.class));
			ViewResourceImpl returned = (ViewResourceImpl) factory.newInstance(ViewResource.class);
			String baseName = viewDirectory.getName().substring(0, viewDirectory.getName().length() - ViewResource.VIEW_SUFFIX.length());
			File xmlFile = new File(viewDirectory, baseName + ".xml");
			ViewInfo vpi = findViewInfo(viewDirectory);
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
			returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.initName(vpi.name);

			returned.setProject(viewLibrary.getProject());

			if (StringUtils.isNotEmpty(vpi.viewPointURI)) {
				returned.viewpointURI = vpi.viewPointURI;
				returned.setViewPointResource(viewLibrary.getServiceManager().getViewPointLibrary().getViewPointResource(vpi.viewPointURI));
			}
			returned.setViewLibrary(viewLibrary);
			returned.setFactory(new ViewModelFactory(returned, viewLibrary.getServiceManager().getEditingContext(), viewLibrary
					.getServiceManager().getTechnologyAdapterService()));

			returned.setResourceCenter(viewLibrary.getProject());
			returned.setServiceManager(viewLibrary.getServiceManager());

			logger.fine("ViewResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());

			// Now look for virtual model instances
			if (viewDirectory.exists() && viewDirectory.isDirectory()) {
				for (File virtualModelFile : viewDirectory.listFiles()) {
					if (virtualModelFile.getName().endsWith(VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX)) {
						VirtualModelInstanceResource virtualModelInstanceResource = VirtualModelInstanceResourceImpl
								.retrieveVirtualModelInstanceResource(virtualModelFile, returned);
						returned.addToContents(virtualModelInstanceResource);
					} else if (virtualModelFile.getName().endsWith(ViewResource.VIEW_SUFFIX)) {
						ViewResource subViewResource = ViewResourceImpl.retrieveSubViewResource(virtualModelFile, returned, viewLibrary);
						returned.addToContents(subViewResource);
						System.out.println(">>>>>>>>> Hop j'ai trouve une subview " + virtualModelFile + " dans " + returned);
					}
				}
			}
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String viewpointURI;

	@Override
	public View getView() {
		return getVirtualModelInstance();
	}

	@Override
	public ViewPoint getViewPoint() {
		if (getViewPointResource() != null) {
			return getViewPointResource().getViewPoint();
		}
		return null;
	}

	/**
	 * Load the &quot;real&quot; load resource data of this resource.
	 * 
	 * @param progress
	 *            a progress monitor in case the resource data is not immediately available.
	 * @return the resource data.
	 * @throws ResourceLoadingCancelledException
	 * @throws ResourceDependencyLoopException
	 * @throws FileNotFoundException
	 */
	@Override
	public View loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		View returned = super.loadResourceData(progress);

		return returned;
	}

	@Override
	public Class<View> getResourceDataClass() {
		return View.class;
	}

	@Override
	public FMLRTTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public List<VirtualModelInstanceResource> getVirtualModelInstanceResources() {
		// View view = getView();
		return getContents(VirtualModelInstanceResource.class);
	}

	/**
	 * Return the list of all {@link VirtualModelInstanceResource} defined in this {@link ViewResource} conform to supplied
	 * {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	@Override
	public List<VirtualModelInstanceResource> getVirtualModelInstanceResources(AbstractVirtualModel<?> virtualModel) {
		List<VirtualModelInstanceResource> returned = new ArrayList<VirtualModelInstanceResource>();
		for (VirtualModelInstanceResource vmiRes : getVirtualModelInstanceResources()) {
			if (virtualModel.isAssignableFrom(vmiRes.getVirtualModelResource().getVirtualModel())) {
				returned.add(vmiRes);
			}
		}
		return returned;
	}

	private static class ViewInfo {
		public String viewPointURI;
		@SuppressWarnings("unused")
		public String viewPointVersion;
		public String name;
	}

	private static ViewInfo findViewInfo(File viewDirectory) {
		Document document;
		try {
			logger.fine("Try to find infos for " + viewDirectory);

			String baseName = viewDirectory.getName().substring(0, viewDirectory.getName().length() - 5);
			File xmlFile = new File(viewDirectory, baseName + ".xml");

			if (xmlFile.exists()) {
				document = XMLUtils.readXMLFile(xmlFile);
				Element root = XMLUtils.getElement(document, "View");
				if (root != null) {
					ViewInfo returned = new ViewInfo();
					returned.name = baseName;
					Iterator<Attribute> it = root.getAttributes().iterator();
					while (it.hasNext()) {
						Attribute at = it.next();
						if (at.getName().equals("viewPointURI")) {
							logger.fine("Returned " + at.getValue());
							returned.viewPointURI = at.getValue();
						} else if (at.getName().equals("viewPointVersion")) {
							logger.fine("Returned " + at.getValue());
							returned.viewPointVersion = at.getValue();
						}
					}
					return returned;
				}
			} else {
				logger.warning("Cannot find file: " + xmlFile.getAbsolutePath());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}

	// Covered by IODelegate
	/*@Override
	public synchronized boolean hasWritePermission() {
		return true;
	}*/

	@Override
	public boolean delete(Object... context) {
		if (super.delete(context)) {
			getServiceManager().getResourceManager().addToFilesToDelete(ResourceLocator.retrieveResourceAsFile(getDirectory()));
			return true;
		}
		return false;
	}

	@Override
	public Resource getDirectory() {
		String parentPath = getDirectoryPath();
		if (ResourceLocator.locateResource(parentPath) == null) {
			FileSystemResourceLocatorImpl.appendDirectoryToFileSystemResourceLocator(parentPath);
		}
		return ResourceLocator.locateResource(parentPath);
	}

	public String getDirectoryPath() {
		return ((FileFlexoIODelegate) getFlexoIODelegate()).getFile().getParentFile().getAbsolutePath();
	}

	@Override
	public View getModelData() {
		return getView();
	}

	@Override
	public View getModel() {
		return getView();
	}
}
