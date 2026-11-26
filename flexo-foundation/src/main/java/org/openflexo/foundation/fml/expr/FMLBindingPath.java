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
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.foundation.fml.expr;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.binding.StaticMethodPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.ExpressionPrettyPrinter;
import org.openflexo.connie.expr.UnresolvedBindingVariable;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;

/**
 * A {@link BindingPath} of FML whose first element may have been taken for a static call too early.
 *
 * 'Foo.bar()' has one syntax for a static call on the type Foo and for a call on the variable Foo (CORE-F-5). As in Java, a variable hides a
 * type of the same name. When the path is built, the variables visible from its owner are not always known yet: while a unit is parsed
 * (parameters, properties, locals are attached later), and in a script (a variable comes into existence when it is first assigned, at run
 * time). The path keeps the receiver it took for a type; each time it is validated, a variable of that name visible from its owner turns
 * the static call into a call on the variable, the way a {@code BindingVariable} that was not resolved is resolved late.
 *
 * The other way round, a receiver that was neither a variable nor a known type is kept as a variable that is not resolved. When, later,
 * no variable of that name is visible and the name resolves to a type in the typing space of the unit of the owner (an import added, a
 * unit that completed), the call on that variable becomes a static call on the type.
 *
 * @author sylvain
 */
public class FMLBindingPath extends BindingPath {

	/** The segments of the receiver of the first element, when that element was taken for a static call; null otherwise */
	private List<String> staticCallReceiver;

	public FMLBindingPath(BindingVariable aBindingVariable, List<BindingPathElement> aBindingPath, Bindable owner,
			ExpressionPrettyPrinter prettyPrinter) {
		super(aBindingVariable, aBindingPath, owner, prettyPrinter);
	}

	/**
	 * Record the receiver ('Foo' in 'Foo.bar()', 'a', 'b', 'C' in 'a.b.C.m()') of the first element of this path, a call taken for a static
	 * call on the type that receiver names
	 */
	public void setStaticCallReceiver(List<String> segments) {
		staticCallReceiver = segments != null && !segments.isEmpty() ? new ArrayList<>(segments) : null;
	}

	public List<String> getStaticCallReceiver() {
		return staticCallReceiver != null ? Collections.unmodifiableList(staticCallReceiver) : null;
	}

	@Override
	public boolean isValid() {
		if (!reconsiderStaticCall()) {
			reconsiderCallOnUnresolvedVariable();
		}
		return super.isValid();
	}

	/**
	 * Turn the first element of this path into a call on a variable when its receiver, taken for a type, names a variable visible from the
	 * owner
	 *
	 * @return true when the path was changed
	 */
	public boolean reconsiderStaticCall() {
		if (staticCallReceiver == null || getBindingVariable() != null || getBindingPath().isEmpty()
				|| !(getBindingPath().get(0) instanceof StaticMethodPathElement)) {
			return false;
		}
		Bindable owner = getOwner();
		if (owner == null || owner.getBindingModel() == null || owner.getBindingFactory() == null
				|| owner.getBindingModel().bindingVariableNamed(staticCallReceiver.get(0)) == null) {
			return false;
		}
		BindingFactory factory = owner.getBindingFactory();
		StaticMethodPathElement<?> staticCall = (StaticMethodPathElement<?>) getBindingPath().get(0);
		List<BindingPathElement> following = new ArrayList<>(getBindingPath().subList(1, getBindingPath().size()));
		List<String> receiver = staticCallReceiver;
		staticCallReceiver = null;

		clearBindingPathElements();
		BindingVariable variable = new UnresolvedBindingVariable(receiver.get(0));
		setBindingVariable(variable);
		IBindingPathElement parent = variable;
		for (String segment : receiver.subList(1, receiver.size())) {
			SimplePathElement<?> element = factory.makeSimplePathElement(parent, segment, owner);
			addBindingPathElement(element);
			parent = element;
		}
		SimpleMethodPathElement<?> call = factory.makeSimpleMethodPathElement(parent, staticCall.getMethodName(), staticCall.getArguments(),
				owner);
		addBindingPathElement(call);
		appendRebuilt(following, call, factory, owner);
		invalidate();
		return true;
	}

	/**
	 * Turn a call on a variable that is not visible from the owner ('Foo.bar()', 'a.b.C.m()') into a static call, when its receiver now
	 * names a type of the typing space of the unit of the owner. The contrary of {@link #reconsiderStaticCall()}
	 *
	 * @return true when the path was changed
	 */
	public boolean reconsiderCallOnUnresolvedVariable() {
		if (staticCallReceiver != null || getBindingVariable() == null || getBindingVariable().getVariableName() == null) {
			return false;
		}
		Bindable owner = getOwner();
		if (owner == null || owner.getBindingModel() == null || owner.getBindingFactory() == null
				|| owner.getBindingModel().bindingVariableNamed(getBindingVariable().getVariableName()) != null) {
			return false;
		}
		// The receiver: the variable, then the names up to the first call
		List<String> receiver = new ArrayList<>();
		receiver.add(getBindingVariable().getVariableName());
		int callIndex = -1;
		for (int i = 0; i < getBindingPath().size(); i++) {
			BindingPathElement element = getBindingPath().get(i);
			if (element instanceof SimpleMethodPathElement) {
				callIndex = i;
				break;
			}
			if (!(element instanceof SimplePathElement)) {
				return false;
			}
			receiver.add(((SimplePathElement<?>) element).getLabel());
		}
		if (callIndex < 0) {
			return false;
		}
		Type type = resolveReceiverType(owner, String.join(".", receiver));
		if (type == null) {
			return false;
		}
		BindingFactory factory = owner.getBindingFactory();
		SimpleMethodPathElement<?> call = (SimpleMethodPathElement<?>) getBindingPath().get(callIndex);
		List<BindingPathElement> following = new ArrayList<>(getBindingPath().subList(callIndex + 1, getBindingPath().size()));

		clearBindingPathElements();
		setBindingVariable(null);
		StaticMethodPathElement<?> staticCall = factory.makeStaticMethodPathElement(type, call.getMethodName(), call.getArguments(), owner);
		addBindingPathElement(staticCall);
		appendRebuilt(following, staticCall, factory, owner);
		staticCallReceiver = receiver;
		invalidate();
		return true;
	}

	/** The type the supplied name stands for in the typing space of the unit of the owner, null when it names no type */
	private static Type resolveReceiverType(Bindable owner, String typeName) {
		if (!(owner instanceof FMLObject)) {
			return null;
		}
		FMLCompilationUnit unit = ((FMLObject) owner).getDeclaringCompilationUnit();
		if (unit == null || unit.getTypingSpace() == null) {
			return null;
		}
		Type type = unit.getTypingSpace().resolveType(typeName);
		return type == null || type instanceof UnresolvedType ? null : type;
	}

	/** Append the supplied elements, built again on their new parent, as when the parent of an element changes */
	private void appendRebuilt(List<BindingPathElement> elements, IBindingPathElement parent, BindingFactory factory, Bindable owner) {
		for (BindingPathElement element : elements) {
			BindingPathElement rebuilt = element;
			if (element instanceof SimplePathElement) {
				rebuilt = factory.makeSimplePathElement(parent, ((SimplePathElement<?>) element).getLabel(), owner);
			}
			else if (element instanceof SimpleMethodPathElement) {
				rebuilt = factory.makeSimpleMethodPathElement(parent, ((SimpleMethodPathElement<?>) element).getMethodName(),
						((SimpleMethodPathElement<?>) element).getArguments(), owner);
			}
			addBindingPathElement(rebuilt);
			parent = rebuilt;
		}
	}

}
