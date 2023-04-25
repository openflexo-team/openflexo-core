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

import java.lang.reflect.Type;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.TypeDeclaration;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AFmlTypeDeclType;
import org.openflexo.foundation.fml.parser.node.ANormalTypeDeclType;
import org.openflexo.foundation.fml.parser.node.ATypeDecl;
import org.openflexo.foundation.fml.parser.node.PTypeDeclType;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class TypeDeclarationNode extends FMLObjectNode<ATypeDecl, TypeDeclaration, FMLCompilationUnitSemanticsAnalyzer> {

	public TypeDeclarationNode(ATypeDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public TypeDeclarationNode(TypeDeclaration typeDeclaration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(typeDeclaration, analyzer);
	}

	@Override
	public TypeDeclarationNode deserialize() {
		if (getParent() instanceof FMLCompilationUnitNode) {
			((FMLCompilationUnitNode) getParent()).getModelObject().addToTypeDeclarations(getModelObject());
		}

		return this;
	}

	@Override
	public TypeDeclaration buildModelObjectFromAST(ATypeDecl astNode) {
		TypeDeclaration returned = getFactory().newTypeDeclaration();
		returned.setAbbrev(astNode.getName().getText());

		Type type = null;
		PTypeDeclType referencedType = astNode.getType();
		if (referencedType instanceof ANormalTypeDeclType) {
			type = TypeFactory.makeType(((ANormalTypeDeclType) referencedType).getReferenceType(), getSemanticsAnalyzer().getTypingSpace());
		}
		else if (referencedType instanceof AFmlTypeDeclType) {
			type = TypeFactory.makeType((((AFmlTypeDeclType) referencedType).getTechnologySpecificType()),
					getSemanticsAnalyzer().getTypingSpace());
		}

		System.out.println("Found type:" + TypeUtils.fullQualifiedRepresentation(type));
		returned.setReferencedType(type);
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", FMLKeywords.Type.getKeyword(), SPACE), getTypeKeywordFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getReferencedType()), SPACE), getTypeFragment());
		append(staticContents("", FMLKeywords.As.getKeyword(), SPACE), getAsFragment());
		append(dynamicContents(() -> getModelObject().getAbbrev()), getAbbrevFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private RawSourceFragment getTypeKeywordFragment() {
		return getFragment(getASTNode().getKwType());
	}

	private RawSourceFragment getTypeFragment() {
		return getFragment(getASTNode().getType());
	}

	private RawSourceFragment getSemiFragment() {
		return getFragment(getASTNode().getSemi());
	}

	private RawSourceFragment getAsFragment() {
		return getFragment(getASTNode().getKwAs());
	}

	private RawSourceFragment getAbbrevFragment() {
		return getFragment(getASTNode().getName());
	}

}
