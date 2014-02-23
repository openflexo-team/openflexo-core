package org.openflexo.prefs;

import org.openflexo.localization.converter.LanguageConverter;
import org.openflexo.model.ModelContext;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.swing.converter.FlexoFontConverter;
import org.openflexo.swing.converter.LookAndFeelConverter;

public class FlexoPreferencesFactory extends ModelFactory {

	public FlexoPreferencesFactory(ModelContext context) throws ModelDefinitionException {
		super(context);
		addConverter(new LanguageConverter());
		addConverter(new FlexoFontConverter());
		addConverter(new LookAndFeelConverter());
	}

	@Override
	public <I> void objectHasBeenCreated(I newlyCreatedObject, Class<I> implementedInterface) {
		super.objectHasBeenCreated(newlyCreatedObject, implementedInterface);
		if (PreferencesContainer.class.isAssignableFrom(implementedInterface)) {
			((PreferencesContainer) newlyCreatedObject).setFlexoPreferencesFactory(this);
		}
	}

}