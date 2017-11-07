/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.project;

import java.util.logging.Logger;

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;

/**
 * {@link FlexoProject} PAMELA factory<br>
 * One instance of this class should be used for each {@link FlexoProjectResource}
 * 
 * @author sylvain
 * 
 */
public class FlexoProjectFactory extends DefaultPamelaResourceModelFactory<FlexoProjectResource> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoProjectFactory.class.getPackage().getName());

	private RelativePathResourceConverter relativePathResourceConverter;

	public FlexoProjectFactory(FlexoProjectResource resource, EditingContext editingContext) throws ModelDefinitionException {
		super(resource, ModelContextLibrary.getModelContext(FlexoProject.class));
		setEditingContext(editingContext);
		addConverter(new FlexoVersionConverter());
		addConverter(relativePathResourceConverter = new RelativePathResourceConverter(null));
		if (resource != null && resource.getIODelegate() != null && resource.getIODelegate().getSerializationArtefactAsResource() != null) {
			relativePathResourceConverter
					.setContainerResource(resource.getIODelegate().getSerializationArtefactAsResource().getContainer());
		}
	}

	public FlexoProject<?> makeNewFlexoProject() {
		return newInstance(FlexoProject.class);
	}

}
