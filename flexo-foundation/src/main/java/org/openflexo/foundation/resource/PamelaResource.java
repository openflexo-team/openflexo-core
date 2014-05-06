package org.openflexo.foundation.resource;

import javax.xml.bind.annotation.XmlAttribute;

import org.openflexo.foundation.FlexoModelFactory;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FlexoVersion;

/**
 * A {@link PamelaResource} is a resource where underlying model is managed by PAMELA framework
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity
@XMLElement
public interface PamelaResource<RD extends ResourceData<RD>, F extends ModelFactory & FlexoModelFactory> extends FlexoFileResource<RD> {

	public static final String MODEL_VERSION = "modelVersion";
	public static final String FACTORY = "factory";

	@Getter(value = FACTORY, ignoreType = true)
	public F getFactory();

	@Setter(FACTORY)
	public void setFactory(F factory);

	@Getter(value = MODEL_VERSION, isStringConvertable = true)
	@XmlAttribute
	public FlexoVersion getModelVersion();

	@Setter(MODEL_VERSION)
	public void setModelVersion(FlexoVersion file);

	public long getNewFlexoID();

	/**
	 * Returns the lastUnique ID used in this resource
	 * 
	 * @return
	 */
	public long getLastID();

	public void setLastID(long lastUniqueID);

	/**
	 * Return the model version currently reflected by executed code (the software version)
	 * 
	 * @return
	 */
	public FlexoVersion latestVersion();

	/**
	 * Internally used to notify factory that a deserialization process has started<br>
	 * This hook allows to handle FlexoID and ignore of edits raised during deserialization process
	 */
	public void startDeserializing();

	/**
	 * Internally used to notify factory that a deserialization process has finished<br>
	 */
	public void stopDeserializing();

}