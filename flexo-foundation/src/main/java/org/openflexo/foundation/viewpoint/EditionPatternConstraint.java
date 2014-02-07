/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.viewpoint;

import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * An EditionPatternConstraint represents a structural constraint attached to an FlexoConcept
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(EditionPatternConstraint.EditionPatternConstraintImpl.class)
@XMLElement(xmlTag = "Constraint")
public interface EditionPatternConstraint extends EditionPatternObject {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String EDITION_PATTERN_KEY = "flexoConcept";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONSTRAINT_KEY = "constraint";

	@Override
	@Getter(value = EDITION_PATTERN_KEY, inverse = FlexoConcept.EDITION_PATTERN_CONSTRAINTS_KEY)
	public FlexoConcept getFlexoConcept();

	@Setter(EDITION_PATTERN_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = CONSTRAINT_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConstraint();

	@Setter(CONSTRAINT_KEY)
	public void setConstraint(DataBinding<Boolean> constraint);

	public static abstract class EditionPatternConstraintImpl extends EditionPatternObjectImpl implements EditionPatternConstraint {

		protected static final Logger logger = FlexoLogger.getLogger(EditionPatternConstraint.class.getPackage().getName());

		private FlexoConcept flexoConcept;
		private DataBinding<Boolean> constraint;

		public EditionPatternConstraintImpl() {
			super();
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getBindingModel();
			} else {
				return null;
			}
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			this.flexoConcept = flexoConcept;
		}

		@Override
		public String getURI() {
			return getFlexoConcept().getURI() + "/Constraints_" + Integer.toHexString(hashCode());
		}

		@Override
		public DataBinding<Boolean> getConstraint() {
			if (constraint == null) {
				constraint = new DataBinding<Boolean>(this, Boolean.class, BindingDefinitionType.GET);
				constraint.setBindingName("constraint");
			}
			return constraint;
		}

		@Override
		public void setConstraint(DataBinding<Boolean> constraint) {
			if (constraint != null) {
				constraint.setOwner(this);
				constraint.setBindingName("constraint");
				constraint.setDeclaredType(Boolean.class);
				constraint.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.constraint = constraint;
		}

	}
}
