/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.ontology;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;

import org.openflexo.foundation.fml.TechnologyAdapterTypeFactory;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyTechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner;

/**
 * An abstract type defined as an {@link IFlexoOntologyIndividual} of a given {@link IFlexoOntologyClass}
 * 
 * @author sylvain
 *
 * @param <TA>
 *            technology adapter class
 * @param <I>
 *            type of {@link IFlexoOntologyIndividual}
 * @param <C>
 *            type of {@link IFlexoOntologyClass}
 */
public abstract class IndividualOfClass<TA extends TechnologyAdapter<TA>, I extends IFlexoOntologyIndividual<TA>, C extends IFlexoOntologyClass<TA>>
		implements TechnologySpecificType<TA> {

	public static <TA extends TechnologyAdapter<TA>, I extends IFlexoOntologyIndividual<TA>, C extends IFlexoOntologyClass<TA>> IndividualOfClass<TA, I, C> getIndividualOfClass(
			C anOntologyClass) {
		if (anOntologyClass == null) {
			return null;
		}
		return (IndividualOfClass<TA, I, C>) ((FlexoOntologyTechnologyContextManager<TA>) anOntologyClass.getTechnologyAdapter()
				.getTechnologyContextManager()).getIndividualOfClass(anOntologyClass);
	}

	/**
	 * Factory for IndividualOfClass instances
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract static class IndividualOfClassTypeFactory<TA extends TechnologyAdapter<TA>, I extends IFlexoOntologyIndividual<TA>, C extends IFlexoOntologyClass<TA>, IC extends IndividualOfClass<TA, I, C>>
			extends TechnologyAdapterTypeFactory<IC, TA> implements ReferenceOwner {

		public IndividualOfClassTypeFactory(TA technologyAdapter) {
			super(technologyAdapter);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public abstract Class<IC> getCustomType();

		public IC getIndividualOfClass(C anOntologyClass) {
			if (anOntologyClass == null) {
				return null;
			}
			return (IC) ((FlexoOntologyTechnologyContextManager<TA>) anOntologyClass.getTechnologyAdapter().getTechnologyContextManager())
					.getIndividualOfClass(anOntologyClass);
		}

		@Override
		public IC makeCustomType(String configuration) {

			FlexoObjectReference<C> reference = new FlexoObjectReference<>(configuration, this);

			C ontologyClass = reference.getObject();

			if (ontologyClass != null) {
				return getIndividualOfClass(ontologyClass);
			}
			return null;
		}

		@Override
		public void configureFactory(IC type) {
		}

		@Override
		public void notifyObjectLoaded(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectCantBeFound(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectDeleted(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {
		}

	}

	private final C ontologyClass;
	private final PropertyChangeSupport pcSupport;

	public IndividualOfClass(C anOntologyClass) {
		pcSupport = new PropertyChangeSupport(this);
		this.ontologyClass = anOntologyClass;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public C getOntologyClass() {
		if (ontologyClass != null) {
			return ontologyClass;
		}
		return null;
	}

	@Override
	public abstract Class<? extends I> getBaseClass();

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof IndividualOfClass) {
			return ontologyClass.isSuperConceptOf(((IndividualOfClass<TA, I, C>) aType).getOntologyClass());
		}
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof IFlexoOntologyIndividual)) {
			return false;
		}
		// TODO please implement me
		return true;
	}

	@Override
	public String getSerializationRepresentation() {
		return new FlexoObjectReference<>(ontologyClass).getStringRepresentation();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyClass() != null) {
			return getOntologyClass().getTechnologyAdapter();
		}
		return null;
	}

	@Override
	public boolean isResolved() {
		return ontologyClass != null;
	}

	@Override
	public void resolve() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ontologyClass == null) ? 0 : ontologyClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndividualOfClass<TA, I, C> other = (IndividualOfClass<TA, I, C>) obj;
		if (ontologyClass == null) {
			if (other.ontologyClass != null)
				return false;
		}
		else if (!ontologyClass.equals(other.ontologyClass))
			return false;
		return true;
	}

}
