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
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;

/**
 * Represent a textual run in a paragraph of a text-based document (eg .docx, .odt, etc...)
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoTextRun<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocRun<D, TA> {

	@PropertyIdentifier(type = String.class)
	public static final String TEXT_KEY = "text";

	@Getter(TEXT_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public String getText();

	@Setter(TEXT_KEY)
	public void setText(String text);

	public String getTextPreview();

	public void fireTextChanged();

	@Implementation
	public static abstract class FlexoTextRunImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoRunImpl<D, TA> implements FlexoTextRun<D, TA> {

		@Override
		public String getTextPreview() {
			// TODO: perf issue
			String rawText = getText();
			if (rawText.length() > 35) {
				return rawText.substring(0, 35) + "...";
			}
			else {
				return rawText;
			}
		}

		@Override
		public void fireTextChanged() {
			getPropertyChangeSupport().firePropertyChange("text", null, getText());
			getPropertyChangeSupport().firePropertyChange("textPreview", null, getTextPreview());
		}
	}

}
