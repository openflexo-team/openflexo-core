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

package org.openflexo.foundation.fml.validation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionVisitor;
import org.openflexo.connie.expr.VisitorException;
import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoException;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.binding.FlexoBehaviourPathElement;
import org.openflexo.foundation.fml.controlgraph.CatchClause;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.controlgraph.TryStatement;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.ThrowStatement;
import org.openflexo.foundation.fml.rt.editionaction.AbstractAddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.FireEvent;
import org.openflexo.foundation.fml.rt.error.FMLErrorTypes;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;

/**
 * Checked exceptions (CORE-F-11 phase 7): the exceptions defined in FML are checked, as Java exceptions; the built-in error types are
 * not. A statement that may raise a checked exception — a {@code throw}, a call of a behaviour, a {@code new} whose creation scheme, a
 * {@code delete} whose deletion scheme, a {@code fire} whose listeners declare it — is valid when an enclosing {@code try} catches it, or
 * when the behaviour it belongs to declares it ({@code throws}). A getter, a setter and an expression property declare nothing.
 * <p>
 * The analysis is static: a call is checked against the behaviour it resolves to, an overriding behaviour declaring no more checked
 * exceptions than the behaviour it overrides; the listeners of a {@code fire} are the ones of the virtual model of the statement.
 */
public class CheckedExceptions {

	private CheckedExceptions() {
	}

	/** The FML exception that supplied type stands for when it is checked, or null */
	public static FlexoException checkedException(Type type) {
		return FMLErrorTypes.getFlexoException(type);
	}

	/** Tell if supplied checked exception is covered by one of supplied types (catch clauses or throws declarations) */
	public static boolean isCovered(FlexoException exception, Collection<? extends Type> handlers) {
		for (Type handler : handlers) {
			if (handler != null && FMLErrorTypes.covers(handler, exception.getInstanceType())) {
				return true;
			}
		}
		return false;
	}

	/** The checked exceptions declared by supplied behaviour */
	public static Set<FlexoException> declaredBy(FlexoBehaviour behaviour) {
		Set<FlexoException> returned = new LinkedHashSet<>();
		if (behaviour != null) {
			for (Type type : behaviour.getThrownTypes()) {
				FlexoException exception = checkedException(type);
				if (exception != null) {
					returned.add(exception);
				}
			}
		}
		return returned;
	}

	/**
	 * The checked exceptions that supplied statement raises itself: its own expressions and the actions it runs, not the statements it
	 * contains (they are checked on their own)
	 */
	public static Set<FlexoException> raisedBy(FMLControlGraph statement) {
		Set<FlexoException> returned = new LinkedHashSet<>();
		if (statement instanceof ThrowStatement && ((ThrowStatement<?>) statement).getAssignableAction() != null) {
			FlexoException exception = checkedException(((ThrowStatement<?>) statement).getAssignableAction().getAssignableType());
			if (exception != null) {
				returned.add(exception);
			}
		}
		if (statement instanceof AbstractAddFlexoConceptInstance) {
			returned.addAll(declaredBy(((AbstractAddFlexoConceptInstance<?, ?>) statement).getCreationScheme()));
		}
		if (statement instanceof DeleteFlexoConceptInstance) {
			returned.addAll(declaredBy(((DeleteFlexoConceptInstance<?>) statement).getDeletionScheme()));
		}
		else if (statement instanceof DeleteAction && !isThis(((DeleteAction<?>) statement).getObject())) {
			// delete this runs no deletion scheme (DeleteAction.execute)
			FlexoConcept deleted = conceptOf(((DeleteAction<?>) statement).getObject());
			if (deleted != null) {
				DeletionScheme scheme = deleted.getDefaultDeletionScheme();
				returned.addAll(declaredBy(scheme));
			}
		}
		if (statement instanceof FinalizeMatching) {
			// end match ... unmatched: the behaviour run on every unmatched instance
			returned.addAll(declaredBy(((FinalizeMatching) statement).getFlexoBehaviour()));
		}
		if (statement instanceof FireEvent) {
			FlexoConcept event = conceptOf(((FireEvent) statement).getEventInstance());
			// The model of the statement: the concept of its behaviour when it is a model, otherwise the model owning that concept
			FlexoConcept concept = statement.getFlexoConcept();
			VirtualModel vm = concept instanceof VirtualModel ? (VirtualModel) concept : concept != null ? concept.getOwningVirtualModel() : null;
			if (event != null && vm != null) {
				for (EventListener listener : listenersOf(vm)) {
					if (listener.getEvent() != null && listener.getEvent().isAssignableFrom(event)) {
						returned.addAll(declaredBy(listener));
					}
				}
			}
		}
		for (DataBinding<?> binding : bindingsOf(statement)) {
			returned.addAll(raisedBy(binding));
		}
		return returned;
	}

	/** The checked exceptions raised by the evaluation of supplied binding: the behaviours and the creation schemes it calls */
	public static Set<FlexoException> raisedBy(DataBinding<?> binding) {
		Set<FlexoException> returned = new LinkedHashSet<>();
		if (binding != null && binding.isSet() && binding.getExpression() != null) {
			collect(binding.getExpression(), returned, new ArrayList<>());
		}
		return returned;
	}

	/**
	 * The types handling an exception raised by supplied statement: the catch clauses of every try statement whose try block contains it
	 */
	public static List<Type> enclosingHandlers(FMLControlGraph statement) {
		List<Type> returned = new ArrayList<>();
		FMLControlGraph previous = statement;
		FMLControlGraphOwner owner = statement.getOwner();
		while (owner instanceof FMLControlGraph) {
			if (owner instanceof TryStatement && previous == ((TryStatement) owner).getTryControlGraph()) {
				for (CatchClause clause : ((TryStatement) owner).getCatchClauses()) {
					returned.add(clause.getCatchType());
				}
			}
			previous = (FMLControlGraph) owner;
			owner = previous.getOwner();
		}
		return returned;
	}

	/** The behaviour whose body holds supplied statement, null for the body of a getter or a setter */
	public static FlexoBehaviour behaviourOf(FMLControlGraph statement) {
		FMLControlGraphOwner root = statement.getRootOwner();
		return root instanceof FlexoBehaviour ? (FlexoBehaviour) root : null;
	}

	/**
	 * Tell if supplied statement is checked: it belongs to a behaviour or to the body of a getter or a setter; a command of an fml-cli
	 * script, which has no try, is not
	 */
	public static boolean isChecked(FMLControlGraph statement) {
		FMLControlGraphOwner root = statement.getRootOwner();
		return root instanceof FlexoBehaviour || root instanceof GetProperty;
	}

	/** The checked exceptions that supplied behaviour declares, as types handling the exceptions raised in its body */
	public static List<Type> declaredHandlers(FlexoBehaviour behaviour) {
		// A built-in error type in throws is documentation: it covers no exception defined in FML
		List<Type> returned = new ArrayList<>();
		for (FlexoException exception : declaredBy(behaviour)) {
			returned.add(exception.getInstanceType());
		}
		return returned;
	}

	private static void collect(Expression expression, Set<FlexoException> into, List<Expression> visited) {
		try {
			expression.visit(new ExpressionVisitor() {
				@Override
				public void visit(Expression e) throws VisitorException {
					if (!(e instanceof BindingPath) || visited.contains(e)) {
						return;
					}
					visited.add(e);
					for (BindingPathElement element : ((BindingPath) e).getBindingPath()) {
						if (element instanceof FlexoBehaviourPathElement) {
							into.addAll(declaredBy(((FlexoBehaviourPathElement) element).getFlexoBehaviour()));
						}
						if (element instanceof CreationSchemePathElement) {
							AbstractCreationScheme scheme = ((CreationSchemePathElement<?>) element).getCreationScheme();
							into.addAll(declaredBy(scheme));
						}
						if (element instanceof FunctionPathElement) {
							for (DataBinding<?> argument : ((FunctionPathElement<?>) element).getArguments()) {
								if (argument != null && argument.getExpression() != null) {
									collect(argument.getExpression(), into, visited);
								}
							}
						}
					}
				}
			});
		} catch (VisitorException e) {
			// Nothing thrown by the visitor
		}
	}

	private static FlexoConcept conceptOf(DataBinding<?> binding) {
		if (binding == null || !binding.isSet()) {
			return null;
		}
		if (binding.getAnalyzedType() instanceof FlexoConceptInstanceType) {
			return ((FlexoConceptInstanceType) binding.getAnalyzedType()).getFlexoConcept();
		}
		// A binding declared with a Java type (FireEvent: FlexoEventInstance) may analyze to it: the type of its last path element
		if (binding.getExpression() instanceof BindingPath) {
			List<BindingPathElement> path = ((BindingPath) binding.getExpression()).getBindingPath();
			if (!path.isEmpty() && path.get(path.size() - 1).getType() instanceof FlexoConceptInstanceType) {
				return ((FlexoConceptInstanceType) path.get(path.size() - 1).getType()).getFlexoConcept();
			}
		}
		return null;
	}

	private static List<EventListener> listenersOf(VirtualModel vm) {
		List<EventListener> returned = new ArrayList<>();
		addListeners(vm, returned);
		for (FlexoConcept concept : vm.getAllFlexoConcepts()) {
			addListeners(concept, returned);
		}
		return returned;
	}

	private static void addListeners(FlexoConcept concept, List<EventListener> into) {
		// Inherited listeners included, as the run-time engine registers them
		for (EventListener listener : concept.getAccessibleFlexoBehaviours(EventListener.class, false)) {
			if (!into.contains(listener)) {
				into.add(listener);
			}
		}
	}

	private static boolean isThis(DataBinding<?> binding) {
		return binding != null && binding.isSet() && "this".equals(binding.toString().trim());
	}

	/**
	 * The bindings of supplied statement: its PAMELA properties of type {@link DataBinding}, and those of the objects it embeds that are
	 * not statements (the arguments of an action, for instance)
	 */
	private static List<DataBinding<?>> bindingsOf(FMLObject object) {
		List<DataBinding<?>> returned = new ArrayList<>();
		collectBindings(object, returned, 0);
		return returned;
	}

	private static void collectBindings(FMLObject object, List<DataBinding<?>> into, int depth) {
		if (object == null || depth > 2 || object.getImplementedInterface() == null) {
			return;
		}
		for (Method method : object.getImplementedInterface().getMethods()) {
			Getter getter = method.getAnnotation(Getter.class);
			if (getter == null || method.getParameterCount() > 0) {
				continue;
			}
			try {
				if (DataBinding.class.isAssignableFrom(method.getReturnType())) {
					Object value = method.invoke(object);
					if (value instanceof DataBinding && !into.contains(value)) {
						into.add((DataBinding<?>) value);
					}
				}
				else if (method.getAnnotation(Embedded.class) != null && getter.cardinality() == Getter.Cardinality.LIST) {
					Object value = method.invoke(object);
					if (value instanceof List) {
						for (Object item : (List<?>) value) {
							if (item instanceof FMLObject && !(item instanceof FMLControlGraph)) {
								collectBindings((FMLObject) item, into, depth + 1);
							}
						}
					}
				}
			} catch (ReflectiveOperationException | RuntimeException e) {
				// A getter that cannot be read here holds no binding the analysis can see
			}
		}
	}
}
