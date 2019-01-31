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
 * A delegate providing pretty-print to an FMLObject.<br>
 * 
 * @author sylvain
 * 
 */
public interface FMLPrettyPrintDelegate<T extends FMLObject> {

	/**
	 * Returned object beeing pretty-printed by this delegate
	 * 
	 * @return
	 */
	public T getFMLObject();

	/**
	 * Return normalized FML representation for that object
	 * 
	 * @return
	 */
	public String getNormalizedFMLRepresentation(PrettyPrintContext context);

	/**
	 * Return FML representation for that object<br>
	 * 
	 * This representation might be different as the normalized one, as underlying FMLObject could be obtained from a parsed FML file. Some
	 * pretty-print implementations may want to preserve original formatting and syntax (including comments)
	 * 
	 * @return
	 */
	public String getFMLRepresentation(PrettyPrintContext context);

	/**
	 * Build and return a new pretty-print context
	 * 
	 * @return
	 */
	public PrettyPrintContext makePrettyPrintContext();

	public interface PrettyPrintContext {

	}
}
