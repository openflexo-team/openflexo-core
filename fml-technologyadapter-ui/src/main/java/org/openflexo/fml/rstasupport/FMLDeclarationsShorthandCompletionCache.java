/*
 * 07/22/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.localization.LocalizedDelegate;

/**
 * A cache of basic template and comment completions for Java, e.g. <code>System.out.println()</code>.
 *
 * @author Steve
 * @see ShorthandCompletionCache
 */
public class FMLDeclarationsShorthandCompletionCache extends ShorthandCompletionCache {

	// private static final ResourceBundle MSG = ResourceBundle.getBundle("org.openflexo.fml.rstasupport.resources");

	public FMLDeclarationsShorthandCompletionCache(FMLSourceCompletionProvider templateProvider,
			DefaultCompletionProvider commentsProvider) {

		super(templateProvider, commentsProvider);
		String template;

		// load defaults

		FMLTechnologyAdapter technologyAdapter = templateProvider.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		LocalizedDelegate locales = technologyAdapter.getLocales();

		template = "concept ${NewConcept} {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "concept", "concept", template,
				locales.localizedForKey("new_concept_template.shortDesc"), locales.localizedForKey("new_concept_template.description"),
				FMLIconLibrary.FLEXO_CONCEPT_ICON));

		template = "event ${NewEvent} {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "event", "event", template,
				locales.localizedForKey("new_event_template.shortDesc"), locales.localizedForKey("new_event_template.description"),
				FMLIconLibrary.FLEXO_EVENT_ICON));

		template = "enum ${NewEnum} {\n\tVALUE_1,\n\tVALUE_2,\n\tVALUE_3${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "enum", "enum", template,
				locales.localizedForKey("new_enum_template.shortDesc"), locales.localizedForKey("new_enum_template.description"),
				FMLIconLibrary.FLEXO_ENUM_ICON));

		template = "${Type} ${behaviour_name}() {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "action_scheme()", "action_scheme()", template,
				locales.localizedForKey("new_action_scheme_template.shortDesc"),
				locales.localizedForKey("new_action_scheme_template.description"), FMLIconLibrary.ACTION_SCHEME_ICON));

		template = "create() {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "create()", "create()", template,
				locales.localizedForKey("new_anonymous_creation_scheme_template.shortDesc"),
				locales.localizedForKey("new_anonymous_creation_scheme_template.description"), FMLIconLibrary.CREATION_SCHEME_ICON));

		template = "create::${behaviour_name}() {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "create::creation_scheme()", "create::creation_scheme()",
				template, locales.localizedForKey("new_creation_scheme_template.shortDesc"),
				locales.localizedForKey("new_creation_scheme_template.description"), FMLIconLibrary.CREATION_SCHEME_ICON));

		template = "delete() {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "delete()", "delete()", template,
				locales.localizedForKey("new_anonymous_deletion_scheme_template.shortDesc"),
				locales.localizedForKey("new_anonymous_deletion_scheme_template.description"), FMLIconLibrary.DELETION_SCHEME_ICON));

		template = "delete::${behaviour_name}() {\n\t${cursor}\n}";
		addShorthandCompletion(new FMLTemplateCompletion(templateProvider, "delete::deletion_scheme()", "delete::deletion_scheme()",
				template, locales.localizedForKey("new_deletion_scheme_template.shortDesc"),
				locales.localizedForKey("new_deletion_scheme_template.description"), FMLIconLibrary.DELETION_SCHEME_ICON));

		// Comments
		addCommentCompletion(new BasicCompletion(commentsProvider, "TODO:", null, locales.localizedForKey("todo_template.description")));
		addCommentCompletion(new BasicCompletion(commentsProvider, "FIXME:", null, locales.localizedForKey("fixme_template.description")));

	}

}
