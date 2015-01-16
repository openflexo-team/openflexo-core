package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * Annotation used to provide to a {@link ModelSlot} the list of all {@link FlexoBahaviour} to consider
 * 
 * @author sylvain
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareFlexoBehaviours {

	public Class<? extends FlexoBehaviour>[] value();

}
