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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * 
 * @author sylvain
 *
 */
public class FMLModelContext {

	protected static final Logger logger = Logger.getLogger(FMLModelContext.class.getPackage().getName());

	public static class FMLEntity<I> {
		private ModelEntity<I> modelEntity;
		private FML fmlAnnotation;
		private Set<FMLProperty<? super I, ?>> properties;

		private FMLEntity(FML fmlAnnotation, ModelEntity<I> modelEntity) {
			this.fmlAnnotation = fmlAnnotation;
			this.modelEntity = modelEntity;
			properties = new HashSet<>();
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
		}

		public FML getFmlAnnotation() {
			return fmlAnnotation;
		}

		public Set<FMLProperty<? super I, ?>> getProperties() {
			return properties;
		}

		public Map<FMLProperty<? super I, ?>, Object> getFMLPropertyValues(I object) {
			Map<FMLProperty<? super I, ?>, Object> returned = new HashMap<>();
			for (FMLProperty<? super I, ?> fmlProperty : properties) {
				Object value = fmlProperty.get(object);
				returned.put(fmlProperty, value);
			}
			return returned;
		}

		@Override
		public String toString() {
			return "FMLEntity[" + modelEntity.getImplementedInterface().getSimpleName() + "]";
		}
	}

	public static class FMLProperty<I, T> {
		private ModelProperty<I> modelProperty;
		private FMLAttribute fmlAnnotation;

		public FMLProperty(ModelProperty<I> modelProperty, FMLAttribute fmlAnnotation) {
			super();
			this.modelProperty = modelProperty;
			this.fmlAnnotation = fmlAnnotation;
		}

		public String getName() {
			return modelProperty.getPropertyIdentifier();
		}

		public Type getType() {
			return modelProperty.getType();
		}

		public T get(I object) {
			try {
				return (T) modelProperty.getGetterMethod().invoke(object, null);
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
			return null;
		}

		public void set(T value, I object) {
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
	}

	private static Map<Class<?>, FMLEntity<?>> fmlEntities = new HashMap<>();

	public static <I> FMLEntity<I> getFMLEntity(Class<I> implementedInterface, FMLModelFactory modelFactory) {
		FMLEntity returned = fmlEntities.get(implementedInterface);
		if (returned == null) {
			if (modelFactory == null) {
				logger.warning("Cannot find FMLEntity: modelFactory is null");
				return null;
			}
			FML fmlAnnotation = implementedInterface.getAnnotation(FML.class);
			if (fmlAnnotation == null) {
				logger.warning("Cannot find @FML annotation in " + implementedInterface);
				return null;
			}
			ModelEntity<I> modelEntity = modelFactory.getModelContext().getModelEntity(implementedInterface);
			System.out.println("modelEntity=" + modelEntity);

			returned = new FMLEntity<I>(fmlAnnotation, modelEntity);
			fmlEntities.put(implementedInterface, returned);

		}
		return returned;
	}

}