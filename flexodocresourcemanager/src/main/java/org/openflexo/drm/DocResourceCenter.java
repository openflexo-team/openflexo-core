/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.openflexo.drm.dm.DocResourceCenterIsModified;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.localization.Language;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationModel;

// TODO: this model should be migrated to PAMELA
@ModelEntity
public class DocResourceCenter extends DocItemFolder implements ResourceData<DocResourceCenter> {

	// Vector of Language objects
	private Vector<Language> languages;

	// Vector of Authors objects
	private Vector<Author> authors;

	private FlexoResource<DocResourceCenter> resource;

	public static final String DOC_RESOURCE_CENTER = "FlexoDocResourceCenter";

	public static final String FLEXO_MODEL = "FlexoModel";

	public static final String FLEXO_TOOL_SET = "FlexoToolSet";

	public static final String ABSTRACT_MODULE = "general module";

	public static final String ABSTRACT_MAIN_PANE = "general main pane";

	public static final String ABSTRACT_CONTROL_PANEL = "general control panel";

	public static final String ABSTRACT_LEFT_VIEW = "general left view";

	public static final String ABSTRACT_RIGHT_VIEW = "general right view";

	public static final String MAIN_PANE_ID = "main-pane";

	public static final String CONTROL_PANEL_ID = "control-panel";

	public static final String LEFT_VIEW_ID = "left-view";

	public static final String RIGHT_VIEW_ID = "right-view";

	private DRMValidationModel _drmValidationModel;

	public DocResourceCenter() {
		super();
		languages = new Vector<>();
		authors = new Vector<>();
	}

	public ValidationModel getDRMValidationModel() {
		if (_drmValidationModel == null) {
			try {
				_drmValidationModel = new DRMValidationModel();
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return _drmValidationModel;
	}

	public Vector<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Vector<Author> authors) {
		this.authors = authors;
		setChanged();
	}

	public void addToAuthors(Author author) {
		authors.add(author);
		setChanged();
	}

	public void removeFromAuthors(Author author) {
		authors.remove(author);
		setChanged();
	}

	public void addToLanguages(Language language) {
		languages.add(language);
		setChanged();
	}

	public void removeFromLanguages(Language language) {
		languages.remove(language);
		setChanged();
	}

	public Vector<Language> getLanguages() {
		return languages;
	}

	public void setLanguages(Vector<Language> languages) {
		this.languages = languages;
		setChanged();
	}

	public static DocResourceCenter createDefaultDocResourceCenter() {
		DocResourceCenter returned = new DocResourceCenter();
		Language english = Language.ENGLISH;
		Language french = Language.FRENCH;
		Author author = Author.createAuthor(System.getProperty("user.name"), "Noname", "Nomail", returned);
		returned.addToLanguages(english);
		returned.addToLanguages(french);
		returned.addToAuthors(author);
		DocItemFolder rootFolder = DocItemFolder.createDocItemFolder(DOC_RESOURCE_CENTER, "Root folder for Flexo documentation", null,
				returned);
		returned.setFolder(rootFolder);
		// Unused DocItemFolder flexoModelFolder =
		DocItemFolder.createDocItemFolder(FLEXO_MODEL, "Description of Flexo model", rootFolder, returned);
		DocItemFolder flexoToolSetFolder = DocItemFolder.createDocItemFolder(FLEXO_TOOL_SET, "Description for the FlexoToolSet application",
				rootFolder, returned);
		DocItemFolder abstractModuleFolder = DocItemFolder.createDocItemFolder(ABSTRACT_MODULE, "Description of what is a module",
				flexoToolSetFolder, returned);
		DocItem abstractMainPaneItem = DocItem.createDocItem(ABSTRACT_MAIN_PANE, "Description of what is the main pane",
				abstractModuleFolder, false);
		DocItem abstractControlPanelItem = DocItem.createDocItem(ABSTRACT_CONTROL_PANEL, "Description of what is the control panel",
				abstractModuleFolder, false);
		DocItem abstractLeftViewItem = DocItem.createDocItem(ABSTRACT_LEFT_VIEW, "Description of what is the left view",
				abstractModuleFolder, false);
		DocItem abstractRightViewItem = DocItem.createDocItem(ABSTRACT_RIGHT_VIEW, "Description of what is the right view",
				abstractModuleFolder, false);
		abstractModuleFolder.getPrimaryDocItem().addToEmbeddingChildItems(abstractMainPaneItem);
		abstractModuleFolder.getPrimaryDocItem().addToEmbeddingChildItems(abstractControlPanelItem);
		abstractModuleFolder.getPrimaryDocItem().addToEmbeddingChildItems(abstractLeftViewItem);
		abstractModuleFolder.getPrimaryDocItem().addToEmbeddingChildItems(abstractRightViewItem);

		/*DocItem item1 = DocItem.createDocItem("Item1","a description for the item1",folder1,returned);
		item1.setTitle("This is a title for item 1",english);
		item1.setTitle("C'est un titre pour l'item 1",french);
		DocItemVersion version = DocItemVersion.createVersion(item1,"1.0",english,"hop en full","hop en short",returned);
		item1.submitVersion(version,author,returned);*/
		return returned;
	}

	public DocItemFolder getModelFolder() {
		return getFolder().getItemFolderNamed("FlexoModel");
	}

	public DocItemFolder getFTSFolder() {
		return getFolder().getItemFolderNamed("FlexoToolSet");
	}

	/*public void save() {
		FileOutputStream out = null;
		try {
			initializeSerialization();
			out = new FileOutputStream(DocResourceManager.instance().getDRMFile());
			String old = StringEncoder.getDateFormat();
			StringEncoder.setDateFormat("HH:mm:ss dd/MM/yyyy");
			XMLCoder.encodeObjectWithMapping(this, DocResourceManager.getDRMMapping(), out);
			StringEncoder.setDateFormat(old);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finalizeSerialization();
		}
		setChanged();
		notifyObservers(new DocResourceCenterIsSaved());
		clearIsModified(false);
	}*/

	/* Unused
	private void initializeSerialization() {
	}
	
	private void finalizeSerialization() {
	}
	*/
	public Author getUser() {
		Author returned = getAuthorNamed(System.getProperty("user.name"));
		if (returned == null) {
			returned = Author.createAuthor(System.getProperty("user.name"), "Noname", "Nomail", this);
		}
		return returned;
	}

	public Author getAuthorNamed(String authorIdentifier) {
		for (Enumeration<Author> en = getAuthors().elements(); en.hasMoreElements();) {
			Author next = en.nextElement();
			if (next.getIdentifier().equals(authorIdentifier)) {
				return next;
			}
		}
		return null;
	}

	public Language getLanguageNamed(String languageIdentifier) {
		for (Enumeration<Language> en = getLanguages().elements(); en.hasMoreElements();) {
			Language next = en.nextElement();
			if (next.getIdentifier().equalsIgnoreCase(languageIdentifier)) {
				return next;
			}
		}
		return null;
	}

	@Override
	public DocItem getItemNamed(String aName) {
		if (getFolder() == null) {
			return null;
		}
		return getFolder().getItemNamed(aName);
	}

	@Override
	public FlexoResource<DocResourceCenter> getResource() {
		return resource;
	}

	@Override
	public void setResource(FlexoResource<DocResourceCenter> resource) {
		this.resource = resource;
	}

	@Override
	public synchronized void setIsModified() {
		boolean notifyObservers = false;
		if (!isModified()) {
			notifyObservers = true;
		}
		super.setIsModified();
		if (notifyObservers) {
			setChanged();
			notifyObservers(new DocResourceCenterIsModified());
			setChanged();
		}
	}

	public List<? extends DRMObject> getAllFoldersAndItems() {
		// return getAllEmbeddedValidableObjects();
		// TODO
		return null;
	}

	public List<DocItem> getAllItems() {
		List<DocItem> returned = new ArrayList<>();
		for (DRMObject next : getAllFoldersAndItems()) {
			if (next instanceof DocItem) {
				returned.add((DocItem) next);
			}
		}
		return returned;
	}

	/**
	 * Overrides getIdentifier
	 * 
	 * @see org.openflexo.drm.DRMObject#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "DocResourceCenter";
	}

}
