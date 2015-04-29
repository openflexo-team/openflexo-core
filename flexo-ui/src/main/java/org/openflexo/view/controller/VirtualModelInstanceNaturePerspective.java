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

package org.openflexo.view.controller;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * A perspective representing all the resources interpretable by a {@link TechnologyAdapter}
 * 
 * @author sylvain
 * 
 * @param <TA>
 */
public class VirtualModelInstanceNaturePerspective extends TechnologyPerspective<FMLRTTechnologyAdapter> {

	static final Logger logger = Logger.getLogger(VirtualModelInstanceNaturePerspective.class.getPackage().getName());

	private final VirtualModelInstanceNature nature;

	/**
	 * @param controller
	 * @param name
	 */
	public VirtualModelInstanceNaturePerspective(VirtualModelInstanceNature nature, FMLRTTechnologyAdapter technologyAdapter,
			FlexoController controller) {
		super(technologyAdapter, controller);
		this.nature = nature;
	}

	/**
	 * Internally called to make technology browser<br>
	 * This job is delegated to the {@link TechnologyAdapterController}
	 * 
	 * @return
	 */
	@Override
	protected FIBTechnologyBrowser<FMLRTTechnologyAdapter> makeTechnologyBrowser() {
		return super.makeTechnologyBrowser();
	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return getController().iconForObject(nature);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof VirtualModelInstance && nature.hasNature((VirtualModelInstance) object)) {
			return true;
		}
		return false;
	}

}
