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
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

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
public interface FlexoTableRow<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

	@PropertyIdentifier(type = FlexoTable.class)
	public static final String TABLE_KEY = "table";
	@PropertyIdentifier(type = String.class)
	public static final String TEXT_KEY = "text";

	@Getter(TABLE_KEY)
	public FlexoTable<D, TA> getTable();

	@Setter(TABLE_KEY)
	public void setTable(FlexoTable<D, TA> table);

	/**
	 * Return index of the row<br>
	 * Index of a run is the row occurence in the table
	 * 
	 * @return
	 */
	public int getIndex();

	public static abstract class FlexoTableRowImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoTableRow<D, TA> {

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
