/**
 * 
 * Copyright (c) 2026, Openflexo
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

import org.openflexo.foundation.fml.FlexoException;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.error.FMLUserError;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * An instance of a {@link FlexoException}, created by {@code new ExceptionType(...)} and raised by {@code throw} (CORE-F-11 phase 7).
 * <p>
 * It is not registered in any virtual model instance. Like any concept instance, its container is its host, found by the climb of its
 * {@code new} ({@link #setHost(FlexoConceptInstance)}), which does not hold it either. Once thrown, it knows the {@link FMLUserError} that carries it (with its FML stack):
 * throwing it again rethrows that very error.
 */
@ModelEntity
@ImplementationClass(FlexoExceptionInstance.FlexoExceptionInstanceImpl.class)
public interface FlexoExceptionInstance extends FlexoConceptInstance {

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String SOURCE_VIRTUAL_MODEL_INSTANCE_KEY = "sourceVirtualModelInstance";

	/**
	 * Return the {@link VirtualModelInstance} in which this exception was created
	 */
	@Getter(value = SOURCE_VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract VirtualModelInstance<?, ?> getSourceVirtualModelInstance();

	@Setter(SOURCE_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setSourceVirtualModelInstance(VirtualModelInstance<?, ?> virtualModelInstance);

	@Override
	public FlexoException getFlexoConcept();

	/**
	 * The message of this exception: its string representation when its exception has a valid renderer, otherwise the name of the
	 * exception and the values of its properties
	 */
	public String getExceptionMessage();

	/**
	 * The error carrying this exception since it was thrown, null before
	 */
	public FMLUserError getThrownError();

	/**
	 * Record the error carrying this exception, when it is thrown. Not persistent: an exception instance lives as long as its propagation
	 * and its handling
	 */
	public void setThrownError(FMLUserError error);

	/**
	 * Set the host of this exception, found by the climb of its {@code new}: an instance of its container concept for a nested exception,
	 * its source virtual model instance for a root one. It is the {@code container} of the exception, which is not registered in it (not
	 * persistent). Set once, by the factory, before the creation scheme runs
	 */
	public void setHost(FlexoConceptInstance host);

	public static abstract class FlexoExceptionInstanceImpl extends FlexoConceptInstanceImpl implements FlexoExceptionInstance {

		private FMLUserError thrownError;

		private FlexoConceptInstance host;

		@Override
		public void setHost(FlexoConceptInstance host) {
			if (this.host != null && this.host != host) {
				throw new IllegalStateException("The host of " + this + " is already set");
			}
			this.host = host;
		}

		// An exception is a concept as the others: its container is its host, although it is not stored in it. Its container instance is that
		// host when it is a concept instance, so that every climb of the containment (property access, actors, new) goes through it
		@Override
		public FlexoConceptInstance getContainerFlexoConceptInstance() {
			if (host != null && !(host instanceof VirtualModelInstance)) {
				return host;
			}
			return (FlexoConceptInstance) performSuperGetter(CONTAINER_FLEXO_CONCEPT_INSTANCE_KEY);
		}

		@Override
		public FlexoConceptInstance getContainer() {
			if (host != null) {
				return host;
			}
			return super.getContainer();
		}

		@Override
		public FlexoException getFlexoConcept() {
			return (FlexoException) super.getFlexoConcept();
		}

		// Not registered in its virtual model instance: its owner is the instance it was created in, as for an event
		@Override
		public VirtualModelInstance<?, ?> getOwningVirtualModelInstance() {
			return getSourceVirtualModelInstance();
		}

		@Override
		public FMLUserError getThrownError() {
			return thrownError;
		}

		@Override
		public void setThrownError(FMLUserError error) {
			this.thrownError = error;
		}

		@Override
		public String getExceptionMessage() {
			if (getFlexoConcept() == null) {
				return "exception";
			}
			if (hasValidRenderer()) {
				return getStringRepresentation();
			}
			StringBuilder sb = new StringBuilder(getFlexoConcept().getName());
			boolean first = true;
			for (FlexoProperty<?> property : getFlexoConcept().getAccessibleProperties()) {
				if (!(property.getFlexoConcept() instanceof FlexoException)) {
					// A property of the model enclosing the exception: not part of the exception
					continue;
				}
				sb.append(first ? "(" : ", ");
				first = false;
				Object value;
				try {
					value = getFlexoPropertyValue(property);
				} catch (RuntimeException e) {
					// The message of an exception must never fail itself
					value = "?";
				}
				sb.append(property.getName()).append('=').append(value instanceof String ? "\"" + value + "\"" : String.valueOf(value));
			}
			if (!first) {
				sb.append(')');
			}
			return sb.toString();
		}
	}
}
