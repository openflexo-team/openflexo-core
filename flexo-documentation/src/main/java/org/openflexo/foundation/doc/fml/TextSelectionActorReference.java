/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Excelconnector, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.doc.fml;

import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocFragment.FragmentConsistencyException;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Implements {@link ActorReference} for {@link FlexoDocFragment}.<br>
 * Represents the actual links in a given {@link FlexoDocument} connecting a template fragment to a generated fragment<br>
 * We need to store here the bindings between elements in template and corresponding elements in referenced {@link FlexoDocument}
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object
 */
@ModelEntity
@ImplementationClass(TextSelectionActorReference.TextSelectionActorReferenceImpl.class)
@XMLElement
@FML("FragmentActorReference")
public interface TextSelectionActorReference<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends ActorReference<TextSelection<D, TA>> {

	@PropertyIdentifier(type = String.class)
	public static final String START_ELEMENT_IDENTIFIER_KEY = "startElementId";
	@PropertyIdentifier(type = String.class)
	public static final String END_ELEMENT_IDENTIFIER_KEY = "endElementId";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_RUN_INDEX_KEY = "startRunId";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_RUN_INDEX_KEY = "endRunId";
	@PropertyIdentifier(type = Integer.class)
	public static final String START_CHARACTER_INDEX_KEY = "startCharId";
	@PropertyIdentifier(type = Integer.class)
	public static final String END_CHARACTER_INDEX_KEY = "endCharId";

	@Getter(START_ELEMENT_IDENTIFIER_KEY)
	@XMLAttribute
	public String getStartElementIdentifier();

	@Setter(START_ELEMENT_IDENTIFIER_KEY)
	public void setStartElementIdentifier(String startElementIdentifier);

	@Getter(END_ELEMENT_IDENTIFIER_KEY)
	@XMLAttribute
	public String getEndElementIdentifier();

	@Setter(END_ELEMENT_IDENTIFIER_KEY)
	public void setEndElementIdentifier(String endElementIdentifier);

	@Getter(value = START_RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getStartRunIndex();

	@Setter(START_RUN_INDEX_KEY)
	public void setStartRunIndex(int startRunIndex);

	@Getter(value = END_RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getEndRunIndex();

	@Setter(END_RUN_INDEX_KEY)
	public void setEndRunIndex(int endRunIndex);

	@Getter(value = START_CHARACTER_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getStartCharacterIndex();

	@Setter(START_CHARACTER_INDEX_KEY)
	public void setStartCharacterIndex(int startRunIndex);

	@Getter(value = END_CHARACTER_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getEndCharacterIndex();

	@Setter(END_CHARACTER_INDEX_KEY)
	public void setEndCharacterIndex(int endRunIndex);

	public abstract static class TextSelectionActorReferenceImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends ActorReferenceImpl<TextSelection<D, TA>> implements TextSelectionActorReference<D, TA> {

		private static final Logger logger = FlexoLogger.getLogger(TextSelectionActorReference.class.getPackage().toString());

		private TextSelection<D, TA> textSelection;

		public FlexoDocument<D, TA> getFlexoDocument() {
			ModelSlotInstance<?, ?> msInstance = getModelSlotInstance();
			if (msInstance != null && msInstance.getAccessedResourceData() != null) {
				return (FlexoDocument<D, TA>) msInstance.getAccessedResourceData();
			}
			return null;
		}

		@Override
		public TextSelection<D, TA> getModellingElement(boolean forceLoading) {

			if (textSelection == null) {
				System.out.println("Decoding TextSelection");
				FlexoDocument<D, TA> document = getFlexoDocument();
				System.out.println("doc=" + document);
				if (document != null) {
					FlexoDocElement<D, TA> startElement = document.getElementWithIdentifier(getStartElementIdentifier());
					FlexoDocElement<D, TA> endElement = document.getElementWithIdentifier(getEndElementIdentifier());
					System.out.println("startElement=" + startElement + " (" + getStartElementIdentifier() + ")");
					System.out.println("endElement=" + endElement + " (" + getEndElementIdentifier() + ")");
					try {
						FlexoDocFragment<D, TA> fragment = document.getFactory().makeFragment(startElement, endElement);
						System.out.println("fragment=" + fragment);
						textSelection = document.getFactory().makeTextSelection(startElement, getStartRunIndex(), getStartCharacterIndex(),
								endElement, getEndRunIndex(), getEndCharacterIndex());
					} catch (FragmentConsistencyException e) {
						e.printStackTrace();
					}
				}
				else {
					logger.warning("Could not access to document from model slot " + getModelSlotInstance());
				}
			}

			return textSelection;
		}

		@Override
		public void setModellingElement(TextSelection<D, TA> aTextSelection) {

			System.out.println("setModellingElement with " + aTextSelection);
			this.textSelection = aTextSelection;
		}

		private String startElementIdentifier;
		private String endElementIdentifier;
		private int startRunIndex = -1;
		private int endRunIndex = -1;
		private int startCharacterIndex = -1;
		private int endCharacterIndex = -1;

		@Override
		public String getStartElementIdentifier() {
			if (textSelection != null) {
				return textSelection.getStartElementIdentifier();
			}
			return startElementIdentifier;
		}

		@Override
		public void setStartElementIdentifier(String startElementIdentifier) {
			this.startElementIdentifier = startElementIdentifier;
		}

		@Override
		public String getEndElementIdentifier() {
			if (textSelection != null) {
				return textSelection.getEndElementIdentifier();
			}
			return endElementIdentifier;
		}

		@Override
		public void setEndElementIdentifier(String endElementIdentifier) {
			this.endElementIdentifier = endElementIdentifier;
		}

		@Override
		public int getStartRunIndex() {
			if (textSelection != null) {
				return textSelection.getStartRunIndex();
			}
			return startRunIndex;
		}

		@Override
		public void setStartRunIndex(int startRunIndex) {
			this.startRunIndex = startRunIndex;
		}

		@Override
		public int getEndRunIndex() {
			if (textSelection != null) {
				return textSelection.getEndRunIndex();
			}
			return endRunIndex;
		}

		@Override
		public void setEndRunIndex(int endRunIndex) {
			this.endRunIndex = endRunIndex;
		}

		@Override
		public int getStartCharacterIndex() {
			if (textSelection != null) {
				return textSelection.getStartCharacterIndex();
			}
			return startCharacterIndex;
		}

		@Override
		public void setStartCharacterIndex(int startCharacterIndex) {
			this.startCharacterIndex = startCharacterIndex;
		}

		@Override
		public int getEndCharacterIndex() {
			if (textSelection != null) {
				return textSelection.getEndCharacterIndex();
			}
			return endCharacterIndex;
		}

		@Override
		public void setEndCharacterIndex(int endCharacterIndex) {
			this.endCharacterIndex = endCharacterIndex;
		}
	}
}
