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
package org.openflexo.components.wizard;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FlexoFrame;

/**
 * Component displaying a Wizard<br>
 * Use GINA technology (component is declared in a FIB)
 * 
 * @author sylvain
 */
public class WizardDialog extends FIBDialog<Wizard> {

	private static final Logger logger = FlexoLogger.getLogger(WizardDialog.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/WizardPanel.fib");

	public WizardDialog(Wizard wizard) {
		super(FIBLibrary.instance().retrieveFIBComponent(FIB_FILE), wizard, FlexoFrame.getActiveFrame(), true, FlexoLocalization
				.getMainLocalizer());
		if (wizard instanceof FlexoWizard) {
			getController().setFlexoController(((FlexoWizard) wizard).getController());
		}
		Dimension preferredSize = wizard.getPreferredSize();
		if (preferredSize != null) {
			setPreferredSize(preferredSize);
		}
	}

	@Override
	public WizardPanelController getController() {
		return (WizardPanelController) super.getController();
	}

	@Override
	protected WizardPanelController makeFIBController(FIBComponent fibComponent, LocalizedDelegate parentLocalizer) {
		return new WizardPanelController(fibComponent);
	}
}
