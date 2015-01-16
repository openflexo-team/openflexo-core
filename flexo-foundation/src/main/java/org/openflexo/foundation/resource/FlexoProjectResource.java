package org.openflexo.foundation.resource;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;

/**
 * A {@link FlexoProjectResource} is a resource which is managed inside a {@link FlexoProject}
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
public interface FlexoProjectResource<RD extends ResourceData<RD>> extends FlexoResource<RD>, FlexoProjectObject {

	@Override
	public FlexoProject getProject();

	@Override
	public void setProject(FlexoProject project);

	@Implementation
	public abstract class FlexoProjectResourceImpl<RD extends ResourceData<RD>> extends FlexoProjectObjectImpl implements
			FlexoProjectResource<RD> {

	}
}
