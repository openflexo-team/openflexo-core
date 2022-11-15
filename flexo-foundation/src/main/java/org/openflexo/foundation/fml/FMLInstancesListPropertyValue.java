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

package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * A {@link FMLPropertyValue} which has a list of {@link FMLObject} as value (wrapped in a {@link WrappedFMLObject})
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLInstancesListPropertyValue.FMLInstancesListPropertyValueImpl.class)
public interface FMLInstancesListPropertyValue<M extends FMLObject, T extends FMLObject> extends FMLPropertyValue<M, List<T>> {

	@PropertyIdentifier(type = WrappedFMLObject.class, cardinality = Cardinality.LIST)
	public static final String INSTANCES_KEY = "instances";

	@Getter(value = INSTANCES_KEY, cardinality = Cardinality.LIST)
	public List<WrappedFMLObject<T>> getInstances();

	@Setter(INSTANCES_KEY)
	public void setInstances(List<WrappedFMLObject<T>> values);

	@Adder(INSTANCES_KEY)
	public void addToInstances(WrappedFMLObject<T> aValue);

	@Remover(INSTANCES_KEY)
	public void removeFromInstances(WrappedFMLObject<T> aValue);

	@Reindexer(INSTANCES_KEY)
	public void moveInstanceToIndex(WrappedFMLObject<T> aValue, int index);

	public static abstract class FMLInstancesListPropertyValueImpl<M extends FMLObject, T extends FMLObject>
			extends FMLPropertyValueImpl<M, List<T>> implements FMLInstancesListPropertyValue<M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(FMLInstancesListPropertyValue.class.getPackage().getName());

		@Override
		public void applyPropertyValueToModelObject() {
			if (getProperty() != null && getObject() != null) {
				for (T v : getValue()) {
					((FMLProperty) getProperty()).addTo(v, getObject());
				}
			}
		}

		@Override
		public void retrievePropertyValueFromModelObject() {

			if (getProperty() != null && getObject() != null && getObject().getFMLModelFactory() != null) {

				List<WrappedFMLObject<T>> valuesToRemove = new ArrayList<>(getInstances());

				List<T> valuesFromObject = getProperty().get(getObject());
				for (T o : valuesFromObject) {
					WrappedFMLObject<T> wo = getObject().getFMLModelFactory().getWrappedFMLObject(o);
					if (getInstances().contains(wo)) {
						// Already inside
						valuesToRemove.remove(wo);
					}
					else {
						addToInstances(wo);
					}
				}
				for (WrappedFMLObject<T> wo : valuesToRemove) {
					removeFromInstances(wo);
				}
			}
		}

		@Override
		public List<T> getValue() {
			List<T> returned = new ArrayList<>();
			for (WrappedFMLObject<T> wrappedFMLObject : getInstances()) {
				returned.add(wrappedFMLObject.getObject());
			}
			return returned;
		}

		@Override
		public String toString() {

			return "FMLInstancesListPropertyValue[" + (getProperty() != null ? getProperty().getName() : "null") + "=" + getValue() + "]";
		}

	}
}
