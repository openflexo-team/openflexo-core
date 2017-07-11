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

package org.openflexo.foundation.ontology.fml;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.fml.rt.ConceptActorReference;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(DataPropertyRole.DataPropertyRoleImpl.class)
public abstract interface DataPropertyRole<P extends IFlexoOntologyDataProperty> extends PropertyRole<P> {

	@PropertyIdentifier(type = BuiltInDataType.class)
	public static final String DATA_TYPE_KEY = "dataType";

	@Getter(value = DATA_TYPE_KEY)
	@XMLAttribute
	public BuiltInDataType getDataType();

	@Setter(DATA_TYPE_KEY)
	public void setDataType(BuiltInDataType dataType);

	public static abstract class DataPropertyRoleImpl<P extends IFlexoOntologyDataProperty> extends PropertyRoleImpl<P> implements
			DataPropertyRole<P> {

		private BuiltInDataType dataType;

		public DataPropertyRoleImpl() {
			super();
		}

		@Override
		public Type getType() {
			if (getParentProperty() == null) {
				return IFlexoOntologyDataProperty.class;
			}
			return super.getType();
		}

		@Override
		public String getTypeDescription() {
			if (getParentProperty() != null) {
				return getParentProperty().getName();
			}
			return "";
		}

		@Override
		public IFlexoOntologyDataProperty getParentProperty() {
			return (IFlexoOntologyDataProperty) super.getParentProperty();
		}

		public void setParentProperty(IFlexoOntologyDataProperty ontologyProperty) {
			super.setParentProperty(ontologyProperty);
		}

		@Override
		public BuiltInDataType getDataType() {
			return dataType;
		}

		@Override
		public void setDataType(BuiltInDataType dataType) {
			this.dataType = dataType;
		}

		@Override
		public ActorReference<P> makeActorReference(P object, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			ConceptActorReference<P> returned = factory.newInstance(ConceptActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(object);
			return returned;
		}
	}
}
