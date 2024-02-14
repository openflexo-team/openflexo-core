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

import java.util.ResourceBundle;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;

/**
 * A cache of basic template and comment completions for Java, e.g. <code>System.out.println()</code>.
 *
 * @author Steve
 * @see ShorthandCompletionCache
 */
public class FMLShorthandCompletionCache extends ShorthandCompletionCache {

	private static final ResourceBundle MSG = ResourceBundle.getBundle("org.openflexo.fml.rstasupport.resources");

	public FMLShorthandCompletionCache(FMLSourceCompletionProvider templateProvider, DefaultCompletionProvider commentsProvider) {

		super(templateProvider, commentsProvider);
		String template;

		// load defaults

		FMLTechnologyAdapter technologyAdapter = templateProvider.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		LocalizedDelegate locales = technologyAdapter.getLocales();

		template = "concept ${NewConcept} {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "concept", "concept", template,
				locales.localizedForKey("new_concept_template.shortDesc"), locales.localizedForKey("new_concept_template.description")));

		template = "event ${NewEvent} {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "event", "event", template,
				locales.localizedForKey("new_event_template.shortDesc"), locales.localizedForKey("new_event_template.description")));

		template = "enum ${NewEnum} {\n\tVALUE_1,\n\tVALUE_2,\n\tVALUE_3${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "enum", "enum", template,
				locales.localizedForKey("new_enum_template.shortDesc"), locales.localizedForKey("new_enum_template.description")));

		template = "log \"${cursor}\";";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "log", "log", template,
				locales.localizedForKey("log_template.shortDesc"), locales.localizedForKey("log_template.description")));

		template = "for (${Type} ${item} : ${collection}) {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "for", "for-loop", template,
				locales.localizedForKey("forloop_template.shortDesc"), locales.localizedForKey("forloop_template.description")));

		template = "for (int ${i} = 0; ${i} < ${10}; ${i}++) {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "for", "for-iterator-loop", template,
				locales.localizedForKey("foriteratorloop_template.shortDesc"),
				locales.localizedForKey("foriteratorloop_template.description")));

		template = "if (${condition}) {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "if", "if-cond", template,
				locales.localizedForKey("ifcond_template.shortDesc"), locales.localizedForKey("ifcond_template.description")));

		template = "if (${condition}) {\n\t${cursor}\n}\nelse {\n\t\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "if", "if-else", template,
				locales.localizedForKey("ifelse_template.shortDesc"), locales.localizedForKey("ifelse_template.description")));

		template = "do {\n\t${cursor}\n} while (${condition});";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "do", "do-loop", template,
				locales.localizedForKey("do_template.shortDesc"), locales.localizedForKey("do_template.description")));

		template = "while (${condition}) {\n\t${cursor}\n}";
		addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "while", "while-cond", template,
				locales.localizedForKey("while_template.shortDesc"), locales.localizedForKey("while_template.description")));

		// template = "switch (${key}) {\n\tcase ${value}:\n\t\t${cursor}\n\t\tbreak;\n\tdefault:\n\t\tbreak;\n}";
		// addShorthandCompletion(new JavaTemplateCompletion(templateProvider, "switch", "switch-statement", template,
		// MSG.getString("switch.case.shortDesc"), MSG.getString("switch.case.summary")));

		// Comments
		addCommentCompletion(new BasicCompletion(commentsProvider, "TODO:", null, MSG.getString("todo")));
		addCommentCompletion(new BasicCompletion(commentsProvider, "FIXME:", null, MSG.getString("fixme")));

	}

}
