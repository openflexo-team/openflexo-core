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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Default implementation for {@link PamelaResource} (a resource where underlying model is managed by PAMELA framework)
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author Sylvain
 * 
 */
public abstract class PamelaPropertiesSerializableResourceImpl<RD extends ResourceData<RD> & AccessibleProxyObject, F extends PamelaModelFactory & PamelaResourceModelFactory>
		extends PamelaResourceImpl<RD, F> implements PamelaPropertiesSerializableResource<RD, F> {

	private static final Logger logger = Logger.getLogger(PamelaPropertiesSerializableResourceImpl.class.getPackage().getName());

	@Override
	protected RD performLoad() throws IOException, Exception {
		// Retrieve the data from an input stream given by the FlexoIOStream
		// delegate of the resource
		InputStream inputStream = getFlexoIOStreamDelegate().getInputStream();

		RD resourceData = getFactory().newInstance(getResourceDataClass());
		ModelEntity<RD> modelEntity = getFactory().getModelEntityForInstance(resourceData);

		Properties properties = new Properties();
		properties.load(inputStream);

		properties.forEach((k, v) -> {
			handleProperty((String) k, (String) v, resourceData, modelEntity);
		});
		return resourceData;
	}

	protected void handleProperty(String key, String value, RD resourceData, ModelEntity<RD> modelEntity) {
		try {
			ModelProperty<? super RD> property = modelEntity.getModelProperty(key);
			if (property != null) {
				getFactory().getHandler(resourceData).invokeSetter(key, value);
			}
			else {
				unhandledProperty(key, value, resourceData, modelEntity);
			}

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}

	}

	protected void unhandledProperty(String key, String value, RD resourceData, ModelEntity<RD> modelEntity) {
		// By default, does nothing
		System.out.println("Je ne sais pas quoi faire avec " + key + "=" + value);
	}

	@Override
	protected void performSave(boolean clearIsModified) throws SaveResourceException {
		logger.warning("Not implemented : on doit sauver la resource " + this);
	}

}
