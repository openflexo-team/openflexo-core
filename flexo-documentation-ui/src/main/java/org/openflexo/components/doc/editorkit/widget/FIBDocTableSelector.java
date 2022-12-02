/**
 * 
 * Copyright (c) 2014-2017, Openflexo
 * 
 * This file is part of Flexo-Documentation-UI, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Please not that some parts of that component are freely inspired from
 * Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
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

package org.openflexo.components.doc.editorkit.widget;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.text.Element;

import org.openflexo.components.doc.editorkit.FlexoDocumentEditorWidget;
import org.openflexo.components.doc.editorkit.FlexoStyledDocument;
import org.openflexo.components.doc.editorkit.element.AbstractDocumentElement;
import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocObject;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.gina.view.widget.FIBCustomWidget;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select an {@link FlexoDocTable} inside a {@link FlexoDocument}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBDocTableSelector<T extends FlexoDocTable<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FIBFlexoObjectSelector<T> {
	static final Logger logger = Logger.getLogger(FIBDocTableSelector.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBDocTableSelector.fib");

	private D document;

	public FIBDocTableSelector(T editedObject) {
		super(editedObject);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<T> getRepresentedType() {
		return (Class) FlexoDocTable.class;
	}

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

	private boolean isSelecting = false;

	@Override
	protected TableSelectorDetailsPanel makeCustomPanel(T editedObject) {

		TableSelectorDetailsPanel returned = null;

		if (getServiceManager() != null && getServiceManager().getTaskManager() != null) {
			LoadEditor task = new LoadEditor(editedObject);
			getServiceManager().getTaskManager().scheduleExecution(task);
			getServiceManager().getTaskManager().waitTask(task);
			returned = task.getPanel();
		}
		else {
			returned = new TableSelectorDetailsPanel(editedObject);
		}

		FIBCustomWidget<?, ?, ?> documentEditorWidget = returned.getDocEditorWidget();
		FlexoDocumentEditorWidget<?, ?> docXEditor = (FlexoDocumentEditorWidget<?, ?>) documentEditorWidget.getCustomComponent();
		docXEditor.getJEditorPane().addCaretListener(new FlexoDocumentEditorWidget.FlexoDocumentSelectionListener(docXEditor) {
			@Override
			public void caretUpdate(CaretEvent evt) {

				if (isSelecting) {
					return;
				}

				super.caretUpdate(evt);

				System.out.println("Caret changed with " + evt);
				int start = Math.min(evt.getDot(), evt.getMark());
				int end = Math.max(evt.getDot(), evt.getMark());
				System.out.println("Selection: " + start + ":" + end);

				// Better ???
				// int startLocation = getEditor().getJEditorPane().getSelectionStart();
				// int endLocation = getEditor().getJEditorPane().getSelectionEnd();

				// If selection is not empty, reduce the selection to be sure to be in a not implied run
				if (start > end) {
					end = end - 1;
				}

				FlexoStyledDocument<?, ?> styledDocument = docXEditor.getEditor().getStyledDocument();

				Element startCharElement = styledDocument.getCharacterElement(start);
				Element endCharElement = styledDocument.getCharacterElement(end);

				FlexoDocObject<?, ?> startDocObject = null;
				// Unused FlexoDocObject<?, ?> endDocObject = null;

				if (startCharElement instanceof AbstractDocumentElement
						&& ((AbstractDocumentElement<?, ?, ?>) startCharElement).getDocObject() instanceof FlexoDocElement) {
					startDocObject = ((AbstractDocumentElement<?, ?, ?>) startCharElement).getDocObject();
				}
				// Unused if (endCharElement instanceof AbstractDocumentElement
				// Unused && ((AbstractDocumentElement<?, ?, ?>) endCharElement).getDocObject() instanceof FlexoDocElement) {
				// Unused endDocObject = ((AbstractDocumentElement<?, ?, ?>) endCharElement).getDocObject();
				// Unused }

				System.out.println("Pour l'element: " + startCharElement);
				System.out.println("Pour l'element par: " + styledDocument.getParagraphElement(start));
				System.out.println("On detecte " + startDocObject);

				if (startDocObject instanceof FlexoDocTable) {
					isSelecting = true;
					setSelectedObject(startDocObject);
					isSelecting = false;
				}

			}
		});

		return returned;

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
		super.setSelectedObject(selectedObject);
		TableSelectorDetailsPanel customPanel = (TableSelectorDetailsPanel) getCustomPanel(false);
		if (customPanel != null && selectedObject instanceof FlexoDocTable) {
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

		private FIBCustomWidget<?, ?, ?> docEditorWidget = null;

		protected TableSelectorDetailsPanel(T anObject) {
			super(anObject);
		}

		public FIBCustomWidget<?, ?, ?> getDocEditorWidget() {
			if (docEditorWidget == null) {
				docEditorWidget = retrieveDocEditorWidget();
			}
			return docEditorWidget;
		}

		private FIBCustomWidget<?, ?, ?> retrieveDocEditorWidget() {
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
	}

	protected void selectTableInDocumentEditor(T table, FIBCustomWidget<?, ?, ?> documentEditorWidget) {

		// System.out.println("customPanel" + getCustomPanel());
		// System.out.println("docEditorWidget=" + getCustomPanel().getDocEditorWidget());

		final FlexoDocumentEditorWidget docXEditor = (FlexoDocumentEditorWidget) documentEditorWidget.getCustomComponent();

		try {

			if (table != null) {

				docXEditor.getEditor().highlight(table);
				scrollTo(table, docXEditor);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		docXEditor.getJEditorPane().revalidate();
		docXEditor.getJEditorPane().repaint();

	}

	private void scrollTo(FlexoDocObject object, FlexoDocumentEditorWidget docXEditor) {
		if (!docXEditor.getEditor().scrollTo(object, false)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					scrollTo(object, docXEditor);
				}
			});
		}
	}

	public class LoadEditor extends FlexoTask {

		private final T table;
		private TableSelectorDetailsPanel panel;

		public LoadEditor(T table) {
			super("DocTableSelector", FlexoLocalization.getMainLocalizer().localizedForKey("opening_document_editor"));
			this.table = table;
		}

		@Override
		public void performTask() throws InterruptedException {
			setExpectedProgressSteps(10);
			panel = new TableSelectorDetailsPanel(table);
		}

		public TableSelectorDetailsPanel getPanel() {
			return panel;
		}
	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject == null) {
			return "";
		}
		return "<Table>";
	}
}
