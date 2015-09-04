/*
 * (c) Copyright 2013- Openflexo
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

package org.openflexo.foundation.doc.fml;

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDocumentFragment;
import org.openflexo.foundation.doc.FlexoTable;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoTableRole.FlexoTableRoleImpl.class)
public interface FlexoTableRole<T extends FlexoTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
		FlexoRole<T> {

	@PropertyIdentifier(type = FlexoDocumentFragment.class)
	public static final String TABLE_KEY = "table";
	@PropertyIdentifier(type = String.class)
	public static final String TABLE_ID_KEY = "tableId";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_ITERATION_INDEX_KEY = "startIterationIndex";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_ITERATION_INDEX_KEY = "endIterationIndex";
	@PropertyIdentifier(type = ColumnTableBinding.class, cardinality = Cardinality.LIST)
	public static final String COLUMN_BINDINGS_KEY = "columnBindings";

	/**
	 * Return the template document
	 * 
	 * @return
	 */
	public FlexoDocument<?, ?> getDocument();

	/**
	 * Return the represented table in the template document resource<br>
	 * Note that is not the table that is to be managed at run-time
	 * 
	 * @return
	 */
	@Getter(value = TABLE_KEY, isStringConvertable = true)
	public T getTable();

	/**
	 * Sets the represented table in the template resource<br>
	 * 
	 * @param fragment
	 */
	@Setter(TABLE_KEY)
	public void setTable(T table);

	/**
	 * Return the represented table id in the template document resource<br>
	 */
	@Getter(TABLE_ID_KEY)
	@XMLAttribute
	public String getTableId();

	@Setter(TABLE_ID_KEY)
	public void setTableId(String tableId);

	/**
	 * Return a {@link DataBinding} representing access to the list of objects on which to iterate
	 * 
	 * @return
	 */
	@Getter(ITERATION_KEY)
	@XMLAttribute
	public DataBinding<List> getIteration();

	/**
	 * Sets {@link DataBinding} representing access to the list of objects on which to iterate
	 * 
	 * @param value
	 */
	@Setter(ITERATION_KEY)
	public void setIteration(DataBinding<List> value);

	@Getter(value = START_ITERATION_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getStartIterationIndex();

	@Setter(START_ITERATION_INDEX_KEY)
	public void setStartIterationIndex(int startIterationIndex);

	@Getter(value = END_ITERATION_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getEndIterationIndex();

	@Setter(END_ITERATION_INDEX_KEY)
	public void setEndIterationIndex(int endIterationIndex);

	@Getter(value = COLUMN_BINDINGS_KEY, cardinality = Cardinality.LIST, inverse = ColumnTableBinding.TABLE_ROLE_KEY)
	@XMLElement
	public List<ColumnTableBinding<D, TA>> getColumnBindings();

	@Setter(COLUMN_BINDINGS_KEY)
	public void setColumnBindings(List<ColumnTableBinding<D, TA>> someColumnBindings);

	@Adder(COLUMN_BINDINGS_KEY)
	public void addToColumnBindings(ColumnTableBinding<D, TA> aColumnBinding);

	@Remover(COLUMN_BINDINGS_KEY)
	public void removeFromColumnBindings(ColumnTableBinding<D, TA> aColumnBinding);

	public BindingModel getTableBindingModel();

	public static abstract class FlexoTableRoleImpl<T extends FlexoTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoRoleImpl<T> implements FlexoTableRole<T, D, TA> {

		private BindingModel tableBindingModel;
		public static final String ITERATOR_NAME = "iterator";

		private T table;
		private String tableId;

		@Override
		public String getTableId() {
			if (getTable() != null) {
				return getTable().getIdentifier();
			}
			return tableId;
		}

		@Override
		public void setTableId(String tableId) {
			if ((tableId == null && this.tableId != null) || (tableId != null && !tableId.equals(this.tableId))) {
				String oldValue = getTableId();
				this.tableId = tableId;
				this.table = null;
				getPropertyChangeSupport().firePropertyChange(TABLE_ID_KEY, oldValue, tableId);
				getPropertyChangeSupport().firePropertyChange(TABLE_KEY, null, table);
			}
		}

		@Override
		public T getTable() {
			if (table == null && StringUtils.isNotEmpty(tableId)) {
				table = (T) getDocument().getElementWithIdentifier(tableId);
			}
			return table;
		}

		@Override
		public void setTable(T table) {
			T oldValue = this.table;
			if (table != oldValue) {
				this.table = table;
				getPropertyChangeSupport().firePropertyChange(TABLE_KEY, oldValue, table);
				getPropertyChangeSupport().firePropertyChange(TABLE_ID_KEY, null, getTableId());
			}
		}

		@Override
		public FlexoDocument<?, ?> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot) {
				return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

		private DataBinding<List> iteration;

		@Override
		public DataBinding<List> getIteration() {
			if (iteration == null) {
				iteration = new DataBinding<List>(this, List.class, DataBinding.BindingDefinitionType.GET);
				iteration.setBindingName("Iteration");
				iteration.setMandatory(false);
			}
			return iteration;
		}

		@Override
		public void setIteration(DataBinding<List> iteration) {
			if (iteration != null) {
				iteration.setOwner(this);
				iteration.setDeclaredType(List.class);
				iteration.setBindingName("Iteration");
				iteration.setMandatory(true);
				iteration.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.iteration = iteration;
			notifiedBindingChanged(getIteration());
			if (tableBindingModel != null) {
				BindingVariable iteratorVariable = tableBindingModel.bindingVariableNamed(ITERATOR_NAME);
				iteratorVariable.setType(getIteratorType());
			}
		}

		public Type getIteratorType() {
			if (getIteration() != null && getIteration().isSet() && getIteration().isValid()) {
				return getIteration().getAnalyzedType();
			}
			return Object.class;
		}

		@Override
		public BindingModel getTableBindingModel() {
			if (tableBindingModel == null) {
				createTableBindingModel();
			}
			return tableBindingModel;
		}

		private void createTableBindingModel() {
			tableBindingModel = new BindingModel(getBindingModel());

			BindingVariable iteratorVariable = new BindingVariable(ITERATOR_NAME, getIteratorType());
			iteratorVariable.setCacheable(false);

			tableBindingModel.addToBindingVariables(iteratorVariable);
			// System.out.println("dataClass="+getDataClass()+" dataClassName="+dataClassName);

			// logger.info("******** Table: "+getName()+" Add BindingVariable: iterator type="+getIteratorClass());
		}

	}
}
