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

package org.openflexo.prefs;

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.gina.ApplicationFIBLibrary;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.swing.editor.JFIBEditor;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * This service manages all the {@link FIBComponent} used in the context of application<br>
 * *
 * 
 * @author sguerin
 */
public class ApplicationFIBLibraryService extends FlexoServiceImpl implements FlexoService, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(ApplicationFIBLibraryService.class.getPackage().getName());

	private ApplicationFIBLibrary applicationFIBLibrary;

	private JFIBEditor applicationFIBEditor;

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	@Override
	public String getServiceName() {
		return "ApplicationFIBLibrary";
	}

	@Override
	public void initialize() {
		applicationFIBLibrary = ApplicationFIBLibraryImpl.createInstance(getServiceManager().getTechnologyAdapterService());
		applicationFIBEditor = new JFIBEditor(applicationFIBLibrary);
		SwingViewFactory.INSTANCE.installInteractiveFIBEditor(applicationFIBEditor);
		status = Status.Started;
	}

	public ApplicationFIBLibrary getApplicationFIBLibrary() {
		return applicationFIBLibrary;
	}

	public JFIBEditor getApplicationFIBEditor() {
		return applicationFIBEditor;
	}

	public FIBComponent retrieveFIBComponent(Resource fibResource) {
		return applicationFIBLibrary.retrieveFIBComponent(fibResource);
	}

	public FIBComponent retrieveFIBComponent(Resource fibResource, boolean useCache) {
		return applicationFIBLibrary.retrieveFIBComponent(fibResource, useCache);
	}

	public FIBComponent retrieveFIBComponent(Resource fibResource, boolean useCache, FIBModelFactory factory) {
		return applicationFIBLibrary.retrieveFIBComponent(fibResource, useCache, factory);
	}

	public boolean componentIsLoaded(Resource fibResourcePath) {
		return applicationFIBLibrary.componentIsLoaded(fibResourcePath);
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine("ApplicationFIBLibraryService received notification " + notification + " from " + caller);
	}

}
