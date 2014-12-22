package org.openflexo.foundation.fml.rm;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(VirtualModelResourceImpl.class)
@XMLElement
public interface VirtualModelResource extends PamelaResource<VirtualModel, VirtualModelModelFactory>,
		TechnologyAdapterResource<VirtualModel, FMLTechnologyAdapter>, DirectoryContainerResource<VirtualModel> {

	public static final String VIEW_POINT_LIBRARY = "viewPointLibrary";
	//public static final String DIRECTORY = "directory";

	/**
	 * Return virtual model stored by this resource<br>
	 * Load the resource data when unloaded
	 */
	public VirtualModel getVirtualModel();

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Do not force the resource data to be loaded
	 */
	public VirtualModel getLoadedVirtualModel();

	/*@Getter(value = VIEW_POINT_LIBRARY, ignoreType = true)
	public ViewPointLibrary getViewPointLibrary();

	@Setter(VIEW_POINT_LIBRARY)
	public void setViewPointLibrary(ViewPointLibrary viewPointLibrary);*/

	/*@Getter(DIRECTORY)
	@XmlAttribute
	public File getDirectory();

	@Setter(DIRECTORY)
	public void setDirectory(File file);*/

	@Override
	public ViewPointResource getContainer();

}
