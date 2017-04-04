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

package org.openflexo.selection;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.openflexo.FlexoCst;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.ActionGroup;
import org.openflexo.foundation.action.ActionMenu;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.NavigationSchemeActionType;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionType;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.action.EditionAction;

public class ContextualMenuManager {

	protected static final Logger logger = Logger.getLogger(ContextualMenuManager.class.getPackage().getName());

	private static final MenuFilter ALL = new MenuFilter() {

		@Override
		public boolean acceptActionType(FlexoActionType<?, ?, ?> actionType) {
			return true;
		}
	};

	private final SelectionManager _selectionManager;

	private boolean _isPopupMenuDisplayed = false;

	private boolean _isPopupTriggering = false;

	private Component _invoker = null;

	private final FlexoController controller;

	public ContextualMenuManager(SelectionManager selectionManager, FlexoController controller) {
		super();
		_selectionManager = selectionManager;
		this.controller = controller;
	}

	public FlexoEditor getEditor() {
		return controller.getEditor();
	}

	public void processMousePressed(MouseEvent e) {
		resetContextualMenuTriggering();
		_isPopupTriggering = e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3;

		if (e.getSource() instanceof Component) {
			_invoker = (Component) e.getSource();
			if (_isPopupTriggering) {

				// Return now when no selection manager defined
				if (_selectionManager == null) {
					return;
				}

				boolean isCtrlDown = (e.getModifiersEx() & FlexoCst.MULTI_SELECTION_MASK) == FlexoCst.MULTI_SELECTION_MASK;

			}
		}
	}

	public void processMouseReleased(MouseEvent e) {
		_isPopupTriggering = _isPopupTriggering || e.isPopupTrigger();

		if (_isPopupTriggering) {
			if (e.getSource() == _invoker /* && (hasSelection()) */) {
				displayPopupMenu((Component) e.getSource(), e);
				e.consume();
				resetContextualMenuTriggering();
			}
		}
	}

	public void processMouseMoved(MouseEvent e) {
		if (_isPopupMenuDisplayed && controller.getApplicationContext().getPresentationPreferences().getCloseOnMouseOut()) {
			Rectangle menuBounds = _popupMenu.getBounds();
			menuBounds.width = menuBounds.width + 40;
			menuBounds.height = menuBounds.height + 40;
			menuBounds.x = menuBounds.x - 20;
			menuBounds.y = menuBounds.y - 20;
			if (e.getSource() instanceof Component) {
				Point pointRelatingToMenu = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), _popupMenu);
				// logger.info ("event="+e);
				// logger.info ("pointRelatingToMenu="+pointRelatingToMenu);
				if (!menuBounds.contains(pointRelatingToMenu)) {
					// Mouse leaves menu
					hidePopupMenu();
				}
			}
		}
	}

	public void resetContextualMenuTriggering() {
		_isPopupTriggering = false;
		_invoker = null;
	}

	// ==========================================================================
	// ============================= Filters ===================================
	// ==========================================================================

	public boolean acceptAction(FlexoActionType<?, ?, ?> action) {
		// override this method to exclude some actions.
		return true;
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> List<FlexoActionType<A, T1, T2>> getActionTypesWithAddType(
			FlexoObject focusedObject, Vector<? extends FlexoObject> globalSelection) {
		List<FlexoActionType<A, T1, T2>> returned = new ArrayList<>();
		if (getEditor() == null) {
			return returned;
		}
		for (FlexoActionType<?, ?, ?> actionType : focusedObject.getActionList()) {
			if (TypeUtils.isAssignableTo(focusedObject, actionType.getFocusedObjectType())
					&& (globalSelection == null || TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
				FlexoActionType<A, T1, T2> cast = (FlexoActionType<A, T1, T2>) actionType;
				if (cast.getActionCategory() == FlexoActionType.ADD_ACTION_TYPE) {
					if (getEditor().isActionVisible(cast, (T1) focusedObject, (Vector<T2>) globalSelection)) {
						if (getEditor().isActionEnabled(cast, (T1) focusedObject, (Vector<T2>) globalSelection)) {
							returned.add(cast);
						}
					}
				}
			}
		}
		return returned;
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> List<FlexoActionType<A, T1, T2>> getActionTypesWithDeleteType(
			FlexoObject focusedObject, Vector<? extends FlexoObject> globalSelection) {
		List<FlexoActionType<A, T1, T2>> returned = new ArrayList<>();
		if (getEditor() == null) {
			return returned;
		}
		for (FlexoActionType<?, ?, ?> actionType : focusedObject.getActionList()) {
			if (TypeUtils.isAssignableTo(focusedObject, actionType.getFocusedObjectType())
					&& (globalSelection == null || TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
				FlexoActionType<A, T1, T2> cast = (FlexoActionType<A, T1, T2>) actionType;
				if (cast.getActionCategory() == FlexoActionType.DELETE_ACTION_TYPE) {
					if (getEditor().isActionVisible(cast, (T1) focusedObject, (Vector<T2>) globalSelection)) {
						if (getEditor().isActionEnabled(cast, (T1) focusedObject, (Vector<T2>) globalSelection)) {
							returned.add(cast);
						}
					}
				}
			}
		}
		return returned;

	}

	// ==========================================================================
	// ============================= Jpopup ===================================
	// ==========================================================================

	public void displayPopupMenu(Component invoker, MouseEvent e) {
		_invoker = invoker;
	}

	public void hidePopupMenu() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("hidePopupMenu()");
		}
		resetContextualMenuTriggering();
		_popupMenu.setVisible(false);
		_isPopupMenuDisplayed = false;
		_popupMenu = null;
	}

	public JPopupMenu makePopupMenu(FlexoObject focusedObject, MenuFilter filter) {
		if (focusedObject != null) {
			ContextualMenu contextualMenu = new ContextualMenu();
			for (FlexoActionType next : focusedObject.getActionList()) {
				if (filter.acceptActionType(next) && getEditor().isActionVisible(next, focusedObject,
						_selectionManager != null ? _selectionManager.getSelection() : null)) {
					contextualMenu.putAction(next);
				}
			}
			if (focusedObject instanceof FlexoConceptInstance) {
				FlexoConceptInstance fci = (FlexoConceptInstance) focusedObject;
				FlexoConcept commonConcept = fci.getFlexoConcept();
				if (FlexoAction.isHomogeneousFlexoConceptInstanceSelection(focusedObject, _selectionManager.getSelection())) {
					if (commonConcept.hasSynchronizationScheme()) {
						contextualMenu.putAction(new SynchronizationSchemeActionType(commonConcept.getSynchronizationScheme(), fci));
					}
					if (commonConcept.hasActionScheme()) {
						for (ActionScheme as : commonConcept.getAccessibleActionSchemes()) {
							contextualMenu.putAction(new ActionSchemeActionType(as, fci));
						}
					}
					if (commonConcept.hasNavigationScheme()) {
						for (NavigationScheme as : commonConcept.getNavigationSchemes()) {
							contextualMenu.putAction(new NavigationSchemeActionType(as, fci));
						}
					}
					if (commonConcept.hasDeletionScheme()) {
						for (DeletionScheme ds : commonConcept.getAccessibleDeletionSchemes()) {
							contextualMenu.putAction(new DeletionSchemeActionType(ds, fci));
						}
					}
					/*if (commonConcept instanceof AbstractVirtualModel) {
						for (FlexoConcept rootConcept : ((AbstractVirtualModel<?>) commonConcept).getAllRootFlexoConcepts()) {
							AddFlexoConceptInstance<AbstractVirtualModelInstance<VMI,?>>
						}
					}*/
				}
			}
			_popupMenu = contextualMenu.makePopupMenu(focusedObject);
		}
		else {
			_popupMenu = new JPopupMenu();
		}
		return _popupMenu;
	}

	public void showPopupMenuForObject(FlexoObject focusedObject, Component invoker, Point position) {
		_invoker = invoker;
		if (focusedObject != null) {
			makePopupMenu(focusedObject, ALL);
			// if (hasSelection())
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("displayPopupMenu() for  " + focusedObject);
			}
			if (_popupMenu.getComponentCount() > 0) {
				_popupMenu.show(_invoker, position.x, position.y);
				_isPopupMenuDisplayed = true;
			}
			else {
				resetContextualMenuTriggering();
			}
		}
	}

	protected class ContextualMenu extends Hashtable<ActionGroup, ContextualMenuGroup> {
		final Hashtable<ActionMenu, ContextualSubMenu> _subMenus = new Hashtable<>();

		public Enumeration<ContextualMenuGroup> orderedGroups() {
			Vector<ContextualMenuGroup> orderedGroups = new Vector<>(values());
			Collections.sort(orderedGroups, new Comparator<ContextualMenuGroup>() {
				@Override
				public int compare(ContextualMenuGroup o1, ContextualMenuGroup o2) {
					return o1.getActionGroup().getIndex() - o2.getActionGroup().getIndex();
				}
			});
			return orderedGroups.elements();
		}

		public void putAction(FlexoActionType<?, ?, ?> actionType) {
			if (acceptAction(actionType)) {
				if (actionType.getActionMenu() != null) {
					ContextualSubMenu subMenu = ensureSubMenuCreated(actionType.getActionMenu());

					/*ContextualSubMenu subMenu = _subMenus.get(actionType.getActionMenu());
					if (subMenu == null) {
						subMenu = new ContextualSubMenu(actionType.getActionMenu());
						addSubMenu(subMenu);
						_subMenus.put(actionType.getActionMenu(), subMenu);
					}*/
					subMenu.addAction(actionType);
				}
				else {
					addAction(actionType);
				}
			}
		}

		private ContextualSubMenu ensureSubMenuCreated(ActionMenu actionMenu) {
			if (actionMenu.getParentMenu() != null) {
				ContextualSubMenu parentMenu = ensureSubMenuCreated(actionMenu.getParentMenu());
				ContextualSubMenu subMenu = parentMenu._subMenus.get(actionMenu);
				if (subMenu == null) {
					subMenu = new ContextualSubMenu(parentMenu, actionMenu);
					parentMenu.addSubMenu(subMenu);
					parentMenu._subMenus.put(actionMenu, subMenu);
				}
				return subMenu;
			}
			else {
				ContextualSubMenu subMenu = _subMenus.get(actionMenu);
				if (subMenu == null) {
					subMenu = new ContextualSubMenu(this, actionMenu);
					addSubMenu(subMenu);
					_subMenus.put(actionMenu, subMenu);
				}
				return subMenu;
			}
		}

		public void addAction(FlexoActionType<?, ?, ?> actionType) {
			if (acceptAction(actionType)) {
				ContextualMenuGroup contextualMenuGroup = get(actionType.getActionGroup());
				if (contextualMenuGroup == null) {
					contextualMenuGroup = new ContextualMenuGroup(actionType.getActionGroup());
					put(actionType.getActionGroup(), contextualMenuGroup);
				}
				contextualMenuGroup.addAction(actionType);
			}
		}

		public void addSubMenu(ContextualSubMenu subMenu) {
			ContextualMenuGroup contextualMenuGroup = get(subMenu.getActionMenu().getActionGroup());
			if (contextualMenuGroup == null) {
				contextualMenuGroup = new ContextualMenuGroup(subMenu.getActionMenu().getActionGroup());
				put(subMenu.getActionMenu().getActionGroup(), contextualMenuGroup);
			}
			contextualMenuGroup.addSubMenu(subMenu);
		}

		public JPopupMenu makePopupMenu(FlexoObject focusedObject) {

			boolean addSeparator = false;
			JPopupMenu returned = new JPopupMenu();
			for (Enumeration<ContextualMenuGroup> en = orderedGroups(); en.hasMoreElements();) {
				ContextualMenuGroup menuGroup = en.nextElement();
				if (addSeparator) {
					returned.addSeparator();
					// System.out.println("------- Ajout de separator -------");
				}
				addSeparator = true;
				// System.out.println("============= Groupe
				// "+menuGroup._actionGroup.getLocalizedName());
				for (Enumeration<?> en2 = menuGroup.elements(); en2.hasMoreElements();) {
					Object nextElement = en2.nextElement();

					if (nextElement instanceof FlexoActionType) {
						// System.out.println("Ajout de " + nextElement);
						makeMenuItem((FlexoActionType) nextElement, focusedObject, returned);
					}
					else if (nextElement instanceof ContextualSubMenu) {
						// System.out.println("Ajout de " + nextElement);
						JMenuItem item = ((ContextualSubMenu) nextElement).makeMenu(focusedObject);
						returned.add(item);
					}
				}
			}
			return returned;
		}

	}

	protected class ContextualMenuGroup extends Vector<Object> {
		private final ActionGroup _actionGroup;

		public ContextualMenuGroup(ActionGroup actionGroup) {
			_actionGroup = actionGroup;
		}

		public void addAction(FlexoActionType<?, ?, ?> actionType) {
			// should have already been checked, but it's more secure.
			if (acceptAction(actionType)) {
				add(actionType);
			}
		}

		public void addSubMenu(ContextualSubMenu subMenu) {
			add(subMenu);
		}

		public ActionGroup getActionGroup() {
			return _actionGroup;
		}
	}

	protected class ContextualSubMenu extends ContextualMenu {
		private final ActionMenu _actionMenu;
		private final ContextualMenu parentMenu;

		public ContextualSubMenu(ContextualMenu parentMenu, ActionMenu actionMenu) {
			_actionMenu = actionMenu;
			this.parentMenu = parentMenu;
		}

		public ContextualMenu getParentMenu() {
			return parentMenu;
		}

		public ActionMenu getActionMenu() {
			return _actionMenu;
		}

		public JMenu makeMenu(FlexoObject focusedObject) {
			boolean addSeparator = false;
			JMenu returned = new JMenu();
			returned.setText(getActionMenu().getLocalizedName(focusedObject.getLocales()));
			if (getActionMenu().getSmallIcon() != null) {
				returned.setIcon(getActionMenu().getSmallIcon());
			}
			for (Enumeration<?> en = orderedGroups(); en.hasMoreElements();) {
				ContextualMenuGroup menuGroup = (ContextualMenuGroup) en.nextElement();
				if (addSeparator) {
					returned.addSeparator();
				}
				addSeparator = true;
				for (Enumeration<?> en2 = menuGroup.elements(); en2.hasMoreElements();) {
					Object nextElement = en2.nextElement();
					if (nextElement instanceof FlexoActionType) {
						makeMenuItem((FlexoActionType) nextElement, focusedObject, returned);
					}
					else if (nextElement instanceof ContextualSubMenu) {
						JMenuItem item = ((ContextualSubMenu) nextElement).makeMenu(focusedObject);
						returned.add(item);
					}
				}
			}
			return returned;
		}

	}

	public interface MenuFilter {
		public boolean acceptActionType(FlexoActionType<?, ?, ?> actionType);
	}

	private <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> JMenuItem makeMenuItem(
			FlexoActionType<A, T1, T2> actionType, FlexoObject focusedObject, JPopupMenu menu) {
		try {
			Vector<T2> globalSelection = new Vector<>();
			if (_selectionManager != null) {
				for (FlexoObject o : _selectionManager.getSelection()) {
					try {
						globalSelection.add((T2) o);
					} catch (ClassCastException e) {
						// This is not good type, discard this object
						logger.warning("Discard from selection " + o);
					}
				}
			}
			EditionAction<A, T1, T2> action = new EditionAction<>(actionType, (T1) focusedObject, globalSelection, getEditor());
			JMenuItem item = menu.add(action);
			if (controller != null && controller.getModuleLocales() != null) {
				item.setText(controller.getModuleLocales().localizedForKey(actionType.getActionName()));
			}
			else {
				item.setText(actionType.getLocalizedName());
			}
			if (getEditor().getKeyStrokeFor(actionType) != null) {
				item.setAccelerator(getEditor().getKeyStrokeFor(actionType));
			}
			if (getEditor().getEnabledIconFor(actionType) != null) {
				item.setIcon(getEditor().getEnabledIconFor(actionType));
			}
			if (getEditor().getDisabledIconFor(actionType) != null) {
				item.setDisabledIcon(getEditor().getDisabledIconFor(actionType));
			}
			return item;
		} catch (ClassCastException exception) {
			logger.warning(
					"ClassCastException raised while trying to build FlexoAction " + actionType + " Exception: " + exception.getMessage());
			return null;
		}
	}

	<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> JMenuItem makeMenuItem(
			FlexoActionType<A, T1, T2> actionType, T1 focusedObject, JMenu menu) {
		try {
			EditionAction<A, T1, T2> action = new EditionAction<>(actionType, focusedObject,
					_selectionManager != null ? (Vector<T2>) _selectionManager.getSelection() : null, getEditor());
			JMenuItem item = menu.add(action);

			if (controller != null && controller.getModuleLocales() != null) {
				item.setText(controller.getModuleLocales().localizedForKey(actionType.getActionName()));
			}
			else {
				item.setText(actionType.getLocalizedName());
			}
			if (getEditor().getKeyStrokeFor(actionType) != null) {
				item.setAccelerator(getEditor().getKeyStrokeFor(actionType));
			}
			if (getEditor().getEnabledIconFor(actionType) != null) {
				item.setIcon(getEditor().getEnabledIconFor(actionType));
			}
			if (getEditor().getDisabledIconFor(actionType) != null) {
				item.setDisabledIcon(getEditor().getDisabledIconFor(actionType));
			}
			return item;
		} catch (ClassCastException exception) {
			logger.warning(
					"ClassCastException raised while trying to build FlexoAction " + actionType + " Exception: " + exception.getMessage());
			return null;
		}
	}

	private JPopupMenu _popupMenu;

	public boolean isPopupMenuDisplayed() {
		return _isPopupMenuDisplayed;
	}

}
