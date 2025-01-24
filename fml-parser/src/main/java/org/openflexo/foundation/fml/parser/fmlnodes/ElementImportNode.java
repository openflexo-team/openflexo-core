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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.URIExpressionFactory;
import org.openflexo.foundation.fml.parser.node.ANamedUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.AObjectInResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.AResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.PReferenceByUri;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ElementImportNode
		extends FMLObjectNode<ANamedUriImportImportDecl, ElementImportDeclaration, FMLCompilationUnitSemanticsAnalyzer> {

	public ElementImportNode(ANamedUriImportImportDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ElementImportNode(ElementImportDeclaration importDeclaration, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(importDeclaration, analyzer);
	}

	@Override
	public ElementImportNode deserialize() {
		if (getParent() instanceof FMLCompilationUnitNode) {
			((FMLCompilationUnitNode) getParent()).getModelObject().addToElementImports(getModelObject());
		}

		return this;
	}

	@Override
	public ElementImportDeclaration buildModelObjectFromAST(ANamedUriImportImportDecl astNode) {
		ElementImportDeclaration returned = getFactory().newElementImportDeclaration();
		PReferenceByUri ref = astNode.getObject();
		returned.setAbbrev(getText(astNode.getName()));
		returned.setDeclaredType(TypeFactory.makeType(astNode.getType(), getSemanticsAnalyzer().getTypingSpace()));

		if (ref instanceof AObjectInResourceReferenceByUri) {
			DataBinding<String> resourceReference = URIExpressionFactory.makeDataBinding(
					((AObjectInResourceReferenceByUri) ref).getResource(), returned, BindingDefinitionType.GET, Object.class,
					getSemanticsAnalyzer(), this);
			returned.setResourceReference(resourceReference);
			DataBinding<String> objectReference = URIExpressionFactory.makeDataBinding(((AObjectInResourceReferenceByUri) ref).getObject(),
					returned, BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer(), this);
			returned.setObjectReference(objectReference);
		}
		if (ref instanceof AResourceReferenceByUri) {
			DataBinding<String> resourceReference = URIExpressionFactory.makeDataBinding(((AResourceReferenceByUri) ref).getResource(),
					returned, BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer(), this);
			returned.setResourceReference(resourceReference);
		}
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", "import", SPACE), getImportFragment());
		append(dynamicContents(() -> serializeType(getTypeToSerialize()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getAbbrev(), SPACE), getAbbrevFragment());
		append(staticContents("", "from", SPACE), getFromFragment());
		append(staticContents("["), getLBktFragment());
		append(dynamicContents(() -> getModelObject().getResourceReference().toString()), getResourceReferenceFragment());
		when(() -> isObjectReference()).thenAppend(staticContents(":"), getColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getObjectReference().toString()), getObjectReferenceFragment());
		append(staticContents("]"), getRBktFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private Type getTypeToSerialize() {
		if (getModelObject() != null) {
			if (getModelObject().getDeclaredType() != null) {
				return getModelObject().getDeclaredType();
			}
			return getModelObject().getAnalyzedType();
		}
		return null;
	}

	private boolean isComputingObjectReference = false;

	private boolean isObjectReference() {
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return true;
		}
		if (isComputingObjectReference) {
			return false;
		}
		isComputingObjectReference = true;
		if (getModelObject() != null && getModelObject().getObjectReference() != null && getModelObject().getObjectReference().isSet()
				&& getModelObject().getObjectReference().isValid()) {
			isComputingObjectReference = false;
			return true;
		}
		isComputingObjectReference = false;
		return false;
	}

	private RawSourceFragment getImportFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwImport());
		}
		return null;
	}

	private PReferenceByUri getReference() {
		if (getASTNode() != null) {
			return getASTNode().getObject();
		}
		return null;
	}

	private RawSourceFragment getResourceReferenceFragment() {
		if (getReference() instanceof AResourceReferenceByUri) {
			return getFragment(((AResourceReferenceByUri) getReference()).getResource());
		}
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return getFragment(((AObjectInResourceReferenceByUri) getReference()).getResource());
		}
		return null;
	}

	private RawSourceFragment getObjectReferenceFragment() {
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return getFragment(((AObjectInResourceReferenceByUri) getReference()).getObject());
		}
		return null;
	}

	private RawSourceFragment getColonFragment() {
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return getFragment(((AObjectInResourceReferenceByUri) getReference()).getColon());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

	private RawSourceFragment getLBktFragment() {
		if (getReference() instanceof AResourceReferenceByUri) {
			return getFragment(((AResourceReferenceByUri) getReference()).getLBkt());
		}
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return getFragment(((AObjectInResourceReferenceByUri) getReference()).getLBkt());
		}
		return null;
	}

	private RawSourceFragment getRBktFragment() {
		if (getReference() instanceof AResourceReferenceByUri) {
			return getFragment(((AResourceReferenceByUri) getReference()).getRBkt());
		}
		if (getReference() instanceof AObjectInResourceReferenceByUri) {
			return getFragment(((AObjectInResourceReferenceByUri) getReference()).getRBkt());
		}
		return null;
	}

	private RawSourceFragment getFromFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwFrom());
		}
		return null;
	}

	private RawSourceFragment getAbbrevFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getName());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

}
