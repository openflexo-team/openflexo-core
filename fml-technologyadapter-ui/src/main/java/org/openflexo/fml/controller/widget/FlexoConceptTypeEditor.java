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

package org.openflexo.fml.controller.widget;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.widget.DefaultCustomTypeEditorImpl;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.CustomTypeManager;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLRTType;
import org.openflexo.foundation.fml.FMLRTWildcardType;
import org.openflexo.foundation.fml.ta.FlexoConceptType;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.controller.CustomTypeEditorProvider;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent.CustomComponentParameter;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * An editor to edit a {@link FlexoConceptType}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/FlexoConceptTypeEditor.fib")
public class FlexoConceptTypeEditor extends FMLCustomTypeEditor<FlexoConceptType> implements PropertyChangeListener {

	public static final Object WILDCARD = new Object() {
		@Override
		public String toString() {
			return "Wilcard";
		}
	};

	private FlexoConceptType editedType;

	private final List<Object> choices;
	private Object choice;

	private final List<GenericParameter> genericParameters;
	private final List<GenericBound> upperBounds;
	private final List<GenericBound> lowerBounds;

	private CustomTypeManager customTypeManager;
	private CustomTypeEditorProvider customTypeEditorProvider;

	private final Map<Class<? extends CustomType>, CustomTypeEditor<?>> customTypeEditors = new HashMap<>();
	private final Map<Class<? extends CustomType>, CustomTypeFactory<?>> customTypeFactories = new HashMap<>();

	public FlexoConceptTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
		choices = new ArrayList<>();
		genericParameters = new ArrayList<>();
		upperBounds = new ArrayList<>();
		lowerBounds = new ArrayList<>();

		updateChoices();
		choice = null;
	}

	@Override
	public FlexoConceptType getEditedType() {
		return editedType;
	}

	private void setEditedType(FlexoConceptType editedType) {
		if ((editedType == null && this.editedType != null) || (editedType != null && !editedType.equals(this.editedType))) {
			FlexoConceptType oldValue = this.editedType;
			this.editedType = editedType;
			getPropertyChangeSupport().firePropertyChange("editedType", oldValue, editedType);
		}
	}

	public CustomTypeManager getCustomTypeManager() {
		return customTypeManager;
	}

	@CustomComponentParameter(name = "customTypeManager", type = CustomComponentParameter.Type.OPTIONAL)
	public void setCustomTypeManager(CustomTypeManager customTypeManager) {

		// System.out.println("******************> setCustomTypeManager with " + customTypeManager);

		if (customTypeManager != this.customTypeManager) {
			CustomTypeManager oldValue = this.customTypeManager;
			this.customTypeManager = customTypeManager;
			updateChoices();
			getPropertyChangeSupport().firePropertyChange("customTypeManager", oldValue, customTypeManager);
		}
	}

	public CustomTypeEditorProvider getCustomTypeEditorProvider() {
		return customTypeEditorProvider;
	}

	public void setCustomTypeEditorProvider(CustomTypeEditorProvider customTypeEditorProvider) {

		if ((customTypeEditorProvider == null && this.customTypeEditorProvider != null)
				|| (customTypeEditorProvider != null && !customTypeEditorProvider.equals(this.customTypeEditorProvider))) {
			CustomTypeEditorProvider oldValue = this.customTypeEditorProvider;
			this.customTypeEditorProvider = customTypeEditorProvider;
			updateChoices();
			getPropertyChangeSupport().firePropertyChange("customTypeEditorProvider", oldValue, customTypeEditorProvider);
		}
	}

	public <T extends CustomType> CustomTypeEditor<T> getCustomTypeEditor(Class<T> typeClass) {
		return (CustomTypeEditor<T>) customTypeEditors.get(typeClass);
	}

	public <T extends CustomType> CustomTypeFactory<T> getCustomTypeFactory(Class<T> typeClass) {
		return (CustomTypeFactory<T>) customTypeFactories.get(typeClass);
	}

	private void updateChoices() {
		choices.clear();

		// First add the technology specific types
		if (getCustomTypeManager() != null) {
			for (Class<? extends CustomType> customType : getCustomTypeManager().getCustomTypeFactories().keySet()) {
				if (isAllowed(customType)) {
					CustomTypeFactory<?> specificFactory = getCustomTypeManager().getCustomTypeFactories().get(customType);
					if (getCustomTypeEditorProvider() != null) {
						CustomTypeEditor<?> specificEditor = getCustomTypeEditorProvider().getCustomTypeEditor(customType);
						if (specificFactory != null && specificEditor != null) {
							choices.add(customType);
							customTypeFactories.put(customType, specificFactory);
							customTypeEditors.put(customType, specificEditor);
						}
					}
				}
			}
		}

		// Then wildcard
		choices.add(WILDCARD);
	}

	// Return flag indicating if this type is allowed in a FMLType
	private boolean isAllowed(Class<? extends CustomType> customType) {
		if (customType.getName().equals("org.openflexo.foundation.fml.VirtualModelInstanceType")) {
			return true;
		}
		if (customType.getName().equals("org.openflexo.foundation.fml.FlexoConceptInstanceType")) {
			return true;
		}
		return false;
	}

	public List<Object> getChoices() {
		return choices;
	}

	public Object getChoice() {
		return choice;
	}

	public void setChoice(Object choice) {
		if (this.choice != choice) {
			CustomTypeFactory<?> oldCustomTypeFactory = getCurrentCustomTypeFactory();
			CustomTypeEditor<?> oldCustomTypeEditor = getCurrentCustomTypeEditor();

			boolean oldIsWildcard = isWildcard();
			boolean oldIsCustomType = isCustomType();

			Object old = this.choice;

			if (old instanceof CustomTypeFactory) {
				((CustomTypeFactory) old).getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			if (oldCustomTypeEditor != null) {
				oldCustomTypeEditor.getPropertyChangeSupport().removePropertyChangeListener(this);
			}

			this.choice = choice;

			if (isCustomType()) {
				CustomTypeFactory factory = getCurrentCustomTypeFactory();
				factory.getPropertyChangeSupport().addPropertyChangeListener(this);

				getCurrentCustomTypeEditor().getPropertyChangeSupport().addPropertyChangeListener(this);

				FMLRTType newType = (FMLRTType) factory.makeCustomType(null);
				// System.out.println("Bon, un truc a faire avec: " + newType + " of " + newType.getClass());

				if (getEditedType() instanceof CustomType) {

					try {
						factory.configureFactory(getEditedType());
					} catch (ClassCastException e) {
						// That may happen if we have changed from a CustomTypefactory to another CustomTypefactory
						// setEditedType(factory.makeCustomType(null));
						// Type newType = factory.makeCustomType(null);
						System.out.println("ClassCastException : un truc a faire avec: " + newType + " of " + newType.getClass());
					}
					// Type newType = factory.makeCustomType(null);
				}
				/*else {
					// setEditedObject(factory.makeCustomType(null));
					Type newType = factory.makeCustomType(null);
					System.out.println("Un truc a faire ici avec: " + newType + " of " + newType.getClass());
				}*/

				setEditedType(new FlexoConceptType(newType));
			}

			if (choice == WILDCARD && !(getEditedType() instanceof WildcardType)) {
				makeWildcardType();
			}

			getPropertyChangeSupport().firePropertyChange("choice", old, choice);
			getPropertyChangeSupport().firePropertyChange("isWildcard", oldIsWildcard, isWildcard());
			getPropertyChangeSupport().firePropertyChange("isCustomType", oldIsCustomType, isCustomType());
			getPropertyChangeSupport().firePropertyChange("currentCustomTypeFactory", oldCustomTypeFactory, getCurrentCustomTypeFactory());
			getPropertyChangeSupport().firePropertyChange("currentCustomTypeEditor", oldCustomTypeEditor, getCurrentCustomTypeEditor());
		}
	}

	/*public Class<?> getBaseClass() {
		return TypeUtils.getBaseClass(getEditedObject());
	}*/

	private CustomTypeFactory<?> getCurrentCustomTypeFactory() {
		if (isCustomType()) {
			return customTypeFactories.get(choice);
		}
		return null;
	}

	public CustomTypeEditor<?> getCurrentCustomTypeEditor() {
		if (isCustomType()) {
			return customTypeEditors.get(choice);
		}
		return null;
	}

	public boolean isWildcard() {
		return (choice == WILDCARD);
	}

	public boolean isCustomType() {
		return (choice instanceof Class);
	}

	// Will be usefull when FML will become parameterized (see TypeSelector.java)
	public boolean hasGenericParameters() {
		return false;
	}

	// Will be usefull when FML will become parameterized (see TypeSelector.java)
	public List<GenericParameter> getGenericParameters() {
		return genericParameters;
	}

	public String getPresentationName(Object aChoice) {
		if (aChoice instanceof Class) {
			CustomTypeEditor<?> editor = getCustomTypeEditor((Class<? extends CustomType>) aChoice);
			if (editor != null) {
				return editor.getPresentationName();
			}
		}
		return aChoice.toString();
	}

	@Override
	public String getPresentationName() {
		return AbstractFMLTypingSpace.CONCEPT;
	}

	@Override
	public Class<FlexoConceptType> getCustomType() {
		return FlexoConceptType.class;
	}

	public List<GenericBound> getUpperBounds() {
		return upperBounds;
	}

	public GenericBound createUpperBound() {
		GenericBound returned = new GenericBound(null);
		upperBounds.add(returned);
		getPropertyChangeSupport().firePropertyChange("upperBounds", null, returned);
		makeWildcardType();
		return returned;
	}

	public void deleteUpperBound(GenericBound bound) {
		bound.delete();
		upperBounds.remove(bound);
		getPropertyChangeSupport().firePropertyChange("upperBounds", bound, null);
		makeWildcardType();
	}

	public List<GenericBound> getLowerBounds() {
		return lowerBounds;
	}

	public GenericBound createLowerBound() {
		GenericBound returned = new GenericBound(null);
		lowerBounds.add(returned);
		getPropertyChangeSupport().firePropertyChange("lowerBounds", null, returned);
		makeWildcardType();
		return returned;
	}

	public void deleteLowerBound(GenericBound bound) {
		bound.delete();
		lowerBounds.remove(bound);
		getPropertyChangeSupport().firePropertyChange("lowerBounds", bound, null);
		makeWildcardType();
	}

	private void makeWildcardType() {
		FMLRTType[] upper = new FMLRTType[upperBounds.size()];
		for (int i = 0; i < upperBounds.size(); i++)
			upper[i] = upperBounds.get(i).getType();
		FMLRTType[] lower = new FMLRTType[lowerBounds.size()];
		for (int i = 0; i < lowerBounds.size(); i++)
			lower[i] = lowerBounds.get(i).getType();

		FMLRTWildcardType wildcard = new FMLRTWildcardType(upper, lower);
		FlexoConceptType newType = new FlexoConceptType(wildcard);
		setEditedType(newType);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getCurrentCustomTypeEditor()) {
			setEditedType(new FlexoConceptType((FMLRTType) getCurrentCustomTypeEditor().getEditedType()));
		}
	}

	@Override
	public FlexoFIBController makeFIBController() {
		// System.out.println("makeFIBController() in DefaultCustomTypeEditorImpl");
		FIBComponent component = ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(getFIBComponentResource());
		FlexoController controller = ((ApplicationContext) getServiceManager()).getModuleLoader().getActiveModule().getController();

		return new FlexoConceptTypeEditorFIBController(component, this, controller);
	}

	public static class FlexoConceptTypeEditorFIBController extends SelectorFIBController {
		// Unused private DefaultCustomTypeEditorImpl<?> editor;

		public FlexoConceptTypeEditorFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public FlexoConceptTypeEditorFIBController(FIBComponent component, DefaultCustomTypeEditorImpl<?> editor,
				FlexoController controller) {
			super(component, SwingViewFactory.INSTANCE);
			// Unused this.editor = editor;
			setFlexoController(controller);
		}

		@Override
		public void setFlexoController(FlexoController aController) {
			super.setFlexoController(aController);
			performEditorConfiguration();
		}

		@Override
		public void setDataObject(Object anObject) {
			super.setDataObject(anObject);
			performEditorConfiguration();
		}

		private void performEditorConfiguration() {
			if (getDataObject() instanceof FlexoConceptTypeEditor && getFlexoController() != null) {
				((FlexoConceptTypeEditor) getDataObject())
						.setCustomTypeManager(getFlexoController().getApplicationContext().getTechnologyAdapterService());
				((FlexoConceptTypeEditor) getDataObject())
						.setCustomTypeEditorProvider(getFlexoController().getApplicationContext().getTechnologyAdapterControllerService());
			}
		}

	}

	public class GenericParameter extends PropertyChangedSupportDefaultImplementation {
		private TypeVariable<?> typeVariable;
		private Type type;

		public GenericParameter(TypeVariable<?> typeVariable, Type type) {
			super();
			this.typeVariable = typeVariable;
			this.type = type;
		}

		public GenericParameter(TypeVariable<?> typeVariable) {
			super();
			this.typeVariable = typeVariable;
		}

		public void delete() {
			this.typeVariable = null;
			this.type = null;
		}

		public TypeVariable<?> getTypeVariable() {
			return typeVariable;
		}

		public void setTypeVariable(TypeVariable<?> typeVariable) {
			if (typeVariable != this.typeVariable) {
				TypeVariable<?> oldValue = this.typeVariable;
				this.typeVariable = typeVariable;
				getPropertyChangeSupport().firePropertyChange("typeVariable", oldValue, typeVariable);
			}
		}

		public Type getType() {
			if (type == null) {
				return getUpperBound();
			}
			return type;
		}

		public void setType(Type type) {

			if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
				Type oldValue = this.type;
				this.type = type;

				// setEditedObject(makeParameterizedType(getBaseClass()));

				getPropertyChangeSupport().firePropertyChange("type", oldValue, type);
			}
		}

		@Override
		public String toString() {
			return typeVariable.getName() + "=" + TypeUtils.fullQualifiedRepresentation(type);
		}

		public ImageIcon getIcon() {
			return isValid() ? UtilsIconLibrary.OK_ICON : UtilsIconLibrary.ERROR_ICON;
		}

		public boolean isValid() {
			return (TypeUtils.isTypeAssignableFrom(getUpperBound(), getType()));
		}

		public Type getUpperBound() {
			if (getTypeVariable() != null && getTypeVariable().getBounds().length > 0) {
				return getTypeVariable().getBounds()[0];
			}
			return Object.class;
		}

		public String getTypeStringRepresentation() {
			return TypeUtils.simpleRepresentation(getType());
		}
	}

	public class GenericBound extends PropertyChangedSupportDefaultImplementation {
		private FMLRTType type;

		public GenericBound(FMLRTType type) {
			super();
			this.type = type;
		}

		public GenericBound() {
			this(null);
		}

		public void delete() {
			this.type = null;
		}

		public FMLRTType getType() {
			return type;
		}

		public void setType(FMLRTType type) {

			if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
				Type oldValue = this.type;
				this.type = type;

				makeWildcardType();

				getPropertyChangeSupport().firePropertyChange("type", oldValue, type);
			}
		}

		@Override
		public String toString() {
			return TypeUtils.fullQualifiedRepresentation(type);
		}

		public ImageIcon getIcon() {
			return isValid() ? UtilsIconLibrary.OK_ICON : UtilsIconLibrary.ERROR_ICON;
		}

		public boolean isValid() {
			return true;
		}

		public String getTypeStringRepresentation() {
			return TypeUtils.simpleRepresentation(getType());
		}
	}

}
