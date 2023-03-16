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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.TypingSpace;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link SimplePathElement} representing a {@link FlexoProperty}, as a binding path applicable to an instance of a given
 * {@link FlexoConcept}<br>
 * Type of parent MUST be an instance of {@link FlexoConcept}
 * 
 * @author sylvain
 *
 * @param <P>
 */
@ModelEntity
@ImplementationClass(FlexoPropertyPathElement.FlexoPropertyPathElementImpl.class)
public interface FlexoPropertyPathElement<P extends FlexoProperty<?>> extends FMLObject, SimplePathElement<P>, PropertyChangeListener {

	public P getFlexoProperty();

	public static abstract class FlexoPropertyPathElementImpl<P extends FlexoProperty<?>> extends FMLSimplePathElementImpl<P>
			implements FlexoPropertyPathElement<P> {

		private static final Logger logger = Logger.getLogger(FlexoPropertyPathElement.class.getPackage().getName());

		private Type lastKnownType = null;

		public FlexoPropertyPathElementImpl() {
			super();
		}

		/*public FlexoPropertyPathElementImpl(IBindingPathElement parent, P flexoProperty) {
			super(parent, flexoProperty.getPropertyName(), flexoProperty.getResultingType());
			setProperty(flexoProperty);
		}*/

		@Override
		public void setProperty(P property) {
			super.setProperty(property);
			if (property != null) {
				lastKnownType = property.getResultingType();
			}
		}

		@Override
		public void activate(BindingPath bindingPath) {
			super.activate(bindingPath);
			if (getProperty() != null && getProperty().getPropertyChangeSupport() != null) {
				getProperty().getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			if (getProperty() instanceof FlexoConceptInstanceRole
					&& ((FlexoConceptInstanceRole) getProperty()).getFlexoConceptType() != null) {
				((FlexoConceptInstanceRole) getProperty()).getFlexoConceptType().getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}

		@Override
		public void desactivate() {
			if (getProperty() instanceof FlexoConceptInstanceRole
					&& ((FlexoConceptInstanceRole) getProperty()).getFlexoConceptType() != null) {
				((FlexoConceptInstanceRole) getProperty()).getFlexoConceptType().getPropertyChangeSupport()
						.removePropertyChangeListener(this);
			}
			if (getProperty() != null && getProperty().getPropertyChangeSupport() != null) {
				getProperty().getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			super.desactivate();
		}

		@Override
		public P getFlexoProperty() {
			return getProperty();
		}

		/**
		 * Invoke dynamic binding on supplied {@link FlexoConceptInstance} for declared {@link FlexoProperty}<br>
		 * (find the most specialized property accessible in supplied {@link FlexoConceptInstance} matching API of FlexoProperty this path
		 * element refers to)
		 * 
		 * @param flexoConceptInstance
		 * @return
		 */
		public FlexoProperty<?> getEffectiveProperty(FlexoConceptInstance flexoConceptInstance) {

			/*if (!flexoProperty.getFlexoConcept().isAssignableFrom(flexoConceptInstance.getFlexoConcept())) {
				FlexoConceptInstance container = flexoConceptInstance.getContainerFlexoConceptInstance();
				while (container != null) {
					if (flexoProperty.getFlexoConcept().isAssignableFrom(container.getFlexoConcept())) {
						return container.getFlexoConcept().getAccessibleProperty(flexoProperty.getName());
					}
					container = container.getContainerFlexoConceptInstance();
				}
			}
			
			if (flexoProperty.getFlexoConcept().isAssignableFrom(flexoConceptInstance.getFlexoConcept().getOwner())) {
				return flexoConceptInstance.getFlexoConcept().getOwner().getAccessibleProperty(flexoProperty.getName());
			}*/

			if (getProperty() == null || flexoConceptInstance == null || flexoConceptInstance.getFlexoConcept() == null) {
				return null;
			}
			return flexoConceptInstance.getFlexoConcept().getAccessibleProperty(getProperty().getName());
		}

		@Override
		public String getLabel() {
			return getPropertyName();
		}

		@Override
		public String getTooltipText(Type resultingType) {
			return getProperty().getDescription();
		}

		@Override
		public Type getType() {
			return getFlexoProperty().getResultingType();
		}

		@Override
		public Object getBindingValue(Object target, BindingEvaluationContext context)
				throws TypeMismatchException, NullReferenceException {
			if (target instanceof FlexoConceptInstance) {
				FlexoConceptInstance flexoConceptInstance = (FlexoConceptInstance) target;

				if (flexoConceptInstance.getFlexoConcept() == null) {
					return null;
				}

				// We have to first lookup the exact property to be executed
				// This is where the dynamic binding is evaluated
				// effectiveProperty might not be the property to be executed, if supplied flexoConceptInstance
				// definition override flexoProperty
				FlexoProperty<?> effectiveProperty = getEffectiveProperty(flexoConceptInstance);

				if (effectiveProperty == null) {
					logger.warning("Cannot find property " + getProperty() + " in " + flexoConceptInstance);
				}

				if (effectiveProperty != null
						&& effectiveProperty.getFlexoConcept().isAssignableFrom(flexoConceptInstance.getFlexoConcept().getOwner())) {
					if (effectiveProperty instanceof FlexoRole
							&& ((FlexoRole<?>) effectiveProperty).getCardinality().isMultipleCardinality()) {
						return flexoConceptInstance.getVirtualModelInstance().getFlexoActorList((FlexoRole<?>) effectiveProperty);
					}
					return flexoConceptInstance.getVirtualModelInstance().getFlexoPropertyValue(effectiveProperty);
				}

				// TODO: see https://bugs.openflexo.org/browse/CORE-309
				// Should be aligned with FlexoPropertyBindingVariable#getValue()

				// TO be tested !!!

				/*if (flexoProperty instanceof FlexoRole) {
					if (flexoProperty.getCardinality().isMultipleCardinality())
						return flexoConceptInstance.getFlexoActorList((FlexoRole<?>) flexoProperty);
					return flexoConceptInstance.getFlexoActor((FlexoRole<?>) flexoProperty);
				}
				return flexoConceptInstance.getFlexoPropertyValue(flexoProperty);*/

				if (effectiveProperty instanceof FlexoRole) {
					if (effectiveProperty.getCardinality().isMultipleCardinality()) {
						return flexoConceptInstance.getFlexoActorList((FlexoRole<?>) effectiveProperty);
					}
					else if (effectiveProperty instanceof FlexoRole) {
						return flexoConceptInstance.getFlexoActor((FlexoRole<?>) effectiveProperty);
					}
				}
				else {
					return flexoConceptInstance.getFlexoPropertyValue((FlexoProperty) effectiveProperty);
				}
			}
			logger.warning("Please implement me, target=" + target + " context=" + context);
			return null;
		}

		@Override
		public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
				throws TypeMismatchException, NullReferenceException {
			if (target instanceof FlexoConceptInstance) {
				FlexoConceptInstance flexoConceptInstance = (FlexoConceptInstance) target;
				FlexoProperty<?> effectiveProperty = getEffectiveProperty(flexoConceptInstance);
				flexoConceptInstance.setFlexoPropertyValue((FlexoProperty) effectiveProperty, value);
				return;
			}
			logger.warning("Please implement me, target=" + target + " context=" + context);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == getFlexoProperty()) {
				if (evt.getPropertyName().equals(FlexoProperty.NAME_KEY) || evt.getPropertyName().equals(FlexoProperty.PROPERTY_NAME_KEY)) {
					// System.out.println("Notify name changing for " + getFlexoProperty() + " new=" + getVariableName());
					setParsed(getFlexoProperty().getName());
					getPropertyChangeSupport().firePropertyChange(NAME_PROPERTY, evt.getOldValue(), getLabel());
				}
				if (evt.getPropertyName().equals(TYPE_PROPERTY) || evt.getPropertyName().equals(FlexoProperty.RESULTING_TYPE_PROPERTY)) {
					Type newType = getFlexoProperty().getResultingType();
					if (lastKnownType == null || !lastKnownType.equals(newType)) {
						getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, newType);
						lastKnownType = newType;
					}
				}
				if (lastKnownType != getType()) {
					// We might arrive here only in the case of a FlexoProperty does not correctely notify
					// its type change. We warn it to 'tell' the developper that such notification should be done
					// in FlexoProperty (see IndividualProperty for example)
					// logger.warning("Detecting un-notified type changing for FlexoProperty " + flexoProperty + " from " + lastKnownType +
					// " to
					// "
					// + getType() + ". Trying to handle case.");
					getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
					lastKnownType = getType();
				}
			}
			if (evt.getSource() instanceof FlexoConcept) {
				if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)
						|| evt.getPropertyName().equals(FlexoConcept.FLEXO_BEHAVIOURS_KEY)) {
					isNotifyingBindingPathChanged = true;
					getPropertyChangeSupport().firePropertyChange(BINDING_PATH_CHANGED, false, true);
					isNotifyingBindingPathChanged = false;
				}
			}
		}

		private boolean isNotifyingBindingPathChanged = false;

		@Override
		public boolean isNotifyingBindingPathChanged() {
			return isNotifyingBindingPathChanged;
		}

		/*@Override
		public String toString() {
			return "FlexoPropertyPathElement " + getFlexoProperty().getName() + " (" + getBindingPath() + ")";
		}*/

		@Override
		public boolean isSettable() {
			if (getProperty() != null) {
				if (getProperty().isReadOnly()) {
					System.out.println("Not settable because of " + getProperty() + " readonly");
				}
				return !getProperty().isReadOnly();
			}
			return super.isSettable();
		}

		/**
		 * Return boolean indicating if this {@link BindingPathElement} is notification-safe (all modifications of data are notified using
		 * {@link PropertyChangeSupport} scheme)<br>
		 * 
		 * When tagged as unsafe, disable caching while evaluating related {@link DataBinding}.
		 * 
		 * Otherwise return true
		 * 
		 * @return
		 */
		@Override
		public boolean isNotificationSafe() {
			return getProperty().isNotificationSafe();
		}

		@Override
		public boolean isResolved() {
			return getProperty() != null;
		}

		@Override
		public void resolve() {
			// TODO
		}

		@Override
		public void invalidate() {
			invalidate(null);
		}

		@Override
		public void invalidate(TypingSpace typingSpace) {
			/*if (getType() != null && typingSpace != null) {
				FlexoConceptInstanceType translatedType = getType().translateTo(typingSpace);
				setFunction(null);
				setType(translatedType);
			}*/
			// TODO
		}
	}
}

/*NOT FOUND: public abstract org.openflexo.foundation.FlexoServiceManager org.openflexo.foundation.fml.FMLObject.getServiceManager() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.Class org.openflexo.foundation.fml.FMLObject.getImplementedInterface(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.FMLModelFactory org.openflexo.foundation.fml.FMLObject.getFMLModelFactory() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract boolean org.openflexo.foundation.fml.FMLObject.hasDescription() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract boolean org.openflexo.foundation.fml.FMLObject.hasMetaData(java.lang.String) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.md.BasicMetaData org.openflexo.foundation.fml.FMLObject.getBasicMetaData(java.lang.String) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.setBasicMetaData(java.lang.String) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.Object org.openflexo.foundation.fml.FMLObject.getSingleMetaData(java.lang.String,java.lang.Class) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.setSingleMetaData(java.lang.String,java.lang.Object,java.lang.Class) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.md.MultiValuedMetaData org.openflexo.foundation.fml.FMLObject.getMultiValuedMetaData(java.lang.String) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.md.ListMetaData org.openflexo.foundation.fml.FMLObject.getListMetaData(java.lang.String) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.FMLCompilationUnit org.openflexo.foundation.fml.FMLObject.getDeclaringCompilationUnit() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.rm.CompilationUnitResource org.openflexo.foundation.fml.FMLObject.getDeclaringCompilationUnitResource() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.String org.openflexo.foundation.fml.FMLObject.getStringRepresentation() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.initializeDeserialization(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.finalizeDeserialization() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.notifiedScopeChanged() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.connie.BindingEvaluationContext org.openflexo.foundation.fml.FMLObject.getReflectedBindingEvaluationContext() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.handleRequiredImports(org.openflexo.foundation.fml.FMLCompilationUnit) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.String org.openflexo.foundation.fml.FMLObject.getFMLKeyword(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract boolean org.openflexo.foundation.fml.FMLObject.hasFMLProperties(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.util.List org.openflexo.foundation.fml.FMLObject.getFMLProperties(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.FMLModelContext$FMLProperty org.openflexo.foundation.fml.FMLObject.getFMLProperty(java.lang.String,org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.util.List org.openflexo.foundation.fml.FMLObject.getFMLPropertyValues(org.openflexo.foundation.fml.FMLModelFactory) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.fml.FMLObject.addToFMLPropertyValues(org.openflexo.foundation.fml.FMLPropertyValue) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.WrappedFMLObject org.openflexo.foundation.fml.FMLObject.getWrappedFMLObject(org.openflexo.foundation.fml.FMLObject) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.technologyadapter.TechnologyAdapterService org.openflexo.foundation.fml.FMLObject.getTechnologyAdapterService() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.fml.VirtualModelLibrary org.openflexo.foundation.fml.FMLObject.getVirtualModelLibrary() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.Class org.openflexo.foundation.FlexoObject.getImplementedInterface() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.localization.LocalizedDelegate org.openflexo.foundation.FlexoObject.getLocales() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract long org.openflexo.foundation.FlexoObject.obtainNewFlexoID() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.util.List org.openflexo.foundation.FlexoObject.getActionList() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.FlexoObject.addToReferencers(org.openflexo.foundation.utils.FlexoObjectReference) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.FlexoObject.removeFromReferencers(org.openflexo.foundation.utils.FlexoObjectReference) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.util.List org.openflexo.foundation.FlexoObject.getReferencers() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.foundation.FlexoObject.setIsModified() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.String org.openflexo.foundation.FlexoObject.render() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.lang.String org.openflexo.foundation.FlexoObject.hash() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract java.util.Collection org.openflexo.pamela.validation.Validable.getEmbeddedValidableObjects() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.connie.BindingModel org.openflexo.connie.Bindable.getBindingModel() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.connie.BindingFactory org.openflexo.connie.Bindable.getBindingFactory() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.connie.Bindable.notifiedBindingChanged(org.openflexo.connie.DataBinding) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract void org.openflexo.connie.Bindable.notifiedBindingDecoded(org.openflexo.connie.DataBinding) in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.resource.ResourceData org.openflexo.foundation.InnerResourceData.getResourceData() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
NOT FOUND: public abstract org.openflexo.foundation.technologyadapter.TechnologyAdapter org.openflexo.foundation.technologyadapter.TechnologyObject.getTechnologyAdapter() in interface org.openflexo.foundation.fml.binding.FlexoPropertyPathElement
*/
