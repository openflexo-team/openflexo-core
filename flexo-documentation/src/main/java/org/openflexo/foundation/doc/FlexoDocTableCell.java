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

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Generic abstract concept representing a table cell in a table of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocTableCell<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoDocObject<D, TA>, FlexoDocElementContainer<D, TA> {

	@PropertyIdentifier(type = FlexoDocTableRow.class)
	public static final String ROW_KEY = "row";
	// @PropertyIdentifier(type = FlexoDocParagraph.class, cardinality = Cardinality.LIST)
	// public static final String PARAGRAPHS_KEY = "paragraphs";
	@PropertyIdentifier(type = Integer.class)
	public static final String COL_SPAN_KEY = "colSpan";
	@PropertyIdentifier(type = Integer.class)
	public static final String ROW_SPAN_KEY = "rowSpan";

	@Getter(ROW_KEY)
	public FlexoDocTableRow<D, TA> getRow();

	@Setter(ROW_KEY)
	public void setRow(FlexoDocTableRow<D, TA> table);

	/**
	 * Return the list of paragraphs in this cell
	 * 
	 * @return
	 */
	/*@Getter(value = PARAGRAPHS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocParagraph.CONTAINER_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocParagraph<D, TA>> getParagraphs();*/

	// @Setter(PARAGRAPHS_KEY)
	// public void setParagraphs(List<FlexoDocParagraph<D, TA>> someParagraphs);

	/**
	 * Add paragraph to this {@link FlexoDocTableCell} (public API).<br>
	 * Paragraph will be added to underlying technology-specific document model and {@link FlexoDocTableCell} will be updated accordingly
	 */
	/*@Adder(PARAGRAPHS_KEY)
	@PastingPoint
	public void addToParagraphs(FlexoDocParagraph<D, TA> anParagraph);
	*/

	/**
	 * Remove paragraph from this {@link FlexoDocTableCell} (public API).<br>
	 * Paragraph will be removed to underlying technology-specific document model and {@link FlexoDocTableCell} will be updated accordingly
	 */
	// @Remover(PARAGRAPHS_KEY)
	// public void removeFromParagraphs(FlexoDocParagraph<D, TA> anParagraph);

	/**
	 * Insert paragraph to this {@link FlexoDocTableCell} at supplied index (public API).<br>
	 * Paragraph will be inserted to underlying technology-specific document model and {@link FlexoDocTableCell} will be updated accordingly
	 */
	// public void insertParagraphAtIndex(FlexoDocParagraph<D, TA> anParagraph, int index);

	/**
	 * Moved paragraph to this {@link FlexoDocTableCell} at supplied index (public API).<br>
	 * Paragraph will be moved inside underlying technology-specific document model and {@link FlexoDocTableCell} will be updated
	 * accordingly
	 */
	// public void moveParagraphToIndex(FlexoDocParagraph<D, TA> anParagraph, int index);

	/**
	 * Return paragraph identified by identifier, or null if no such paragraph exists
	 */
	// public FlexoDocParagraph<D, TA> getParagraphWithIdentifier(String identifier);

	/**
	 * Return index of the cell<br>
	 * Index of a run is the cell occurence in the row
	 * 
	 * @return
	 */
	public int getIndex();

	/**
	 * Return identifier of the cell
	 * 
	 * @return
	 */
	public String getIdentifier();

	@Getter(value = COL_SPAN_KEY, defaultValue = "1")
	public int getColSpan();

	@Setter(COL_SPAN_KEY)
	public void setColSpan(int colSpan);

	@Getter(value = ROW_SPAN_KEY, defaultValue = "1")
	public int getRowSpan();

	@Setter(ROW_SPAN_KEY)
	public void setRowSpan(int rowSpan);

	/**
	 * Return a string representation (plain text) of contents of the cell
	 * 
	 * @return
	 */
	public String getRawText();

	/**
	 * Sets contents of the cell by erasing actual structure, and replacing it by a some paragraphs reflecting supplied text
	 * 
	 * @return
	 */
	public void setRawText(String someText);

	/**
	 * Return element identified by identifier, asserting that this element exists in the table (eg a paragraph in a cell), or null if no
	 * such element exists
	 */
	@Override
	public FlexoDocElement<D, TA> getElementWithIdentifier(String identifier);

	public static abstract class FlexoTableCellImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoDocObjectImpl<D, TA> implements FlexoDocTableCell<D, TA> {

		@Override
		public int getIndex() {
			if (getRow() != null) {
				return getRow().getTableCells().indexOf(this);
			}
			return -1;
		}

		@Override
		public String toString() {
			return "Cell" + getIndex();
		}

	}

}
