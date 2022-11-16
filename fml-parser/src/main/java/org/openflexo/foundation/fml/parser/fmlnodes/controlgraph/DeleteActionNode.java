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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ADeleteActionFmlActionExp;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class DeleteActionNode extends ControlGraphNode<ADeleteActionFmlActionExp, DeleteAction<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeleteActionNode.class.getPackage().getName());

	public DeleteActionNode(ADeleteActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public DeleteActionNode(DeleteAction<?> action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DeleteAction<?> buildModelObjectFromAST(ADeleteActionFmlActionExp astNode) {
		DeleteAction<?> returned = getFactory().newDeleteAction();

		DataBinding objectToDelete = ExpressionFactory.makeDataBinding(getASTNode().getExpression(), returned, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);
		returned.setObject(objectToDelete);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", "delete", SPACE), getDeleteFragment());
		// append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> getModelObject().getObject().toString()), getExpressionFragment());
		// append(staticContents(")"), getRParFragment());
		append(staticContents(";"), getSemiFragment());

	}

	protected RawSourceFragment getDeleteFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwDelete());
		}
		return null;
	}

	/*protected RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}*/

	protected RawSourceFragment getExpressionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

	/*protected RawSourceFragment getRParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRPar());
		}
		return null;
	}*/

}
