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

package org.openflexo.foundation.utils;

import org.openflexo.foundation.FlexoException;

/**
 * Must be thrown whenever the user or flexo choose to cancel <i>open project procedure</i>. It can append whenever user choose it, or flexo
 * itself detect (by version inspection) that it cannot open the prj.
 * <ul>
 * <li>user click on "cancel" in a fileChooser to select a prj file</li>
 * <li>user choose to not convert a project requiring conversion
 * <li>
 * <li>prj version is less than 1.3</li>
 * <li>prj version is higher than current flexo version (i.e. the prj has been modified by a newer version of Flexo)</li>
 * </ul>
 */
public class ProjectLoadingCancelledException extends FlexoException {

	public ProjectLoadingCancelledException(String message) {
		super();
	}

	public ProjectLoadingCancelledException() {
		super();
	}
}
