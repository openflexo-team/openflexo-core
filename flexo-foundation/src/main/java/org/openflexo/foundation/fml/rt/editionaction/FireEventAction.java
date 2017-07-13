/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.rt.editionaction;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoEventInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * Primitive used to fire a new {@link FlexoEvent}.<br>
 * Life-cycle of event is somewhat different from {@link FlexoConcept} instance, since it's life is restricted to the propagation of the
 * event
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FireEventAction.FireEventActionImpl.class)
@XMLElement
public interface FireEventAction<VMI extends AbstractVirtualModelInstance<VMI, ?>>
		extends AbstractAddFlexoConceptInstance<FlexoConceptInstance, VMI> {

	public FlexoEvent getEventType();

	public static abstract class FireEventActionImpl<VMI extends AbstractVirtualModelInstance<VMI, ?>>
			extends AbstractAddFlexoConceptInstanceImpl<FlexoConceptInstance, VMI> implements FireEventAction<VMI> {

		private static final Logger logger = Logger.getLogger(FireEventAction.class.getPackage().getName());

		@Override
		public String getStringRepresentation() {
			return "fireEvent " + (getEventType() != null ? getEventType().getName() : "null");
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getStringRepresentation(), context);
			return out.toString();
		}

		@Override
		public FlexoEvent getEventType() {
			if (getFlexoConceptType() instanceof FlexoEvent) {
				return (FlexoEvent) getFlexoConceptType();
			}
			return null;
		}

		@Override
		public Class<VMI> getVirtualModelInstanceClass() {
			return (Class) VirtualModelInstance.class;
		}

		@Override
		protected FlexoEventInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) {
			VMI vmi = getVirtualModelInstance(evaluationContext);
			return vmi.makeNewEvent(getEventType());
		}

		@Override
		public FlexoEventInstance execute(RunTimeEvaluationContext evaluationContext) {
			VMI vmi = getVirtualModelInstance(evaluationContext);
			FlexoEventInstance returned = (FlexoEventInstance) super.execute(evaluationContext);

			// And we fire the new event to the listening FMLRunTimeEngine(s)
			vmi.getPropertyChangeSupport().firePropertyChange(VirtualModelInstance.EVENT_FIRED, null, returned);

			return returned;
		}
	}

}
