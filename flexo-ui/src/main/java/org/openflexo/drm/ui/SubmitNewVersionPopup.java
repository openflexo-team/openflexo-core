/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.drm.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.openflexo.drm.DocItem;
import org.openflexo.drm.DocItemVersion;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.localization.Language;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.controller.FlexoController;

/**
 * Popup allowing to choose some objects of a given type from Flexo model objects hierarchy
 * 
 * @author sguerin
 * 
 */
public class SubmitNewVersionPopup extends FlexoDialog {

	DocItemVersion _versionToSubmit = null;

	protected SubmitNewVersionView _view;
	protected JButton hideShowDetailsButton;

	public SubmitNewVersionPopup(final DocItem docItem, Language language, Frame owner, final FlexoController controller,
			FlexoEditor editor) {
		super(owner);

		String title = controller.getModuleLocales().localizedForKey("submit_documentation_for") + " " + docItem.getIdentifier();
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());

		_view = new SubmitNewVersionView(docItem, language, editor);
		_view.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());

		JButton applyButton = new JButton(controller.getModuleLocales().localizedForKey("submit_documentation"));
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_versionToSubmit = _view.getDocResourceManager().getEditedVersion(docItem);
				_view.getDocResourceManager().stopEditVersion(_view.getDocResourceManager().getEditedVersion(docItem));
				dispose();
			}
		});
		controlPanel.add(applyButton);
		applyButton.setSelected(true);

		JButton cancelButton = new JButton(controller.getModuleLocales().localizedForKey("cancel_submission"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_view.getDocResourceManager().stopEditVersion(_view.getDocResourceManager().getEditedVersion(docItem));
				dispose();
			}
		});
		controlPanel.add(cancelButton);

		hideShowDetailsButton = new JButton(controller.getModuleLocales().localizedForKey("show_details"));
		hideShowDetailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_view.showDetails) {
					_view.hideDetails();
					hideShowDetailsButton.setText(controller.getModuleLocales().localizedForKey("show_details", hideShowDetailsButton));
				}
				else {
					setSize(1000, 900);
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					setLocation((dim.width - getSize().width) / 2, (dim.height - getSize().height) / 2 - 100);
					_view.showDetails();
					hideShowDetailsButton.setText(controller.getModuleLocales().localizedForKey("hide_details", hideShowDetailsButton));
					pack();
					repaint();
				}
			}
		});
		controlPanel.add(hideShowDetailsButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		contentPanel.add(_view, BorderLayout.CENTER);
		contentPanel.add(controlPanel, BorderLayout.SOUTH);

		getContentPane().add(contentPanel, BorderLayout.CENTER);

		setModal(true);
		validate();

		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getSize().width) / 2, (dim.height - getSize().height) / 2 - 100);

		show();
	}

	public DocItemVersion getVersionToSubmit() {
		return _versionToSubmit;
	}

}
