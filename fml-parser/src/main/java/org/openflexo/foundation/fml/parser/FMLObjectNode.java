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
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.JavaRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.foundation.fml.parser.RawSource.RawSourcePosition;
import org.openflexo.foundation.fml.parser.fmlnodes.AbstractPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.PrimitiveRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.parser.node.APrivateVisibility;
import org.openflexo.foundation.fml.parser.node.AProtectedVisibility;
import org.openflexo.foundation.fml.parser.node.APublicVisibility;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.PVisibility;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.toolbox.ChainedCollection;

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

	private N astNode;
	private final FMLSemanticsAnalyzer analyser;
	private T fmlObject;

	private FMLObjectNode<?, ?> parent;
	private ArrayList<FMLObjectNode<?, ?>> children = new ArrayList<>();

	private RawSourcePosition startPosition;
	private RawSourcePosition endPosition;
	private RawSourceFragment parsedFragment;

	// private DerivedRawSource derivedRawSource;

	private List<PrettyPrintableContents<?>> ppContents = new ArrayList<>();

	public FMLObjectNode(N astNode, FMLSemanticsAnalyzer analyser) {
		this.astNode = astNode;
		this.analyser = analyser;

		fmlObject = buildFMLObjectFromAST(astNode);
		fmlObject.setPrettyPrintDelegate(this);
		fmlObject.initializeDeserialization(getFactory());
	}

	public FMLObjectNode(T aFMLObject, FMLSemanticsAnalyzer analyser) {
		this.analyser = analyser;
		this.fmlObject = aFMLObject;
		fmlObject.setPrettyPrintDelegate(this);
		prepareNormalizedPrettyPrint();
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

	public N getASTNode() {
		return astNode;
	}

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

	/**
	 * Build and return a new pretty-print context
	 * 
	 * @return
	 */
	@Override
	public PrettyPrintContext makePrettyPrintContext() {
		return new DefaultPrettyPrintContext(0);
	}

	protected void preparePrettyPrint() {
		defaultInsertionPoint = getStartPosition();
	}

	protected void prepareNormalizedPrettyPrint() {
	}

	@Override
	public final String getNormalizedFMLRepresentation(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		for (PrettyPrintableContents<?> child : ppContents) {
			sb.append(child.getNormalizedPrettyPrint(context));
		}
		// System.out.println("On indente pour indentation=[" + context.getResultingIndentation() + "]");
		// System.out.println("Ce qu'on indente: " + sb.toString());
		// System.out.println("On retourne: " + context.indent(sb.toString()));
		return context.indent(sb.toString());
	}

	private RawSourcePosition defaultInsertionPoint;

	public RawSourcePosition getDefaultInsertionPoint() {
		return defaultInsertionPoint;
	}

	/**
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param staticContents
	 *            value to append
	 * @param fragment
	 */
	public void appendStaticContents(String staticContents, RawSourceFragment fragment) {
		StaticContents<?> newContents = new StaticContents<>(null, staticContents, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link StaticContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param staticContents
	 *            value to append
	 * @param fragment
	 */
	public void appendStaticContents(String staticContents) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		StaticContents<?> newContents = new StaticContents<>(null, staticContents, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 * @param fragment
	 */
	public void appendStaticContents(String prelude, String staticContents, RawSourceFragment fragment) {
		StaticContents<?> newContents = new StaticContents<>(prelude, staticContents, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 * @param postlude
	 *            postlude to add if normalized pretty-print is to be applied
	 * @param fragment
	 */
	public void appendStaticContents(String prelude, String staticContents, String postlude, RawSourceFragment fragment) {
		StaticContents<?> newContents = new StaticContents<>(prelude, staticContents, postlude, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, RawSourceFragment fragment) {
		DynamicContents<?> newContents = new DynamicContents<>(null, stringRepresentationSupplier, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents<?> newContents = new DynamicContents<>(null, stringRepresentationSupplier, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public void appendDynamicContents(String prelude, Supplier<String> stringRepresentationSupplier, RawSourceFragment fragment) {
		DynamicContents<?> newContents = new DynamicContents<>(prelude, stringRepresentationSupplier, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 */
	public void addDynamicContents(String prelude, Supplier<String> stringRepresentationSupplier) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents<?> newContents = new DynamicContents<>(prelude, stringRepresentationSupplier, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param postlude
	 * @param fragment
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, String postlude, RawSourceFragment fragment) {
		DynamicContents<?> newContents = new DynamicContents<>(null, stringRepresentationSupplier, postlude, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param postlude
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, String postlude) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents<?> newContents = new DynamicContents<>(null, stringRepresentationSupplier, postlude, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link ChildContents} managing pretty-print for supplied childObject<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * @param childObject
	 */
	protected void appendToChildPrettyPrintContents(String prelude, FMLPrettyPrintable childObject, String postude, int indentationLevel) {

		FMLObjectNode<?, ?> childNode = getObjectNode(childObject);
		if (childNode == null) {
			childNode = makeObjectNode(childObject);
			addToChildren(childNode);
		}
		ChildContents<?> newChildContents = new ChildContents<>(prelude, childNode, postude, indentationLevel);
		ppContents.add(newChildContents);
	}

	/**
	 * Called to indicate that supplied childObject must be serialized at this pretty-print level<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * @param childObject
	 */
	protected <T2 extends FMLPrettyPrintable> void appendToChildrenPrettyPrintContents(String prelude,
			Supplier<List<? extends T2>> childrenObjects, String postude, int indentationLevel, Class<T2> childrenType) {

		ChildrenContents<T2> newChildrenContents = new ChildrenContents<>(prelude, childrenObjects, postude, indentationLevel, this,
				childrenType);
		ppContents.add(newChildrenContents);
	}

	@Override
	public String getFMLRepresentation(PrettyPrintContext context) {
		// TODO: implement a cache !!!!

		if (getASTNode() == null) {
			return getNormalizedFMLRepresentation(context);
		}

		DerivedRawSource derivedRawSource = computeFMLRepresentation(context);
		return derivedRawSource.getStringRepresentation();
	}

	/**
	 * Computes and return a String representing FML representation as the merge of:
	 * <ul>
	 * <li>Last parsed version (as it was in the original source file)</li>
	 * <li>Eventual model modifications</li> </u>
	 * 
	 * @return
	 */
	protected DerivedRawSource computeFMLRepresentation(PrettyPrintContext context) {
		boolean debug = (this instanceof FMLCompilationUnitNode);

		DerivedRawSource derivedRawSource = new DerivedRawSource(getLastParsedFragment());

		if (getFMLObject() == null) {
			logger.warning("Unexpected null model object in " + this);
			return derivedRawSource;
		}

		/*List<PrettyPrintableContents> childrenObjects = ppContents;
		
		if (childrenObjects.size() == 0) {
			return getLastParsedFragment().getRawText();
		}*/

		if (debug) {
			System.out.println("-------------------------> START Pretty-Print for " + getClass().getSimpleName());
		}

		for (PrettyPrintableContents<?> prettyPrintableContents : ppContents) {
			prettyPrintableContents.updatePrettyPrint(derivedRawSource, context);
		}

		return derivedRawSource;

		/*Map<PrettyPrintableContents, String> updatedChildRepresentations = new HashMap<>();
		
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
		
		RawSourcePosition current = getStartPosition();
		StringBuffer sb = new StringBuffer();
		
		// if (debug) {
		// System.out.println("currentLine=" + currentLine + " currentChar=" + currentChar);
		// }
		
		for (PrettyPrintableContents childObject : childrenObjects) {
			if (childObject instanceof ChildContents) {
				FMLObjectNode<?, ?> childNode = ((ChildContents) childObject).childNode;
				// System.out.println(
				// "> " + childNode.getClass().getSimpleName() + " from " + childNode.getStartLine() + ":" + childNode.getStartChar()
				// + "-" + childNode.getEndLine() + ":" + childNode.getEndChar() + " for " + childNode.getFMLObject());
				RawSourcePosition toPosition = childNode.getStartPosition();
		
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
		return sb.toString();*/

		// System.out.println("Not handled yet");
		// return getLastParsedFragment().getRawText();
	}

	protected <O extends FMLPrettyPrintable> FMLObjectNode<?, O> makeObjectNode(O object) {
		if (object instanceof JavaImportDeclaration) {
			return (FMLObjectNode<?, O>) new JavaImportNode((JavaImportDeclaration) object, getAnalyser());
		}
		if (object instanceof VirtualModel) {
			return (FMLObjectNode<?, O>) new VirtualModelNode((VirtualModel) object, getAnalyser());
		}
		if (object instanceof FlexoConcept) {
			return (FMLObjectNode<?, O>) new FlexoConceptNode((FlexoConcept) object, getAnalyser());
		}
		if (object instanceof PrimitiveRole) {
			return (FMLObjectNode<?, O>) new PrimitiveRoleNode((PrimitiveRole) object, getAnalyser());
		}
		if (object instanceof JavaRole) {
			return (FMLObjectNode<?, O>) new JavaRoleNode((JavaRole) object, getAnalyser());
		}
		if (object instanceof AbstractProperty) {
			return (FMLObjectNode<?, O>) new AbstractPropertyNode((AbstractProperty) object, getAnalyser());
		}
		System.err.println("Not supported: " + object);
		Thread.dumpStack();
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

	/**
	 * Return position as a cursor BEFORE the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition getPositionBefore(Token token) {
		return getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() - 1);
	}

	/**
	 * Return position as a cursor AFTER the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition getPositionAfter(Token token) {
		return getRawSource().makePositionAfterChar(token.getLine(), token.getPos());
	}

	/**
	 * Return fragment matching supplied node in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node) {
		if (node instanceof Token) {
			Token token = (Token) node;
			return getRawSource().makeFragment(getRawSource().makePositionBeforeChar(token.getLine(), token.getPos()),
					getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() + token.getText().length()));
		}
		else {
			return getAnalyser().getFragmentManager().getFragment(node);
		}
	}

	/**
	 * Return fragment matching supplied nodes in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node, List<? extends Node> otherNodes) {
		ChainedCollection<Node> collection = new ChainedCollection<>();
		collection.add(node);
		collection.add(otherNodes);
		return getAnalyser().getFragmentManager().getFragment(collection);
	}

	public List<String> makeFullQualifiedIdentifierList(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifierList(identifier, additionalIdentifiers);
	}

	public String makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
	}

	protected static final String SPACE = " ";
	protected static final String LINE_SEPARATOR = "\n";

	protected String getVisibilityAsString(Visibility visibility) {
		if (visibility != null) {
			switch (visibility) {
				case Default:
					return "";
				case Public:
					return "public";
				case Protected:
					return "protected";
				case Private:
					return "private";
			}
		}
		return "";
	}

	protected Visibility getVisibility(PVisibility visibility) {
		if (visibility == null) {
			return Visibility.Default;
		}
		else if (visibility instanceof APublicVisibility) {
			return Visibility.Public;
		}
		else if (visibility instanceof AProtectedVisibility) {
			return Visibility.Protected;
		}
		else if (visibility instanceof APrivateVisibility) {
			return Visibility.Private;
		}
		return null;
	}

	/*protected RawSourceFragment getFragment(PVisibility visibility) {
		if (visibility instanceof APrivateVisibility) {
			return getFragment(((APrivateVisibility) visibility).getPrivate());
		}
		if (visibility instanceof AProtectedVisibility) {
			return getFragment(((AProtectedVisibility) visibility).getProtected());
		}
		if (visibility instanceof APublicVisibility) {
			return getFragment(((APublicVisibility) visibility).getPublic());
		}
		return null;
	}*/

	/*protected RawSourceFragment getFragment(PType type) {
		return null;
	}*/

}
