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

package org.openflexo.drm;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.openflexo.ApplicationContext;
import org.openflexo.drm.DocItem;
import org.openflexo.help.FlexoHelp;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.FlexoFrame;

public class TrackComponentCHForHelpView extends TrackComponentCH {

	private static final Logger logger = Logger.getLogger(TrackComponentCHForHelpView.class.getPackage().getName());

	private static Cursor HELP_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(IconLibrary.HELP_CURSOR.getImage(), new Point(8, 8),
			"Help cursor");

	public TrackComponentCHForHelpView(FlexoFrame frame, ApplicationContext applicationContext) {
		super(frame, applicationContext);
		frame.getContentPane().setCursor(HELP_CURSOR);
	}

	@Override
	public void applyTracking(JComponent component) {
		DocItem item = getDocResourceManager().getDocForComponent(focusedComponent);
		if (item != null) {
			FlexoHelp.getHelpBroker().setCurrentID(item.getIdentifier());
			FlexoHelp.getHelpBroker().setDisplayed(true);
			logger.info("Trying to display help for " + item.getIdentifier());
		}
	}

}
