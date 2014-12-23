package org.openflexo.view.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareVirtualModelInstanceNature {

	public Class<? extends VirtualModelInstanceNature> nature();

}
