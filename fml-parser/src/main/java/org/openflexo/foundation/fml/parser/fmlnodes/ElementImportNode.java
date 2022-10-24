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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.URIExpressionFactory;
import org.openflexo.foundation.fml.parser.node.ANamedUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.AObjectInResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.AResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.AUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.PImportDecl;
import org.openflexo.foundation.fml.parser.node.PReferenceByUri;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.toolbox.StringUtils;

/**
 * @author sylvain
 * 
 */

// AUriImportImportDecl
// ANamedUriImportImportDecl
public class ElementImportNode extends FMLObjectNode<PImportDecl, ElementImportDeclaration, FMLCompilationUnitSemanticsAnalyzer> {

	public ElementImportNode(PImportDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
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
	public ElementImportDeclaration buildModelObjectFromAST(PImportDecl astNode) {
		ElementImportDeclaration returned = getFactory().newElementImportDeclaration();
		PReferenceByUri ref = null;
		if (astNode instanceof AUriImportImportDecl) {
			ref = ((AUriImportImportDecl) astNode).getObject();
		}
		if (astNode instanceof ANamedUriImportImportDecl) {
			ref = ((ANamedUriImportImportDecl) astNode).getObject();
			returned.setAbbrev(getText(((ANamedUriImportImportDecl) astNode).getName()));
		}
		if (ref instanceof AObjectInResourceReferenceByUri) {
			DataBinding<String> resourceReference = URIExpressionFactory.makeDataBinding(
					((AObjectInResourceReferenceByUri) ref).getResource(), returned, BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer(),
					this);
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
		append(staticContents("["), getLBktFragment());
		append(dynamicContents(() -> getModelObject().getResourceReference().toString()), getResourceReferenceFragment());
		when(() -> isObjectReference()).thenAppend(staticContents(":"), getColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getObjectReference().toString()), getObjectReferenceFragment());
		append(staticContents("]"), getRBktFragment());
		when(() -> isNamedImport()).thenAppend(staticContents(SPACE, "as", SPACE), getAsFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getAbbrev()), getAbbrevFragment());
		append(staticContents(";"), getSemiFragment());
	}

	private boolean isNamedImport() {
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return true;
		}
		if (getModelObject() != null && StringUtils.isNotEmpty(getModelObject().getAbbrev())) {
			return true;
		}
		return false;
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
		if (getASTNode() instanceof AUriImportImportDecl) {
			return getFragment(((AUriImportImportDecl) getASTNode()).getKwImport());
		}
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return getFragment(((ANamedUriImportImportDecl) getASTNode()).getKwImport());
		}
		return null;
	}

	private PReferenceByUri getReference() {
		if (getASTNode() instanceof AUriImportImportDecl) {
			return ((AUriImportImportDecl) getASTNode()).getObject();
		}
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return ((ANamedUriImportImportDecl) getASTNode()).getObject();
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
		if (getASTNode() instanceof AUriImportImportDecl) {
			return getFragment(((AUriImportImportDecl) getASTNode()).getSemi());
		}
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return getFragment(((ANamedUriImportImportDecl) getASTNode()).getSemi());
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

	private RawSourceFragment getAsFragment() {
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return getFragment(((ANamedUriImportImportDecl) getASTNode()).getKwAs());
		}
		return null;
	}

	private RawSourceFragment getAbbrevFragment() {
		if (getASTNode() instanceof ANamedUriImportImportDecl) {
			return getFragment(((ANamedUriImportImportDecl) getASTNode()).getName());
		}
		return null;
	}

}
