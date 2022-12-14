/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.doc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.doc.FlexoDocFragment.FragmentConsistencyException;
import org.openflexo.foundation.doc.TextSelection.TextMarker;
import org.openflexo.foundation.doc.rm.FlexoDocumentResource;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.converter.RelativePathResourceConverter;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * DocX factory for managing {@link DocXDocument}<br>
 * One instance of this class should be used for each {@link FlexoDocumentResource}
 * 
 * @author sylvain
 * 
 */
public abstract class DocumentFactory<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>> extends PamelaModelFactory
		implements PamelaResourceModelFactory<FlexoDocumentResource<D, TA, ?>> {

	private static final Logger logger = Logger.getLogger(DocumentFactory.class.getPackage().getName());

	private final FlexoDocumentResource<D, TA, ?> resource;
	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	private RelativePathResourceConverter relativePathResourceConverter;

	public DocumentFactory(PamelaMetaModel pamelaMetaModel, FlexoDocumentResource<D, TA, ?> resource, EditingContext editingContext) {
		super(pamelaMetaModel);
		this.resource = resource;
		setEditingContext(editingContext);
		addConverter(relativePathResourceConverter = new RelativePathResourceConverter(null));
		if (resource != null && resource.getIODelegate() != null && resource.getIODelegate().getSerializationArtefactAsResource() != null) {
			relativePathResourceConverter
					.setContainerResource(resource.getIODelegate().getSerializationArtefactAsResource().getContainer());
		}
	}

	@Override
	public FlexoDocumentResource<D, TA, ?> getResource() {
		return resource;
	}

	/**
	 * Build new empty document
	 * 
	 * @return
	 */
	public abstract D makeDocument();

	/**
	 * Generate a new unique id
	 * 
	 * @return
	 */
	public abstract String generateId();

	/**
	 * Build new empty paragraph
	 * 
	 * @return
	 */
	public abstract FlexoDocParagraph<D, TA> makeParagraph();

	/**
	 * Build new empty paragraph style
	 * 
	 * @return
	 */
	public abstract FlexoParagraphStyle<D, TA> makeParagraphStyle();

	/**
	 * Build new empty run style
	 * 
	 * @return
	 */
	public abstract FlexoRunStyle<D, TA> makeRunStyle();

	/**
	 * Build new empty run
	 * 
	 * @return
	 */
	public abstract FlexoTextRun<D, TA> makeTextRun();

	/**
	 * Build new run set with supplied text
	 * 
	 * @return
	 */
	public abstract FlexoTextRun<D, TA> makeTextRun(String text);

	/**
	 * Build new empty run
	 * 
	 * @return
	 */
	public abstract FlexoDrawingRun<D, TA> makeDrawingRun();

	/**
	 * Build new drawing run set with supplied image file
	 * 
	 * @return
	 */
	public abstract FlexoDrawingRun<D, TA> makeDrawingRun(File imageFile);

	/**
	 * Build new drawing run set with supplied image
	 * 
	 * @return
	 */
	public abstract FlexoDrawingRun<D, TA> makeDrawingRun(BufferedImage image);

	// public abstract void updateDrawingRun(FlexoDrawingRun<D, TA> drawingRun, BufferedImage image);

	/**
	 * Build new empty table
	 * 
	 * @return
	 */
	public abstract FlexoDocTable<D, TA> makeTable();

	/**
	 * Build new empty table row
	 * 
	 * @return
	 */
	public abstract FlexoDocTableRow<D, TA> makeTableRow();

	/**
	 * Build new empty table cell
	 * 
	 * @return
	 */
	public abstract FlexoDocTableCell<D, TA> makeTableCell();

	/**
	 * Build new empty FlexoDocSdtBlock
	 * 
	 * @return
	 */
	public abstract FlexoDocSdtBlock<D, TA> makeSdtBlock();

	/**
	 * Build new empty FlexoDocUnmappedElement
	 * 
	 * @return
	 */
	public abstract FlexoDocUnmappedElement<D, TA> makeUnmappedElement();

	/**
	 * Build new empty style
	 * 
	 * @return
	 */
	protected abstract NamedDocStyle<D, TA> makeNamedStyle();

	/**
	 * Build new empty fragment
	 * 
	 * @return
	 */
	protected abstract FlexoDocFragment<D, TA> makeFragment(D document);

	/**
	 * Build new fragment
	 * 
	 * @param startElement
	 * @param endElement
	 * @return
	 * @throws FragmentConsistencyException
	 */
	public FlexoDocFragment<D, TA> makeFragment(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
			throws FragmentConsistencyException {
		if (startElement == null) {
			throw new FragmentConsistencyException("Undefined start element");
		}
		if (endElement == null) {
			throw new FragmentConsistencyException("Undefined end element");
		}
		FlexoDocFragment<D, TA> returned = makeFragment(startElement.getFlexoDocument());
		returned.setStartElement(startElement);
		returned.setEndElement(endElement);
		// Perform some checks
		returned.checkConsistency();
		return returned;
	}

	private final Map<FlexoDocElement<D, TA>, Map<FlexoDocElement<D, TA>, FlexoDocFragment<D, TA>>> fragments = new HashMap<>();

	/**
	 * Retrieve fragment identified by start and end element<br>
	 * Implements cache
	 * 
	 * @param startElement
	 * @param endElement
	 * @return
	 * @throws FragmentConsistencyException
	 */
	public FlexoDocFragment<D, TA> getFragment(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
			throws FragmentConsistencyException {

		Map<FlexoDocElement<D, TA>, FlexoDocFragment<D, TA>> map = fragments.get(startElement);
		if (map == null) {
			map = new HashMap<>();
			fragments.put(startElement, map);
		}
		FlexoDocFragment<D, TA> returned = map.get(endElement);
		if (returned == null) {
			returned = makeFragment(startElement, endElement);
			map.put(endElement, returned);
		}
		return returned;
	}

	public TextSelection<D, TA> makeTextSelection(FlexoDocFragment<D, TA> fragment, FlexoDocElement<D, TA> startElement, int startRunId,
			int startCharId, FlexoDocElement<D, TA> endElement, int endRunId, int endCharId) {
		TextSelection<D, TA> returned = newInstance(TextSelection.class);
		returned.setFragment(fragment);
		returned.setStartElement(startElement);
		returned.setStartRunIndex(startRunId);
		returned.setStartCharacterIndex(startCharId);
		returned.setEndElement(endElement);
		returned.setEndRunIndex(endRunId);
		returned.setEndCharacterIndex(endCharId);
		return returned;
	}

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId, int startCharId,
			FlexoDocElement<D, TA> endElement, int endRunId, int endCharId) throws FragmentConsistencyException {
		FlexoDocFragment<D, TA> fragment = getFragment(startElement, endElement);
		return makeTextSelection(fragment, startElement, startRunId, startCharId, endElement, endRunId, endCharId);
	}

	public TextSelection<D, TA> makeTextSelection(FlexoDocElement<D, TA> startElement, int startRunId, FlexoDocElement<D, TA> endElement,
			int endRunId) throws FragmentConsistencyException {
		FlexoDocFragment<D, TA> fragment = getFragment(startElement, endElement);
		return makeTextSelection(fragment, startElement, startRunId, -1, endElement, endRunId, -1);
	}

	public TextSelection<D, TA> makeTextSelection(TextMarker start, TextMarker end) throws FragmentConsistencyException {
		if ((start.documentElement == null || end.documentElement == null)) {
			return null;
		}
		FlexoDocElement<D, TA> startElement = (FlexoDocElement<D, TA>) start.documentElement;
		FlexoDocElement<D, TA> endElement = (FlexoDocElement<D, TA>) end.documentElement;
		int startRunId, endRunId;
		int startCharId, endCharId;

		if (start.firstChar) {
			startCharId = -1;
		}
		else {
			startCharId = start.characterIndex;
		}
		if (end.lastChar) {
			endCharId = -1;
		}
		else {
			endCharId = end.characterIndex;
		}

		if (start.firstRun && start.firstChar) {
			startRunId = -1;
		}
		else {
			startRunId = start.runIndex;
		}
		if (end.lastRun && end.lastChar && startRunId == -1) {
			endRunId = -1;
		}
		else {
			endRunId = end.runIndex;
		}

		/*System.out.println("startRunId=" + startRunId);
		System.out.println("startCharId=" + startCharId);
		System.out.println("endRunId=" + endRunId);
		System.out.println("endCharId=" + endCharId);*/

		return makeTextSelection(startElement, startRunId, startCharId, endElement, endRunId, endCharId);
	}

	@Override
	public synchronized void startDeserializing() {
		EditingContext editingContext = getResource().getServiceManager().getEditingContext();

		if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
			undoManager = (FlexoUndoManager) editingContext.getUndoManager();
			undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(resource));
			// System.out.println("@@@@@@@@@@@@@@@@ START LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public synchronized void stopDeserializing() {
		if (ignoreHandler != null) {
			undoManager.removeFromIgnoreHandlers(ignoreHandler);
			// System.out.println("@@@@@@@@@@@@@@@@ END LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		super.objectHasBeenDeserialized(newlyCreatedObject, implementedInterface);
		if (newlyCreatedObject instanceof FlexoObject) {
			if (getResource() != null) {
				getResource().setLastID(((FlexoObject) newlyCreatedObject).getFlexoID());
			}
			else {
				logger.warning("Could not access resource beeing deserialized");
			}
		}
	}

	/**
	 * Convert the image from the file into an array of bytes.
	 *
	 * @param file
	 *            the image file to be converted
	 * @return the byte array containing the bytes from the image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static byte[] convertImageToByteArray(File file) throws FileNotFoundException, IOException {
		byte[] bytes;
		try (InputStream is = new FileInputStream(file)) {
			long length = file.length();
			// You cannot create an array using a long, it needs to be an int.
			if (length > Integer.MAX_VALUE) {
				System.out.println("File too large!!");
			}
			bytes = new byte[(int) length];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			// Ensure all the bytes have been read
			if (offset < bytes.length) {
				System.out.println("Could not completely read file " + file.getName());
			}
		}
		return bytes;
	}

	/**
	 * Convert the image from the file into an array of bytes.
	 *
	 * @param file
	 *            the image file to be converted
	 * @return the byte array containing the bytes from the image
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static byte[] convertImageToByteArray(BufferedImage image) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			return imageInByte;
		}
	}
}
