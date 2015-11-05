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

package org.openflexo.components;

import java.util.logging.Logger;

import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.utils.FlexoProgressFactory;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.ResourceSavingInfo;

/**
 * Dialog allowing to select resources to save
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class ReviewUnsavedDialog extends JFIBDialog<ResourceSavingInfo> {

	static final Logger logger = Logger.getLogger(ReviewUnsavedDialog.class.getPackage().getName());
	

	public static final Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/Dialog/ReviewUnsavedDialog.fib");

	private final ResourceManager resourceManager;

	/**
	 * Constructor
	 * 
	 */
	public ReviewUnsavedDialog(ResourceManager resourceManager) {

		super(FIBLibrary.instance().retrieveFIBComponent(FIB_FILE_NAME), new ResourceSavingInfo(resourceManager), FlexoFrame.getActiveFrame(),
				true, FlexoLocalization.getMainLocalizer());
		this.resourceManager = resourceManager;
		setTitle("Save modified resources");

	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void saveSelection(FlexoProgressFactory progressFactory) throws SaveResourceExceptionList, SaveResourcePermissionDeniedException {
		getData().saveSelectedResources(progressFactory);
		// _reviewUnsavedModel.saveSelected();
		getResourceManager().deleteFilesToBeDeleted();
	}

	/*public String savedFilesList() {
		return _reviewUnsavedModel.savedFilesList();
	}*/

	/*public static class ReviewUnsavedModel implements HasPropertyChangeSupport {

		private final FlexoEditor editor;
		private final Hashtable<FlexoResource<?>, Boolean> resourcesToSave;

		private final PropertyChangeSupport pcSupport;

		public ReviewUnsavedModel(FlexoEditor editor, Collection<FlexoFileResource<?>> resources) {
			super();
			this.editor = editor;
			pcSupport = new PropertyChangeSupport(this);
			resourcesToSave = new Hashtable<FlexoResource<?>, Boolean>();
			for (FlexoResource<?> r : resources) {
				if (r.isLoaded()) {
					System.out.println("loaded resource " + r + " with " + r.getLoadedResourceData());
				}
				if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
					resourcesToSave.put(r, Boolean.TRUE);
				} else {
					resourcesToSave.put(r, Boolean.FALSE);
				}
			}
		}

		@Override
		public String getDeletedProperty() {
			return null;
		}

		@Override
		public PropertyChangeSupport getPropertyChangeSupport() {
			return pcSupport;
		}

		public Hashtable<FlexoResource<?>, Boolean> getResourcesToSave() {
			return resourcesToSave;
		}

		public boolean isSelected(FlexoResource<?> resource) {
			return resourcesToSave.get(resource);
		}

		public void setSelected(boolean selected, FlexoResource<?> resource) {
			System.out.println("setSelected " + selected + " resource=" + resource);
			resourcesToSave.put(resource, selected);
			pcSupport.firePropertyChange("getNbOfFilesToSave()", -1, getNbOfFilesToSave());
		}

		public Icon getIcon(FlexoResource<?> resource) {
			return IconLibrary.getIconForResource(resource);
		}

		public int getNbOfFilesToSave() {
			int nbOfFilesToSave = 0;
			for (FlexoResource<?> r : resourcesToSave.keySet()) {
				if (resourcesToSave.get(r)) {
					nbOfFilesToSave++;
				}
			}
			return nbOfFilesToSave;
		}

		public void saveSelected() throws SaveResourceExceptionList, SaveResourcePermissionDeniedException {

			SaveResourceExceptionList listOfRaisedExceptions = null;

			int nbOfFilesToSave = getNbOfFilesToSave();

			if (nbOfFilesToSave > 0) {

				FlexoProgress progress = editor.getFlexoProgressFactory().makeFlexoProgress(
						FlexoLocalization.localizedForKey("saving_selected_resources"), nbOfFilesToSave);

				for (FlexoResource<?> r : resourcesToSave.keySet()) {
					if (resourcesToSave.get(r)) {
						try {
							r.save(progress);
						} catch (SaveResourceException e) {
							if (listOfRaisedExceptions == null) {
								listOfRaisedExceptions = new SaveResourceExceptionList(e);
							} else {
								listOfRaisedExceptions.registerNewException(e);
							}
							e.printStackTrace();
						}
					}
				}

				progress.hideWindow();

				if (listOfRaisedExceptions != null) {
					throw listOfRaisedExceptions;
				}

			}
		}

		private String savedFilesList;

		public String savedFilesList() {
			return savedFilesList;
		}

		public void selectAll() {
			for (FlexoResource<?> r : resourcesToSave.keySet()) {
				if (!resourcesToSave.get(r)) {
					resourcesToSave.put(r, Boolean.TRUE);
				}
			}
		}

		public void deselectAll() {
			for (FlexoResource<?> r : resourcesToSave.keySet()) {
				if (resourcesToSave.get(r)) {
					resourcesToSave.put(r, Boolean.FALSE);
				}
			}
		}
	}*/

}
