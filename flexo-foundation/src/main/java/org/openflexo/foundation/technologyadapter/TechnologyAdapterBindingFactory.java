package org.openflexo.foundation.technologyadapter;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingFactory;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.JavaBindingFactory;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.TechnologySpecificCustomType;

/**
 * This class represent the {@link BindingFactory} dedicated to handle technology-specific binding elements<br>
 * This is the place where we implements binding path element strategy for types handled by this technology.<br>
 * For example, any {@link FlexoRole} defined in this technology-specific adapter should by handled. <br>
 * Following methods should be implemented: getAccessibleSimplePathElements(BindingPathElement),
 * getAccessibleFunctionPathElements(BindingPathElement), makeSimplePathElement(BindingPathElement,String),
 * makeFunctionPathElement(BindingPathElement,Function,List<DataBinding<?>>)
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapterBindingFactory extends JavaBindingFactory {
	static final Logger logger = Logger.getLogger(TechnologyAdapterBindingFactory.class.getPackage().getName());

	private final HashMap<BindingPathElement, HashMap<Object, SimplePathElement>> storedBindingPathElements;

	public TechnologyAdapterBindingFactory() {
		storedBindingPathElements = new HashMap<BindingPathElement, HashMap<Object, SimplePathElement>>();
	}

	protected final SimplePathElement getSimplePathElement(Object object, BindingPathElement parent) {
		HashMap<Object, SimplePathElement> storedValues = storedBindingPathElements.get(parent);
		if (storedValues == null) {
			storedValues = new HashMap<Object, SimplePathElement>();
			storedBindingPathElements.put(parent, storedValues);
		}
		SimplePathElement returned = storedValues.get(object);
		if (returned == null) {
			returned = makeSimplePathElement(object, parent);
			storedValues.put(object, returned);
		}
		return returned;
	}

	protected abstract SimplePathElement makeSimplePathElement(Object object, BindingPathElement parent);

	/**
	 * Return boolean indicating if this binding path element strategy should apply to supplied type
	 * 
	 * @param technologySpecificType
	 * @return
	 */
	public abstract boolean handleType(TechnologySpecificCustomType<?> technologySpecificType);

}