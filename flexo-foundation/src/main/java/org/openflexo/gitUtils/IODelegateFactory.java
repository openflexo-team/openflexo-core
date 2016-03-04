package org.openflexo.gitUtils;

import java.io.File;

import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.model.factory.ModelFactory;

public interface IODelegateFactory<I> {
	public FlexoIODelegate<I> makeIODelegateNewInstance(FlexoResource<?> resource,SerializationArtefactKind artefactType);
}