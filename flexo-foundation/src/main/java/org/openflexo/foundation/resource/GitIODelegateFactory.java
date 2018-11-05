package org.openflexo.foundation.resource;

import java.io.File;

import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

public class GitIODelegateFactory implements IODelegateFactory<File> {

	@Override
	public FlexoIODelegate<File> makeNewInstance(FlexoResource<?> resource) {
		ModelFactory factory;
		GitIODelegate gitIODelegate = null;
		try {
			factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(GitIODelegate.class));
			gitIODelegate = factory.newInstance(GitIODelegate.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return gitIODelegate;
	}

}
