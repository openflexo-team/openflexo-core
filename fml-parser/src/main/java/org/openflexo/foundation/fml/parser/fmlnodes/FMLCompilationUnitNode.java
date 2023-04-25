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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.TypeDeclaration;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.pamela.validation.Validable;

/**
 * @author sylvain
 * 
 */
public class FMLCompilationUnitNode extends FMLObjectNode<AFmlCompilationUnit, FMLCompilationUnit, FMLCompilationUnitSemanticsAnalyzer> {

	private static final Logger logger = Logger.getLogger(FMLCompilationUnitNode.class.getPackage().getName());

	private RawSourcePosition startPosition;
	private RawSourcePosition endPosition;

	public FMLCompilationUnitNode(AFmlCompilationUnit astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
		startPosition = getRawSource().getStartPosition();
		endPosition = getRawSource().getEndPosition();
	}

	public FMLCompilationUnitNode(FMLCompilationUnit compilationUnit, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(compilationUnit, analyzer);
	}

	@Override
	public FMLCompilationUnit buildModelObjectFromAST(AFmlCompilationUnit astNode) {
		return getFactory().newCompilationUnit();
	}

	@Override
	public FMLCompilationUnitNode deserialize() {
		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		append(childrenContents("", "", () -> getModelObject().getNamespaces(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, NamespaceDeclaration.class, "Namespaces"));

		append(childrenContents("", "", () -> getModelObject().getUseDeclarations(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, UseModelSlotDeclaration.class, "UseDeclarations"));

		append(childrenContents("", "", () -> getModelObject().getJavaImports(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, JavaImportDeclaration.class, "JavaImports"));

		append(childrenContents("", "", () -> getModelObject().getElementImports(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, ElementImportDeclaration.class, "ElementImports"));

		append(childrenContents("", "", () -> getModelObject().getTypeDeclarations(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, TypeDeclaration.class, "TypeDeclarations"));

		append(childContents("", () -> getModelObject().getVirtualModel(), "", Indentation.DoNotIndent, "VirtualModel"));

		/*appendToChildrenPrettyPrintContents("", "", () -> getModelObject().getJavaImports(), LINE_SEPARATOR,
				LINE_SEPARATOR + LINE_SEPARATOR, Indentation.DoNotIndent, JavaImportDeclaration.class);
		appendToChildPrettyPrintContents("", () -> getModelObject().getVirtualModel(), LINE_SEPARATOR, Indentation.DoNotIndent);*/
	}

	@Override
	public RawSourcePosition getStartPosition() {
		return startPosition;
	}

	@Override
	public RawSourcePosition getEndPosition() {
		return endPosition;
	}

	private List<SemanticAnalysisIssue> semanticAnalysisIssues = new ArrayList<>();

	@Override
	public List<SemanticAnalysisIssue> getSemanticAnalysisIssues() {
		return semanticAnalysisIssues;
	}

	/*@Override
	public void throwIssue(String errorMessage, RawSourceFragment fragment) {
		SemanticAnalysisIssue issue = new SemanticAnalysisIssue(errorMessage, fragment);
		semanticAnalysisIssues.add(issue);
	}*/

	public void throwIssue(Object modelObject, String errorMessage, RawSourceFragment fragment) {
		if (modelObject instanceof DataBinding) {
			// In this case, DataBinding is not the Validable, try the owner (a Bindable)
			modelObject = ((DataBinding) modelObject).getOwner();
		}
		if (modelObject instanceof Validable) {
			SemanticAnalysisIssue issue = new SemanticAnalysisIssue((Validable) modelObject, errorMessage, fragment);
			semanticAnalysisIssues.add(issue);
		}
		else {
			logger.warning("Semantics issue found in non validable object: " + modelObject + ": " + errorMessage);
			SemanticAnalysisIssue issue = new SemanticAnalysisIssue(null, errorMessage, fragment);
			semanticAnalysisIssues.add(issue);
		}
	}

}
