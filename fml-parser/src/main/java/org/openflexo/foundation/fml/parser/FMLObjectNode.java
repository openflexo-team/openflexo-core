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

	private N astNode;
	private final FMLSemanticsAnalyzer analyser;
	private T fmlObject;

	private FMLObjectNode<?, ?> parent;
	private ArrayList<FMLObjectNode<?, ?>> children = new ArrayList<>();

	private int startLine = -1, startChar = -1;
	private int endLine = -1, endChar = -1;

	public FMLObjectNode(N astNode, FMLSemanticsAnalyzer analyser) {
		this.astNode = astNode;
		this.analyser = analyser;
		fmlObject = buildFMLObjectFromAST();
		fmlObject.setPrettyPrintDelegate(this);
		fmlObject.initializeDeserialization(getFactory());
		/*if (!analyser.fmlNodes.isEmpty()) {
			parent = analyser.fmlNodes.peek();
			// System.out.println("Parent " + parent.getClass().getSimpleName() + Integer.toHexString(parent.hashCode()) + " > "
			// + getClass().getSimpleName() + Integer.toHexString(hashCode()));
			parent.children.add(this);
		}*/
	}

	public FMLObjectNode(T fmlObject, FMLSemanticsAnalyzer analyser) {
		this.analyser = analyser;
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

	public N getASTNode() {
		return astNode;
	}

	public FMLObjectNode<?, ?> getParent() {
		return parent;
	}

	public List<FMLObjectNode<?, ?>> getChildren() {
		return children;
	}

	public abstract T buildFMLObjectFromAST();

	public abstract FMLObjectNode<N, T> deserialize();

	public void finalizeDeserialization() {
		// Override when required
	}

	@Override
	public T getFMLObject() {
		return fmlObject;
	}

	protected void handleToken(Token token) {
		if (startLine == -1 || startLine > token.getLine()) {
			startLine = token.getLine();
			startChar = -1;
			if (startChar == -1 || startChar > token.getPos()) {
				startChar = token.getPos();
			}
		}
		if (endLine == -1 || endLine < token.getLine()) {
			endLine = token.getLine();
			endChar = -1;
			if (endChar == -1 || endChar < token.getPos()) {
				endChar = token.getPos();
			}
		}
		if (getParent() != null) {
			getParent().handleToken(token);
		}
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartChar() {
		return startChar;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndChar() {
		return endChar;
	}

	public List<String> getRawSource() {
		return analyser.getRawSource();
	}

	/**
	 * Build and return a String representing underlying FMLObject as a String in FML language, as it was last parsed
	 * 
	 * @return
	 */
	public String getLastParsed() {
		if (getStartLine() > -1 && getStartChar() > -1 && getEndLine() > -1 && getEndChar() > -1 && getStartLine() <= getEndLine()) {
			if (getStartLine() == getEndLine()) {
				// All in one line
				return getRawSource().get(getStartLine() - 1).substring(getStartChar() - 1, getEndChar() - 1);
			}
			StringBuffer sb = new StringBuffer();
			for (int i = getStartLine(); i <= getEndLine(); i++) {
				if (i == getStartLine()) {
					// First line
					sb.append(getRawSource().get(i - 1).substring(getStartChar() - 1) + "\n");
				}
				else if (i == getEndLine()) {
					// Last line
					// try {
					sb.append(getRawSource().get(i - 1).substring(0, getEndChar() - 1));
					/*} catch (StringIndexOutOfBoundsException e) {
						System.out.println("Bizarre, pour " + getClass().getSimpleName() + " from " + getStartLine() + ":" + getStartChar()
								+ " to " + getEndLine() + ":" + getEndChar());
						System.out.println("String = [" + getRawSource().get(i - 1) + "]");
						System.out.println("Je cherche a extraire 0-" + (getEndChar() - 1));
						sb.append("ERROR!");
					}*/
				}
				else {
					sb.append(getRawSource().get(i - 1) + "\n");
				}
			}
			return sb.toString();
		}
		return null;
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
		return new DefaultPrettyPrintContext();
	}

	protected String buildFMLRepresentation(List<FMLPrettyPrintable> childrenObjects, PrettyPrintContext context) {

		Map<FMLObjectNode<?, ?>, String> updatedChildRepresentations = new HashMap<>();

		int insertionPoint = 0;

		for (FMLPrettyPrintable childObject : childrenObjects) {
			FMLObjectNode<?, ?> childNode = getObjectNode(childObject);
			if (childNode == null) {
				childNode = makeObjectNode(childObject);
				addToChildren(childNode, insertionPoint);
			}
			updatedChildRepresentations.put(childNode, childNode.updateFMLRepresentation(context));
			insertionPoint = getChildren().indexOf(childNode) + 1;
		}

		System.out.println("------------------------->  OK on est la dans " + getClass().getSimpleName() + " avec");
		// System.out.println(getLastParsed());

		for (FMLObjectNode<?, ?> childNode : getChildren()) {
			System.out.println(
					"> " + childNode.getClass().getSimpleName() + " from " + childNode.getStartLine() + ":" + childNode.getStartChar() + "-"
							+ childNode.getEndLine() + ":" + childNode.getEndChar() + " for " + childNode.getFMLObject());

		}

		return getNormalizedFMLRepresentation(context);
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
