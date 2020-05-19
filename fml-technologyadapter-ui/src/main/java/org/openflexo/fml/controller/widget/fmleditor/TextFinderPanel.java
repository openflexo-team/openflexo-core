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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.openflexo.icon.IconLibrary;

/**
 * Widget allowing find text
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class TextFinderPanel extends JToolBar implements ActionListener {

	static final Logger logger = Logger.getLogger(TextFinderPanel.class.getPackage().getName());

	private final FMLEditor editor;

	private JTextField searchField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;

	public TextFinderPanel(FMLEditor editor) {
		super();

		this.editor = editor;

		searchField = new JTextField(30);
		add(searchField);
		final JButton nextButton = new JButton(IconLibrary.NAVIGATION_FORWARD_ICON /*"Find Next"*/);
		nextButton.setActionCommand("FindNext");
		nextButton.addActionListener(this);
		nextButton.setBorder(BorderFactory.createEmptyBorder());
		add(nextButton);
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});
		JButton prevButton = new JButton(IconLibrary.NAVIGATION_BACKWARD_ICON /*"Find Previous"*/);
		prevButton.setActionCommand("FindPrev");
		prevButton.addActionListener(this);
		prevButton.setBorder(BorderFactory.createEmptyBorder());
		add(prevButton);
		regexCB = new JCheckBox("Regex");
		regexCB.setFont(getFont().deriveFont(11f));
		add(regexCB);
		matchCaseCB = new JCheckBox("Match Case");
		matchCaseCB.setFont(getFont().deriveFont(11f));
		add(matchCaseCB);
	}

	public FMLEditor getEditor() {
		return editor;
	}

	public RSyntaxTextArea getTextArea() {
		return getEditor().getTextArea();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// "FindNext" => search forward, "FindPrev" => search backward
		String command = e.getActionCommand();
		boolean forward = "FindNext".equals(command);

		// Create an object defining our search parameters.
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.length() == 0) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
		context.setRegularExpression(regexCB.isSelected());
		context.setSearchForward(forward);
		context.setWholeWord(false);

		boolean found = SearchEngine.find(getTextArea(), context).wasFound();
		if (!found) {
			JOptionPane.showMessageDialog(TextFinderPanel.this, "Text not found");
		}

	}

}
