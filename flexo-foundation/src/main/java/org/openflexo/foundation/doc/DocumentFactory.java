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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.doc.FlexoDocumentFragment.FragmentConsistencyException;
import org.openflexo.foundation.doc.rm.FlexoDocumentResource;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.ModelContext;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * DocX factory for managing {@link DocXDocument}<br>
 * One instance of this class should be used for each {@link FlexoDocumentResource}
 * 
 * @author sylvain
 * 
 */
public abstract class DocumentFactory<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends ModelFactory
		implements PamelaResourceModelFactory<FlexoDocumentResource<D, TA, ?>> {

	private static final Logger logger = Logger.getLogger(DocumentFactory.class.getPackage().getName());

	private final FlexoDocumentResource<D, TA, ?> resource;
	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	public DocumentFactory(ModelContext modelContext, FlexoDocumentResource<D, TA, ?> resource, EditingContext editingContext)
			throws ModelDefinitionException {
		super(modelContext);
		this.resource = resource;
		setEditingContext(editingContext);
		if (resource != null) {
			addConverter(new RelativePathResourceConverter(resource.getFlexoIODelegate().getParentPath()));
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
	protected abstract D makeDocument();

	/**
	 * Build new empty paragraph
	 * 
	 * @return
	 */
	protected abstract FlexoParagraph<D, TA> makeParagraph();

	/**
	 * Build new empty style
	 * 
	 * @return
	 */
	protected abstract FlexoStyle<D, TA> makeStyle();

	/**
	 * Build new empty fragment
	 * 
	 * @return
	 */
	protected abstract FlexoDocumentFragment<D, TA> makeFragment(D document);

	/**
	 * Build new fragment
	 * 
	 * @param startElement
	 * @param endElement
	 * @return
	 * @throws FragmentConsistencyException
	 */
	public FlexoDocumentFragment<D, TA> makeFragment(FlexoDocumentElement<D, TA> startElement, FlexoDocumentElement<D, TA> endElement)
			throws FragmentConsistencyException {
		FlexoDocumentFragment<D, TA> returned = makeFragment(startElement.getFlexoDocument());
		returned.setStartElement(startElement);
		returned.setEndElement(endElement);
		// Perform some checks
		returned.checkConsistency();
		return returned;
	}

	private final Map<FlexoDocumentElement<D, TA>, Map<FlexoDocumentElement<D, TA>, FlexoDocumentFragment<D, TA>>> fragments = new HashMap<FlexoDocumentElement<D, TA>, Map<FlexoDocumentElement<D, TA>, FlexoDocumentFragment<D, TA>>>();

	/**
	 * Retrieve fragment identified by start and end element<br>
	 * Implements cache
	 * 
	 * @param startElement
	 * @param endElement
	 * @return
	 * @throws FragmentConsistencyException
	 */
	public FlexoDocumentFragment<D, TA> getFragment(FlexoDocumentElement<D, TA> startElement, FlexoDocumentElement<D, TA> endElement)
			throws FragmentConsistencyException {

		Map<FlexoDocumentElement<D, TA>, FlexoDocumentFragment<D, TA>> map = fragments.get(startElement);
		if (map == null) {
			map = new HashMap<FlexoDocumentElement<D, TA>, FlexoDocumentFragment<D, TA>>();
			fragments.put(startElement, map);
		}
		FlexoDocumentFragment<D, TA> returned = map.get(endElement);
		if (returned == null) {
			returned = makeFragment(startElement, endElement);
			map.put(endElement, returned);
		}
		return returned;
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
			} else {
				logger.warning("Could not access resource beeing deserialized");
			}
		}
	}

}
