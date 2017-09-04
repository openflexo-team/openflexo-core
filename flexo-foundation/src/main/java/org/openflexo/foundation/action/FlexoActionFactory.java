/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.action;

import java.awt.Component;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;

/**
 * A factory for {@link FlexoAction}
 * 
 * Implements stuff to instantiate {@link FlexoAction} in a given context, and allowing to integrate {@link FlexoAction} management in a
 * editing environment
 * 
 * @author sylvain
 *
 * @param <A>
 *            type of FlexoAction
 * @param <T1>
 *            type of object such {@link FlexoAction} is to be applied as focused object
 * @param <T2>
 *            type of additional object such {@link FlexoAction} is to be applied as global selection
 */
public abstract class FlexoActionFactory<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> {

	private static final Logger logger = Logger.getLogger(FlexoActionFactory.class.getPackage().getName());

	public static final ActionGroup inspectGroup = new ActionGroup("inspect", 0);
	public static final ActionGroup defaultGroup = new ActionGroup("default", 1);
	public static final ActionGroup editGroup = new ActionGroup("edit", 2);
	// 3 to 9 are reserved for custom groups
	public static final ActionGroup printGroup = new ActionGroup("print", 10);
	public static final ActionGroup helpGroup = new ActionGroup("help", 11);
	public static final ActionGroup docGroup = new ActionGroup("documentation", 12);

	public static final ActionMenu newMenu = new ActionMenu("new", defaultGroup);
	public static final ActionGroup newMenuGroup1 = new ActionGroup("new_group_1", 0);
	public static final ActionGroup newMenuGroup2 = new ActionGroup("new_group_2", 1);
	public static final ActionGroup newMenuGroup3 = new ActionGroup("new_group_3", 2);
	public static final ActionGroup newMenuGroup4 = new ActionGroup("new_group_4", 2);

	public static final ActionMenu newVirtualModelMenu = new ActionMenu("virtual_model", defaultGroup, newMenu);
	public static final ActionMenu newPropertyMenu = new ActionMenu("property", defaultGroup, newMenu);
	public static final ActionMenu newBehaviourMenu = new ActionMenu("behaviour", defaultGroup, newMenu);

	public static final ActionMenu importMenu = new ActionMenu("import", defaultGroup);
	public static final ActionMenu exportMenu = new ActionMenu("export", defaultGroup);
	public static final ActionMenu convertMenu = new ActionMenu("convert_to", defaultGroup);

	public static final ActionMenu executionModelMenu = new ActionMenu("execution_model", defaultGroup);

	public static final int NORMAL_ACTION_TYPE = 0;
	public static final int ADD_ACTION_TYPE = 1;
	public static final int DELETE_ACTION_TYPE = 2;

	private String _actionName;
	// private Icon _smallIcon;
	// private Icon _smallDisabledIcon;
	private ActionGroup _actionGroup;
	private ActionMenu _actionMenu;
	private int _actionCategory;

	// private Vector _modulesWhereActionIsRegistered;

	protected FlexoActionFactory(String actionName) {
		this(actionName, null, defaultGroup, NORMAL_ACTION_TYPE);
	}

	public Type getFocusedObjectType() {
		Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), FlexoActionFactory.class);
		for (Entry<TypeVariable<?>, Type> e : typeArguments.entrySet()) {
			if (e.getKey().getName().equals("T1") && e.getKey().getGenericDeclaration() == FlexoActionFactory.class) {
				return e.getValue();
			}
		}
		return FlexoObject.class;
	}

	public Type getGlobalSelectionType() {
		Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), FlexoActionFactory.class);
		for (Entry<TypeVariable<?>, Type> e : typeArguments.entrySet()) {
			if (e.getKey().getName().equals("T2") && e.getKey().getGenericDeclaration() == FlexoActionFactory.class) {
				return e.getValue();
			}
		}
		return FlexoObject.class;
	}

	/*protected FlexoActionFactory (String actionName, Icon icon)
	{
	    this(actionName,null,defaultGroup,icon,NORMAL_ACTION_TYPE);
	}*/

	protected FlexoActionFactory(String actionName, ActionGroup actionGroup) {
		this(actionName, null, actionGroup, NORMAL_ACTION_TYPE);
	}

	protected FlexoActionFactory(String actionName, ActionGroup actionGroup, ActionMenu actionMenu) {
		this(actionName, actionMenu, actionGroup, NORMAL_ACTION_TYPE);
	}

	/*protected FlexoActionFactory (String actionName, ActionGroup actionGroup, Icon icon)
	{
	    this(actionName,null,actionGroup,icon,NORMAL_ACTION_TYPE);
	}*/

	protected FlexoActionFactory(String actionName, ActionMenu actionMenu, ActionGroup actionGroup) {
		this(actionName, actionMenu, actionGroup, NORMAL_ACTION_TYPE);
	}

	/* protected FlexoActionFactory (String actionName, ActionMenu actionMenu, ActionGroup actionGroup, Icon icon)
	 {
	     this(actionName,actionMenu,actionGroup,icon,NORMAL_ACTION_TYPE);
	 }*/

	protected FlexoActionFactory(String actionName, int actionCategory) {
		this(actionName, defaultGroup, null, actionCategory);
	}

	/* protected FlexoActionFactory (String actionName, Icon icon, int actionCategory)
	 {
	     this(actionName,null,defaultGroup,icon,actionCategory);
	 }*/

	protected FlexoActionFactory(String actionName, ActionGroup actionGroup, int actionCategory) {
		this(actionName, actionGroup, null, actionCategory);
	}

	protected FlexoActionFactory(String actionName, ActionGroup actionGroup, ActionMenu actionMenu, int actionCategory) {
		this(actionName, actionMenu, actionGroup, actionCategory);
	}

	/*protected FlexoActionFactory (String actionName, ActionGroup actionGroup, Icon icon, int actionCategory)
	{
	    this(actionName,null,actionGroup,icon,actionCategory);
	}*/

	protected FlexoActionFactory(String actionName, ActionMenu actionMenu, ActionGroup actionGroup, int actionCategory) {
		super();
		_actionCategory = actionCategory;
		_actionName = actionName;
		setActionMenu(actionMenu);
		setActionGroup(actionGroup);
	}

	/*protected FlexoActionFactory (String actionName, ActionMenu actionMenu, ActionGroup actionGroup, Icon icon, int actionCategory)
	{
	    super();
	    _actionCategory = actionCategory;
	    _actionName = actionName;
	    setActionMenu(actionMenu);
	    setActionGroup(actionGroup);
	    setSmallIcon(icon);
	    //_modulesWhereActionIsRegistered = new Vector();
	}*/

	/*public void registerInModule(IModule module)
	{
	    _modulesWhereActionIsRegistered.add(module);
	}
	
	private boolean hasAtLeastAnAvailableModule()
	{
	    for (Enumeration en=_modulesWhereActionIsRegistered.elements(); en.hasMoreElements();) {
	        IModule next = (IModule)en.nextElement();
	        if (next.isAvailable()) return true;
	    }
	    if (logger.isLoggable(Level.FINE))
	        logger.fine ("FlexoActionFactory: sorry action not available !");
	    return false;
	}*/

	public String getUnlocalizedName() {
		return _actionName;
	}

	public LocalizedDelegate getLocales() {
		return FlexoLocalization.getMainLocalizer();
	}

	public String getLocalizedName() {
		return getLocales().localizedForKey(_actionName);
	}

	public String getLocalizedName(Component component) {
		return getLocales().localizedForKey(_actionName, component);
	}

	public String getLocalizedDescription() {
		return getLocales().localizedForKey(_actionName + "_description");
	}

	/**
	 * Deprecated call to an action building outside the context of a FlexoEditor Please DON'T use it anymore !!!
	 * 
	 * All old implementations using this method must be rewritten
	 * 
	 * @deprecated
	 * @param focusedObject
	 *            the focused object
	 * @param globalSelection
	 *            a vector of FlexoModelObject, which represent all the selected objects
	 * @return
	 */
	@Deprecated
	public A makeNewAction(T1 focusedObject, Vector<T2> globalSelection) {
		return makeNewAction(focusedObject, globalSelection, null);
	}

	/**
	 * 
	 * @param focusedObject
	 *            the focused object
	 * @param globalSelection
	 *            a vector of FlexoModelObject, which represent all the selected objects
	 * @param editor
	 *            TODO
	 * @return
	 */
	public abstract A makeNewAction(T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor);

	/**
	 * 
	 * @param focusedObject
	 *            the focused object
	 * @param globalSelection
	 *            a vector of FlexoModelObject, which represent all the selected objects
	 * @param editor
	 *            TODO
	 * @return
	 */
	public A makeNewEmbeddedAction(T1 focusedObject, Vector<T2> globalSelection, FlexoAction<?, ?, ?> ownerAction) {
		A returned = makeNewAction(focusedObject, globalSelection, ownerAction.getEditor());
		returned.setOwnerAction(ownerAction);
		ownerAction.addToEmbeddedActions(returned);
		return returned;
	}

	/**
	 * Indicates if this action (eventually disabled) might be presented
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean isVisibleForSelection(T1 object, Vector<T2> globalSelection);

	/**
	 * Indicates if this action (eventually disabled) is enabled
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean isEnabledForSelection(T1 object, Vector<T2> globalSelection);

	/**
	 * Indicates if this action is enabled (assert that action is visible)
	 * 
	 * @param object
	 * @return
	 */
	public boolean isEnabled(T1 object, Vector<T2> globalSelection) {
		if (object != null && object.getActionList().indexOf(this) == -1) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Cannot execute " + getLocalizedName() + " on " + object.getClass().getName()
						+ " because action is not registered on this object type");
			}
			return false;
		}

		return isEnabledForSelection(object, globalSelection);
	}

	public String getDisabledReason(T1 object, Vector<T2> globalSelection, FlexoEditor editor) {
		if (object != null && object.getActionList().indexOf(this) == -1) {
			return getLocales().localizedForKey("action") + " " + getLocalizedName() + " "
					+ getLocales().localizedForKey("is_not_active_for") + " "
					+ getLocales().localizedForKey(object.getClass().getSimpleName());
		}
		if (!isEnabledForSelection(object, globalSelection)) {
			return getLocales().localizedForKey("action") + " " + getLocalizedName() + " "
					+ getLocales().localizedForKey("is_not_active_for_this_selection");
		}
		return null;
	}

	public String getActionName() {
		return _actionName;
	}

	public ActionGroup getActionGroup() {
		return _actionGroup;
	}

	public void setActionGroup(ActionGroup actionGroup) {
		_actionGroup = actionGroup;
	}

	public ActionMenu getActionMenu() {
		return _actionMenu;
	}

	public void setActionMenu(ActionMenu actionMenu) {
		_actionMenu = actionMenu;
	}

	public int getActionCategory() {
		return _actionCategory;
	}

	protected String[] getPersistentProperties() {
		return new String[0];
	}

}
