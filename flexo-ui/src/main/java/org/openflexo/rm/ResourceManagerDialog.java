/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.rm;

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.FileResource;
import org.openflexo.view.FlexoFrame;

/**
 * Tool allowing to manage resources
 * 
 * @author sguerin
 */
public class ResourceManagerDialog extends FIBDialog<ResourceManager> {

	private static final Logger logger = FlexoLogger.getLogger(ResourceManagerDialog.class.getPackage().getName());

	public static final FileResource FIB_FILE = new FileResource("Fib/ResourceManager.fib");

	public ResourceManagerDialog(ApplicationContext context) {
		super(FIBLibrary.instance().retrieveFIBComponent(FIB_FILE), context.getResourceManager(), FlexoFrame.getActiveFrame(), true,
				FlexoLocalization.getMainLocalizer());
		setResizable(false);
	}
}
