/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnit;
import org.openflexo.foundation.fml.parser.FMLCompilationUnit.Fragment;
import org.openflexo.foundation.fml.parser.FMLPrettyPrintContext;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLSyntaxAnalyzer;
import org.openflexo.foundation.fml.parser.SemanticsException;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.toolbox.StringUtils;

/**
 * Base implementation for internal representation of a node of the FML abstract syntax tree
 * 
 * @author sylvain
 *
 */
public abstract class IRNode<O extends FMLObject, N extends Node> {

	@SuppressWarnings("unused")
	static final Logger logger = Logger.getLogger(IRNode.class.getPackage().getName());

	private O fmlObject;
	private N node;
	private FMLSemanticsAnalyzer semanticsAnalyzer;
	private Fragment fragment;

	private IRNode<?, ?> parent;
	private final List<IRNode<?, ?>> children;

	public IRNode(N node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		this.node = node;
		this.semanticsAnalyzer = semanticsAnalyzer;
		children = new ArrayList<>();
		fragment = retrieveFragment();
		System.out.println("Fragment=" + fragment + StringUtils.LINE_SEPARATOR);
		System.out.println(fragment.getText());
	}

	public final O makeFMLObject() {
		fmlObject = buildFMLObject();
		System.out.println("--------> Created new FMLObject " + fmlObject);
		if (semanticsAnalyzer.getRootNode() != null) {
			System.out.println("################# Hop, on enregistre " + fmlObject + " pour " + node.getClass().getSimpleName());
			semanticsAnalyzer.getRootNode().registerFMLObject(fmlObject, this);
		}
		return fmlObject;
	}

	public FMLSemanticsAnalyzer getSemanticsAnalyzer() {
		return semanticsAnalyzer;
	}

	public FMLSyntaxAnalyzer getSyntaxAnalyzer() {
		return getSemanticsAnalyzer().getSyntaxAnalyzer();
	}

	public FMLCompilationUnit getCompilationUnit() {
		return getSemanticsAnalyzer().getFMLCompilationUnit();
	}

	public IRCompilationUnitNode getRootNode() {
		return getSemanticsAnalyzer().getRootNode();
	}

	public IRNode<?, ?> getParent() {
		return parent;
	}

	public List<IRNode<?, ?>> getChildren() {
		return children;
	}

	public void addToChilren(IRNode<?, ?> child) {
		if (child != null) {
			children.add(child);
			child.parent = this;
		}
		else {
			logger.warning("Cannot add null child");
		}
	}

	public void removeFromChilren(IRNode<?, ?> child) {
		if (child != null) {
			children.remove(child);
			child.parent = null;
		}
		else {
			logger.warning("Cannot remove null child");
		}
	}

	public O getFMLObject() {
		return fmlObject;
	}

	public N getNode() {
		return node;
	}

	private Fragment retrieveFragment() {
		int beginLine = getSyntaxAnalyzer().getBeginLine(getNode());
		int beginCol = getSyntaxAnalyzer().getBeginColumn(getNode());
		int endLine = getSyntaxAnalyzer().getEndLine(getNode());
		int endCol = getSyntaxAnalyzer().getEndColumn(getNode());
		fragment = semanticsAnalyzer.getFMLCompilationUnit().new Fragment(beginLine, beginCol, endLine, endCol);
		System.out.println("On extrait fragment " + fragment + " pour " + getNode());
		return fragment;
	}

	public Fragment getFragment() {
		return fragment;
	}

	abstract O buildFMLObject();

	protected void fireSemanticsException(SemanticsException e) {
		System.err.println("SemanticsException at position " + e.getFragmentInError() + " " + e.getFragmentInError().getText() + " : "
				+ e.getMessage());
	}

	// Voir du cote de GeneratorFormatter pour formatter tout ca
	public abstract String getFMLPrettyPrint(FMLPrettyPrintContext context);

	private String prettyPrint;

	public final String getFMLPrettyPrint() {
		if (prettyPrint == null) {
			prettyPrint = getFMLPrettyPrint(new FMLPrettyPrintContext());
		}
		return prettyPrint;
	}

	public void clearFMLPrettyPrint() {
		prettyPrint = null;
	}

}
