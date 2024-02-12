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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.controller.widget.FIBCompilationUnitDetailedBrowser;
import org.openflexo.fml.controller.widget.FMLValidationPanel;
import org.openflexo.fml.rstasupport.FMLLanguageSupport;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.swing.layout.JXMultiSplitPane;
import org.openflexo.swing.layout.MultiSplitLayout;
import org.openflexo.swing.layout.MultiSplitLayoutFactory;
import org.openflexo.swing.layout.JXMultiSplitPane.DividerPainter;
import org.openflexo.swing.layout.MultiSplitLayout.Divider;
import org.openflexo.swing.layout.MultiSplitLayout.Leaf;
import org.openflexo.swing.layout.MultiSplitLayout.Split;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to edit a {@link FMLCompilationUnit} using FML textual syntax
 * 
 * This widget is build with a {@link FMLRSyntaxTextArea} presenting source code, and is augmented with a browser, a validation panel, and
 * search features
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLEditor extends JPanel implements PropertyChangeListener, HyperlinkListener {

	static final Logger logger = Logger.getLogger(FMLEditor.class.getPackage().getName());

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping(FMLLanguageSupport.SYNTAX_STYLE_FML, "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	private final CompilationUnitResource fmlResource;

	private FMLRSyntaxTextArea textArea;
	private RTextScrollPane scrollPane;
	// Vertical band at left where errors are shown
	private Gutter gutter;
	// Vertical band at right where errors are shown
	private ErrorStrip errorStrip;
	private TextFinderPanel finderToolbar;

	private FMLEditorParser parser;

	private JXMultiSplitPane centerPanel;
	private MultiSplitLayout centerLayout;

	private FlexoController flexoController;

	FIBCompilationUnitDetailedBrowser browser;
	FMLValidationPanel validationPanel;

	public FMLEditor(CompilationUnitResource fmlResource, FlexoController flexoController) {
		super(new BorderLayout());

		this.flexoController = flexoController;

		Split<?> defaultLayout = getDefaultLayout();

		MultiSplitLayout centerLayout = new MultiSplitLayout(true, MSL_FACTORY);
		centerLayout.setLayoutMode(MultiSplitLayout.NO_MIN_SIZE_LAYOUT);
		centerLayout.setModel(defaultLayout);

		JXMultiSplitPane splitPanel = new JXMultiSplitPane(centerLayout);
		splitPanel.setDividerSize(DIVIDER_SIZE);
		splitPanel.setDividerPainter(new DividerPainter() {

			@Override
			protected void doPaint(Graphics2D g, Divider divider, int width, int height) {
				if (!divider.isVisible()) {
					return;
				}
				if (divider.isVertical()) {
					int x = (width - KNOB_SIZE) / 2;
					int y = (height - DIVIDER_KNOB_SIZE) / 2;
					for (int i = 0; i < 3; i++) {
						Graphics2D graph = (Graphics2D) g.create(x, y + i * (KNOB_SIZE + KNOB_SPACE), KNOB_SIZE + 1, KNOB_SIZE + 1);
						graph.setPaint(KNOB_PAINTER);
						graph.fillOval(0, 0, KNOB_SIZE, KNOB_SIZE);
					}
				}
				else {
					int x = (width - DIVIDER_KNOB_SIZE) / 2;
					int y = (height - KNOB_SIZE) / 2;
					for (int i = 0; i < 3; i++) {
						Graphics2D graph = (Graphics2D) g.create(x + i * (KNOB_SIZE + KNOB_SPACE), y, KNOB_SIZE + 1, KNOB_SIZE + 1);
						graph.setPaint(KNOB_PAINTER);
						graph.fillOval(0, 0, KNOB_SIZE, KNOB_SIZE);
					}
				}

			}
		});

		add(splitPanel, BorderLayout.CENTER);

		this.fmlResource = fmlResource;

		textArea = new FMLRSyntaxTextArea(this);
		textArea.setSyntaxEditingStyle(FMLLanguageSupport.SYNTAX_STYLE_FML);
		textArea.setCodeFoldingEnabled(true);

		textArea.setCaretPosition(0);
		textArea.addHyperlinkListener(this);
		textArea.requestFocusInWindow();
		textArea.setMarkOccurrences(true);
		textArea.setCodeFoldingEnabled(true);
		textArea.setTabsEmulated(true);
		textArea.setTabSize(3);

		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					clearHighlights();
				}
				if (e.getClickCount() == 2) {
					// System.out.println("Open the browser at the right position !!!");
					// System.out.println("Position: " + textArea.getCaretPosition());
					// System.out.println("Line number: " + textArea.getCaretLineNumber());
					// System.out.println("Offset: " + textArea.getCaretOffsetFromLineStart());
					FMLCompilationUnit cu = fmlResource.getCompilationUnit();
					FMLCompilationUnitNode cuNode = (FMLCompilationUnitNode) cu.getPrettyPrintDelegate();
					// System.out.println("cuNode=" + cuNode);

					FMLObject focusedObject = cuNode.getFMLObjectAtLocation(textArea.getCaretLineNumber() + 1,
							textArea.getCaretOffsetFromLineStart());
					logger.info("select and focus object = " + focusedObject);
					browser.makeVisible(focusedObject);
				}
			}
		});

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

		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(206, 235, 255));

		// textArea.setText(fmlResource.getLoadedResourceData().getFMLPrettyPrint());

		fmlResource.getLoadedResourceData().getPropertyChangeSupport().addPropertyChangeListener(this);

		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new BorderLayout());

		scrollPane = new RTextScrollPane(textArea);
		((RSyntaxTextArea) scrollPane.getTextArea()).setSyntaxEditingStyle(FMLLanguageSupport.SYNTAX_STYLE_FML);
		editorPanel.add(scrollPane, BorderLayout.CENTER);

		finderToolbar = new TextFinderPanel(this);
		editorPanel.add(finderToolbar, BorderLayout.SOUTH);

		gutter = scrollPane.getGutter();
		// gutter.setBookmarkIcon(new ImageIcon("bookmark.png"));
		// gutter.setBookmarkIcon(IconLibrary.FIXABLE_ERROR_ICON);
		gutter.setBookmarkingEnabled(true);
		// gutter.setFoldIndicatorEnabled(true);
		// gutter.setFoldIcons(IconLibrary.NAVIGATION_BACKWARD_ICON, IconLibrary.NAVIGATION_FORWARD_ICON);

		errorStrip = new ErrorStrip(textArea);
		editorPanel.add(errorStrip, BorderLayout.LINE_END);

		splitPanel.add(editorPanel, LayoutPosition.CENTER.name());

		parser = new FMLEditorParser(this, fmlResource.getLoadedResourceData());
		textArea.setParser(parser);

		browser = new FIBCompilationUnitDetailedBrowser(fmlResource.getLoadedResourceData(), this, flexoController);
		splitPanel.add(browser, LayoutPosition.RIGHT.name());

		validationPanel = new FMLValidationPanel(getValidationReport(), this, flexoController);
		splitPanel.add(validationPanel, LayoutPosition.BOTTOM.name());

		LanguageSupportFactory.get().register(textArea);
		ToolTipManager.sharedInstance().registerComponent(textArea);

		// boolean requiresNewParsing = !fmlResource.getLoadedResourceData().getPrettyPrintDelegate().hasSource();

		// This call is a little bit brutal, because it triggers a new parsing
		// But it has the advantage to recompute a full-valid FML pretty-print with FML code and internal representation synchronized
		// Validation status is also updated during this call
		// String fmlPrettyPrint = fmlResource.getLoadedResourceData().getFMLPrettyPrint();
		String fmlPrettyPrint = fmlResource.getRawSource();
		textArea.setText(fmlPrettyPrint);

		boolean requiresNewParsing = fmlResource.getLoadedResourceData().getPrettyPrintDelegate() != null
				&& (!fmlResource.getLoadedResourceData().getPrettyPrintDelegate().hasSource());
		if (requiresNewParsing) {
			// Special case for a FML which wasn't deserialized from text and not having textual FML representation
			// Force parsing now
			logger.info("No initial textual FML, reparse it now");
			parseImmediately();

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					textArea.setText(fmlPrettyPrint);
				}
			});
		}

	}

	public void delete() {
		textArea.setParser(null);
		LanguageSupportFactory.get().unregister(textArea);
		ToolTipManager.sharedInstance().unregisterComponent(textArea);
		logger.warning("delete() not fully implemented for FMLEditor");
	}

	public CompilationUnitResource getFMLResource() {
		return fmlResource;
	}

	public FlexoServiceManager getServiceManager() {
		if (fmlResource != null) {
			return fmlResource.getServiceManager();
		}
		if (flexoController != null) {
			return flexoController.getApplicationContext();
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

	public FMLRSyntaxTextArea getTextArea() {
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

	public FMLValidationReport getValidationReport() {
		if (parser != null) {
			return parser.getValidationReport();
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (modelWillChange) {
			return;
		}
		if (evt.getPropertyName().equals("FMLPrettyPrint")) {

			// FML MAY have changed (this is not sure)
			// System.out.println("Received " + evt);
			if (SwingUtilities.isEventDispatchThread()) {
				updateFMLAsText();
			}
			else {
				if (!updateFMLAsTextRequested) {
					// We aggregate all the notifications to avoid to compute too many pretty-prints
					updateFMLAsTextRequested = true;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updateFMLAsText();
						}
					});
				}
			}
		}
	}

	private boolean updateFMLAsTextRequested = false;

	public void parseImmediately() {
		parser.parse((RSyntaxDocument) textArea.getDocument(), textArea.getSyntaxEditingStyle());
	}

	private String lastFML;

	/**
	 * Called (always in EDT) when FML pretty print of represented {@link FMLCompilationUnit} may have changed
	 * 
	 * We first check that FML really change, and update text accordingly, preventing the parser to be called again
	 */
	private void updateFMLAsText() {

		if (!fmlResource.getCompilationUnit().isFMLPrettyPrintAvailable()) {
			// In this case, we don't care
			return;
		}

		String newFML = fmlResource.getCompilationUnit().getFMLPrettyPrint();

		if (lastFML == null || !lastFML.equals(newFML)) {

			// This is a real change
			// System.out.println(">>>>>>>>>>> Really update FML !!!! ");
			// System.out.println(newFML);
			// Thread.dumpStack();

			lastFML = newFML;
			getTextArea().setTextNoParsingAnalysis(newFML);

			// FML has changed, so we update validation decoration
			if (parser != null) {
				parser.validate(fmlResource.getCompilationUnit());
			}
		}

		updateFMLAsTextRequested = false;

	}

	/**
	 * Called to force pretty-print of textual FML from internal representation<br>
	 * ("synchronize" in GUI)
	 */
	public void synchronizeTextualFML() {

		// We prevent the caret to move during operation
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		int updatePolicy = caret.getUpdatePolicy();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		// Update FML
		updateFMLAsText();

		// Reset caret update policy
		caret.setUpdatePolicy(updatePolicy);
	}

	private boolean modelWillChange = false;

	protected void modelWillChange() {
		modelWillChange = true;
	}

	protected void modelHasChanged(boolean requiresNewPrettyPrint) {
		modelWillChange = false;
		if (validationPanel != null) {
			validationPanel.setDataObject(getValidationReport());
		}
		if (requiresNewPrettyPrint) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateFMLAsText();
				}
			});
		}
	}

	private static final int KNOB_SIZE = 5;
	private static final int KNOB_SPACE = 2;
	private static final int DIVIDER_SIZE = KNOB_SIZE + 2 * KNOB_SPACE;
	private static final int DIVIDER_KNOB_SIZE = 3 * KNOB_SIZE + 2 * KNOB_SPACE;

	private static final Paint KNOB_PAINTER = new RadialGradientPaint(new Point((KNOB_SIZE - 1) / 2, (KNOB_SIZE - 1) / 2),
			(KNOB_SIZE - 1) / 2, new float[] { 0.0f, 1.0f }, new Color[] { Color.GRAY, Color.LIGHT_GRAY });

	private static final MultiSplitLayoutFactory MSL_FACTORY = new MultiSplitLayoutFactory.DefaultMultiSplitLayoutFactory();

	public static enum LayoutPosition {
		CENTER, RIGHT, BOTTOM;
	}

	protected static Split<?> getDefaultLayout() {
		Split root = MSL_FACTORY.makeColSplit();
		root.setName("ROOT");

		Split middle = getHorizontalSplit(LayoutPosition.CENTER, 0.8, LayoutPosition.RIGHT, 0.2);
		middle.setWeight(0.8);
		middle.setName("middle");

		Leaf<?> bottom = MSL_FACTORY.makeLeaf(LayoutPosition.BOTTOM.name());
		bottom.setWeight(0.2);

		root.setChildren(middle, MSL_FACTORY.makeDivider(), bottom);
		return root;
	}

	protected static Split<?> getHorizontalSplit(LayoutPosition position1, double weight1, LayoutPosition position2, double weight2) {
		Split split = MSL_FACTORY.makeRowSplit();
		Leaf<?> l1 = MSL_FACTORY.makeLeaf(position1.name());
		l1.setWeight(weight1);
		Leaf<?> l2 = MSL_FACTORY.makeLeaf(position2.name());
		l2.setWeight(weight2);
		split.setChildren(l1, MSL_FACTORY.makeDivider(), l2);
		return split;
	}

	protected static Split<?> getVerticalSplit(LayoutPosition position1, double weight1, LayoutPosition position2, double weight2) {
		Split split = MSL_FACTORY.makeColSplit();
		Leaf<?> l1 = MSL_FACTORY.makeLeaf(position1.name());
		l1.setWeight(weight1);
		Leaf<?> l2 = MSL_FACTORY.makeLeaf(position2.name());
		l2.setWeight(weight2);
		split.setChildren(l1, MSL_FACTORY.makeDivider(), l2);
		return split;
	}

	private Highlighter highlighter;
	private HighlightPainter painter;

	public void clearHighlights() {
		highlighter.removeAllHighlights();
	}

	public void highlightObject(FMLPrettyPrintable object) {
		FMLObjectNode<?, ?, ?> node = (FMLObjectNode<?, ?, ?>) object.getPrettyPrintDelegate();
		if (node == null || node.getRawSource() == null) {
			// When no textual FML source, abort
			return;
		}
		if (node.getLastParsedFragment() == null) {
			// When no parsed fragment, abort
			return;
		}
		int beginIndex = node.getRawSource().getIndex(node.getLastParsedFragment().getStartPosition());
		int endIndex = node.getRawSource().getIndex(node.getLastParsedFragment().getEndPosition());
		// System.out.println("Fragment: "+node.getLastParsedFragment());
		// System.out.println("On selectionne "+beginIndex+"-"+endIndex);
		try {
			Rectangle viewRect = textArea.modelToView(beginIndex);
			if (viewRect != null && scrollPane != null && scrollPane.getBounds() != null) {
				viewRect.height = scrollPane.getBounds().height - 20;
				// Scroll to make the rectangle visible
				textArea.scrollRectToVisible(viewRect);
				// And add highlight
				highlighter.addHighlight(beginIndex, endIndex, painter);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void highlightLine(int lineNb) {
		// System.out.println("Highlighting line "+lineNb);
		FMLCompilationUnitNode cuNode = (FMLCompilationUnitNode) getFMLResource().getCompilationUnit().getPrettyPrintDelegate();
		if (cuNode != null) {
			int index = cuNode.getRawSource().getIndex(cuNode.getRawSource().new RawSourcePosition(lineNb, 0));
			// System.out.println("Index: "+index);
			try {
				Rectangle viewRect = textArea.modelToView(index);
				viewRect.height = scrollPane.getBounds().height - 20;
				// Scroll to make the rectangle visible
				textArea.scrollRectToVisible(viewRect);
				textArea.setCaretPosition(index);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Called when a hyperlink is clicked in the text area.
	 *
	 * @param e
	 *            The event.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		System.out.println("Hyperlink event: " + e.getEventType());
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			if (url == null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
			else {
				JOptionPane.showMessageDialog(this, "URL clicked:\n" + url);
			}
		}
	}

}
