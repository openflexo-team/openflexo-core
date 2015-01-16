package org.openflexo.foundation.resource;

import javax.xml.bind.annotation.XmlAttribute;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.rm.Resource;

/**
 * A {@link DirectoryContainerResource} is a resource bound to a directory on file system<br>
 * This directory contains a main file (the file which is accessed through {@link FlexoFileResource} api) and some other resources (files)
 * contained in this directory.<br>
 * This resource also manage a RelativeFilePathConverter that allow to access resources inside this directory. Such resource is easyly
 * transportable (any resources contained in this directory are always retrievable whereever the parent directory is).
 * 
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity(isAbstract = true)
public interface DirectoryContainerResource<RD extends ResourceData<RD>> extends FlexoResource<RD> {

	public static final String DIRECTORY = "directory";
	public static final String RELATIVE_RESOURCE_PATH_CONVERTER = "relativePathResourceConverter";

	/**
	 * Return the directory (parent file)
	 * 
	 * @return
	 */
	@Getter(DIRECTORY)
	@XmlAttribute
	public Resource getDirectory();

	/**
	 * Sets the directory (parent file)
	 */
	@Setter(DIRECTORY)
	public void setDirectory(Resource resource);

	/**
	 * Return the relative file path converter associated with the directory location
	 * 
	 * @return
	 */
	@Getter(value = RELATIVE_RESOURCE_PATH_CONVERTER, ignoreType = true)
	public RelativePathResourceConverter getRelativePathResourceConverter();

	/**
	 * Sets the relative file path converter
	 */
	@Setter(RELATIVE_RESOURCE_PATH_CONVERTER)
	public void setRelativePathResourceConverter(RelativePathResourceConverter converter);

}