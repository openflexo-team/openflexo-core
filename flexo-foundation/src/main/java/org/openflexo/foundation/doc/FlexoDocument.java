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
import java.util.List;

import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.resource.CannotRenameException;
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
 * Such a document is not structured, as it contains only a list of {@link FlexoDocumentElement}, but a structuration can be dynamically
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
public interface FlexoDocument<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoDocObject<D, TA>, ResourceData<D> {

	@PropertyIdentifier(type = FlexoDocumentElement.class, cardinality = Cardinality.LIST)
	public static final String ELEMENTS_KEY = "elements";

	@PropertyIdentifier(type = FlexoStyle.class, cardinality = Cardinality.LIST)
	public static final String STYLES_KEY = "styles";

	@PropertyIdentifier(type = FlexoStyle.class, cardinality = Cardinality.LIST)
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
	 * Return the list of root elements of this document (elements like paragraphs or tables, sequentially composing the document)
	 * 
	 * @return
	 */
	@Getter(value = ELEMENTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoDocumentElement.DOCUMENT_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoDocumentElement<D, TA>> getElements();

	@Setter(ELEMENTS_KEY)
	public void setElements(List<FlexoDocumentElement<D, TA>> someElements);

	@Adder(ELEMENTS_KEY)
	@PastingPoint
	public void addToElements(FlexoDocumentElement<D, TA> anElement);

	@Remover(ELEMENTS_KEY)
	public void removeFromElements(FlexoDocumentElement<D, TA> anElement);

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
	public List<FlexoDocumentElement<D, TA>> getRootElements();

	/**
	 * Return the list of style used in this document
	 * 
	 * @return
	 */
	@Getter(value = STYLES_KEY, cardinality = Cardinality.LIST, inverse = FlexoStyle.DOCUMENT_KEY)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<FlexoStyle<D, TA>> getStyles();

	@Setter(STYLES_KEY)
	public void setStyles(List<FlexoStyle<D, TA>> someStyles);

	@Adder(STYLES_KEY)
	@PastingPoint
	public void addToStyles(FlexoStyle<D, TA> aStyle);

	@Remover(STYLES_KEY)
	public void removeFromStyles(FlexoStyle<D, TA> aStyle);

	/**
	 * Return an ordered list of styles to be used to present a structured document<br>
	 * 
	 * @return
	 */
	@Getter(value = STRUCTURING_STYLES_KEY, cardinality = Cardinality.LIST)
	public List<FlexoStyle<D, TA>> getStructuringStyles();

	@Setter(STRUCTURING_STYLES_KEY)
	public void setStructuringStyles(List<FlexoStyle<D, TA>> someStyles);

	@Adder(STRUCTURING_STYLES_KEY)
	public void addToStructuringStyles(FlexoStyle<D, TA> aStyle);

	@Remover(STRUCTURING_STYLES_KEY)
	public void removeFromStructuringStyles(FlexoStyle<D, TA> aStyle);

	@Finder(collection = STYLES_KEY, attribute = FlexoStyle.NAME_KEY)
	public FlexoStyle<D, TA> getStyleByName(String styleName);

	@Finder(collection = STYLES_KEY, attribute = FlexoStyle.STYLE_ID_KEY)
	public FlexoStyle<D, TA> getStyleByIdentifier(String styleId);

	public static abstract class FlexoDocumentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			FlexoDocObjectImpl<D, TA> implements FlexoDocument<D, TA> {

		@Override
		public <E> List<E> getElements(Class<E> elementType) {
			List<E> returned = new ArrayList<E>();
			for (FlexoDocumentElement<D, TA> e : getElements()) {
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

		private List<FlexoDocumentElement<D, TA>> rootElements = null;

		/**
		 * Return the list of root elements of this document, which are infered to be root while interpreting the document as a structured
		 * document (see {@link #getStructuringStyles()})
		 * 
		 * @return
		 */
		@Override
		public List<FlexoDocumentElement<D, TA>> getRootElements() {
			if (rootElements == null) {
				rootElements = computeRootElements();
				System.out.println("For document " + this);
				System.out.println("root elements are: " + rootElements);
			}
			return rootElements;
		}

		private List<FlexoDocumentElement<D, TA>> computeRootElements() {
			List<FlexoDocumentElement<D, TA>> returned = new ArrayList<FlexoDocumentElement<D, TA>>();
			Integer l = null;
			for (FlexoDocumentElement<D, TA> e : getElements()) {
				if (l == null) {
					returned.add(e);
					if (e instanceof FlexoParagraph) {
						if (((FlexoParagraph<D, TA>) e).getStyle() != null && ((FlexoParagraph<D, TA>) e).getStyle().isLevelled()) {
							l = ((FlexoParagraph<D, TA>) e).getStyle().getLevel();
						}
					}
				} else {
					if (e instanceof FlexoParagraph) {
						if (((FlexoParagraph<D, TA>) e).getStyle() != null) {
							if (((FlexoParagraph<D, TA>) e).getStyle().getLevel().equals(l)) {
								returned.add(e);
							}
						}
					}
				}
			}
			return returned;
		}
	}

}
