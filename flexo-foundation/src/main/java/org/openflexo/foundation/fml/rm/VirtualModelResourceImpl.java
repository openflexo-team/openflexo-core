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

package org.openflexo.foundation.fml.rm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate.DirectoryBasedFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.InJarFlexoIODelegate;
import org.openflexo.foundation.resource.InJarFlexoIODelegate.InJarFlexoIODelegateImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;

public abstract class VirtualModelResourceImpl extends AbstractVirtualModelResourceImpl<VirtualModel> implements VirtualModelResource,
		AccessibleProxyObject {

	static final Logger logger = Logger.getLogger(VirtualModelResourceImpl.class.getPackage().getName());

	public static VirtualModelResource makeVirtualModelResource(String name, File containerDir, ViewPointResource viewPointResource,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class,
					VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);
			returned.initName(name);

			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(containerDir, "",
					CORE_FILE_SUFFIX, returned, factory));

			/*DirectoryBasedFlexoIODelegate delegate = (DirectoryBasedFlexoIODelegate) returned.getFlexoIODelegate();
			System.out.println("Nouveau VM");
			System.out.println("containerDir=" + containerDir);
			System.out.println("dir=" + delegate.getDirectory());
			System.out.println("file=" + delegate.getFile());*/

			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(virtualModelXMLFile, factory));
			returned.setURI(viewPointResource.getURI() + "/" + name);
			returned.setServiceManager(serviceManager);
			viewPointResource.addToContents(returned);
			viewPointResource.notifyContentsAdded(returned);

			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static VirtualModelResource retrieveVirtualModelResource(File virtualModelDirectory/*, File virtualModelXMLFile*/,
			ViewPointResource viewPointResource, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class,
					VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);
			String baseName = virtualModelDirectory.getName();
			File xmlFile = new File(virtualModelDirectory, baseName + CORE_FILE_SUFFIX);
			VirtualModelInfo vpi = null;
			try {
				vpi = findVirtualModelInfo(new FileInputStream(xmlFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}

			returned.initName(baseName);
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(
					virtualModelDirectory.getParentFile(), "", CORE_FILE_SUFFIX, returned, factory));

			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.setURI(viewPointResource.getURI() + "/" + virtualModelDirectory.getName());
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			/*if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
			}*/

			returned.setServiceManager(serviceManager);

			logger.fine("VirtualModelResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());

			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static VirtualModelResource retrieveVirtualModelResource(InJarResourceImpl inJarResource, Resource parent,
			ViewPointResource viewPointResource, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(InJarFlexoIODelegate.class,
					VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);

			returned.setFlexoIODelegate(InJarFlexoIODelegateImpl.makeInJarFlexoIODelegate(inJarResource, factory));
			VirtualModelInfo vpi = findVirtualModelInfo(returned.getFlexoIOStreamDelegate().getInputStream());
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}

			// returned.setFile(xmlFile);
			// returned.setDirectory(parent);
			returned.initName(vpi.name);
			returned.setURI(viewPointResource.getURI() + "/" + FilenameUtils.getBaseName(inJarResource.getRelativePath()));
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			/*if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
			}*/

			returned.setServiceManager(serviceManager);

			logger.fine("VirtualModelResource " + returned.getFlexoIODelegate().toString() + " version " + returned.getModelVersion());

			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Do not force the resource data to be loaded
	 */
	@Override
	public VirtualModel getLoadedVirtualModel() {
		if (isLoaded()) {
			return getVirtualModel();
		}
		return null;
	}

	@Override
	public Class<VirtualModel> getResourceDataClass() {
		return VirtualModel.class;
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
	public VirtualModel loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		VirtualModel returned = super.loadResourceData(progress);
		// We notify a deserialization start on ViewPoint AND VirtualModel, to avoid addToVirtualModel() and setViewPoint() to notify
		// UndoManager
		boolean containerWasDeserializing = getContainer().isDeserializing();
		if (!containerWasDeserializing) {
			getContainer().startDeserializing();
		}
		startDeserializing();
		getContainer().getViewPoint().addToVirtualModels(returned);
		returned.clearIsModified();
		// And, we notify a deserialization stop
		stopDeserializing();
		if (!containerWasDeserializing) {
			getContainer().stopDeserializing();
		}
		return returned;
	}

	@Override
	public void notifyResourceLoaded() {
		super.notifyResourceLoaded();
		getPropertyChangeSupport().firePropertyChange("virtualModel", null, getLoadedResourceData());
		getPropertyChangeSupport().firePropertyChange("loadedVirtualModel", null, getLoadedResourceData());
	}

	private static class VirtualModelInfo {
		public String version;
		public String name;
		public String modelVersion;
	}

	private static VirtualModelInfo findVirtualModelInfo(InputStream inputStream) {
		Document document;
		try {
			// logger.fine("Try to find infos for " + virtualModelDirectory);

			// String baseName = virtualModelDirectory.getName();
			// File xmlFile = new File(virtualModelDirectory, baseName + ".xml");

			// if (xmlFile.exists()) {

			document = readXMLInputStream(inputStream);// (xmlFile);
			Element root = getElement(document, "VirtualModel");
			if (root != null) {
				VirtualModelInfo returned = new VirtualModelInfo();
				Iterator<Attribute> it = root.getAttributes().iterator();
				while (it.hasNext()) {
					Attribute at = it.next();
					if (at.getName().equals("name")) {
						logger.fine("Returned " + at.getValue());
						returned.name = at.getValue();
					} else if (at.getName().equals("version")) {
						logger.fine("Returned " + at.getValue());
						returned.version = at.getValue();
					} else if (at.getName().equals("modelVersion")) {
						logger.fine("Returned " + at.getValue());
						returned.modelVersion = at.getValue();
					}
				}
				if (StringUtils.isEmpty(returned.name)) {
					// returned.name = virtualModelDirectory.getName();
					returned.name = "NoName";
				}
				return returned;
			}
			/*} else {
				logger.warning("While analysing virtual model candidate: " + virtualModelDirectory.getAbsolutePath() + " cannot find file "
						+ xmlFile.getAbsolutePath());
			}*/
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}

	/*private static VirtualModelInfo findVirtualModelInfo(File virtualModelDirectory) {
		Document document;
		try {
			logger.fine("Try to find infos for " + virtualModelDirectory);
	
			String baseName = virtualModelDirectory.getName();
			File xmlFile = new File(virtualModelDirectory, baseName + ".xml");
	
			if (xmlFile.exists()) {
	
				document = readXMLFile(xmlFile);
				Element root = getElement(document, "VirtualModel");
				if (root != null) {
					VirtualModelInfo returned = new VirtualModelInfo();
					Iterator<Attribute> it = root.getAttributes().iterator();
					while (it.hasNext()) {
						Attribute at = it.next();
						if (at.getName().equals("name")) {
							logger.fine("Returned " + at.getValue());
							returned.name = at.getValue();
						} else if (at.getName().equals("version")) {
							logger.fine("Returned " + at.getValue());
							returned.version = at.getValue();
						} else if (at.getName().equals("modelVersion")) {
							logger.fine("Returned " + at.getValue());
							returned.modelVersion = at.getValue();
						}
					}
					if (StringUtils.isEmpty(returned.name)) {
						returned.name = virtualModelDirectory.getName();
					}
					return returned;
				}
			} else {
				logger.warning("While analysing virtual model candidate: " + virtualModelDirectory.getAbsolutePath() + " cannot find file "
						+ xmlFile.getAbsolutePath());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}*/

	@Override
	public ViewPointResource getContainer() {
		return (ViewPointResource) performSuperGetter(CONTAINER);
	}
}
