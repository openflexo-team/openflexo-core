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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate.DirectoryBasedFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoXMLFileResourceImpl;
import org.openflexo.foundation.resource.InJarFlexoIODelegate;
import org.openflexo.foundation.resource.InJarFlexoIODelegate.InJarFlexoIODelegateImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;
import org.openflexo.xml.XMLRootElementReader;

public abstract class ViewPointResourceImpl extends AbstractVirtualModelResourceImpl<ViewPoint>implements ViewPointResource {

	static final Logger logger = Logger.getLogger(FlexoXMLFileResourceImpl.class.getPackage().getName());

	private static XMLRootElementReader reader = new XMLRootElementReader();

	public static ViewPointResource makeViewPointResource(String name, String uri, File containerDir, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			returned.initName(name);
			returned.setURI(uri);
			returned.setVersion(new FlexoVersion("0.1"));
			returned.setModelVersion(new FlexoVersion("1.0"));

			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(containerDir, VIEWPOINT_SUFFIX,
					CORE_FILE_SUFFIX, returned, factory));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}

			returned.setServiceManager(serviceManager);
			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ViewPointResource retrieveViewPointResource(File viewPointDirectory, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			String baseName = viewPointDirectory.getName().substring(0, viewPointDirectory.getName().length() - VIEWPOINT_SUFFIX.length());
			File xmlFile = new File(viewPointDirectory, baseName + CORE_FILE_SUFFIX);
			ViewPointInfo vpi = null;
			try {
				vpi = findViewPointInfo(new FileInputStream(xmlFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}

			returned.setURI(vpi.uri);
			returned.initName(vpi.name);

			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(
					viewPointDirectory.getParentFile(), VIEWPOINT_SUFFIX, CORE_FILE_SUFFIX, returned, factory));

			returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));

			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			/*boolean hasBeenConverted = false;
			if (StringUtils.isEmpty(vpi.modelVersion)) {
				// This is the old model, convert to new model
				convertViewPoint(viewPointDirectory, xmlFile);
				hasBeenConverted = true;
			}*/

			/*
			 * Will be activitated when the convertion will be fully compliant
			 */
			/*if (isAn16Viewpoint(returned)) {
				logger.fine("Converting viewpoint " + xmlFile.getAbsolutePath());
				convertViewPoint16ToViewpoint17(returned);
			}*/

			if (StringUtils.isEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion("0.1"));
			} else {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}

			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}

			returned.setServiceManager(serviceManager);

			logger.fine("ViewPointResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());

			// Now look for virtual models
			returned.exploreVirtualModels(returned.getDirectory());

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ViewPointResource retrieveViewPointResource(InJarResourceImpl inJarResource, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(InJarFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);

			returned.setFlexoIODelegate(InJarFlexoIODelegateImpl.makeInJarFlexoIODelegate(inJarResource, factory));

			// String parentPath = FilenameUtils.getFullPath(inJarResource.getRelativePath());
			// BasicResourceImpl parent = (BasicResourceImpl)
			// ((ClasspathResourceLocatorImpl)(inJarResource.getLocator())).getJarResourcesList().get(parentPath);

			ViewPointInfo vpi = findViewPointInfo(returned.getFlexoIOStreamDelegate().getInputStream());
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}

			// returned.setDirectory(parent);
			returned.setURI(vpi.uri);
			returned.initName(vpi.name);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}

			if (StringUtils.isEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion("0.1"));
			} else {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}

			returned.setFactory(new FMLModelFactory(returned, serviceManager));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}

			returned.setServiceManager(serviceManager);

			// Now look for virtual models
			returned.exploreVirtualModels(returned.getDirectory());

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void exploreVirtualModels(Resource parent) {
		XMLRootElementInfo result = null;

		for (Resource child : parent.getContents()) {
			if (child.isContainer()) {
				exploreVirtualModels(child);
			} else {
				try {
					if (child.getURI().endsWith(".xml")) {
						result = reader.readRootElement(child);
						// Serialization artefact is File
						if (result.getName().equals("VirtualModel") && getFlexoIODelegate() instanceof FileFlexoIODelegate) {
							VirtualModelResource virtualModelResource = VirtualModelResourceImpl.retrieveVirtualModelResource(
									new File(FilenameUtils.getFullPath(child.getRelativePath())),
									/*ResourceLocator.retrieveResourceAsFile(child),*/ this, getServiceManager());
							addToContents(virtualModelResource);
						}
						// Serialization artefact is InJarResource
						else if (result.getName().equals("VirtualModel") && getFlexoIODelegate() instanceof InJarFlexoIODelegate) {
							VirtualModelResource virtualModelResource = VirtualModelResourceImpl
									.retrieveVirtualModelResource((InJarResourceImpl) child, parent, this, getServiceManager());
							addToContents(virtualModelResource);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
		/*if (getDirectory().exists() && getDirectory().isDirectory()) {
			for (File f : getDirectory().listFiles()) {
				if (f.isDirectory()) {
					File virtualModelFile = new File(f, f.getName() + ".xml");
					if (virtualModelFile.exists()) {
		
						// This directory should be append to resources to be looked-up, because of images.
						FileSystemResourceLocatorImpl fsrl = (FileSystemResourceLocatorImpl) ResourceLocator
								.getInstanceForLocatorClass(FileSystemResourceLocatorImpl.class);
						if (fsrl != null && f.getPath() != null) {
							fsrl.appendToDirectories(f.getPath());
						}
		
						try {
		
							result = reader.readRootElement(virtualModelFile);
		
							if (result.getName().equals("VirtualModel")) {
								VirtualModelResource virtualModelResource = VirtualModelResourceImpl.retrieveVirtualModelResource(f,
										virtualModelFile, this, getServiceManager());
								addToContents(virtualModelResource);
							} 
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}*/
	}

	@Override
	public ViewPoint getViewPoint() {

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
	public ViewPoint loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {

		ViewPointImpl returned = (ViewPointImpl) super.loadResourceData(progress);

		// Vincent : the name should already exsit no?
		// String baseName = getDirectory().getRelativePath().substring(0, getDirectory().getRelativePath().length() - 10);
		// returned.init(baseName,/* getDirectory(), getFile(),*/getViewPointLibrary());

		/*for (VirtualModel vm : returned.getVirtualModels()) {
			for (FlexoConcept ep : vm.getFlexoConcepts()) {
				ep.finalizeFlexoConceptDeserialization();
			}
			vm.clearIsModified();
		}*/

		returned.clearIsModified();

		return returned;
	}

	@Override
	public Class<ViewPoint> getResourceDataClass() {
		return ViewPoint.class;
	}

	/**
	 * Return flag indicating if this resource is loadable<br>
	 * By default, such resource is loadable if based on 1.6 architecture (model version greater or equals to 1.0)
	 * 
	 * @return
	 */
	@Override
	public boolean isLoadable() {
		return !isDeprecatedVersion();
	}

	@Override
	public boolean isDeprecatedVersion() {
		if (getModelVersion() == null) {
			return true;
		}
		return getModelVersion().isLesserThan(new FlexoVersion("1.0"));
	}

	// TODO REimplement it using Input Stream only
	/*public static boolean isAn16Viewpoint(ViewPointResource viewpointResource) {
		try {
			for (File f : viewpointResource.getDirectory().listFiles()) {
				if (f.isDirectory()) {
					for (File file : f.listFiles()) {
						try {
							if (file.getName().endsWith(".palette")) {
								if (contains16Elements(XMLUtils.readXMLFile(file))) {
									return true;
								}
							}
							if (file.getName().endsWith(".diagram")) {
								if (contains16Elements(XMLUtils.readXMLFile(file))) {
									return true;
								}
							}
							if (file.getName().endsWith(".xml")) {
								if (contains16Elements(XMLUtils.readXMLFile(file))) {
									return true;
								}
							}
						}
						catch  (JDOMException e) {
							logger.warning("Error when parsing file : " + file.getName());
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			logger.warning("Error when checking Viewpoin version for  : " + viewpointResource.getURI());
			e.printStackTrace();
		}
		return false;
	}*/

	private static boolean contains16Elements(Document document) {
		if (document
				.getDescendants(new ElementFilter("EditionPattern").or(new ElementFilter("ContainedEditionPatternInstancePatternRole").or(
						new ElementFilter("ContainedEMFObjectIndividualPatternRole").or(new ElementFilter("ContainedShapePatternRole").or(
								new ElementFilter("ContainedConnectorPatternRole").or(new ElementFilter("ContainedOWLIndividualPatternRole")
										.or(new ElementFilter("ContainedExcelRowPatternRole").or(new ElementFilter(
												"ContainedExcelCellPatternRole").or(new ElementFilter("ContainedExcelSheetPatternRole")
														.or(new ElementFilter("EditionPatternInstanceParameter")
																.or(new ElementFilter("MatchEditionPatternInstance")
																		.or(new ElementFilter("CreateEditionPatternInstanceParameter")
																				.or(new ElementFilter("Palette")
																						.or(new ElementFilter("PaletteElement")
																								.or(new ElementFilter("Shema").or(
																										new ElementFilter("ContainedShape")
																												.or(new ElementFilter(
																														"ContainedConnector")
																																.or(new ElementFilter(
																																		"FromShape")
																																				.or(new ElementFilter(
																																						"ToShape")
																																								.or(new ElementFilter(
																																										"Border")
																																												.or(new ElementFilter(
																																														"AddEditionPatternInstance")
																																																.or(new ElementFilter(
																																																		"AddEditionPatternInstanceParameter")
																																																				.or(new ElementFilter(
																																																						"AddressedSelectEditionPatternInstance")
																																																								.or(new ElementFilter(
																																																										"AddressedSelectFlexoConceptInstance")))))))))))))))))))))))))
				.hasNext()) {
			return true;
		}
		return false;
	}

	private static class ViewPointInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
	}

	private static ViewPointInfo findViewPointInfo(InputStream inputStream) {
		Document document;
		try {
			document = readXMLInputStream(inputStream);
			Element root = getElement(document, "ViewPoint");
			if (root != null) {
				ViewPointInfo returned = new ViewPointInfo();
				Iterator<Attribute> it = root.getAttributes().iterator();
				while (it.hasNext()) {
					Attribute at = it.next();
					if (at.getName().equals("uri")) {
						logger.fine("Returned " + at.getValue());
						returned.uri = at.getValue();
					} else if (at.getName().equals("name")) {
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
					if (StringUtils.isNotEmpty(returned.uri)) {
						if (returned.uri.indexOf("/") > -1) {
							returned.name = returned.uri.substring(returned.uri.lastIndexOf("/") + 1);
						} else if (returned.uri.indexOf("\\") > -1) {
							returned.name = returned.uri.substring(returned.uri.lastIndexOf("\\") + 1);
						} else {
							returned.name = returned.uri;
						}
					}
				}
				return returned;

			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}

	// TODO, reimplement with input stream
	public static void convertViewPoint(ViewPointResource viewPointResource) {

		File viewPointDirectory = ResourceLocator.retrieveResourceAsFile(viewPointResource.getDirectory());
		File xmlFile = (File) viewPointResource.getFlexoIODelegate().getSerializationArtefact();// getFile();

		logger.info("Converting " + viewPointDirectory.getAbsolutePath());

		File diagramSpecificationDir = new File(viewPointDirectory, "DiagramSpecification");
		diagramSpecificationDir.mkdir();

		logger.fine("Creating directory " + diagramSpecificationDir.getAbsolutePath());

		try {
			Document viewPointDocument = XMLUtils.readXMLFile(xmlFile);
			Document diagramSpecificationDocument = XMLUtils.readXMLFile(xmlFile);

			for (File f : viewPointDirectory.listFiles()) {
				if (!f.equals(xmlFile) && !f.equals(diagramSpecificationDir) && !f.getName().endsWith("~")) {
					if (f.getName().endsWith(".shema")) {
						try {
							File renamedExampleDiagramFile = new File(f.getParentFile(),
									f.getName().substring(0, f.getName().length() - 6) + ".diagram");
							FileUtils.rename(f, renamedExampleDiagramFile);
							f = renamedExampleDiagramFile;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					File destFile = new File(diagramSpecificationDir, f.getName());
					FileUtils.rename(f, destFile);
					logger.fine("Moving file " + f.getAbsolutePath() + " to " + destFile.getAbsolutePath());
				}
				if (f.getName().endsWith("~")) {
					f.delete();
				}
			}

			Element diagramSpecification = XMLUtils.getElement(diagramSpecificationDocument, "ViewPoint");
			diagramSpecification.setName("DiagramSpecification");
			FileOutputStream fos = new FileOutputStream(new File(diagramSpecificationDir, "DiagramSpecification.xml"));
			Format prettyFormat = Format.getPrettyFormat();
			prettyFormat.setLineSeparator(LineSeparator.SYSTEM);
			XMLOutputter outputter = new XMLOutputter(prettyFormat);
			try {
				outputter.output(diagramSpecificationDocument, fos);
			} catch (IOException e) {
				e.printStackTrace();
			}
			fos.flush();
			fos.close();
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		((ViewPointResourceImpl) viewPointResource).exploreVirtualModels(viewPointResource.getDirectory());

	}

	@Override
	public List<VirtualModelResource> getVirtualModelResources() {
		ViewPoint vp = getViewPoint();
		return getContents(VirtualModelResource.class);
	}

	@Override
	public ViewPointLibrary getViewPointLibrary() {
		ViewPointLibrary returned = (ViewPointLibrary) performSuperGetter(VIEW_POINT_LIBRARY);
		if (returned == null && getServiceManager() != null) {
			return getServiceManager().getViewPointLibrary();
		}
		return returned;
	}

	/* TODO reimplements using input streams
	public static void convertViewPoint16ToViewpoint17(ViewPointResource viewPointResource) {
	
		File viewPointDirectory = viewPointResource.getDirectory();
	
		List<File> paletteFiles = new ArrayList<File>();
		List<File> exampleDiagramFiles = new ArrayList<File>();
		List<File> virtualModels = new ArrayList<File>();
	
		logger.info("Converting " + viewPointDirectory.getAbsolutePath());
	
		try {
			for (File f : viewPointResource.getDirectory().listFiles()) {
				if (f.isDirectory()) {
	
					// Initialize files list
					paletteFiles.clear();
					exampleDiagramFiles.clear();
					virtualModels.clear();
	
					// Find palette files if any
					for (File palette : f.listFiles()) {
						if (palette.getName().endsWith(".palette")) {
							paletteFiles.add(palette);
							// Store the old file
							FileUtils.copyFileToFile(palette, new File(f, palette.getName() + ".old16"));
						}
					}
					// Find diagram files if any
					for (File exampleDiagram : f.listFiles()) {
						if (exampleDiagram.getName().endsWith(".diagram")) {
							exampleDiagramFiles.add(exampleDiagram);
							// Store the old file
							FileUtils.copyFileToFile(exampleDiagram, new File(f, exampleDiagram.getName() + ".old16"));
						}
					}
					// Find virtualmodels files if any
					File virtualModelFile = new File(f, f.getName() + ".xml");
					if (virtualModelFile.exists()) {
						virtualModels.add(virtualModelFile);
						// Store the old file
						FileUtils.copyFileToFile(virtualModelFile, new File(f, virtualModelFile.getName() + ".old16"));
					}
					// Execute the convertion
					convertVirtualModels16ToVirtualModels17(viewPointResource, virtualModelFile, paletteFiles, exampleDiagramFiles);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}*/

	private static void convertVirtualModels16ToVirtualModels17(ViewPointResource viewPointResource, File virtualModelFile,
			List<File> paletteFiles, List<File> exampleDiagramFiles) {
		try {
			if (virtualModelFile.exists()) {
				Document d = XMLUtils.readXMLFile(virtualModelFile);
				if (d.getRootElement().getName().equals("VirtualModel")) {
					convertVirtualModel16ToVirtualModel17(d);
					XMLUtils.saveXMLFile(d, virtualModelFile);
				}
				if (d.getRootElement().getName().equals("DiagramSpecification")) {
					convertDiagramSpecification16ToVirtualModel17(virtualModelFile, d, paletteFiles, exampleDiagramFiles,
							viewPointResource);

				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void convertDiagramSpecification16ToVirtualModel17(File file, Document diagram, List<File> oldPaletteFiles,
			List<File> oldExampleDiagramFiles, ViewPointResource viewPointResource) {

		// Create the diagram specification and a virtual model with a diagram typed model slot referencing this diagram specification

		final String ADDRESSED_DIAGRAM_MODEL_SLOT = "AddressedDiagramModelSlot";
		final String MODELSLOT_VIRTUAL_MODEL_MODEL_SLOT = "ModelSlot_VirtualModelModelSlot";
		final String MODELSLOT_TYPED_DIAGRAM_MODEL_SLOT = "ModelSlot_TypedDiagramModelSlot";

		try {
			String diagramName = file.getName().replace(".xml", "");
			// Create a folder that contains the diagram specification
			File diagramSpecificationFolder = new File(file.getParentFile() + "/" + diagramName + ".diagramspecification");
			diagramSpecificationFolder.mkdir();

			// Retrieve diagram drop schemes
			Iterator<Element> dropSchemeElements = diagram.getDescendants(new ElementFilter("DropScheme"));
			List<Element> dropSchemes = IteratorUtils.toList(dropSchemeElements);

			// Retrieve Diagram Model slots references
			Iterator<? extends Content> thisModelSlotsIterator = diagram
					.getDescendants(new ElementFilter("DiagramModelSlot").or(new ElementFilter(ADDRESSED_DIAGRAM_MODEL_SLOT)));
			List<Element> thisModelSlots = IteratorUtils.toList(thisModelSlotsIterator);

			// Retrieve the DiagramModelSlot (this), and transform it to a virtual model slot with a virtual model uri
			int thisID = 0;
			String newThisUri = viewPointResource.getURI() + "/" + diagramName;
			String diagramSpecificationURI = newThisUri + "/" + diagramName + ".diagramspecification";
			Element typedDiagramModelSlot = null;
			boolean foundThis = false;
			for (Element thisMs : thisModelSlots) {
				// Retriev the ID and URI of this DiagramModelSlot
				if (thisMs.getAttribute("name") != null && thisMs.getAttributeValue("name").equals("this") && !foundThis) {
					// Store its ID and its URI
					thisID = thisMs.getAttribute("id").getIntValue();
					if (thisMs.getAttributeValue("virtualModelURI") != null) {
						newThisUri = thisMs.getAttributeValue("virtualModelURI");
						thisMs.removeAttribute("virtualModelURI");
						thisMs.removeAttribute("name");
						thisMs.getAttribute("id").setName("idref");
						thisMs.setAttribute("idref", Integer.toString(thisID));
					}
					// Replace by a Typed model slot
					typedDiagramModelSlot = new Element(MODELSLOT_TYPED_DIAGRAM_MODEL_SLOT);
					typedDiagramModelSlot.setAttribute("metaModelURI", diagramSpecificationURI);
					typedDiagramModelSlot.setAttribute("name", "typedDiagramModelSlot");
					typedDiagramModelSlot.setAttribute("id", Integer.toString(computeNewID(diagram)));
					foundThis = true;
				}
			}
			// Replace the Diagram Model Slot by a Virtual Model Model slot
			for (Element thisMs : thisModelSlots) {
				if (hasSameID(thisMs, thisID) && thisMs.getName().equals("DiagramModelSlot")) {
					thisMs.setName("FMLRTModelSlot");
					thisMs.getAttributes().add(new Attribute("virtualModelURI", newThisUri));
					thisMs.getAttributes().add(new Attribute("name", "virtualModelInstance"));
					thisMs.getAttributes().add(new Attribute("id", Integer.toString(thisID)));
					thisMs.removeAttribute("idref");
				}
			}
			// Update ids for all model slots
			Iterator<? extends Content> diagramModelSlotsIterator = diagram
					.getDescendants(new ElementFilter("DiagramModelSlot").or(new ElementFilter(ADDRESSED_DIAGRAM_MODEL_SLOT)));
			List<Element> thisDiagramModelSlots = IteratorUtils.toList(diagramModelSlotsIterator);
			for (Element diagramMs : thisDiagramModelSlots) {
				if (diagramMs.getAttribute("id") != null && typedDiagramModelSlot != null) {
					diagramMs.setAttribute("id", typedDiagramModelSlot.getAttributeValue("id"));
				}
				if (diagramMs.getAttribute("idref") != null) {
					// Change to TypedDiagramModelSlot
					if (diagramMs.getParentElement().getName().equals("AddShape")
							|| diagramMs.getParentElement().getName().equals("AddConnector")
							|| diagramMs.getParentElement().getName().equals("AddDiagram")
							|| diagramMs.getParentElement().getName().equals("ContainedShapePatternRole")
							|| diagramMs.getParentElement().getName().equals("ContainedConnectorPatternRole")
							|| diagramMs.getParentElement().getName().equals("ContainedDiagramPatternRole")) {
						if (typedDiagramModelSlot.getAttributeValue("id") != null) {
							diagramMs.setAttribute("idref", typedDiagramModelSlot.getAttributeValue("id"));
							diagramMs.setName("TypedDiagramModelSlot");
						}
					} else {
						diagramMs.setName(MODELSLOT_VIRTUAL_MODEL_MODEL_SLOT);
					}
				}
			}
			for (Content content : diagram.getDescendants()) {
				if (content instanceof Element) {
					Element element = (Element) content;
					if (element.getName().equals("AddShape") || element.getName().equals("AddConnector")
							|| element.getName().equals("AddDiagram")) {
						if (element.getChild("TypedDiagramModelSlot") == null && element.getChild("AddressedDiagramModelSlot") == null) {
							Element adressedMsElement = new Element("TypedDiagramModelSlot");
							Attribute newIdRefAttribute = new Attribute("idref", typedDiagramModelSlot.getAttributeValue("id"));
							adressedMsElement.getAttributes().add(newIdRefAttribute);
							element.addContent(adressedMsElement);
						}
					}
				}
			}

			// Update DiagramSpecification URI
			for (Content content : diagram.getDescendants()) {
				if (content instanceof Element) {
					Element element = (Element) content;
					if (element.getAttribute("diagramSpecificationURI") != null) {
						String oldDiagramSpecificationUri = element.getAttributeValue("diagramSpecificationURI");
						String diagramSpecificationName = oldDiagramSpecificationUri.substring(oldDiagramSpecificationUri.lastIndexOf("/"));
						String newDiagramSpecificationUri = oldDiagramSpecificationUri + diagramSpecificationName + ".diagramspecification";
						element.getAttribute("diagramSpecificationURI").setValue(newDiagramSpecificationUri);
					}
				}
			}

			// Change all the "diagram" binding with "this", and "toplevel" with typedDiagramModelSlot.topLevel" in case of not
			// DropSchemeAction
			for (Content content : diagram.getDescendants()) {
				if (content instanceof Element) {
					Element element = (Element) content;
					for (Attribute attribute : element.getAttributes()) {
						if (attribute.getValue().startsWith("diagram")) {
							attribute.setValue(attribute.getValue().replace("diagram", "this"));
						}
						if (attribute.getValue().startsWith("topLevel")) {
							boolean diagramScheme = false;
							Element parentElement = element.getParentElement();
							while (parentElement != null) {
								if (parentElement.getName().equals("DropScheme") || parentElement.getName().equals("LinkScheme")) {
									diagramScheme = true;
								}
								parentElement = parentElement.getParentElement();
							}
							if (!diagramScheme) {
								attribute.setValue(attribute.getValue().replace("topLevel", "virtualModelInstance.typedDiagramModelSlot"));
							}
						}
					}
				}
			}

			// Create the diagram specificaion xml file
			File diagramSpecificationFile = new File(diagramSpecificationFolder, file.getName());
			Document diagramSpecification = new Document();
			Element rootElement = new Element("DiagramSpecification");
			Attribute name = new Attribute("name", diagramName);
			Attribute diagramSpecificationURIAttribute = new Attribute("uri", diagramSpecificationURI);
			diagramSpecification.addContent(rootElement);
			rootElement.getAttributes().add(name);
			rootElement.getAttributes().add(diagramSpecificationURIAttribute);
			XMLUtils.saveXMLFile(diagramSpecification, diagramSpecificationFile);

			// Copy the palette files inside diagram specification repository
			ArrayList<File> newPaletteFiles = new ArrayList<File>();
			for (File paletteFile : oldPaletteFiles) {
				File newFile = new File(diagramSpecificationFolder + "/" + paletteFile.getName());
				FileUtils.rename(paletteFile, newFile);
				newPaletteFiles.add(newFile);
				Document palette = XMLUtils.readXMLFile(newFile);
				Attribute diagramSpecUri = new Attribute("diagramSpecificationURI", diagramSpecificationURI);
				palette.getRootElement().getAttributes().add(diagramSpecUri);
				convertNames16ToNames17(palette);
				XMLUtils.saveXMLFile(palette, newFile);
			}

			// Copy the example diagram files inside diagram specification repository
			ArrayList<File> newExampleDiagramFiles = new ArrayList<File>();
			for (File exampleDiagramFile : oldExampleDiagramFiles) {
				File newFile = new File(diagramSpecificationFolder + "/" + exampleDiagramFile.getName());
				FileUtils.rename(exampleDiagramFile, newFile);
				newExampleDiagramFiles.add(newFile);
				Document exampleDiagram = XMLUtils.readXMLFile(newFile);
				exampleDiagram.getRootElement().setAttribute("uri",
						diagramSpecificationURI + "/" + exampleDiagram.getRootElement().getAttributeValue("name"));
				Attribute diagramSpecUri = new Attribute("diagramSpecificationURI", diagramSpecificationURI);
				exampleDiagram.getRootElement().getAttributes().add(diagramSpecUri);
				exampleDiagram.getRootElement().setAttribute("uri",
						diagramSpecificationURI + "/" + exampleDiagram.getRootElement().getAttributeValue("name"));
				convertNames16ToNames17(exampleDiagram);
				XMLUtils.saveXMLFile(exampleDiagram, newFile);
			}

			// Update the diagram palette element bindings
			ArrayList<Element> paletteElementBindings = new ArrayList<Element>();
			for (File paletteFile : newPaletteFiles) {
				Document palette = XMLUtils.readXMLFile(paletteFile);
				String paletteUri = diagramSpecificationURI + "/" + palette.getRootElement().getAttribute("name").getValue() + ".palette";
				Iterator<? extends Content> paletteElements = palette.getDescendants(new ElementFilter("DiagramPaletteElement"));
				while (paletteElements.hasNext()) {
					Element paletteElement = (Element) paletteElements.next();
					Element binding = createPaletteElementBinding(paletteElement, paletteUri, dropSchemes);
					if (binding != null) {
						paletteElementBindings.add(binding);
					}
				}
				XMLUtils.saveXMLFile(palette, paletteFile);
			}
			// Add the Palette Element Bindings to the TypedDiagramModelSlot
			if (!paletteElementBindings.isEmpty()) {
				typedDiagramModelSlot.addContent(paletteElementBindings);
			}
			if (typedDiagramModelSlot != null) {
				diagram.getRootElement().addContent(typedDiagramModelSlot);
			}

			// Update names
			convertNames16ToNames17(diagram);
			convertOldNameToNewNames("DiagramSpecification", "VirtualModel", diagram);

			// Save the files
			XMLUtils.saveXMLFile(diagram, file);

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void convertVirtualModel16ToVirtualModel17(Document document) {
		convertNames16ToNames17(document);
	}

	private static void convertNames16ToNames17(Document document) {

		// Convert common properties
		addProperty("userID", "FLX", document, null);

		// Edition Patterns
		// Edition patterns name
		convertOldNameToNewNames("EditionPattern", "FlexoConcept", document);
		// Parent pattern property is no more an attribute but an element
		IteratorIterable<? extends Content> fcElementsIterator = document.getDescendants(new ElementFilter("FlexoConcept"));
		List<Element> fcElements = IteratorUtils.toList(fcElementsIterator);

		for (Element fc : fcElements) {
			if (fc.getAttribute("parentEditionPattern") != null) {
				Element parentEp = new Element("ParentFlexoConcept");
				Attribute parentIdRef = new Attribute("idref", getFlexoConceptID(document, fc.getAttributeValue("parentEditionPattern")));
				parentEp.getAttributes().add(parentIdRef);
				fc.removeAttribute("parentEditionPattern");
				fc.addContent(parentEp);
			}
		}

		// Pattern Roles
		// Pattern roles properties
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "ContainedEditionPatternInstancePatternRole");
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "AddressedSelectEditionPatternInstance");
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "SelectEditionPatternInstance");
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "EditionPatternInstanceParameter");
		changePropertyName("paletteElementID", "paletteElementId", document, "FMLDiagramPaletteElementBinding");
		changePropertyName("editionPattern", "flexoConcept", document, "PaletteElement");
		removeProperties("patternRole", document);
		removeProperty("className", document, "DrawingGraphicalRepresentation");
		removeProperty("className", document, "ShapeGraphicalRepresentation");
		removeProperty("className", document, "ConnectorGraphicalRepresentation");
		// Pattern roles name
		convertOldNameToNewNames("ContainedEditionPatternInstancePatternRole", "FlexoConceptInstanceRole", document);
		convertOldNameToNewNames("EditionPatternInstancePatternRole", "FlexoConceptInstanceRole", document);
		convertOldNameToNewNames("ContainedShapePatternRole", "ShapeRole", document);
		convertOldNameToNewNames("ShapePatternRole", "ShapeRole", document);
		convertOldNameToNewNames("ContextShapePatternRole", "ShapeRole", document);
		convertOldNameToNewNames("ParentShapePatternRole", "ParentShapeRole", document);
		convertOldNameToNewNames("ContainedEMFObjectIndividualPatternRole", "EMFObjectIndividualRole", document);
		convertOldNameToNewNames("EMFObjectIndividualPatternRole", "EMFObjectIndividualRole", document);
		convertOldNameToNewNames("ContainedConnectorPatternRole", "ConnectorRole", document);
		convertOldNameToNewNames("ConnectorPatternRole", "ConnectorRole", document);
		convertOldNameToNewNames("ContainedOWLIndividualPatternRole", "OWLIndividualRole", document);
		convertOldNameToNewNames("OWLIndividualPatternRole", "OWLIndividualRole", document);
		convertOldNameToNewNames("ContainedObjectPropertyStatementPatternRole", "ObjectPropertyStatementRole", document);
		convertOldNameToNewNames("ObjectPropertyStatementPatternRole", "ObjectPropertyStatementRole", document);
		convertOldNameToNewNames("ContainedDataPropertyStatementPatternRole", "DataPropertyStatementRole", document);
		convertOldNameToNewNames("DataPropertyStatementPatternRole", "DataPropertyStatementRole", document);
		convertOldNameToNewNames("ContainedExcelCellPatternRole", "ExcelCellRole", document);
		convertOldNameToNewNames("ExcelCellPatternRole", "ExcelCellRole", document);
		convertOldNameToNewNames("ContainedExcelSheetPatternRole", "ExcelSheetRole", document);
		convertOldNameToNewNames("ExcelSheetPatternRole", "ExcelSheetRole", document);
		convertOldNameToNewNames("ContainedExcelRowPatternRole", "ExcelRowRole", document);
		convertOldNameToNewNames("ExcelRowPatternRole", "ExcelRowRole", document);
		convertOldNameToNewNames("ContainedDiagramPatternRole", "DiagramRole", document);
		convertOldNameToNewNames("DiagramPatternRole", "DiagramRole", document);
		convertOldNameToNewNames("ContainedXMLIndividualPatternRole", "XMLIndividualRole", document);
		convertOldNameToNewNames("XMLIndividualPatternRole", "XMLIndividualRole", document);
		convertOldNameToNewNames("ContainedXSIndividualPatternRole", "XSIndividualRole", document);
		convertOldNameToNewNames("XSIndividualPatternRole", "XSIndividualRole", document);
		convertOldNameToNewNames("ContainedXSClassPatternRole", "XSClassRole", document);
		convertOldNameToNewNames("XSClassPatternRole", "XSClassRole", document);

		// Actions
		convertOldNameToNewNames("EditionPatternInstanceParameter", "FlexoConceptInstanceParameter", document);
		convertOldNameToNewNames("MatchEditionPatternInstance", "MatchFlexoConceptInstance", document);
		convertOldNameToNewNames("CreateEditionPatternInstanceParameter", "CreateFlexoConceptInstanceParameter", document);
		// Retrieve Fetch Actions
		IteratorIterable<? extends Content> fetchElementsIterator = document
				.getDescendants(new ElementFilter("FetchRequestIterationAction"));
		List<Element> fetchElements = IteratorUtils.toList(fetchElementsIterator);
		for (Element fetchElement : fetchElements) {
			for (Element child : fetchElement.getChildren()) {
				if (child.getName().equals("AddressedSelectEditionPatternInstance")) {
					child.setName("FetchRequest_SelectFlexoConceptInstance");
				}
				if (child.getName().equals("AddressedSelectEMFObjectIndividual")) {
					child.setName("FetchRequest_SelectEMFObjectIndividual");
				}
				if (child.getName().equals("AddressedSelectIndividual")) {
					child.setName("FetchRequest_SelectIndividual");
				}
				if (child.getName().equals("AddressedSelectExcelCell")) {
					child.setName("FetchRequest_SelectExcelCell");
				}
				if (child.getName().equals("AddressedSelectExcelRow")) {
					child.setName("FetchRequest_SelectExcelRow");
				}
				if (child.getName().equals("AddressedSelectExcelSheet")) {
					child.setName("FetchRequest_SelectExcelSheet");
				}
				if (child.getName().equals("AddressedSelectEditionPatternInstance")) {
					child.setName("FetchRequest_SelectFlexoConceptInstance");
				}
			}
		}
		// Built-in actions
		convertOldNameToNewNames("DeclareFlexoRole", "DeclareFlexoRole", document);
		convertOldNameToNewNames("AddEditionPatternInstance", "AddFlexoConceptInstance", document);
		convertOldNameToNewNames("AddEditionPatternInstanceParameter", "AddFlexoConceptInstanceParameter", document);
		convertOldNameToNewNames("AddressedSelectEditionPatternInstance", "SelectFlexoConceptInstance", document);
		convertOldNameToNewNames("AddressedSelectFlexoConceptInstance", "SelectFlexoConceptInstance", document);
		convertOldNameToNewNames("SelectEditionPatternInstance", "SelectFlexoConceptInstance", document);

		// Model Slots
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if ((element.getParentElement() != null) && (element.getParentElement().getName().equals("DiagramSpecification")
						|| element.getParentElement().getName().equals("VirtualModel"))) {
					if (element.getName().equals("EMFModelSlot")) {
						element.setName("ModelSlot_EMFModelSlot");
					} else if (element.getName().equals("XMLModelSlot")) {
						element.setName("ModelSlot_XMLModelSlot");
					} else if (element.getName().equals("XSDModelSlot")) {
						element.setName("ModelSlot_XSDModelSlot");
					} else if (element.getName().equals("BasicExcelModelSlot")) {
						element.setName("ModelSlot_BasicExcelModelSlot");
					} else if (element.getName().equals("SemanticsExcelModelSlot")) {
						element.setName("ModelSlot_SemanticsExcelModelSlot");
					} else if (element.getName().equals("BasicPowerpointModelSlot")) {
						element.setName("ModelSlot_SemanticsPowerpointModelSlot");
					} else if (element.getName().equals("OWLModelSlot")) {
						element.setName("ModelSlot_OWLModelSlot");
					} else if (element.getName().equals("FMLRTModelSlot")) {
						element.setName("ModelSlot_VirtualModelModelSlot");
					}
				} else {
					if (element.getName().equals("AddressedEMFModelSlot")) {
						element.setName("EMFModelSlot");
					} else if (element.getName().equals("AddressedXMLModelSlot")) {
						element.setName("XMLModelSlot");
					} else if (element.getName().equals("AddressedXSDModelSlot")) {
						element.setName("XSDModelSlot");
					} else if (element.getName().equals("AddressedBasicExcelModelSlot")) {
						element.setName("BasicExcelModelSlot");
					} else if (element.getName().equals("AddressedSemanticsExcelModelSlot")) {
						element.setName("SemanticsExcelModelSlot");
					} else if (element.getName().equals("AddressedBasicPowerpointModelSlot")) {
						element.setName("BasicPowerpointModelSlot");
					} else if (element.getName().equals("AddressedSemanticsPowerpointModelSlot")) {
						element.setName("SemanticsPowerpointModelSlot");
					} else if (element.getName().equals("AddressedOWLModelSlot")) {
						element.setName("OWLModelSlot");
					} else if (element.getName().equals("AddressedDiagramModelSlot")) {
						element.setName("TypedDiagramModelSlot");
					} else if (element.getName().equals("AddressedVirtualModelModelSlot")) {
						element.setName("FMLRTModelSlot");
					}
				}
			}
		}

		// Palettes/ExampleDiagrams
		// Retrieve Connector GRs
		IteratorIterable<? extends Content> connectorGRElementsIterator = document
				.getDescendants(new ElementFilter("ConnectorGraphicalRepresentation"));
		List<Element> connectorGRElements = IteratorUtils.toList(connectorGRElementsIterator);
		for (Element connectorGRElement : connectorGRElements) {
			Element grSpec = null;
			if (connectorGRElement.getChild("RectPolylinConnector") != null) {
				grSpec = connectorGRElement.getChild("RectPolylinConnector");
			} else if (connectorGRElement.getChild("LineConnector") != null) {
				grSpec = connectorGRElement.getChild("LineConnector");
			} else if (connectorGRElement.getChild("CurvedPolylinConnector") != null) {
				grSpec = connectorGRElement.getChild("CurvedPolylinConnector");
			} else if (connectorGRElement.getChild("ArcConnector") != null) {
				grSpec = connectorGRElement.getChild("ArcConnector");
			}
			if (connectorGRElement.getAttribute("startSymbol") != null) {
				Attribute startSymbol = new Attribute("startSymbol", connectorGRElement.getAttributeValue("startSymbol"));
				grSpec.getAttributes().add(startSymbol);
				connectorGRElement.removeAttribute("startSymbol");
			}
			if (connectorGRElement.getAttribute("endSymbol") != null) {
				Attribute endSymbol = new Attribute("endSymbol", connectorGRElement.getAttributeValue("endSymbol"));
				grSpec.getAttributes().add(endSymbol);
				connectorGRElement.removeAttribute("endSymbol");
			}
			if (connectorGRElement.getAttribute("middleSymbol") != null) {
				Attribute middleSymbol = new Attribute("middleSymbol", connectorGRElement.getAttributeValue("middleSymbol"));
				grSpec.getAttributes().add(middleSymbol);
				connectorGRElement.removeAttribute("middleSymbol");
			}
			if (connectorGRElement.getAttribute("startSymbolSize") != null) {
				Attribute startSymbolSize = new Attribute("startSymbolSize", connectorGRElement.getAttributeValue("startSymbolSize"));
				grSpec.getAttributes().add(startSymbolSize);
				connectorGRElement.removeAttribute("startSymbolSize");
			}
			if (connectorGRElement.getAttribute("endSymbolSize") != null) {
				Attribute endSymbolSize = new Attribute("endSymbolSize", connectorGRElement.getAttributeValue("endSymbolSize"));
				grSpec.getAttributes().add(endSymbolSize);
				connectorGRElement.removeAttribute("endSymbolSize");
			}
			if (connectorGRElement.getAttribute("middleSymbolSize") != null) {
				Attribute middleSymbolSize = new Attribute("middleSymbolSize", connectorGRElement.getAttributeValue("middleSymbolSize"));
				grSpec.getAttributes().add(middleSymbolSize);
				connectorGRElement.removeAttribute("middleSymbolSize");
			}
			if (connectorGRElement.getAttribute("relativeMiddleSymbolLocation") != null) {
				Attribute relativeMiddleSymbolLocation = new Attribute("relativeMiddleSymbolLocation",
						connectorGRElement.getAttributeValue("relativeMiddleSymbolLocation"));
				grSpec.getAttributes().add(relativeMiddleSymbolLocation);
				connectorGRElement.removeAttribute("relativeMiddleSymbolLocation");
			}
		}

		convertOldNameToNewNames("Palette", "DiagramPalette", document);
		convertOldNameToNewNames("PaletteElement", "DiagramPaletteElement", document);
		convertOldNameToNewNames("Shema", "Diagram", document);
		convertOldNameToNewNames("ContainedShape", "Shape", document);
		convertOldNameToNewNames("ContainedConnector", "Connector", document);
		convertOldNameToNewNames("FromShape", "StartShape", document);
		convertOldNameToNewNames("ToShape", "EndShape", document);
		convertOldNameToNewNames("Border", "ShapeBorder", document);
		convertOldNameToNewNames("LineConnector", "LineConnectorSpecification", document);
		convertOldNameToNewNames("CurvedPolylinConnector", "CurvedPolylinConnectorSpecification", document);
		convertOldNameToNewNames("RectPolylinConnector", "RectPolylinConnectorSpecification", document);

		removeNamedElements(document, "PrimaryConceptOWLIndividualPatternRole");
		removeNamedElements(document, "StartShapeGraphicalRepresentation");
		removeNamedElements(document, "EndShapeGraphicalRepresentation");
		removeNamedElements(document, "ArtifactFromShapeGraphicalRepresentation");
		removeNamedElements(document, "ArtifactToShapeGraphicalRepresentation");
		removeNamedElements(document, "PrimaryRepresentationConnectorPatternRole");
		removeNamedElements(document, "PrimaryRepresentationShapePatternRole");
		removeNamedElements(document, "PrimaryConceptObjectPropertyStatementPatternRole");
		removeNamedElements(document, "ToShapePatternRole");
		removeNamedElements(document, "StartShapeGraphicalRepresentation");
		removeNamedElements(document, "EndShapeGraphicalRepresentation");

		// Change all "this"
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				for (Attribute attribute : element.getAttributes()) {
					if (attribute.getValue().startsWith("this")) {
						if (element.getName().equals("ModelSlot_VirtualModelModelSlot")) {
							attribute.setValue(attribute.getValue().replace("this", "virtualModelInstance"));
						}
						if (element.getName().equals("FMLRTModelSlot")) {
							attribute.setValue(attribute.getValue().replace("this", "virtualModelInstance"));
						}
						if (attribute.getName().equals("virtualModelInstance")) {
							attribute.setValue(attribute.getValue().replace("this", "virtualModelInstance"));
						} else {
							attribute.setValue(attribute.getValue().replace("this", "flexoBehaviourInstance"));
						}
					}
				}
			}
		}

	}

	private static int computeNewID(Document document) {
		int id = 1;
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (id < retrieveID(element)) {
					id = retrieveID(element) + 1;
				}
			}
		}
		return id + 1;
	}

	private static Element createPaletteElementBinding(Element paletteElement, String paletteUri, List<Element> dropSchemeElements) {
		Attribute ep = null, ds = null, id = null;
		if (paletteElement.getAttribute("flexoConcept") != null) {
			ep = paletteElement.getAttribute("flexoConcept");
		}
		if (paletteElement.getAttribute("dropSchemeName") != null) {
			ds = paletteElement.getAttribute("dropSchemeName");
		}
		if (paletteElement.getAttribute("id") != null) {
			id = paletteElement.getAttribute("id");
		}
		if (ep != null && ds != null) {
			Element paletteElementBinding = new Element("FMLDiagramPaletteElementBinding");
			Attribute paletteElementId = new Attribute("paletteElementID", paletteUri + "#" + ep.getValue() + id.getValue());
			paletteElementBinding.getAttributes().add(paletteElementId);
			// Find cooresponding dropscheme
			for (Element dropScheme : dropSchemeElements) {
				if (dropScheme.getAttributeValue("name").equals(ds.getValue())
						&& dropScheme.getParentElement().getAttributeValue("name").equals(ep.getValue())) {
					Element dropSchemeRef = new Element("DropScheme");
					dropSchemeRef.setAttribute("idref", dropScheme.getAttributeValue("id"));
					paletteElementBinding.addContent(dropSchemeRef);
				}
			}

			// Update Palette Element, change its name, and remove dropscheme flexo concept informations
			paletteElement.setAttribute("name", ep.getValue() + id.getValue());
			paletteElement.removeAttribute(ep);
			paletteElement.removeAttribute(ds);

			return paletteElementBinding;
		}
		return null;
	}

	private static boolean hasSameID(Element element, int id) {
		try {
			if ((element.getAttribute("id") != null && element.getAttribute("id").getIntValue() == id)
					|| (element.getAttribute("idref") != null && element.getAttribute("idref").getIntValue() == id)) {
				return true;
			}
		} catch (DataConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private static int retrieveID(Element element) {
		try {
			if (element.getAttribute("id") != null) {
				return element.getAttribute("id").getIntValue();
			}
			if (element.getAttribute("idref") != null) {
				return element.getAttribute("idref").getIntValue();
			}
		} catch (DataConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private static void convertOldNameToNewNames(String oldName, String newName, Document document) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (element.getName().equals(oldName)) {
					element.setName(newName);
				}
			}
		}
	}

	private static void addProperty(String property, String value, Document document, String elementName) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (elementName == null || elementName.equals(element.getName())) {
					element.setAttribute(property, value);
				}
			}
		}
	}

	private static void removeProperty(String property, Document document, String elementName) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (elementName == null || elementName.equals(element.getName())) {
					element.removeAttribute(property);
				}
			}
		}
	}

	private static void removeProperties(String property, Document document) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				element.removeAttribute(property);
			}
		}
	}

	private static void removeNamedElements(Document document, String elementName) {
		ArrayList<Element> parentElements = new ArrayList<Element>();
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (!element.getChildren(elementName).isEmpty()) {
					parentElements.add(element);
				}
			}
		}
		for (Element parent : parentElements) {
			parent.removeChildren(elementName);
		}
		parentElements = null;
	}

	private static void changePropertyName(String oldPropertyName, String newPropertyName, Document document, String elementName) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (elementName == null || elementName.equals(element.getName())) {
					if (element.getAttribute(oldPropertyName) != null) {
						element.getAttribute(oldPropertyName).setName(newPropertyName);
					}
				}
			}
		}
	}

	private static String retrieveVirtualModelInstanceURI(Document document) {
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (element.getName().equals("AddressedVirtualModelModelSlot") || element.getName().equals("FMLRTModelSlot")) {
					if (element.getAttribute("name") != null
							&& (element.getAttributeValue("name").equals("this")
									|| element.getAttributeValue("name").equals("virtualModelInstance"))
							&& element.getAttribute("virtualModelURI") != null) {
						return element.getAttributeValue("virtualModelURI");
					}
				}
			}
		}
		return null;
	}

	private static String getFlexoConceptID(Document document, String flexoConceptUri) {
		String virtualModelInstanceUri = retrieveVirtualModelInstanceURI(document);
		if (flexoConceptUri.equals(virtualModelInstanceUri)) {
			return document.getRootElement().getAttributeValue("id");
		}
		for (Content content : document.getDescendants()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				if (element.getName().equals("FlexoConcept") || element.getName().equals("EditionPattern")) {
					if ((virtualModelInstanceUri + "#" + element.getAttributeValue("name")).equals(flexoConceptUri)) {
						return element.getAttributeValue("id");
					}
				}
			}
		}
		return null;
	}

}
