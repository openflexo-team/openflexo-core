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

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.doc.DocumentFactory;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.nature.ScreenshotService.CouldNotGenerateScreenshotException;
import org.openflexo.foundation.nature.ScreenshotableNature;
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
 * Implements {@link ActorReference} for {@link FlexoDrawingRun}.
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object
 */
@ModelEntity
@ImplementationClass(ImageActorReference.ImageActorReferenceImpl.class)
@XMLElement
@FML("ParagraphActorReference")
public interface ImageActorReference<R extends FlexoDrawingRun<?, ?>> extends ActorReference<R> {

	@PropertyIdentifier(type = String.class)
	public static final String PARAGRAPH_ID_KEY = "paragraphId";
	@PropertyIdentifier(type = Integer.class)
	public static final String RUN_INDEX_KEY = "runIndex";

	/**
	 * Return the drawingRun id where the drawing run is contained<br>
	 */
	@Getter(PARAGRAPH_ID_KEY)
	@XMLAttribute
	public String getParagraphId();

	/**
	 * Sets the drawingRun id where the drawing run is contained<br>
	 * 
	 * @param paragraphId
	 */
	@Setter(PARAGRAPH_ID_KEY)
	public void setParagraphId(String paragraphId);

	/**
	 * Return the run index of drawingRun where the drawing run is contained<br>
	 */
	@Getter(value = RUN_INDEX_KEY, defaultValue = "-1")
	@XMLAttribute
	public int getRunIndex();

	/**
	 * Sets the run index of drawingRun where the drawing run is contained<br>
	 * 
	 * @param runIndex
	 */
	@Setter(RUN_INDEX_KEY)
	public void setRunIndex(int runIndex);

	/**
	 * This method is called to generate the image
	 * 
	 */
	public void generateImage() throws CouldNotGenerateScreenshotException;

	public abstract static class ImageActorReferenceImpl<R extends FlexoDrawingRun<?, ?>> extends ActorReferenceImpl<R>
			implements ImageActorReference<R> {

		private static final Logger logger = FlexoLogger.getLogger(ImageActorReference.class.getPackage().toString());

		private String paragraphId;
		private int runIndex;
		private R drawingRun;

		public FlexoDocument<?, ?> getFlexoDocument() {
			ModelSlotInstance<?, ?> msInstance = getModelSlotInstance();
			if (msInstance != null && msInstance.getAccessedResourceData() != null) {
				return (FlexoDocument<?, ?>) msInstance.getAccessedResourceData();
			}
			return null;
		}

		@Override
		public String getParagraphId() {
			if (drawingRun != null && drawingRun.getParagraph() != null) {
				return drawingRun.getParagraph().getIdentifier();
			}
			return paragraphId;
		}

		@Override
		public void setParagraphId(String paragraphIdentifier) {
			this.paragraphId = paragraphIdentifier;
		}

		@Override
		public int getRunIndex() {
			if (drawingRun != null && drawingRun.getParagraph() != null) {
				return drawingRun.getIndex();
			}
			return runIndex;
		}

		@Override
		public void setRunIndex(int index) {
			this.runIndex = index;
		}

		private FlexoDocParagraph<?, ?> getParagraph() {
			if (drawingRun != null) {
				return drawingRun.getParagraph();
			}
			FlexoDocument<?, ?> document = getFlexoDocument();
			FlexoDocElement<?, ?> element = document.getElementWithIdentifier(paragraphId);
			if (element instanceof FlexoDocParagraph) {
				return (FlexoDocParagraph) element;
			}
			return null;
		}

		@Override
		public R getModellingElement() {

			FlexoDocument<?, ?> document = getFlexoDocument();
			if (drawingRun == null && StringUtils.isNotEmpty(paragraphId) && runIndex > -1 && document != null) {
				FlexoDocElement<?, ?> element = document.getElementWithIdentifier(paragraphId);
				if (element instanceof FlexoDocParagraph) {
					// TODO: implement here a scheme to retrieve the closest DrawingRun if runIndex does not match
					drawingRun = (R) ((FlexoDocParagraph) element).getRuns().get(runIndex);
				}
			}
			return drawingRun;
		}

		@Override
		public void setModellingElement(R aDrawingRun) {

			if (aDrawingRun != drawingRun) {
				R oldValue = drawingRun;
				drawingRun = aDrawingRun;
				getPropertyChangeSupport().firePropertyChange(MODELLING_ELEMENT_KEY, oldValue, drawingRun);
				getPropertyChangeSupport().firePropertyChange(PARAGRAPH_ID_KEY, null, getParagraphId());
				getPropertyChangeSupport().firePropertyChange(RUN_INDEX_KEY, null, getRunIndex());
			}
		}

		private void deleteDrawingRun() {
			drawingRun.getParagraph().removeFromRuns((FlexoDrawingRun) drawingRun);
			drawingRun.delete();
			drawingRun = null;
		}

		/**
		 * This method is called to generate the image
		 * 
		 * @throws CouldNotGenerateScreenshotException
		 * 
		 */
		@Override
		public void generateImage() throws CouldNotGenerateScreenshotException {
			System.out.println("On y va on genere l'image pour " + getFlexoRole());
			System.out.println("getServiceManager()=" + getServiceManager());
			System.out.println("getScreenshotService()=" + getServiceManager().getScreenshotService());

			FlexoImageRole<R, ?, ?> imageRole = (FlexoImageRole<R, ?, ?>) getFlexoRole();

			FlexoObject objectToRepresent;
			try {
				objectToRepresent = imageRole.getRepresentedObject().getBindingValue(getFlexoConceptInstance());
				System.out.println("fci:" + getFlexoConceptInstance());
				System.out.println("representedObjectBinding: " + imageRole.getRepresentedObject());
				System.out.println("objectToRepresent=" + objectToRepresent);
				System.out.println("drawingRun=" + drawingRun);
				drawingRun = getModellingElement(); // To be sure we are in sync
				FlexoDocParagraph<?, ?> paragraph = getParagraph(); // take care that getParagraph() will return null during next code !!!
				if (getParagraph() == null) {
					logger.warning("Could not find pararaph, abort");
				}
				else if (objectToRepresent != null) {
					System.out.println("***************** We have to represent: " + objectToRepresent);
					Class<? extends ScreenshotableNature> natureClass = imageRole.getNature();
					BufferedImage image = getServiceManager().getScreenshotService().generateScreenshot(objectToRepresent,
							(Class) natureClass);
					if (drawingRun != null) {
						deleteDrawingRun();
					}
					DocumentFactory<?, ?> factory = getFlexoDocument().getFactory();
					drawingRun = (R) factory.makeDrawingRun(image);

					// Add eventual required empty run, for that runIndex matches generated drawingRun
					while (getRunIndex() > paragraph.getRuns().size()) {
						FlexoTextRun<?, ?> emptyRun = factory.makeTextRun();
						paragraph.addToRuns((FlexoTextRun) emptyRun);
					}

					// Now, getRunIndex() <= getParagraph().getRuns().size()) {
					paragraph.insertRunAtIndex((FlexoDrawingRun) drawingRun, getRunIndex());

				}
				else { // object to represent = null > delete the drawing run !
					System.out.println("***************** No object represent: " + objectToRepresent);
					System.out.println("drawingRun=" + drawingRun);
					if (drawingRun != null) {
						// Existing run must be removed
						deleteDrawingRun();
					}
				}
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

}
