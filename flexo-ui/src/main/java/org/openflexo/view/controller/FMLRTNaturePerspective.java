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
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstanceNature;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewNature;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.view.ModuleView;

/**
 * A perspective representing all the resources interpretable by a {@link FMLRTTechnologyAdapter} according to specific natures.<br>
 * Those natures are handled by a specific technology adapter
 * 
 * @author sylvain
 * 
 * @param <TA>
 */
public abstract class FMLRTNaturePerspective extends TechnologyPerspective<FMLRTTechnologyAdapter> {

	static final Logger logger = Logger.getLogger(FMLRTNaturePerspective.class.getPackage().getName());

	private final TechnologyAdapter handlingTechnologyAdapter;
	private final ViewNature viewNature;
	private final VirtualModelInstanceNature virtualModelInstanceNature;
	private final FlexoConceptInstanceNature flexoConceptInstanceNature;

	public FMLRTNaturePerspective(ViewNature viewNature, VirtualModelInstanceNature virtualModelInstanceNature,
			FlexoConceptInstanceNature flexoConceptInstanceNature, FMLRTTechnologyAdapter fmlRTtechnologyAdapter,
			TechnologyAdapter handlingTechnologyAdapter, FlexoController controller) {
		super(fmlRTtechnologyAdapter, controller);
		this.handlingTechnologyAdapter = handlingTechnologyAdapter;
		this.viewNature = viewNature;
		this.virtualModelInstanceNature = virtualModelInstanceNature;
		this.flexoConceptInstanceNature = flexoConceptInstanceNature;
	}

	/**
	 * Return the technology adapter handling specific natures
	 * 
	 * @return
	 */
	public TechnologyAdapter getHandlingTechnologyAdapter() {
		return handlingTechnologyAdapter;
	}

	/**
	 * Return the technology adapter controller handling specific natures
	 * 
	 * @return
	 */
	public TechnologyAdapterController<?> getHandlingTechnologyAdapterController() {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(getHandlingTechnologyAdapter());
	}

	/**
	 * Internally called to make technology browser<br>
	 * Instead of creating a browser for each perspective, we try to share the same instance
	 * 
	 * @return
	 */
	@Override
	protected final FIBTechnologyBrowser<FMLRTTechnologyAdapter> makeTechnologyBrowser() {
		FIBTechnologyBrowser<FMLRTTechnologyAdapter> returned = getController().getSharedFMLRTBrowser();
		return returned;
	}

	public ViewNature getViewNature() {
		return viewNature;
	}

	public VirtualModelInstanceNature getVirtualModelInstanceNature() {
		return virtualModelInstanceNature;
	}

	public FlexoConceptInstanceNature getFlexoConceptInstanceNature() {
		return flexoConceptInstanceNature;
	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return getController().iconForObject(getHandlingTechnologyAdapter());
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof View && viewNature != null && viewNature.hasNature((View) object)) {
			return true;
		}
		if (object instanceof VirtualModelInstance && virtualModelInstanceNature != null
				&& virtualModelInstanceNature.hasNature((VirtualModelInstance) object)) {
			return true;
		}
		if (object instanceof FlexoConceptInstance && flexoConceptInstanceNature != null
				&& flexoConceptInstanceNature.hasNature((FlexoConceptInstance) object)) {
			return true;
		}
		return false;
	}

	@Override
	public final ModuleView<?> createModuleViewForObject(FlexoObject object) {

		if (object instanceof View) {
			return createModuleViewForView((View) object);

			// return getHandlingTechnologyAdapterController().createViewModuleViewForSpecificNature(view, getViewNature(), getController(),
			// this);
		}
		if (object instanceof VirtualModelInstance) {
			return createModuleViewForVirtualModelInstance((VirtualModelInstance) object);

			/*VirtualModelInstance vmInstance = (VirtualModelInstance) object;
			return getHandlingTechnologyAdapterController().createVirtualModelInstanceModuleViewForSpecificNature(vmInstance,
					getVirtualModelInstanceNature(), getController(), this);*/
		}
		if (object instanceof FlexoConceptInstance) {
			return createModuleViewForFlexoConceptInstance((FlexoConceptInstance) object);

			/*FlexoConceptInstance fci = (FlexoConceptInstance) object;
			return getHandlingTechnologyAdapterController().createFlexoConceptInstanceModuleViewForSpecificNature(fci,
					getFlexoConceptInstanceNature(), getController(), this);*/
		}
		return super.createModuleViewForObject(object);
	}

	protected abstract ModuleView<View> createModuleViewForView(View view);

	protected abstract ModuleView<VirtualModelInstance> createModuleViewForVirtualModelInstance(VirtualModelInstance vmInstance);

	protected abstract ModuleView<FlexoConceptInstance> createModuleViewForFlexoConceptInstance(FlexoConceptInstance flexoConceptInstance);

}
