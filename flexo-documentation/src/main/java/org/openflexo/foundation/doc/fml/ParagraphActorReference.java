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

import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocParagraph.FlexoDocParagraphImpl;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Implements {@link ActorReference} for {@link FlexoDocParagraph}.<br>
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object
 */
@ModelEntity
@ImplementationClass(ParagraphActorReference.ParagraphActorReferenceImpl.class)
@XMLElement
@FML("ParagraphActorReference")
public interface ParagraphActorReference<P extends FlexoDocParagraph<?, ?>> extends ActorReference<P> {

	@PropertyIdentifier(type = String.class)
	public static final String PARAGRAPH_ID_KEY = "paragraphIdentifier";

	@Getter(value = PARAGRAPH_ID_KEY)
	@XMLAttribute
	public String getParagraphId();

	@Setter(PARAGRAPH_ID_KEY)
	public void setParagraphId(String paragraphIdentifier);

	public abstract static class ParagraphActorReferenceImpl<P extends FlexoDocParagraphImpl<?, ?>> extends ActorReferenceImpl<P>
			implements ParagraphActorReference<P> {

		private static final Logger logger = FlexoLogger.getLogger(ParagraphActorReference.class.getPackage().toString());

		private String paragraphIdentifier;
		private P paragraph;

		public FlexoDocument<?, ?> getFlexoDocument() {
			ModelSlotInstance<?, ?> msInstance = getModelSlotInstance();
			if (msInstance != null && msInstance.getAccessedResourceData() != null) {
				return (FlexoDocument<?, ?>) msInstance.getAccessedResourceData();
			}
			return null;
		}

		@Override
		public String getParagraphId() {
			if (paragraph != null) {
				return paragraph.getIdentifier();
			}
			return paragraphIdentifier;
		}

		@Override
		public void setParagraphId(String paragraphIdentifier) {
			this.paragraphIdentifier = paragraphIdentifier;
		}

		@Override
		public P getModellingElement(boolean forceLoading) {

			if (paragraph == null) {
				FlexoDocument<?, ?> document = getFlexoDocument();
				if (document != null) {
					if (StringUtils.isNotEmpty(paragraphIdentifier)) {
						paragraph = (P) document.getElementWithIdentifier(paragraphIdentifier);
					}
				}
			}

			return paragraph;
		}

		@Override
		public void setModellingElement(P aParagraph) {

			if (aParagraph != paragraph) {
				P oldValue = paragraph;
				paragraph = aParagraph;
				getPropertyChangeSupport().firePropertyChange(MODELLING_ELEMENT_KEY, oldValue, paragraph);
			}
		}
	}

}
