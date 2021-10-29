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

import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ANamespaceDecl;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *   namespace_decl = kw_namespace [string_literal]:lit_string kw_as [ns_id]:identifier semi;
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class NamespaceDeclarationNode extends FMLObjectNode<ANamespaceDecl, NamespaceDeclaration, FMLCompilationUnitSemanticsAnalyzer> {

	public NamespaceDeclarationNode(ANamespaceDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public NamespaceDeclarationNode(NamespaceDeclaration importDeclaration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(importDeclaration, analyzer);
	}

	@Override
	public NamespaceDeclaration buildModelObjectFromAST(ANamespaceDecl astNode) {
		NamespaceDeclaration returned = getFactory().newNamespaceDeclaration();
		returned.setValue(astNode.getStringLiteral().getText().substring(1, astNode.getStringLiteral().getText().length() - 1));
		returned.setAbbrev(getText(astNode.getNsId()));
		return returned;
	}

	@Override
	public NamespaceDeclarationNode deserialize() {
		if (getParent() instanceof FMLCompilationUnitNode) {
			((FMLCompilationUnitNode) getParent()).getModelObject().addToNamespaces(getModelObject());
		}

		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", "namespace", SPACE), getNamespaceFragment());
		append(dynamicContents("\"", () -> getModelObject().getValue(), "\""), getStringLiteralFragment());
		append(staticContents(SPACE, "as", SPACE), getAsFragment());
		append(dynamicContents(() -> getModelObject().getAbbrev()), getNsIdFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private RawSourceFragment getNamespaceFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwNamespace());
		}
		return null;
	}

	private RawSourceFragment getStringLiteralFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getStringLiteral());
		}
		return null;
	}

	private RawSourceFragment getNsIdFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getNsId());
		}
		return null;
	}

	private RawSourceFragment getAsFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwAs());
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
