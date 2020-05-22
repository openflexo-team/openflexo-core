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
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.controller.widget.FIBCompilationUnitDetailedBrowser;
import org.openflexo.fml.controller.widget.ValidationPanel;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.swing.layout.JXMultiSplitPane;
import org.openflexo.swing.layout.JXMultiSplitPane.DividerPainter;
import org.openflexo.swing.layout.MultiSplitLayout;
import org.openflexo.swing.layout.MultiSplitLayout.Divider;
import org.openflexo.swing.layout.MultiSplitLayout.Leaf;
import org.openflexo.swing.layout.MultiSplitLayout.Split;
import org.openflexo.swing.layout.MultiSplitLayoutFactory;
import org.openflexo.view.controller.FlexoController;
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

	private JXMultiSplitPane centerPanel;
	private MultiSplitLayout centerLayout;

	private FlexoController flexoController;

	FIBCompilationUnitDetailedBrowser browser;
	ValidationPanel validationPanel;

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

		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new BorderLayout());

		RTextScrollPane sp = new RTextScrollPane(textArea);
		((RSyntaxTextArea) sp.getTextArea()).setSyntaxEditingStyle("text/fml");
		editorPanel.add(sp, BorderLayout.CENTER);

		finderToolbar = new TextFinderPanel(this);
		editorPanel.add(finderToolbar, BorderLayout.SOUTH);

		gutter = sp.getGutter();
		// gutter.setBookmarkIcon(new ImageIcon("bookmark.png"));
		// gutter.setBookmarkIcon(IconLibrary.FIXABLE_ERROR_ICON);
		gutter.setBookmarkingEnabled(true);
		// gutter.setFoldIndicatorEnabled(true);
		// gutter.setFoldIcons(IconLibrary.NAVIGATION_BACKWARD_ICON, IconLibrary.NAVIGATION_FORWARD_ICON);

		ErrorStrip errorStrip = new ErrorStrip(textArea);
		editorPanel.add(errorStrip, BorderLayout.LINE_END);

		parser = new FMLEditorParser(this);// new XmlParser();
		textArea.addParser(parser);

		splitPanel.add(editorPanel, LayoutPosition.CENTER.name());

		browser = new FIBCompilationUnitDetailedBrowser(fmlResource.getLoadedResourceData(), flexoController);
		splitPanel.add(browser, LayoutPosition.RIGHT.name());

		validationPanel = new ValidationPanel(getValidationReport(), flexoController);
		splitPanel.add(validationPanel, LayoutPosition.BOTTOM.name());

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
			// System.out.println("Received " + evt);
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

	public void parseImmediately() {
		parser.parse((RSyntaxDocument) textArea.getDocument(), textArea.getSyntaxEditingStyle());
	}

	private void updateFMLAsText() {
		getTextArea().setText(fmlResource.getCompilationUnit().getFMLPrettyPrint());
	}

	private boolean modelWillChange = false;

	protected void modelWillChange() {
		modelWillChange = true;
	}

	protected void modelHasChanged(boolean requiresNewPrettyPrint) {
		modelWillChange = false;
		validationPanel.setDataObject(getValidationReport());
		if (requiresNewPrettyPrint) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateFMLAsText();
				}
			});
		}
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

}
