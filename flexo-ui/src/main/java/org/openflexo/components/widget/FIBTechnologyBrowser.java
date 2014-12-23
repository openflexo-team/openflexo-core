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
package org.openflexo.components.widget;

import java.util.logging.Logger;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBBrowserView;
import org.openflexo.view.controller.FlexoController;

/**
 * Browser allowing to browse through information intepretable by a {@link TechnologyAdapter}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBTechnologyBrowser<TA extends TechnologyAdapter> extends FIBBrowserView<TechnologyAdapter> {
	static final Logger logger = Logger.getLogger(FIBTechnologyBrowser.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/FIBTechnologyBrowser.fib");

	public FIBTechnologyBrowser(TechnologyAdapter technologyAdapter, FlexoController controller) {
		super(technologyAdapter, controller, FIB_FILE);
	}

	protected FIBTechnologyBrowser(TechnologyAdapter technologyAdapter, FlexoController controller, Resource fibFile) {
		super(technologyAdapter, controller, fibFile);
	}
}