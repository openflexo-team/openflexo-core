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

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDocumentFragment;
import org.openflexo.foundation.doc.FlexoTable;
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
 * Implements {@link ActorReference} for {@link FlexoDocumentFragment}.<br>
 * Represents the actual links in a given {@link FlexoDocument} connecting a template table to a generated table<br>
 * We need to store here the bindings between elements in template and corresponding elements in referenced {@link FlexoDocument}
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object
 */
@ModelEntity
@ImplementationClass(TableActorReference.TableActorReferenceImpl.class)
@XMLElement
@FML("TableActorReference")
public interface TableActorReference<T extends FlexoTable<?, ?>> extends ActorReference<T> {

	@PropertyIdentifier(type = IterationElementReference.class, cardinality = Cardinality.LIST)
	public static final String ITERATION_ELEMENT_REFERENCES_KEY = "iterationElementReferences";

	/**
	 * Return the list of iteration element references
	 * 
	 * @return
	 */
	@Getter(value = ITERATION_ELEMENT_REFERENCES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	@Embedded
	public List<IterationElementReference> getElementReferences();

	@Setter(ITERATION_ELEMENT_REFERENCES_KEY)
	public void setIterationElementReferences(List<IterationElementReference> someElementReferences);

	@Adder(ITERATION_ELEMENT_REFERENCES_KEY)
	public void addToIterationElementReferences(IterationElementReference anElementReference);

	@Remover(ITERATION_ELEMENT_REFERENCES_KEY)
	public void removeFromIterationElementReferences(IterationElementReference anElementReference);

	/**
	 * This method is called to extract a value from the federated data and apply it to the represented table representation
	 * 
	 */
	public void applyDataToDocument();

	/**
	 * This method is called to extract a value from the table, and apply it to underlying federated data
	 * 
	 * @return
	 */
	public void reinjectDataFromDocument();

	public abstract static class TableActorReferenceImpl<T extends FlexoTable<?, ?>> extends ActorReferenceImpl<T> implements
			TableActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(TableActorReference.class.getPackage().toString());

		private T table;

		/**
		 * Default constructor
		 */
		public TableActorReferenceImpl() {
			super();
		}

		public FlexoDocument<?, ?> getFlexoDocument() {
			ModelSlotInstance<?, ?> msInstance = getModelSlotInstance();
			if (msInstance != null && msInstance.getAccessedResourceData() != null) {
				return (FlexoDocument<?, ?>) msInstance.getAccessedResourceData();
			}
			return null;
		}

		/*@Override
		public T getModellingElement() {

			if (table == null) {
				FlexoDocument<?, ?> document = getFlexoDocument();
				if (document != null) {
					if (getElementReferences().size() > 0) {
						FlexoDocumentElement startElement = null, endElement = null;
						int index = 0;
						for (ElementReference er : getElementReferences()) {
							FlexoDocumentElement element = document.getElementWithIdentifier(er.getElementId());
							element.setBaseIdentifier(er.getTemplateElementId());
							if (index == 0) {
								startElement = element;
							} else if (index == getElementReferences().size() - 1) {
								endElement = element;
							}
							index++;
						}
						try {
							table = (F) document.getFactory().makeFragment(startElement, endElement);
						} catch (FragmentConsistencyException e) {
							logger.warning("Could not build table");
							e.printStackTrace();
						}
					}
				} else {
					logger.warning("Could not access to document from model slot " + getModelSlotInstance());
				}
			}

			return table;
		}

		@Override
		public void setModellingElement(T aTable) {

			if (aTable != table) {

				// First remove all existing ElementReference occurences when it exists
				if (table != null) {
					for (ElementReference er : new ArrayList<ElementReference>(getElementReferences())) {
						removeFromElementReferences(er);
					}
				}

				// Retrieve template table
				F templateFragment = (F) ((FlexoFragmentRole) getFlexoRole()).getFragment();

				for (FlexoDocumentElement<?, ?> element : aTable.getElements()) {
					ElementReference er = getFactory().newInstance(ElementReference.class);
					er.setElementId(element.getIdentifier());
					if (element.getBaseIdentifier() != null) {
						er.setTemplateElementId(element.getBaseIdentifier());
					}
					addToElementReferences(er);
					if (element instanceof FlexoTable) {
						FlexoTable<?, ?> table = (FlexoTable<?, ?>) element;
						for (FlexoTableRow<?, ?> row : table.getTableRows()) {
							for (FlexoTableCell<?, ?> cell : row.getTableCells()) {
								for (FlexoDocumentElement<?, ?> e2 : cell.getElements()) {
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

				table = aTable;
			}
		}*/

		/**
		 * This method is called to extract a value from the federated data and apply it to the represented table representation
		 * 
		 */
		@Override
		public void applyDataToDocument() {
			for (TextBinding tb : ((FlexoFragmentRole<?, ?, ?>) getFlexoRole()).getTextBindings()) {
				tb.applyToFragment(getFlexoConceptInstance());
			}
		}

		/**
		 * This method is called to extract a value from the table, and apply it to underlying federated data
		 * 
		 * @return
		 */
		@Override
		public void reinjectDataFromDocument() {
			for (TextBinding tb : ((FlexoFragmentRole<?, ?, ?>) getFlexoRole()).getTextBindings()) {
				tb.extractFromFragment(getFlexoConceptInstance());
			}
		}

	}

	@ModelEntity
	@XMLElement
	public interface IterationElementReference {

		@PropertyIdentifier(type = Integer.class)
		public static final String INDEX_KEY = "index";
		@PropertyIdentifier(type = Integer.class)
		public static final String ROW_INDEX_KEY = "rowIndex";

		/**
		 * Index of iterated object as it has been appeared in iteration
		 * 
		 * @return
		 */
		@Getter(value = INDEX_KEY, defaultValue = "-1")
		@XMLAttribute
		public int getIndex();

		@Setter(INDEX_KEY)
		public void setIndex(int index);

		@Getter(value = ROW_INDEX_KEY, defaultValue = "-1")
		@XMLAttribute
		public int getRowIndex();

		@Setter(ROW_INDEX_KEY)
		public void setRowIndex(int rowIndex);

	}

}
