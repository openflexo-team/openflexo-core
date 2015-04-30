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

package org.openflexo;

/*
 * FlexoCst.java
 * Project WorkflowEditor
 *
 * Created by benoit on Mar 1, 2004
 */

/**
 * Constants used by the FLEXO application.
 *
 * @author benoit
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.openflexo.rm.BasicResourceImpl;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.ToolBox;

public class FlexoCst extends ColorCst {
	public static final String DLPM_WORKPACKAGE_ID = "1000023";

	public static final FlexoVersion BUSINESS_APPLICATION_VERSION = new FlexoVersion(ApplicationVersion.BUSINESS_APPLICATION_VERSION);

	public static final String BUSINESS_APPLICATION_VERSION_NAME = "OpenFlexo " + BUSINESS_APPLICATION_VERSION;

	public static final String OUTPUT_FILES_ENCODING = "UTF-8";

	public static final int META_MASK = ToolBox.getPLATFORM() == ToolBox.MACOS ? InputEvent.META_MASK : InputEvent.CTRL_MASK;

	public static final int MULTI_SELECTION_MASK = ToolBox.getPLATFORM() == ToolBox.MACOS ? InputEvent.META_DOWN_MASK
			: InputEvent.CTRL_DOWN_MASK;

	public static final int DELETE_KEY_CODE = ToolBox.getPLATFORM() == ToolBox.MACOS ? KeyEvent.VK_BACK_SPACE : KeyEvent.VK_DELETE;
	public static final int BACKSPACE_DELETE_KEY_CODE = ToolBox.getPLATFORM() == ToolBox.MACOS ? KeyEvent.VK_DELETE
			: KeyEvent.VK_BACK_SPACE;

	public static final Font BIG_FONT = new Font("SansSerif", Font.PLAIN, 13);

	public static final Font TITLE_FONT = new Font("SansSerif", Font.PLAIN, 18);

	public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 11);

	public static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 11);

	public static final Font MEDIUM_FONT = new Font("SansSerif", Font.PLAIN, 10);

	public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 9);

	public static final Font CODE_FONT = new Font("Monospaced", Font.PLAIN, 10);

	public static final String DENALI_SUPPORT_EMAIL = "benoit.mangez@denali.be";

	public static final Color UNDECORATED_DIALOG_BORDER_COLOR = Color.LIGHT_GRAY;

	public static URL cssUrl() {
		if (_cssURL == null) {
			_cssURL = ((BasicResourceImpl) ResourceLocator.locateResource("Config/FlexoMasterStyle.css")).getURL();
		}
		return _cssURL;
	}

	private static URL _cssURL;

	public static final BasicStroke STROKE_BOLD = new BasicStroke(2.0f);

	public static final BasicStroke STROKE = new BasicStroke(1.0f);

	public static final BasicStroke DASHED_STROKE_BOLD = new BasicStroke(1.0f);

	public static final Border FOCUSED_BORDER = BorderFactory.createLineBorder(Color.RED, 1);

	public static final Border HIDDEN_BORDER = BorderFactory.createLineBorder(new Color(0f, 0f, 0f, 0f), 1);

	public static boolean MODEL_EVENT_LISTENER_DEBUG = false;

	public static final Color DARK_BLUE_FLEXO_COLOR = new Color(245, 255, 246); // new Color(2,67,123);

	public static final Color WELCOME_FLEXO_COLOR = new Color(62, 80, 100);

	public static final Color OPEN_BLUE_COLOR = new Color(65, 91, 116);

	public static final int MINIMUM_BROWSER_VIEW_WIDTH = 200;
	public static final int MINIMUM_BROWSER_VIEW_HEIGHT = 300;
	public static final int PREFERRED_BROWSER_VIEW_WIDTH = 200;
	public static final int PREFERRED_BROWSER_VIEW_HEIGHT = 200;
	public static final int MINIMUM_BROWSER_CONTROL_PANEL_HEIGHT = 50;

	public static void switchColors(String colorSet) {
		if (colorSet.equals("Contento")) {
			oddLineColor = new Color(249, 246, 249);
			otherLineColor = new Color(231, 232, 234);
			flexoTextColor = new Color(29, 67, 130);
			flexoMainColor = new Color(145, 170, 208);
		} else if (colorSet.equals("Omniscio")) {
			oddLineColor = new Color(255, 255, 255);
			otherLineColor = new Color(253, 229, 200);
			flexoTextColor = new Color(0, 0, 0);
			flexoMainColor = new Color(249, 186, 109);
		} else {
			// flexoTextColor=new Color(74,119,50);
			// flexoMainColor=new Color(152,185,94);
			flexoTextColor = new Color(53, 85, 36);
			flexoMainColor = new Color(162, 185, 94);
			oddLineColor = new Color(244, 246, 235);
			otherLineColor = new Color(232, 237, 215);
		}
	}

	public static Color flexoTextColor = new Color(53, 85, 36);

	public static Color flexoMainColor = new Color(162, 185, 94);

	public static Color oddLineColor = new Color(244, 246, 235);

	public static Color otherLineColor = new Color(232, 237, 215);

	public static final int LOADING_PROGRESS_STEPS = 26;

	public static final int TEMPORARY_MESSAGE_PERSISTENCY = 2000;
}
