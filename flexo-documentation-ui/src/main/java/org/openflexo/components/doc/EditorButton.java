/**
 * 
 */
package org.openflexo.components.doc;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.openflexo.foundation.doc.Property;

/**
 * @author Bruno Quercia
 *
 */
public class EditorButton extends JButton implements Action {

	private Property property;

	/**
	 * 
	 */
	public EditorButton(Property property) {
		this.property = property;
	}

	/**
	 * @param arg0
	 */
	public EditorButton(Icon arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public EditorButton(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public EditorButton(Action arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EditorButton(String arg0, Icon arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.swing.Action#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Action#putValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	public Property getProperty() {
		return this.property;
	}

}
