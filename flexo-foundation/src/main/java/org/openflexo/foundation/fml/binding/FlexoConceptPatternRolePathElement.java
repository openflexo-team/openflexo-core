/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */


package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

public class FlexoConceptPatternRolePathElement<PR extends FlexoRole<?>> extends SimplePathElement {

	private static final Logger logger = Logger.getLogger(FlexoConceptPatternRolePathElement.class.getPackage().getName());

	private final PR flexoRole;

	public FlexoConceptPatternRolePathElement(BindingPathElement parent, PR flexoRole) {
		super(parent, flexoRole.getRoleName(), flexoRole.getType());
		this.flexoRole = flexoRole;
	}

	public PR getFlexoRole() {
		return flexoRole;
	}

	@Override
	public String getLabel() {
		return flexoRole.getRoleName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return flexoRole.getDescription();
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoConceptInstance) {
			FlexoConceptInstance epi = (FlexoConceptInstance) target;
			return epi.getFlexoActor((FlexoRole) flexoRole);
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context) throws TypeMismatchException,
			NullReferenceException {
		if (target instanceof FlexoConceptInstance) {
			((FlexoConceptInstance) target).setFlexoActor(value, (FlexoRole) flexoRole);
			return;
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

}
