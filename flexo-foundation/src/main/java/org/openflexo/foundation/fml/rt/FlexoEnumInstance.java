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

import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link FlexoEnumInstance} is the run-time concept (instance) of an {@link FlexoFlexoEnum}.<br>
 * 
 * This is the run-time concept of a {@link FlexoEnumValue}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoEnumInstance.FlexoEnumInstanceImpl.class)
@XMLElement
public interface FlexoEnumInstance extends FlexoConceptInstance {

	@Override
	public FlexoEnum getFlexoConcept();

	public FlexoEnum getFlexoEnum();

	public FlexoEnumValue getValue();

	public static abstract class FlexoEnumInstanceImpl extends FlexoConceptInstanceImpl implements FlexoEnumInstance {

		private static final Logger logger = FlexoLogger.getLogger(FlexoEnumInstance.class.getPackage().toString());

		private FlexoEnumValue value;

		@Override
		public FlexoEnum getFlexoEnum() {
			return getFlexoConcept();
		}

		@Override
		public FlexoEnum getFlexoConcept() {
			return (FlexoEnum) super.getFlexoConcept();
		}

		@Override
		public FlexoEnumValue getValue() {
			if (value == null && getFlexoEnum() != null && getFlexoEnum().getValues().size() > 0) {
				return getFlexoEnum().getValues().get(0);
			}
			return value;
		}

		public void setValue(FlexoEnumValue value) {
			if ((value == null && this.value != null) || (value != null && !value.equals(this.value))) {
				FlexoEnumValue oldValue = this.value;
				this.value = value;
				getPropertyChangeSupport().firePropertyChange("value", oldValue, value);
			}
		}

	}
}
