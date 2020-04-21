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

package org.openflexo.foundation.technologyadapter;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.ta.FMLModelSlot;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * 
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(UseModelSlotDeclaration.UseModelSlotDeclarationImpl.class)
@XMLElement
public interface UseModelSlotDeclaration extends FlexoObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLCompilationUnit.class)
	public static final String COMPILATION_UNIT_KEY = "compilationUnit";
	@PropertyIdentifier(type = Class.class)
	public static final String MODEL_SLOT_CLASS_KEY = "modelSlotClass";
	@PropertyIdentifier(type = String.class)
	public static final String ABBREV_KEY = "abbrev";

	@Getter(value = MODEL_SLOT_CLASS_KEY)
	@XMLAttribute
	public Class<? extends ModelSlot<?>> getModelSlotClass();

	@Setter(MODEL_SLOT_CLASS_KEY)
	public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass);

	@Getter(value = COMPILATION_UNIT_KEY, inverse = VirtualModel.USE_DECLARATIONS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLCompilationUnit getCompilationUnit();

	@Setter(COMPILATION_UNIT_KEY)
	public void setCompilationUnit(FMLCompilationUnit compilationUnit);

	@Getter(value = ABBREV_KEY)
	@XMLAttribute
	public String getAbbrev();

	@Setter(ABBREV_KEY)
	public void setAbbrev(String abbrev);

	public abstract class UseModelSlotDeclarationImpl extends FMLObjectImpl implements UseModelSlotDeclaration {

		@Deprecated
		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			return getCompilationUnit();
		}

		@Override
		public String toString() {
			return "UseModelSlotDeclaration:" + getModelSlotClass();
		}

		@Override
		public String getAbbrev() {
			String returned = (String) performSuperGetter(ABBREV_KEY);
			if (returned == null && getModelSlotClass() != null) {
				if (FMLRTVirtualModelInstanceModelSlot.class.isAssignableFrom(getModelSlotClass())) {
					return "FMLRT";
				}
				if (FMLModelSlot.class.isAssignableFrom(getModelSlotClass())) {
					return "FMLRT";
				}
				if (getModelSlotClass() != null && getCompilationUnit() != null) {
					TechnologyAdapter ta = getCompilationUnit().getTechnologyAdapterService()
							.getTechnologyAdapterForModelSlot(getModelSlotClass());
					if (ta != null) {
						return ta.getIdentifier();
					}
				}
			}
			return returned;
		}
	}
}
