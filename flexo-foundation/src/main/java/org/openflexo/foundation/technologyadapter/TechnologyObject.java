/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation.technologyadapter;

import java.lang.reflect.Type;

import org.openflexo.foundation.FlexoObject;

/**
 * Implemented by all objects specific to a given technology
 *
 * @author sylvain
 *
 */
public interface TechnologyObject<TA extends TechnologyAdapter<TA>> extends FlexoObject {

	/**
	 * Return the {@link TechnologyAdapter} of technical space where this concept exists
	 *
	 * @return
	 */
	public TA getTechnologyAdapter();

	/**
	 * Return the technology-specific type reflecting this object in FML type system, or null when this object does not expose a dedicated
	 * type (in which case its raw Java type is used).<br>
	 *
	 * This is used to provide dynamic typing in FML-script: when a value is assigned to a variable, its type is infered from the actual
	 * value (see {@link org.openflexo.foundation.fml.FMLUtils#inferType(Object)}). Technology objects carrying a dedicated FML type (eg a
	 * typed XML individual) should override this method to return that type.
	 *
	 * @return
	 */
	default Type getInstanceType() {
		return null;
	}

}
