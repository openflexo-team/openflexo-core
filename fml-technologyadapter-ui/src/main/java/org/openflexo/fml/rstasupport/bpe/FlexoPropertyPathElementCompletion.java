/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport.bpe;

import java.awt.Graphics;

import javax.swing.Icon;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.MemberCompletion;
import org.openflexo.fml.rstasupport.MethodCompletion;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.view.controller.FlexoController;

/**
 * A completion for a {@link FlexoPropertyBindingVariable}
 *
 * @author sylvain
 */
public class FlexoPropertyPathElementCompletion extends AbstractSimplePathElementCompletion<FlexoPropertyPathElement<?>>
		implements MemberCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	public FlexoPropertyPathElementCompletion(FMLSourceCompletionProvider provider, FlexoPropertyPathElement<?> bindingVariable) {
		super(provider, bindingVariable);
		setRelevance(RELEVANCE);
	}

	public FlexoProperty<?> getFlexoProperty() {
		if (getSimplePathElement() != null) {
			return getSimplePathElement().getFlexoProperty();
		}
		return null;
	}

	public FlexoConcept getFlexoConcept() {
		if (getFlexoProperty() != null) {
			return getFlexoProperty().getFlexoConcept();
		}
		return null;
	}

	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		if (getFlexoConcept() != null) {
			return getFlexoConcept().getName();
		}
		return null;
	}

	@Override
	public Icon getIcon() {
		return FlexoController.statelessIconForObject(getFlexoProperty());
		// return getProvider().getFMLTechnologyAdapterController().getIconForTechnologyObject(getFlexoProperty());
	}

	@Override
	public String getSignature() {
		return getSimplePathElement().getPropertyName();
	}

	@Override
	public String getType() {
		return TypeUtils.simpleRepresentation(getSimplePathElement().getType());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FlexoPropertyPathElementCompletion)
				&& ((FlexoPropertyPathElementCompletion) obj).getSignature().equals(getSignature());
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}

	@Override
	public boolean isDeprecated() {
		// return data.isDeprecated();
		return false;
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		MethodCompletion.rendererText(this, g, x, y, selected);
	}

}