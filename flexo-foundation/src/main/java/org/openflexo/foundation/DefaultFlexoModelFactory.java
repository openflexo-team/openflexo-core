package org.openflexo.foundation;

import java.util.logging.Logger;

import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.model.ModelContext;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * A {@link ModelFactory} providing automatic FlexoId management
 * 
 * @author sylvain
 * 
 */
public class DefaultFlexoModelFactory extends ModelFactory implements FlexoModelFactory {

	protected static final Logger logger = Logger.getLogger(DefaultFlexoModelFactory.class.getPackage().getName());

	public DefaultFlexoModelFactory(Class<?> baseClass) throws ModelDefinitionException {
		super(baseClass);
	}

	public DefaultFlexoModelFactory(ModelContext modelContext) {
		super(modelContext);
	}

	private PamelaResource<?, ?> resourceBeeingDeserialized = null;
	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	@Override
	public synchronized void startDeserializing(PamelaResource<?, ?> resource) throws ConcurrentDeserializationException {
		if (resourceBeeingDeserialized == null) {
			resourceBeeingDeserialized = resource;
		} else {
			throw new ConcurrentDeserializationException(resource);
		}

		EditingContext editingContext = resource.getServiceManager().getEditingContext();

		if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
			undoManager = (FlexoUndoManager) editingContext.getUndoManager();
			undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(resource));
			// System.out.println("@@@@@@@@@@@@@@@@ START LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public synchronized void stopDeserializing(PamelaResource<?, ?> resource) {
		if (resourceBeeingDeserialized == resource) {
			resourceBeeingDeserialized = null;
		}

		if (ignoreHandler != null) {
			undoManager.removeFromIgnoreHandlers(ignoreHandler);
			// System.out.println("@@@@@@@@@@@@@@@@ END LOADING RESOURCE " + resource.getURI());
		}

	}

	@Override
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		super.objectHasBeenDeserialized(newlyCreatedObject, implementedInterface);
		if (newlyCreatedObject instanceof FlexoObject) {
			if (resourceBeeingDeserialized != null) {
				resourceBeeingDeserialized.setLastID(((FlexoObject) newlyCreatedObject).getFlexoID());
			} else {
				logger.warning("Could not access resource beeing deserialized");
			}
		}
	}

}
