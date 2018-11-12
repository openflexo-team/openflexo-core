/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;

@ModelEntity
@XMLElement
public interface FlexoServerInstance {

	public static final String URL = "url";
	public static final String WS_URL = "ws_url";
	public static final String REST_URL = "restURL";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String USER_TYPES = "userTypes";
	public static final String OTHER_ID = "other";

	@Getter(ID)
	@XMLAttribute
	public String getID();

	@Setter(ID)
	public void setID(String id);

	@Getter(URL)
	@XMLAttribute
	public String getURL();

	@Setter(URL)
	public void setURL(String url);

	@Getter(WS_URL)
	@XMLAttribute
	public String getWSURL();

	@Setter(WS_URL)
	public void setWSURL(String url);

	@Getter(REST_URL)
	@XMLAttribute
	public String getRestURL();

	@Setter(REST_URL)
	public void setRestURL(String url);

	@Getter(NAME)
	@XMLAttribute
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	@Getter(value = USER_TYPES, cardinality = Cardinality.LIST)
	@XMLElement(xmlTag = "usertype")
	public List<String> getUserTypes();

	@Setter(USER_TYPES)
	public void setUserTypes(List<String> userTypes);

	@Adder(USER_TYPES)
	public void addToUserTypes(String userType);

	@Remover(USER_TYPES)
	public void removeFromUserTypes(String userType);
}
