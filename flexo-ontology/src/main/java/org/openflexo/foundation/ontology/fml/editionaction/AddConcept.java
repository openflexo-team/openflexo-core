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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(AddConcept.AddConceptImpl.class)
public abstract interface AddConcept<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T>
		extends TechnologySpecificAction<MS, M, T> {

	public static abstract class AddConceptImpl<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T>
			extends TechnologySpecificActionImpl<MS, M, T> implements AddConcept<MS, M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(AddConcept.class.getPackage().getName());

		public AddConceptImpl() {
			super();
		}

		public abstract IFlexoOntologyClass<?> getOntologyClass();

		public abstract void setOntologyClass(IFlexoOntologyClass<?> ontologyClass);

		/*public IFlexoOntologyConcept getOntologyObject(FlexoProject project)
		{
			getCalc().loadWhenUnloaded();
			if (StringUtils.isEmpty(getConceptURI())) return null;
			return project.getOntologyLibrary().getOntologyObject(getConceptURI());
		}*/

		/*@Override
		public R getPatternRole() {
			try {
				return super.getPatternRole();
			} catch (ClassCastException e) {
				logger.warning("Unexpected pattern property type");
				setPatternRole(null);
				return null;
			}
		}*/

		// FIXME: if we remove this useless code, some FIB won't work (see FlexoConceptView.fib, inspect an AddIndividual)
		// Need to be fixed in KeyValueProperty.java
		/*@Override
		public void setPatternRole(R patternRole) {
			super.setPatternRole(patternRole);
		}*/

		@Override
		public abstract Type getAssignableType();

		/**
		 * Overrides parent method by returning default model slot if model slot is not defined for this action
		 */
		/*@Override
		public MS getModelSlot() {
			MS returned = (MS) performSuperGetter(MODEL_SLOT_KEY);
			if (returned == null && getOwningVirtualModel() != null) {
				@SuppressWarnings("rawtypes")
				List<TypeAwareModelSlot> msList = getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class);
				if (msList.size() > 0) {
					return (MS) msList.get(0);
				}
			}
			return returned;
		}
		
		@Override
		public void setModelSlot(MS modelSlot) {
			performSuperSetter(MODEL_SLOT_KEY, modelSlot);
		}*/

		@Override
		public TypeAwareModelSlotInstance<?, ?, MS> getModelSlotInstance(RunTimeEvaluationContext evaluationContext) {
			return (TypeAwareModelSlotInstance<?, ?, MS>) super.getModelSlotInstance(evaluationContext);
		}

	}
}
