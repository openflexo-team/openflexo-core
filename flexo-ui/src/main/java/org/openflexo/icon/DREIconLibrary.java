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

package org.openflexo.icon;

import javax.swing.ImageIcon;

import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ImageIconResource;

/**
 * Utility class containing all icons used in context of DREModule
 * 
 * @author sylvain
 * 
 */
public class DREIconLibrary extends IconLibrary {

	

	// Module icons
	public static final ImageIcon DRE_SMALL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DRC_A_Small.gif"));
	public static final ImageIcon DRE_MEDIUM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DRC_A.gif"));
	public static final ImageIcon DRE_MEDIUM_ICON_WITH_HOVER = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DRC_S.gif"));
	public static final ImageIcon DRE_BIG_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DRC_A.gif"));

	// Perspective icons
	public static final ImageIcon DRE_DRE_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DREPerspective_A.png"));

	// Editor icons
	public static final ImageIcon DOC_FOLDER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/Folder.gif"));
	public static final ImageIcon DOC_ITEM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/DocItem.gif"));
	public static final ImageIcon UNDOCUMENTED_DOC_ITEM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/UndocumentedDocItem.gif"));
	public static final ImageIcon APPROVING_PENDING_DOC_ITEM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/DRE/ApprovingPendingDocItem.gif"));
	public static final ImageIcon AVAILABLE_NEW_VERSION_PENDING_DOC_ITEM_ICON = new ImageIconResource(ResourceLocator.locateResource(
			"Icons/DRE/AvailableNewVersionPendingDocItem.gif"));

}
