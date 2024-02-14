/*
 * 06/25/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport;

import javax.swing.Icon;

import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * A completion for 'use' directive
 *
 * @author sylvain
 */
public class UseCompletion<MS extends ModelSlot<?>> extends AbstractTemplateCompletion {

	private Class<MS> msClass;

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 4;

	private static String getTemplate(FMLSourceCompletionProvider provider, Class<? extends ModelSlot<?>> msClass) {
		return "use " + msClass.getCanonicalName() + " as ${" + getDefaultTAId(provider, msClass) + "};";
	}

	private static String getDefaultTAId(FMLSourceCompletionProvider provider, Class<? extends ModelSlot<?>> msClass) {
		return getTechnologyAdapter(provider, msClass).getIdentifier();
	}

	private static TechnologyAdapter<?> getTechnologyAdapter(FMLSourceCompletionProvider provider, Class<? extends ModelSlot<?>> msClass) {
		return provider.getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(msClass);
	}

	public UseCompletion(FMLSourceCompletionProvider provider, String inputText, Class<MS> msClass) {
		super(provider, "use " + getDefaultTAId(provider, msClass), "use " + getDefaultTAId(provider, msClass),
				getTemplate(provider, msClass));
		this.msClass = msClass;
		setRelevance(RELEVANCE);
	}

	public Class<MS> getModelSlotClass() {
		return msClass;
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		return getTechnologyAdapter(getProvider(), getModelSlotClass());
	}

	@Override
	public Icon getIcon() {

		TechnologyAdapter modelSlotTA = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapterForModelSlot(getModelSlotClass());
		TechnologyAdapterControllerService tacService = getServiceManager().getService(TechnologyAdapterControllerService.class);

		TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(modelSlotTA);
		if (tac != null) {
			return IconFactory.getImageIcon(tac.getIconForModelSlot(getModelSlotClass()), IconLibrary.IMPORT);
		}
		return FMLIconLibrary.IMPORT_ICON;
	}

	@Override
	public String getShortDescription() {
		return getTechnologyAdapter().getLocales().localizedForKeyWithParams("use_model_slot_($object.simpleName)", getModelSlotClass());
	}

	@Override
	public String getSummary() {
		if (getModelSlotClass().getAnnotation(FML.class) != null) {
			FML annotation = getModelSlotClass().getAnnotation(FML.class);
			return "<html>" + "<b>" + annotation.value() + "</b>" + "<br>" + annotation.description() + "</html>";
		}
		return "No description available";
	}
}
