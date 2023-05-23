/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.inspector;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.binding.FlexoConceptFormatterBindingModel;
import org.openflexo.foundation.fml.binding.FlexoConceptInspectorBindingModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Represents inspector associated with an Edition Pattern
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoConceptInspector.FlexoConceptInspectorImpl.class)
@XMLElement(xmlTag = "Inspector")
public interface FlexoConceptInspector extends FlexoConceptObject {

	public static final String FORMATTER_INSTANCE_PROPERTY = "instance";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexo_concept";
	@PropertyIdentifier(type = String.class)
	public static final String INSPECTOR_TITLE_KEY = "inspectorTitle";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RENDERER_KEY = "renderer";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DELEGATE_CONCEPT_INSTANCE_KEY = "delegateConceptInstance";
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

	@Getter(value = DELEGATE_CONCEPT_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getDelegateConceptInstance();

	@Setter(DELEGATE_CONCEPT_INSTANCE_KEY)
	public void setDelegateConceptInstance(DataBinding<FlexoConceptInstance> delegateConceptInstance);

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

	@Reindexer(ENTRIES_KEY)
	public void moveInspectorEntryToIndex(InspectorEntry entry, int index);

	@Finder(collection = ENTRIES_KEY, attribute = InspectorEntry.NAME_KEY)
	public InspectorEntry getEntry(String name);

	public String getAvailableEntryName(String baseName);

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
		// private Vector<InspectorEntry> entries;
		private DataBinding<String> renderer;
		private DataBinding<FlexoConceptInstance> delegateConceptInstance;

		private final FlexoConceptFormatter formatter;

		private FlexoConceptInspectorBindingModel bindingModel;

		public FlexoConceptInspectorImpl() {
			super();
			// entries = new Vector<InspectorEntry>();
			formatter = new FlexoConceptFormatterImpl();
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
		public String getInspectorTitle() {
			return inspectorTitle;
		}

		@Override
		public void setInspectorTitle(String inspectorTitle) {
			this.inspectorTitle = inspectorTitle;
		}

		@Override
		public String getAvailableEntryName(String baseName) {
			String testName = baseName;
			int index = 2;
			while (getEntry(testName) != null) {
				testName = baseName + index;
				index++;
			}
			return testName;
		}

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
			List<InspectorEntry> entries = getEntries();
			entries.remove(p);
			entries.add(0, p);
			setChanged();
			notifyObservers();
			notifyChange(ENTRIES_KEY, null, entries);
		}

		@Override
		public void entryUp(InspectorEntry p) {
			List<InspectorEntry> entries = getEntries();
			int index = entries.indexOf(p);
			if (index > 0) {
				entries.remove(p);
				entries.add(index - 1, p);
				setChanged();
				notifyObservers();
				notifyChange(ENTRIES_KEY, null, entries);
			}
		}

		@Override
		public void entryDown(InspectorEntry p) {
			List<InspectorEntry> entries = getEntries();
			int index = entries.indexOf(p);
			if (index > -1) {
				entries.remove(p);
				entries.add(index + 1, p);
				setChanged();
				notifyObservers();
				notifyChange(ENTRIES_KEY, null, entries);
			}
		}

		@Override
		public void entryLast(InspectorEntry p) {
			List<InspectorEntry> entries = getEntries();
			entries.remove(p);
			entries.add(p);
			setChanged();
			notifyObservers();
			notifyChange(ENTRIES_KEY, null, entries);
		}

		@Override
		public DataBinding<String> getRenderer() {
			if (renderer == null) {
				renderer = new DataBinding<>(formatter, String.class, BindingDefinitionType.GET);
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

		@Override
		public DataBinding<FlexoConceptInstance> getDelegateConceptInstance() {
			if (delegateConceptInstance == null) {
				delegateConceptInstance = new DataBinding<>(this, FlexoConceptInstance.class, BindingDefinitionType.GET);
				delegateConceptInstance.setBindingName("delegateConceptInstance");
			}
			return delegateConceptInstance;
		}

		@Override
		public void setDelegateConceptInstance(DataBinding<FlexoConceptInstance> aDelegateConceptInstance) {
			if (aDelegateConceptInstance != null) {
				aDelegateConceptInstance.setOwner(this);
				aDelegateConceptInstance.setDeclaredType(FlexoConceptInstance.class);
				aDelegateConceptInstance.setBindingDefinitionType(BindingDefinitionType.GET);
				aDelegateConceptInstance.setBindingName("delegateConceptInstance");
			}
			this.delegateConceptInstance = aDelegateConceptInstance;
			notifiedBindingChanged(this.delegateConceptInstance);
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

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getRenderer().rebuild();
			getDelegateConceptInstance().rebuild();
		}

	}

	public interface FlexoConceptFormatter extends Bindable {

		@Override
		public FlexoConceptFormatterBindingModel getBindingModel();

	}

}
