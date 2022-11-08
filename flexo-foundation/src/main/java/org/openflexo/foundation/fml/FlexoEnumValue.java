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

package org.openflexo.foundation.fml;

import java.util.logging.Logger;

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link FlexoEnumValue} represent the possible value of a {@link FlexoEnum}
 *
 * A {@link FlexoEnumValue} is identified by its name (see {@link #getName()})
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoEnumValue.FlexoEnumValueImpl.class)
@XMLElement
public interface FlexoEnumValue extends FlexoConcept {

	@PropertyIdentifier(type = FlexoEnum.class)
	public static final String FLEXO_ENUM_KEY = "flexoEnum";

	@Getter(value = FLEXO_ENUM_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoEnum getFlexoEnum();

	@Setter(FLEXO_ENUM_KEY)
	public void setFlexoEnum(FlexoEnum flexoEnum);

	public int getIndex();

	public static abstract class FlexoEnumValueImpl extends FlexoConceptImpl implements FlexoEnumValue {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoEnumValue.class.getPackage().getName());

		@Override
		public int getIndex() {
			if (getFlexoEnum() != null) {
				return getFlexoEnum().getValues().indexOf(this);
			}
			return -1;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getFlexoEnum() != null) {
				return getFlexoEnum().getResourceData();
			}
			return null;
		}

		@Override
		public VirtualModel getOwningVirtualModel() {
			if (getFlexoEnum() != null) {
				return getFlexoEnum().getOwner();
			}
			return null;
		}

		@Override
		public void setName(String name) throws InvalidNameException {
			// TODO Auto-generated method stub
			super.setName(name);
		}

	}

}
