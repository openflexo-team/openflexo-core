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

package org.openflexo.foundation.resource;

import javax.xml.bind.annotation.XmlAttribute;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FlexoVersion;

/**
 * A {@link PamelaResource} is a resource where underlying model is managed by PAMELA framework
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity(isAbstract = true)
public interface PamelaResource<RD extends ResourceData<RD>, F extends PamelaModelFactory & PamelaResourceModelFactory>
		extends FlexoResource<RD> {

	String MODEL_VERSION = "modelVersion";
	String FACTORY = "factory";

	@Getter(value = FACTORY, ignoreType = true)
	F getFactory();

	@Setter(FACTORY)
	void setFactory(F factory);

	@Getter(value = MODEL_VERSION, isStringConvertable = true)
	@XmlAttribute
	FlexoVersion getModelVersion();

	@Setter(MODEL_VERSION)
	void setModelVersion(FlexoVersion file);

	long getNewFlexoID();

	/**
	 * Returns the lastUnique ID used in this resource
	 * 
	 * @return
	 */
	long getLastID();

	void setLastID(long lastUniqueID);

	/**
	 * Return the model version currently reflected by executed code (the software version)
	 * 
	 * @return
	 */
	FlexoVersion latestVersion();

	/**
	 * Internally used to notify factory that a deserialization process has started<br>
	 * This hook allows to handle FlexoID and ignore of edits raised during deserialization process
	 */
	void startDeserializing();

	/**
	 * Internally used to notify factory that a deserialization process has finished<br>
	 */
	void stopDeserializing();

	boolean isIndexing();

	/**
	 *
	 * @param object
	 */
	void register(FlexoObject object);

	/**
	 * Retrieve object with supplied flexoId and userIdentifier
	 * 
	 * @param flexoId
	 * @param userIdentifier
	 * @return
	 */
	FlexoObject getFlexoObject(Long flexoId, String userIdentifier);
}
