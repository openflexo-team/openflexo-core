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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

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

	@Finder(collection = VALUES_KEY, attribute = FlexoEnumValue.NAME_KEY)
	public FlexoEnumValue getValue(String name);

	public void valueFirst(FlexoEnumValue p);

	public void valueUp(FlexoEnumValue p);

	public void valueDown(FlexoEnumValue p);

	public void valueLast(FlexoEnumValue p);

	public static abstract class FlexoEnumImpl extends FlexoConceptImpl implements FlexoEnum {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoEnum.class.getPackage().getName());

		protected void updateValues() {

		}

		@Override
		public void setValues(List<FlexoEnumValue> someValues) {
			performSuperSetter(VALUES_KEY, someValues);
			updateValues();
		}

		@Override
		public void addToValues(FlexoEnumValue Value) {
			performSuperAdder(VALUES_KEY, Value);
			updateValues();
		}

		@Override
		public void removeFromValues(FlexoEnumValue Value) {
			performSuperRemover(VALUES_KEY, Value);
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

	}

}
