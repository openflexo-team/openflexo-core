/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.view.popups;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.openflexo.diff.ComputeDiff;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.swing.diff.DiffPanel;
import org.openflexo.toolbox.TokenMarkerStyle;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.controller.FlexoController;

/**
 * @author gpolet
 */
public class FileDiffEditorPopup extends FlexoDialog {
	private Logger logger = FlexoLogger.getLogger(FileDiffEditorPopup.class.getPackage().getName());

	private FlexoController controller;

	private String leftSource;

	private String rightSource;

	private DiffPanel diffPanel;

	public FileDiffEditorPopup(String leftTitle, String rightTitle, String leftSource, String rightSource, FlexoController controller) {
		super(controller.getFlexoFrame(), FlexoLocalization.localizedForKey("diff_editor"), false);
		this.leftSource = leftSource;
		this.rightSource = rightSource;
		this.controller = controller;

		diffPanel = new DiffPanel(ComputeDiff.diff(leftSource, rightSource), TokenMarkerStyle.None, leftTitle, rightTitle, null, true);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(diffPanel, BorderLayout.CENTER);
		JPanel controlPanel = new JPanel(new FlowLayout());
		JButton button = new JButton();
		button.setText(FlexoLocalization.localizedForKey("close", button));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		controlPanel.add(button);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(1000, 800));
		validate();
		pack();
	}

}
