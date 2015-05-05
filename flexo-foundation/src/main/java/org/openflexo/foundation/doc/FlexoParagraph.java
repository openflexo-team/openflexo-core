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
 * Generic abstract concept representing a paragraph of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoParagraph<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocumentElement<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String IDENTIFIER_KEY = "identifier";
	@PropertyIdentifier(type = FlexoStyle.class)
	public static final String STYLE_KEY = "style";

	/**
	 * Return identifier of the {@link FlexoParagraph} in the {@link FlexoDocument}<br>
	 * The identifier is here a {@link String} and MUST be unique regarding the whole {@link FlexoDocument}.<br>
	 * Please note that two different documents may have both a paragraph with same identifier
	 * 
	 * @return
	 */
	@Getter(IDENTIFIER_KEY)
	public String getIdentifier();

	@Setter(IDENTIFIER_KEY)
	public void setIdentifier(String identifier);

	@Getter(value = STYLE_KEY)
	public FlexoStyle<D, TA> getStyle();

	@Setter(STYLE_KEY)
	public void setStyle(FlexoStyle<D, TA> style);

	public static abstract class FlexoParagraphImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocumentElementImpl<D, TA>implements FlexoParagraph<D, TA> {
	}

}
