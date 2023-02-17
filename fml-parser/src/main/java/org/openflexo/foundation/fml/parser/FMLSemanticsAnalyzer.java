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
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLInstancePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLInstancesListPropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLSimplePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLTypePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.WrappedFMLObjectNode;
import org.openflexo.foundation.fml.parser.node.ACompositeCident;
import org.openflexo.foundation.fml.parser.node.ACompositeCidentAnnotationTag;
import org.openflexo.foundation.fml.parser.node.ACompositeTident;
import org.openflexo.foundation.fml.parser.node.ACompositeTidentAnnotationTag;
import org.openflexo.foundation.fml.parser.node.AConstantCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AInstanceQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.AListInstancesQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.AMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.ANormalCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.ATypeQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAnnotationTag;
import org.openflexo.foundation.fml.parser.node.PCompositeCident;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PCompositeTident;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.TCidentifier;
import org.openflexo.foundation.fml.parser.node.TLidentifier;
import org.openflexo.foundation.fml.parser.node.TUidentifier;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.ChainedCollection;

/**
 * Base class implementing semantics analyzer, based on sablecc FML grammar visitor<br>
 * 
 * A {@link FMLSemanticsAnalyzer} basically manages:
 * <ul>
 * <li>a {@link FMLModelFactory}</li>
 * <li>a {@link FMLBindingFactory}</li>
 * <li>a typing space ({@link AbstractFMLTypingSpace})</li>
 * <li>an eventual {@link FragmentManager}</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public abstract class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	private static final Logger logger = Logger.getLogger(FMLSemanticsAnalyzer.class.getPackage().getName());

	private FMLModelFactory factory;

	// Stack of FMLObjectNode beeing build during semantics analyzing
	protected Stack<ObjectNode<?, ?, ?>> fmlNodes = new Stack<>();

	private Node rootNode;

	// This list of DataBinding is built during semantics analysing process, and is completed with DataBinding which are not valid
	// Since many structural modifications can happen till the end of deserialization, we keep this list to try a final revalidate at the
	// end of semantics analyzing
	private List<DataBinding<?>> invalidBindings = new ArrayList<>();

	public FMLSemanticsAnalyzer(FMLModelFactory factory, Node rootNode) {
		this.factory = factory;
		this.rootNode = rootNode;
	}

	/**
	 * Return {@link FMLCompilationUnit} we are dealing with, if any
	 * 
	 * @return
	 */
	public abstract FMLCompilationUnit getCompilationUnit();

	/**
	 * Return applicable {@link AbstractFMLTypingSpace} in the context of this {@link FMLSemanticsAnalyzer}
	 * 
	 * @return
	 */
	public abstract AbstractFMLTypingSpace getTypingSpace();

	/**
	 * Return applicable {@link FMLBindingFactory} in the context of this {@link FMLSemanticsAnalyzer}
	 * 
	 * @return
	 */
	public abstract FMLBindingFactory getFMLBindingFactory();

	/**
	 * Return applicable {@link FragmentManager} in the context of this {@link FMLSemanticsAnalyzer}, if any<br>
	 * (might be null if fragment management is not applicable to this analyzer)
	 * 
	 * @return
	 */
	public abstract FragmentManager getFragmentManager();

	/**
	 * Return original version of last serialized raw source, FOR THE ENTIRE compilation unit
	 * 
	 * @return
	 */
	public abstract RawSource getRawSource();

	/**
	 * Retrieve (creates when required) a new {@link ObjectNode} for supplied AST node
	 * 
	 * @param <N>
	 *            type of AST node
	 * @param <FMLN>
	 *            type of {@link ObjectNode}
	 * @param astNode
	 *            the AST node we are considering
	 * @param function
	 *            a function returning the {@link ObjectNode} to build from AST node, when required
	 * @return
	 */
	public abstract <N extends Node, FMLN extends ObjectNode<?, ?, ?>> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function);

	/**
	 * Add supplied binding to the list of {@link DataBinding} which are flagged as invalid.
	 * 
	 * This list of {@link DataBinding} is built during semantics analysing process, and is completed with {@link DataBinding} which are not
	 * valid
	 * 
	 * Since many structural modifications can happen till the end of deserialization, we keep this list to try a final revalidate at the
	 * end of semantics analyzing
	 * 
	 * @param binding
	 */
	public void addToInvalidBindings(DataBinding<?> binding) {
		invalidBindings.add(binding);
	}

	/**
	 * Attempt to fix all invalid bindings
	 */
	protected void attemptToFixInvalidBindings() {
		for (DataBinding<?> dataBinding : new ArrayList<>(invalidBindings)) {
			if (dataBinding.revalidate()) {
				logger.info("DataBinding " + dataBinding + " has been finally successfully revalidated at the end of process");
				invalidBindings.remove(dataBinding);
			}
			else {
				logger.warning("DataBinding " + dataBinding + " still invalid at the end of process, reason: "
						+ dataBinding.invalidBindingReason());
			}
		}

	}

	protected void clearInvalidBindings() {
		invalidBindings.clear();
	}

	/**
	 * Called when an issue was found, handled by the adequate FMLSemanticsManager implementation
	 * 
	 * @param errorMessage
	 * @param fragment
	 * @param startPosition
	 */
	public abstract void throwIssue(Object modelObject, String errorMessage, RawSourceFragment fragment, RawSourcePosition startPosition);

	/**
	 * Return a list of all semantics analyzing issues found in the context of this {@link FMLSemanticsAnalyzer}
	 * 
	 * @return
	 */
	public abstract List<SemanticAnalysisIssue> getSemanticAnalysisIssues();

	// Not sure it is still required
	// TODO: check this
	@Deprecated
	public abstract FMLCompilationUnitSemanticsAnalyzer getCompilationUnitAnalyzer();

	public Node getRootNode() {
		return rootNode;
	}

	public final FMLModelFactory getModelFactory() {
		return factory;
	}

	public final void setModelFactory(FMLModelFactory factory) {
		this.factory = factory;
	}

	public final FlexoServiceManager getServiceManager() {
		if (getModelFactory() != null) {
			return getModelFactory().getServiceManager();
		}
		return null;
	}

	protected final void finalizeDeserialization(ObjectNode<?, ?, ?> node) {
		node.finalizeDeserialization();
		for (P2PPNode<?, ?> child : new ArrayList<>(node.getChildren())) {
			finalizeDeserialization((ObjectNode<?, ?, ?>) child);
		}
	}

	protected void push(ObjectNode<?, ?, ?> fmlNode) {
		if (!fmlNodes.isEmpty()) {
			ObjectNode<?, ?, ?> current = fmlNodes.peek();
			current.addToChildren(fmlNode);
		}
		fmlNodes.push(fmlNode);
	}

	protected <N extends ObjectNode<?, ?, ?>> N pop() {
		N builtFMLNode = (N) fmlNodes.pop();
		builtFMLNode.deserialize();
		// builtFMLNode.initializePrettyPrint();
		return builtFMLNode;
	}

	public <N extends ObjectNode<?, ?, ?>> N peek() {
		if (!fmlNodes.isEmpty()) {
			return (N) fmlNodes.peek();
		}
		return null;
	}

	public ObjectNode<?, ?, ?> getCurrentNode() {
		return peek();
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
			return getFragmentManager().retrieveFragment(node);
		}
	}

	/**
	 * Return fragment matching supplied nodes in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node, Node otherNode) {
		return getFragment(node, Collections.singletonList(otherNode));
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
		return getFragmentManager().getFragment(collection);
	}

	public String getText(Node node) {
		return getFragment(node).getRawText();
	}

	public List<String> makeFullQualifiedIdentifierList(List<PIdentifierPrefix> prefixes, TLidentifier identifier) {
		List<String> returned = new ArrayList<>();
		for (PIdentifierPrefix p : prefixes) {
			if (p instanceof AIdentifierPrefix) {
				returned.add(((AIdentifierPrefix) p).getLidentifier().getText());
			}
		}
		returned.add(identifier.getText());
		return returned;
	}

	public String makeFullQualifiedIdentifier(List<PIdentifierPrefix> prefixes, TLidentifier identifier) {
		StringBuffer returned = new StringBuffer();
		for (PIdentifierPrefix p : prefixes) {
			if (p instanceof AIdentifierPrefix) {
				returned.append(((AIdentifierPrefix) p).getLidentifier().getText() + ".");
			}
		}
		returned.append(identifier.getText());
		return returned.toString();
	}

	public String makeFullQualifiedIdentifier(PCompositeIdent compositeIdentifier) {
		if (compositeIdentifier instanceof ANormalCompositeIdent) {
			return makeFullQualifiedIdentifier(((ANormalCompositeIdent) compositeIdentifier).getPrefixes(),
					((ANormalCompositeIdent) compositeIdentifier).getIdentifier());
		}
		if (compositeIdentifier instanceof AConstantCompositeIdent) {
			return makeFullQualifiedIdentifier(((AConstantCompositeIdent) compositeIdentifier).getPrefixes(),
					((AConstantCompositeIdent) compositeIdentifier).getIdentifier());
		}
		return null;
	}

	public String makeFullQualifiedIdentifier(PAnnotationTag annotationTag) {
		if (annotationTag instanceof ACompositeCidentAnnotationTag) {
			return makeFullQualifiedIdentifier(((ACompositeCidentAnnotationTag) annotationTag).getCompositeCident());
		}
		if (annotationTag instanceof ACompositeTidentAnnotationTag) {
			return makeFullQualifiedIdentifier(((ACompositeTidentAnnotationTag) annotationTag).getCompositeTident());
		}
		return null;
	}

	public List<String> makeFullQualifiedIdentifierList(List<PIdentifierPrefix> prefixes, TUidentifier identifier) {
		List<String> returned = new ArrayList<>();
		for (PIdentifierPrefix p : prefixes) {
			if (p instanceof AIdentifierPrefix) {
				returned.add(((AIdentifierPrefix) p).getLidentifier().getText());
			}
		}
		returned.add(identifier.getText());
		return returned;
	}

	public String makeFullQualifiedIdentifier(List<PIdentifierPrefix> prefixes, TUidentifier identifier) {
		StringBuffer returned = new StringBuffer();
		for (PIdentifierPrefix p : prefixes) {
			if (p instanceof AIdentifierPrefix) {
				returned.append(((AIdentifierPrefix) p).getLidentifier().getText() + ".");
			}
		}
		returned.append(identifier.getText());
		return returned.toString();
	}

	public String makeFullQualifiedIdentifier(List<PIdentifierPrefix> prefixes, TCidentifier identifier) {
		StringBuffer returned = new StringBuffer();
		for (PIdentifierPrefix p : prefixes) {
			if (p instanceof AIdentifierPrefix) {
				returned.append(((AIdentifierPrefix) p).getLidentifier().getText() + ".");
			}
		}
		returned.append(identifier.getText());
		return returned.toString();
	}

	public String makeFullQualifiedIdentifier(PCompositeTident compositeIdentifier) {
		if (compositeIdentifier instanceof ACompositeTident) {
			return makeFullQualifiedIdentifier(((ACompositeTident) compositeIdentifier).getPrefixes(),
					((ACompositeTident) compositeIdentifier).getIdentifier());
		}
		return null;
	}

	public String makeFullQualifiedIdentifier(PCompositeCident compositeIdentifier) {
		if (compositeIdentifier instanceof ACompositeCident) {
			return makeFullQualifiedIdentifier(((ACompositeCident) compositeIdentifier).getPrefixes(),
					((ACompositeCident) compositeIdentifier).getIdentifier());
		}
		return null;
	}

	// Hack used to detect that we are not deserializing FML property values but a MatchingCriteria
	// MatchingCriterias must be replaced with MatchCondition as in InitiateMatching
	// TODO fix this hack
	protected boolean insideMatchAction = false;

	@Override
	public void inAMatchActionFmlActionExp(AMatchActionFmlActionExp node) {
		super.inAMatchActionFmlActionExp(node);
		insideMatchAction = true;
	}

	@Override
	public void outAMatchActionFmlActionExp(AMatchActionFmlActionExp node) {
		super.outAMatchActionFmlActionExp(node);
		insideMatchAction = false;
	}

	// Hack used to detect that we are deserializing a new_instance
	// TODO fix this hack
	protected boolean insideNewInstance = false;

	@Override
	public void inASimpleNewInstance(ASimpleNewInstance node) {
		super.inASimpleNewInstance(node);
		insideNewInstance = true;
	}

	@Override
	public void outASimpleNewInstance(ASimpleNewInstance node) {
		super.outASimpleNewInstance(node);
		insideNewInstance = false;
	}

	@Override
	public void inAFullQualifiedNewInstance(AFullQualifiedNewInstance node) {
		super.inAFullQualifiedNewInstance(node);
		insideNewInstance = true;
	}

	@Override
	public void outAFullQualifiedNewInstance(AFullQualifiedNewInstance node) {
		super.outAFullQualifiedNewInstance(node);
		insideNewInstance = false;
	}

	protected boolean handleFMLArgument() {
		return !insideMatchAction && !insideNewInstance;
	}

	@Override
	public final void inASimpleQualifiedArgument(ASimpleQualifiedArgument node) {
		super.inASimpleQualifiedArgument(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new FMLSimplePropertyValueNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public final void outASimpleQualifiedArgument(ASimpleQualifiedArgument node) {
		super.outASimpleQualifiedArgument(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

	@Override
	public void inATypeQualifiedArgument(ATypeQualifiedArgument node) {
		super.inATypeQualifiedArgument(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new FMLTypePropertyValueNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public void outATypeQualifiedArgument(ATypeQualifiedArgument node) {
		super.outATypeQualifiedArgument(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

	@Override
	public final void inAInstanceQualifiedArgument(AInstanceQualifiedArgument node) {
		super.inAInstanceQualifiedArgument(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node,
					n -> new FMLInstancePropertyValueNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public final void outAInstanceQualifiedArgument(AInstanceQualifiedArgument node) {
		super.outAInstanceQualifiedArgument(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

	@Override
	public final void inAListInstancesQualifiedArgument(AListInstancesQualifiedArgument node) {
		super.inAListInstancesQualifiedArgument(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node,
					n -> new FMLInstancesListPropertyValueNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public final void outAListInstancesQualifiedArgument(AListInstancesQualifiedArgument node) {
		super.outAListInstancesQualifiedArgument(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

	@Override
	public final void inASimpleQualifiedInstance(ASimpleQualifiedInstance node) {
		super.inASimpleQualifiedInstance(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new WrappedFMLObjectNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public final void outASimpleQualifiedInstance(ASimpleQualifiedInstance node) {
		super.outASimpleQualifiedInstance(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

	@Override
	public final void inAFullQualifiedQualifiedInstance(AFullQualifiedQualifiedInstance node) {
		super.inAFullQualifiedQualifiedInstance(node);
		if (handleFMLArgument()) {
			// System.out.println("ENTER in " + peek() + " with " + node);
			push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new WrappedFMLObjectNode(n, getCompilationUnitAnalyzer())));
		}
	}

	@Override
	public final void outAFullQualifiedQualifiedInstance(AFullQualifiedQualifiedInstance node) {
		super.outAFullQualifiedQualifiedInstance(node);
		if (handleFMLArgument()) {
			pop();
			// System.out.println("EXIT from " + peek() + " with " + node);
		}
	}

}
