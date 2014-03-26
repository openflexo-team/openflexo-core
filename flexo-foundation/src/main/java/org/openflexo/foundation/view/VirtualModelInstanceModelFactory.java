/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.view;

import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathFileConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle VirtualModelInstance models<br>
 * Only one instance of this class should be used in a session
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInstanceModelFactory extends ModelFactory {

	/*public VirtualModelInstanceModelFactory() throws ModelDefinitionException {
		super(ModelContextLibrary.getModelContext(VirtualModelInstance.class));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
	}*/

	public VirtualModelInstanceModelFactory(VirtualModelInstanceResource virtualModelInstanceResource) throws ModelDefinitionException {
		super(ModelContextLibrary.getModelContext(VirtualModelInstance.class));
		addConverter(new RelativePathFileConverter(virtualModelInstanceResource.getFile()));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		if (virtualModelInstanceResource != null) {
			addConverter(virtualModelInstanceResource.getProject().getObjectReferenceConverter());
		}
	}

}
