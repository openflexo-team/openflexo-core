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

package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link FlexoEventInstance} is the run-time concept (instance) of an {@link FlexoEvent}.<br>
 * 
 * As such, a {@link FlexoEventInstance} is instantiated inside a {@link FMLRTVirtualModelInstance}.<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoEventInstance.FlexoEventInstanceImpl.class)
@XMLElement
public interface FlexoEventInstance extends FlexoConceptInstance {

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String SOURCE_VIRTUAL_MODEL_INSTANCE_KEY = "sourceVirtualModelInstance";

	/**
	 * Return the {@link FMLRTVirtualModelInstance} where this FlexoEventInstance was fired
	 * 
	 * @return
	 */
	@Getter(value = SOURCE_VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract VirtualModelInstance<?, ?> getSourceVirtualModelInstance();

	@Setter(SOURCE_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setSourceVirtualModelInstance(VirtualModelInstance<?, ?> virtualModelInstance);

	@Override
	public FlexoEvent getFlexoConcept();

	public FlexoEvent getFlexoEvent();

	public static abstract class FlexoEventInstanceImpl extends FlexoConceptInstanceImpl implements FlexoEventInstance {

		private static final Logger logger = FlexoLogger.getLogger(FlexoEventInstance.class.getPackage().toString());

		@Override
		public FlexoEvent getFlexoEvent() {
			return getFlexoConcept();
		}

		@Override
		public FlexoEvent getFlexoConcept() {
			return (FlexoEvent) super.getFlexoConcept();
		}

		// We override here getOwningVirtualModelInstance() by relying on getSourceVirtualModelInstance() instead of
		// getVirtualModelInstance() which returns
		// null value because FlexoEventInstance is not persistent in the FMLRTVirtualModelInstance
		@Override
		public VirtualModelInstance<?, ?> getOwningVirtualModelInstance() {
			return getSourceVirtualModelInstance();
		}

	}
}
