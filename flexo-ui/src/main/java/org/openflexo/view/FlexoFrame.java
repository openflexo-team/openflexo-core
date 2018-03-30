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

package org.openflexo.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.icon.IconLibrary;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.task.TaskManagerPanel;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * Abstract main frame used in the context of an application module
 * 
 * @author sguerin
 */

public final class FlexoFrame extends JFrame implements FlexoActionSource<FlexoObject, FlexoObject>, PropertyChangeListener {

	// private FlexoModule<?>

	private final class FlexoModuleWindowListener extends WindowAdapter {

		@Override
		public void windowActivated(WindowEvent e) {
			if (!(e.getOppositeWindow() instanceof TaskManagerPanel) && getModuleLoader().isLoaded(getModule().getModule())) {
				// System.out.println("windowActivated for " + getModule());
				// System.out.println("Opposite: " + e.getOppositeWindow());
				// System.out.println("active module: " + getModuleLoader().getActiveModule());
				// System.out.println("WindowEvent source: " + e.getSource());
				switchToModule();
			}
		}

		@Override
		public void windowClosing(WindowEvent event) {
			close();
		}
	}

	static final Logger logger = Logger.getLogger(FlexoFrame.class.getPackage().getName());

	private FlexoController _controller;

	private List<FlexoRelativeWindow> _relativeWindows;

	private List<FlexoRelativeWindow> _displayedRelativeWindows;

	// Unused private ComponentListener windowResizeListener;

	private MouseListener mouseListener;

	private WindowListener windowListener;

	public static FlexoFrame getActiveFrame() {
		return getActiveFrame(true);
	}

	public static FlexoFrame getActiveFrame(boolean createDefaultIfNull) {
		for (Frame frame : getFrames()) {
			if (frame.isActive()) {
				if (frame instanceof FlexoFrame) {
					return (FlexoFrame) frame;
				}
				else if (frame instanceof FlexoRelativeWindow) {
					((FlexoRelativeWindow) frame).getParentFrame();
				}
				else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Found active frame " + frame.getTitle() + " which is not a FlexoFrame nor a Relative Window.");
					}
				}
				// We break since there won't be any other active frame.
				break;
			}
			else if (frame instanceof FlexoFrame) {
				if (hasActiveOwnedWindows(frame)) {
					return (FlexoFrame) frame;
				}
			}
		}
		for (Frame frame : getFrames()) {
			if (frame instanceof FlexoFrame) {
				return (FlexoFrame) frame;
			}
		}
		return createDefaultIfNull ? getDefaultFrame() : null;
	}

	protected static boolean hasActiveOwnedWindows(Window window) {
		for (Window w : window.getOwnedWindows()) {
			if (w.isActive()) {
				return true;
			}
			else if (hasActiveOwnedWindows(w)) {
				return true;
			}
		}
		return false;
	}

	public static Frame getOwner(Frame owner) {
		return owner != null ? owner : getActiveFrame();
	}

	private static FlexoFrame defaultFrame;

	private static FlexoFrame getDefaultFrame() {
		if (defaultFrame == null) {
			defaultFrame = new FlexoFrame();
			defaultFrame.setUndecorated(true);
			defaultFrame.setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2, 0, 0);
			defaultFrame.setResizable(false);
			defaultFrame.setVisible(true);
		}
		return defaultFrame;
	}

	private static void disposeDefaultFrameWhenPossible() {
		SwingUtilities.invokeLater(() -> disposeDefaultFrame());
	}

	private static void disposeDefaultFrame() {
		Frame f = defaultFrame;
		if (f != null) {
			boolean isDisposable = true;
			for (Window w : f.getOwnedWindows()) {
				if (w.isVisible()) {
					// a bit clumsy if two dialogs are visible but eventually
					// the frame should get disposed.
					w.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							disposeDefaultFrameWhenPossible();
						}
					});
					isDisposable = false;
				}
			}
			if (isDisposable) {
				f.setVisible(false);
				f.dispose();
				defaultFrame = null;
			}
		}
	}

	private FlexoFrame() {
		super(FlexoCst.BUSINESS_APPLICATION_VERSION_NAME);
		setIconImage(IconLibrary.OPENFLEXO_NOTEXT_128.getImage());
	}

	public FlexoFrame(FlexoController controller) {
		super();
		_controller = controller;
		_relativeWindows = new Vector<>();
		_displayedRelativeWindows = new Vector<>();
		Rectangle bounds = null;
		if (getController().getApplicationContext().getGeneralPreferences() != null) {
			bounds = getController().getApplicationContext().getPresentationPreferences()
					.getBoundForFrameWithID(getController().getModule().getShortName() + "Frame");
		}
		if (bounds != null) {
			// In case we remove a screen (if you go from 3 to 2 screen, go to
			// hell, that's all you deserve ;-))
			if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length == 1) {
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				if (screen.width <= bounds.x) {
					bounds.x = 0;
				}
				else if (screen.height <= bounds.y) {
					bounds.y = 0;
				}
			}
			setBounds(bounds);
		}
		else {
			setSize(3 * Toolkit.getDefaultToolkit().getScreenSize().width / 4, 3 * Toolkit.getDefaultToolkit().getScreenSize().height / 4);
			setLocationByPlatform(true);
		}
		Integer state = null;
		if (getController().getApplicationContext().getGeneralPreferences() != null) {
			state = getController().getApplicationContext().getPresentationPreferences()
					.getFrameStateForFrameWithID(getController().getModule().getShortName() + "Frame");
		}
		if (state != null && ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
				|| (state & Frame.MAXIMIZED_HORIZ) == Frame.MAXIMIZED_HORIZ || (state & Frame.MAXIMIZED_VERT) == Frame.MAXIMIZED_VERT)) {
			setExtendedState(getController().getApplicationContext().getPresentationPreferences()
					.getFrameStateForFrameWithID(getController().getModule().getShortName() + "Frame"));
		}
		_controller.getControllerModel().getPropertyChangeSupport().addPropertyChangeListener(ControllerModel.CURRENT_EDITOR, this);
		if (defaultFrame != null) {
			disposeDefaultFrameWhenPossible();
		}
		if (!ToolBox.isWindows()) {
			setIconImage(controller.getModule().getModule().getBigIcon().getImage());
		}
		else {
			setIconImage(IconLibrary.OPENFLEXO_NOTEXT_128.getImage());
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(true);
		setFocusable(true);

		/**
		 * Listeners
		 */
		addWindowListener(windowListener = new FlexoModuleWindowListener());
		addComponentListener(new ComponentAdapter() { // Unused windowResizeListener =
			@Override
			public void componentMoved(ComponentEvent e) {
				saveBoundsInPreferenceWhenPossible();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				saveBoundsInPreferenceWhenPossible();
			}

		});
	}

	public FlexoModule<?> getModule() {
		if (getController() != null) {
			return getController().getModule();
		}
		else {
			return null;
		}
	}

	protected void switchToModule() {
		if (getModule() != null) {
			try {
				getModuleLoader().switchToModule(getModule().getModule());
			} catch (ModuleLoadingException e1) {
				e1.printStackTrace();
			}
		}
	}

	private ModuleLoader getModuleLoader() {
		return getController().getModuleLoader();
	}

	/**
	 * @return Returns the controller.
	 */
	public FlexoController getController() {
		return _controller;
	}

	public String getLocalizedName() {
		return getModule().getName();
	}

	public void addToRelativeWindows(FlexoRelativeWindow aRelativeWindow) {
		if (!_relativeWindows.contains(aRelativeWindow)) {
			_relativeWindows.add(aRelativeWindow);
		}
	}

	public void removeFromRelativeWindows(FlexoRelativeWindow aRelativeWindow) {
		if (_relativeWindows.contains(aRelativeWindow)) {
			_relativeWindows.remove(aRelativeWindow);
		}
		removeFromDisplayedRelativeWindows(aRelativeWindow);
	}

	public void disposeAll() {
		for (FlexoRelativeWindow next : new ArrayList<>(_relativeWindows)) {
			next.dispose();
		}
		_relativeWindows.clear();
		if (_controller != null) {
			_controller.getControllerModel().getPropertyChangeSupport().removePropertyChangeListener(ControllerModel.CURRENT_LOCATION,
					this);
			if (_controller.getProject() != null) {
				_controller.getProject().getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			_controller = null;
		}
		if (windowListener != null) {
			removeWindowListener(windowListener);
			windowListener = null;
		}
		if (mouseListener != null) {
			removeMouseListener(mouseListener);
		}
		setJMenuBar(null);
		if (getContentPane() != null) {
			getContentPane().removeAll();
		}
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Disposing " + this);
		}
		dispose();
	}

	public void addToDisplayedRelativeWindows(FlexoRelativeWindow aRelativeWindow) {
		if (!_displayedRelativeWindows.contains(aRelativeWindow)) {
			_displayedRelativeWindows.add(aRelativeWindow);
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.finer("addToRelativeWindows with " + aRelativeWindow);
		}
	}

	public void removeFromDisplayedRelativeWindows(FlexoRelativeWindow aRelativeWindow) {
		if (_displayedRelativeWindows.contains(aRelativeWindow)) {
			_displayedRelativeWindows.remove(aRelativeWindow);
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.finer("removeFromRelativeWindows with " + aRelativeWindow);
		}
	}

	private static final String WINDOW_MODIFIED = "windowModified";

	public void updateWindowModified() {
		if (ToolBox.isMacOS()) {
			getRootPane().putClientProperty(WINDOW_MODIFIED,
					getController().getApplicationContext().getResourceManager().getUnsavedResources().size() > 0);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ControllerModel.CURRENT_EDITOR)) {
			FlexoEditor oldEditor = (FlexoEditor) evt.getOldValue();
			FlexoEditor newEditor = (FlexoEditor) evt.getNewValue();
			if (oldEditor != newEditor) {
				if (oldEditor != null && oldEditor.getProject() != null) {
					oldEditor.getProject().getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (newEditor != null && newEditor.getProject() != null) {
					newEditor.getProject().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				updateTitle();
			}
		}
		if (evt.getPropertyName().equals(FlexoProject.PROJECT_NAME_KEY)) {
			updateTitle();
		}
		updateWindowModified();

	}

	@Override
	public void setVisible(boolean mainFrameIsVisible) {
		if (getController() != null) {
			if (mainFrameIsVisible && getModule() != null && getModule().isActive() || !mainFrameIsVisible) {
				setRelativeVisible(mainFrameIsVisible);
			}
		}
		super.setVisible(mainFrameIsVisible);
	}

	public void setRelativeVisible(boolean relativeWindowsAreVisible) {
		if (relativeWindowsAreVisible) {
			showRelativeWindows();
		}
		else {
			hideRelativeWindows();
		}
	}

	private int showRelativeWindows() {
		int returned = 0;
		if (_displayedRelativeWindows != null) {
			for (FlexoRelativeWindow next : _displayedRelativeWindows) {
				if (!next.isShowing()) {
					next.setVisibleNoParentFrameNotification(true);
					returned++;
				}
			}
		}
		return returned;
	}

	private void hideRelativeWindows() {
		if (_displayedRelativeWindows != null) {
			for (FlexoRelativeWindow next : _displayedRelativeWindows) {
				if (next.isShowing()) {
					next.setVisibleNoParentFrameNotification(false);
				}
			}
		}
	}

	public void updateTitle() {
		setTitle(getController().getWindowTitle());
	}

	public List<FlexoRelativeWindow> getRelativeWindows() {
		return _relativeWindows;
	}

	@Override
	public void validate() {
		super.validate();
		// TODO: is this hack still necessary ?
		// if (getController() != null && getController().getApplicationContext() != null
		// && getController().getApplicationContext().getDocResourceManager() != null) {
		// getController().getApplicationContext().getDocResourceManager().validateWindow(this);
		// }
	}

	private Thread boundsSaver;

	protected synchronized void saveBoundsInPreferenceWhenPossible() {
		if (!isVisible()) {
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
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					go = true;// interruption is used to reset sleep.
				}
			}
			saveBoundsInPreference();
		});
		boundsSaver.start();
	}

	protected void saveBoundsInPreference() {
		if (getController() == null) {
			return;
		}
		int state = getExtendedState();
		if (state == -1 || (state & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH
				&& (state & Frame.MAXIMIZED_HORIZ) != Frame.MAXIMIZED_HORIZ && (state & Frame.MAXIMIZED_VERT) != Frame.MAXIMIZED_VERT) {
			getController().getApplicationContext().getPresentationPreferences()
					.setBoundForFrameWithID(getController().getModule().getShortName() + "Frame", getBounds());
		}
		getController().getApplicationContext().getPresentationPreferences()
				.setFrameStateForFrameWithID(getController().getModule().getShortName() + "Frame", getExtendedState());
		getController().getApplicationContext().getPreferencesService().savePreferences();
		boundsSaver = null;
	}

	@Override
	public FlexoEditor getEditor() {
		return getController().getEditor();
	}

	@Override
	public FlexoObject getFocusedObject() {
		return getController().getSelectionManager().getFocusedObject();
	}

	@Override
	public Vector<FlexoObject> getGlobalSelection() {
		return getController().getSelectionManager().getSelection();
	}

	public void close() {
		if (getModule().close()) {
			dispose();
		}
	}

}
