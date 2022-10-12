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

package org.openflexo.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openflexo.ApplicationVersion;
import org.openflexo.FlexoCst;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.ToolBox;

@SuppressWarnings("serial")
public class SplashWindow extends JDialog {

	private final JLabel splash;

	public SplashWindow(Frame f) {
		super(f);
		setUndecorated(true);
		Dimension imageDim = new Dimension(IconLibrary.SPLASH_IMAGE.getIconWidth(), IconLibrary.SPLASH_IMAGE.getIconHeight());

		// cree un label avec notre image
		splash = new JLabel(IconLibrary.SPLASH_IMAGE);
		splash.setBorder(BorderFactory.createLineBorder(FlexoCst.UNDECORATED_DIALOG_BORDER_COLOR));

		// ajoute le label au panel
		getContentPane().setLayout(null);
		JLabel flexoLabel = new JLabel(IconLibrary.OPENFLEXO_TEXT_ICON, SwingConstants.RIGHT);
		flexoLabel.setForeground(FlexoCst.WELCOME_FLEXO_COLOR);
		flexoLabel.setBackground(Color.RED);
		flexoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		getContentPane().add(flexoLabel);
		flexoLabel.setBounds(319, 142, 231, 59);

		JLabel businessLabel = new JLabel("Openflexo Diatomée distribution", SwingConstants.RIGHT);
		businessLabel.setForeground(FlexoCst.WELCOME_FLEXO_COLOR);
		businessLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
		getContentPane().add(businessLabel);
		businessLabel.setBounds(260, 195, 280, 15);

		JLabel versionLabel = new JLabel("Version " + FlexoCst.BUSINESS_APPLICATION_VERSION + " (build " + ApplicationVersion.BUILD_ID
				+ ")", SwingConstants.RIGHT);
		versionLabel.setForeground(Color.DARK_GRAY);
		versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		getContentPane().add(versionLabel);
		versionLabel.setBounds(260, 215, 280, 15);

		JLabel urlLabel = new JLabel("<html><u>www.openflexo.org</u></html>", SwingConstants.RIGHT);
		urlLabel.addMouseListener(new MouseAdapter() {

			/**
			 * Overrides mouseEntered
			 * 
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			/**
			 * Overrides mouseEntered
			 * 
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			/**
			 * Overrides mouseClicked
			 * 
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				ToolBox.openURL("http://www.openflexo.org");
			}
		});
		urlLabel.setForeground(new Color(180, 150, 200));
		urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
		getContentPane().add(urlLabel);
		urlLabel.setBounds(290, 263, 280, 12);

		JLabel copyrightLabel = new JLabel("(c) Copyright Openflexo, 2013-2022, all rights reserved", SwingConstants.RIGHT);
		copyrightLabel.setForeground(Color.DARK_GRAY);
		copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
		getContentPane().add(copyrightLabel);
		copyrightLabel.setBounds(290, 277, 280, 12);

		getContentPane().add(splash);
		splash.setBounds(0, 0, imageDim.width, imageDim.height);
		validate();
		setSize(imageDim);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
