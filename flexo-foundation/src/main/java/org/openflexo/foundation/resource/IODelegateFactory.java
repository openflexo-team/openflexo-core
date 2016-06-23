package org.openflexo.foundation.resource;

public interface IODelegateFactory<I> {
	public FlexoIODelegate<I> makeNewInstance(FlexoResource<?> resource);
}
