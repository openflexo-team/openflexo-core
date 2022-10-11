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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Implements {@link ActorReference} for {@link FlexoObject} as modelling elements.<br>
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity
@ImplementationClass(ModelObjectActorReference.ModelObjectActorReferenceImpl.class)
@XMLElement
public interface ModelObjectActorReference<T extends FlexoObject> extends ActorReference<T> {

	@PropertyIdentifier(type = FlexoObjectReference.class)
	String OBJECT_REFERENCE_KEY = "objectReference";

	@Getter(value = OBJECT_REFERENCE_KEY, isStringConvertable = true)
	@XMLAttribute
	FlexoObjectReference<T> getObjectReference();

	@Setter(OBJECT_REFERENCE_KEY)
	void setObjectReference(FlexoObjectReference<T> objectReference);

	abstract class ModelObjectActorReferenceImpl<T extends FlexoObject> extends ActorReferenceImpl<T>
			implements ModelObjectActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(ModelObjectActorReference.class.getPackage().toString());

		private boolean isLoading = false;

		@Override
		public void setModellingElement(T object) {
			if (object != null) {
				setObjectReference(new FlexoObjectReference<>(object));
			}
			else {
				setObjectReference(null);
			}
		}

		@Override
		public synchronized T getModellingElement(boolean forceLoading) {
			if (getResourceData() != null && getResourceData().getResource() instanceof PamelaResource
					&& ((PamelaResource<?, ?>) getResourceData().getResource()).isIndexing()) {
				return null;
			}
			if (isLoading) {
				return null;
			}
			else if (getObjectReference() != null) {
				isLoading = true;
				T returned = getObjectReference().getObject(true);
				if (returned == null) {
					logger.warning("Could not retrieve object " + getObjectReference());
				}
				isLoading = false;
				return returned;
			}
			isLoading = false;
			return null;
		}

		@Override
		public String toString() {
			return "ModelObjectActorReference [" + getRoleName() + "] " + Integer.toHexString(hashCode()) + " references "
					+ getModellingElement() + "[reference: " + getObjectReference() + "]";
		}

		@Override
		public String getStringRepresentation() {
			if (getModellingElement() instanceof FlexoConceptInstance) {
				return ((FlexoConceptInstance) getModellingElement()).getStringRepresentationWithID();
			}
			return super.getStringRepresentation();
		}
	}
}
