/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller.action;

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.action.AbstractCreateVirtualModelInstanceWizard;

public class CreateBasicVirtualModelInstanceWizard extends AbstractCreateVirtualModelInstanceWizard<CreateBasicVirtualModelInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateBasicVirtualModelInstanceWizard.class.getPackage().getName());

	public CreateBasicVirtualModelInstanceWizard(CreateBasicVirtualModelInstance action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_virtual_model_instance");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	@Override
	protected ChooseVirtualModel makeChooseVirtualModel() {
		return new ChooseVirtualModel();
	}

	@Override
	protected ChooseAndConfigureCreationSchemeForVirtualModel makeChooseAndConfigureCreationScheme() {
		return new ChooseAndConfigureCreationSchemeForVirtualModel();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ChooseVirtualModel.fib")
	public class ChooseVirtualModel extends AbstractChooseVirtualModel {

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_virtual_model");
		}
	}

	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ChooseAndConfigureCreationSchemeForVirtualModel.fib")
	public class ChooseAndConfigureCreationSchemeForVirtualModel extends AbstractChooseAndConfigureCreationScheme {

		public ChooseAndConfigureCreationSchemeForVirtualModel() {
			super(CreateBasicVirtualModelInstanceWizard.this.getAction().getCreationSchemeAction());
		}

	}
}
