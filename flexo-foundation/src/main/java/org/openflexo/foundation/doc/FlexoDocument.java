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

import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * Generic abstract concept representing a text-based document (eg .docx, .odt, etc...)
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

	public static abstract class FlexoDocumentImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends
			FlexoDocObjectImpl<D, TA> implements FlexoDocument<D, TA> {
	}

}
