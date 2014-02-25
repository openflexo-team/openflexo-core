package org.openflexo.foundation.viewpoint.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.viewpoint.FlexoRole;

public class PatternRoleBindingVariable extends BindingVariable {
	static final Logger logger = Logger.getLogger(PatternRoleBindingVariable.class.getPackage().getName());

	private FlexoRole<?> flexoRole;

	public PatternRoleBindingVariable(FlexoRole<?> patternRole) {
		super(patternRole.getName(), patternRole.getType(), true);
		this.flexoRole = patternRole;
	}

	@Override
	public Type getType() {
		return getFlexoRole().getType();
	}

	public FlexoRole getFlexoRole() {
		return flexoRole;
	}
}