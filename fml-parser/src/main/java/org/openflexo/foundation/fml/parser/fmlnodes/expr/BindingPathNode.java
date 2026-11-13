/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.parser.AbstractExpressionFactory;
import org.openflexo.foundation.fml.parser.BindingPathFactory;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.TKwNew;
import org.openflexo.foundation.fml.parser.node.TLPar;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * @author sylvain
 * 
 */
public class BindingPathNode extends ExpressionNode<Node, BindingPath> {

	public BindingPathNode(Node astNode, AbstractExpressionFactory expressionFactory) {
		super(astNode, expressionFactory);
		// buildModelObjectFromAST() was already called, but too early (bindable not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public BindingPathNode(BindingPath bindingPath, AbstractExpressionFactory expressionFactory) {
		super(bindingPath, expressionFactory);
	}

	@Override
	public BindingPath buildModelObjectFromAST(Node astNode) {

		if (getExpressionFactory() != null) {
			return BindingPathFactory.makeBindingPath(astNode, getExpressionFactory(), this);

			// List<AbstractBindingPathElement> bindingPath = BindingPathFactory.makeBindingPath(astNode, getExpressionFactory(), this);
			// return new BindingPath(bindingPath, getExpressionFactory().getBindable(), JavaPrettyPrinter.getInstance());
		}
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------------------------
	// Preserving pretty-print (CORE-D-33)
	// A path is printed element by element. Its elements are not found by identity: a binding variable, or an element that was resolved
	// late, is replaced by another object when the path is resolved. They are found by position: element i of the path is printed in the
	// place of the i-th element that was parsed, as long as the path has the same shape (same number of elements, of the same kind, a
	// call with as many arguments). A name keeps its parsed text while its value does not change; a call keeps its parsed head
	// ("foo", "new A::init") while the head of its serialization does not change, and its arguments are printed by their own nodes.
	// ------------------------------------------------------------------------------------------------------------------------------

	/** The nodes of the elements of this path, as parsed, the binding variable first when there is one */
	private List<AbstractBindingPathElementNode<?, ?>> parsedElements;

	private List<AbstractBindingPathElementNode<?, ?>> getParsedElements() {
		if (parsedElements == null) {
			parsedElements = new ArrayList<>();
			for (P2PPNode<?, ?> child : getChildren()) {
				if (child instanceof AbstractBindingPathElementNode) {
					parsedElements.add((AbstractBindingPathElementNode<?, ?>) child);
				}
			}
		}
		return parsedElements;
	}

	/** The current element at the supplied position (the binding variable counts as the first element), null when there is none */
	private IBindingPathElement currentElement(int position) {
		BindingPath path = getModelObject();
		if (path == null) {
			return null;
		}
		int index = position;
		if (path.getBindingVariable() != null) {
			if (position == 0) {
				return path.getBindingVariable();
			}
			index--;
		}
		if (path.getBindingPath() == null || index < 0 || index >= path.getBindingPath().size()) {
			return null;
		}
		return path.getBindingPath().get(index);
	}

	private int currentSize() {
		BindingPath path = getModelObject();
		if (path == null) {
			return 0;
		}
		return (path.getBindingVariable() != null ? 1 : 0) + (path.getBindingPath() != null ? path.getBindingPath().size() : 0);
	}

	private DataBinding<?> currentArgument(int position, int argumentIndex) {
		IBindingPathElement element = currentElement(position);
		if (element instanceof FunctionPathElement) {
			List<DataBinding<?>> arguments = ((FunctionPathElement<?>) element).getArguments();
			if (arguments != null && argumentIndex < arguments.size()) {
				return arguments.get(argumentIndex);
			}
		}
		return null;
	}

	/** The value a name stands for: the name of a variable, the serialization of a simple element */
	private String nameValue(int position) {
		IBindingPathElement element = currentElement(position);
		if (element instanceof BindingVariable) {
			return ((BindingVariable) element).getVariableName();
		}
		return element != null ? element.getSerializationRepresentation() : null;
	}

	/** The value the head of a call stands for: its serialization up to the parenthesis that opens its arguments */
	private String headValue(int position) {
		IBindingPathElement element = currentElement(position);
		if (element == null) {
			return null;
		}
		String serialization = element.getSerializationRepresentation();
		if (serialization == null) {
			return null;
		}
		int lpar = serialization.indexOf('(');
		return lpar >= 0 ? serialization.substring(0, lpar) : serialization;
	}

	@Override
	protected void prepareParsedPrettyPrint() {
		List<AbstractBindingPathElementNode<?, ?>> elements = getParsedElements();
		for (int i = 0; i < elements.size(); i++) {
			final int position = i;
			AbstractBindingPathElementNode<?, ?> elementNode = elements.get(i);
			if (elementNode instanceof AbstractCallBindingPathElementNode) {
				AbstractCallBindingPathElementNode<?, ?> callNode = (AbstractCallBindingPathElementNode<?, ?>) elementNode;
				RawSourceFragment head = headFragment(callNode);
				if (head == null) {
					// The parenthesis was not found: the whole call is a leaf
					appendLeaf(() -> {
						IBindingPathElement element = currentElement(position);
						return element != null ? element.getSerializationRepresentation() : null;
					}, callFragment(callNode));
					continue;
				}
				appendLeaf(() -> headValue(position), head);
				List<DataBinding<?>> arguments = callNode.getArguments();
				if (arguments != null) {
					for (int k = 0; k < arguments.size(); k++) {
						final int argumentIndex = k;
						append(childContents("", () -> currentArgument(position, argumentIndex), "", Indentation.DoNotIndent));
					}
				}
			}
			else {
				appendLeaf(() -> nameValue(position), elementNode.getFragment());
			}
		}
	}

	/** The parsed text of a call, from its head to its last character */
	private RawSourceFragment callFragment(AbstractCallBindingPathElementNode<?, ?> callNode) {
		RawSourceFragment whole = callNode.getFragment();
		RawSourcePosition start = callNode.getStartLocation();
		if (whole == null || start == null) {
			return whole;
		}
		return getRawSource().makeFragment(start, whole.getEndPosition());
	}

	/**
	 * The head of a call: from its first character ("new" for an instantiation, whose AST also holds the containment that precedes it;
	 * the method name for a method call, whose AST also holds the receiver) to the parenthesis that opens its arguments
	 */
	private RawSourceFragment headFragment(AbstractCallBindingPathElementNode<?, ?> callNode) {
		if (callNode.getASTNode() == null) {
			return null;
		}
		List<Token> tokens = new ArrayList<>();
		((Node) callNode.getASTNode()).apply(new DepthFirstAdapter() {
			@Override
			public void defaultCase(Node node) {
				if (node instanceof Token) {
					tokens.add((Token) node);
				}
			}
		});
		RawSourcePosition start = null;
		if (callNode instanceof AbstractNewFlexoConceptInstanceNode || callNode instanceof NewClassInstanceNode) {
			for (Token token : tokens) {
				if (token instanceof TKwNew) {
					start = getFragment(token).getStartPosition();
					break;
				}
			}
		}
		if (start == null) {
			start = callNode.getStartLocation();
		}
		if (start == null) {
			return null;
		}
		for (Token token : tokens) {
			if (token instanceof TLPar) {
				RawSourcePosition lpar = getFragment(token).getStartPosition();
				if (lpar.isAfterOrEquals(start)) {
					return getRawSource().makeFragment(start, lpar);
				}
			}
		}
		return null;
	}

	/**
	 * The parsed text is used while the path has the shape it was parsed with: same number of elements, of the same kind, calls with as
	 * many arguments, each of them still printed by a node of this path
	 */
	@Override
	protected boolean canPreserveParsedText() {
		if (getModelObject() == null) {
			return false;
		}
		List<AbstractBindingPathElementNode<?, ?>> elements = getParsedElements();
		if (elements.size() != currentSize()) {
			return false;
		}
		for (int i = 0; i < elements.size(); i++) {
			AbstractBindingPathElementNode<?, ?> elementNode = elements.get(i);
			IBindingPathElement current = currentElement(i);
			if (elementNode instanceof BindingVariableNode) {
				if (!(current instanceof BindingVariable)) {
					return false;
				}
			}
			else if (elementNode instanceof AbstractCallBindingPathElementNode) {
				if (!(current instanceof FunctionPathElement)) {
					return false;
				}
				List<DataBinding<?>> parsedArguments = ((AbstractCallBindingPathElementNode<?, ?>) elementNode).getArguments();
				List<DataBinding<?>> currentArguments = ((FunctionPathElement<?>) current).getArguments();
				int parsedCount = parsedArguments != null ? parsedArguments.size() : 0;
				int currentCount = currentArguments != null ? currentArguments.size() : 0;
				if (parsedCount != currentCount) {
					return false;
				}
				for (int k = 0; k < currentCount; k++) {
					if (currentArguments.get(k) == null || getObjectNode(currentArguments.get(k)) == null) {
						return false;
					}
				}
			}
			else if (!(current instanceof SimplePathElement)) {
				return false;
			}
		}
		return true;
	}

}
