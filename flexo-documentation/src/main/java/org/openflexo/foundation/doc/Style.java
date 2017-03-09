package org.openflexo.foundation.doc;

import java.util.Iterator;

/**
 * This abstract class is what gathers different properties applicable to an element.
 * @author Bruno Quercia
 *
 */
public abstract class Style {
	PropertySet properties;
	Style parentStyle;
	
	/**
	 * Creates an empty style with no properties.
	 */
	public Style(){
		this.properties = new PropertySet();
		this.parentStyle = null;
	}
	
	/**
	 * Creates a style with some properties.
	 * @param properties the properties to associate to the style.
	 */
	public Style(PropertySet properties) {
		this.properties = properties;
		this.parentStyle = null;
	}
	
	/**
	 * Creates a style with properties and a parent style.
	 * All the properties of the parent style are automatically added to the new style.
	 * If the parent contains a property that is also in properties, the value of the parent property gets overwritten.
	 * For example if the parent contains 'font-weight: bold' and 'font-weight: normal' is added, the final value will be 'normal'.
	 * @param properties the properties to associate to the style.
	 * @param parentStyle the parent style.
	 */
	public Style(PropertySet properties, Style parentStyle) {
		this.parentStyle = parentStyle;
		this.properties.addAll(parentStyle.getProperties());
		this.properties.addAll(properties);
	}
	
	public Style(Style s){
		this.parentStyle = s.parentStyle;
		this.properties = s.properties;
	}
	
	/**
	 * 
	 * @return the properties of this Style.
	 */
	public PropertySet getProperties(){
		return this.properties;
	}
	
	/**
	 * Adds a property to the Style.
	 * If the property already exists, it will be overwritten.
	 * @param p property to be added
	 * @return success.
	 */
	public boolean addProperty(Property p){
		return this.properties.add(p);
	}
	
	/**
	 * Adds a set of properties to the Style.
	 * This will have the same result as a series of addProperty over p.
	 * @param p set of properties.
	 * @return success.
	 */
	public boolean addProperties(PropertySet p){
		return this.properties.addAll(p);
	}
	
	/**
	 * Sets the parent of the Style.
	 * All properties are discarded, the Style becomes identical to its parent.
	 * @param p the new parent.
	 */
	public void setParent(Style p){
		this.parentStyle = p;
		this.properties.addAll(p.getProperties());
	}
	
	/**
	 * Returns the parent element of the Style, null if it has none.
	 * @return
	 */
	public Style getParent(){
		return this.parentStyle;
	}
	
	/**
	 * Removes a property from the Style.
	 * @param label name of the property.
	 * @param value value of the property.
	 */
	public void removeProperty(String label, String value){
		Iterator<Property> i = this.properties.iterator();
		boolean propertyFound = false;
		while(i.hasNext() && !propertyFound){
			Property p = i.next();
			if(p.getLabel() == label && p.getValue() == value){
				propertyFound = true;
				this.properties.remove(p);
			}
		}
	}
}
