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
import org.openflexo.foundation.fml.FlexoRole;
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
public class FlexoRoleDeclarationCompletion<FR extends FlexoRole<?>> extends FMLPropertyDeclarationCompletion<FR> {

	private static String getTemplate(FMLSourceCompletionProvider provider, Class<? extends FlexoRole<?>> roleClass) {
		String dataType = TypeUtils.simpleRepresentation(getFlexoRoleDataType(roleClass));
		String parametersTemplate = getParametersTemplate(provider, roleClass);
		return "${" + dataType + "} ${role} with " + roleClass.getSimpleName() + "(" + parametersTemplate + ");";
	}

	private static Type getFlexoRoleDataType(Class<? extends FlexoRole<?>> roleClass) {
		return TypeUtils.getTypeArgument(roleClass, FlexoRole.class, 0);
	}

	private static TechnologyAdapter<?> getTechnologyAdapter(FMLSourceCompletionProvider provider,
			Class<? extends FlexoRole<?>> roleClass) {
		Class<? extends ModelSlot<?>> msClass = provider.getServiceManager().getTechnologyAdapterService().getModelSlotClass(roleClass);
		return provider.getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(msClass);
	}

	public FlexoRoleDeclarationCompletion(FMLSourceCompletionProvider provider, String inputText, Class<FR> roleClass) {
		super(provider, "declare " + roleClass.getSimpleName(), "declare " + roleClass.getSimpleName(), getTemplate(provider, roleClass),
				roleClass);
	}

	public Class<FR> getRoleClass() {
		return getPropertyClass();
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		return getTechnologyAdapter(getProvider(), getRoleClass());
	}

	@Override
	public Icon getIcon() {

		TechnologyAdapter modelSlotTA = getTechnologyAdapter();
		TechnologyAdapterControllerService tacService = getServiceManager().getService(TechnologyAdapterControllerService.class);
		TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(modelSlotTA);
		if (tac != null) {
			return tac.getIconForFlexoRole(getRoleClass());
		}
		return FMLIconLibrary.FLEXO_ROLE_ICON;
	}

	@Override
	public String getShortDescription() {
		return getTechnologyAdapter().getLocales().localizedForKeyWithParams("declare_flexo_role_($object.simpleName)", getRoleClass());
	}

}
