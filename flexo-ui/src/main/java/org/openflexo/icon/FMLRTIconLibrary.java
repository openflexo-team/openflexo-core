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

package org.openflexo.icon;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewObject;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.rm.ResourceLocator;

/**
 * Utility class containing all icons used in context of FML@runtime technology adapter
 * 
 * @author sylvain
 * 
 */
public class FMLRTIconLibrary extends IconLibrary {

	private static final Logger logger = Logger.getLogger(FMLRTIconLibrary.class.getPackage().getName());

	public static final ImageIconResource FML_RT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VE/FML-RT.png"));
	public static final ImageIconResource FML_RT_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FML-RT_32x32.png"));
	public static final ImageIconResource FML_RT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FML-RT_64x64.png"));

	public static final ImageIconResource VIEW_LIBRARY_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/ViewLibrary.png"));

	public static final ImageIconResource VIEW_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/View_16x16.png"));
	public static final ImageIconResource VIEW_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/View_32x32.png"));
	public static final ImageIconResource VIEW_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/View_64x64.png"));

	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModelInstance.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModelInstance_32x32.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModelInstance_64x64.png"));
	public static final ImageIconResource FLEXO_CONCEPT_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FlexoConceptInstance.png"));
	public static final ImageIconResource FLEXO_CONCEPT_INSTANCE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FlexoConceptInstance32.png"));
	public static final ImageIconResource MODEL_SLOT_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/ModelSlotInstance.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_SMALL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModelInstance_8x8.png"));

	public static final IconMarker VIRTUAL_MODEL_INSTANCE_MARKER = new IconMarker(VIRTUAL_MODEL_INSTANCE_SMALL_ICON, 8, 0);

	public static final ImageIconResource UNKNOWN_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/UnknownIcon.gif"));

	public static ImageIcon iconForObject(ViewObject object) {
		if (object instanceof View) {
			return VIEW_ICON;
		}
		else if (object instanceof ModelSlotInstance) {
			return FMLIconLibrary.iconForObject(((ModelSlotInstance) object).getModelSlot());
		}
		else if (object instanceof VirtualModelInstance) {
			return VIRTUAL_MODEL_INSTANCE_ICON;
		}
		else if (object instanceof FlexoConceptInstance) {
			return FLEXO_CONCEPT_INSTANCE_ICON;
		}
		logger.warning("No icon for " + object.getClass());
		return UNKNOWN_ICON;
	}

	public static ImageIcon iconForObject(ViewResource object) {
		return VIEW_ICON;
	}

	public static ImageIcon iconForObject(VirtualModelInstanceResource object) {
		return VIRTUAL_MODEL_INSTANCE_ICON;
	}

}
