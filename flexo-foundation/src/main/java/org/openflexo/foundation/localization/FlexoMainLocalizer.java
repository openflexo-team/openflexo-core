/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.localization;

import java.util.logging.Logger;

import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * This is the general Openflexo localized implementation<br>
 * Default localized directory is managed in FlexoLocalization directory<br>
 * 
 * This implementation use the deprecated localization resources stored in GINA to rebuild all dictionaries
 * 
 * @author sylvain
 * 
 */
public class FlexoMainLocalizer extends LocalizedDelegateImpl {

	private static final Logger logger = Logger.getLogger(FlexoMainLocalizer.class.getPackage().getName());

	public static final String DEPRECATED_LOCALIZATION_DIRNAME = "FlexoLocalization/Deprecated";
	public static final String FLEXO_LOCALIZATION_DIRNAME = "FlexoLocalization/General";

	private static LocalizedDelegate makeDeprecatedLocalizer() {
		return new LocalizedDelegateImpl(ResourceLocator.locateResource(DEPRECATED_LOCALIZATION_DIRNAME), null, false, false);
	}

	public FlexoMainLocalizer(boolean isDev) {
		super(ResourceLocator.locateResource(FLEXO_LOCALIZATION_DIRNAME), makeDeprecatedLocalizer(), isDev, isDev);
		// If we want to update locales, we have to retrieve source code dictionaries
		if (isDev) {
			// Get Localized from flexolocalization here because we need main Localized support to come from gina project
			Resource sourceCodeResource = ResourceLocator.locateSourceCodeResource(getLocalizedDirectoryResource(),
					".*flexolocalization.*");
			if (sourceCodeResource != null) {
				setLocalizedDirectoryResource(sourceCodeResource);
			}
		}
	}

	public LocalizedDelegate getDeprecatedLocalizer() {
		return getParent();
	}

}
