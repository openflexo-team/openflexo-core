/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Gina-swing, a component of the software infrastructure 
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

package org.openflexo.components.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBController;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBCustom;
import org.openflexo.fib.model.FIBCustom.FIBCustomComponent;
import org.openflexo.fib.swing.logging.FlexoLoggingViewer;
import org.openflexo.fib.swing.utils.LoadedClassesInfo;
import org.openflexo.fib.swing.utils.LoadedClassesInfo.ClassInfo;
import org.openflexo.fib.view.FIBView;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.swing.TextFieldCustomPopup;
import org.openflexo.swing.VerticalLayout;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Widget allowing to edit a binding
 * 
 * @author sguerin
 * 
 */
public class TypeSelector extends TextFieldCustomPopup<Type> implements FIBCustomComponent<Type, TypeSelector>, HasPropertyChangeSupport {
	@SuppressWarnings("hiding")
	static final Logger LOGGER = Logger.getLogger(TypeSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.getResourceLocator().locateResource("Fib/TypeSelector.fib");

	public static final Object JAVA_TYPE = new Object() {
		@Override
		public String toString() {
			return "Java type";
		}
	};
	public static final Object JAVA_ARRAY = new Object() {
		@Override
		public String toString() {
			return "Java array";
		}
	};
	public static final Object JAVA_WILDCARD = new Object() {
		@Override
		public String toString() {
			return "Java wilcard";
		}
	};

	private Type _revertValue;

	protected TypeSelectorDetailsPanel _selectorPanel;

	private final List<Object> choices;
	private Object choice;

	private final PropertyChangeSupport pcSupport;

	public TypeSelector(Type editedObject) {
		super(editedObject);
		setRevertValue(editedObject);
		setFocusable(true);
		pcSupport = new PropertyChangeSupport(this);
		choices = new ArrayList<Object>();
		for (PrimitiveType primitiveType : PrimitiveType.values()) {
			choices.add(primitiveType);
		}
		choices.add(JAVA_TYPE);
		choices.add(JAVA_ARRAY);
		choices.add(JAVA_WILDCARD);
		fireEditedObjectChanged();
	}

	public List<Object> getChoices() {
		return choices;
	}

	public Object getChoice() {
		return choice;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public void setChoice(Object choice) {
		if (this.choice != choice) {
			System.out.println("on change le choix pour " + choice);
			Object old = this.choice;
			this.choice = choice;

			if (choice instanceof PrimitiveType) {
				setEditedObject(((PrimitiveType) choice).getType());
			}
			getPropertyChangeSupport().firePropertyChange("choice", old, choice);
			if (isJavaType()) {
				getPropertyChangeSupport().firePropertyChange("loadedClassesInfo", null, getLoadedClassesInfo());
			}
		}
	}

	public boolean isJavaType() {
		return (choice instanceof PrimitiveType || choice == JAVA_TYPE || choice == JAVA_ARRAY || choice == JAVA_WILDCARD);
	}

	public boolean isPrimitiveType() {
		return getPrimitiveType() != null;
	}

	public PrimitiveType getPrimitiveType() {
		for (PrimitiveType primitiveType : PrimitiveType.values()) {
			if (primitiveType.getType().equals(getEditedObject())) {
				return primitiveType;
			}
		}
		return null;
	}

	@Override
	public void setEditedObject(Type object) {
		super.setEditedObject(object);
	}

	@Override
	public void fireEditedObjectChanged() {
		super.fireEditedObjectChanged();

		System.out.println("Hop, on fait setEditedObject with " + getEditedObject());

		// First try to find the type of object
		PrimitiveType primitiveType = getPrimitiveType();
		if (primitiveType != null) {
			setChoice(primitiveType);
		} else {
			if (getEditedObject() instanceof Class) {
				setChoice(JAVA_TYPE);
			}
			if (choice == JAVA_TYPE || choice == JAVA_ARRAY || choice == JAVA_WILDCARD) {
				getLoadedClassesInfo().setSelectedClassInfo(getLoadedClassesInfo().getClass(getBaseClass()));
				System.out.println("On a selectionne: " + getLoadedClassesInfo().getSelectedClassInfo());
				getPropertyChangeSupport().firePropertyChange("loadedClassesInfo", null, getLoadedClassesInfo());
			}
		}
	}

	public Class<?> getBaseClass() {
		return TypeUtils.getBaseClass(getEditedObject());
	}

	private boolean isListeningLoadedClassesInfo = false;

	public LoadedClassesInfo getLoadedClassesInfo() {

		LoadedClassesInfo returned = LoadedClassesInfo.instance(getBaseClass());
		if (!isListeningLoadedClassesInfo) {
			returned.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("selectedClassInfo")) {
						if (isJavaType()) {
							ClassInfo classInfo = (ClassInfo) evt.getNewValue();
							System.out.println("Hop, on y est classInfo=" + classInfo);
							if (classInfo != null) {
								setEditedObject(classInfo.getClazz());
							}
						}
					}
				}
			});
			isListeningLoadedClassesInfo = true;
		}
		return returned;
	}

	@Override
	public void delete() {
		super.delete();
		if (_selectorPanel != null) {
			_selectorPanel.delete();
			_selectorPanel = null;
		}

	}

	@Override
	public void init(FIBCustom component, FIBController controller) {
	}

	@Override
	public void setRevertValue(Type oldValue) {
		// WARNING: we need here to clone to keep track back of previous data !!!
		if (oldValue != null) {
			_revertValue = oldValue;
		} else {
			_revertValue = null;
		}
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Sets revert value to " + _revertValue);
		}
	}

	@Override
	public Type getRevertValue() {
		return _revertValue;
	}

	@Override
	protected ResizablePanel createCustomPanel(Type editedObject) {
		_selectorPanel = makeCustomPanel(editedObject);
		return _selectorPanel;
	}

	protected TypeSelectorDetailsPanel makeCustomPanel(Type editedObject) {
		return new TypeSelectorDetailsPanel(editedObject);
	}

	@Override
	public void updateCustomPanel(Type editedObject) {
		// logger.info("updateCustomPanel with "+editedObject+" _selectorPanel="+_selectorPanel);
		if (_selectorPanel != null) {
			_selectorPanel.update();
		}
	}

	public class TypeSelectorDetailsPanel extends ResizablePanel {
		private final FIBComponent fibComponent;
		private FIBView fibView;
		private CustomFIBController controller;

		protected TypeSelectorDetailsPanel(Type aClass) {
			super();

			fibComponent = FIBLibrary.instance().retrieveFIBComponent(FIB_FILE_NAME, true);
			controller = new CustomFIBController(fibComponent);
			fibView = controller.buildView(fibComponent);

			controller.setDataObject(TypeSelector.this);

			setLayout(new BorderLayout());
			add(fibView.getResultingJComponent(), BorderLayout.CENTER);

		}

		public void delete() {
			controller.delete();
			fibView.delete();
			controller = null;
			fibView = null;
		}

		public void update() {
			controller.setDataObject(TypeSelector.this);
		}

		@Override
		public Dimension getDefaultSize() {
			return new Dimension(fibComponent.getWidth(), fibComponent.getHeight());
		}

		public class CustomFIBController extends FIBController {
			public CustomFIBController(FIBComponent component) {
				super(component);
			}

			public void apply() {
				// setEditedObject(LoadedClassesInfo.instance().getSelectedClassInfo().getRepresentedClass());
				TypeSelector.this.apply();
			}

			public void cancel() {
				TypeSelector.this.cancel();
			}

			public void reset() {
				setEditedObject(null);
				TypeSelector.this.apply();
			}

			public void classChanged() {
				System.out.println("Class changed !!!");
			}

		}

	}

	/* @Override
	 public void setEditedObject(BackgroundStyle object)
	 {
	 	logger.info("setEditedObject with "+object);
	 	super.setEditedObject(object);
	 }*/

	@Override
	public void apply() {

		System.out.println("SelectedClassInfo=" + getLoadedClassesInfo().getSelectedClassInfo());

		setRevertValue(getEditedObject());
		closePopup();
		super.apply();
	}

	@Override
	public void cancel() {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("CANCEL: revert to " + getRevertValue());
		}
		setEditedObject(getRevertValue());
		closePopup();
		super.cancel();
	}

	@Override
	protected void deletePopup() {
		if (_selectorPanel != null) {
			_selectorPanel.delete();
		}
		_selectorPanel = null;
		super.deletePopup();
	}

	/*protected void pointerLeavesPopup()
	{
	   cancel();
	}*/

	public TypeSelectorDetailsPanel getSelectorPanel() {
		return _selectorPanel;
	}

	@Override
	public TypeSelector getJComponent() {
		return this;
	}

	@Override
	public Class<Type> getRepresentedType() {
		return Type.class;
	}

	@Override
	public String renderedString(Type editedObject) {
		if (editedObject == null) {
			return "";
		}
		return TypeUtils.simpleRepresentation(editedObject);
	}

	/**
	 * This main allows to launch an application testing the BindingSelector
	 * 
	 * @param args
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void main(String[] args) throws SecurityException, IOException {

		Resource loggingFile = ResourceLocator.locateResource("Config/logging_INFO.properties");
		FlexoLoggingManager.initialize(-1, true, loggingFile, Level.INFO, null);
		final JDialog dialog = new JDialog((Frame) null, false);

		final TypeSelector selector = new TypeSelector(String.class) {
			@Override
			public void apply() {
				super.apply();
				System.out.println("Apply, getEditedObject()=" + getEditedObject());
			}

			@Override
			public void cancel() {
				super.cancel();
				System.out.println("Cancel, getEditedObject()=" + getEditedObject());
			}
		};
		selector.setRevertValue(Object.class);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selector.delete();
				dialog.dispose();
				System.exit(0);
			}
		});

		JButton logButton = new JButton("Logs");
		logButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FlexoLoggingViewer.showLoggingViewer(FlexoLoggingManager.instance(), dialog);
			}
		});

		JPanel panel = new JPanel(new VerticalLayout());
		panel.add(selector);

		panel.add(closeButton);
		panel.add(logButton);

		dialog.setPreferredSize(new Dimension(550, 600));
		dialog.getContentPane().add(panel);
		dialog.pack();

		dialog.setVisible(true);
	}
}