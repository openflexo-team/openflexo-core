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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	@PropertyIdentifier(type = FlexoStyle.class)
	public static final String STYLE_KEY = "style";

	@Getter(value = STYLE_KEY)
	public FlexoStyle<D, TA> getStyle();

	@Setter(STYLE_KEY)
	public void setStyle(FlexoStyle<D, TA> style);

	public String getRawText();

	public String getRawTextPreview();

	/**
	 * Return the list of sub-paragraph using interpretation of
	 * 
	 * @return
	 */
	// public List<FlexoParagraph<D,TA>> getSubParagraphs();

	public static abstract class FlexoParagraphImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocumentElementImpl<D, TA>implements FlexoParagraph<D, TA> {

		@Override
		public String toString() {
			return "Paragraph(" + getIdentifier() + ") " + getRawText() + " "
					+ (getStyle() != null ? "[" + getStyle().getName() + "]" : "");
		}

		@Override
		protected List<FlexoDocumentElement<D, TA>> computeChildrenElements() {
			if (getFlexoDocument() == null) {
				return null;
			}
			if (getStyle() == null || !getStyle().isLevelled()) {
				return Collections.emptyList();
			}

			Integer parentLevel = getStyle().getLevel();
			Integer childLevel = null;
			int start = getFlexoDocument().getElements().indexOf(this) + 1;

			int i = start;

			List<FlexoDocumentElement<D, TA>> returned = new ArrayList<FlexoDocumentElement<D, TA>>();

			while (i < getFlexoDocument().getElements().size()) {
				FlexoDocumentElement<D, TA> e = getFlexoDocument().getElements().get(i);

				if (e instanceof FlexoParagraph) {
					if (((FlexoParagraph<D, TA>) e).getStyle() != null && ((FlexoParagraph<D, TA>) e).getStyle().isLevelled()) {
						if (((FlexoParagraph<D, TA>) e).getStyle().getLevel() <= parentLevel) {
							return returned;
						}
					}
				}

				if (childLevel == null) {
					returned.add(e);
					if (e instanceof FlexoParagraph) {
						if (((FlexoParagraph<D, TA>) e).getStyle() != null && ((FlexoParagraph<D, TA>) e).getStyle().isLevelled()) {
							childLevel = ((FlexoParagraph<D, TA>) e).getStyle().getLevel();
						}
					}
				} else {
					if (e instanceof FlexoParagraph) {
						if (((FlexoParagraph<D, TA>) e).getStyle() != null) {
							if (((FlexoParagraph<D, TA>) e).getStyle().getLevel().equals(childLevel)) {
								returned.add(e);
							}
						}
					}
				}

				/*if (childLevel == null) 
				if (e instanceof FlexoParagraph) {
					if (((FlexoParagraph<D, TA>) e).getStyle() != null && ((FlexoParagraph<D, TA>) e).getStyle().isLevelled()) {
						if (((FlexoParagraph<D, TA>) e).getStyle().getLevel() <= l) {
							return returned;
						}
					}
				}
				returned.add(e);*/
				i++;
			}

			return returned;

		}

		@Override
		public String getRawTextPreview() {
			// TODO: perf issue
			String rawText = getRawText();
			if (rawText.length() > 35) {
				return rawText.substring(0, 35) + "...";
			} else {
				return rawText;
			}
		}
	}

}
