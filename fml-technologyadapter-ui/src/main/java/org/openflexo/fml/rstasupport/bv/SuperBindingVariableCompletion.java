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
package org.openflexo.fml.rstasupport.bv;

import java.awt.Graphics;

import javax.swing.Icon;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.MemberCompletion;
import org.openflexo.fml.rstasupport.MethodCompletion;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.SuperBindingVariable;
import org.openflexo.view.controller.FlexoController;

/**
 * A completion for a {@link SuperBindingVariable}
 *
 * @author sylvain
 */
public class SuperBindingVariableCompletion extends AbstractBindingVariableCompletion<SuperBindingVariable> implements MemberCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	public SuperBindingVariableCompletion(FMLSourceCompletionProvider provider, SuperBindingVariable bindingVariable) {
		super(provider, bindingVariable);
		setRelevance(RELEVANCE);
	}

	public FlexoConcept getSuperConcept() {
		if (getBindingVariable() != null) {
			return getBindingVariable().getSuperConcept();
		}
		return null;
	}

	public VirtualModel getOwner() {
		if (getSuperConcept() != null) {
			return getSuperConcept().getOwner();
		}
		return null;
	}

	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		if (getOwner() != null) {
			return getOwner().getName();
		}
		return "-";
	}

	@Override
	public Icon getIcon() {
		return FlexoController.statelessIconForObject(getSuperConcept());
		// return getProvider().getFMLTechnologyAdapterController().getIconForTechnologyObject(getFlexoProperty());
	}

	@Override
	public String getSignature() {
		return getBindingVariable().getVariableName();
	}

	@Override
	public String getType() {
		return TypeUtils.simpleRepresentation(getBindingVariable().getType());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof SuperBindingVariableCompletion)
				&& ((SuperBindingVariableCompletion) obj).getSignature().equals(getSignature());
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

	@Override
	public String getSummary() {
		return "<html>Access to current instance using super concept</html>";
	}

}
