/**
 * 
 * Copyright (c) 2014-2017, Openflexo
 * 
 * This file is part of Flexo-Documentation-UI, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Please not that some parts of that component are freely inspired from
 * Stanislav Lapitsky code (see http://java-sl.com/docx_editor_kit.html)
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

package org.openflexo.components.doc.editorkit;

import java.awt.Color;
import java.io.Serializable;

/**
 * This class implements a border attributes set. Such as border color, border style etc.
 *
 * @author Stanislav Lapitsky
 */
@SuppressWarnings("serial")
public class BorderAttributes implements Serializable {
	/**
	 * top border value
	 */
	public static final int TOP = 1;
	/**
	 * horizontal middle border value
	 */
	public static final int HORIZONTAL = 2;
	/**
	 * bottom border value
	 */
	public static final int BOTTOM = 4;
	/**
	 * left border value
	 */
	public static final int LEFT = 8;
	/**
	 * vertical middle border value
	 */
	public static final int VERTICAL = 16;
	/**
	 * right border value
	 */
	public static final int RIGHT = 32;
	/**
	 * Top border presence
	 */
	public int borderTop = 0;
	/**
	 * Horizontal inner border presence
	 */
	public int borderHorizontal = 0;
	/**
	 * Bottom border presence
	 */
	public int borderBottom = 0;

	/**
	 * Left border presence
	 */
	public int borderLeft = 0;
	/**
	 * Vertical inner border presence
	 */
	public int borderVertical = 0;
	/**
	 * Right border presence
	 */
	public int borderRight = 0;

	/**
	 * Color of border line
	 */
	public Color lineColor = Color.BLACK;

	/**
	 * Set values of table borders
	 * 
	 * @param borders
	 *            Binary symbol rank corresponds to appropriate border 1 - top border 2 - horizontal inner border 3 - bottom border 4 - left
	 *            border 5 - vertical inner border 6 - right border
	 */
	public void setBorders(int borders) {
		int val = borders;

		borderTop = val % 2;
		val = val / 2;
		borderHorizontal = val % 2;
		val = val / 2;
		borderBottom = val % 2;
		val = val / 2;

		borderLeft = val % 2;
		val = val / 2;
		borderVertical = val % 2;
		val = val / 2;
		borderRight = val % 2;
		val = val / 2;
	}

	/**
	 * @return numeric representation of the borders
	 */
	public int getBorders() {
		int result = 0;
		result += borderTop;
		result += borderHorizontal * 2;
		result += borderBottom * 4;

		result += borderLeft * 8;
		result += borderVertical * 16;
		result += borderRight * 32;
		return result;
	}
}
