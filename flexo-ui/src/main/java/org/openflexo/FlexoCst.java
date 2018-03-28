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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.uicst.ColorUtils;

public class FlexoCst extends ColorUtils {
	public static final FlexoVersion BUSINESS_APPLICATION_VERSION = new FlexoVersion(ApplicationVersion.BUSINESS_APPLICATION_VERSION);

	public static final String BUSINESS_APPLICATION_VERSION_NAME = "OpenFlexo " + BUSINESS_APPLICATION_VERSION;

	public static final int META_MASK = ToolBox.isMacOS() ? InputEvent.META_MASK : InputEvent.CTRL_MASK;

	public static final int MULTI_SELECTION_MASK = ToolBox.isMacOS() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

	public static final int DELETE_KEY_CODE = ToolBox.isMacOS() ? KeyEvent.VK_BACK_SPACE : KeyEvent.VK_DELETE;
	public static final int BACKSPACE_DELETE_KEY_CODE = ToolBox.isMacOS() ? KeyEvent.VK_DELETE : KeyEvent.VK_BACK_SPACE;

	public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 9);

	public static final Color UNDECORATED_DIALOG_BORDER_COLOR = Color.LIGHT_GRAY;

	public static final Color WELCOME_FLEXO_COLOR = new Color(62, 80, 100);

	public static final int TEMPORARY_MESSAGE_PERSISTENCY = 2000;
}
