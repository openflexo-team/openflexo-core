package org.openflexo.foundation.viewpoint.rm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoXMLFileResourceImpl;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.ViewPointModelFactory;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.toolbox.StringUtils;

public abstract class ViewPointResourceImpl extends PamelaResourceImpl<ViewPoint, ViewPointModelFactory> implements ViewPointResource {

	static final Logger logger = Logger.getLogger(FlexoXMLFileResourceImpl.class.getPackage().getName());

	public static ViewPointResource makeViewPointResource(String name, String uri, File viewPointDirectory,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(ViewPointResource.class);
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			String baseName = viewPointDirectory.getName().substring(0, viewPointDirectory.getName().length() - VIEWPOINT_SUFFIX.length());
			File xmlFile = new File(viewPointDirectory, baseName + ".xml");
			returned.setName(name);
			returned.setURI(uri);
			returned.setVersion(new FlexoVersion("0.1"));
			returned.setModelVersion(new FlexoVersion("1.0"));
			returned.setFile(xmlFile);
			returned.setDirectory(viewPointDirectory);

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}

			returned.setServiceManager(serviceManager);
			returned.setFactory(new ViewPointModelFactory(returned));

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ViewPointResource retrieveViewPointResource(File viewPointDirectory, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(ViewPointResource.class);
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			String baseName = viewPointDirectory.getName().substring(0, viewPointDirectory.getName().length() - VIEWPOINT_SUFFIX.length());
			File xmlFile = new File(viewPointDirectory, baseName + ".xml");
			ViewPointInfo vpi = findViewPointInfo(viewPointDirectory);
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
			returned.setFile(xmlFile);
			returned.setDirectory(viewPointDirectory);
			returned.setURI(vpi.uri);
			returned.setName(vpi.name);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			/*boolean hasBeenConverted = false;
			if (StringUtils.isEmpty(vpi.modelVersion)) {
				// This is the old model, convert to new model
				convertViewPoint(viewPointDirectory, xmlFile);
				hasBeenConverted = true;
			}*/

			if (StringUtils.isEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion("0.1"));
			} else {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}

			returned.setFactory(new ViewPointModelFactory(returned));

			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}

			returned.setServiceManager(serviceManager);

			logger.fine("ViewPointResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());

			/*
			 * Will be activitated when the convertion will be fully compliant
			 */
			// convertViewPoint16ToViewpoint17(returned);

			// Now look for virtual models
			returned.exploreVirtualModels();

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void exploreVirtualModels() {
		if (getDirectory().exists() && getDirectory().isDirectory()) {
			for (File f : getDirectory().listFiles()) {
				if (f.isDirectory()) {
					File virtualModelFile = new File(f, f.getName() + ".xml");
					if (virtualModelFile.exists()) {
						// TODO: we must find something more efficient
						try {
							Document d = XMLUtils.readXMLFile(virtualModelFile);
							if (d.getRootElement().getName().equals("VirtualModel")) {
								VirtualModelResource virtualModelResource = VirtualModelResourceImpl.retrieveVirtualModelResource(f,
										virtualModelFile, this, getServiceManager());
								addToContents(virtualModelResource);
							} /*else if (d.getRootElement().getName().equals("DiagramSpecification")) {
								DiagramSpecificationResource diagramSpecificationResource = DiagramSpecificationResourceImpl
										.retrieveDiagramSpecificationResource(f, virtualModelFile, this, getViewPointLibrary());
								addToContents(diagramSpecificationResource);
								}*/
						} catch (JDOMException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
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

		String baseName = getDirectory().getName().substring(0, getDirectory().getName().length() - 10);

		returned.init(baseName,/* getDirectory(), getFile(),*/getViewPointLibrary());

		for (VirtualModel vm : returned.getVirtualModels()) {
			for (FlexoConcept ep : vm.getFlexoConcepts()) {
				ep.finalizeFlexoConceptDeserialization();
			}
			vm.clearIsModified();
		}

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
		return getModelVersion().isLesserThan(new FlexoVersion("1.0"));
	}

	private static class ViewPointInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
	}

	private static ViewPointInfo findViewPointInfo(File viewpointDirectory) {
		Document document;
		try {
			logger.fine("Try to find infos for " + viewpointDirectory);

			String baseName = viewpointDirectory.getName().substring(0, viewpointDirectory.getName().length() - 10);
			File xmlFile = new File(viewpointDirectory, baseName + ".xml");

			if (xmlFile.exists()) {

				document = readXMLFile(xmlFile);
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
			} else {
				logger.warning("While analysing viewpoint candidate: " + viewpointDirectory.getAbsolutePath() + " cannot find file "
						+ xmlFile.getAbsolutePath());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
	}

	public static void convertViewPoint(ViewPointResource viewPointResource) {

		File viewPointDirectory = viewPointResource.getDirectory();
		File xmlFile = viewPointResource.getFile();

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
							File renamedExampleDiagramFile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length() - 6)
									+ ".diagram");
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

		((ViewPointResourceImpl) viewPointResource).exploreVirtualModels();

	}

	@Override
	public List<VirtualModelResource> getVirtualModelResources() {
		ViewPoint vp = getViewPoint();
		return getContents(VirtualModelResource.class);
	}

	@Override
	public boolean delete() {
		if (super.delete()) {
			getServiceManager().getResourceManager().addToFilesToDelete(getDirectory());
			return true;
		}
		return false;
	}

	private static void convertViewPoint16ToViewpoint17(ViewPointResource viewPointResource) {

		File viewPointDirectory = viewPointResource.getDirectory();

		List<Document> palettes = new ArrayList<Document>();
		List<Document> exampleDiagrams = new ArrayList<Document>();
		List<File> virtualModels = new ArrayList<File>();

		logger.info("Converting " + viewPointDirectory.getAbsolutePath());

		/*
		 *  Find the resources
		 */
		try {
			for (File f : viewPointResource.getDirectory().listFiles()) {
				if (f.isDirectory()) {
					// Find palette files if any
					for (File palette : f.listFiles()) {
						if (palette.getName().endsWith(".palette")) {
							palettes.add(XMLUtils.readXMLFile(palette));
						}
					}
					// Find diagram files if any
					for (File exampleDiagram : f.listFiles()) {
						if (exampleDiagram.getName().endsWith(".diagram")) {
							exampleDiagrams.add(XMLUtils.readXMLFile(exampleDiagram));
						}
					}
					// Find virtualmodels files if any
					File virtualModelFile = new File(f, f.getName() + ".xml");
					if (virtualModelFile.exists()) {
						virtualModels.add(virtualModelFile);
					}
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 *  Execute the convertion
		 */
		for (File virtualModelFile : virtualModels) {
			convertVirtualModels16ToVirtualModels17(viewPointResource, virtualModelFile, palettes, exampleDiagrams);
		}

	}

	private static void convertVirtualModels16ToVirtualModels17(ViewPointResource viewPointResource, File virtualModelFile,
			List<Document> palettes, List<Document> exampleDiagrams) {
		try {
			Document d = XMLUtils.readXMLFile(virtualModelFile);
			convertNames16ToNames17(d);
			if (d.getRootElement().getName().equals("DiagramSpecification")) {
				convertDiagramSpecification16ToVirtualModel17(virtualModelFile, d, palettes, exampleDiagrams);
				XMLUtils.saveXMLFile(d, virtualModelFile);
			}
			if (d.getRootElement().getName().equals("VirtualModel")) {
				convertVirtualModel16ToVirtualModel17(d);
				XMLUtils.saveXMLFile(d, virtualModelFile);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void convertDiagramSpecification16ToVirtualModel17(File file, Document diagram, List<Document> palettes,
			List<Document> exampleDiagrams) {
		// Create a new Virtual Model with a TypedDiagramModelSlot
		convertOldNameToNewNames("DiagramSpecification", "VirtualModel", diagram);

		// Create the diagram specification
		File diagramSpecificationFile = new File(file.getParentFile(), file.getName().replace(".xml", "") + ".diagramspecification");
		Document diagramSpecification = new Document();
		Element rootElement = new Element("DiagramSpecification");
		diagramSpecification.addContent(rootElement);

		// Find all example diagrams
		for (Document exampleDiagram : exampleDiagrams) {
			exampleDiagram.getRootElement().setName("Diagram");
			rootElement.addContent(exampleDiagram.cloneContent());
		}

		// Create palettes
		for (Document palette : palettes) {
			rootElement.addContent(palette.cloneContent());
		}

		// Find drop schemes

		// Save the file
		XMLUtils.saveXMLFile(diagramSpecification, diagramSpecificationFile);

	}

	private static void convertVirtualModel16ToVirtualModel17(Document document) {

	}

	// userID="FLX"
	private static void convertNames16ToNames17(Document document) {
		// Convert properties
		convertProperties16ToProperties17(document);

		// Edition Patterns
		convertOldNameToNewNames("EditionPattern", "FlexoConcept", document);
		// Pattern Roles
		convertOldNameToNewNames("ContainedEditionPatternInstancePatternRole", "FlexoConceptInstanceRole", document);
		convertOldNameToNewNames("ContainedEMFObjectIndividualPatternRole", "EMFObjectIndividualRole", document);
		// Model Slots
		convertOldNameToNewNames("EMFModelSlot", "ModelSlot_EMFModelSlot", document);
		convertOldNameToNewNames("XMLModelSlot", "ModelSlot_XMLModelSlot", document);
		convertOldNameToNewNames("XSDModelSlot", "ModelSlot_XSDModelSlot", document);
		convertOldNameToNewNames("BasicExcelModelSlot", "ModelSlot_BasicExcelModelSlot", document);
		convertOldNameToNewNames("SemanticsExcelModelSlot", "ModelSlot_SemanticsExcelModelSlot", document);
		convertOldNameToNewNames("BasicPowerpointModelSlot", "ModelSlot_BasicPowerpointModelSlot", document);
		convertOldNameToNewNames("SemanticsPowerpointModelSlot", "ModelSlot_SemanticsPowerpointModelSlot", document);
		convertOldNameToNewNames("OWLModelSlot", "ModelSlot_OWLModelSlot", document);
		convertOldNameToNewNames("VirtualModelModelSlot", "ModelSlot_VirtualModelModelSlot", document);

		// Connection to ModelSlots
		convertOldNameToNewNames("AddressedEMFModelSlot", "EMFModelSlot", document);
		convertOldNameToNewNames("AddressedXMLModelSlot", "XMLModelSlot", document);
		convertOldNameToNewNames("AddressedXSDModelSlot", "XSDModelSlot", document);
		convertOldNameToNewNames("AddressedBasicExcelModelSlot", "BasicExcelModelSlot", document);
		convertOldNameToNewNames("AddressedSemanticsExcelModelSlot", "SemanticsExcelModelSlot", document);
		convertOldNameToNewNames("AddressedBasicPowerpointModelSlot", "BasicPowerpointModelSlot", document);
		convertOldNameToNewNames("AddressedSemanticsPowerpointModelSlot", "SemanticsPowerpointModelSlot", document);
		convertOldNameToNewNames("AddressedOWLModelSlot", "OWLModelSlot", document);
		convertOldNameToNewNames("AddressedVirtualModelModelSlot", "VirtualModelModelSlot", document);

		// Actions
		convertOldNameToNewNames("AddEditionPatternInstance", "AddFlexoConceptInstance", document);
		convertOldNameToNewNames("AddEditionPatternInstanceParameter", "AddFlexoConceptInstanceParameter", document);
		convertOldNameToNewNames("AddressedSelectEditionPatternInstance", "SelectFlexoConceptInstance", document);
		convertOldNameToNewNames("AddressedSelectFlexoConceptInstance", "SelectFlexoConceptInstance", document);

	}

	private static void convertProperties16ToProperties17(Document document) {
		// All elements
		addProperty("userID", "FLX", document, null);
		// Pattern roles
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "ContainedEditionPatternInstancePatternRole");
		changePropertyName("editionPatternTypeURI", "flexoConceptTypeURI", document, "AddressedSelectEditionPatternInstance");
		removeProperty("patternRole", document, "ContainedEMFObjectIndividualPatternRole");
		removeProperty("patternRole", document, "ContainedEditionPatternInstancePatternRole");
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

}
