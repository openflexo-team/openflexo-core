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

import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * {@link PamelaModelFactory} used to handle {@link VirtualModelInstance} models<br>
 * Only one instance of this class should be used in a session
 * 
 * @author sylvain
 * 
 */
public class FMLRTVirtualModelInstanceModelFactory extends AbstractVirtualModelInstanceModelFactory<FMLRTVirtualModelInstanceResource> {

	public FMLRTVirtualModelInstanceModelFactory(FMLRTVirtualModelInstanceResource resource, EditingContext editingContext,
			TechnologyAdapterService taService) throws ModelDefinitionException {
		super(resource, FMLRTVirtualModelInstance.class, editingContext, taService);
	}

	/**
	 * General API to instantiate a {@link FlexoConceptInstance} conform to a given {@link FlexoConcept} in a container.<br>
	 * Default {@link CreationScheme} will be used and executed for the initialization of the instance, after the initialization of default
	 * values. If no default {@link CreationScheme} is declared, none will be executed
	 * 
	 * @param concept
	 *            type of {@link FlexoConceptInstance} to be instantiated
	 * @param container
	 *            the container of the new instance (might be null if this instance is a root level of owner {@link VirtualModelInstance})
	 * @param ownerVirtualModelInstance
	 *            the {@link VirtualModelInstance} where the instance is to be initialized (cannot be null)
	 * @param evaluationContext
	 *            {@link RunTimeEvaluationContext} providing executing environment for default values calculation
	 * @return
	 * @throws FMLExecutionException
	 */
	public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept, FlexoConceptInstance container,
			VirtualModelInstance<?, ?> ownerVirtualModelInstance, RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
		return makeNewFlexoConceptInstance(concept, container, ownerVirtualModelInstance, null, evaluationContext);
	}

	/**
	 * General API to instantiate a {@link FlexoConceptInstance} conform to a given {@link FlexoConcept} in a container.<br>
	 * Provides a {@link CreationScheme} to be executed for the initialization of the instance, which will be executed after the
	 * initialization of default values
	 * 
	 * @param concept
	 *            type of {@link FlexoConceptInstance} to be instantiated
	 * @param container
	 *            the container of the new instance (might be null if this instance is a root level of owner {@link VirtualModelInstance})
	 * @param ownerVirtualModelInstance
	 *            the {@link VirtualModelInstance} where the instance is to be initialized (cannot be null)
	 * @param creationScheme
	 *            when not null, {@link CreationScheme} to be executed for the initialization of the instance
	 * @param evaluationContext
	 *            {@link RunTimeEvaluationContext} providing executing environment for default values calculation, as well as
	 *            {@link CreationScheme} execution
	 * @return
	 * @throws FMLExecutionException
	 */
	public FlexoConceptInstance makeNewFlexoConceptInstance(FlexoConcept concept, FlexoConceptInstance container,
			VirtualModelInstance<?, ?> ownerVirtualModelInstance, AbstractCreationScheme creationScheme,
			RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

		// Perform some checks
		if (concept == null) {
			throw new FMLExecutionException("Cannot instanciate a FlexoConceptInstance with null FlexoConcept");
		}
		if (ownerVirtualModelInstance == null) {
			throw new FMLExecutionException("Cannot instanciate a FlexoConceptInstance in null ownerVirtualModelInstance");
		}
		if (!ownerVirtualModelInstance.getVirtualModel().isAssignableFrom(concept.getDeclaringCompilationUnit().getVirtualModel())) {
			throw new FMLExecutionException("Cannot instanciate a FlexoConceptInstance : invalid FlexoConcept declaring compilation unit");
		}
		if (container == null && !concept.isRoot()) {
			// check that the FlexoConcept is root
			throw new FMLExecutionException("Cannot instanciate a FlexoConceptInstance : not root FlexoConcept");
		}
		if (container != null && container != ownerVirtualModelInstance
				&& !container.getFlexoConcept().isAssignableFrom(concept.getContainerFlexoConcept())) {
			// check that the container is valid
			throw new FMLExecutionException("Cannot instanciate a FlexoConceptInstance : invalid FlexoConcept container");
		}

		// Then create the new FlexoConceptInstance
		FlexoConceptInstance returned = buildNewFlexoConceptInstance(concept);

		// If container is not null, add it to the container
		if (container != null && container != ownerVirtualModelInstance) {
			container.addToEmbeddedFlexoConceptInstances(returned);
		}

		// Don't forget to declate it in the owner VirtualModelInstance
		ownerVirtualModelInstance.addToFlexoConceptInstances(returned);

		// Preferably use supplied evaluation context
		if (evaluationContext == null) {
			// evaluationContext = returned;
		}

		// Initialize default values
		returned.initializeDefaultValues(evaluationContext);

		executeCreationScheme(returned, creationScheme, evaluationContext);

		return returned;

	}

	/**
	 * General API to instantiate a {@link FlexoEventInstance} conform to a given {@link FlexoEvent} in a container.<br>
	 * 
	 * @param event
	 *            type of {@link FlexoEventInstance} to be instantiated
	 * @param ownerVirtualModelInstance
	 *            the {@link VirtualModelInstance} where the instance is to be initialized (cannot be null)
	 * @param evaluationContext
	 *            {@link RunTimeEvaluationContext} providing executing environment for default values calculation
	 * @return
	 * @throws FMLExecutionException
	 */
	public FlexoEventInstance makeNewEventInstance(FlexoEvent event, VirtualModelInstance<?, ?> ownerVirtualModelInstance,
			AbstractCreationScheme creationScheme, RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

		FlexoEventInstance returned = newInstance(FlexoEventInstance.class);
		returned.setFlexoConcept(event);
		returned.setSourceVirtualModelInstance(ownerVirtualModelInstance);

		executeCreationScheme(returned, creationScheme, evaluationContext);

		return returned;
	}

	private FlexoConceptInstance buildNewFlexoConceptInstance(FlexoConcept concept) {

		FlexoConceptInstance returned = newInstance(FlexoConceptInstance.class);
		returned.setFlexoConcept(concept);

		return returned;
	}

}
