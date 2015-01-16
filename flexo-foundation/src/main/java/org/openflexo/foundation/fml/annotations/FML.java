package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that annotated type is managed as part of FML language.<br>
 * value() used as element identifier in FML syntax
 * 
 * @author sylvain
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface FML {

	/**
	 * The identifier in FML syntax
	 * 
	 * @return the property identifier of this getter
	 */
	public String value();

}
