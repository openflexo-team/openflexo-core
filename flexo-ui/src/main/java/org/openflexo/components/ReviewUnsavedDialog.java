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

import javax.swing.JFrame;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
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
	 * Constructor without frame argument: defaut Flexo active Frame will be used
	 * 
	 */
	public ReviewUnsavedDialog(ApplicationContext applicationContext, ResourceManager resourceManager) {
		this(applicationContext, resourceManager, FlexoFrame.getActiveFrame());
	}

	/**
	 * Constructor with JFrame argument
	 * 
	 */
	public ReviewUnsavedDialog(ApplicationContext applicationContext, ResourceManager resourceManager, JFrame frame) {

		super(applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(FIB_FILE_NAME),
				new ResourceSavingInfo(resourceManager), frame, true, FlexoLocalization.getMainLocalizer());
		this.resourceManager = resourceManager;
		setTitle("Save modified resources");

	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void saveSelection() throws SaveResourceExceptionList, SaveResourcePermissionDeniedException {
		getData().saveSelectedResources();
		// _reviewUnsavedModel.saveSelected();
		getResourceManager().deleteFilesToBeDeleted();
	}
}
