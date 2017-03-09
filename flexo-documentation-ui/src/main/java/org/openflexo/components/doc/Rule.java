/**
 * 
 */
package org.openflexo.components.doc;

import org.openflexo.foundation.doc.Property;

/**
 * @author Bruno Quercia
 *
 */
public class Rule {
	private Property property;
	private String before;

	public String getBefore() {
		return before;
	}

	public String getAfter() {
		return after;
	}

	private String after;

	/**
	 * 
	 */
	public Rule(Property p, String b, String a) {
		this.property = p;
		this.before = b;
		this.after = a;
	}

	public Rule(Property p) {
		this.property = p;
		this.before = "<span style='" + p.getLabel() + ":" + p.getValue() + "'>";
		this.after = "</span>";
	}

	@Override
	public boolean equals(Object o) {
		return property.equals(((Rule) o).getProperty());
	}

	public Property getProperty() {
		return this.property;
	}

}
