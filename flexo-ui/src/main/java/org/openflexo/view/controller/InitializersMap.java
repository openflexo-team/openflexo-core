package org.openflexo.view.controller;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.selection.MouseSelectionManager;

public class InitializersMap {
	private final Map<FlexoActionFactory<?, ?, ?>, ActionInitializer<?, ?, ?>> initializersByFactory = new Hashtable<>();
	private final Map<Class<?>, ActionInitializer<?, ?, ?>> initializersByActionClass = new Hashtable<>();

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void put(
			FlexoActionFactory<A, T1, T2> actionFactory, ActionInitializer<A, T1, T2> initializer) {
		initializersByFactory.put(actionFactory, initializer);
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void put(Class<A> actionClass,
			ActionInitializer<A, T1, T2> initializer) {
		initializersByActionClass.put(actionClass, initializer);
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> get(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		return (ActionInitializer<A, T1, T2>) initializersByFactory.get(actionFactory);
	}

	@SuppressWarnings("unchecked")
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> ActionInitializer<A, T1, T2> get(
			Class<A> actionClass) {
		return (ActionInitializer<A, T1, T2>) TypeUtils.objectForClass(actionClass, initializersByActionClass);
	}

	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void remove(
			FlexoActionFactory<A, T1, T2> actionFactory) {
		initializersByFactory.remove(actionFactory);
	}

	public void remove(Class<?> actionType) {
		initializersByActionClass.remove(actionType);
	}

	public void registerShortcuts(FlexoController controller) {
		for (final Entry<FlexoActionFactory<?, ?, ?>, ActionInitializer<?, ?, ?>> entry : initializersByFactory.entrySet()) {
			KeyStroke accelerator = entry.getValue().getShortcut();
			if (accelerator != null) {
				controller.registerActionForKeyStroke(new AbstractAction() {
					@SuppressWarnings("unchecked")
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("actionPerformed() with " + entry.getKey());
						MouseSelectionManager sm = controller.getSelectionManager();
						FlexoObject focusedObject = sm.getFocusedObject();
						Vector<? extends FlexoObject> globalSelection = sm.getSelection();
						@SuppressWarnings("rawtypes")
						FlexoActionFactory actionType = entry.getKey();
						if (TypeUtils.isAssignableTo(focusedObject, actionType.getFocusedObjectType()) && (globalSelection == null
								|| TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
							controller.getEditor().performActionFactory(actionType, focusedObject, globalSelection, e);
						}
					}
				}, accelerator, entry.getKey().getUnlocalizedName());
			}
		}
	}

}
