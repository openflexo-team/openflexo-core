package org.openflexo.foundation;

import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.factory.ModelFactory;

/**
 * 
 * TODO : JAVADOC is not relevant A {@link ModelFactory} providing automatic.
 * FlexoId management
 * 
 * @author sylvain
 * 
 */
public interface FlexoModelFactory {

    void startDeserializing(PamelaResource<?, ?> resource) throws ConcurrentDeserializationException;

    void stopDeserializing(PamelaResource<?, ?> resource);

    public static class ConcurrentDeserializationException extends Exception {
        public ConcurrentDeserializationException(PamelaResource<?, ?> resource) {
            super("ConcurrentDeserializationException raised for " + resource);
        }
    }
}
