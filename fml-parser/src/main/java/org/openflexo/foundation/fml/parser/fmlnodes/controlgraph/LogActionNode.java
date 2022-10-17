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
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ALogActionFmlActionExp;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class LogActionNode extends ControlGraphNode<ALogActionFmlActionExp, LogAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LogActionNode.class.getPackage().getName());

	public LogActionNode(ALogActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public LogActionNode(LogAction action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public LogAction buildModelObjectFromAST(ALogActionFmlActionExp astNode) {
		LogAction returned = getFactory().newLogAction();

		DataBinding<String> logString = ExpressionFactory.makeDataBinding(getASTNode().getExpression(), returned, BindingDefinitionType.GET,
				String.class, getSemanticsAnalyzer(), this);
		// returned.setLogString(makeBinding(getASTNode().getExpression(), returned));
		returned.setLogString(logString);
		returned.setLogLevel(LogLevel.INFO);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("","log",SPACE), getLogFragment());
		// append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> getModelObject().getLogString().toString()), getExpressionFragment());
		// append(staticContents(")"), getRParFragment());
		append(staticContents(";"), getSemiFragment());

	}

	protected RawSourceFragment getLogFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwLog());
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
