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
import org.openflexo.model.annotations.ModelEntity;

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

	/**
	 * Return name of the {@link FlexoStyle} in the {@link FlexoDocument}<br>
	 * The identifier is here a {@link String} and MUST be unique regarding the whole {@link FlexoDocument}.<br>
	 * Please note that two different documents may have both a paragraph with same identifier
	 * 
	 * @return
	 */
	public String getName();

	public static abstract class FlexoStyleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			InnerFlexoDocumentImpl<D, TA> implements FlexoStyle<D, TA> {
	}

}
