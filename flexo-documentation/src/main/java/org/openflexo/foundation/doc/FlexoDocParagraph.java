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
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.toolbox.StringUtils;

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
public interface FlexoDocParagraph<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocElement<D, TA> {

	@PropertyIdentifier(type = FlexoDocRun.class, cardinality = Cardinality.LIST)
	public static final String RUNS_KEY = "runs";

	/**
	 * Return the list of runs of this paragraph
	 * 
	 * @return
	 */
	@Getter(value = RUNS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocRun.PARAGRAPH_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocRun<D, TA>> getRuns();

	@Setter(RUNS_KEY)
	public void setRuns(List<FlexoDocRun<D, TA>> someRuns);

	/**
	 * Add run to this {@link FlexoDocParagraph} (public API).<br>
	 * Element will be added to underlying technology-specific model and {@link FlexoDocParagraph} will be updated accordingly
	 */
	@Adder(RUNS_KEY)
	@PastingPoint
	public void addToRuns(FlexoDocRun<D, TA> aRun);

	/**
	 * Remove run from this {@link FlexoDocParagraph} (public API).<br>
	 * Element will be removed from underlying technology-specific model and {@link FlexoDocParagraph} will be updated accordingly
	 */
	@Remover(RUNS_KEY)
	public void removeFromRuns(FlexoDocRun<D, TA> aRun);

	/**
	 * Insert run to this {@link FlexoDocParagraph} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific model and {@link FlexoDocParagraph} will be updated accordingly
	 */
	public void insertRunAtIndex(FlexoDocRun<D, TA> anElement, int index);

	/**
	 * Move run in this {@link FlexoDocParagraph} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific model and {@link FlexoDocParagraph} will be updated accordingly
	 */
	public void moveRunToIndex(FlexoDocRun<D, TA> anElement, int index);

	/**
	 * Return a string representation (plain text) of contents of the paragraph (styles associated to runs are not reflected)
	 * 
	 * @return
	 */
	public String getRawText();

	/**
	 * Sets contents of the paragraph by erasing actual structure, and replacing it by a unique run reflecting supplied text
	 * 
	 * @return
	 */
	public void setRawText(String someText);

	public String getRawTextPreview();

	public void fireTextChanged();

	/**
	 * Return a new list containing {@link FlexoDrawingRun} for this paragraph
	 * 
	 * @return
	 */
	public List<FlexoDrawingRun<D, TA>> getDrawingRuns();

	public static abstract class FlexoDocParagraphImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoDocumentElementImpl<D, TA> implements FlexoDocParagraph<D, TA> {

		@Override
		public String toString() {
			return "Paragraph(" + getIdentifier() + ") " /* + getRawText() + " "*/
					+ (getNamedStyle() != null ? "[" + getNamedStyle().getName() + "]" : "")
					+ (getParagraphStyle() != null ? "[" + getParagraphStyle().getStringRepresentation() + "]" : "");
		}

		@Override
		protected List<FlexoDocElement<D, TA>> computeChildrenElements() {
			if (getFlexoDocument() == null) {
				return null;
			}
			if (getNamedStyle() == null || !getNamedStyle().isLevelled()) {
				return Collections.emptyList();
			}

			Integer parentLevel = getNamedStyle().getLevel();
			Integer childLevel = null;
			int start = getFlexoDocument().getElements().indexOf(this) + 1;

			int i = start;

			List<FlexoDocElement<D, TA>> returned = new ArrayList<>();

			while (i < getFlexoDocument().getElements().size()) {
				FlexoDocElement<D, TA> e = getFlexoDocument().getElements().get(i);

				if (e instanceof FlexoDocParagraph) {
					if (((FlexoDocParagraph<D, TA>) e).getNamedStyle() != null
							&& ((FlexoDocParagraph<D, TA>) e).getNamedStyle().isLevelled()) {
						if (((FlexoDocParagraph<D, TA>) e).getNamedStyle().getLevel() <= parentLevel) {
							return returned;
						}
					}
				}

				if (childLevel == null) {
					returned.add(e);
					if (e instanceof FlexoDocParagraph) {
						if (((FlexoDocParagraph<D, TA>) e).getNamedStyle() != null
								&& ((FlexoDocParagraph<D, TA>) e).getNamedStyle().isLevelled()) {
							childLevel = ((FlexoDocParagraph<D, TA>) e).getNamedStyle().getLevel();
						}
					}
				}
				else {
					if (e instanceof FlexoDocParagraph) {
						if (((FlexoDocParagraph<D, TA>) e).getNamedStyle() != null) {
							if (((FlexoDocParagraph<D, TA>) e).getNamedStyle().getLevel().equals(childLevel)) {
								returned.add(e);
							}
						}
					}
				}

				/*if (childLevel == null) 
				if (e instanceof FlexoDocParagraph) {
					if (((FlexoDocParagraph<D, TA>) e).getStyle() != null && ((FlexoDocParagraph<D, TA>) e).getStyle().isLevelled()) {
						if (((FlexoDocParagraph<D, TA>) e).getStyle().getLevel() <= l) {
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
			}
			else if (StringUtils.isNotEmpty(rawText)) {
				return rawText;
			}
			else {
				return "<newline>";
			}
		}

		@Override
		public void fireTextChanged() {
			getPropertyChangeSupport().firePropertyChange("rawText", null, getRawText());
			getPropertyChangeSupport().firePropertyChange("rawTextPreview", null, getRawTextPreview());
		}

		@Override
		public List<FlexoDrawingRun<D, TA>> getDrawingRuns() {
			List<FlexoDrawingRun<D, TA>> returned = new ArrayList<>();
			for (FlexoDocRun<?, ?> run : getRuns()) {
				if (run instanceof FlexoDrawingRun) {
					returned.add((FlexoDrawingRun) run);
				}
			}
			return returned;
		}
	}

}
