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
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter.Cardinality;

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
public interface FlexoDocTable<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocElement<D, TA> {

	@PropertyIdentifier(type = FlexoDocTableRow.class, cardinality = Cardinality.LIST)
	public static final String TABLE_ROWS_KEY = "tableRows";

	/**
	 * Return the list of rows of this table
	 * 
	 * @return
	 */
	@Getter(value = TABLE_ROWS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocTableRow.TABLE_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocTableRow<D, TA>> getTableRows();

	@Setter(TABLE_ROWS_KEY)
	public void setTableRows(List<FlexoDocTableRow<D, TA>> someTableRows);

	/**
	 * Add table row to this {@link FlexoDocTable} (public API).<br>
	 * Element will be added to underlying technology-specific model and {@link FlexoDocTable} will be updated accordingly
	 */
	@Adder(TABLE_ROWS_KEY)
	@PastingPoint
	public void addToTableRows(FlexoDocTableRow<D, TA> aTableRow);

	/**
	 * Remove table row from this {@link FlexoDocTable} (public API).<br>
	 * Element will be removed from underlying technology-specific model and {@link FlexoDocTable} will be updated accordingly
	 */
	@Remover(TABLE_ROWS_KEY)
	public void removeFromTableRows(FlexoDocTableRow<D, TA> aTableRow);

	/**
	 * Insert table row to this {@link FlexoDocTable} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific model and {@link FlexoDocTable} will be updated accordingly
	 */
	public void insertTableRowAtIndex(FlexoDocTableRow<D, TA> anElement, int index);

	/**
	 * Move table row in this {@link FlexoDocTable} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific model and {@link FlexoDocTable} will be updated accordingly
	 */
	public void moveTableRowToIndex(FlexoDocTableRow<D, TA> anElement, int index);

	/**
	 * Return element identified by identifier, asserting that this element exists in the table (eg a paragraph in a cell), or null if no
	 * such element exists
	 */
	public FlexoDocElement<D, TA> getElementWithIdentifier(String identifier);

	/**
	 * Return row identified by identifier, or null if no such row exists
	 */
	public FlexoDocTableRow<D, TA> getRowWithIdentifier(String identifier);

	/**
	 * Return cell at supplied row and column, if that position is valid. Otherwise null is returned.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public FlexoDocTableCell<D, TA> getCell(int row, int col);

	public int getColumnWidth(int colIndex);

	public static abstract class FlexoTableImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoDocumentElementImpl<D, TA> implements FlexoDocTable<D, TA> {

		/**
		 * Return cell at supplied row and column, if that position is valid. Otherwise null is returned.
		 * 
		 * @param row
		 * @param col
		 * @return
		 */
		@Override
		public FlexoDocTableCell<D, TA> getCell(int row, int col) {
			if (row < getTableRows().size() && col < getTableRows().get(row).getTableCells().size()) {
				return getTableRows().get(row).getTableCells().get(col);
			}
			return null;
		}

		/**
		 * Return row identified by identifier, or null if no such row exists
		 */
		@Override
		public FlexoDocTableRow<D, TA> getRowWithIdentifier(String identifier) {
			for (FlexoDocTableRow<D, TA> row : getTableRows()) {
				if (row.getIdentifier().equals(identifier)) {
					return row;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return "Table(" + getIdentifier() + ") ";
		}
	}

}
