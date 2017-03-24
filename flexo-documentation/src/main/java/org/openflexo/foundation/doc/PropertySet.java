package org.openflexo.foundation.doc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A PropertySet is a HashSet of Property elements.
 * The difference with a classical HashSet is that the unicity only considers the label of the elements.
 * Therefore there cannot be two properties with the same label.
 * @author Bruno Quercia
 *
 */
public class PropertySet extends HashSet<Property> {
	
	@Override
	public boolean add(Property e){
		Iterator<Property> i = this.iterator();
		boolean valueChanged = false;
		while(i.hasNext() && !valueChanged){
			Property p = i.next();
			if(p.getLabel().equals(e.getLabel())){
				try {
					p.setValue(e.getValue());
					System.out.println("Une propri�t� a �t� remplac�e");
					return true;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else System.out.println("Labels diff�rents : source " + p.getLabel() + ", cible " + e.getLabel());
		}
		return super.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends Property> c){
		Iterator<?> i = c.iterator();
		boolean result = true;
		while(i.hasNext()){
			result = result && this.add((Property)i.next());
		}
		return result;
	}
}
