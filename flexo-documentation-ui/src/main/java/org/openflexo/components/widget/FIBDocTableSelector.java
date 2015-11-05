/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.components.widget;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBCustom;
import org.openflexo.fib.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.fib.view.widget.FIBCustomWidget;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.rm.Resource;

/**
 * Widget allowing to select an {@link FlexoDocTable} inside a {@link FlexoDocument}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBDocTableSelector<T extends FlexoDocTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FIBFlexoObjectSelector<T> {
	static final Logger logger = Logger.getLogger(FIBDocTableSelector.class.getPackage().getName());

	private D document;

	public FIBDocTableSelector(T editedObject) {
		super(editedObject);
	}

	@Override
	public abstract Resource getFIBResource();

	@Override
	public abstract Class<T> getRepresentedType();

	public D getDocument() {
		return document;
	}

	public void setDocument(D document) {
		if ((document == null && this.document != null) || (document != null && !document.equals(this.document))) {
			D oldValue = this.document;
			this.document = document;
			getPropertyChangeSupport().firePropertyChange("document", oldValue, document);
		}
	}

	public String renderedString(D editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	@Override
	protected TableSelectorDetailsPanel makeCustomPanel(T editedObject) {
		return new TableSelectorDetailsPanel(editedObject);
	}

	@Override
	protected TableSelectorFIBController makeCustomFIBController(FIBComponent fibComponent) {
		return new TableSelectorFIBController(fibComponent, this);
	}

	@Override
	protected TableSelectorFIBController getController() {
		return (TableSelectorFIBController) super.getController();
	}

	@Override
	public TableSelectorDetailsPanel getCustomPanel() {
		return (TableSelectorDetailsPanel) super.getCustomPanel();
	}

	@Override
	public void setSelectedObject(Object selectedObject) {
		// TODO Auto-generated method stub
		super.setSelectedObject(selectedObject);
		TableSelectorDetailsPanel customPanel = (TableSelectorDetailsPanel) getCustomPanel(false);
		if (customPanel != null) {
			selectTableInDocumentEditor((T) selectedObject, customPanel.getDocEditorWidget());
		}
	}

	public static class TableSelectorFIBController extends SelectorFIBController {
		public TableSelectorFIBController(final FIBComponent component, final FIBDocTableSelector selector) {
			super(component, selector);
			/*addSelectionListener(new FIBSelectionListener() {
				@Override
				public void selectionChanged(List<Object> selection) {
					List<FlexoDocElement<?, ?>> elements = new ArrayList<>();
					FlexoDocument<?, ?> doc = null;
					if (selection != null) {
						for (Object o : selection) {
							if (o instanceof FlexoDocElement && ((FlexoDocElement) o).getFlexoDocument() != null) {
								if (doc == null) {
									doc = ((FlexoDocElement) o).getFlexoDocument();
								}
								if (doc == ((FlexoDocElement) o).getFlexoDocument()) {
									elements.add((FlexoDocElement<?, ?>) o);
								}
							}
						}
					}
					final FlexoDocument<?, ?> docReference = doc;
					Collections.sort(elements, new Comparator<FlexoDocElement>() {
						@Override
						public int compare(FlexoDocElement o1, FlexoDocElement o2) {
							return docReference.getElements().indexOf(o1) - docReference.getElements().indexOf(o2);
						}
					});
					selector.updateWith(elements);
				}
			});*/
		}
	}

	public class TableSelectorDetailsPanel extends SelectorDetailsPanel {

		private FIBCustomWidget<?, ?> docEditorWidget = null;

		protected TableSelectorDetailsPanel(T anObject) {
			super(anObject);
		}

		public FIBCustomWidget<?, ?> getDocEditorWidget() {
			if (docEditorWidget == null) {
				docEditorWidget = retrieveDocEditorWidget();
			}
			return docEditorWidget;
		}

		private FIBCustomWidget<?, ?> retrieveDocEditorWidget() {
			List<FIBComponent> listComponent = getFIBComponent().getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBCustom) {
					return (FIBCustomWidget) getController().viewForComponent(c);
				}
			}
			return null;
		}

		@Override
		public TableSelectorFIBController getController() {
			return (TableSelectorFIBController) super.getController();
		}

		@Override
		protected void selectValue(T value) {

			/*FIBBrowserWidget browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				// Force reselect value because tree may have been recomputed
				browserWidget.setSelected(value);
			}*/

			setSelectedValue(value);

			JFIBBrowserWidget browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				if (value == null) {
					browserWidget.clearSelection();
				}
				else {
					browserWidget.setSelected(value);
				}
			}

			selectTableInDocumentEditor(value, getDocEditorWidget());
		}

		@Override
		public void update() {
			// TODO Auto-generated method stub
			super.update();
		}

	}

	protected void selectTableInDocumentEditor(T table, FIBCustomWidget<?, ?> documentEditorWidget) {

	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject == null) {
			return "";
		}
		return "<Table>";
	}
}
