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

package org.openflexo.foundation.fml.rt.reflect;

import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * {@link PamelaModelFactory} used to handle {@link ReflectedVirtualModelInstance} models<br>
 * 
 * @author sylvain
 * 
 */
public abstract class ReflectedVirtualModelInstanceModelFactory<R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>, S>
		extends AbstractVirtualModelInstanceModelFactory<R> {

	public ReflectedVirtualModelInstanceModelFactory(R virtualModelInstanceResource,
			Class<? extends VirtualModelInstance<?, ?>> baseVMIClass, EditingContext editingContext, TechnologyAdapterService taService)
			throws ModelDefinitionException {

		super(virtualModelInstanceResource, baseVMIClass, editingContext, taService);
	}

	public ReflectedFlexoConceptInstance<S> makeNewFlexoConceptInstance(FlexoConcept concept, S supportObject,
			FlexoConceptInstance container, VirtualModelInstance<?, ?> ownerVirtualModelInstance,
			RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
		return makeNewFlexoConceptInstance(concept, supportObject, container, ownerVirtualModelInstance, null, evaluationContext);
	}

	public abstract ReflectedFlexoConceptInstance<S> makeNewFlexoConceptInstance(FlexoConcept concept, S supportObject,
			FlexoConceptInstance container, VirtualModelInstance<?, ?> ownerVirtualModelInstance, AbstractCreationScheme creationScheme,
			RunTimeEvaluationContext evaluationContext) throws FMLExecutionException;

}
