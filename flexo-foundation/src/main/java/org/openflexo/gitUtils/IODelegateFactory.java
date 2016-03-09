package org.openflexo.gitUtils;

import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;

public interface IODelegateFactory<I> {
	public FlexoIODelegate<I> makeIODelegateNewInstance(FlexoResource<?> resource,SerializationArtefactKind artefactType);
}