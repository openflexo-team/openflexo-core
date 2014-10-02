/*
 * (c) Copyright 2010-2013 AgileBirds
 * (c) Copyright 2014 Openflexo
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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.swing.FIBJPanel;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Encodes a step of a {@link FlexoWizard}.<br>
 * This class is generally enriched with configuration data<br>
 * isValid() method should return true if and only if the configuration data is valid regarding the purpose of the wizard.<br>
 * Assuming validity of this step causes the "next" and/or "finish" button of the wizard to be enabled.
 * 
 * @author guillaume, sylvain
 * 
 */
public abstract class WizardStep {

	private static final Logger logger = FlexoLogger.getLogger(WizardStep.class.getPackage().getName());

	private FlexoWizard wizard;

	/**
	 * The title of this page.
	 * 
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * Return the current wizard that is using this page
	 * 
	 * @param wizard
	 */
	protected FlexoWizard getWizard() {
		return wizard;
	}

	/**
	 * Sets the current wizard that is using this page
	 * 
	 * @param wizard
	 */
	protected void setWizard(FlexoWizard wizard) {
		this.wizard = wizard;
	}

	/**
	 * The next page of this wizard page. Can be null.
	 * 
	 * @return
	 */
	// public WizardStep getNextPage();

	/**
	 * The previous page of this wizard page. Can be null.
	 * 
	 * @return
	 */
	// public WizardStep getPreviousPage();

	public Image getPageImage() {
		return null;
	}

	public abstract boolean isValid();

	public boolean isNextEnabled() {
		return true;
	}

	// public boolean isFinishEnabled();

	public boolean isPreviousEnabled() {
		return true;
	}

	private FIBComponent fibComponent = null;

	public FIBComponent getFIBComponent() {
		if (fibComponent == null) {
			if (getClass().getAnnotation(FIBPanel.class) != null) {
				System.out.println("Found annotation " + getClass().getAnnotation(FIBPanel.class));
				String fibPanelName = getClass().getAnnotation(FIBPanel.class).value();
				Resource fibPanelResource = ResourceLocator.locateResource(fibPanelName);
				System.out.println("fibPanelResource=" + fibPanelResource);
				if (fibPanelResource != null) {
					fibComponent = FIBLibrary.instance().retrieveFIBComponent(fibPanelResource);
					logger.info("Found " + fibComponent);
				}
			}
		}
		return fibComponent;

	}

	private FIBJPanel<?> jComponent;

	public FIBJPanel<?> getJComponent() {
		if (jComponent == null) {
			if (getFIBComponent() != null) {
				jComponent = new FIBJPanel(fibComponent, this, FlexoLocalization.getMainLocalizer()) {

					@Override
					public Class<?> getRepresentedType() {
						return getClass();
					}

					@Override
					public void delete() {
					}

				};
			}
		}
		return jComponent;
	}

	/*public JComponent initUserInterface(JComponent parent);

	public JComponent getUserInterface();*/

}
