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

package org.openflexo.foundation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Default implementation for {@link PamelaResourceModelFactory} A {@link DefaultPamelaResourceModelFactory} provides automatic FlexoId
 * management
 * 
 * @author sylvain
 * 
 */
public class DefaultPamelaResourceModelFactory<R extends PamelaResource<?, ?>> extends PamelaModelFactory
		implements PamelaResourceModelFactory<R> {

	protected static final Logger logger = Logger.getLogger(DefaultPamelaResourceModelFactory.class.getPackage().getName());

	private final R resource;
	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	public DefaultPamelaResourceModelFactory(R resource, Class<?> baseClass) throws ModelDefinitionException {
		super(baseClass);
		this.resource = resource;
	}

	public DefaultPamelaResourceModelFactory(R resource, PamelaMetaModel pamelaMetaModel) {
		super(pamelaMetaModel);
		this.resource = resource;
	}

	public DefaultPamelaResourceModelFactory(R resource, Collection<Class<?>> classes) throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(appendGRClasses(classes)));
		this.resource = resource;
	}

	private static Class<?>[] appendGRClasses(final Collection<Class<?>> classes) {
		final Set<Class<?>> returned = new HashSet<>(classes);
		return returned.toArray(new Class<?>[returned.size()]);
	}

	@Override
	public R getResource() {
		return resource;
	}

	@Override
	public synchronized void startDeserializing() {
		if (resource == null) {
			logger.warning("startDeserializing() called for null resource");
		}
		if (resource.getServiceManager() == null) {
			logger.warning("startDeserializing() called for resource with null ServiceManager");
		}

		EditingContext editingContext = resource.getServiceManager().getEditingContext();

		if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
			undoManager = (FlexoUndoManager) editingContext.getUndoManager();
			undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(resource));
			// System.out.println("@@@@@@@@@@@@@@@@ START LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public synchronized void stopDeserializing() {
		if (ignoreHandler != null) {
			undoManager.removeFromIgnoreHandlers(ignoreHandler);
			// System.out.println("@@@@@@@@@@@@@@@@ END LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		super.objectHasBeenDeserialized(newlyCreatedObject, implementedInterface);
		if (newlyCreatedObject instanceof FlexoObject) {
			if (getResource() != null) {
				getResource().setLastID(((FlexoObject) newlyCreatedObject).getFlexoID());
			}
			else {
				logger.warning("Could not access resource beeing deserialized");
			}
		}
	}

}
