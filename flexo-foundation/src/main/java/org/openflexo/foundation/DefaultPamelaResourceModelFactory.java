package org.openflexo.foundation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * Default implementation for {@link PamelaResourceModelFactory} A {@link DefaultPamelaResourceModelFactory} provides automatic FlexoId
 * management
 * 
 * @author sylvain
 * 
 */
public class DefaultPamelaResourceModelFactory<R extends PamelaResource<?, ?>> extends ModelFactory implements
		PamelaResourceModelFactory<R> {

	protected static final Logger logger = Logger.getLogger(DefaultPamelaResourceModelFactory.class.getPackage().getName());

	private final R resource;
	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	public DefaultPamelaResourceModelFactory(R resource, Class<?> baseClass) throws ModelDefinitionException {
		super(baseClass);
		this.resource = resource;
	}

	public DefaultPamelaResourceModelFactory(R resource, ModelContext modelContext) {
		super(modelContext);
		this.resource = resource;
	}

	public DefaultPamelaResourceModelFactory(R resource, Collection<Class<?>> classes) throws ModelDefinitionException {
		super(ModelContextLibrary.getCompoundModelContext(appendGRClasses(classes)));
		this.resource = resource;
	}

	private static Class<?>[] appendGRClasses(final Collection<Class<?>> classes) {
		final Set<Class<?>> returned = new HashSet<Class<?>>(classes);
		return returned.toArray(new Class<?>[returned.size()]);
	}

	@Override
	public R getResource() {
		return resource;
	}

	@Override
	public synchronized void startDeserializing() {
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
			} else {
				logger.warning("Could not access resource beeing deserialized");
			}
		}
	}

}
