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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;

public class VirtualModelModelSlotPathElement<MS extends ModelSlot> extends SimplePathElement {

	private static final Logger logger = Logger.getLogger(VirtualModelModelSlotPathElement.class.getPackage().getName());

	private final MS modelSlot;

	public VirtualModelModelSlotPathElement(BindingPathElement parent, MS modelSlot) {
		super(parent, modelSlot.getName(), modelSlot.getResultingType());
		this.modelSlot = modelSlot;
	}

	public MS getModelSlot() {
		return modelSlot;
	}

	@Override
	public String getLabel() {
		return modelSlot.getName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return modelSlot.getDescription();
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof AbstractVirtualModelInstance) {
			AbstractVirtualModelInstance<?, ?> vmi = (AbstractVirtualModelInstance) target;
			ModelSlotInstance<?, ?> msi = vmi.getModelSlotInstance(modelSlot);
			if (msi != null) {
				return msi.getAccessedResourceData();
			}
			return null;
		}
		logger.warning("Please implement me, modelSlot=" + modelSlot + " target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException {

		if (target instanceof AbstractVirtualModelInstance) {

			System.out.println("OK, on tente de mettre la valeur suivante a " + modelSlot + " : " + value);

			AbstractVirtualModelInstance<?, ?> vmi = (AbstractVirtualModelInstance) target;
			vmi.setFlexoPropertyValue(modelSlot, value);
			return;
		}

		logger.warning("Please implement me, modelSlot=" + modelSlot + " target=" + target + " context=" + context);
	}

}
