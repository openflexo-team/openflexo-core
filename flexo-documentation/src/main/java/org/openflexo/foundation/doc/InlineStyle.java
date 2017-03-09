/**
 * 
 */
package org.openflexo.foundation.doc;

/**
 * @author Bruno Quercia
 *
 */
public class InlineStyle extends Style {

	/**
	 * 
	 */
	public InlineStyle() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param properties
	 */
	public InlineStyle(PropertySet properties) {
		super(properties);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param properties
	 * @param parentStyle
	 */
	public InlineStyle(PropertySet properties, Style parentStyle) {
		super(properties, parentStyle);
		// TODO Auto-generated constructor stub
	}
	
	public InlineStyle(InlineStyle s){
		super(s);
	}

}
