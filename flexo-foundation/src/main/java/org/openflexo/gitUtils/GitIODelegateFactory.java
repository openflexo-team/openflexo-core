package org.openflexo.gitUtils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jgit.lib.ObjectId;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoIOGitDelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.GitResourceCenter;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FlexoVersion;

public class GitIODelegateFactory implements IODelegateFactory<File> {
	
	@Override
	public FlexoIODelegate<File> makeIODelegateNewInstance(FlexoResource<?> resource,SerializationArtefactKind artefactType) {
		ModelFactory factory;
		FlexoIOGitDelegate gitIODelegate = null;
		try {
			factory = new ModelFactory(ModelContextLibrary
					.getCompoundModelContext(FlexoIOGitDelegate.class));
			gitIODelegate = factory.newInstance(FlexoIOGitDelegate.class);
			gitIODelegate.setGitCommitIds(new HashMap<FlexoVersion,ObjectId>());
			gitIODelegate.setSerializationArtefactKind(artefactType);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return gitIODelegate;
	}
	
}
