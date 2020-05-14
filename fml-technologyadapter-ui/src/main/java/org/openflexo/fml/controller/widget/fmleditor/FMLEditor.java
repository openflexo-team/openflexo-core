/**
 * 
 * Copyright (c) 2020, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to edit a {@link FMLCompilationUnit} using FML textual syntax
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLEditor extends JPanel implements PropertyChangeListener, DocumentListener {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/fml", "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	private final CompilationUnitResource fmlResource;

	private FMLRSyntaxTextArea textArea;
	private Gutter gutter;
	private TextFinderPanel finderToolbar;

	private FMLEditorParser parser;

	public FMLEditor(CompilationUnitResource fmlResource) {
		super(new BorderLayout());

		this.fmlResource = fmlResource;

		textArea = new FMLRSyntaxTextArea(this);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);

		textArea.getDocument().addDocumentListener(this);

		try {
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
			Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml"));
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
			// Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/vs.xml"));
			theme.apply(textArea);
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

		try {
			fmlResource.loadResourceData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FlexoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		textArea.setText(fmlResource.getLoadedResourceData().getFMLPrettyPrint());

		fmlResource.getLoadedResourceData().getPropertyChangeSupport().addPropertyChangeListener(this);

		RTextScrollPane sp = new RTextScrollPane(textArea);
		((RSyntaxTextArea) sp.getTextArea()).setSyntaxEditingStyle("text/fml");
		add(sp, BorderLayout.CENTER);

		finderToolbar = new TextFinderPanel(this);
		add(finderToolbar, BorderLayout.SOUTH);

		gutter = sp.getGutter();
		// gutter.setBookmarkIcon(new ImageIcon("bookmark.png"));
		// gutter.setBookmarkIcon(IconLibrary.FIXABLE_ERROR_ICON);
		gutter.setBookmarkingEnabled(true);
		// gutter.setFoldIndicatorEnabled(true);
		// gutter.setFoldIcons(IconLibrary.NAVIGATION_BACKWARD_ICON, IconLibrary.NAVIGATION_FORWARD_ICON);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		add(errorStrip, BorderLayout.LINE_END);

		parser = new FMLEditorParser(this);// new XmlParser();
		textArea.addParser(parser);
		// p.parse(textArea.getD, style)

		/*try {
			gutter.addLineTrackingIcon(0, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(1, IconLibrary.FIXABLE_ERROR_ICON);
			gutter.addLineTrackingIcon(3, IconLibrary.FIXABLE_ERROR_ICON);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/

		// textArea.getDocument().addDocumentListener(this);

		// RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
		// String style = textArea.getSyntaxEditingStyle();
		// ParseResult res = p.parse(doc, style);
		// System.out.println("res=" + res.getNotices());

		/*RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter) textArea.getHighlighter();
		
		for (ParserNotice notice : res.getNotices()) {
			HighlightInfo highlight = null;
			highlight = h.addParserHighlight(notice, painter1);
		}*/

		/*doc.readLock();
		try {
		for (int i=0; i<parserCount; i++) {
		Parser parser = getParser(i);
		if (parser.isEnabled()) {
			ParseResult res = parser.parse(doc, style);
			addParserNoticeHighlights(res);
		}
		else {
			clearParserNoticeHighlights(parser);
		}
		}
		textArea.fireParserNoticesChange();
		} finally {
		doc.readUnlock();
		}*/

		/*setContentPane(cp);
		setTitle("Find and Replace Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);*/

	}

	public void delete() {
		logger.warning("delete() not implemented for FMLEditor");
	}

	public CompilationUnitResource getFMLResource() {
		return fmlResource;
	}

	public FlexoServiceManager getServiceManager() {
		if (fmlResource != null) {
			return fmlResource.getServiceManager();
		}
		return null;
	}

	public LocalizedDelegate getFlexoLocalizer() {
		if (getServiceManager() != null) {
			return getServiceManager().getLocalizationService().getFlexoLocalizer();
		}
		return null;
	}

	public LocalizedDelegate getFMLLocalizer() {
		if (getFMLTechnologyAdapterController() != null) {
			getFMLTechnologyAdapterController().getLocales();
		}
		return null;
	}

	public Gutter getGutter() {
		return gutter;
	}

	public FMLModelFactory getFactory() {
		return fmlResource.getFactory();
	}

	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	public List<ParserNotice> getParserNotices(int line) {
		return parser.getParserNotices(line);
	}

	public FMLTechnologyAdapterController getFMLTechnologyAdapterController() {
		if (fmlResource != null) {
			TechnologyAdapterService taService = fmlResource.getServiceManager().getService(TechnologyAdapterService.class);
			TechnologyAdapterControllerService tacService = fmlResource.getServiceManager()
					.getService(TechnologyAdapterControllerService.class);
			FMLTechnologyAdapter fmlTA = taService.getTechnologyAdapter(FMLTechnologyAdapter.class);
			return tacService.getTechnologyAdapterController(fmlTA);
		}
		return null;
	}

	// private boolean isUpdatingText = false;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (modelWillChange) {
			return;
		}
		/*if (isUpdatingText) {
			return;
		}*/
		if (evt.getPropertyName().equals("FMLPrettyPrint")) {
			System.out.println("Received " + evt);
			if (SwingUtilities.isEventDispatchThread()) {
				updateFMLAsText();
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateFMLAsText();
					}
				});
			}
		}
	}

	private void updateFMLAsText() {
		// isUpdatingText = true;
		// parser.fmlWillChange();
		try {
			getTextArea().setText(fmlResource.getCompilationUnit().getFMLPrettyPrint());
		} finally {
			// parser.fmlHasChanged();
			// isUpdatingText = false;
			// documentModified = false;
		}
	}

	private boolean modelWillChange = false;

	protected void modelWillChange() {
		modelWillChange = true;
	}

	/*protected void modelHasChanged(boolean successfullParsing) {
		String fmlPrettyPrint = fmlResource.getCompilationUnit().getFMLPrettyPrint().trim();
		modelWillChange = false;
		if (successfullParsing) {
			if (!getTextArea().getText().trim().equals(fmlPrettyPrint)) {
				// Found different FML
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						System.out.println("Updating FML....");
						parser.fmlWillChange();
						getTextArea().setText(fmlPrettyPrint);
						parser.fmlHasChanged();
						documentModified = false;
					}
				});
			}
		}
		documentModified = false;
	}*/

	protected void modelHasChanged(boolean requiresNewPrettyPrint) {
		modelWillChange = false;
		if (requiresNewPrettyPrint) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateFMLAsText();
				}
			});
		}
		// documentModified = false;
	}

	private boolean documentModified = false;

	public boolean isDocumentModified() {
		return documentModified;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentModified = true;
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentModified = true;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		documentModified = true;
	}

}
