package org.openflexo.foundation.resource;

import java.io.File;

import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

public class GitIODelegateFactory implements IODelegateFactory<File> {

	@Override
	public FlexoIODelegate<File> makeNewInstance(FlexoResource<?> resource) {
		PamelaModelFactory factory;
		GitIODelegate gitIODelegate = null;
		try {
			factory = new PamelaModelFactory(PamelaMetaModelLibrary.getCompoundModelContext(GitIODelegate.class));
			gitIODelegate = factory.newInstance(GitIODelegate.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return gitIODelegate;
	}

}
