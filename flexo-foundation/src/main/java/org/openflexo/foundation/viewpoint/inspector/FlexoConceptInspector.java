/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.viewpoint.inspector;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.binding.DefaultBindable;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptObject;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.binding.FlexoConceptFormatterBindingModel;
import org.openflexo.foundation.viewpoint.binding.FlexoConceptInspectorBindingModel;
import org.openflexo.foundation.viewpoint.dm.InspectorEntryInserted;
import org.openflexo.foundation.viewpoint.dm.InspectorEntryRemoved;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Represents inspector associated with an Edition Pattern
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoConceptInspector.FlexoConceptInspectorImpl.class)
@XMLElement(xmlTag = "Inspector")
public interface FlexoConceptInspector extends FlexoConceptObject, Bindable {

	public static final String FORMATTER_INSTANCE_PROPERTY = "instance";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexo_concept";
	@PropertyIdentifier(type = String.class)
	public static final String INSPECTOR_TITLE_KEY = "inspectorTitle";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RENDERER_KEY = "renderer";
	@PropertyIdentifier(type = Vector.class)
	public static final String ENTRIES_KEY = "entries";

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.INSPECTOR_KEY)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = INSPECTOR_TITLE_KEY)
	@XMLAttribute
	public String getInspectorTitle();

	@Setter(INSPECTOR_TITLE_KEY)
	public void setInspectorTitle(String inspectorTitle);

	@Getter(value = RENDERER_KEY)
	@XMLAttribute
	public DataBinding<String> getRenderer();

	@Setter(RENDERER_KEY)
	public void setRenderer(DataBinding<String> renderer);

	@Getter(value = ENTRIES_KEY, cardinality = Cardinality.LIST, inverse = InspectorEntry.INSPECTOR_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<InspectorEntry> getEntries();

	@Setter(ENTRIES_KEY)
	public void setEntries(List<InspectorEntry> entries);

	@Adder(ENTRIES_KEY)
	public void addToEntries(InspectorEntry aEntrie);

	@Remover(ENTRIES_KEY)
	public void removeFromEntries(InspectorEntry aEntrie);

	public TextFieldInspectorEntry createNewTextField();

	public TextAreaInspectorEntry createNewTextArea();

	public IntegerInspectorEntry createNewInteger();

	public CheckboxInspectorEntry createNewCheckbox();

	public IndividualInspectorEntry createNewIndividual();

	public ClassInspectorEntry createNewClass();

	public PropertyInspectorEntry createNewProperty();

	public ObjectPropertyInspectorEntry createNewObjectProperty();

	public DataPropertyInspectorEntry createNewDataProperty();

	public InspectorEntry deleteEntry(InspectorEntry entry);

	public void entryFirst(InspectorEntry p);

	public void entryUp(InspectorEntry p);

	public void entryDown(InspectorEntry p);

	public void entryLast(InspectorEntry p);

	public FlexoConceptFormatter getFormatter();

	@Override
	public FlexoConceptInspectorBindingModel getBindingModel();

	public static abstract class FlexoConceptInspectorImpl extends FlexoConceptObjectImpl implements FlexoConceptInspector {

		private static final Logger logger = FlexoLogger.getLogger(FlexoConceptInspector.class.getPackage().toString());

		private String inspectorTitle;
		private FlexoConcept _flexoConcept;
		private Vector<InspectorEntry> entries;
		private DataBinding<String> renderer;

		private final FlexoConceptFormatter formatter;

		private FlexoConceptInspectorBindingModel bindingModel;

		public FlexoConceptInspectorImpl() {
			super();
			entries = new Vector<InspectorEntry>();
			formatter = new FlexoConceptFormatterImpl();
		}

		@Override
		public String getURI() {
			return null;
		}

		@Override
		public FlexoConceptFormatter getFormatter() {
			return formatter;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return _flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			if (_flexoConcept != flexoConcept) {
				FlexoConcept old = _flexoConcept;
				_flexoConcept = flexoConcept;
				getPropertyChangeSupport().firePropertyChange(FLEXO_CONCEPT_KEY, old, flexoConcept);
			}
		}

		@Override
		public VirtualModel getVirtualModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getVirtualModel();
			}
			return null;
		}

		@Override
		public String getInspectorTitle() {
			return inspectorTitle;
		}

		@Override
		public void setInspectorTitle(String inspectorTitle) {
			this.inspectorTitle = inspectorTitle;
		}

		@Override
		public Vector<InspectorEntry> getEntries() {
			return entries;
		}

		public void setEntries(Vector<InspectorEntry> someEntries) {
			entries = someEntries;
		}

		@Override
		public void addToEntries(InspectorEntry anEntry) {
			anEntry.setInspector(this);
			entries.add(anEntry);
			setChanged();
			notifyObservers(new InspectorEntryInserted(anEntry, this));
		}

		@Override
		public void removeFromEntries(InspectorEntry anEntry) {
			anEntry.setInspector(null);
			entries.remove(anEntry);
			setChanged();
			notifyObservers(new InspectorEntryRemoved(anEntry, this));
		}

		@Override
		public TextFieldInspectorEntry createNewTextField() {
			TextFieldInspectorEntry newEntry = getVirtualModelFactory().newTextFieldInspectorEntry();
			newEntry.setName("textfield");
			// newEntry.setLabel("textfield");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public TextAreaInspectorEntry createNewTextArea() {
			TextAreaInspectorEntry newEntry = getVirtualModelFactory().newTextAreaInspectorEntry();
			newEntry.setName("textarea");
			// newEntry.setLabel("textarea");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public IntegerInspectorEntry createNewInteger() {
			IntegerInspectorEntry newEntry = getVirtualModelFactory().newIntegerInspectorEntry();
			newEntry.setName("integer");
			// newEntry.setLabel("integer");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public CheckboxInspectorEntry createNewCheckbox() {
			CheckboxInspectorEntry newEntry = getVirtualModelFactory().newCheckboxInspectorEntry();
			newEntry.setName("checkbox");
			// newEntry.setLabel("checkbox");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public IndividualInspectorEntry createNewIndividual() {
			IndividualInspectorEntry newEntry = getVirtualModelFactory().newIndividualInspectorEntry();
			newEntry.setName("individual");
			// newEntry.setLabel("individual");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public ClassInspectorEntry createNewClass() {
			ClassInspectorEntry newEntry = getVirtualModelFactory().newClassInspectorEntry();
			newEntry.setName("class");
			// newEntry.setLabel("class");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public PropertyInspectorEntry createNewProperty() {
			PropertyInspectorEntry newEntry = getVirtualModelFactory().newPropertyInspectorEntry();
			newEntry.setName("property");
			// newEntry.setLabel("class");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public ObjectPropertyInspectorEntry createNewObjectProperty() {
			ObjectPropertyInspectorEntry newEntry = getVirtualModelFactory().newObjectPropertyInspectorEntry();
			newEntry.setName("property");
			// newEntry.setLabel("class");
			addToEntries(newEntry);
			return newEntry;
		}

		@Override
		public DataPropertyInspectorEntry createNewDataProperty() {
			DataPropertyInspectorEntry newEntry = getVirtualModelFactory().newDataPropertyInspectorEntry();
			newEntry.setName("property");
			// newEntry.setLabel("class");
			addToEntries(newEntry);
			return newEntry;
		}

		/*
		 * public FlexoObjectInspectorEntry createNewFlexoObject() {
		 * FlexoObjectInspectorEntry newEntry = new FlexoObjectInspectorEntry();
		 * newEntry.setName("flexoObject"); // newEntry.setLabel("flexoObject");
		 * addToEntries(newEntry); return newEntry; }
		 */

		@Override
		public InspectorEntry deleteEntry(InspectorEntry entry) {
			removeFromEntries(entry);
			entry.delete();
			return entry;
		}

		@Override
		public final FlexoConceptInspectorBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new FlexoConceptInspectorBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public void entryFirst(InspectorEntry p) {
			entries.remove(p);
			entries.insertElementAt(p, 0);
			setChanged();
			notifyObservers();
			notifyChange("entries", null, entries);
		}

		@Override
		public void entryUp(InspectorEntry p) {
			int index = entries.indexOf(p);
			if (index > 0) {
				entries.remove(p);
				entries.insertElementAt(p, index - 1);
				setChanged();
				notifyObservers();
				notifyChange("entries", null, entries);
			}
		}

		@Override
		public void entryDown(InspectorEntry p) {
			int index = entries.indexOf(p);
			if (index > -1) {
				entries.remove(p);
				entries.insertElementAt(p, index + 1);
				setChanged();
				notifyObservers();
				notifyChange("entries", null, entries);
			}
		}

		@Override
		public void entryLast(InspectorEntry p) {
			entries.remove(p);
			entries.add(p);
			setChanged();
			notifyObservers();
			notifyChange("entries", null, entries);
		}

		@Override
		public DataBinding<String> getRenderer() {
			if (renderer == null) {
				renderer = new DataBinding<String>(formatter, String.class, BindingDefinitionType.GET);
				renderer.setBindingName("renderer");
			}
			return renderer;
		}

		@Override
		public void setRenderer(DataBinding<String> renderer) {
			if (renderer != null) {
				renderer.setOwner(formatter);
				renderer.setDeclaredType(String.class);
				renderer.setBindingDefinitionType(BindingDefinitionType.GET);
				renderer.setBindingName("renderer");
			}
			this.renderer = renderer;
			notifiedBindingChanged(this.renderer);
		}

		public class FlexoConceptFormatterImpl extends DefaultBindable implements FlexoConceptFormatter {
			private FlexoConceptFormatterBindingModel formatterBindingModel = null;

			@Override
			public BindingFactory getBindingFactory() {
				return FlexoConceptInspectorImpl.this.getBindingFactory();
			}

			@Override
			public FlexoConceptFormatterBindingModel getBindingModel() {
				if (formatterBindingModel == null) {
					formatterBindingModel = new FlexoConceptFormatterBindingModel(FlexoConceptInspectorImpl.this);
				}
				return formatterBindingModel;
			}

			@Override
			public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				if (dataBinding == getRenderer()) {
					FlexoConceptInspectorImpl.this.notifiedBindingChanged(dataBinding);
				}
			}

			@Override
			public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				if (dataBinding == getRenderer()) {
					FlexoConceptInspectorImpl.this.notifiedBindingDecoded(dataBinding);
				}
			}

		}

	}

	public interface FlexoConceptFormatter extends Bindable {

		@Override
		public FlexoConceptFormatterBindingModel getBindingModel();

	}

}
