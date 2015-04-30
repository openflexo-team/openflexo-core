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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.binding.InspectorEntryBindingModel;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents an inspector entry (a data related to an flexo concept which can be inspected)
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(InspectorEntry.InspectorEntryImpl.class)
@Imports({ @Import(CheckboxInspectorEntry.class), @Import(ClassInspectorEntry.class), @Import(FloatInspectorEntry.class),
		@Import(IndividualInspectorEntry.class), @Import(IntegerInspectorEntry.class), @Import(PropertyInspectorEntry.class),
		@Import(TextAreaInspectorEntry.class), @Import(TextFieldInspectorEntry.class), @Import(DataPropertyInspectorEntry.class),
		@Import(ObjectPropertyInspectorEntry.class) })
public abstract interface InspectorEntry extends FlexoConceptObject, Bindable {

	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String INSPECTOR_KEY = "inspector";

	@PropertyIdentifier(type = String.class)
	public static final String LABEL_KEY = "label";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_READ_ONLY_KEY = "isReadOnly";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DATA_KEY = "data";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	@Getter(value = INSPECTOR_KEY/*, inverse = FlexoConceptInspector.ENTRIES_KEY*/)
	@XMLElement(xmlTag = "Inspector")
	public FlexoConceptInspector getInspector();

	@Setter(INSPECTOR_KEY)
	public void setInspector(FlexoConceptInspector inspector);

	@Getter(value = LABEL_KEY)
	@XMLAttribute
	public String getLabel();

	@Setter(LABEL_KEY)
	public void setLabel(String label);

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(value = IS_READ_ONLY_KEY, defaultValue = "false")
	@XMLAttribute(xmlTag = "readOnly")
	public boolean getIsReadOnly();

	@Setter(IS_READ_ONLY_KEY)
	public void setIsReadOnly(boolean isReadOnly);

	@Getter(value = DATA_KEY)
	@XMLAttribute
	public DataBinding<?> getData();

	@Setter(DATA_KEY)
	public void setData(DataBinding<?> data);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	public String getWidgetName();

	public int getIndex();

	@Override
	public InspectorEntryBindingModel getBindingModel();

	public static abstract class InspectorEntryImpl extends FlexoConceptObjectImpl implements InspectorEntry {

		static final Logger logger = Logger.getLogger(InspectorEntry.class.getPackage().getName());

		// private FlexoConceptInspector inspector;
		// private String label;
		// private boolean readOnly;

		private DataBinding<?> data;
		private DataBinding<Boolean> conditional;

		private InspectorEntryBindingModel bindingModel;

		public InspectorEntryImpl() {
			super();
		}

		@Override
		public String getURI() {
			return null;
		}

		public Type getType() {
			return getDefaultDataClass();
		}

		public abstract Class<?> getDefaultDataClass();

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getInspector() != null) {
				return getInspector().getFlexoConcept();
			}
			return null;
		}

		@Override
		public String getLabel() {
			String label = (String) performSuperGetter(LABEL_KEY);
			if (label == null || StringUtils.isEmpty(label)) {
				return getName();
			}
			return label;
		}

		@Override
		public void setLabel(String label) {
			if (label != null && label.equals(getName())) {
				return;
			}
			performSuperSetter(LABEL_KEY, label);
		}

		public boolean isSingleEntry() {
			return true;
		}

		@Override
		public int getIndex() {
			if (getInspector() == null) {
				return -1;
			}
			return getInspector().getEntries().indexOf(this);
		}

		/*@Override
		public boolean getIsReadOnly() {
			return readOnly;
		}*/

		@Override
		public void setIsReadOnly(boolean readOnly) {
			performSuperSetter(IS_READ_ONLY_KEY, readOnly);
			if (data != null) {
				data.setBindingDefinitionType(readOnly ? DataBinding.BindingDefinitionType.GET : DataBinding.BindingDefinitionType.GET_SET);
				notifiedBindingChanged(this.data);
			}
		}

		@Override
		public DataBinding<?> getData() {
			if (data == null) {
				data = new DataBinding<Object>(this, getType(), (getIsReadOnly() ? DataBinding.BindingDefinitionType.GET
						: DataBinding.BindingDefinitionType.GET_SET));
				data.setBindingName("data");
			}
			return data;
		}

		@Override
		public void setData(DataBinding<?> data) {
			if (data != null) {
				data.setOwner(this);
				data.setDeclaredType(getType());
				data.setBindingDefinitionType(getIsReadOnly() ? DataBinding.BindingDefinitionType.GET
						: DataBinding.BindingDefinitionType.GET_SET);
				data.setBindingName("data");
			}
			this.data = data;
			notifiedBindingChanged(this.data);
		}

		@Override
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}

		@Override
		public void setConditional(DataBinding<Boolean> conditional) {
			if (conditional != null) {
				conditional.setOwner(this);
				conditional.setDeclaredType(Boolean.class);
				conditional.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			this.conditional = conditional;
		}

		@Override
		public InspectorEntryBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new InspectorEntryBindingModel(this);
			}
			return bindingModel;
		}

	}

	@DefineValidationRule
	public static class DataBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<InspectorEntry> {
		public DataBindingIsRequiredAndMustBeValid() {
			super("'data'_binding_is_not_valid", InspectorEntry.class);
		}

		@Override
		public DataBinding<?> getBinding(InspectorEntry object) {
			return object.getData();
		}

	}

}
