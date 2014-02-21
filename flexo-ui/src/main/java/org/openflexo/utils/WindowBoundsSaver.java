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
			bounds = controller.getApplicationContext().getGeneralPreferences().getBoundForFrameWithID(id);
		}
		if (bounds == null) {
			bounds = defaultBounds;
		} else {
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

		boundsSaver = new Thread(new Runnable() {
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
						Thread.sleep(800);
					} catch (InterruptedException e) {
						go = true;// interruption is used to reset sleep.
					}
				}
				saveBoundsInPreference();
			}
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
			controller.getApplicationContext().getGeneralPreferences().setBoundForFrameWithID(id, window.getBounds());
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
