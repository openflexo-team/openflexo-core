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

import java.awt.Color;
import java.awt.Font;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * Generic abstract concept representing style information of a run
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoRunStyle<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocStyle<D, TA> {

	@PropertyIdentifier(type = FlexoRunStyle.class)
	public static final String BASED_ON_KEY = "basedOn";

	@PropertyIdentifier(type = Font.class)
	public static final String FONT_KEY = "font";
	@PropertyIdentifier(type = Integer.class)
	public static final String FONT_SIZE_KEY = "fontSize";
	@PropertyIdentifier(type = Color.class)
	public static final String FONT_COLOR_KEY = "fontColor";
	@PropertyIdentifier(type = Color.class)
	public static final String BACKGROUND_COLOR_KEY = "backgroundColor";
	@PropertyIdentifier(type = Boolean.class)
	public static final String BOLD_KEY = "bold";
	@PropertyIdentifier(type = Boolean.class)
	public static final String ITALIC_KEY = "italic";
	@PropertyIdentifier(type = Boolean.class)
	public static final String UNDERLINE_KEY = "underline";

	@Getter(BASED_ON_KEY)
	public FlexoRunStyle<D, TA> getBasedOn();

	@Setter(BASED_ON_KEY)
	public void setBasedOn(FlexoRunStyle<D, TA> style);

	@Getter(FONT_KEY)
	public Font getFont();

	@Setter(FONT_KEY)
	public void setFont(Font aFont);

	@Getter(FONT_SIZE_KEY)
	public Integer getFontSize();

	@Setter(FONT_SIZE_KEY)
	public void setFontSize(Integer aFontSize);

	@Getter(FONT_COLOR_KEY)
	public Color getFontColor();

	@Setter(FONT_COLOR_KEY)
	public void setFontColor(Color aColor);

	@Getter(BACKGROUND_COLOR_KEY)
	public Color getBackgroundColor();

	@Setter(BACKGROUND_COLOR_KEY)
	public void setBackgroundColor(Color aColor);

	@Getter(BOLD_KEY)
	public Boolean getBold();

	@Setter(BOLD_KEY)
	public void setBold(Boolean bold);

	@Getter(ITALIC_KEY)
	public Boolean getItalic();

	@Setter(ITALIC_KEY)
	public void setItalic(Boolean italic);

	@Getter(UNDERLINE_KEY)
	public Boolean getUnderline();

	@Setter(UNDERLINE_KEY)
	public void setUnderline(Boolean underline);

	@Override
	public String getStringRepresentation();

	public static abstract class FlexoRunStyleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoDocStyleImpl<D, TA> implements FlexoRunStyle<D, TA> {

		@Override
		public String getStringRepresentation() {
			StringBuffer sb = new StringBuffer();
			sb.append(getFont() != null ? getFont().getFontName() + "," : "");
			sb.append(getFontSize() != null ? getFontSize() + "pt," : "");
			sb.append(getFontColor() != null ? getFontColor() : "");
			sb.append(getBold() != null ? (getBold() ? "bold," : "") : "");
			sb.append(getItalic() != null ? (getItalic() ? "italic," : "") : "");
			sb.append(getUnderline() != null ? (getUnderline() ? "underline," : "") : "");
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		}

	}

}
