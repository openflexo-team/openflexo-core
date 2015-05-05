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
public interface FlexoStyle<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends InnerFlexoDocument<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String STYLE_ID_KEY = "styleId";

	/**
	 * Return name of the {@link FlexoStyle} in the {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(value = NAME_KEY)
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	/**
	 * Return identifier of the {@link FlexoStyle} in the {@link FlexoDocument}<br>
	 * 
	 * @return
	 */
	@Getter(value = STYLE_ID_KEY)
	public String getStyleId();

	@Setter(STYLE_ID_KEY)
	public void setStyleId(String styleId);

	public static abstract class FlexoStyleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends InnerFlexoDocumentImpl<D, TA>implements FlexoStyle<D, TA> {
	}

}
