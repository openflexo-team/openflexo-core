package org.openflexo.view.menu;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.action.MenuItemAction;

public class FlexoMenuItemWithFactory<A extends FlexoAction<A, FlexoObject, FlexoObject>> extends FlexoMenuItem {
	private FlexoActionFactory<A, FlexoObject, FlexoObject> actionType;

	public FlexoMenuItemWithFactory(FlexoActionFactory<A, FlexoObject, FlexoObject> actionType, KeyStroke accelerator, Icon icon,
			FlexoController controller) {
		this(actionType, controller);
		setIcon(icon);
		if (accelerator != null) {
			setAccelerator(accelerator);
		}
	}

	/* Unused 
	private FlexoMenuItemWithFactory(FlexoActionFactory<A, FlexoObject, FlexoObject> actionType, Icon icon, FlexoController controller) {
		this(actionType, controller);
		setIcon(icon);
	}
	*/

	public FlexoMenuItemWithFactory(FlexoActionFactory<A, FlexoObject, FlexoObject> actionType, FlexoController controller) {
		super(controller, actionType.getUnlocalizedName());
		this.actionType = actionType;
		setAction(new MenuItemAction<A, FlexoObject, FlexoObject>(actionType, this));
	}

	/**
	 * 
	 */
	public void itemWillShow() {
		if (actionType instanceof FlexoActionFactory && getSelectionManager() != null) {
			if (getFocusedObject() == null || getFocusedObject().getActionList().indexOf(actionType) > -1) {
				setEnabled(actionType.isEnabled(getFocusedObject(), getGlobalSelection()));
			}
			else {
				setEnabled(false);
			}
		}
	}
}
