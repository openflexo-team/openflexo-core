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
 * Utility class containing all icons used in context of FPSModule
 * 
 * @author sylvain
 * 
 */
public class FPSIconLibrary extends IconLibrary {

	

	// Module icons
	public static final ImageIcon FPS_SMALL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/module-fps-16.png"));
	public static final ImageIcon FPS_MEDIUM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/module-fps-32.png"));
	public static final ImageIcon FPS_MEDIUM_ICON_WITH_HOVER = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/module-fps-hover-32.png"));
	public static final ImageIcon FPS_BIG_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/module-fps-hover-64.png"));

	// Editor icons
	public static final ImageIcon FPS_MARK_AS_MERGED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/MarkAsMergedIcon.gif"));
	public static final ImageIcon FPS_MARK_AS_MERGED_DISABLED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/MarkAsMergedIcon-disabled.gif"));
	public static final ImageIcon FPS_UPDATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/UpdateIcon.gif"));
	public static final ImageIcon FPS_UPDATE_DISABLED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/UpdateIcon-disabled.gif"));
	public static final ImageIcon FPS_COMMIT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/CommitIcon.gif"));
	public static final ImageIcon FPS_COMMIT_DISABLED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/CommitIcon-disabled.gif"));
	public static final ImageIcon RESOLVED_CONFLICT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/ResolvedConflict.gif"));

	public static final ImageIcon FPS_AFP_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/AllFilesViewMode.gif"));
	public static final ImageIcon FPS_CFP_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/ConflictingViewMode.gif"));
	public static final ImageIcon FPS_IFP_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/InterestingFilesViewMode.gif"));
	public static final ImageIcon FPS_LMP_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/LocallyModifiedViewMode.gif"));
	public static final ImageIcon FPS_RMP_ACTIVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/RemotelyModifiedViewMode.gif"));

	// Model icons
	public static final ImageIcon CVS_REPOSITORY_LIST_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/RepositoryList.gif"));
	public static final ImageIcon CVS_REPOSITORY_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/CVSRepository.gif"));
	public static final ImageIcon CVS_MODULE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FPS/CVSModule.gif"));

}
