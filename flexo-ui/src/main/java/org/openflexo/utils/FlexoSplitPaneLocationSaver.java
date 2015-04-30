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

package org.openflexo.utils;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSplitPane;

import org.openflexo.view.controller.FlexoController;

public class FlexoSplitPaneLocationSaver implements PropertyChangeListener {

	private final JSplitPane splitPane;
	private final String id;
	private final FlexoController controller;

	public FlexoSplitPaneLocationSaver(JSplitPane pane, String id, FlexoController controller) {
		this(pane, id, null, controller);
	}

	public FlexoSplitPaneLocationSaver(JSplitPane pane, final String id, final Double defaultDividerLocation, FlexoController controller) {
		this.splitPane = pane;
		this.id = id;
		this.controller = controller;
		layoutSplitPaneWhenShowing(id, defaultDividerLocation);
	}

	public void layoutSplitPaneWhenShowing(final String id, final Double defaultDividerLocation) {
		splitPane.addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
					layoutSplitPane(id, defaultDividerLocation);
					splitPane.removeHierarchyListener(this);
				}
			}
		});
	}

	private void layoutSplitPane(String id, Double defaultDividerLocation) {
		if (controller.getApplicationContext().getGeneralPreferences().getDividerLocationForSplitPaneWithID(id) >= 0) {
			splitPane.setDividerLocation(controller.getApplicationContext().getGeneralPreferences()
					.getDividerLocationForSplitPaneWithID(id));
		} else if (defaultDividerLocation != null) {
			splitPane.setDividerLocation(defaultDividerLocation);
		} else {
			splitPane.resetToPreferredSizes();
		}
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		saveLocationInPreferenceWhenPossible();
	}

	private Thread locationSaver;

	protected synchronized void saveLocationInPreferenceWhenPossible() {
		if (!splitPane.isVisible()) {
			return;
		}
		if (locationSaver != null) {
			locationSaver.interrupt();// Resets thread sleep
			return;
		}

		locationSaver = new Thread(new Runnable() {
			/**
			 * Overrides run
			 * 
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				boolean go = true;
				while (go) {
					try {
						go = false;
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						go = true;// interruption is used to reset sleep.
					}
				}
				saveLocationInPreference();
			}
		}, "Splitpane location saver for " + id);
		locationSaver.start();
	}

	protected void saveLocationInPreference() {
		int value = splitPane.getDividerLocation();
		if (value > splitPane.getMaximumDividerLocation()) {
			value = splitPane.getMaximumDividerLocation();
		} else if (value < splitPane.getMinimumDividerLocation()) {
			value = splitPane.getMinimumDividerLocation();
		}
		controller.getApplicationContext().getGeneralPreferences().setDividerLocationForSplitPaneWithID(value, id);
		controller.getApplicationContext().getPreferencesService().savePreferences(); // FlexoPreferences.savePreferences(true);
		locationSaver = null;
	}

}
