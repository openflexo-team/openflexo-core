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

package org.openflexo.foundation.fml.parser.fmlnodes;

import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ANamedJavaImportImportDeclaration;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class NamedJavaImportNode extends AbstractJavaImportNode<ANamedJavaImportImportDecl> {

	public NamedJavaImportNode(ANamedJavaImportImportDeclaration astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public NamedJavaImportNode(JavaImportDeclaration importDeclaration, MainSemanticsAnalyzer analyser) {
		super(importDeclaration, analyser);
	}

	@Override
	public JavaImportDeclaration buildModelObjectFromAST(ANamedJavaImportImportDecl astNode) {
		JavaImportDeclaration returned = super.buildModelObjectFromAST(astNode);
		returned.setFullQualifiedClassName(makeFullQualifiedIdentifier((ACompositeIdent) astNode.getIdentifier()));
		returned.setAbbrev(astNode.getName().getText());
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", "import", SPACE), getImportFragment());
		append(dynamicContents(() -> getModelObject().getFullQualifiedClassName()), getFullQualifiedFragment());
		append(staticContents(SPACE, "as", SPACE), getAsFragment());
		append(dynamicContents(() -> getModelObject().getAbbrev()), getNameFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private RawSourceFragment getImportFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwImport());
		}
		return null;
	}

	private RawSourceFragment getFullQualifiedFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getIdentifier());
		}
		return null;
	}

	private RawSourceFragment getAsFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwAs());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getName());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

}
