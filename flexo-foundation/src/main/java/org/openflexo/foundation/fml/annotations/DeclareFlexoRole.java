package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.fml.FlexoRole;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareFlexoRole {

	public String FML();

	public Class<? extends FlexoRole> flexoRoleClass();

}
