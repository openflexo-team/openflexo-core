/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.drm;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.openflexo.ApplicationContext;
import org.openflexo.drm.action.ApproveVersion;
import org.openflexo.drm.action.RefuseVersion;
import org.openflexo.drm.action.SubmitVersion;
import org.openflexo.drm.dm.DocResourceCenterIsSaved;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.localization.Language;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.module.FlexoModule;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.view.FlexoMainPane;
import org.openflexo.view.ModuleView;

/**
 * This is a service allowing to manage documentation
 * 
 * @author sylvain
 * 
 */
public class DocResourceManager extends FlexoServiceImpl {

	private static final Logger logger = Logger.getLogger(DocResourceManager.class.getPackage().getName());

	/**
	 * Hashtable storing edited FlexoVersion (not yet stored in DocItem), and where keys are DocItem instances
	 */
	private final Hashtable<DocItem, DocItemVersion> _editedDocItems;
	private final Vector<DocItemVersion> _versionsToEventuallySave;
	private DocSubmissionReport _sessionSubmissions;

	private DocResourceCenter docResourceCenter;

	private DRMModelFactory drmModelFactory;
	private DocResourceCenterResource drcResource;

	public DocResourceManager() {
		super();
		_editedDocItems = new Hashtable<>();
		_versionsToEventuallySave = new Vector<>();
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	@Override
	public String getServiceName() {
		return "DocResourceManager";
	}

	private FlexoEditor _editor;

	private void load() {

		try {
			drcResource.loadResourceData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
	}

	public void save() {

		try {
			drcResource.save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
		docResourceCenter.setChanged();
		docResourceCenter.notifyObservers(new DocResourceCenterIsSaved());
		/*for (DocItemVersion next : _versionsToEventuallySave) {
			if (next.needsSaving()) {
				next.save();
			}
		}*/
		docResourceCenter.clearIsModified(false);
		isSaving = false;
	}

	public DocSubmissionReport getSessionSubmissions() {
		return _sessionSubmissions;
	}

	public boolean needSaving() {
		if (docResourceCenter == null) {
			return false;
		}
		if (docResourceCenter.isModified()) {
			return true;
		}
		/*for (DocItemVersion next : _versionsToEventuallySave) {
			if (next.needsSaving()) {
				return true;
			}
		}*/
		return false;
	}

	public DocResourceCenter getDocResourceCenter() {
		return docResourceCenter;
	}

	private File drmFile;
	private boolean isSaving;

	public File getDRMFile() {
		if (drmFile == null) {
			Resource drmResource = ResourceLocator.locateResource("DocResourceCenter.xml");
			if (drmResource == null) {
				logger.info("DRM File not found");
				/*try {
					drmResource = new FileResourceImpl(
							new File(((FileResourceImpl) getDocResourceCenterDirectory()).getFile(), "DocResourceCenter.xml"));
				} catch (Exception e) {
					logger.severe("Unable to create DocResourceCenter files");
					e.printStackTrace();
				}
				drmFile = ((FileResourceImpl) drmResource).getFile();*/
			}
			else if (drmResource instanceof FileResourceImpl) {
				logger.info("Found DRM File : " + drmResource.getURI());
				drmFile = ((FileResourceImpl) drmResource).getFile();
			}
		}
		if (drmFile != null && !drmFile.exists() && !isSaving) {
			docResourceCenter = DocResourceCenter.createDefaultDocResourceCenter();
			save();
		}
		return drmFile;
	}

	private Resource drmDirectory;

	public Resource getDocResourceCenterDirectory() {
		if (drmDirectory == null) {
			try {
				drmDirectory = new FileResourceImpl(getDRMFile().getParentFile());
			} catch (Exception e) {
				logger.severe("Unable to find DocResourceCenterDirectory");
				e.printStackTrace();
			}
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Doc Resource Center Directory: " + drmDirectory.getURI());
			}
		}
		return drmDirectory;
	}

	/*public DocItem importInspector(InspectorGroup inspectorGroup, String inspectorName, InspectorModel inspectorModel) {
		DocItem inspectorDocItem = getDocResourceCenter().getItemNamed(inspectorName);
		DocItemFolder inspectorGroupFolder;
		if (inspectorDocItem == null) {
			DocItemFolder modelFolder = getDocResourceCenter().getModelFolder();
			String inspectorGroupName = inspectorGroup.getName();
			inspectorGroupFolder = modelFolder.getItemFolderNamed(inspectorGroupName);
			if (inspectorGroupFolder == null) {
				inspectorGroupFolder = DocItemFolder.createDocItemFolder(inspectorGroupName, "No description", modelFolder,
						getDocResourceCenter());
			}
			if (inspectorGroupFolder.getItemNamed(inspectorName) == null) {
				logger.fine("Add entry for " + inspectorName + " in documentation !");
				inspectorDocItem = DocItem.createDocItem(inspectorName, "No description", inspectorGroupFolder, getDocResourceCenter(),
						false);
				InspectorModel parentInspector = inspectorModel.getSuperInspector();
				if (parentInspector != null) {
					String superInspectorName = inspectorModel.superInspectorName;
					superInspectorName = superInspectorName.substring(0, superInspectorName.lastIndexOf(".inspector"));
					DocItem parentItem = getDocItemForInspector(superInspectorName);
					if (parentItem == null) {
						parentItem = importInspector(inspectorGroup, superInspectorName, parentInspector);
					}
					parentItem.addToInheritanceChildItems(inspectorDocItem);
				}
			}
		} else {
			logger.fine("Found entry for " + inspectorName + " in documentation !");
			inspectorGroupFolder = inspectorDocItem.getFolder();
		}
		if (inspectorDocItem != null) {
			for (TabModel tabModel : inspectorModel.getTabs().values()) {
				for (PropertyModel propertyModel : tabModel.getProperties().values()) {
					String propName = inspectorName + "-" + propertyModel.name;
					DocItem propertyDocItem = inspectorGroupFolder.getItemNamed(propName);
					if (propertyDocItem == null) {
						logger.fine("Add entry for " + propName + " in documentation !");
						propertyDocItem = DocItem.createDocItem(propName, "No description", inspectorGroupFolder, getDocResourceCenter(),
								true);
						inspectorDocItem.addToEmbeddingChildItems(propertyDocItem);
					} else {
						logger.fine("Found entry for " + propName + " in documentation !");
					}
				}
			}
		}
		return inspectorDocItem;
	}
	*/
	protected DocItem getDocItemForInspector(String inspectorName) {
		return getDocResourceCenter().getItemNamed(inspectorName);
	}

	public boolean isEdited(DocItem docItem) {
		return getEditedVersion(docItem) != null;
	}

	public boolean isSubmitting(DocItem docItem) {
		return getEditedVersion(docItem) != null && !docItem.getVersions().contains(getEditedVersion(docItem));
	}

	public DocItemVersion getEditedVersion(DocItem docItem) {
		return _editedDocItems.get(docItem);
	}

	public void beginVersionSubmission(DocItem docItem, Language language) {
		DocItemVersion version = new DocItemVersion();
		version.setDocItem(docItem);
		version.setLanguage(language);
		_editedDocItems.put(docItem, version);
		_versionsToEventuallySave.add(version);
	}

	public DocItemAction endVersionSubmission(DocItem docItem) {
		logger.warning("This implementation is not correct: you should not use FlexoAction primitive from the model !");
		// TODO: Please implement this better later
		// Used editor will be null
		SubmitVersion action = SubmitVersion.actionType.makeNewAction(docItem, null, getEditor());
		action.setAuthor(getUser());
		action.setVersion(getEditedVersion(docItem));
		action.doAction();
		_editedDocItems.remove(docItem);
		return action.getNewAction();
	}

	public DocItemVersion beginVersionReview(DocItemVersion docItemVersion) {
		FlexoVersion lastVersionId = docItemVersion.getVersion();
		FlexoVersion newVersionId = new FlexoVersion(lastVersionId.major, lastVersionId.minor, lastVersionId.patch + 1, 0, false, false);
		DocItemVersion version = new DocItemVersion();
		version.setDocItem(docItemVersion.getDocItem());
		version.setVersion(newVersionId);
		version.setLanguage(docItemVersion.getLanguage());
		version.setShortHTMLDescription(docItemVersion.getShortHTMLDescription());
		version.setFullHTMLDescription(docItemVersion.getFullHTMLDescription());
		_editedDocItems.put(docItemVersion.getDocItem(), version);
		_versionsToEventuallySave.add(version);
		return version;
	}

	public DocItemAction endVersionReview(DocItem docItem) {
		logger.warning("This implementation is not correct: you should not use FlexoAction primitive from the model !");
		// TODO: Please implement this better later
		// Used editor will be null
		SubmitVersion action = SubmitVersion.actionType.makeNewAction(docItem, null, getEditor());
		action.setAuthor(getUser());
		action.setVersion(getEditedVersion(docItem));
		action.doAction();
		_editedDocItems.remove(docItem);
		return action.getNewAction();

		/*DocItemAction returned = docItem.reviewVersion(getEditedVersion(docItem),getUser(),getDocResourceCenter());
		_editedDocItems.remove(docItem);
		return returned;*/
	}

	public DocItemAction approveVersion(DocItemVersion version) {
		logger.warning("This implementation is not correct: you should not use FlexoAction primitive from the model !");
		// TODO: Please implement this better later
		// Used editor will be null
		ApproveVersion action = ApproveVersion.actionType.makeNewAction(version.getDocItem(), null, getEditor());
		action.setAuthor(getUser());
		action.setVersion(version);
		action.doAction();
		return action.getNewAction();

		// return version.getItem().approveVersion(version,getUser(),getDocResourceCenter());
	}

	public DocItemAction refuseVersion(DocItemVersion version) {
		logger.warning("This implementation is not correct: you should not use FlexoAction primitive from the model !");
		// TODO: Please implement this better later
		// Used editor will be null
		RefuseVersion action = RefuseVersion.actionType.makeNewAction(version.getDocItem(), null, getEditor());
		action.setAuthor(getUser());
		action.setVersion(version);
		action.doAction();
		return action.getNewAction();

		// return version.getItem().refuseVersion(version,getUser(),getDocResourceCenter());
	}

	public Author getUser() {
		return getDocResourceCenter().getUser();
	}

	public Language getLanguage(org.openflexo.localization.Language language) {
		return Language.ENGLISH;
		// TODO repair this when DocResourceCenter will be set up again
		// return getDocResourceCenter().getLanguageNamed(language.getName());
	}

	public void editVersion(DocItemVersion version) {
		_editedDocItems.put(version.getDocItem(), version);
		_versionsToEventuallySave.add(version);
	}

	public void stopEditVersion(DocItemVersion version) {
		_editedDocItems.remove(version.getDocItem());
	}

	// ================================================
	// ============ Access to documentation ===========
	// ================================================

	public DocItem getDocResourceCenterItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		return getDocResourceCenter().getItemNamed(DocResourceCenter.DOC_RESOURCE_CENTER);
	}

	public DocItem getFlexoModelItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		return getDocResourceCenter().getItemNamed(DocResourceCenter.FLEXO_MODEL);
	}

	public DocItem getFlexoToolSetItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		return getDocResourceCenter().getItemNamed(DocResourceCenter.FLEXO_TOOL_SET);
	}

	public DocItem getAbstractModuleItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		DocItemFolder f = getDocResourceCenter().getFTSFolder().getItemFolderNamed(DocResourceCenter.ABSTRACT_MODULE);
		if (f == null) {
			f = DocItemFolder.createDocItemFolder(DocResourceCenter.ABSTRACT_MODULE, "Description of what is a module",
					getDocResourceCenter().getFTSFolder(), getDocResourceCenter());
		}
		DocItem it = getDocResourceCenter().getItemNamed(DocResourceCenter.ABSTRACT_MODULE);
		if (it == null) {
			f.createDefaultPrimaryDocItem();
		}
		return it;
	}

	public DocItem getAbstractMainPaneItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		DocItem item = getDocResourceCenter().getItemNamed(DocResourceCenter.ABSTRACT_MAIN_PANE);
		if (item == null) {
			item = DocItem.createDocItem(DocResourceCenter.ABSTRACT_MAIN_PANE, "Description of what is the main pane",
					getAbstractModuleItem().getFolder(), false);
			getAbstractModuleItem().addToEmbeddingChildItems(item);
		}
		return item;
	}

	public DocItem getAbstractControlPanelItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		DocItem item = getDocResourceCenter().getItemNamed(DocResourceCenter.ABSTRACT_CONTROL_PANEL);
		if (item == null) {
			item = DocItem.createDocItem(DocResourceCenter.ABSTRACT_CONTROL_PANEL, "Description of what is the control panel",
					getAbstractModuleItem().getFolder(), false);
			getAbstractModuleItem().addToEmbeddingChildItems(item);
		}
		return item;
	}

	public DocItem getAbstractLeftViewItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		DocItem item = getDocResourceCenter().getItemNamed(DocResourceCenter.ABSTRACT_LEFT_VIEW);
		if (item == null) {
			item = DocItem.createDocItem(DocResourceCenter.ABSTRACT_LEFT_VIEW, "Description of what is the left view",
					getAbstractModuleItem().getFolder(), false);
			getAbstractModuleItem().addToEmbeddingChildItems(item);
		}
		return item;
	}

	public DocItem getAbstractRightViewItem() {
		if (getDocResourceCenter() == null) {
			return null;
		}
		DocItem item = getDocResourceCenter().getItemNamed(DocResourceCenter.ABSTRACT_RIGHT_VIEW);
		if (item == null) {
			item = DocItem.createDocItem(DocResourceCenter.ABSTRACT_RIGHT_VIEW, "Description of what is the right view",
					getAbstractModuleItem().getFolder(), false);
			getAbstractModuleItem().addToEmbeddingChildItems(item);
		}
		return item;
	}

	public DocItem getDocItemFor(FlexoObject object) {
		if (getDocResourceCenter() == null) {
			return null;
		}
		return getDocResourceCenter().getItemNamed(object.getClass().getSimpleName());
	}

	public DocItem getDocItemFor(Class<? extends FlexoObject> objectClass) {
		if (getDocResourceCenter() == null) {
			return null;
		}
		return getDocResourceCenter().getItemNamed(objectClass.getSimpleName());
	}

	// TODO: is it to be rewritten
	/*public DocItem getDocItemFor(InspectorModel inspectorModel) {
		String itemIdentifier = inspectorModel.inspectorName;
		return getDocResourceCenter().getItemNamed(itemIdentifier);
	}
	
	public DocItem getDocItemFor(PropertyModel propertyModel) {
		InspectorModel inspectorModel = propertyModel.getInspectorModel();
	
		InspectorModel currentInspectorModel = propertyModel.getInspectorModel();
		DocItem returned = null;
	
		while (returned == null && currentInspectorModel != null) {
			String itemIdentifier = currentInspectorModel.inspectorName + "-" + propertyModel.name;
			returned = getDocResourceCenter().getItemNamed(itemIdentifier);
			currentInspectorModel = currentInspectorModel.getSuperInspector();
		}
	
		return returned;
	
	}*/

	public DocItem getDocItemWithId(String itemIdentifier) {
		if (getDocResourceCenter() == null) {
			return null;
		}
		if (itemIdentifier != null) {
			return getDocResourceCenter().getItemNamed(itemIdentifier);
		}
		else
			return null;
	}

	public DocItem getDocItem(String itemIdentifier) {
		return getDocItemWithId(itemIdentifier);
	}

	// ================================================
	// ============ Import facilities ===========
	// ================================================

	public void importDocSubmissionReport(DocSubmissionReport docSubmissionReport, Vector<?> actionsToImport) {
		Vector<?> actions;
		if (actionsToImport == null) {
			actions = docSubmissionReport.getSubmissionActions();
		}
		else {
			actions = actionsToImport;
		}
		for (Enumeration<?> en = actions.elements(); en.hasMoreElements();) {
			DocItemAction action = (DocItemAction) en.nextElement();
			importDocSubmissionAction(action);
		}
	}

	private void importDocSubmissionAction(DocItemAction action) {
		logger.info("Import action " + action.getLocalizedName());
		DocItem parsedItem = action.getItem();
		DocItem existingItem = getDocResourceCenter().getItemNamed(parsedItem.getIdentifier());
		if (existingItem != null) {
			FlexoVersion newVersion = action.getVersion().getVersion();
			// while (existingItem.getVersion(newVersion) != null) {
			logger.info("Version " + newVersion + " already exist");
			newVersion = new FlexoVersion(newVersion.major, newVersion.minor, newVersion.patch + 1, 0, false, false);
			logger.info("Using version " + newVersion);
			action.getVersion().setVersion(newVersion);
			// }
			for (Enumeration<?> en = getDocResourceCenter().getLanguages().elements(); en.hasMoreElements();) {
				Language lang = (Language) en.nextElement();
				if (parsedItem.getTitle(lang) != null) {
					existingItem.setTitle(parsedItem.getTitle(lang), lang);
				}
			}
			// action.setItem(existingItem);
			// action.getVersion().setItem(existingItem);
			existingItem.addToActions(action);
			existingItem.addToVersions(action.getVersion());
			_versionsToEventuallySave.add(action.getVersion());
			// action.getVersion().setNeedsSaving();
		}
		else {
			logger.warning("Unable to import action: item is not locally registered (" + parsedItem.getIdentifier()
					+ "). Please implement this feature.");
		}
	}

	public FlexoEditor getEditor() {
		if (_editor == null) {
			_editor = new DefaultFlexoEditor(null, null);
		}
		return _editor;
	}

	public void setEditor(FlexoEditor editor) {
		_editor = editor;
	}

	// ================================================
	// ============ Validation management =============
	// ================================================

	@Override
	public void initialize() {
		logger.info("Initialized DocResourceManager service");
		try {
			drmModelFactory = new DRMModelFactory();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		drcResource = DocResourceCenterResourceImpl.retrieveDocResourceCenterResource(this, null);
		load();
		status = Status.Started;
	}

	public DRMModelFactory getDRMModelFactory() {
		return drmModelFactory;
	}

	private final WeakHashMap<JComponent, DocItem> _docForComponent = new WeakHashMap<>();

	private final WeakHashMap<JComponent, String> _pendingComponents = new WeakHashMap<>();

	private static synchronized Window getWindowForComponent(JComponent component) {
		JComponent current = component;
		Container parent;
		while (current.getParent() != null) {
			parent = current.getParent();
			if (parent instanceof JComponent) {
				current = (JComponent) parent;
			}
			else if (parent instanceof Window) {
				return (Window) parent;
			}
			else {
				return null;
			}
		}
		return null;
	}

	private static class SortComponents {
		private final List<JComponent> initialVector;
		private final List<Component> sortedVector;

		protected SortComponents(List<JComponent> sortingSet, Window window) {
			initialVector = sortingSet;
			sortedVector = new ArrayList<>();
			populateFrom(window);
		}

		private void populateFrom(Container component) {
			if (initialVector.contains(component)) {
				sortedVector.add(component);
			}
			for (int i = 0; i < component.getComponentCount(); i++) {
				Component comp = component.getComponent(i);
				if (comp instanceof Container) {
					populateFrom((Container) comp);
				}
				else if (initialVector.contains(comp)) {
					sortedVector.add(comp);
				}
			}
		}

		protected List<Component> getSortedVector() {
			return sortedVector;
		}

	}

	public synchronized void validateWindow(Window window) {
		// logger.info("Validate window");
		List<JComponent> concernedComponents = new ArrayList<>();
		for (Entry<JComponent, String> e : _pendingComponents.entrySet()) {
			JComponent next = e.getKey();
			if (next != null && getWindowForComponent(next) == window) {
				concernedComponents.add(next);
			}
		}
		List<Component> sortedConcernedComponents = new SortComponents(concernedComponents, window).getSortedVector();
		for (Component next : sortedConcernedComponents) {
			validateHelpItem((JComponent) next, _pendingComponents.get(next));
			_pendingComponents.remove(next);
		}
	}

	public synchronized void setHelpItem(final JComponent component, final String anIdentifier) {
		// logger.info("setHelpItem for "+anIdentifier);
		if (component != null && anIdentifier != null) {
			_pendingComponents.put(component, anIdentifier);
		}
	}

	public synchronized void validateHelpItem(final JComponent component, final String anIdentifier) {
		JComponent parent = getClosestDocumentedAncestorComponent(component);
		String identifier = anIdentifier;
		if (parent != null) {
			DocItem parentDocItem = getDocForComponent(parent);
			if (!anIdentifier.startsWith(parentDocItem.getIdentifier())) {
				identifier = parentDocItem.getIdentifier() + "-" + anIdentifier;
			}
			validateHelpItem(component, identifier, parentDocItem);
			return;
		}
		validateHelpItem(component, identifier, null);
	}

	public synchronized void validateHelpItem(final JComponent component, final String anIdentifier, final DocItem parentDocItem) {
		// logger.info(">>>>>>> Validate for "+anIdentifier+" under "+parentDocItem);
		String identifier = anIdentifier;
		if (parentDocItem != null) {
			if (!anIdentifier.startsWith(parentDocItem.getIdentifier())) {
				identifier = parentDocItem.getIdentifier() + "-" + anIdentifier;
			}
		}
		DocItem item = getDocItem(identifier);
		if (item == null) {
			item = createCHEntry(component, identifier, parentDocItem);
		}
		_docForComponent.put(component, item);
		Language lang = getLanguage(getServiceManager().getGeneralPreferences().getLanguage());
		String tooltipText = null;

		DocItem currentItem = item;

		while (tooltipText == null && currentItem != null) {
			if (currentItem.getLastApprovedActionForLanguage(lang) != null) {
				tooltipText = currentItem.getLastApprovedActionForLanguage(lang).getVersion().getShortHTMLDescription();
			}
			currentItem = currentItem.getInheritanceParentItem();
		}

		/*if (item.getLastApprovedActionForLanguage(lang) != null) {
		    tooltipText = item.getLastApprovedActionForLanguage(lang).getVersion().getShortHTMLDescription();
		}
		else {
		    tooltipText = FlexoLocalization.localizedForKeyAndLanguage("no_documentation",GeneralPreferences.getLanguage());
		}*/

		if (tooltipText == null) {
			tooltipText = getServiceManager().getLocalizationService().getFlexoLocalizer().localizedForKeyAndLanguage("no_documentation",
					getServiceManager().getGeneralPreferences().getLanguage());
		}

		component.setToolTipText("<html>" + tooltipText + "</html>");
	}

	public synchronized void setHelpItem(JComponent component, DocItem item) {
		if (item != null) {
			setHelpItem(component, item.getIdentifier());
		}
	}

	private synchronized JComponent getClosestDocumentedAncestorComponent(JComponent component) {
		JComponent parent = null;
		DocItem parentItem = null;
		JComponent current = component;
		while (parentItem == null && current.getParent() != null && current.getParent() instanceof JComponent) {
			current = (JComponent) current.getParent();
			parentItem = getDocForComponent(current);
			if (parentItem != null) {
				parent = current;
			}
		}
		return parent;
	}

	private synchronized DocItem createCHEntry(JComponent component, String identifier, DocItem parentItem) {
		if (parentItem == null) {
			parentItem = getFlexoToolSetItem();
		}
		logger.info("Create entry for " + identifier + " under " + parentItem);
		if (parentItem == null) {
			return null;
		}
		DocItem newEntry = DocItem.createDocItem(identifier, "", parentItem.getFolder(), false);
		parentItem.addToEmbeddingChildItems(newEntry);
		return newEntry;
	}

	public DocItem getDocForComponent(JComponent component) {
		return _docForComponent.get(component);
	}

	public void ensureHelpEntryForModuleHaveBeenCreated(FlexoModule<?> module) {
		// Unused DocItem newModuleItem =
		getDocItemFor(module);
		// Unused DocItem mainPaneItem =
		getMainPaneItemFor(module);
		// Unused DocItem controlPanelItem =
		getControlPanelItemFor(module);
		// Unused DocItem leftViewItem =
		getLeftViewItemFor(module);
		// Unused DocItem rightViewItem =
		getRightViewItemFor(module);

		// Unused FlexoFrame frame =
		module.getFlexoFrame();
		FlexoMainPane mainPane = module.getFlexoController().getMainPane();
		if (mainPane != null) {
			// TODO: restore help on main pane top bar
			// setHelpItem(mainPane.getControlPanel(), controlPanelItem);
		}
	}

	public DocItem getDocItemFor(FlexoModule<?> module) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		String identifier = module.getModule().getHelpTopic();
		if (getDocItem(identifier) == null) {
			DocItemFolder newModuleFolder = DocItemFolder.createDocItemFolder(identifier, "", getFlexoToolSetItem().getFolder(), drc);
			getAbstractModuleItem().addToInheritanceChildItems(newModuleFolder.getPrimaryDocItem());
		}
		return getDocItem(identifier);
	}

	public DocItem getMainPaneItemFor(FlexoModule<?> module) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		String identifier = module.getModule().getHelpTopic();
		String mainPaneId = identifier + "-" + DocResourceCenter.MAIN_PANE_ID;
		DocItem newModuleItem = getDocItemFor(module);
		if (getDocItem(mainPaneId) == null) {
			DocItemFolder mainPaneFolder = DocItemFolder.createDocItemFolder(mainPaneId, "", newModuleItem.getFolder(), drc);
			getAbstractMainPaneItem().addToInheritanceChildItems(mainPaneFolder.getPrimaryDocItem());
			newModuleItem.addToEmbeddingChildItems(mainPaneFolder.getPrimaryDocItem());
		}
		return getDocItem(mainPaneId);

	}

	public DocItem getModuleViewItemFor(FlexoModule<?> module, ModuleView<?> view) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		DocItem mainPaneItem = getMainPaneItemFor(module);
		DocItemFolder folder;
		if (view.getPerspective() != null) {
			String perspectiveIdentifier = view.getPerspective().getName();
			if (getDocItem(perspectiveIdentifier) == null) {
				DocItemFolder perspectiveFolder = DocItemFolder.createDocItemFolder(perspectiveIdentifier,
						"Documentation on that perspective", mainPaneItem.getFolder(), drc);
				mainPaneItem.addToEmbeddingChildItems(perspectiveFolder.getPrimaryDocItem());
			}
			folder = getDocItem(perspectiveIdentifier).getFolder();
		}
		else {
			folder = mainPaneItem.getFolder();
		}
		String moduleViewIdentifier = view.getClass().getName();
		if (moduleViewIdentifier.lastIndexOf('.') > 0) {
			moduleViewIdentifier = moduleViewIdentifier.substring(moduleViewIdentifier.lastIndexOf('.') + 1);
		}
		if (getDocItem(moduleViewIdentifier) == null) {
			DocItemFolder moduleViewFolder = DocItemFolder.createDocItemFolder(moduleViewIdentifier, "Documentation on that view", folder,
					drc);
			mainPaneItem.addToInheritanceChildItems(moduleViewFolder.getPrimaryDocItem());
			if (view.getPerspective() != null && folder.getPrimaryDocItem() != null) {
				moduleViewFolder.getPrimaryDocItem().addToRelatedToItems(folder.getPrimaryDocItem());
			}
		}
		return getDocItem(moduleViewIdentifier);

	}

	public DocItem getControlPanelItemFor(FlexoModule<?> module) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		String identifier = module.getModule().getHelpTopic();
		String controlPanelId = identifier + "-" + DocResourceCenter.CONTROL_PANEL_ID;
		DocItem newModuleItem = getDocItemFor(module);
		if (getDocItem(controlPanelId) == null) {
			DocItemFolder controlPanelFolder = DocItemFolder.createDocItemFolder(controlPanelId, "", newModuleItem.getFolder(), drc);
			getAbstractControlPanelItem().addToInheritanceChildItems(controlPanelFolder.getPrimaryDocItem());
			newModuleItem.addToEmbeddingChildItems(controlPanelFolder.getPrimaryDocItem());
		}
		return getDocItem(controlPanelId);
	}

	public DocItem getLeftViewItemFor(FlexoModule<?> module) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		String identifier = module.getModule().getHelpTopic();
		String leftViewId = identifier + "-" + DocResourceCenter.LEFT_VIEW_ID;
		DocItem newModuleItem = getDocItemFor(module);
		if (getDocItem(leftViewId) == null) {
			DocItemFolder leftViewFolder = DocItemFolder.createDocItemFolder(leftViewId, "", newModuleItem.getFolder(), drc);
			getAbstractLeftViewItem().addToInheritanceChildItems(leftViewFolder.getPrimaryDocItem());
			newModuleItem.addToEmbeddingChildItems(leftViewFolder.getPrimaryDocItem());
		}
		return getDocItem(leftViewId);
	}

	public DocItem getRightViewItemFor(FlexoModule<?> module) {
		DocResourceCenter drc = getDocResourceCenter();
		if (drc == null) {
			return null;
		}
		String identifier = module.getModule().getHelpTopic();
		String rightViewId = identifier + "-" + DocResourceCenter.RIGHT_VIEW_ID;
		DocItem newModuleItem = getDocItemFor(module);
		if (getDocItem(rightViewId) == null) {
			DocItemFolder rightViewFolder = DocItemFolder.createDocItemFolder(rightViewId, "", newModuleItem.getFolder(), drc);
			getAbstractRightViewItem().addToInheritanceChildItems(rightViewFolder.getPrimaryDocItem());
			newModuleItem.addToEmbeddingChildItems(rightViewFolder.getPrimaryDocItem());
		}
		return getDocItem(rightViewId);
	}

	public void clearComponentsHashtable() {
		_docForComponent.clear();
		_pendingComponents.clear();
	}

	public File getDocumentationCssResourceFile() {
		// TODO
		return null;
	}
}
