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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.fml.FlexoTableRole.FlexoTableRoleImpl;
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
import org.openflexo.toolbox.StringUtils;

/**
 * Implements {@link ActorReference} for {@link FlexoTableRole}.<br>
 * Represents the actual links in a given {@link FlexoDocument} connecting a template table to a generated table<br>
 * We need to store here the mapping:
 * <ul>
 * <li>between static rows (link between id of row in template table and id of row in generated table)</li>
 * <li>between dynamic rows (link between occurence of object from iteration and id of row in generated table)</li>
 * </ul>
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of referenced object (here this is a {@link FlexoDocTable})
 */
@ModelEntity
@ImplementationClass(TableActorReference.TableActorReferenceImpl.class)
@XMLElement
@FML("TableActorReference")
public interface TableActorReference<T extends FlexoDocTable<?, ?>> extends ActorReference<T> {

	@PropertyIdentifier(type = String.class)
	public static final String TABLE_IDENTIFIER_KEY = "tableIdentifier";
	@PropertyIdentifier(type = StaticRowReference.class, cardinality = Cardinality.LIST)
	public static final String STATIC_ROW_REFERENCES_KEY = "staticRowReferences";
	@PropertyIdentifier(type = DynamicRowReference.class, cardinality = Cardinality.LIST)
	public static final String DYNAMIC_ROW_REFERENCES_KEY = "dynamicRowReferences";

	@Getter(TABLE_IDENTIFIER_KEY)
	@XMLAttribute
	public String getTableIdentifier();

	@Setter(TABLE_IDENTIFIER_KEY)
	public void setTableIdentifier(String tableIdentifier);

	/**
	 * Return the list of static row references
	 * 
	 * @return
	 */
	@Getter(value = STATIC_ROW_REFERENCES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	@Embedded
	public List<StaticRowReference> getStaticRowReferences();

	@Setter(STATIC_ROW_REFERENCES_KEY)
	public void setStaticRowReferences(List<StaticRowReference> someReferences);

	@Adder(STATIC_ROW_REFERENCES_KEY)
	public void addToStaticRowReferences(StaticRowReference aReference);

	@Remover(STATIC_ROW_REFERENCES_KEY)
	public void removeFromStaticRowReferences(StaticRowReference aReference);

	/**
	 * Return the list of dynamic row references
	 * 
	 * @return
	 */
	@Getter(value = DYNAMIC_ROW_REFERENCES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	@Embedded
	public List<DynamicRowReference> getDynamicRowReferences();

	@Setter(DYNAMIC_ROW_REFERENCES_KEY)
	public void setDynamicRowReferences(List<DynamicRowReference> someReferences);

	@Adder(DYNAMIC_ROW_REFERENCES_KEY)
	public void addToDynamicRowReferences(DynamicRowReference aReference);

	@Remover(DYNAMIC_ROW_REFERENCES_KEY)
	public void removeFromDynamicRowReferences(DynamicRowReference aReference);

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

	public abstract static class TableActorReferenceImpl<T extends FlexoDocTable<?, ?>> extends ActorReferenceImpl<T>
			implements TableActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(TableActorReference.class.getPackage().toString());

		private T table;
		private String tableIdentifier;

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

		@Override
		public String getTableIdentifier() {
			if (table != null) {
				return table.getIdentifier();
			}
			return tableIdentifier;
		}

		@Override
		public void setTableIdentifier(String tableIdentifier) {
			this.tableIdentifier = tableIdentifier;
		}

		@Override
		public T getModellingElement(boolean forceLoading) {

			if (table == null) {
				FlexoDocument<?, ?> document = getFlexoDocument();
				if (document != null) {
					if (StringUtils.isNotEmpty(tableIdentifier)) {
						table = (T) document.getElementWithIdentifier(tableIdentifier);
					}
				}
			}

			return table;
		}

		@Override
		public void setModellingElement(T aTable) {

			if (aTable != table) {

				FlexoTableRole<T, ?, ?> tableRole = ((FlexoTableRole<T, ?, ?>) getFlexoRole());

				// First remove all existing static references
				if (table != null) {
					for (StaticRowReference r : new ArrayList<>(getStaticRowReferences())) {
						removeFromStaticRowReferences(r);
					}
					/*for (DynamicRowReference r : new ArrayList<DynamicRowReference>(getDynamicRowReferences())) {
						removeFromDynamicRowReferences(r);
					}*/
				}

				// Retrieve template table
				T templateTable = tableRole.getTable();

				if (aTable != null) {
					for (int i = 0; i < aTable.getTableRows().size(); i++) {
						FlexoDocTableRow<?, ?> generatedRow = aTable.getTableRows().get(i);
						if (generatedRow.getTableCells().size() > 0 && generatedRow.getTableCells().get(0).getElements().size() > 0) {
							FlexoDocElement<?, ?> generatedElement = generatedRow.getTableCells().get(0).getElements().get(0);
							if (StringUtils.isNotEmpty(generatedElement.getBaseIdentifier())) {
								FlexoDocElement<?, ?> templateParagraph = templateTable
										.getElementWithIdentifier(generatedElement.getBaseIdentifier());
								FlexoDocTableCell<?, ?> templateCell = (FlexoDocTableCell<?, ?>) templateParagraph.getContainer();
								FlexoDocTableRow<?, ?> templateRow = templateCell.getRow();
								if (templateRow != null) {
									if (templateRow.getIndex() < tableRole.getStartIterationIndex()
											|| templateRow.getIndex() > tableRole.getEndIterationIndex()) {
										// This means that we found a matching between the two rows, outside iteration area
										// we need to store that information as a StaticRowReference
										System.out.println("OK pour la ligne " + i + " je trouve " + templateRow.getIdentifier());
										StaticRowReference srr = getFactory().newInstance(StaticRowReference.class);
										srr.setRowId(generatedRow.getIdentifier());
										srr.setTemplateRowId(templateRow.getIdentifier());
										addToStaticRowReferences(srr);
										System.out.println(
												"OK j'associe " + generatedRow.getIdentifier() + " a " + templateRow.getIdentifier());
									}
								}
							}
						}
					}
				}
				else {
					logger.warning("INVESTIGATE: Setting a tableActorReference (" + this.getRoleName() + ") to NULL in Concept"
							+ this.getFlexoConceptInstance());
				}
				table = aTable;
			}
		}

		/**
		 * This method is called to extract a value from the federated data and apply it to the represented table representation
		 * 
		 */
		@Override
		public void applyDataToDocument() {

			FlexoTableRole<T, ?, ?> tableRole = (FlexoTableRole<T, ?, ?>) getFlexoRole();

			logger.info("Updating table");

			// First, we have to retrieve all rows

			List<Object> rowObjects = null;
			try {
				rowObjects = tableRole.getIteration().getBindingValue(getFlexoConceptInstance());
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			// First, we have to detect iteration range on generation target
			ObjectRowLookupResult lookup = lookupObjects();

			logger.info("Generate between " + lookup.startIterationRowIndex + " et " + lookup.endIterationRowIndex);

			int currentRowNumbers = lookup.endIterationRowIndex - lookup.startIterationRowIndex + 1;

			if (currentRowNumbers < rowObjects.size()) {
				// Some rows need to be added
				for (int i = 0; i < rowObjects.size() - currentRowNumbers; i++) {
					logger.info("Add row " + (i + lookup.endIterationRowIndex + 1));
					FlexoDocTableRow<?, ?> rowToClone = getModellingElement().getTableRows().get(i + lookup.endIterationRowIndex);
					FlexoDocTableRow<?, ?> clonedRow = (FlexoDocTableRow<?, ?>) rowToClone.cloneObject();
					for (int j = 0; j < clonedRow.getTableCells().size(); j++) {
						FlexoDocTableCell<?, ?> cell = clonedRow.getTableCells().get(j);
						for (int k = 0; k < cell.getElements().size(); k++) {
							FlexoDocElement<?, ?> element = cell.getElements().get(k);
							element.setIdentifier(element.getFlexoDocument().getFactory().generateId());
						}
					}

					getModellingElement().insertTableRowAtIndex((FlexoDocTableRow) clonedRow, (i + lookup.endIterationRowIndex + 1));
				}
			}

			if (currentRowNumbers > rowObjects.size()) {
				// Some rows need to be removed
				for (int i = 0; i < currentRowNumbers - rowObjects.size(); i++) {
					logger.info("Remove row at index " + (i + lookup.endIterationRowIndex + 1));
					getModellingElement().removeFromTableRows(
							(FlexoDocTableRow) getModellingElement().getTableRows().get(i + lookup.endIterationRowIndex));
				}
			}

			lookup.endIterationRowIndex = lookup.endIterationRowIndex + rowObjects.size() - currentRowNumbers;
			currentRowNumbers = rowObjects.size();

			if (getModellingElement() != null) {
				for (DynamicRowReference drr : new ArrayList<>(getDynamicRowReferences())) {
					removeFromDynamicRowReferences(drr);
				}
			}

			// At this point, we have the right number of rows

			int i = 0;
			for (final Object rowObject : rowObjects) {
				for (ColumnTableBinding<?, ?> ctb : tableRole.getColumnBindings()) {
					try {
						Object value = ctb.getValue().getBindingValue(new BindingEvaluationContext() {

							@Override
							public Object getValue(BindingVariable variable) {
								if (variable.getVariableName().equals(FlexoTableRoleImpl.ITERATOR_NAME)) {
									return rowObject;
								}
								return getFlexoConceptInstance().getValue(variable);
							}
						});
						FlexoDocTableCell<?, ?> cell = getModellingElement().getCell(i + lookup.startIterationRowIndex,
								ctb.getColumnIndex());
						cell.setRawText((String) value);

					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}

				DynamicRowReference drr = getFactory().newInstance(DynamicRowReference.class);
				drr.setRowId(getModellingElement().getTableRows().get(i + lookup.startIterationRowIndex).getIdentifier());
				drr.setIndex(i);
				addToDynamicRowReferences(drr);

				i++;

			}

		}

		/**
		 * Return list of rows in generated table matching row identified by supplied rowId
		 * 
		 * @param templateRowId
		 *            identifier of template row
		 * @return
		 */
		public List<FlexoDocTableRow<?, ?>> getRowsMatchingTemplateRowId(String templateRowId) {
			List<FlexoDocTableRow<?, ?>> returned = new ArrayList<>();
			for (StaticRowReference srr : getStaticRowReferences()) {
				if (srr.getTemplateRowId().equals(templateRowId)) {
					FlexoDocTableRow<?, ?> matchingRow = getModellingElement().getRowWithIdentifier(srr.getRowId());
					returned.add(matchingRow);
				}
			}
			return returned;
		}

		/**
		 * Return list of rows in generated table matching supplied row
		 * 
		 * @param templateRow
		 * @return
		 */
		public List<FlexoDocTableRow<?, ?>> getRowsMatchingTemplateRow(FlexoDocTableRow<?, ?> templateRow) {
			return getRowsMatchingTemplateRowId(templateRow.getIdentifier());
		}

		/**
		 * This method is called to extract a value from the table, and apply it to underlying federated data
		 * 
		 * @return
		 */
		@Override
		public void reinjectDataFromDocument() {

			FlexoTableRole<T, ?, ?> tableRole = (FlexoTableRole<T, ?, ?>) getFlexoRole();

			logger.info("DocXTable: reinjectDataFromDocument");

			// First, we have to retrieve all rows

			List<Object> rowObjects = null;
			try {
				rowObjects = tableRole.getIteration().getBindingValue(getFlexoConceptInstance());
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			System.out.println("rowObjects=" + rowObjects);
			System.out.println("getModellingElement()=" + getModellingElement());

			ObjectRowLookupResult lookup = lookupObjects();

			System.out.println("Du coup, on reinjecte entre " + lookup.startIterationRowIndex + " et " + lookup.endIterationRowIndex);

			List<Object> objectsToCallFromDeletion = new ArrayList<>(rowObjects);

			for (int rowIndex = lookup.startIterationRowIndex; rowIndex <= lookup.endIterationRowIndex; rowIndex++) {
				System.out.println("Row " + rowIndex);
				FlexoDocTableRow<?, ?> row = table.getTableRows().get(rowIndex);
				int objectIndex = expectedObjectIndexForRowId(row.getIdentifier());
				if (objectIndex == -1) {
					// Attempt to *guess*
					objectIndex = rowIndex - lookup.startIterationRowIndex;
				}
				if (objectIndex < rowObjects.size()) {
					final Object rowObject = rowObjects.get(objectIndex);
					System.out.println("Found a matching object : " + rowObject);

					for (ColumnTableBinding<?, ?> ctb : tableRole.getColumnBindings()) {
						FlexoDocTableCell<?, ?> cell = getModellingElement().getCell(rowIndex, ctb.getColumnIndex());
						String rawTextToReinject = cell.getRawText();

						try {
							ctb.getValue().setBindingValue(rawTextToReinject, new BindingEvaluationContext() {

								@Override
								public Object getValue(BindingVariable variable) {
									if (variable.getVariableName().equals(FlexoTableRoleImpl.ITERATOR_NAME)) {
										return rowObject;
									}
									return getFlexoConceptInstance().getValue(variable);
								}
							});

						} catch (TypeMismatchException e) {
							e.printStackTrace();
						} catch (NullReferenceException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NotSettableContextException e) {
							e.printStackTrace();
						}
					}

					objectsToCallFromDeletion.remove(rowObject);
				}
				else {
					// This appear to be a new object
					System.out.println("Consider create a new object from row");
				}
			}

			for (Object toDelete : objectsToCallFromDeletion) {
				System.out.println("Consider delete this object: " + toDelete);
			}

		}

		private int expectedObjectIndexForRowId(String rowId) {
			if (rowId == null) {
				return -1;
			}
			for (DynamicRowReference drr : getDynamicRowReferences()) {
				if (rowId.equals(drr.getRowId())) {
					return drr.getIndex();
				}
			}
			return -1;
		}

		class ObjectRowLookupResult {
			int startIterationRowIndex;
			int endIterationRowIndex;
		}

		private ObjectRowLookupResult lookupObjects() {
			ObjectRowLookupResult returned = new ObjectRowLookupResult();

			FlexoTableRole<T, ?, ?> tableRole = (FlexoTableRole<T, ?, ?>) getFlexoRole();

			// First, we have to detect iteration range on generation target
			returned.startIterationRowIndex = 0;
			returned.endIterationRowIndex = getModellingElement().getTableRows().size() - 1;

			// start iteration row index is computed from the last static reference before iteration area
			if (tableRole.getStartIterationIndex() > 0) {
				FlexoDocTableRow<?, ?> lastTemplateHeaderRow = tableRole.getTable().getTableRows()
						.get(tableRole.getStartIterationIndex() - 1);
				List<FlexoDocTableRow<?, ?>> lastHeaderRows = getRowsMatchingTemplateRow(lastTemplateHeaderRow);
				if (lastHeaderRows.size() > 0) {
					FlexoDocTableRow<?, ?> lastHeaderRow = lastHeaderRows.get(0);
					returned.startIterationRowIndex = lastHeaderRow.getIndex() + 1;
				}
			}
			// end iteration row index is computed from the last static reference after iteration area
			if (tableRole.getEndIterationIndex() > 0 && tableRole.getEndIterationIndex() < tableRole.getTable().getTableRows().size() - 1) {
				FlexoDocTableRow<?, ?> firstTemplateFooterRow = tableRole.getTable().getTableRows()
						.get(tableRole.getEndIterationIndex() + 1);
				List<FlexoDocTableRow<?, ?>> firstFooterRows = getRowsMatchingTemplateRow(firstTemplateFooterRow);
				if (firstFooterRows.size() > 0) {
					FlexoDocTableRow<?, ?> firstFooterRow = firstFooterRows.get(0);
					returned.endIterationRowIndex = firstFooterRow.getIndex() - 1;
				}
			}

			return returned;
		}

	}

	/**
	 * Used to store link between a row and its data
	 * 
	 * @author sylvain
	 *
	 */
	@ModelEntity(isAbstract = true)
	public interface AbstractRowReference {

		@PropertyIdentifier(type = String.class)
		public static final String ROW_IDENTIFIER_KEY = "rowId";

		@Getter(ROW_IDENTIFIER_KEY)
		@XMLAttribute
		public String getRowId();

		@Setter(ROW_IDENTIFIER_KEY)
		public void setRowId(String rowId);

	}

	/**
	 * Used to store link between occurence of iteration object with index of row in table
	 * 
	 * @author sylvain
	 *
	 */
	@ModelEntity
	@XMLElement
	public interface DynamicRowReference extends AbstractRowReference {

		@PropertyIdentifier(type = Integer.class)
		public static final String INDEX_KEY = "index";

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

	}

	/**
	 * Used to store link between occurence of iteration object with index of row in table
	 * 
	 * @author sylvain
	 *
	 */
	@ModelEntity
	@XMLElement
	public interface StaticRowReference extends AbstractRowReference {

		@PropertyIdentifier(type = String.class)
		public static final String TEMPLATE_ROW_IDENTIFIER_KEY = "templateRowId";

		@Getter(TEMPLATE_ROW_IDENTIFIER_KEY)
		@XMLAttribute
		public String getTemplateRowId();

		@Setter(TEMPLATE_ROW_IDENTIFIER_KEY)
		public void setTemplateRowId(String templateRowId);

	}

}
