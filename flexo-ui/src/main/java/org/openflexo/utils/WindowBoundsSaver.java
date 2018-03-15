/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.openflexo.view.controller.FlexoController;

public class WindowBoundsSaver implements ComponentListener {

	private final Window window;
	private final String id;
	private Thread boundsSaver;
	private final FlexoController controller;

	public WindowBoundsSaver(Window window, String id, Rectangle defaultBounds, FlexoController controller) {
		super();
		this.window = window;
		this.id = id;
		this.controller = controller;
		Rectangle bounds = null;
		if (controller.getApplicationContext().getGeneralPreferences() != null) {
			bounds = controller.getApplicationContext().getPresentationPreferences().getBoundForFrameWithID(id);
		}
		if (bounds == null) {
			bounds = defaultBounds;
		}
		else {
			boolean ok = false;
			for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				if (device.getDefaultConfiguration().getBounds().contains(bounds.getLocation())) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				bounds.setLocation(defaultBounds.getLocation());
			}
		}
		window.setBounds(bounds);
		window.addComponentListener(this);
	}

	protected synchronized void saveBoundsInPreferenceWhenPossible() {
		if (!window.isVisible()) {
			return;
		}
		if (boundsSaver != null) {
			boundsSaver.interrupt();// Resets thread sleep
			return;
		}

		boundsSaver = new Thread(() -> {
			boolean go = true;
			while (go) {
				try {
					go = false;
					Thread.sleep(800);
				} catch (InterruptedException e) {
					go = true;// interruption is used to reset sleep.
				}
			}
			saveBoundsInPreference();
		});
		boundsSaver.start();
	}

	protected void saveBoundsInPreference() {
		try {
			if (window.getBounds().x + window.getBounds().width < 0) {
				return;
			}
			if (window.getBounds().y + window.getHeight() < 0) {
				return;
			}
			controller.getApplicationContext().getPresentationPreferences().setBoundForFrameWithID(id, window.getBounds());
			controller.getApplicationContext().getPreferencesService().savePreferences();
		} finally {
			boundsSaver = null;
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		saveBoundsInPreferenceWhenPossible();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		saveBoundsInPreferenceWhenPossible();
	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}
}
