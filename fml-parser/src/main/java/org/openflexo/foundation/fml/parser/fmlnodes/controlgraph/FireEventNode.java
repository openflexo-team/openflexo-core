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
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFireActionFmlActionExp;
import org.openflexo.foundation.fml.rt.FlexoEventInstance;
import org.openflexo.foundation.fml.rt.editionaction.FireEvent;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class FireEventNode extends ControlGraphNode<AFireActionFmlActionExp, FireEvent> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FireEventNode.class.getPackage().getName());

	public FireEventNode(AFireActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public FireEventNode(FireEvent action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FireEvent buildModelObjectFromAST(AFireActionFmlActionExp astNode) {
		FireEvent returned = getFactory().newFireEvent();

		DataBinding<FlexoEventInstance> eventInstance = ExpressionFactory.makeDataBinding(getASTNode().getExpression(), returned,
				BindingDefinitionType.GET, FlexoEventInstance.class, getSemanticsAnalyzer(), this);
		returned.setEventInstance(eventInstance);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", FMLKeywords.Fire.getKeyword(), SPACE), getFireFragment());
		append(dynamicContents(() -> getModelObject().getEventInstance().toString()), getExpressionFragment());
		append(staticContents(";"), getSemiFragment());

	}

	protected RawSourceFragment getFireFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwFire());
		}
		return null;
	}

	protected RawSourceFragment getExpressionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

}
