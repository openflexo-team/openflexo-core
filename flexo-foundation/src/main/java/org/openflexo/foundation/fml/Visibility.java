/**
 * 
 * Copyright (c) 2014-2019, Openflexo
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

/**
 * Visibility of FML elements ({@link VirtualModel}, {@link FlexoConcept}, {@link FlexoProperty}, {@link FlexoBehaviour}), declared with the
 * {@code public}, {@code protected} and {@code private} keywords; an element declared without keyword has the {@link #Default}
 * visibility.
 * <p>
 * The tooling relies on the {@link #Public} visibility: for instance, only public behaviours are proposed in the contextual menu of an
 * instance, and only public properties are selected by default in a generated inspector.
 *
 * @author sylvain
 *
 */
public enum Visibility {
	/**
	 * Visibility of an element declared without visibility keyword
	 */
	Default {
		@Override
		public String getFMLRepresentation() {
			return "";
		}
	},
	/**
	 * Public visibility ({@code public}): the element is exposed by the tooling, e.g. behaviours are available through right-clicking on
	 * instances
	 */
	Public {
		@Override
		public String getFMLRepresentation() {
			return "public ";
		}
	},
	/**
	 * Protected visibility ({@code protected})
	 */
	Protected {
		@Override
		public String getFMLRepresentation() {
			return "protected ";
		}
	},
	/**
	 * Private visibility ({@code private})
	 */
	Private {
		@Override
		public String getFMLRepresentation() {
			return "private ";
		}
	};

	public abstract String getFMLRepresentation();
}
