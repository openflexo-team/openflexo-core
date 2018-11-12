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
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Generic abstract concept representing an unmapped element in a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocUnmappedElement<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocElement<D, TA> {

	public static abstract class FlexoDocUnmappedElementImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoDocumentElementImpl<D, TA> implements FlexoDocUnmappedElement<D, TA> {

		@Override
		public String toString() {
			return "UnmappedElement(" + getIdentifier() + ")";
		}
	}

}
