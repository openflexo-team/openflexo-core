/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.localization;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.rm.ResourceLocator;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public class DefaultLocalizationService extends FlexoServiceImpl implements LocalizationService {

	private static final Logger logger = Logger.getLogger(DefaultLocalizationService.class.getPackage().getName());

	private LocalizedDelegate flexoLocalizer = null;

	/*@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
	}*/

	@Override
	public void initialize() {

		logger.info("Initializing localization...");

		flexoLocalizer = new FlexoMainLocalizer(true);

		logger.info("Main localization directory: " + ((FlexoMainLocalizer) flexoLocalizer).getLocalizedDirectoryResource());
		logger.info("Deprecated localization directory: "
				+ ((LocalizedDelegateImpl) ((FlexoMainLocalizer) flexoLocalizer).getDeprecatedLocalizer()).getLocalizedDirectoryResource());

		FlexoLocalization.initWith(flexoLocalizer);
	}

	@Override
	public void initializeMainLocalizer(String relativePath) {

		logger.info("Initializing localization with " + relativePath);
		flexoLocalizer = new LocalizedDelegateImpl(ResourceLocator.locateResource(relativePath), new FlexoMainLocalizer(true), true, true);
		FlexoLocalization.initWith(flexoLocalizer);
	}

	@Override
	public LocalizedDelegate getFlexoLocalizer() {
		return flexoLocalizer;
	}

}
