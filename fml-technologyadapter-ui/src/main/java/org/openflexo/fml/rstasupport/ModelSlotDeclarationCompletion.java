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

import java.lang.reflect.Type;

import javax.swing.Icon;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * A completion for a {@link ModelSlot} declaration
 *
 * @author sylvain
 */
public class ModelSlotDeclarationCompletion<MS extends ModelSlot<?>> extends FMLPropertyDeclarationCompletion<MS> {

	private static String getTemplate(FMLSourceCompletionProvider provider, Class<? extends ModelSlot<?>> msClass) {
		String dataType = TypeUtils.simpleRepresentation(getModelSlotDataType(msClass));
		String parametersTemplate = getParametersTemplate(provider, msClass);
		return "${" + dataType + "} ${modelSlot} with " + msClass.getSimpleName() + "(" + parametersTemplate + ");";
	}

	private static Type getModelSlotDataType(Class<? extends ModelSlot<?>> msClass) {
		return TypeUtils.getTypeArgument(msClass, ModelSlot.class, 0);
	}

	private static TechnologyAdapter<?> getTechnologyAdapter(FMLSourceCompletionProvider provider, Class<? extends ModelSlot<?>> msClass) {
		return provider.getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(msClass);
	}

	public ModelSlotDeclarationCompletion(FMLSourceCompletionProvider provider, String inputText, Class<MS> msClass) {
		super(provider, "declare " + msClass.getSimpleName(), "declare " + msClass.getSimpleName(), getTemplate(provider, msClass),
				msClass);
	}

	public Class<MS> getModelSlotClass() {
		return getPropertyClass();
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
			return tac.getIconForModelSlot(getModelSlotClass());
		}
		return FMLIconLibrary.MODEL_SLOT_ICON;
	}

	@Override
	public String getShortDescription() {
		return getTechnologyAdapter().getLocales().localizedForKeyWithParams("declare_model_slot_($object.simpleName)",
				getModelSlotClass());
	}

}
