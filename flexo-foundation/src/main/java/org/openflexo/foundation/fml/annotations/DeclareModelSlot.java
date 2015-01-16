package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.foundation.technologyadapter.ModelSlot;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = ElementType.TYPE)
public @interface DeclareModelSlot {

	public String FML();

	public Class<? extends ModelSlot<?>> modelSlotClass();

}
