package org.openflexo.foundation.fmlrt.rm;

import java.util.List;

import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModelTechnologyAdapter;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fmlrt.View;
import org.openflexo.foundation.fmlrt.ViewLibrary;
import org.openflexo.foundation.fmlrt.ViewModelFactory;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * This is the {@link FlexoResource} encoding a {@link View}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ViewResourceImpl.class)
@XMLElement
public interface ViewResource extends PamelaResource<View, ViewModelFactory>, FlexoProjectResource<View>, DirectoryContainerResource<View>,
		TechnologyAdapterResource<View, VirtualModelTechnologyAdapter>, FlexoModelResource<View, ViewPoint, VirtualModelTechnologyAdapter> {

	public static final String VIEW_SUFFIX = ".view";

	public static final String VIEW_LIBRARY = "viewLibrary";
	public static final String DIRECTORY = "directory";
	public static final String VIEWPOINT_RESOURCE = "viewPointResource";

	public ViewPoint getViewPoint();

	@Getter(value = VIEW_LIBRARY, ignoreType = true)
	public ViewLibrary getViewLibrary();

	@Setter(VIEW_LIBRARY)
	public void setViewLibrary(ViewLibrary viewLibrary);

	@Getter(value = VIEWPOINT_RESOURCE, ignoreType = true)
	public ViewPointResource getViewPointResource();

	@Setter(VIEWPOINT_RESOURCE)
	public void setViewPointResource(ViewPointResource viewPointResource);

	public View getView();

	public List<VirtualModelInstanceResource> getVirtualModelInstanceResources();

}
