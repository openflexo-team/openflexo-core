/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
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
 *          of EPL 1.0, the licensors of this Program grant you additional
 *          permission to convey the resulting work. *
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

package org.openflexo.foundation.fml;

import java.util.Objects;
import java.util.function.Function;

/**
 * A reference, by its name, to an {@link FMLObject} found in a context that the holder of the reference knows: a property of the concept
 * a match addresses, the behaviour an 'end match' calls on it, a parameter of the behaviour an argument is given to.<br>
 *
 * This is the value of the PAMELA properties that hold such references. Two references are equal when they name the same object, so that
 * merging a compilation unit with another one ({@code updateWith()}, what the FML editor does with the text it has just parsed) compares
 * names instead of descending into the referenced objects, and never brings an object of the other compilation unit into this one: the
 * {@code @Updater} of such a property keeps the name only ({@link #unresolved()}), and the holder resolves it in its own context
 * ({@link #resolve(Function)}).<br>
 *
 * A reference made from an object ({@link #to(FMLObject)}) follows the renaming of that object, and stays usable when its context cannot
 * be reached; a reference made from a name ({@link #named(String)}) designates whatever object has this name in the context.<br>
 *
 * Instances are immutable.
 *
 * @author sylvain
 *
 * @param <O>
 *            type of the referenced object
 */
public final class FMLNamedReference<O extends FMLObject> {

	private final String name;
	private final O object;

	private FMLNamedReference(String name, O object) {
		this.name = name;
		this.object = object;
	}

	/**
	 * A reference to the supplied object, which follows its renaming
	 *
	 * @param object
	 *            the referenced object
	 * @return the reference, or null for a null object
	 */
	public static <O extends FMLObject> FMLNamedReference<O> to(O object) {
		return object != null ? new FMLNamedReference<>(null, object) : null;
	}

	/**
	 * A reference to the object which has the supplied name in the context of the holder of the reference
	 *
	 * @param name
	 *            the name of the referenced object
	 * @return the reference, or null for a null name
	 */
	public static <O extends FMLObject> FMLNamedReference<O> named(String name) {
		return name != null ? new FMLNamedReference<>(name, null) : null;
	}

	/**
	 * The name of the referenced object: its current name when the reference was made from an object
	 *
	 * @return the name
	 */
	public String getName() {
		return object != null ? object.getName() : name;
	}

	/**
	 * The object this reference was made from, if any
	 *
	 * @return the object, or null for a reference made from a name
	 */
	public O getObject() {
		return object;
	}

	/**
	 * The same reference, without the object it was made from: what a holder keeps from a reference held by an object of another
	 * compilation unit
	 *
	 * @return a reference made from the name of this one
	 */
	public FMLNamedReference<O> unresolved() {
		return object != null ? new FMLNamedReference<>(getName(), null) : this;
	}

	/**
	 * Resolves this reference in the context of its holder
	 *
	 * @param lookup
	 *            finds an object by its name in the context of the holder; null when the context cannot be reached
	 * @return the object of the context having the name of this reference; otherwise the object this reference was made from, if any
	 */
	public O resolve(Function<String, ? extends O> lookup) {
		String referencedName = getName();
		O found = lookup != null && referencedName != null ? lookup.apply(referencedName) : null;
		return found != null ? found : object;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FMLNamedReference)) {
			return false;
		}
		return Objects.equals(getName(), ((FMLNamedReference<?>) obj).getName());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName());
	}

	@Override
	public String toString() {
		return "FMLNamedReference[" + getName() + "]";
	}
}
