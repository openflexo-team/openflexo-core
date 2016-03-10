package org.openflexo.gitUtils;

import java.io.File;

import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoIOGitDelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

public class GitIODelegateFactory implements IODelegateFactory<File> {
	
	@Override
	public FlexoIODelegate<File> makeNewInstance(FlexoResource<?> resource) {
		ModelFactory factory;
		FlexoIOGitDelegate gitIODelegate = null;
		try {
			factory = new ModelFactory(ModelContextLibrary
					.getCompoundModelContext(FlexoIOGitDelegate.class));
			gitIODelegate = factory.newInstance(FlexoIOGitDelegate.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return gitIODelegate;
	}
	
}
