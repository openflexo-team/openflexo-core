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
 * Generic abstract concept representing a style of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocStyle<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String STYLE_ID_KEY = "styleId";
	@PropertyIdentifier(type = Integer.class)
	public static final String LEVEL_KEY = "level";

	/**
	 * Return name of the {@link FlexoDocStyle} in the {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(value = NAME_KEY)
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	/**
	 * Return identifier of the {@link FlexoDocStyle} in the {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(value = STYLE_ID_KEY)
	public String getStyleId();

	@Setter(STYLE_ID_KEY)
	public void setStyleId(String styleId);

	/**
	 * Return level of this style, when trying to interpret the owner document as a structured document.<br>
	 * This level is computed from the list of structuring styles as defined in {@link FlexoDocument} (see #
	 * 
	 * If this style is not structuring (not present in document's structuring style), return null
	 * 
	 * @return
	 */
	@Getter(value = LEVEL_KEY)
	public Integer getLevel();

	@Setter(LEVEL_KEY)
	public void setLevel(Integer level);

	/**
	 * Return flag indicating if this style is levelled or not
	 * 
	 * @return
	 */
	public boolean isLevelled();

	public static abstract class FlexoStyleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoDocStyle<D, TA> {

		@Override
		public boolean isLevelled() {
			if (getFlexoDocument() != null) {
				return getFlexoDocument().getStructuringStyles().contains(this);
			}
			return false;
		}

		@Override
		public Integer getLevel() {
			if (getFlexoDocument() != null) {
				return getFlexoDocument().getStructuringStyles().indexOf(this);
			}
			return null;
		}

		@Override
		public void setLevel(Integer level) {
			// TODO Auto-generated method stub
		}
	}

}
