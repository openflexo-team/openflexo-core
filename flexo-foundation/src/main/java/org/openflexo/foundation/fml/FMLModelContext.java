/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.annotations.FMLAttribute.AttributeKind;
import org.openflexo.foundation.fml.annotations.UsageExample;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * 
 * @author sylvain
 *
 */
public class FMLModelContext {

	protected static final Logger logger = Logger.getLogger(FMLModelContext.class.getPackage().getName());

	public static class FMLEntity<I extends FMLObject> {
		private ModelEntity<I> modelEntity;
		private FML fmlAnnotation;
		private List<FMLProperty<? super I, ?>> properties;

		private FMLEntity(FML fmlAnnotation, ModelEntity<I> modelEntity) {
			this.fmlAnnotation = fmlAnnotation;
			this.modelEntity = modelEntity;
			properties = new ArrayList<>();
			try {
				Iterator<ModelProperty<? super I>> iterator = modelEntity.getProperties();
				while (iterator.hasNext()) {
					ModelProperty property = iterator.next();
					// System.out.println("> property: " + property.getGetterMethod());
					Method getterMethod = property.getGetterMethod();
					if (getterMethod.isAnnotationPresent(FMLAttribute.class)) {
						FMLProperty<? super I, ?> fmlProperty = new FMLProperty<I, Object>(property,
								getterMethod.getAnnotation(FMLAttribute.class));
						properties.add(fmlProperty);
					}
				}
			} catch (ModelDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Collections.sort(properties, new Comparator<FMLProperty<? super I, ?>>() {
				@Override
				public int compare(FMLProperty<? super I, ?> o1, FMLProperty<? super I, ?> o2) {
					if (o1.isRequired() && !o2.isRequired()) {
						return -1;
					}
					if (!o1.isRequired() && o2.isRequired()) {
						return 1;
					}

					if (o1.getIndex() == o2.getIndex()) {
						return Collator.getInstance().compare(o1.getLabel(), o2.getLabel());
					}
					else {
						return o1.getIndex() - o2.getIndex();
					}
				}
			});
		}

		public FML getFmlAnnotation() {
			return fmlAnnotation;
		}

		public UsageExample[] getFMLExamples() {
			return fmlAnnotation.examples();
		}

		public List<FMLProperty<? super I, ?>> getProperties() {
			return properties;
		}

		/*public List<FMLPropertyValue<? super I, ?>> getFMLPropertyValues(I object) {
		
			List<FMLPropertyValue<? super I, ?>> returned = new ArrayList<>();
		
			for (FMLProperty<? super I, ?> fmlProperty : properties) {
				Object value = fmlProperty.get(object);
				FMLPropertyValue<? super I, ?> propertyValue = null;
				switch (fmlProperty.getKind()) {
					case PropertyValue:
						propertyValue = object.getFMLModelFactory().newSimplePropertyValue((FMLProperty) fmlProperty, value);
						returned.add(propertyValue);
					default:
		
				}
			}
			return returned;
		}*/

		@Override
		public String toString() {
			return "FMLEntity[" + modelEntity.getImplementedInterface().getSimpleName() + "]";
		}
	}

	public static class FMLProperty<I extends FMLObject, T> {
		private ModelProperty<I> modelProperty;
		private FMLAttribute fmlAnnotation;

		public FMLProperty(ModelProperty<I> modelProperty, FMLAttribute fmlAnnotation) {
			super();
			this.modelProperty = modelProperty;
			this.fmlAnnotation = fmlAnnotation;
		}

		@Override
		public String toString() {
			return "FMLProperty[" + getName() + "/" + getType() + "/required=" + isRequired() + "]";
		}

		public String getName() {
			return modelProperty.getPropertyIdentifier();
		}

		public String getLabel() {
			return fmlAnnotation.value();
		}

		public String getDescription() {
			return fmlAnnotation.description();
		}

		public Type getType() {
			return modelProperty.getType();
		}

		public AttributeKind getKind() {
			return fmlAnnotation.kind();
		}

		public boolean isRequired() {
			return fmlAnnotation.required();
		}

		public int getIndex() {
			return fmlAnnotation.index();
		}

		public ModelProperty<I> getModelProperty() {
			return modelProperty;
		}

		public String getPathNameInUsage() {
			return getLabel() + "_path";
		}

		public T getDefaultValue(ModelFactory factory) {
			try {
				return (T) getModelProperty().getDefaultValue(factory);
			} catch (InvalidDataException e) {
				e.printStackTrace();
				return null;
			}
		}

		public T get(I object) {
			if (object instanceof WrappedFMLObject) {
				object = (I) ((WrappedFMLObject) object).getObject();
			}
			try {
				return (T) modelProperty.getGetterMethod().invoke(object, null);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				logger.warning("IllegalArgumentException thrown while calling " + modelProperty.getGetterMethod() + " from " + object);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public void set(T value, I object) {
			if (object instanceof WrappedFMLObject) {
				object = (I) ((WrappedFMLObject) object).getObject();
			}
			try {
				modelProperty.getSetterMethod().invoke(object, value);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void addTo(T value, I object) {
			if (object instanceof WrappedFMLObject) {
				object = (I) ((WrappedFMLObject) object).getObject();
			}
			try {
				modelProperty.getAdderMethod().invoke(object, value);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public FMLPropertyValue<I, T> makeFMLPropertyValue(I object) {

			T value = get(object);
			if (value instanceof DataBinding && !((DataBinding) value).isSet()) {
				// Prevent empty DataBinding to be serialized
				return null;
			}
			if (object.getFMLModelFactory() == null) {
				logger.warning("No FMLModelFactory for " + object);
				return null;
			}
			FMLPropertyValue<? super I, ?> propertyValue = null;
			switch (getKind()) {
				case PropertyValue:
					return object.getFMLModelFactory().newSimplePropertyValue(this, object, value);
				case Instance:
					if (value instanceof FMLObject) {
						FMLInstancePropertyValue returnedInstance = object.getFMLModelFactory().newInstancePropertyValue((FMLProperty) this,
								object);
						returnedInstance.setInstance(object.getFMLModelFactory().getWrappedFMLObject((FMLObject) value));
						return returnedInstance;
					}
					else {
						logger.warning("Unexpected value in FMLInstancePropertyValue: " + value);
						return null;
					}
				case InstancesList:
					if (value instanceof List) {
						FMLInstancesListPropertyValue returnedInstancesList = object.getFMLModelFactory()
								.newInstancesListPropertyValue((FMLProperty) this, object);
						for (FMLObject o : ((List<FMLObject>) value)) {
							returnedInstancesList.addToInstances(object.getFMLModelFactory().getWrappedFMLObject(o));
						}
						return returnedInstancesList;
					}
					else {
						logger.warning("Unexpected value in FMLInstancesListPropertyValue: " + value);
						return null;
					}
				default:

			}
			return null;
		}

	}

	private static Map<Class<?>, FMLEntity<?>> fmlEntities = new HashMap<>();

	public static <I extends FMLObject> FMLEntity<I> getFMLEntity(Class<I> implementedInterface, FMLModelFactory modelFactory) {
		FMLEntity returned = fmlEntities.get(implementedInterface);
		if (returned == null) {
			if (modelFactory == null) {
				logger.warning("Cannot find FMLEntity: modelFactory is null");
				return null;
			}
			FML fmlAnnotation = implementedInterface.getAnnotation(FML.class);
			if (fmlAnnotation == null) {
				// logger.warning("Cannot find @FML annotation in " + implementedInterface);
				// Thread.dumpStack();
				return null;
			}
			ModelEntity<I> modelEntity = modelFactory.getModelContext().getModelEntity(implementedInterface);
			// System.out.println("modelEntity=" + modelEntity);

			returned = new FMLEntity<I>(fmlAnnotation, modelEntity);
			fmlEntities.put(implementedInterface, returned);

		}
		return returned;
	}

}
