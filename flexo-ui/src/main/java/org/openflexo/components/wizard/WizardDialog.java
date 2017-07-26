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

package org.openflexo.components.wizard;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Component displaying a Wizard<br>
 * Use GINA technology (component is declared in a FIB)
 * 
 * @author sylvain
 */
public class WizardDialog extends JFIBDialog<Wizard> {

	private static final Logger logger = FlexoLogger.getLogger(WizardDialog.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/WizardPanel.fib");

	public WizardDialog(Wizard wizard, FlexoController controller) {
		// We first initialize the FIBComponent in order to have the FlexoController well initialized in the WizardPanelController
		// Otherwise, first step of wizard will have a controller with a null FlexoController
		super(getFIBComponent(controller != null ? controller.getApplicationContext() : null), wizard, FlexoFrame.getActiveFrame(), true,
				makeFIBController(getFIBComponent(controller != null ? controller.getApplicationContext() : null),
						SwingViewFactory.INSTANCE, FlexoLocalization.getMainLocalizer(), wizard));
		if (wizard instanceof FlexoWizard) {
			getController().setFlexoController(((FlexoWizard) wizard).getController());
		}
		Dimension preferredSize = wizard.getPreferredSize();
		if (preferredSize != null) {
			setPreferredSize(preferredSize);
		}
		getController().setFlexoController(controller);

		// Attempt to manage focus on buttons
		/*JFIBButtonWidget buttonWidget = (JFIBButtonWidget) getController().viewForComponent("CancelButton");
		System.out.println("et hop, buttonWidget=" + buttonWidget);
		System.out.println("button=" + buttonWidget.getJComponent());
		((JButton) buttonWidget.getJComponent()).setSelected(true);
		buttonWidget.getJComponent().requestFocus();*/

		getController().updateFocusPolicy();
	}

	@Override
	public WizardPanelController getController() {
		return (WizardPanelController) super.getController();
	}

	@Override
	protected WizardPanelController makeFIBController(FIBComponent fibComponent, GinaViewFactory<?> viewFactory,
			LocalizedDelegate parentLocalizer) {
		WizardPanelController returned = new WizardPanelController(fibComponent, viewFactory);
		returned.setParentLocalizer(parentLocalizer);
		return returned;
	}

	private static FIBComponent getFIBComponent(ApplicationContext applicationContext) {
		if (applicationContext != null) {
			return applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(FIB_FILE);
		}
		else {
			return ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(FIB_FILE);
		}
	}

	protected static WizardPanelController makeFIBController(FIBComponent fibComponent, GinaViewFactory<?> viewFactory,
			LocalizedDelegate parentLocalizer, Wizard data) {
		WizardPanelController returned = new WizardPanelController(fibComponent, viewFactory);
		if (data instanceof FlexoWizard) {
			returned.setFlexoController(((FlexoWizard) data).getController());
		}
		returned.setParentLocalizer(parentLocalizer);
		return returned;
	}
}
