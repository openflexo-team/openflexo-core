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
 * Generic abstract concept representing a run in a paragraph of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoDocRun<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocObject<D, TA> {

	@PropertyIdentifier(type = FlexoDocParagraph.class)
	public static final String PARAGRAPH_KEY = "paragraph";
	@PropertyIdentifier(type = FlexoRunStyle.class)
	public static final String RUN_STYLE_KEY = "runStyle";

	@Getter(PARAGRAPH_KEY)
	public FlexoDocParagraph<D, TA> getParagraph();

	@Setter(PARAGRAPH_KEY)
	public void setParagraph(FlexoDocParagraph<D, TA> paragraph);

	@Getter(value = RUN_STYLE_KEY, ignoreType = true)
	public FlexoRunStyle<D, TA> getRunStyle();

	@Setter(RUN_STYLE_KEY)
	public void setRunStyle(FlexoRunStyle<D, TA> style);

	/**
	 * Return index of the run<br>
	 * Index of a run is the run occurence in the paragraph
	 * 
	 * @return
	 */
	public int getIndex();

	public static abstract class FlexoRunImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocObjectImpl<D, TA> implements FlexoDocRun<D, TA> {

		@Override
		public int getIndex() {
			if (getParagraph() != null) {
				return getParagraph().getRuns().indexOf(this);
			}
			return -1;
		}

	}

}
