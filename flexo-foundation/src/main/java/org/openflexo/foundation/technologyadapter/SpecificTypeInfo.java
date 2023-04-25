/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import java.util.HashMap;
import java.util.Map;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.TechnologySpecificType;

/**
 * A class which contain informations to instantiate a technology-specific type
 * 
 * @author sylvain
 *
 */
public class SpecificTypeInfo<TA extends TechnologyAdapter<TA>> {

	private final Class<? extends TechnologySpecificType<TA>> technologySpecificTypeClass;
	private Map<String, Object> parameters;

	private String serializationForm = null;

	public SpecificTypeInfo(Class<? extends TechnologySpecificType<TA>> technologySpecificTypeClass) {
		this.technologySpecificTypeClass = technologySpecificTypeClass;
		parameters = new HashMap<>();
	}

	public Class<? extends TechnologySpecificType<TA>> getTechnologySpecificTypeClass() {
		return technologySpecificTypeClass;
	}

	public Class<? extends TechnologyAdapter<TA>> getTechnologyAdapterClass() {
		return (Class) TypeUtils.getTypeArgument(getTechnologySpecificTypeClass(), TechnologySpecificType.class, 0);
	}

	public Object getParameter(String name) {
		return parameters.get(name);
	}

	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}

	public String getSerializationForm() {
		return serializationForm;
	}

	public void setSerializationForm(String serializationForm) {
		this.serializationForm = serializationForm;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(technologySpecificTypeClass.toGenericString());
		sb.append("[" + getTechnologyAdapterClass() + "]");
		sb.append("(");
		for (String key : parameters.keySet()) {
			sb.append(key + "=" + getParameter(key));
		}
		sb.append(")");
		return sb.toString();
	}

}
