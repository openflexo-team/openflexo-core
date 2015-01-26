/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

public class URIUtilities {

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean testValidURI(FlexoModel<?, ?> model, String conceptURI) {
		if (StringUtils.isEmpty(conceptURI)) {
			return false;
		}
		if (StringUtils.isEmpty(conceptURI.trim())) {
			return false;
		}
		return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(model, conceptURI);
	}

	/**
	 * Return true if URI is duplicated in the context of supplied {@link FlexoModel}
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isDuplicatedURI(FlexoModel<?, ?> model, String conceptURI) {
		if (model != null) {
			return model.getObject(model.getURI() + "#" + conceptURI) != null;
		}
		return false;
	}

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean testValidURI(FlexoMetaModel<?> metaModel, String conceptURI) {
		if (StringUtils.isEmpty(conceptURI)) {
			return false;
		}
		if (StringUtils.isEmpty(conceptURI.trim())) {
			return false;
		}
		return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(metaModel, conceptURI);
	}

	/**
	 * Return true if URI is duplicated in the context of supplied {@link FlexoModel}
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isDuplicatedURI(FlexoMetaModel<?> metaModel, String conceptURI) {
		if (metaModel != null) {
			return metaModel.getObject(metaModel.getURI() + "#" + conceptURI) != null;
		}
		return false;
	}

}
