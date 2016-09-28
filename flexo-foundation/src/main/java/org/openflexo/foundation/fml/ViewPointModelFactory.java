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

package org.openflexo.foundation.fml;

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.RelativePathResourceConverter;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
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
		if (viewPointResource != null) {
			addConverter(new RelativePathResourceConverter(viewPointResource.getFlexoIODelegate()));
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
