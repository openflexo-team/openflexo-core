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
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
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
 * Widget allowing to select an {@link FlexoDrawingRun} inside a {@link FlexoDocument}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBDocImageSelector<T extends FlexoDrawingRun<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends FIBFlexoObjectSelector<T> {
	static final Logger logger = Logger.getLogger(FIBDocImageSelector.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBDocImageSelector.fib");

	private D document;

	public FIBDocImageSelector(T editedObject) {
		super(editedObject);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<T> getRepresentedType() {
		return (Class) FlexoDrawingRun.class;
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

	private boolean isSelecting = false;

	@Override
	protected ImageSelectorDetailsPanel makeCustomPanel(T editedObject) {
		ImageSelectorDetailsPanel returned = null;

		if (getServiceManager() != null && getServiceManager().getTaskManager() != null) {
			LoadEditor task = new LoadEditor(editedObject);
			getServiceManager().getTaskManager().scheduleExecution(task);
			getServiceManager().getTaskManager().waitTask(task);
			returned = task.getPanel();
		}
		else {
			returned = new ImageSelectorDetailsPanel(editedObject);
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

				if (startDocObject instanceof FlexoDrawingRun) {
					isSelecting = true;
					setSelectedObject(startDocObject);
					isSelecting = false;
				}

			}
		});

		return returned;
	}

	@Override
	protected ImageSelectorFIBController makeCustomFIBController(FIBComponent fibComponent) {
		return new ImageSelectorFIBController(fibComponent, this);
	}

	@Override
	protected ImageSelectorFIBController getController() {
		return (ImageSelectorFIBController) super.getController();
	}

	@Override
	public ImageSelectorDetailsPanel getCustomPanel() {
		return (ImageSelectorDetailsPanel) super.getCustomPanel();
	}

	@Override
	public void setSelectedObject(Object selectedObject) {
		super.setSelectedObject(selectedObject);
		ImageSelectorDetailsPanel customPanel = (ImageSelectorDetailsPanel) getCustomPanel(false);
		if (customPanel != null) {
			selectImageInDocumentEditor((T) selectedObject, customPanel.getDocEditorWidget());
		}
	}

	public static class ImageSelectorFIBController extends SelectorFIBController {
		public ImageSelectorFIBController(final FIBComponent component, final FIBDocImageSelector selector) {
			super(component, selector);
		}

		public boolean containsDrawingRun(FlexoDocParagraph<?, ?> paragraph) {
			if (paragraph.getChildrenElements().size() > 0) {
				return true;
			}
			for (FlexoDocRun<?, ?> run : paragraph.getRuns()) {
				if (run instanceof FlexoDrawingRun) {
					return true;
				}
			}
			return false;
		}

		public boolean isSingleDrawingRun(FlexoDocParagraph<?, ?> paragraph) {
			return (paragraph.getDrawingRuns().size() == 1);
		}

	}

	public class ImageSelectorDetailsPanel extends SelectorDetailsPanel {

		private FIBCustomWidget<?, ?, ?> docEditorWidget = null;

		protected ImageSelectorDetailsPanel(T anObject) {
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
		public ImageSelectorFIBController getController() {
			return (ImageSelectorFIBController) super.getController();
		}

		@Override
		protected void selectValue(T value) {

			/*FIBBrowserWidget browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				// Force reselect value because tree may have been recomputed
				browserWidget.setSelected(value);
			}*/

			setSelectedValue(value);

			JFIBBrowserWidget<T> browserWidget = getFIBBrowserWidget();
			if (browserWidget != null) {
				if (value == null) {
					browserWidget.clearSelection();
				}
				else {
					browserWidget.setSelected(value);
				}
			}

			selectImageInDocumentEditor(value, getDocEditorWidget());
		}
	}

	protected void selectImageInDocumentEditor(T drawingRun, FIBCustomWidget<?, ?, ?> documentEditorWidget) {
		// System.out.println("customPanel" + getCustomPanel());
		// System.out.println("docEditorWidget=" + getCustomPanel().getDocEditorWidget());

		final FlexoDocumentEditorWidget docXEditor = (FlexoDocumentEditorWidget) documentEditorWidget.getCustomComponent();

		try {

			if (drawingRun != null) {

				docXEditor.getEditor().highlight(drawingRun);
				scrollTo(drawingRun.getParagraph(), docXEditor);
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

		private final T drawingRun;
		private ImageSelectorDetailsPanel panel;

		public LoadEditor(T drawingRun) {
			super("DocImageSelector", FlexoLocalization.getMainLocalizer().localizedForKey("opening_document_editor"));
			this.drawingRun = drawingRun;
		}

		@Override
		public void performTask() throws InterruptedException {
			setExpectedProgressSteps(10);
			panel = new ImageSelectorDetailsPanel(drawingRun);
		}

		public ImageSelectorDetailsPanel getPanel() {
			return panel;
		}
	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject == null) {
			return "";
		}
		return editedObject.getImageName();
	}
}
