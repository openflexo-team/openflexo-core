/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

import java.lang.reflect.Type;

import org.openflexo.connie.java.JavaTypingSpace;
import org.openflexo.foundation.fml.rt.action.MatchingSet;

/**
 * FML typing space, build on top of {@link JavaTypingSpace}
 * 
 * Support import of VirtualModels
 * 
 * @author sylvain
 *
 */
public abstract class AbstractFMLTypingSpace extends JavaTypingSpace {

	public AbstractFMLTypingSpace() {
	}

	/**
	 * Return boolean indicating if supplied {@link Type} is actually in current typing space
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public boolean isTypeImported(Type type) {
		return super.isTypeImported(type);
	}

	/**
	 * Import supplied type in this typing space
	 * 
	 * @param type
	 */
	@Override
	public void importType(Type type) {
		super.importType(type);
	}

	/**
	 * Resolve {@link Type} according to current typing space using supplied type {@link String} representation
	 * 
	 * @param typeAsString
	 * @return
	 */
	@Override
	public Type resolveType(String typeAsString) {
		if (typeAsString.equals("ConceptInstance")) {
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}
		if (typeAsString.equals("MatchingSet")) {
			return MatchingSet.class;
		}
		return super.resolveType(typeAsString);
	}

}
