/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.viewpoint.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.viewpoint.FlexoRole;

public class FlexoConceptPatternRolePathElement<PR extends FlexoRole<?>> extends SimplePathElement {

	private static final Logger logger = Logger.getLogger(FlexoConceptPatternRolePathElement.class.getPackage().getName());

	private PR patternRole;

	public FlexoConceptPatternRolePathElement(BindingPathElement parent, PR patternRole) {
		super(parent, patternRole.getRoleName(), patternRole.getType());
		this.patternRole = patternRole;
	}

	@Override
	public String getLabel() {
		return patternRole.getRoleName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return patternRole.getDescription();
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoConceptInstance) {
			FlexoConceptInstance epi = (FlexoConceptInstance) target;
			return epi.getFlexoActor((FlexoRole) patternRole);
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context) throws TypeMismatchException,
			NullReferenceException {
		if (target instanceof FlexoConceptInstance) {
			((FlexoConceptInstance) target).setFlexoActor(value, (FlexoRole) patternRole);
			return;
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

}