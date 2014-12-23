/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
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
import org.openflexo.toolbox.ImageIconResource;

/**
 * Utility class containing all icons used in context of FML@runtime technology adapter
 * 
 * @author sylvain
 * 
 */
public class FMLRTIconLibrary extends IconLibrary {

	private static final Logger logger = Logger.getLogger(FMLRTIconLibrary.class.getPackage().getName());

	public static final ImageIconResource VIEW_LIBRARY_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/ViewLibrary.png"));
	public static final ImageIconResource VIEW_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VE/View.png"));
	public static final ImageIconResource VIEW_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/View_32x32.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModelInstance.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModel_32x32.png"));
	public static final ImageIconResource VIRTUAL_MODEL_INSTANCE_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/VirtualModel_64x64.png"));
	public static final ImageIconResource FLEXO_CONCEPT_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FlexoConceptInstance.png"));
	public static final ImageIconResource FLEXO_CONCEPT_INSTANCE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/FlexoConceptInstance32.png"));
	public static final ImageIconResource MODEL_SLOT_INSTANCE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/ModelSlotInstance.png"));

	public static final ImageIconResource UNKNOWN_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/UnknownIcon.gif"));

	public static ImageIcon iconForObject(ViewObject object) {
		if (object instanceof View) {
			return VIEW_ICON;
		} else if (object instanceof ModelSlotInstance) {
			return MODEL_SLOT_INSTANCE_ICON;
		} else if (object instanceof VirtualModelInstance) {
			return VIRTUAL_MODEL_INSTANCE_ICON;
		} else if (object instanceof FlexoConceptInstance) {
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
