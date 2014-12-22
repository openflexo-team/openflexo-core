package org.openflexo.foundation.technologyadapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.fml.FlexoBehaviour;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareFlexoBehaviour {
	
	public String FML();

	public Class<? extends FlexoBehaviour> flexoBehaviourClass();
	
}
