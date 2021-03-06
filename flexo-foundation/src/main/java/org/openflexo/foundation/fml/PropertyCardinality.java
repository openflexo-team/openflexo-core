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

/**
 * Represents the cardinality of a property
 * 
 * <ul>
 * <li><tt>ZeroOne</tt>: means that the value is unique or null</li>
 * <li><tt>One</tt>: means that the value is unique and non-null</li>
 * <li><tt>ZeroMany</tt>: means that the value is represented by a list which is eventually empty</li>
 * <li><tt>OneMany</tt>: means that the value is represented by a non-empty list</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public enum PropertyCardinality {
	ZeroOne {
		@Override
		public boolean isMultipleCardinality() {
			return false;
		}

		@Override
		public String stringRepresentation() {
			return "0-1";
		}
	},
	One {
		@Override
		public boolean isMultipleCardinality() {
			return false;
		}

		@Override
		public String stringRepresentation() {
			return "1";
		}
	},
	ZeroMany {
		@Override
		public boolean isMultipleCardinality() {
			return true;
		}

		@Override
		public String stringRepresentation() {
			return "0..*";
		}
	},
	OneMany {
		@Override
		public boolean isMultipleCardinality() {
			return true;
		}

		@Override
		public String stringRepresentation() {
			return "1..*";
		}
	};
	public abstract boolean isMultipleCardinality();

	public abstract String stringRepresentation();
}