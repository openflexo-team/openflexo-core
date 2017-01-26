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
 * Generic abstract concept representing a table row in a table of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocTableRow<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocObject<D, TA> {

	@PropertyIdentifier(type = FlexoDocTable.class)
	public static final String TABLE_KEY = "table";

	@PropertyIdentifier(type = FlexoDocTableCell.class, cardinality = Cardinality.LIST)
	public static final String TABLE_CELLS_KEY = "tableCells";

	@Getter(TABLE_KEY)
	public FlexoDocTable<D, TA> getTable();

	@Setter(TABLE_KEY)
	public void setTable(FlexoDocTable<D, TA> table);

	/**
	 * Return the list of cells of this row
	 * 
	 * @return
	 */
	@Getter(value = TABLE_CELLS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocTableCell.ROW_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocTableCell<D, TA>> getTableCells();

	@Setter(TABLE_CELLS_KEY)
	public void setTableCells(List<FlexoDocTableCell<D, TA>> someTableCells);

	/**
	 * Add table cell to this {@link FlexoDocTableRow} (public API).<br>
	 * Element will be added to underlying technology-specific model and {@link FlexoDocTableRow} will be updated accordingly
	 */
	@Adder(TABLE_CELLS_KEY)
	@PastingPoint
	public void addToTableCells(FlexoDocTableCell<D, TA> aTableCell);

	/**
	 * Remove table cell from this {@link FlexoDocTableRow} (public API).<br>
	 * Element will be removed from underlying technology-specific model and {@link FlexoDocTableRow} will be updated accordingly
	 */
	@Remover(TABLE_CELLS_KEY)
	public void removeFromTableCells(FlexoDocTableCell<D, TA> aTableCell);

	/**
	 * Insert table cell to this {@link FlexoDocTableRow} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific model and {@link FlexoDocTableRow} will be updated accordingly
	 */
	public void insertTableCellAtIndex(FlexoDocTableCell<D, TA> anElement, int index);

	/**
	 * Move table cell in this {@link FlexoDocTableRow} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific model and {@link FlexoDocTableRow} will be updated accordingly
	 */
	public void moveTableCellToIndex(FlexoDocTableCell<D, TA> anElement, int index);

	/**
	 * Return index of the row<br>
	 * Index of a row is the row occurence in the table
	 * 
	 * @return
	 */
	public int getIndex();

	/**
	 * Return identifier of the row
	 * 
	 * @return
	 */
	public String getIdentifier();

	/**
	 * Return element identified by identifier, asserting that this element exists in the row (eg a paragraph in a cell), or null if no such
	 * element exists
	 */
	public FlexoDocElement<D, TA> getElementWithIdentifier(String identifier);

	public static abstract class FlexoTableRowImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocObjectImpl<D, TA> implements FlexoDocTableRow<D, TA> {

		@Override
		public int getIndex() {
			if (getTable() != null) {
				return getTable().getTableRows().indexOf(this);
			}
			return -1;
		}

		@Override
		public String toString() {
			return "Row" + getIndex();
		}

	}

}
