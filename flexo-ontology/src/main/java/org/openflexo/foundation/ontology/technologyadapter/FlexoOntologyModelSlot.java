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

package org.openflexo.foundation.ontology.technologyadapter;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.fml.IndividualRole;
import org.openflexo.foundation.ontology.fml.editionaction.AddIndividual;
import org.openflexo.foundation.ontology.fml.rt.FlexoOntologyModelSlotInstance;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * Implementation of a {@link TypeAwareModelSlot} in a given technology conform to {@link IFlexoOntology}<br>
 * We implements here a model/metamodel conformance This model slot provides a symbolic access to a <br>
 * {@link IFlexoOntology} (model) conform to a {@link IFlexoOntology} (meta-model) with basic conformance <br>
 * contract. <br>
 * 
 * @see FlexoModel
 * @see FlexoMetaModel
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoOntologyModelSlot.FlexoOntologyModelSlotImpl.class)
public interface FlexoOntologyModelSlot<M extends FlexoModel<M, MM> & IFlexoOntology<TA>, MM extends FlexoMetaModel<MM> & IFlexoOntology<TA>, TA extends TechnologyAdapter>
		extends TypeAwareModelSlot<M, MM> {

	/**
	 * Instantiate a new IndividualRole
	 * 
	 * @param ontClass
	 * @return
	 */
	public IndividualRole<?> makeIndividualRole(IFlexoOntologyClass ontClass);

	public AddIndividual<? extends FlexoOntologyModelSlot, ?> makeAddIndividualAction(IndividualRole<?> patternRole,
			AbstractCreationScheme creationScheme);

	public static abstract class FlexoOntologyModelSlotImpl<M extends FlexoModel<M, MM> & IFlexoOntology<TA>, MM extends FlexoMetaModel<MM> & IFlexoOntology<TA>, TA extends TechnologyAdapter>
			extends TypeAwareModelSlotImpl<M, MM> implements FlexoOntologyModelSlot<M, MM, TA> {

		private static final Logger logger = Logger.getLogger(FlexoOntologyModelSlot.class.getPackage().getName());

		private FlexoMetaModelResource<M, MM, ?> metaModelResource;
		private String metaModelURI;

		/**
		 * Instanciate a new model slot instance configuration for this model slot
		 */
		@Override
		public abstract ModelSlotInstanceConfiguration<? extends FlexoOntologyModelSlot<M, MM, TA>, M> createConfiguration(
				CreateVirtualModelInstance action);

		/**
		 * Instantiate a new IndividualRole
		 * 
		 * @param ontClass
		 * @return
		 */
		@Override
		public IndividualRole<?> makeIndividualRole(IFlexoOntologyClass ontClass) {
			Class<? extends IndividualRole> individualPRClass = getFlexoRoleClass(IndividualRole.class);
			IndividualRole<?> returned = makeFlexoRole(individualPRClass);
			returned.setOntologicType(ontClass);
			return returned;
		}

		@Override
		public AddIndividual<? extends FlexoOntologyModelSlot, ?> makeAddIndividualAction(IndividualRole<?> patternRole,
				AbstractCreationScheme creationScheme) {
			Class<? extends AddIndividual<? extends FlexoOntologyModelSlot, ?>> addIndividualClass = (Class<? extends AddIndividual<? extends FlexoOntologyModelSlot, ?>>) getEditionActionClass(AddIndividual.class);
			AddIndividual<? extends FlexoOntologyModelSlot, ?> returned = makeEditionAction(addIndividualClass);

			// returned.setAssignation(new DataBinding(patternRole.getRoleName()));
			if (creationScheme.getParameter("uri") != null) {
				returned.setIndividualName(new DataBinding("parameters.uri"));
			}
			return returned;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " type=" + getClass().getSimpleName() + " conformTo=\"" + getMetaModelURI() + "\""
					+ " required=" + getIsRequired() + " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
		}

		public abstract String getURIForObject(
				FlexoOntologyModelSlotInstance<M, MM, ? extends FlexoOntologyModelSlot<M, MM, TA>, TA> msInstance, Object o);

		public abstract Object retrieveObjectWithURI(
				FlexoOntologyModelSlotInstance<M, MM, ? extends FlexoOntologyModelSlot<M, MM, TA>, TA> msInstance, String objectURI);

		/**
		 * Return flag indicating if this model slot implements a strict meta-modelling contract (return true if and only if a model in this
		 * technology can be conform to only one metamodel). Otherwise, this is simple metamodelling (a model is conform to exactely one
		 * metamodel)
		 * 
		 * @return
		 */
		@Override
		public abstract boolean isStrictMetaModelling();

		@Override
		public String getModelSlotDescription() {
			return "Model conform to " + getMetaModelURI();
		}

	}
}
