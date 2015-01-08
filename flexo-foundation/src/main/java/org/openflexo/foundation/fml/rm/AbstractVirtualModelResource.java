package org.openflexo.foundation.fml.rm;

import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.DirectoryContainerResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(ViewPointResourceImpl.class)
@XMLElement
public interface AbstractVirtualModelResource<VM extends AbstractVirtualModel<VM>> extends PamelaResource<VM, FMLModelFactory>,
		DirectoryContainerResource<VM>, TechnologyAdapterResource<VM, FMLTechnologyAdapter> {

	public VM getVirtualModel();

}
