package org.openflexo.foundation.fml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicates that annotated method is used as a serialized property in FML language syntax (this entry is used in
 * key-value pairs)
 * 
 * @author sylvain
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FMLProperty {

	public static final String UNDEFINED = "";

	/**
	 * The identifier of FML property
	 * 
	 * @return
	 */
	public String value();

	/**
	 * Indicates if whether this property is required
	 * 
	 * The default value is <code>false</code>
	 * 
	 * @return true if this property is required
	 */
	public boolean required() default false;

	/**
	 * A string convertable value that is set by default on the property
	 * 
	 * @return the string converted default value.
	 */
	public String defaultValue() default UNDEFINED;

}
