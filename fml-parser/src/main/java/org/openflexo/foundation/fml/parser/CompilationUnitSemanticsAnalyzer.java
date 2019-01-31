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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.NamedJavaImportNode;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.foundation.fml.parser.node.AJavaImportImportDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamedJavaImportImportDeclaration;
import org.openflexo.foundation.fml.parser.node.Start;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public abstract class CompilationUnitSemanticsAnalyzer extends FMLCoreSemanticsAnalyzer {

	private FMLCompilationUnitNode compilationUnitNode;

	public CompilationUnitSemanticsAnalyzer(FMLModelFactory factory, Start tree, List<String> rawSource) {
		super(factory, tree, rawSource);
	}

	public FMLCompilationUnit getCompilationUnit() {
		return compilationUnitNode.getFMLObject();
	}

	public FMLCompilationUnitNode getCompilationUnitNode() {
		return compilationUnitNode;
	}

	@Override
	protected final void finalizeDeserialization() {
		finalizeDeserialization(compilationUnitNode);
	}

	@Override
	public void inAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.inAFmlCompilationUnit(node);
		push(compilationUnitNode = new FMLCompilationUnitNode(node, (FMLSemanticsAnalyzer) this));
	}

	@Override
	public void outAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.outAFmlCompilationUnit(node);
		pop();
	}

	@Override
	public void inAJavaImportImportDeclaration(AJavaImportImportDeclaration node) {
		super.inAJavaImportImportDeclaration(node);
		push(new JavaImportNode(node, (FMLSemanticsAnalyzer) this));
	}

	@Override
	public void outAJavaImportImportDeclaration(AJavaImportImportDeclaration node) {
		super.outAJavaImportImportDeclaration(node);
		pop();
	}

	@Override
	public void inANamedJavaImportImportDeclaration(ANamedJavaImportImportDeclaration node) {
		super.inANamedJavaImportImportDeclaration(node);
		push(new NamedJavaImportNode(node, (FMLSemanticsAnalyzer) this));
	}

	@Override
	public void outANamedJavaImportImportDeclaration(ANamedJavaImportImportDeclaration node) {
		super.outANamedJavaImportImportDeclaration(node);
		pop();
	}
}
