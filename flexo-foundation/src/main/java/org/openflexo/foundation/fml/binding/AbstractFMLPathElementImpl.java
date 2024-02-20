/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject.FMLObjectImpl;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Abstract base implementation for a path element
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractFMLPathElementImpl extends FMLObjectImpl implements BindingPathElement, HasPropertyChangeSupport {

	private IBindingPathElement parent;
	private boolean activated = false;
	private Bindable bindable;
	private BindingPath bindingPath;

	private String parsed;

	private BindingPathElementOwner bindingPathElementOwner;

	public static final String NAME_PROPERTY = "propertyName";
	public static final String TYPE_PROPERTY = "type";

	public AbstractFMLPathElementImpl() {
	}

	/*public AbstractFMLPathElementImpl(IBindingPathElement parent, String parsed, Bindable bindable) {
		this();
		setParent(parent);
		setParsed(parsed);
		setBindable(bindable);
	}*/

	@Override
	public Bindable getBindable() {
		return bindable;
	}

	@Override
	public void setBindable(Bindable bindable) {
		if ((bindable == null && this.bindable != null) || (bindable != null && !bindable.equals(this.bindable))) {
			Bindable oldValue = this.bindable;
			this.bindable = bindable;
			getPropertyChangeSupport().firePropertyChange("bindable", oldValue, bindable);
		}
	}

	@Override
	public BindingPathElementOwner getBindingPathElementOwner() {
		return bindingPathElementOwner;
	}

	@Override
	public void setBindingPathElementOwner(BindingPathElementOwner owner) {
		if ((owner == null && this.bindingPathElementOwner != null) || (owner != null && !owner.equals(this.bindingPathElementOwner))) {
			BindingPathElementOwner oldValue = this.bindingPathElementOwner;
			this.bindingPathElementOwner = owner;
			getPropertyChangeSupport().firePropertyChange("bindingPathElementOwner", oldValue, owner);
		}
	}

	/**
	 * Return original parsed String
	 * 
	 * @return
	 */
	public String getParsed() {
		return parsed;
	}

	public void setParsed(String parsed) {
		if ((parsed == null && this.parsed != null) || (parsed != null && !parsed.equals(this.parsed))) {
			String oldValue = this.parsed;
			this.parsed = parsed;
			getPropertyChangeSupport().firePropertyChange("parsed", oldValue, parsed);
		}
	}

	/**
	 * Activate this {@link BindingPathElement} by starting observing relevant objects when required
	 */
	@Override
	public void activate(BindingPath bindingPath) {
		this.activated = true;
		this.bindingPath = bindingPath;
	}

	/**
	 * Desactivate this {@link BindingPathElement} by stopping observing relevant objects when required
	 */
	@Override
	public void desactivate() {
		this.activated = false;
		this.bindingPath = null;
	}

	/**
	 * Return boolean indicating if this {@link BindingPathElement} is activated
	 * 
	 * @return
	 */
	@Override
	public boolean isActivated() {
		return activated;
	}

	@Override
	public BindingPath getBindingPath() {
		return bindingPath;
	}

	@Override
	public void setBindingPath(BindingPath bindingPath) {
		this.bindingPath = bindingPath;
	}

	/**
	 * Return parent of this BindingPathElement
	 * 
	 * @return
	 */
	@Override
	public IBindingPathElement getParent() {
		return parent;
	}

	/**
	 * Sets parent of this {@link BindingPathElement}
	 * 
	 * @param parent
	 */
	@Override
	public void setParent(IBindingPathElement parent) {
		if ((parent == null && this.parent != null) || (parent != null && !parent.equals(this.parent))) {
			IBindingPathElement oldValue = this.parent;
			this.parent = parent;
			getPropertyChangeSupport().firePropertyChange("parent", oldValue, parent);
		}
	}

	/**
	 * Evaluate the acceptability relatively to type checking of this {@link BindingPathElement} in the context of a parent
	 * {@link IBindingPathElement}
	 * 
	 * @param parentElement
	 *            parent element of current {@link IBindingPathElement}
	 * @param parentType
	 *            resulting type for the parent {@link IBindingPathElement} in its context
	 * @return
	 */
	@Override
	public BindingPathCheck checkBindingPathIsValid(IBindingPathElement parentElement, Type parentType) {

		BindingPathCheck check = new BindingPathCheck();

		if (requiresContext()) {
			if (getParent() == null) {
				check.invalidBindingReason = "No parent for: " + this;
				check.valid = false;
				return check;
			}

			if (getParent() != parentElement) {
				check.invalidBindingReason = "Inconsistent parent for: " + this;
				check.valid = false;
				return check;
			}

			if (!TypeUtils.isTypeAssignableFrom(parentElement.getType(), getParent().getType(), true)) {
				check.invalidBindingReason = "Mismatched: " + parentElement.getType() + " and " + getParent().getType();
				check.valid = false;
				return check;
			}
			check.returnedType = TypeUtils.makeInstantiatedType(getType(), parentType);
		}

		check.valid = true;
		return check;
	}

	/**
	 * Return boolean indicating if this path element requires a context (a parent path element)
	 * 
	 * @return
	 */
	public abstract boolean requiresContext();

	@Override
	public final String toString() {
		return getClass().getSimpleName() + ":" + getSerializationRepresentation();
	}

	@Override
	public BindingFactory getBindingFactory() {
		if (getBindable() != null) {
			return getBindable().getBindingFactory();
		}
		return super.getBindingFactory();
	}

	@Override
	public BindingModel getBindingModel() {
		if (getBindable() != null) {
			return getBindable().getBindingModel();
		}
		return null;
	}

	@Override
	public FMLCompilationUnit getResourceData() {
		return null;
	}

	@Override
	public String getRelativePath() {
		if (getParent() != null) {
			return getParent().getRelativePath() + "." + getSerializationRepresentation();
		}
		return getSerializationRepresentation();
	}

}
