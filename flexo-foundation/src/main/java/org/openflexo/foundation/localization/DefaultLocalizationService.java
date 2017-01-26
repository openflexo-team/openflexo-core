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
import org.openflexo.toolbox.StringUtils;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public class DefaultLocalizationService extends FlexoServiceImpl implements LocalizationService {

	private static final Logger logger = Logger.getLogger(DefaultLocalizationService.class.getPackage().getName());

	private FlexoMainLocalizer mainLocalizer = null;
	private LocalizedDelegate flexoLocalizer = null;
	private String generalLocalizerRelativePath = null;

	private boolean automaticSaving = true;

	/*@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
	}*/

	@Override
	public void initialize() {

		logger.info("Initializing localization...");

		mainLocalizer = new FlexoMainLocalizer(true);
		logger.info("Main localization directory: " + mainLocalizer.getLocalizedDirectoryResource());
		logger.info("Deprecated localization directory: "
				+ ((LocalizedDelegateImpl) mainLocalizer.getDeprecatedLocalizer()).getLocalizedDirectoryResource());

		if (StringUtils.isNotEmpty(getGeneralLocalizerRelativePath())) {
			flexoLocalizer = new LocalizedDelegateImpl(ResourceLocator.locateResource(getGeneralLocalizerRelativePath()), mainLocalizer,
					getAutomaticSaving(), true);
		}
		else {
			flexoLocalizer = mainLocalizer;
		}

		/*System.out.println("Localizers:");
		System.out.println("flexoLocalizer=" + flexoLocalizer);
		System.out.println("flexoLocalizer.getParent()=" + flexoLocalizer.getParent());
		System.out.println("flexoLocalizer.getParent().getParent()=" + flexoLocalizer.getParent().getParent());*/

		FlexoLocalization.initWith(flexoLocalizer);
	}

	@Override
	public String getGeneralLocalizerRelativePath() {
		return generalLocalizerRelativePath;
	}

	@Override
	public void setGeneralLocalizerRelativePath(String relativePath) {
		this.generalLocalizerRelativePath = relativePath;
	}

	@Override
	public LocalizedDelegate getFlexoLocalizer() {
		return flexoLocalizer;
	}

	@Override
	public boolean getAutomaticSaving() {
		return automaticSaving;
	}

	@Override
	public void setAutomaticSaving(boolean automaticSaving) {
		this.automaticSaving = automaticSaving;
	}

}
