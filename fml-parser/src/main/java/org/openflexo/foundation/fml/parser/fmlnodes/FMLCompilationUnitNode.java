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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * @author sylvain
 * 
 */
public class FMLCompilationUnitNode extends FMLObjectNode<AFmlCompilationUnit, FMLCompilationUnit, MainSemanticsAnalyzer> {

	private RawSourcePosition startPosition;
	private RawSourcePosition endPosition;

	public FMLCompilationUnitNode(AFmlCompilationUnit astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
		startPosition = getRawSource().getStartPosition();
		endPosition = getRawSource().getEndPosition();
	}

	public FMLCompilationUnitNode(FMLCompilationUnit compilationUnit, MainSemanticsAnalyzer analyser) {
		super(compilationUnit, analyser);
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

		append(childrenContents("", "", () -> getModelObject().getJavaImports(), LINE_SEPARATOR, LINE_SEPARATOR + LINE_SEPARATOR,
				Indentation.DoNotIndent, JavaImportDeclaration.class));

		append(childContents("", () -> getModelObject().getVirtualModel(), LINE_SEPARATOR, Indentation.DoNotIndent));

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

}
