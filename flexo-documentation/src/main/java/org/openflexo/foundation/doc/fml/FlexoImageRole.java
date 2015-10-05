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

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
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
@ImplementationClass(FlexoImageRole.FlexoImageRoleImpl.class)
public interface FlexoImageRole<R extends FlexoDrawingRun<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FlexoRole<R> {

	@PropertyIdentifier(type = FlexoDrawingRun.class)
	public static final String DRAWING_RUN_KEY = "drawingRun";
	@PropertyIdentifier(type = String.class)
	public static final String PARAGRAPH_ID_KEY = "paragraphId";
	@PropertyIdentifier(type = Integer.class)
	public static final String RUN_INDEX_KEY = "runIndex";

	/**
	 * Return the template document
	 * 
	 * @return
	 */
	public FlexoDocument<D, TA> getDocument();

	/**
	 * Return the represented drawing run in the template document resource<br>
	 * Note that is not the run that is to be managed at run-time
	 * 
	 * @return
	 */
	@Getter(value = DRAWING_RUN_KEY)
	public R getDrawingRun();

	/**
	 * Sets the represented drawing run in the template document resource<br>
	 * 
	 * @param fragment
	 */
	@Setter(DRAWING_RUN_KEY)
	public void setDrawingRun(R run);

	/**
	 * Return the drawingRun id where the drawing run is contained, in the template document resource<br>
	 */
	@Getter(PARAGRAPH_ID_KEY)
	@XMLAttribute
	public String getParagraphId();

	/**
	 * Sets the drawingRun id where the drawing run is contained, in the template document resource<br>
	 * 
	 * @param paragraphId
	 */
	@Setter(PARAGRAPH_ID_KEY)
	public void setParagraphId(String paragraphId);

	/**
	 * Return the run index of drawingRun where the drawing run is contained, in the template document resource<br>
	 */
	@Getter(value = RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getRunIndex();

	/**
	 * Sets the run index of drawingRun where the drawing run is contained, in the template document resource<br>
	 * 
	 * @param runIndex
	 */
	@Setter(RUN_INDEX_KEY)
	public void setRunIndex(int runIndex);

	public static abstract class FlexoImageRoleImpl<R extends FlexoDrawingRun<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoRoleImpl<R>implements FlexoImageRole<R, D, TA> {

		private R drawingRun;
		private String paragraphId;
		private int runIndex = -1;

		@Override
		public FlexoDocument<D, TA> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot) {
				return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

		@Override
		public String getParagraphId() {
			if (getDrawingRun() != null && getDrawingRun().getParagraph() != null) {
				return getDrawingRun().getParagraph().getIdentifier();
			}
			return paragraphId;
		}

		@Override
		public void setParagraphId(String paragraphId) {
			if ((paragraphId == null && this.paragraphId != null) || (paragraphId != null && !paragraphId.equals(this.paragraphId))) {
				String oldValue = getParagraphId();
				this.paragraphId = paragraphId;
				this.drawingRun = null;
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_ID_KEY, oldValue, paragraphId);
				getPropertyChangeSupport().firePropertyChange(DRAWING_RUN_KEY, null, drawingRun);
			}
		}

		@Override
		public int getRunIndex() {
			if (getDrawingRun() != null && getDrawingRun().getParagraph() != null) {
				return getDrawingRun().getIndex();
			}
			return runIndex;
		}

		@Override
		public void setRunIndex(int index) {
			if (index != this.runIndex) {
				int oldValue = getRunIndex();
				this.runIndex = index;
				this.drawingRun = null;
				getPropertyChangeSupport().firePropertyChange(RUN_INDEX_KEY, oldValue, index);
				getPropertyChangeSupport().firePropertyChange(DRAWING_RUN_KEY, null, drawingRun);
			}
		}

		@Override
		public R getDrawingRun() {
			if (drawingRun == null && StringUtils.isNotEmpty(paragraphId) && runIndex > -1 && getDocument() != null) {
				FlexoDocElement<D, TA> element = getDocument().getElementWithIdentifier(paragraphId);
				if (element instanceof FlexoDocParagraph) {
					// TODO: implement here a scheme to retrieve the closest DrawingRun if runIndex does not match
					drawingRun = (R) ((FlexoDocParagraph) element).getRuns().get(runIndex);
				}
			}
			return drawingRun;
		}

		@Override
		public void setDrawingRun(R run) {
			R oldValue = this.drawingRun;
			if (run != oldValue) {
				this.drawingRun = run;
				getPropertyChangeSupport().firePropertyChange(DRAWING_RUN_KEY, oldValue, run);
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_ID_KEY, null, getParagraphId());
				getPropertyChangeSupport().firePropertyChange(RUN_INDEX_KEY, null, getRunIndex());
			}
		}

		@Override
		public ImageActorReference<R> makeActorReference(R drawingRun, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory factory = fci.getFactory();
			ImageActorReference<R> returned = factory.newInstance(ImageActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(drawingRun);
			return returned;
		}

	}
}
