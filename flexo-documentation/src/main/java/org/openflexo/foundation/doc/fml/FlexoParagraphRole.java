/*
 * (c) Copyright 2013- Openflexo
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

package org.openflexo.foundation.doc.fml;

import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoParagraphRole.FlexoParagraphRoleImpl.class)
public interface FlexoParagraphRole<P extends FlexoDocParagraph<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FlexoRole<P> {

	@PropertyIdentifier(type = FlexoDocParagraph.class)
	public static final String PARAGRAPH_KEY = "paragraph";
	@PropertyIdentifier(type = String.class)
	public static final String PARAGRAPH_ID_KEY = "paragraphId";

	/**
	 * Return the template document
	 * 
	 * @return
	 */
	public FlexoDocument<D, TA> getDocument();

	/**
	 * Return the represented paragraph containing image in the template document resource<br>
	 * Note that is not the paragraph that is to be managed at run-time
	 * 
	 * @return
	 */
	@Getter(value = PARAGRAPH_KEY)
	public P getParagraph();

	/**
	 * Sets the represented paragraph containing image in the template document resource<br>
	 * 
	 * @param fragment
	 */
	@Setter(PARAGRAPH_KEY)
	public void setParagraph(P table);

	/**
	 * Return the represented paragraph id in the template document resource<br>
	 */
	@Getter(PARAGRAPH_ID_KEY)
	@XMLAttribute
	public String getParagraphId();

	@Setter(PARAGRAPH_ID_KEY)
	public void setParagraphId(String paragraphId);

	public static abstract class FlexoParagraphRoleImpl<P extends FlexoDocParagraph<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoRoleImpl<P>implements FlexoParagraphRole<P, D, TA> {

		private P paragraph;
		private String paragraphId;

		@Override
		public FlexoDocument<D, TA> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot) {
				return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

		@Override
		public String getParagraphId() {
			if (getParagraph() != null) {
				return getParagraph().getIdentifier();
			}
			return paragraphId;
		}

		@Override
		public void setParagraphId(String tableId) {
			if ((tableId == null && this.paragraphId != null) || (tableId != null && !tableId.equals(this.paragraphId))) {
				String oldValue = getParagraphId();
				this.paragraphId = tableId;
				this.paragraph = null;
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_ID_KEY, oldValue, tableId);
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_KEY, null, paragraph);
			}
		}

		@Override
		public P getParagraph() {
			if (paragraph == null && StringUtils.isNotEmpty(paragraphId) && getDocument() != null) {
				paragraph = (P) getDocument().getElementWithIdentifier(paragraphId);
			}
			return paragraph;
		}

		@Override
		public void setParagraph(P paragraph) {
			P oldValue = this.paragraph;
			if (paragraph != oldValue) {
				this.paragraph = paragraph;
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_KEY, oldValue, paragraph);
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_ID_KEY, null, getParagraphId());
			}
		}

		@Override
		public ParagraphActorReference<P> makeActorReference(P paragraph, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory factory = fci.getFactory();
			ParagraphActorReference<P> returned = factory.newInstance(ParagraphActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(paragraph);
			return returned;
		}

	}
}
