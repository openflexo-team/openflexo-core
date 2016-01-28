package org.openflexo.gitUtils;

import java.io.File;

import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

public class FileIODelegateFactory implements IODelegateFactory<File> {

	@Override
	public FlexoIODelegate<File> makeNewInstance(FlexoResource<?> resource) {
		ModelFactory factory;
		FileFlexoIODelegate fileIODelegate = null;
		try {
			factory = new ModelFactory(ModelContextLibrary
					.getCompoundModelContext(FileFlexoIODelegate.class));
			fileIODelegate = factory.newInstance(FileFlexoIODelegate.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return fileIODelegate;
	}
	

}
