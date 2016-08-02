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

package org.openflexo.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.Flexo;
import org.openflexo.components.AboutDialog;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.module.ModuleLoader;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Handles application events, such as quit, about and preferences
 * 
 * @author sguerin
 */
public class FlexoApplicationAdapter extends ApplicationAdapter {

	private static final Logger logger = Logger.getLogger(FlexoApplicationAdapter.class.getPackage().getName());

	private final ApplicationContext applicationContext;

	protected FlexoApplicationAdapter(ApplicationContext applicationContext) {
		super();
		this.applicationContext = applicationContext;
	}

	public ModuleLoader getModuleLoader() {
		return applicationContext.getModuleLoader();
	}

	@Override
	public void handleAbout(ApplicationEvent event) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("handleAbout");
		}
		event.setHandled(true);
		new AboutDialog();
	}

	@Override
	public void handlePreferences(ApplicationEvent event) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("handlePreferences");
		}
		applicationContext.getPreferencesService().showPreferences();
	}

	@Override
	public void handleQuit(ApplicationEvent event) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("handleQuit");
		}
		try {
			getModuleLoader().quit(true);
		} catch (OperationCancelledException e) {
		}
	}

	@Override
	public void handleOpenFile(ApplicationEvent arg0) {
		Flexo.setFileNameToOpen(arg0.getFilename());
	}

}
