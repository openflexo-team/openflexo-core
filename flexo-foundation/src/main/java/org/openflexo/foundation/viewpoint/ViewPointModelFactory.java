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
package org.openflexo.foundation.viewpoint;

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle ViewPoint models<br>
 * One instance is declared for a {@link ViewPointResource}
 * 
 * @author sylvain
 * 
 */
public class ViewPointModelFactory extends DefaultPamelaResourceModelFactory<ViewPointResource> {

	public ViewPointModelFactory(ViewPointResource viewPointResource, EditingContext editingContext) throws ModelDefinitionException {
		super(viewPointResource, ModelContextLibrary.getModelContext(ViewPoint.class));
		setEditingContext(editingContext);
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		if(viewPointResource!=null && viewPointResource.getFlexoIODelegate() instanceof FileFlexoIODelegate){
			addConverter(new RelativePathResourceConverter(viewPointResource.getDirectory().getRelativePath()));
		}
	}

	/**
	 * Deserialized object are always set with basic controls
	 */
	@Override
	public <I> void objectHasBeenDeserialized(final I newlyCreatedObject, final Class<I> implementedInterface) {
		super.objectHasBeenDeserialized(newlyCreatedObject, implementedInterface);
		if (newlyCreatedObject instanceof ViewPoint && ((ViewPoint) newlyCreatedObject).getLocalizedDictionary() == null) {
			// Always set a ViewPointLocalizedDictionary for a ViewPoint
			ViewPointLocalizedDictionary localizedDictionary = newInstance(ViewPointLocalizedDictionary.class);
			((ViewPoint) newlyCreatedObject).setLocalizedDictionary(localizedDictionary);
		}
	}

	@Override
	public <I> void objectHasBeenCreated(final I newlyCreatedObject, final Class<I> implementedInterface) {
		super.objectHasBeenCreated(newlyCreatedObject, implementedInterface);
		if (newlyCreatedObject instanceof ViewPoint && ((ViewPoint) newlyCreatedObject).getLocalizedDictionary() == null) {
			// Always set a ViewPointLocalizedDictionary for a ViewPoint
			ViewPointLocalizedDictionary localizedDictionary = newInstance(ViewPointLocalizedDictionary.class);
			((ViewPoint) newlyCreatedObject).setLocalizedDictionary(localizedDictionary);
		}
	}
}
