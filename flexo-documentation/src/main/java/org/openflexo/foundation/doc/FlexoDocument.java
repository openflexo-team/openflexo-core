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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openflexo.foundation.doc.FlexoDocFragment.FragmentConsistencyException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Generic abstract concept representing a text-based document (eg .docx, .odt, etc...)<br>
 * 
 * Such a document is not structured, as it contains only a list of {@link FlexoDocElement}, but a structuration can be dynamically
 * infered from the styles. See {@link #getStructuringStyles()}
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocument.FlexoDocumentImpl.class)
public interface FlexoDocument<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocObject<D, TA>,
		ResourceData<D>, FlexoDocElementContainer<D, TA> {

	@PropertyIdentifier(type = FlexoDocElement.class, cardinality = Cardinality.LIST)
	public static final String ELEMENTS_KEY = "elements";

	public static final String ROOT_ELEMENTS_KEY = "rootElements";

	@PropertyIdentifier(type = FlexoDocStyle.class, cardinality = Cardinality.LIST)
	public static final String STYLES_KEY = "styles";

	@PropertyIdentifier(type = FlexoDocStyle.class, cardinality = Cardinality.LIST)
	public static final String STRUCTURING_STYLES_KEY = "structuringStyles";

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";

	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	public String getURI();

	/**
	 * Return the list of top-level elements of this document (elements like paragraphs or tables, sequentially composing the document)
	 * 
	 * @return
	 */
	@Override
	@Getter(value = ELEMENTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocElement.CONTAINER_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocElement<D, TA>> getElements();

	@Setter(ELEMENTS_KEY)
	public void setElements(List<FlexoDocElement<D, TA>> someElements);

	/**
	 * Add element to this {@link FlexoDocument} (public API).<br>
	 * Element will be added to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	@Adder(ELEMENTS_KEY)
	@PastingPoint
	public void addToElements(FlexoDocElement<D, TA> anElement);

	/**
	 * Remove element from this {@link FlexoDocument} (public API).<br>
	 * Element will be removed to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	@Remover(ELEMENTS_KEY)
	public void removeFromElements(FlexoDocElement<D, TA> anElement);

	/**
	 * Insert element to this {@link FlexoDocument} at supplied index (public API).<br>
	 * Element will be inserted to underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	public void insertElementAtIndex(FlexoDocElement<D, TA> anElement, int index);

	/**
	 * Moved element to this {@link FlexoDocument} at supplied index (public API).<br>
	 * Element will be moved inside underlying technology-specific document model and {@link FlexoDocument} will be updated accordingly
	 */
	public void moveElementToIndex(FlexoDocElement<D, TA> anElement, int index);

	/**
	 * Return element identified by identifier, or null if no such element exists
	 */
	@Override
	public FlexoDocElement<D, TA> getElementWithIdentifier(String identifier);

	/**
	 * Return elements matching supplied base identifier
	 */
	public List<FlexoDocElement<D, TA>> getElementsWithBaseIdentifier(String baseIdentifier);

	/**
	 * Return a new list of elements of supplied type
	 * 
	 * @param elementType
	 * @return
	 */
	public <E> List<E> getElements(Class<E> elementType);

	/**
	 * Return the list of root elements of this document, which are infered to be root while interpreting the document as a structured
	 * document (see {@link #getStructuringStyles()})
	 * 
	 * @return
	 */
	public List<FlexoDocElement<D, TA>> getRootElements();

	public void invalidateRootElements();

	public void notifyRootElementsChanged();

	/**
	 * Return the list of style used in this document
	 * 
	 * @return
	 */
	@Getter(value = STYLES_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocStyle.DOCUMENT_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocStyle<D, TA>> getStyles();

	@Setter(STYLES_KEY)
	public void setStyles(List<FlexoDocStyle<D, TA>> someStyles);

	@Adder(STYLES_KEY)
	@PastingPoint
	public void addToStyles(FlexoDocStyle<D, TA> aStyle);

	@Remover(STYLES_KEY)
	public void removeFromStyles(FlexoDocStyle<D, TA> aStyle);

	/**
	 * Return an ordered list of styles to be used to present a structured document<br>
	 * 
	 * @return
	 */
	@Getter(value = STRUCTURING_STYLES_KEY, cardinality = Cardinality.LIST)
	public List<FlexoDocStyle<D, TA>> getStructuringStyles();

	@Setter(STRUCTURING_STYLES_KEY)
	public void setStructuringStyles(List<FlexoDocStyle<D, TA>> someStyles);

	@Adder(STRUCTURING_STYLES_KEY)
	public void addToStructuringStyles(FlexoDocStyle<D, TA> aStyle);

	@Remover(STRUCTURING_STYLES_KEY)
	public void removeFromStructuringStyles(FlexoDocStyle<D, TA> aStyle);

	@Finder(collection = STYLES_KEY, attribute = FlexoDocStyle.NAME_KEY)
	public FlexoDocStyle<D, TA> getStyleByName(String styleName);

	@Finder(collection = STYLES_KEY, attribute = FlexoDocStyle.STYLE_ID_KEY)
	public FlexoDocStyle<D, TA> getStyleByIdentifier(String styleId);

	/**
	 * Activate style identified by styleId
	 * 
	 * @param styleId
	 * @return
	 */
	public FlexoDocStyle<D, TA> activateStyle(String styleId);

	/**
	 * Return a collection of all identifiers of styles that can be activated for this document
	 * 
	 * @return
	 */
	public Collection<String> getKnownStyleIds();

	/**
	 * Add at the end of the document a paragraph with a single run containing supplied text. The paragraph is set to supplied style.
	 * 
	 * @param style
	 * @param text
	 * @return
	 */
	public FlexoDocParagraph<D, TA> addStyledParagraphOfText(FlexoDocStyle<D, TA> style, String text);

	/**
	 * Insert a specified index in the document a paragraph with a single run containing supplied text. The paragraph is set to supplied
	 * style.
	 * 
	 * @param style
	 * @param text
	 * @param index
	 * @return
	 */
	public FlexoDocParagraph<D, TA> insertStyledParagraphOfTextAtIndex(FlexoDocStyle<D, TA> style, String text, int index);

	/**
	 * Add at the end of the document a table presets with supplied number of rows and columns, with empty paragraphs inside each cell
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @return
	 */
	public FlexoDocTable<D, TA> addTable(int rows, int cols);

	/**
	 * Insert a specified index in the document a table presets with supplied number of rows and columns, with empty paragraphs inside each
	 * cell style.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @return
	 */
	public FlexoDocTable<D, TA> insertTableAtIndex(int rows, int cols, int index);

	/**
	 * Return fragment identified by start and end elements (inclusive)
	 * 
	 * @param startElement
	 * @param endElement
	 * @return
	 */
	public FlexoDocFragment<D, TA> getFragment(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
			throws FragmentConsistencyException;

	/**
	 * Return the {@link DocumentFactory} with which this document was built
	 * 
	 * @return
	 */
	public DocumentFactory<D, TA> getFactory();

	public String debugContents();

	public String debugStructuredContents();

	public static abstract class FlexoDocumentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			FlexoDocObjectImpl<D, TA> implements FlexoDocument<D, TA> {

		@Override
		public <E> List<E> getElements(Class<E> elementType) {
			List<E> returned = new ArrayList<E>();
			for (FlexoDocElement<D, TA> e : getElements()) {
				if (elementType.isAssignableFrom(e.getImplementedInterface())) {
					returned.add((E) e);
				}
			}
			return returned;
		}

		@Override
		public TA getTechnologyAdapter() {
			if (getResource() != null) {
				return ((TechnologyAdapterResource<D, TA>) getResource()).getTechnologyAdapter();
			}
			return null;
		}

		/**
		 * Return the URI of the {@link AbstractVirtualModel}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name >#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return (String) performSuperGetter(NAME_KEY);
		}

		@Override
		public void setName(String name) {
			if (requireChange(getName(), name)) {
				String oldValue = getName();
				if (getResource() != null) {
					try {
						getResource().setName(name);
						getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
					} catch (CannotRenameException e) {
						e.printStackTrace();
					}
				} else {
					performSuperSetter(NAME_KEY, name);
				}
			}
		}

		private List<FlexoDocElement<D, TA>> rootElements = null;

		/**
		 * Return the list of root elements of this document, which are infered to be root while interpreting the document as a structured
		 * document (see {@link #getStructuringStyles()})
		 * 
		 * @return
		 */
		@Override
		public List<FlexoDocElement<D, TA>> getRootElements() {
			if (rootElements == null) {
				rootElements = computeRootElements();
			}
			return rootElements;
		}

		@Override
		public void invalidateRootElements() {
			if (rootElements != null) {
				for (FlexoDocElement<D, TA> e : rootElements) {
					e.invalidateChildrenElements();
				}
			}
			rootElements = null;
		}

		protected boolean postponeRootElementChangedNotifications = false;

		@Override
		public void notifyRootElementsChanged() {
			if (!postponeRootElementChangedNotifications) {
				getPropertyChangeSupport().firePropertyChange(ROOT_ELEMENTS_KEY, null, getRootElements());
			}
		}

		private List<FlexoDocElement<D, TA>> computeRootElements() {
			List<FlexoDocElement<D, TA>> returned = new ArrayList<FlexoDocElement<D, TA>>();
			Integer l = null;
			for (FlexoDocElement<D, TA> e : getElements()) {
				if (l == null) {
					returned.add(e);
					if (e instanceof FlexoDocParagraph) {
						if (((FlexoDocParagraph<D, TA>) e).getStyle() != null && ((FlexoDocParagraph<D, TA>) e).getStyle().isLevelled()) {
							l = ((FlexoDocParagraph<D, TA>) e).getStyle().getLevel();
						}
					}
				} else {
					if (e instanceof FlexoDocParagraph) {
						if (((FlexoDocParagraph<D, TA>) e).getStyle() != null) {
							if (((FlexoDocParagraph<D, TA>) e).getStyle().getLevel().equals(l)) {
								returned.add(e);
							}
						}
					}
				}
			}
			return returned;
		}

		@Override
		public DocumentFactory<D, TA> getFactory() {

			if (getResource() != null) {
				return (DocumentFactory<D, TA>) ((PamelaResource<?, ?>) getResource()).getFactory();
			}
			return null;
		}

		@Override
		public FlexoDocFragment<D, TA> getFragment(FlexoDocElement<D, TA> startElement, FlexoDocElement<D, TA> endElement)
				throws FragmentConsistencyException {
			if (getFactory() != null) {
				return getFactory().getFragment(startElement, endElement);
			}
			return null;
		}

	}

}
