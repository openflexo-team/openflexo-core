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

import org.openflexo.foundation.fml.rt.FlexoEnumInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * An {@link FlexoEnum} represent an enumeration reflected by a {@link FlexoConcept}
 * 
 * It defines an arbitrary and immutable set of {@link FlexoEnumValue} representing each value this enumeration can take
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoEnum.FlexoEnumImpl.class)
@XMLElement
public interface FlexoEnum extends FlexoConcept {

	@PropertyIdentifier(type = FlexoEnumValue.class, cardinality = Cardinality.LIST)
	public static final String VALUES_KEY = "values";

	@Getter(value = VALUES_KEY, cardinality = Cardinality.LIST, inverse = FlexoEnumValue.FLEXO_ENUM_KEY)
	@Embedded
	@XMLElement(context = "Value_")
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoEnumValue> getValues();

	@Setter(VALUES_KEY)
	public void setValues(List<FlexoEnumValue> values);

	@Adder(VALUES_KEY)
	@PastingPoint
	public void addToValues(FlexoEnumValue aValue);

	@Remover(VALUES_KEY)
	public void removeFromValues(FlexoEnumValue aValue);

	@Reindexer(VALUES_KEY)
	public void moveValueToIndex(FlexoEnumValue aValue, int index);

	@Finder(collection = VALUES_KEY, attribute = FlexoEnumValue.NAME_KEY)
	public FlexoEnumValue getValue(String name);

	public void valueFirst(FlexoEnumValue p);

	public void valueUp(FlexoEnumValue p);

	public void valueDown(FlexoEnumValue p);

	public void valueLast(FlexoEnumValue p);

	@Override
	public FlexoEnumType getInstanceType();

	/**
	 * Return list of run-time instances of this {@link FlexoEnum}.<br>
	 * 
	 * Take care that those instances are shared by the whole application: only one {@link FlexoEnumInstance} might be instantiated for one
	 * {@link FlexoEnumValue} in the JVM
	 * 
	 * @return
	 */
	public List<FlexoEnumInstance> getInstances();

	/**
	 * Return run-time instance matching supplied name
	 * 
	 * @param name
	 * @return
	 */
	public FlexoEnumInstance getInstance(String name);

	public static abstract class FlexoEnumImpl extends FlexoConceptImpl implements FlexoEnum {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoEnum.class.getPackage().getName());

		private final FlexoEnumType instanceType = new FlexoEnumType(this);

		/**
		 * Run-time instances of this {@link FlexoEnum}
		 */
		private List<FlexoEnumInstance> instances = new ArrayList<>();

		static ModelFactory RT_FACTORY;

		static {
			try {
				RT_FACTORY = new ModelFactory(FlexoEnumInstance.class);
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}

		// TODO: remove this > not used
		protected void updateValues() {

		}

		@Override
		public void setValues(List<FlexoEnumValue> someValues) {
			performSuperSetter(VALUES_KEY, someValues);
			updateValues();
		}

		@Override
		public void addToValues(FlexoEnumValue value) {
			performSuperAdder(VALUES_KEY, value);
			FlexoEnumInstance newInstance = RT_FACTORY.newInstance(FlexoEnumInstance.class);
			newInstance.setValue(value);
			instances.add(newInstance);
			updateValues();
		}

		@Override
		public void removeFromValues(FlexoEnumValue value) {
			performSuperRemover(VALUES_KEY, value);
			for (FlexoEnumInstance i : new ArrayList<>(instances)) {
				if (i.getValue() == value) {
					instances.remove(i);
				}
			}
			updateValues();
		}

		@Override
		public void valueFirst(FlexoEnumValue p) {
			getValues().remove(p);
			getValues().add(0, p);
			getPropertyChangeSupport().firePropertyChange(VALUES_KEY, null, getValues());
			updateValues();
		}

		@Override
		public void valueUp(FlexoEnumValue p) {
			int index = getValues().indexOf(p);
			if (index > 0) {
				getValues().remove(p);
				getValues().add(index - 1, p);
				getPropertyChangeSupport().firePropertyChange(VALUES_KEY, null, getValues());
				updateValues();
			}
		}

		@Override
		public void valueDown(FlexoEnumValue p) {
			int index = getValues().indexOf(p);
			if (index > -1) {
				getValues().remove(p);
				getValues().add(index + 1, p);
				getPropertyChangeSupport().firePropertyChange(VALUES_KEY, null, getValues());
				updateValues();
			}
		}

		@Override
		public void valueLast(FlexoEnumValue p) {
			getValues().remove(p);
			getValues().add(p);
			getPropertyChangeSupport().firePropertyChange(VALUES_KEY, null, getValues());
			updateValues();
		}

		@Override
		public FlexoEnumType getInstanceType() {
			return instanceType;
		}

		/*@Override
		public FlexoEnumValue getValue(String name) {
			for (FlexoEnumValue value : getValues()) {
				if (value.getName().equals(name)) {
					return value;
				}
			}
			return null;
		}*/

		@Override
		public FlexoEnumInstance getInstance(String name) {
			for (FlexoEnumInstance instance : getInstances()) {
				if (instance.getValue().getName().equals(name)) {
					return instance;
				}
			}
			return null;
		}

		/**
		 * Return list of run-time instances of this {@link FlexoEnum}.<br>
		 * 
		 * Take care that those instances are shared by the whole application: only one {@link FlexoEnumInstance} might be instantiated for
		 * one {@link FlexoEnumValue} in the JVM
		 * 
		 * @return
		 */
		@Override
		public List<FlexoEnumInstance> getInstances() {
			return instances;
		}

	}

}
