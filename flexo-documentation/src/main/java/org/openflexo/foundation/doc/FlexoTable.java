/*
 * (c) Copyright 2013 Openflexo
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

package org.openflexo.foundation.doc;

import java.util.List;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * Generic abstract concept representing a table of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoTable<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocumentElement<D, TA> {

	@PropertyIdentifier(type = FlexoTableRow.class, cardinality = Cardinality.LIST)
	public static final String TABLE_ROWS_KEY = "tableRows";

	/**
	 * Return the list of rows of this table
	 * 
	 * @return
	 */
	@Getter(value = TABLE_ROWS_KEY, cardinality = Cardinality.LIST, inverse = FlexoTableRow.TABLE_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoTableRow<D, TA>> getTableRows();

	@Setter(TABLE_ROWS_KEY)
	public void setTableRows(List<FlexoTableRow<D, TA>> someTableRows);

	/**
	 * Add table row to this {@link FlexoTable} (public API).<br>
	 * Element will be added to underlying technology-specific model and {@link FlexoTable} will be updated accordingly
	 */
	@Adder(TABLE_ROWS_KEY)
	@PastingPoint
	public void addToTableRows(FlexoTableRow<D, TA> aTableRow);

	/**
	 * Remove table row from this {@link FlexoTable} (public API).<br>
	 * Element will be removed from underlying technology-specific model and {@link FlexoTable} will be updated accordingly
	 */
	@Remover(TABLE_ROWS_KEY)
	public void removeFromTableRows(FlexoTableRow<D, TA> aTableRow);

	/**
	 * Insert table row to this {@link FlexoTable} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific model and {@link FlexoTable} will be updated accordingly
	 */
	public void insertTableRowAtIndex(FlexoTableRow<D, TA> anElement, int index);

	/**
	 * Move table row in this {@link FlexoTable} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific model and {@link FlexoTable} will be updated accordingly
	 */
	public void moveTableRowToIndex(FlexoTableRow<D, TA> anElement, int index);

	/**
	 * Return element identified by identifier, asserting that this element exists in the table (eg a paragraph in a cell), or null if no
	 * such element exists
	 */
	public FlexoDocumentElement<D, TA> getElementWithIdentifier(String identifier);

	/**
	 * Return cell at supplied row and column, if that position is valid. Otherwise null is returned.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public FlexoTableCell<D, TA> getCell(int row, int col);

	public static abstract class FlexoTableImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocumentElementImpl<D, TA>implements FlexoTable<D, TA> {

		/**
		 * Return cell at supplied row and column, if that position is valid. Otherwise null is returned.
		 * 
		 * @param row
		 * @param col
		 * @return
		 */
		@Override
		public FlexoTableCell<D, TA> getCell(int row, int col) {
			if (row < getTableRows().size() && col < getTableRows().get(row).getTableCells().size()) {
				return getTableRows().get(row).getTableCells().get(col);
			}
			return null;
		}

		@Override
		public String toString() {
			return "Table(" + getIdentifier() + ") ";
		}
	}

}
