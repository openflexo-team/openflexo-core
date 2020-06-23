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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.toolbox.StringUtils;

@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoTableRole.FlexoTableRoleImpl.class)
public interface FlexoTableRole<T extends FlexoDocTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoRole<T> {

	@PropertyIdentifier(type = FlexoDocFragment.class)
	public static final String TABLE_KEY = "table";
	@PropertyIdentifier(type = String.class)
	public static final String TABLE_ID_KEY = "tableId";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_ITERATION_INDEX_KEY = "startIterationIndex";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_ITERATION_INDEX_KEY = "endIterationIndex";
	@PropertyIdentifier(type = DataOrientation.class)
	public static final String DATA_ORIENTATION_KEY = "dataOrientation";
	@PropertyIdentifier(type = ColumnTableBinding.class, cardinality = Cardinality.LIST)
	public static final String COLUMN_BINDINGS_KEY = "columnBindings";

	public static enum DataOrientation {
		Vertical, Horizontal
	}

	/**
	 * Return the template document
	 * 
	 * @return
	 */
	public FlexoDocument<D, TA> getDocument();

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

	@Getter(value = DATA_ORIENTATION_KEY)
	@XMLAttribute
	public DataOrientation getDataOrientation();

	@Setter(DATA_ORIENTATION_KEY)
	public void setDataOrientation(DataOrientation dataOrientation);

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

	public static abstract class FlexoTableRoleImpl<T extends FlexoDocTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
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
			if (table == null && StringUtils.isNotEmpty(tableId) && getDocument() != null) {
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
		public DataOrientation getDataOrientation() {
			DataOrientation returned = (DataOrientation) performSuperGetter(DATA_ORIENTATION_KEY);
			if (returned == null) {
				return DataOrientation.Vertical;
			}
			return returned;
		}

		@Override
		public FlexoDocument<D, TA> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot) {
				if (((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource() != null) {
					return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
				}
			}
			return null;
		}

		private DataBinding<List> iteration;

		@Override
		public DataBinding<List> getIteration() {
			if (iteration == null) {
				iteration = new DataBinding<>(this, List.class, DataBinding.BindingDefinitionType.GET);
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
				Type iterationType = getIteration().getAnalyzedType();
				if (iterationType instanceof ParameterizedType) {
					return ((ParameterizedType) iterationType).getActualTypeArguments()[0];
				}
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

			BindingVariable iteratorVariable = new BindingVariable(ITERATOR_NAME, getIteratorType()) {
				@Override
				public Type getType() {
					return getIteratorType();
				}
			};
			iteratorVariable.setCacheable(false);

			tableBindingModel.addToBindingVariables(iteratorVariable);
			// System.out.println("dataClass="+getDataClass()+" dataClassName="+dataClassName);

			// logger.info("******** Table: "+getName()+" Add BindingVariable: iterator type="+getIteratorClass());
		}

		@Override
		public TableActorReference<T> makeActorReference(T table, FlexoConceptInstance fci) {

			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			TableActorReference<T> returned = factory.newInstance(TableActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(table);
			return returned;

		}

	}
}
