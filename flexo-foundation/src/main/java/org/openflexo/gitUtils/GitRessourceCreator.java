package org.openflexo.gitUtils;

import java.io.File;

import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

public class GitRessourceCreator<T extends FlexoResource<?>> {
	
	private Class<T> resourceClass; 
	
	
	public <I>  T makeGitResource(String modelURI, File powerpointFile,
			TechnologyContextManager technologyContextManager, FlexoResourceCenter<?> resourceCenter) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(FileFlexoIODelegate.class,resourceClass ));
			T returned = (T) factory
					.newInstance(resourceClass);
			((TechnologyAdapterResource) returned).setTechnologyAdapter(technologyContextManager.getTechnologyAdapter());
			returned.setTechnologyContextManager(technologyContextManager);
			((FlexoResource<?>) returned).initName(powerpointFile.getName());

			// returned.setFile(powerpointFile);
			FileFlexoIODelegate fileIODelegate = factory.newInstance(FileFlexoIODelegate.class);
			returned.setFlexoIODelegate(fileIODelegate);
			fileIODelegate.setFile(powerpointFile);

			returned.setURI(modelURI);
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(technologyContextManager.getTechnologyAdapter().getTechnologyAdapterService().getServiceManager());
			technologyContextManager.registerResource(returned);

			PowerpointSlideshow resourceData = new PowerpointSlideshow(technologyContextManager.getTechnologyAdapter());
			returned.setResourceData(resourceData);
			resourceData.setResource(returned);

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

}
