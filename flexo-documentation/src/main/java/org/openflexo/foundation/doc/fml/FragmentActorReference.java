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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocFragment.FragmentConsistencyException;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
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
 * Implements {@link ActorReference} for {@link FlexoDocFragment}.<br>
 * Represents the actual links in a given {@link FlexoDocument} connecting a template fragment to a generated fragment<br>
 * We need to store here the bindings between elements in template and corresponding elements in referenced {@link FlexoDocument}
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
public interface FragmentActorReference<F extends FlexoDocFragment<?, ?>> extends ActorReference<F> {

	@PropertyIdentifier(type = ElementReference.class, cardinality = Cardinality.LIST)
	public static final String ELEMENT_REFERENCES_KEY = "elementReferences";

	/**
	 * Return the list of root elements of this document (elements like paragraphs or tables, sequentially composing the document)
	 * 
	 * @return
	 */
	@Getter(value = ELEMENT_REFERENCES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	@Embedded
	public List<ElementReference> getElementReferences();

	@Setter(ELEMENT_REFERENCES_KEY)
	public void setElementReferences(List<ElementReference> someElementReferences);

	@Adder(ELEMENT_REFERENCES_KEY)
	public void addToElementReferences(ElementReference anElementReference);

	@Remover(ELEMENT_REFERENCES_KEY)
	public void removeFromElementReferences(ElementReference anElementReference);

	/**
	 * Return list of elements in generated fragment matching element identified by supplied templateElementId
	 * 
	 * @param templateElementId
	 *            identifier of template element
	 * @return
	 */
	public List<FlexoDocElement<?, ?>> getElementsMatchingTemplateElementId(String templateElementId);

	/**
	 * Return list of elements in generated fragment matching supplied templateElement
	 * 
	 * @param templateElement
	 * @return
	 */
	public List<FlexoDocElement<?, ?>> getElementsMatchingTemplateElement(FlexoDocElement<?, ?> templateElement);

	public void removeReferencesTo(FlexoDocElement<?, ?> element);

	/**
	 * This method is called to extract a value from the federated data and apply it to the represented fragment representation
	 * 
	 */
	public void applyDataToDocument();

	/**
	 * This method is called to extract a value from the fragment, and apply it to underlying federated data
	 * 
	 * @return
	 */
	public void reinjectDataFromDocument();

	public abstract static class FragmentActorReferenceImpl<F extends FlexoDocFragment<?, ?>> extends ActorReferenceImpl<F>
			implements FragmentActorReference<F> {

		private static final Logger logger = FlexoLogger.getLogger(FragmentActorReference.class.getPackage().toString());

		private F fragment;

		/**
		 * Default constructor
		 */
		public FragmentActorReferenceImpl() {
			super();
		}

		public FlexoDocument<?, ?> getFlexoDocument() {
			ModelSlotInstance<?, ?> msInstance = getModelSlotInstance();
			if (msInstance != null && msInstance.getAccessedResourceData() != null) {
				return (FlexoDocument<?, ?>) msInstance.getAccessedResourceData();
			}
			return null;
		}

		@Override
		public F getModellingElement(boolean forceLoading) {

			if (fragment == null) {
				FlexoDocument<?, ?> document = getFlexoDocument();
				if (document != null) {
					if (getElementReferences().size() > 0) {
						FlexoDocElement startElement = null, endElement = null;
						int index = 0;
						for (ElementReference er : getElementReferences()) {
							FlexoDocElement<?, ?> element = document.getElementWithIdentifier(er.getElementId());
							if (element != null) {
								element.setBaseIdentifier(er.getTemplateElementId());
								if (index == 0) {
									startElement = element;
								}
								if (index == getElementReferences().size() - 1) {
									endElement = element;
								}
							}
							else {
								logger.warning("Could not find element matching " + er.getElementId());
							}
							index++;
						}
						try {
							fragment = (F) document.getFactory().makeFragment(startElement, endElement);
						} catch (FragmentConsistencyException e) {
							logger.warning("Could not build fragment");
							e.printStackTrace();
						}
					}
				}
				else {
					logger.warning("Could not access to document from model slot " + getModelSlotInstance());
				}
			}

			return fragment;
		}

		@Override
		public void setModellingElement(F aFragment) {

			if (aFragment != fragment) {

				// First remove all existing ElementReference occurences when it exists
				if (fragment != null) {
					for (ElementReference er : new ArrayList<>(getElementReferences())) {
						removeFromElementReferences(er);
					}
				}

				// Retrieve template fragment
				F templateFragment = (F) ((FlexoFragmentRole) getFlexoRole()).getFragment();

				if (aFragment != null) {
					for (FlexoDocElement<?, ?> element : aFragment.getElements()) {
						ElementReference er = getFactory().newInstance(ElementReference.class);
						er.setElementId(element.getIdentifier());
						if (element.getBaseIdentifier() != null) {
							er.setTemplateElementId(element.getBaseIdentifier());
						}
						addToElementReferences(er);
						if (element instanceof FlexoDocTable) {
							FlexoDocTable<?, ?> table = (FlexoDocTable<?, ?>) element;
							for (FlexoDocTableRow<?, ?> row : table.getTableRows()) {
								for (FlexoDocTableCell<?, ?> cell : row.getTableCells()) {
									for (FlexoDocElement<?, ?> e2 : cell.getElements()) {
										if (e2.getBaseIdentifier() != null) {
											ElementReference er2 = getFactory().newInstance(ElementReference.class);
											er2.setElementId(e2.getIdentifier());
											er2.setTemplateElementId(e2.getBaseIdentifier());
											addToElementReferences(er2);
										}
									}
								}
							}
						}
					}
				}
				fragment = aFragment;
			}
		}

		/**
		 * This method is called to extract a value from the federated data and apply it to the represented fragment representation
		 * 
		 */
		@Override
		public void applyDataToDocument() {
			for (TextBinding<?, ?> tb : ((FlexoFragmentRole<?, ?, ?>) getFlexoRole()).getTextBindings()) {
				tb.applyToFragment(getFlexoConceptInstance());
			}
		}

		/**
		 * This method is called to extract a value from the fragment, and apply it to underlying federated data
		 * 
		 * @return
		 */
		@Override
		public void reinjectDataFromDocument() {
			for (TextBinding<?, ?> tb : ((FlexoFragmentRole<?, ?, ?>) getFlexoRole()).getTextBindings()) {
				tb.extractFromFragment(getFlexoConceptInstance());
			}
		}

		/**
		 * Return list of elements in generated fragment matching element identified by supplied templateElementId
		 * 
		 * @param templateElementId
		 *            identifier of template element
		 * @return
		 */
		@Override
		public List<FlexoDocElement<?, ?>> getElementsMatchingTemplateElementId(String templateElementId) {
			List<FlexoDocElement<?, ?>> returned = new ArrayList<>();
			for (ElementReference er : getElementReferences()) {
				if (er.getTemplateElementId().equals(templateElementId)) {
					returned.add(getFlexoDocument().getElementWithIdentifier(er.getElementId()));
				}
			}
			return returned;
		}

		/**
		 * Return list of elements in generated fragment matching supplied templateElement
		 * 
		 * @param templateElement
		 * @return
		 */
		@Override
		public List<FlexoDocElement<?, ?>> getElementsMatchingTemplateElement(FlexoDocElement<?, ?> templateElement) {
			return getElementsMatchingTemplateElementId(templateElement.getIdentifier());
		}

		@Override
		public void removeReferencesTo(FlexoDocElement<?, ?> element) {
			List<ElementReference> referencesToRemove = new ArrayList<>();
			for (ElementReference er : getElementReferences()) {
				if (er.getElementId().equals(element.getIdentifier())) {
					referencesToRemove.add(er);
				}
			}
			for (ElementReference er : referencesToRemove) {
				removeFromElementReferences(er);
			}
		}

	}

	@ModelEntity
	@XMLElement
	public interface ElementReference {

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
