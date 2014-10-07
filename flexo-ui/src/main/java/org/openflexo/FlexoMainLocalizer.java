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
