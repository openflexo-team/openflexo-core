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

import java.util.List;
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
import org.openflexo.foundation.fml.parser.fmlnodes.AbstractPropertyNode;
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
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.PrettyPrintContext;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.ChainedCollection;

/**
 * Maintains consistency between the model (represented by an {@link FMLObject}) and source code represented in FML language
 * 
 * Works
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectNode<N extends Node, T extends FMLPrettyPrintable> extends P2PPNode<N, T>
		implements FMLPrettyPrintDelegate<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLObjectNode.class.getPackage().getName());

	private final FMLSemanticsAnalyzer analyser;

	public FMLObjectNode(N astNode, FMLSemanticsAnalyzer analyser) {
		super(null, astNode);
		this.analyser = analyser;

		fmlObject = buildFMLObjectFromAST(astNode);
		fmlObject.setPrettyPrintDelegate(this);
		fmlObject.initializeDeserialization(getFactory());
	}

	public FMLObjectNode(T aFMLObject, FMLSemanticsAnalyzer analyser) {
		super(aFMLObject, null);
		this.analyser = analyser;

		fmlObject.setPrettyPrintDelegate(this);
		preparePrettyPrint(false);
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

	// Make this method visible
	@Override
	public void addToChildren(P2PPNode<?, ?> child) {
		super.addToChildren(child);
	}

	// Make this method visible
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
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

		if (getParent() instanceof FMLObjectNode) {
			((FMLObjectNode<?, ?>) getParent()).handleToken(token);
		}
	}

	/**
	 * Return original version of last serialized raw source, FOR THE ENTIRE compilation unit
	 * 
	 * @return
	 */
	@Override
	public RawSource getRawSource() {
		return analyser.getRawSource();
	}

	@Override
	public String getFMLRepresentation(PrettyPrintContext context) {
		return getTextualRepresentation(context);
	}

	@Override
	public String getNormalizedFMLRepresentation(PrettyPrintContext context) {
		return getNormalizedTextualRepresentation(context);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <C> P2PPNode<?, C> makeObjectNode(C object) {
		if (object instanceof JavaImportDeclaration) {
			return (P2PPNode<?, C>) new JavaImportNode((JavaImportDeclaration) object, getAnalyser());
		}
		if (object instanceof VirtualModel) {
			return (P2PPNode<?, C>) new VirtualModelNode((VirtualModel) object, getAnalyser());
		}
		if (object instanceof FlexoConcept) {
			return (P2PPNode<?, C>) new FlexoConceptNode((FlexoConcept) object, getAnalyser());
		}
		if (object instanceof PrimitiveRole) {
			return (P2PPNode<?, C>) new PrimitiveRoleNode((PrimitiveRole) object, getAnalyser());
		}
		if (object instanceof JavaRole) {
			return (P2PPNode<?, C>) new JavaRoleNode((JavaRole) object, getAnalyser());
		}
		if (object instanceof AbstractProperty) {
			return (P2PPNode<?, C>) new AbstractPropertyNode((AbstractProperty) object, getAnalyser());
		}
		System.err.println("Not supported: " + object);
		Thread.dumpStack();
		return null;
	}

	/**
	 * Return fragment matching supplied node in AST
	 * 
	 * @param token
	 * @return
	 */
	// @Override
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
	// @Override
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

}
