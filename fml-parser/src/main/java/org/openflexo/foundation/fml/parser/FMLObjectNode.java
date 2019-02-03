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

package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.foundation.fml.parser.RawSource.RawSourcePosition;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * Maintains consistency between the model (represented by an {@link FMLObject}) and source code represented in FML language
 * 
 * Works
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectNode<N extends Node, T extends FMLPrettyPrintable> implements FMLPrettyPrintDelegate<T> {

	private static final Logger logger = Logger.getLogger(FMLObjectNode.class.getPackage().getName());

	// private N astNode;
	private final FMLSemanticsAnalyzer analyser;
	private T fmlObject;

	private FMLObjectNode<?, ?> parent;
	private ArrayList<FMLObjectNode<?, ?>> children = new ArrayList<>();

	private RawSourcePosition startPosition;
	private RawSourcePosition endPosition;
	private RawSourceFragment parsedFragment;

	public FMLObjectNode(N astNode, FMLSemanticsAnalyzer analyser) {
		// this.astNode = astNode;
		this.analyser = analyser;
		fmlObject = buildFMLObjectFromAST(astNode);
		fmlObject.setPrettyPrintDelegate(this);
		fmlObject.initializeDeserialization(getFactory());
		/*if (!analyser.fmlNodes.isEmpty()) {
			parent = analyser.fmlNodes.peek();
			// System.out.println("Parent " + parent.getClass().getSimpleName() + Integer.toHexString(parent.hashCode()) + " > "
			// + getClass().getSimpleName() + Integer.toHexString(hashCode()));
			parent.children.add(this);
		}*/
	}

	public FMLObjectNode(T aFMLObject, FMLSemanticsAnalyzer analyser) {
		this.analyser = analyser;
		this.fmlObject = aFMLObject;
		fmlObject.setPrettyPrintDelegate(this);
	}

	protected void addToChildren(FMLObjectNode<?, ?> child, int index) {
		child.parent = this;
		children.add(index, child);
	}

	protected void addToChildren(FMLObjectNode<?, ?> child) {
		child.parent = this;
		children.add(child);
	}

	public FMLModelFactory getFactory() {
		return analyser.getFactory();
	}

	public FMLSemanticsAnalyzer getAnalyser() {
		return analyser;
	}

	public TypeFactory getTypeFactory() {
		return analyser.getTypeFactory();
	}

	/*public N getASTNode() {
		return astNode;
	}*/

	public FMLObjectNode<?, ?> getParent() {
		return parent;
	}

	public List<FMLObjectNode<?, ?>> getChildren() {
		return children;
	}

	public abstract T buildFMLObjectFromAST(N astNode);

	public abstract FMLObjectNode<N, T> deserialize();

	public void finalizeDeserialization() {
		// Override when required
	}

	@Override
	public T getFMLObject() {
		return fmlObject;
	}

	protected void handleToken(Token token) {

		// System.out.println("Receiving Token " + token.getLine() + ":" + token.getPos() + ":" + token.getText() + " tokenEnd=" + tokenEnd
		// + " endPosition=" + endPosition);

		RawSourcePosition tokenStart = getRawSource().makePositionBeforeChar(token.getLine(), token.getPos());
		RawSourcePosition tokenEnd = getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() + token.getText().length());

		if (startPosition == null || tokenStart.compareTo(startPosition) < 0) {
			startPosition = tokenStart;
			parsedFragment = null;
		}
		if (endPosition == null || tokenEnd.compareTo(endPosition) > 0) {
			endPosition = tokenEnd;
			parsedFragment = null;
		}

		if (getParent() != null) {
			getParent().handleToken(token);
		}
	}

	/**
	 * Return original version of last serialized raw source, FOR THE ENTIRE compilation unit
	 * 
	 * @return
	 */
	public RawSource getRawSource() {
		return analyser.getRawSource();
	}

	/**
	 * Return starting position of RawSource, where underlying model object is textually serialized, inclusive
	 * 
	 * @return
	 */
	public RawSourcePosition getStartPosition() {
		return startPosition;
	}

	/**
	 * Return end position of RawSource, where underlying model object is textually serialized, inclusive
	 * 
	 * @return
	 */
	public RawSourcePosition getEndPosition() {
		return endPosition;
	}

	/**
	 * Return fragment representing underlying FMLObject as a String in FML language, as it was last parsed
	 * 
	 * @return
	 */
	public RawSourceFragment getLastParsedFragment() {
		if (parsedFragment == null && getStartPosition() != null && getEndPosition() != null) {
			parsedFragment = getRawSource().makeFragment(getStartPosition(), getEndPosition());
		}
		return parsedFragment;
	}

	@Override
	public String getFMLRepresentation(PrettyPrintContext context) {
		// TODO: implement a cache !!!!
		return updateFMLRepresentation(context);
	}

	/**
	 * Computes and return a String representing FML representation as the merge of:
	 * <ul>
	 * <li>Last parsed version (as it was in the original source file)</li>
	 * <li>Eventual model modifications</li> </u>
	 * 
	 * @return
	 */
	public abstract String updateFMLRepresentation(PrettyPrintContext context);

	/**
	 * Build and return a new pretty-print context
	 * 
	 * @return
	 */
	@Override
	public PrettyPrintContext makePrettyPrintContext() {
		return new DefaultPrettyPrintContext(0);
	}

	public static abstract class PrettyPrintableContents {
		public PrettyPrintableContents(String prelude, String postlude, PrettyPrintContext context) {
			super();
			this.prelude = prelude;
			this.postlude = postlude;
			this.context = context;
		}

		String prelude;
		String postlude;
		PrettyPrintContext context;
	}

	public static class ChildContents extends PrettyPrintableContents {
		public ChildContents(String prelude, FMLPrettyPrintable contents, String postlude, PrettyPrintContext context) {
			super(prelude, postlude, context);
			this.contents = contents;
		}

		FMLPrettyPrintable contents;
		FMLObjectNode<?, ?> childNode;
	}

	public static class StaticContents extends PrettyPrintableContents {
		public StaticContents(String contents, PrettyPrintContext context) {
			super("", "", context);
			this.contents = contents;
		}

		String contents;
	}

	protected abstract List<PrettyPrintableContents> preparePrettyPrint(PrettyPrintContext context);

	@Override
	public final String getNormalizedFMLRepresentation(PrettyPrintContext context) {
		List<PrettyPrintableContents> childrenObjects = preparePrettyPrint(context);
		StringBuffer sb = new StringBuffer();
		for (PrettyPrintableContents child : childrenObjects) {
			if (child instanceof ChildContents) {
				FMLObjectNode<?, ?> childNode = getObjectNode(((ChildContents) child).contents);
				if (childNode == null) {
					childNode = makeObjectNode(((ChildContents) child).contents);
					addToChildren(childNode);
				}
				sb.append(child.prelude + context.getIndentation() + childNode.getNormalizedFMLRepresentation(child.context)
						+ child.postlude);
			}
			else if (child instanceof StaticContents) {
				sb.append(child.prelude + context.getIndentation() + ((StaticContents) child).contents + child.postlude);
			}
		}
		return sb.toString();
	}

	protected String updatePrettyPrintForChildren(PrettyPrintContext context) {

		boolean debug = (this instanceof FMLCompilationUnitNode);

		List<PrettyPrintableContents> childrenObjects = preparePrettyPrint(context);

		if (childrenObjects.size() == 0) {
			return getLastParsedFragment().getRawText();
		}

		Map<PrettyPrintableContents, String> updatedChildRepresentations = new HashMap<>();

		int insertionPoint = 0;

		for (PrettyPrintableContents childObject : childrenObjects) {
			if (childObject instanceof ChildContents) {
				FMLObjectNode<?, ?> childNode = getObjectNode(((ChildContents) childObject).contents);
				if (childNode == null) {
					childNode = makeObjectNode(((ChildContents) childObject).contents);
					addToChildren(childNode, insertionPoint);
				}
				((ChildContents) childObject).childNode = childNode;
				updatedChildRepresentations.put(childObject, childNode.updateFMLRepresentation(context));
				insertionPoint = getChildren().indexOf(childNode) + 1;
			}
			else if (childObject instanceof StaticContents) {
				updatedChildRepresentations.put(childObject, ((StaticContents) childObject).contents);
			}
		}

		if (debug) {
			System.out.println("-------------------------> START Pretty-Print for " + getClass().getSimpleName());
			System.out.println("last parsed: [" + getLastParsedFragment() + "]");
		}

		RawSourcePosition current = getStartPosition();
		// int currentLine = getStartLine();
		// int currentChar = getStartChar();
		StringBuffer sb = new StringBuffer();

		/*if (debug) {
			System.out.println("currentLine=" + currentLine + " currentChar=" + currentChar);
		}*/

		for (PrettyPrintableContents childObject : childrenObjects) {
			if (childObject instanceof ChildContents) {
				FMLObjectNode<?, ?> childNode = ((ChildContents) childObject).childNode;
				// System.out.println(
				// "> " + childNode.getClass().getSimpleName() + " from " + childNode.getStartLine() + ":" + childNode.getStartChar()
				// + "-" + childNode.getEndLine() + ":" + childNode.getEndChar() + " for " + childNode.getFMLObject());
				RawSourcePosition toPosition = childNode.getStartPosition();
				/*int toLine = childNode.getStartLine();
				int toChar = childNode.getStartChar();
				if (toChar == 1) {
					toLine--;
					toChar = getRawSource().get(toLine - 1).length();
				}*/

				RawSourceFragment prelude = getRawSource().makeFragment(current, toPosition);

				// RawSourceFragment prelude = extract(currentLine, currentChar, toLine, toChar);
				if (debug) {
					System.out.println("Before handling " + childNode.getFMLObject() + " / Adding " + prelude + " value ["
							+ prelude.getRawText() + "]");
				}
				sb.append(prelude.getRawText());

				String updatedChildrenPP = childNode.updateFMLRepresentation(context.derive());
				if (debug) {
					System.out.println("Now consider " + getFMLObject() + " " + childNode.getLastParsedFragment() + " value ["
							+ updatedChildrenPP + "]");
				}
				sb.append(updatedChildrenPP);

				current = childNode.getEndPosition();

				/*currentLine = childNode.getEndLine();
				currentChar = childNode.getEndChar() + 1;
				if (currentChar >= getRawSource().get(currentLine - 1).length()) {
					currentLine++;
					currentChar = 1;
				}*/
				if (debug) {
					System.out.println("AFTER adding children current=" + current);
				}
			}
		}

		System.out.println("current=" + current);
		System.out.println("getEndPosition()=" + getEndPosition());
		RawSourceFragment postlude = getRawSource().makeFragment(current, getEndPosition());

		if (debug) {
			System.out.println("At the end for " + getFMLObject() + " / Adding remaining " + postlude + ") value [" + postlude + "]");
		}
		sb.append(postlude.getRawText());

		if (debug) {
			System.out.println("<------------------------> DONE Pretty-Print for " + getClass().getSimpleName());
			System.out.println("RESULT: [" + sb.toString() + "]");
		}
		return sb.toString();
	}

	protected <O extends FMLPrettyPrintable> FMLObjectNode<?, O> makeObjectNode(O object) {
		if (object instanceof FlexoConcept) {
			return (FMLObjectNode<?, O>) new FlexoConceptNode((FlexoConcept) object, getAnalyser());
		}
		System.err.println("Not supported: " + object);
		return null;
	}

	protected <O extends FMLPrettyPrintable> FMLObjectNode<?, O> getObjectNode(O object) {
		for (FMLObjectNode<?, ?> objectNode : getChildren()) {
			if (objectNode.getFMLObject() == object) {
				return (FMLObjectNode<?, O>) objectNode;
			}
		}
		return null;
	}

	public List<String> makeFullQualifiedIdentifierList(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifierList(identifier, additionalIdentifiers);
	}

	public String makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
	}

}
