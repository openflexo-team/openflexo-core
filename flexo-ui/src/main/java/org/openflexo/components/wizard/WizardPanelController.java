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

package org.openflexo.components.wizard;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.widget.FIBReferencedComponent;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.gina.swing.view.widget.JFIBButtonWidget;
import org.openflexo.gina.swing.view.widget.JFIBReferencedComponentWidget;
import org.openflexo.gina.view.FIBView;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * {@link FIBController} used in Wizard context
 * 
 * We manage here a focus-traversal policy allowing to edit a wizard without using mouse
 * 
 * @author sylvain
 *
 */
public class WizardPanelController extends FlexoFIBController {

	public WizardPanelController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
	}

	@Override
	public Wizard getDataObject() {
		return (Wizard) super.getDataObject();
	}

	@Override
	public void cancelAndDispose() {
		getDataObject().cancel();
		super.cancelAndDispose();
	}

	public void finish() {
		getDataObject().finish();
		super.validateAndDispose();
	}

	public void performNext() {
		getDataObject().performNext();
		updateFocusPolicy();
	}

	public void performPrevious() {
		getDataObject().performPrevious();
		updateFocusPolicy();
	}

	public void updateFocusPolicy() {

		List<JComponent> focusPolicyList = new ArrayList<>();

		if (getRootComponent() instanceof FIBContainer) {
			for (FIBComponent c : ((FIBContainer) getRootComponent()).getAllSubComponents()) {
				if (!c.getName().equals("PreviousButton") && !c.getName().equals("NextButton") && !c.getName().equals("CancelButton")
						&& !c.getName().equals("FinishButton")) {
					setFocusTraversal(c, this, focusPolicyList);
					if (c instanceof FIBReferencedComponent) {
						JFIBReferencedComponentWidget refWidget = (JFIBReferencedComponentWidget) (FIBView) viewForComponent(c);
						FIBController embeddedController = refWidget.getEmbeddedFIBController();
						for (FIBComponent c2 : ((FIBContainer) embeddedController.getRootComponent()).getAllSubComponents()) {
							setFocusTraversal(c2, embeddedController, focusPolicyList);
						}
					}
				}
			}
		}

		previousButtonWidget = (JFIBButtonWidget) viewForComponent("PreviousButton");
		nextButtonWidget = (JFIBButtonWidget) viewForComponent("NextButton");
		cancelButtonWidget = (JFIBButtonWidget) viewForComponent("CancelButton");
		finishButtonWidget = (JFIBButtonWidget) viewForComponent("FinishButton");

		focusPolicyList.add(previousButtonWidget.getJComponent());
		focusPolicyList.add(nextButtonWidget.getJComponent());
		focusPolicyList.add(cancelButtonWidget.getJComponent());
		focusPolicyList.add(finishButtonWidget.getJComponent());

		Set<AWTKeyStroke> forwardKeys = ((JFIBView<?, ?>) getRootView()).getResultingJComponent()
				.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
		Set<AWTKeyStroke> newForwardKeys = new HashSet<>(forwardKeys);
		newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
		((JFIBView<?, ?>) getRootView()).getResultingJComponent().setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				newForwardKeys);

		Set<AWTKeyStroke> downKeys = ((JFIBView<?, ?>) getRootView()).getResultingJComponent()
				.getFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS);
		Set<AWTKeyStroke> newDownKeys = new HashSet<>(downKeys);
		newDownKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		((JFIBView<?, ?>) getRootView()).getResultingJComponent().setFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS,
				newDownKeys);

		((JFIBView<?, ?>) getRootView()).getResultingJComponent().setFocusTraversalPolicyProvider(true);
		((JFIBView<?, ?>) getRootView()).getResultingJComponent().setFocusTraversalPolicy(new WizardFocusTraversalPolicy(focusPolicyList));

		knownNextEnabled = getDataObject().isNextEnabled();
		knownCanFinish = getDataObject().canFinish();

		trackNextAndFinish();

		finishButtonWidget.getJComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (getDataObject().canFinish()) {
						finish();
					}
				}
			}
		});

		nextButtonWidget.getJComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (getDataObject().isNextEnabled()) {
						performNext();
					}
				}
			}
		});

		previousButtonWidget.getJComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (getDataObject().isPreviousEnabled()) {
						performPrevious();
					}
				}
			}
		});

		cancelButtonWidget.getJComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					cancelAndDispose();
				}
			}
		});

	}

	private JFIBButtonWidget finishButtonWidget;
	private JFIBButtonWidget nextButtonWidget;
	private JFIBButtonWidget previousButtonWidget;
	private JFIBButtonWidget cancelButtonWidget;

	private void setFocusTraversal(FIBComponent component, FIBController controller, List<JComponent> focusPolicyList) {
		if (component instanceof FIBWidget && ((FIBWidget) component).isFocusable()) {
			setKeyAdapter((FIBWidget) component, controller);
			focusPolicyList.add(((JFIBView<?, ?>) controller.viewForComponent(component)).getJComponent());
		}
	}

	private void setKeyAdapter(FIBWidget tf, FIBController controller) {
		JFIBView<?, ?> widget = (JFIBView<?, ?>) controller.viewForComponent(tf);
		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				trackNextAndFinish();
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (getDataObject().canFinish()) {
						finish();
					}
					else if (getDataObject().isNextEnabled()) {
						performNext();
					}
				}
			}
		};

		recursivelyAddKeyAdapter(widget.getJComponent(), ka);
	}

	private void recursivelyAddKeyAdapter(JComponent component, KeyAdapter ka) {
		component.addKeyListener(ka);
		if (component instanceof Container) {
			for (Component subComponent : ((Container) component).getComponents()) {
				if (subComponent instanceof JComponent) {
					recursivelyAddKeyAdapter((JComponent) subComponent, ka);
				}
			}
		}
	}

	private boolean knownNextEnabled = false;
	private boolean knownCanFinish = false;

	private void trackNextAndFinish() {

		if (!knownCanFinish && getDataObject().canFinish()) {
			((JButton) finishButtonWidget.getJComponent()).setSelected(true);
		}
		else if (knownCanFinish && !getDataObject().canFinish()) {
			((JButton) finishButtonWidget.getJComponent()).setSelected(false);
		}
		if (!knownNextEnabled && getDataObject().isNextEnabled() && !getDataObject().canFinish()) {
			((JButton) nextButtonWidget.getJComponent()).setSelected(true);
		}
		else if (knownNextEnabled && (getDataObject().canFinish() || !getDataObject().isNextEnabled())) {
			((JButton) nextButtonWidget.getJComponent()).setSelected(false);
		}

		knownNextEnabled = getDataObject().isNextEnabled();
		knownCanFinish = getDataObject().canFinish();

	}

	class WizardFocusTraversalPolicy extends FocusTraversalPolicy {

		private List<JComponent> focusPolicyList;

		public WizardFocusTraversalPolicy(List<JComponent> focusPolicyList) {
			this.focusPolicyList = focusPolicyList;
		}

		@Override
		public Component getComponentAfter(Container aContainer, Component aComponent) {

			if (allComponentsAreDisabled()) {
				return null;
			}

			int index = focusPolicyList.indexOf(aComponent);
			if (index > -1) {
				JComponent next = null;
				if (index < focusPolicyList.size() - 1) {
					next = focusPolicyList.get(index + 1);
				}
				else {
					next = focusPolicyList.get(0);
				}
				if (next.isEnabled()) {
					return next;
				}
				else {
					return getComponentAfter(aContainer, next);
				}
			}
			return null;
		}

		@Override
		public Component getComponentBefore(Container aContainer, Component aComponent) {
			if (allComponentsAreDisabled()) {
				return null;
			}
			int index = focusPolicyList.indexOf(aComponent);
			if (index > -1) {
				JComponent previous = null;
				if (index > 0) {
					previous = focusPolicyList.get(index - 1);
				}
				else {
					previous = focusPolicyList.get(focusPolicyList.size() - 1);
				}
				if (previous.isEnabled()) {
					return previous;
				}
				else {
					return getComponentBefore(aContainer, previous);
				}
			}
			return null;
		}

		@Override
		public Component getDefaultComponent(Container aContainer) {
			Component returned = getFirstComponent(aContainer);
			return returned;
		}

		@Override
		public Component getFirstComponent(Container aContainer) {
			if (allComponentsAreDisabled()) {
				return null;
			}
			JComponent returned = null;
			if (focusPolicyList.size() > 0) {
				returned = focusPolicyList.get(0);
			}
			if (returned == null) {
				return null;
			}
			if (returned.isEnabled()) {
				return returned;
			}
			else {
				return getComponentAfter(aContainer, returned);
			}
		}

		@Override
		public Component getLastComponent(Container aContainer) {
			if (allComponentsAreDisabled()) {
				return null;
			}
			JComponent returned = null;
			if (focusPolicyList.size() > 0) {
				returned = focusPolicyList.get(focusPolicyList.size() - 1);
			}
			if (returned == null) {
				return null;
			}
			if (returned.isEnabled()) {
				return returned;
			}
			else {
				return getComponentBefore(aContainer, returned);
			}
		}

		private boolean allComponentsAreDisabled() {
			if (focusPolicyList.size() == 0) {
				return true;
			}
			for (JComponent c : focusPolicyList) {
				if (c.isEnabled()) {
					return false;
				}
			}
			return true;
		}

	}

}
