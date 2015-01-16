package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Annotation used to provide to a {@link TechnologyAdapter} the list of all {@link ModelSlot} to consider
 * 
 * @author sylvain
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareModelSlots {

	public Class<? extends ModelSlot<?>>[] value();

}
