package org.openflexo.foundation;

import java.util.logging.Logger;

import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.ModelContext;
import org.openflexo.model.exceptions.ModelDefinitionException;
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

	@Override
	public synchronized void startDeserializing(PamelaResource<?, ?> resource) throws ConcurrentDeserializationException {
		if (resourceBeeingDeserialized == null) {
			resourceBeeingDeserialized = resource;
		} else {
			throw new ConcurrentDeserializationException(resource);
		}
	}

	@Override
	public synchronized void stopDeserializing(PamelaResource<?, ?> resource) {
		if (resourceBeeingDeserialized == resource) {
			resourceBeeingDeserialized = null;
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
