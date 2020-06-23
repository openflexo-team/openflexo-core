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

import java.util.List;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Getter.Cardinality;

/**
 * Generic abstract concept representing style information of a paragraph
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
public interface FlexoParagraphStyle<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends FlexoDocStyle<D, TA> {

	public enum ParagraphAlignment {
		Right, Left, Center, Justify
	}

	@ModelEntity
	public static interface ParagraphTab {
		@PropertyIdentifier(type = Float.class)
		public static final String POS_KEY = "pos";
		@PropertyIdentifier(type = Integer.class)
		public static final String ALIGN_KEY = "align";
		@PropertyIdentifier(type = Integer.class)
		public static final String LEADER_KEY = "leader";

		@Getter(POS_KEY)
		public Float getPos();

		@Setter(POS_KEY)
		public void setPos(Float pos);

		@Getter(ALIGN_KEY)
		public Integer getAlign();

		@Setter(ALIGN_KEY)
		public void setAlign(Integer align);

		@Getter(LEADER_KEY)
		public Integer getLeader();

		@Setter(LEADER_KEY)
		public void setLeader(Integer leader);
	}

	@ModelEntity
	public static interface ParagraphSpacing {
		@PropertyIdentifier(type = LineSpacingRule.class)
		public static final String LINE_SPACING_RULE_KEY = "lineSpacingRule";
		@PropertyIdentifier(type = Integer.class)
		public static final String LINE_KEY = "line";
		@PropertyIdentifier(type = Integer.class)
		public static final String BEFORE_KEY = "before";
		@PropertyIdentifier(type = Integer.class)
		public static final String AFTER_KEY = "after";

		public enum LineSpacingRule {
			AUTO, EXACT, AT_LEAST
		};

		@Getter(LINE_SPACING_RULE_KEY)
		public LineSpacingRule getLineSpacingRule();

		@Setter(LINE_SPACING_RULE_KEY)
		public void setLineSpacingRule(LineSpacingRule lineSpacingRule);

		@Getter(LINE_KEY)
		public Integer getLine();

		@Setter(LINE_KEY)
		public void setLine(Integer line);

		@Getter(BEFORE_KEY)
		public Integer getBefore();

		@Setter(BEFORE_KEY)
		public void setBefore(Integer before);

		@Getter(AFTER_KEY)
		public Integer getAfter();

		@Setter(AFTER_KEY)
		public void setAfter(Integer after);

	}

	@ModelEntity
	public static interface ParagraphIndent {
		@PropertyIdentifier(type = Integer.class)
		public static final String LEFT_KEY = "left";
		@PropertyIdentifier(type = Integer.class)
		public static final String RIGHT_KEY = "right";
		@PropertyIdentifier(type = Integer.class)
		public static final String FIRST_KEY = "first";

		@Getter(LEFT_KEY)
		public Integer getLeft();

		@Setter(LEFT_KEY)
		public void setLeft(Integer left);

		@Getter(RIGHT_KEY)
		public Integer getRight();

		@Setter(RIGHT_KEY)
		public void setRight(Integer right);

		@Getter(FIRST_KEY)
		public Integer getFirst();

		@Setter(FIRST_KEY)
		public void setFirst(Integer first);

	}

	@ModelEntity
	public static interface ParagraphNumbering {
		@PropertyIdentifier(type = Integer.class)
		public static final String NUM_ID_KEY = "numId";
		@PropertyIdentifier(type = Integer.class)
		public static final String ILVL_KEY = "ilvl";

		@Getter(NUM_ID_KEY)
		public Integer getNumId();

		@Setter(NUM_ID_KEY)
		public void setNumId(Integer left);

		@Getter(ILVL_KEY)
		public Integer getIlvl();

		@Setter(ILVL_KEY)
		public void setIlvl(Integer right);

	}

	@PropertyIdentifier(type = ParagraphAlignment.class)
	public static final String PARAGRAPH_ALIGNMENT_KEY = "paragraphAlignment";
	@PropertyIdentifier(type = ParagraphTab.class, cardinality = Cardinality.LIST)
	public static final String PARAGRAPH_TABS_KEY = "paragraphTabs";
	@PropertyIdentifier(type = ParagraphSpacing.class)
	public static final String PARAGRAPH_SPACING_KEY = "paragraphSpacing";
	@PropertyIdentifier(type = ParagraphIndent.class)
	public static final String PARAGRAPH_INDENT_KEY = "paragraphIndent";
	@PropertyIdentifier(type = ParagraphNumbering.class)
	public static final String PARAGRAPH_NUMBERING_KEY = "paragraphNumbering";

	@Getter(PARAGRAPH_ALIGNMENT_KEY)
	public ParagraphAlignment getParagraphAlignment();

	@Setter(PARAGRAPH_ALIGNMENT_KEY)
	public void setParagraphAlignment(ParagraphAlignment align);

	@Getter(value = PARAGRAPH_TABS_KEY, cardinality = Cardinality.LIST)
	@Embedded
	public List<ParagraphTab> getParagraphTabs();

	@Setter(PARAGRAPH_TABS_KEY)
	public void setParagraphTabs(List<ParagraphTab> someTabs);

	@Adder(PARAGRAPH_TABS_KEY)
	public void addToParagraphTabs(ParagraphTab aTab);

	@Remover(PARAGRAPH_TABS_KEY)
	public void removeFromParagraphTabs(ParagraphTab aTab);

	@Getter(PARAGRAPH_SPACING_KEY)
	public ParagraphSpacing getParagraphSpacing();

	@Setter(PARAGRAPH_SPACING_KEY)
	public void setParagraphSpacing(ParagraphSpacing spacing);

	@Getter(PARAGRAPH_INDENT_KEY)
	public ParagraphIndent getParagraphIndent();

	@Setter(PARAGRAPH_INDENT_KEY)
	public void setParagraphIndent(ParagraphIndent indent);

	@Getter(PARAGRAPH_NUMBERING_KEY)
	public ParagraphNumbering getParagraphNumbering();

	@Setter(PARAGRAPH_NUMBERING_KEY)
	public void setParagraphNumbering(ParagraphNumbering numbering);

	@Override
	public String getStringRepresentation();

	public static abstract class FlexoParagraphStyleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoDocStyleImpl<D, TA> implements FlexoParagraphStyle<D, TA> {

		@Override
		public String getStringRepresentation() {
			StringBuffer sb = new StringBuffer();
			sb.append(getParagraphAlignment() != null ? getParagraphAlignment().name() + "," : "");
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		}

	}

}
