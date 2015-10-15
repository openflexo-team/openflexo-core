/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.doc.fml;

import java.util.logging.Logger;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlotObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * This class represent a column (or a row in inversed layout) in a {@link FlexoTableRole} when an iteration was set
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ColumnTableBinding.ColumnTableBindingImpl.class)
@XMLElement
public interface ColumnTableBinding<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends ModelSlotObject<D>, FlexoConceptObject {

	@PropertyIdentifier(type = String.class)
	public static final String COLUMN_NAME_KEY = "columnName";
	@PropertyIdentifier(type = Integer.class)
	public static final String COLUMN_INDEX_KEY = "columnIndex";

	/**
	 * Return index of column in template table
	 * 
	 * @return
	 */
	@Getter(value = COLUMN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getColumnIndex();

	@Setter(COLUMN_INDEX_KEY)
	public void setColumnIndex(int index);

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@PropertyIdentifier(type = FlexoTableRole.class)
	public static final String TABLE_ROLE_KEY = "tableRole";

	@Getter(COLUMN_NAME_KEY)
	@XMLAttribute
	public String getColumnName();

	@Setter(COLUMN_NAME_KEY)
	public void setColumnName(String columnName);

	@Getter(VALUE_KEY)
	@XMLAttribute
	public DataBinding<String> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<String> value);

	@Getter(TABLE_ROLE_KEY)
	public FlexoTableRole<?, D, TA> getTableRole();

	@Setter(TABLE_ROLE_KEY)
	public void setTableRole(FlexoTableRole<?, D, TA> tableRole);

	public static abstract class ColumnTableBindingImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoConceptObjectImpl implements ColumnTableBinding<D, TA> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ColumnTableBinding.class.getPackage().getName());

		private DataBinding<String> value;

		@Override
		public String getURI() {
			return null;
		}

		@Override
		public DataBinding<String> getValue() {
			if (value == null) {
				value = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				value.setBindingName("ColumnValue" + getColumnIndex());
				value.setMandatory(true);
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<String> value) {
			if (value != null) {
				value.setOwner(this);
				value.setDeclaredType(String.class);
				value.setBindingName("ColumnValue" + getColumnIndex());
				value.setMandatory(true);
				value.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.value = value;
			notifiedBindingChanged(getValue());
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return getTableRole() != null ? getTableRole().getFlexoConcept() : null;
		}

		@Override
		public BindingFactory getBindingFactory() {
			return getFlexoConcept().getInspector().getBindingFactory();
		}

		@Override
		public BindingModel getBindingModel() {
			if (getTableRole() != null) {
				return getTableRole().getTableBindingModel();
			}
			return null;
		}

		@Override
		public ModelSlot<D> getModelSlot() {
			if (getTableRole() != null) {
				return (ModelSlot) getTableRole().getModelSlot();
			}
			return null;
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getModelSlot() != null) {
				return getModelSlot().getModelSlotTechnologyAdapter();
			}
			return null;
		}

		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getVirtualModel();
			}
			return null;
		}

	}
}
