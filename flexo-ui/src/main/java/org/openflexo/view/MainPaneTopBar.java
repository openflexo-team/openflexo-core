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

package org.openflexo.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openflexo.components.widget.FIBProjectSelector;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.icon.IconLibrary;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.project.InteractiveProjectLoader;
import org.openflexo.swing.BarButton;
import org.openflexo.swing.CustomPopup;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;
import org.openflexo.view.controller.model.FlexoPerspective;

public class MainPaneTopBar extends JMenuBar {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(MainPaneTopBar.class.getPackage().getName());

	private final PropertyChangeListenerRegistrationManager registrationManager;

	private final ControllerModel model;

	private JPanel left;
	private JPanel center;
	private JPanel right;

	private JComponent header;

	private JButton leftViewToggle;

	private JButton rightViewToggle;

	private JPanel perspectives;

	private final boolean forcePreferredSize;

	private final FlexoController controller;

	private FIBProjectSelector projectSelector;

	public MainPaneTopBar(FlexoController controller) {
		this.controller = controller;
		this.model = controller.getControllerModel();
		registrationManager = new PropertyChangeListenerRegistrationManager();
		setLayout(new BorderLayout());
		this.forcePreferredSize = true/*ToolBox.getPLATFORM() != ToolBox.MACOS*/;
		add(left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)), BorderLayout.WEST);
		add(center = new JPanel(new BorderLayout()));
		add(right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)), BorderLayout.EAST);
		left.setOpaque(false);
		center.setOpaque(false);
		right.setOpaque(false);
		initLeftRightViewVisibilityControls();
		initModules();
		// if (controller.getModule().getModule().requireProject()) {
		initProjectSelector();
		// }
		initNavigationControls();
		initPerspectives();
	}

	public void delete() {
		registrationManager.delete();
		if (projectSelector != null) {
			projectSelector.delete();
		}
	}

	private void initModules() {
		for (final Module<?> module : model.getModuleLoader().getKnownModules()) {
			final JButton button = new BarButton(module.getMediumIcon());
			button.setToolTipText(module.getName());
			button.setEnabled(true);
			button.setFocusable(false);
			if (forcePreferredSize && button.getIcon() != null) {
				button.setPreferredSize(new Dimension(button.getIcon().getIconWidth() + 4, button.getIcon().getIconHeight() + 4));
			}
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// We activate the module LATER
					// to let the window manager to handle all events BEFORE to perform the switch
					SwingUtilities.invokeLater(() -> {
						try {
							model.getModuleLoader().switchToModule(module);
						} catch (ModuleLoadingException e1) {
							e1.printStackTrace();
							FlexoController.notify(e1.getLocalizedMessage());
						}
					});
				}
			});
			PropertyChangeListener listener = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					button.setIcon(model.getModuleLoader().isLoaded(module) ? module.getMediumIconWithHover() : module.getMediumIcon());
					// button.setSelected(model.getModuleLoader().isActive(module));
					if (module.equals(controller.getModule().getModule())) {
						button.setSelected(true);
					}
					else {
						button.setSelected(false);
					}
				}
			};
			registrationManager.new PropertyChangeListenerRegistration(ModuleLoader.ACTIVE_MODULE, listener, model.getModuleLoader());
			registrationManager.new PropertyChangeListenerRegistration(ModuleLoader.MODULE_ACTIVATED, listener, model.getModuleLoader());
			registrationManager.new PropertyChangeListenerRegistration(ModuleLoader.MODULE_LOADED, listener, model.getModuleLoader());
			registrationManager.new PropertyChangeListenerRegistration(ModuleLoader.MODULE_UNLOADED, listener, model.getModuleLoader());
			left.add(button);
		}
	}

	private void initNavigationControls() {
		final JButton backwardButton = new BarButton(IconLibrary.NAVIGATION_BACKWARD_ICON);
		if (forcePreferredSize) {
			backwardButton.setPreferredSize(new Dimension(24, 24));
		}
		backwardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.historyBack();
			}
		});
		final JButton forwardButton = new BarButton(IconLibrary.NAVIGATION_FORWARD_ICON);
		if (forcePreferredSize) {
			forwardButton.setPreferredSize(new Dimension(24, 24));
		}
		forwardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.historyForward();
			}
		});
		final JButton upButton = new BarButton(IconLibrary.NAVIGATION_UP_ICON);
		if (forcePreferredSize) {
			upButton.setPreferredSize(new Dimension(24, 24));
		}
		upButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.goUp();
			}
		});
		PropertyChangeListener listener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateNavigationControlState(backwardButton, forwardButton, upButton);
			}

		};
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.CURRENT_LOCATION, listener, model);
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.CURRENT_EDITOR, listener, model);
		left.add(backwardButton);
		left.add(upButton);
		left.add(forwardButton);
		updateNavigationControlState(backwardButton, forwardButton, upButton);
	}

	private void initProjectSelector() {
		projectSelector = new FIBProjectSelector(null) {
			@Override
			public boolean evaluateSelectableCondition(FlexoProject project) {
				return model.isSelectableProject(project);
			}
		};
		projectSelector.addApplyCancelListener(new CustomPopup.ApplyCancelListener() {
			@Override
			public void fireApplyPerformed() {
				if (projectSelector.getEditedObject() != null) {
					model.setCurrentProject(projectSelector.getEditedObject());
					controller.selectAndFocusObject(controller.getDefaultObjectToSelect(projectSelector.getEditedObject()));
				}
				else {
					projectSelector.setEditedObject(model.getCurrentProject());
				}
			}

			@Override
			public void fireCancelPerformed() {

			}

		});
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.CURRENT_EDITOR, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				projectSelector.setEditedObject(model.getCurrentProject());
			}
		}, model);
		projectSelector.setShowReset(false);
		projectSelector.setFlexoController(controller);
		if (model.getProjectLoader() instanceof InteractiveProjectLoader) {
			projectSelector.setProjectLoader((InteractiveProjectLoader) model.getProjectLoader());
		}
		left.add(projectSelector);
	}

	protected void updateNavigationControlState(final JButton backwardButton, final JButton forwardButton, final JButton upButton) {
		backwardButton.setEnabled(model.canGoBack());
		forwardButton.setEnabled(model.canGoForward());
		upButton.setEnabled(model.canGoUp());
	}

	private void initPerspectives() {
		right.add(perspectives = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)), 0);
		perspectives.setOpaque(false);
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.PERSPECTIVES, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() != null) {
					insertPerspective((FlexoPerspective) evt.getNewValue());
				}
				else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Perspective removal not supported by top bar.");
					}
				}
			}
		}, model);
		for (final FlexoPerspective p : model.getPerspectives()) {
			insertPerspective(p);
		}
	}

	private void insertPerspective(final FlexoPerspective p) {
		final JButton button = new BarButton(p.getActiveIcon());
		button.setToolTipText(controller.getModuleLocales().localizedTooltipForKey(p.getName(), button));
		if (forcePreferredSize) {
			int size = Math.max(button.getIcon().getIconWidth() + 8, button.getIcon().getIconHeight() + 4);
			button.setPreferredSize(new Dimension(size, size));
		}
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.setCurrentPerspective(p);
			}
		});
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.CURRENT_LOCATION, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateIconForButtonPerspective(button, p);
			}
		}, model);
		updateIconForButtonPerspective(button, p);
		perspectives.add(button);
	}

	protected void updateIconForButtonPerspective(JButton buttonPerspective, FlexoPerspective p) {
		buttonPerspective.setSelected(model.getCurrentPerspective() == p);
	}

	private void initLeftRightViewVisibilityControls() {
		leftViewToggle = getToggleVisibilityButton();
		leftViewToggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.setLeftViewVisible(!model.isLeftViewVisible());
			}
		});
		left.add(leftViewToggle, 0);
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.LEFT_VIEW_VISIBLE, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateLeftViewToggleIcon();
			}
		}, model);
		updateLeftViewToggleIcon();
		rightViewToggle = getToggleVisibilityButton();
		rightViewToggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.setRightViewVisible(!model.isRightViewVisible());
			}
		});
		right.add(rightViewToggle);
		registrationManager.new PropertyChangeListenerRegistration(ControllerModel.RIGHT_VIEW_VISIBLE, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateRightViewToggleIcon();
			}
		}, model);
		updateRightViewToggleIcon();

	}

	protected void updateLeftViewToggleIcon() {
		leftViewToggle.setIcon(model.isLeftViewVisible() ? IconLibrary.TOGGLE_ARROW_BOTTOM_ICON : IconLibrary.TOGGLE_ARROW_TOP_ICON);
		leftViewToggle.setRolloverIcon(
				model.isLeftViewVisible() ? IconLibrary.TOGGLE_ARROW_BOTTOM_SELECTED_ICON : IconLibrary.TOGGLE_ARROW_TOP_SELECTED_ICON);
	}

	protected void updateRightViewToggleIcon() {
		rightViewToggle.setIcon(model.isRightViewVisible() ? IconLibrary.TOGGLE_ARROW_BOTTOM_ICON : IconLibrary.TOGGLE_ARROW_TOP_ICON);
		rightViewToggle.setRolloverIcon(
				model.isRightViewVisible() ? IconLibrary.TOGGLE_ARROW_BOTTOM_SELECTED_ICON : IconLibrary.TOGGLE_ARROW_TOP_SELECTED_ICON);
	}

	private JButton getToggleVisibilityButton() {
		final JButton button = new BarButton(IconLibrary.TOGGLE_ARROW_TOP_ICON);
		button.setRolloverIcon(IconLibrary.TOGGLE_ARROW_TOP_SELECTED_ICON);
		if (forcePreferredSize) {
			button.setPreferredSize(new Dimension(button.getIcon().getIconWidth() + 2, button.getIcon().getIconHeight() + 20));
		}
		return button;
	}

	public void setHeader(JComponent header) {
		if (this.header != header) {
			if (this.header != null) {
				right.remove(this.header);
				right.revalidate();
			}
			this.header = header;
			if (header != null) {
				header.setOpaque(false);
				right.add(header, 0);
				right.revalidate();
			}
		}
	}

	public void setLeftViewToggle(boolean visible) {
		leftViewToggle.setEnabled(visible);
	}

	public void setRightViewToggle(boolean visible) {
		rightViewToggle.setEnabled(visible);
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getDefaults().getColor("ToolBar.floatingForeground"));
	}
}
