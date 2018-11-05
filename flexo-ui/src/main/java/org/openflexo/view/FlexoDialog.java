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

package org.openflexo.view;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.openflexo.swing.DialogFactory;

public class FlexoDialog extends JDialog {

	public FlexoDialog() throws HeadlessException {
		this((Frame) null);
	}

	public FlexoDialog(Frame owner) throws HeadlessException {
		super(FlexoFrame.getOwner(owner));
		init();
	}

	public FlexoDialog(Frame owner, boolean modal) throws HeadlessException {
		super(FlexoFrame.getOwner(owner), modal);
		init();
	}

	public FlexoDialog(Frame owner, String title) throws HeadlessException {
		super(FlexoFrame.getOwner(owner), title);
		init();
	}

	public FlexoDialog(Frame owner, String title, boolean modal) throws HeadlessException {
		super(FlexoFrame.getOwner(owner), title, modal);
		init();
	}

	public FlexoDialog(Dialog owner) throws HeadlessException {
		super(owner);
		init();
	}

	public FlexoDialog(Dialog owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		init();
	}

	public FlexoDialog(Dialog owner, String title) throws HeadlessException {
		super(owner, title);
		init();
	}

	public FlexoDialog(Dialog owner, String title, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		init();
	}

	private void init() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FlexoDialog.this.dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		// (SGU) Very important !!!
		// Don't put this flag to true again, as it lead to very severe issues
		// while managing popup menu (see CustomPopup) which often appear below
		// JDialog
		// setAlwaysOnTop(true);
		setJMenuBar(null);
	}

	@Override
	public void show() {
		centerDialog();
		/*if (FlexoModule.getActiveModule()!=null)
			FlexoModule.getActiveModule().getFlexoController().dismountWindowsOnTop(getBounds());*/
		super.show();
	}

	public void centerDialog() {
		setLocationRelativeTo(getOwner());
	}

	public static final DialogFactory DIALOG_FACTORY = new DialogFactory() {

		@Override
		public Dialog getNewDialog() throws HeadlessException {
			return new FlexoDialog();
		}

		@Override
		public Dialog getNewDialog(Frame owner) throws HeadlessException {
			return new FlexoDialog(owner);
		}

		@Override
		public Dialog getNewDialog(Frame owner, boolean modal) throws HeadlessException {
			return new FlexoDialog(owner, modal);
		}

		@Override
		public Dialog getNewDialog(Frame owner, String title) throws HeadlessException {
			return new FlexoDialog(owner, title);
		}

		@Override
		public Dialog getNewDialog(Frame owner, String title, boolean modal) throws HeadlessException {
			return new FlexoDialog(owner, title, modal);
		}

		@Override
		public Dialog getNewDialog(Dialog owner) throws HeadlessException {
			return new FlexoDialog(owner);
		}

		@Override
		public Dialog getNewDialog(Dialog owner, boolean modal) throws HeadlessException {
			return new FlexoDialog(owner, modal);
		}

		@Override
		public Dialog getNewDialog(Dialog owner, String title) throws HeadlessException {
			return new FlexoDialog(owner, title);
		}

		@Override
		public Dialog getNewDialog(Dialog owner, String title, boolean modal) throws HeadlessException {
			return new FlexoDialog(owner, title, modal);
		}

	};

}
