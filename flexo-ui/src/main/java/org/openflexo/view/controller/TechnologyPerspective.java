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
package org.openflexo.view.controller;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * A perspective representing all the resources interpretable by a {@link TechnologyAdapter}
 * 
 * @author sylvain
 * 
 * @param <TA>
 */
public class TechnologyPerspective<TA extends TechnologyAdapter> extends FlexoPerspective {

	static final Logger logger = Logger.getLogger(TechnologyPerspective.class.getPackage().getName());

	private final TA technologyAdapter;
	private final FIBTechnologyBrowser<TA> technologyBrowser;

	/**
	 * @param controller
	 * @param name
	 */
	public TechnologyPerspective(TA technologyAdapter, FlexoController controller) {
		super(technologyAdapter.getName(), controller);
		this.technologyAdapter = technologyAdapter;

		technologyBrowser = new FIBTechnologyBrowser<TA>(technologyAdapter, controller);
		setTopLeftView(technologyBrowser);

	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return getController().iconForObject(technologyAdapter);
	}

	public String getWindowTitleforObject(FlexoObject object, FlexoController controller) {
		if (object instanceof TechnologyObject) {
			TechnologyAdapter ta = ((TechnologyObject) object).getTechnologyAdapter();
			TechnologyAdapterController<?> tac = controller.getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			return tac.getWindowTitleforObject((TechnologyObject) object, controller);
		}
		if (object instanceof IFlexoOntologyObject) {
			return ((IFlexoOntologyObject) object).getName();
		}
		if (object != null) {
			return object.toString();
		}
		logger.warning("Unexpected null object here");
		return null;
	}

}
