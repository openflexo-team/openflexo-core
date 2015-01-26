/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.action;

import javax.swing.Icon;

import org.openflexo.localization.FlexoLocalization;

public class ActionGroup {

	private int _index;
	private String _actionMenuName;
	private Icon _smallIcon;

	public ActionGroup(String actionMenuName, int index) {
		super();
		_actionMenuName = actionMenuName;
		_index = index;
	}

	public ActionGroup(String actionMenuName, int index, Icon icon) {
		this(actionMenuName, index);
		setSmallIcon(icon);
	}

	public String getUnlocalizedName() {
		return _actionMenuName;
	}

	public String getLocalizedName() {
		return FlexoLocalization.localizedForKey(_actionMenuName);
	}

	public String getLocalizedDescription() {
		return FlexoLocalization.localizedForKey(_actionMenuName + "_description");
	}

	public Icon getSmallIcon() {
		return _smallIcon;
	}

	public void setSmallIcon(Icon smallIcon) {
		_smallIcon = smallIcon;
	}

	public int getIndex() {
		return _index;
	}
}
