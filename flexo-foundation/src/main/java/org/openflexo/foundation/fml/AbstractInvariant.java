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

package org.openflexo.foundation.fml;

import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingModel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * An {@link AbstractInvariant} represents an invariant attached to an {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractInvariant.AbstractInvariantImpl.class)
@Imports({ @Import(SimpleInvariant.class), @Import(IterationInvariant.class) })
public interface AbstractInvariant extends FlexoConceptObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.INVARIANTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	public int getIndex();

	public static abstract class AbstractInvariantImpl extends FlexoConceptObjectImpl implements AbstractInvariant {

		protected static final Logger logger = FlexoLogger.getLogger(AbstractInvariant.class.getPackage().getName());

		private FlexoConcept flexoConcept;

		@Override
		public int getIndex() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getInvariants().indexOf(this);
			}
			return -1;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getBindingModel();
			}
			return null;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			FlexoConcept oldValue = this.flexoConcept;
			BindingModel oldBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			this.flexoConcept = flexoConcept;
			BindingModel newBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, oldBM, newBM);
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, flexoConcept);
		}
	}

}
