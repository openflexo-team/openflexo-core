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

package org.openflexo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * This is the general Openflexo localized implementation<br>
 * Default localized directory is managed here
 * 
 * @author sylvain
 * 
 */
public class FlexoMainLocalizer extends LocalizedDelegateImpl {

	private static final Logger logger = Logger.getLogger(FlexoLocalization.class.getPackage().getName());

	public static final String LOCALIZATION_DIRNAME = "Localized";

	private static Resource _localizedDirectory = null;
	private static FlexoMainLocalizer instance = null;

	/**
	 * Return directory where localized dictionnaries for main localizer are stored
	 * 
	 * @return
	 */
	private static Resource getMainLocalizerLocalizedDirectory() {
		if (_localizedDirectory == null) {
			_localizedDirectory = ResourceLocator.locateResource(LOCALIZATION_DIRNAME);

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Setting localized directory" + _localizedDirectory.getURI());
			}
		}
		return _localizedDirectory;
	}

	private FlexoMainLocalizer() {
		super(getMainLocalizerLocalizedDirectory(), null, Flexo.isDev, Flexo.isDev);
	}

	public static FlexoMainLocalizer getInstance() {
		if (instance == null) {
			instance = new FlexoMainLocalizer();
		}
		return instance;
	}

	public static void main(String[] args) {
		FlexoLocalization.initWith(new FlexoMainLocalizer());
		System.out.println("Returning " + FlexoLocalization.localizedForKeyAndLanguage("save", Language.FRENCH));
	}
}
