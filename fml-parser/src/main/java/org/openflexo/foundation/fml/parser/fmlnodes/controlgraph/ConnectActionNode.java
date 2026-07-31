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
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.editionaction.ConnectAction;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AConnectActionFmlActionExp;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ConnectActionNode extends ControlGraphNode<AConnectActionFmlActionExp, ConnectAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ConnectActionNode.class.getPackage().getName());

	public ConnectActionNode(AConnectActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public ConnectActionNode(ConnectAction action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ConnectAction buildModelObjectFromAST(AConnectActionFmlActionExp astNode) {
		ConnectAction returned = getFactory().newConnectAction();

		DataBinding<?> connectObject = ExpressionFactory.makeDataBinding(getASTNode().getConnect(), returned, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);
		DataBinding<?> usingObject = ExpressionFactory.makeDataBinding(getASTNode().getUsing(), returned, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);

		// returned.setLogString(logString);
		// returned.setLogLevel(LogLevel.INFO);

		// System.out.println("Hop");
		// System.out.println("connectObject=" + connectObject + " of " + connectObject.getAnalyzedType());
		// System.out.println("usingObject=" + usingObject + " of " + usingObject.getAnalyzedType());

		returned.setConnect(connectObject);
		returned.setUsing(usingObject);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", FMLKeywords.Connect.getKeyword(), SPACE), getConnectKeywordFragment());
		append(dynamicContents(() -> getModelObject().getConnect().toString(), SPACE), getConnectFragment());
		append(staticContents("", FMLKeywords.Using.getKeyword(), SPACE), getUsingKeywordFragment());
		append(dynamicContents(() -> getModelObject().getUsing().toString()), getUsingFragment());
		append(staticContents(";"), getSemiFragment());

	}

	protected RawSourceFragment getConnectKeywordFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwConnect());
		}
		return null;
	}

	protected RawSourceFragment getConnectFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getConnect());
		}
		return null;
	}

	protected RawSourceFragment getUsingKeywordFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwUsing());
		}
		return null;
	}

	protected RawSourceFragment getUsingFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getUsing());
		}
		return null;
	}

}
