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

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathFileConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle View models<br>
 * Only one instance of this class should be used in a session
 * 
 * @author sylvain
 * 
 */
public class ViewModelFactory extends DefaultPamelaResourceModelFactory<ViewResource> {

	public ViewModelFactory(ViewResource viewResource, EditingContext editingContext) throws ModelDefinitionException {
		super(viewResource, ModelContextLibrary.getModelContext(View.class));
		setEditingContext(editingContext);
		addConverter(new RelativePathFileConverter(viewResource.getDirectory()));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
	}

}
