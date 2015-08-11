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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocumentFragment;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Implements {@link ActorReference} for {@link FlexoDocumentFragment}.<br>
 * We need to store here the bindings between elements in template and corresponding elements in referenced FlexoDocument
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object
 */
@ModelEntity
@ImplementationClass(FragmentActorReference.FragmentActorReferenceImpl.class)
@XMLElement
@FML("FragmentActorReference")
public interface FragmentActorReference<F extends FlexoDocumentFragment<?, ?>> extends ActorReference<F> {

	@PropertyIdentifier(type = ElementBinding.class, cardinality = Cardinality.LIST)
	public static final String ELEMENTS_BINDINGS_KEY = "elementBindings";

	/**
	 * Return the list of root elements of this document (elements like paragraphs or tables, sequentially composing the document)
	 * 
	 * @return
	 */
	@Getter(value = ELEMENTS_BINDINGS_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	@Embedded
	public List<ElementBinding> getElementBindings();

	@Setter(ELEMENTS_BINDINGS_KEY)
	public void setElements(List<ElementBinding> someElementBindings);

	@Adder(ELEMENTS_BINDINGS_KEY)
	public void addToElements(ElementBinding anElementBinding);

	@Remover(ELEMENTS_BINDINGS_KEY)
	public void removeFromElements(ElementBinding anElementBinding);

	public abstract static class FragmentActorReferenceImpl<F extends FlexoDocumentFragment<?, ?>> extends ActorReferenceImpl<F>
			implements FragmentActorReference<F> {

		private static final Logger logger = FlexoLogger.getLogger(FragmentActorReference.class.getPackage().toString());

		private F fragment;

		/**
		 * Default constructor
		 */
		public FragmentActorReferenceImpl() {
			super();
		}

		@Override
		public F getModellingElement() {
			/*if (object == null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				if (msInstance != null && msInstance.getAccessedResourceData() != null) {
					object = (T) msInstance.getModelSlot().retrieveObjectWithURI(msInstance, objectURI);
				} else {
					logger.warning("Could not access to model in model slot " + getModelSlotInstance());
				}
			}
			if (object == null) {
				logger.warning("Could not retrieve object " + objectURI);
			}
			return object;*/

			return null;
		}

		@Override
		public void setModellingElement(F aFragment) {

			F templateFragment = (F) ((FlexoDocumentFragmentRole) getFlexoRole()).getFragment();

			/*this.object = object;
			if (object != null && getModelSlotInstance() != null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
					objectURI = msInstance.getModelSlot().getURIForObject(msInstance, object);
			}*/

		}

		/*@Override
		public String getObjectURI() {
			if (object != null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				objectURI = msInstance.getModelSlot().getURIForObject(msInstance, object);
			}
			return objectURI;
		}
		
		@Override
		public void setObjectURI(String objectURI) {
			this.objectURI = objectURI;
		}*/

	}

	@ModelEntity
	@XMLElement
	public interface ElementBinding {

		@PropertyIdentifier(type = String.class)
		public static final String TEMPLATE_ELEMENT_IDENTIFIER_KEY = "templateElementId";
		@PropertyIdentifier(type = String.class)
		public static final String ELEMENT_IDENTIFIER_KEY = "elementId";

		@Getter(TEMPLATE_ELEMENT_IDENTIFIER_KEY)
		@XMLAttribute
		public String getTemplateElementId();

		@Setter(TEMPLATE_ELEMENT_IDENTIFIER_KEY)
		public void setTemplateElementId(String elementId);

		@Getter(ELEMENT_IDENTIFIER_KEY)
		@XMLAttribute
		public String getElementId();

		@Setter(ELEMENT_IDENTIFIER_KEY)
		public void setElementId(String elementId);

	}
}
