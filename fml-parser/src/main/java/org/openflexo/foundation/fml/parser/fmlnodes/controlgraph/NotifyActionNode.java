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
import org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AInClause;
import org.openflexo.foundation.fml.parser.node.ANotifyActionFmlActionExp;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * @author sylvain
 * 
 */
public class NotifyActionNode extends ControlGraphNode<ANotifyActionFmlActionExp, NotifyPropertyChangedAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NotifyActionNode.class.getPackage().getName());

	public NotifyActionNode(ANotifyActionFmlActionExp astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public NotifyActionNode(NotifyPropertyChangedAction action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public NotifyPropertyChangedAction buildModelObjectFromAST(ANotifyActionFmlActionExp astNode) {
		NotifyPropertyChangedAction returned = getFactory().newNotifyPropertyChangedAction();

		DataBinding<String> propertyName = ExpressionFactory.makeDataBinding(getASTNode().getExpression(), returned,
				BindingDefinitionType.GET, String.class, getSemanticsAnalyzer(), this);
		// returned.setLogString(makeBinding(getASTNode().getExpression(), returned));
		returned.setPropertyName(propertyName);
		if (astNode.getInClause() != null) {
			AInClause inClause = (AInClause) astNode.getInClause();
			DataBinding<HasPropertyChangeSupport> sourceObject = ExpressionFactory.makeDataBinding(inClause.getExpression(), returned,
					BindingDefinitionType.GET, HasPropertyChangeSupport.class, getSemanticsAnalyzer(), this);
			returned.setObject(sourceObject);
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(staticContents("", FMLKeywords.Notify.getKeyword(), SPACE), getNotifyFragment());
		append(dynamicContents(() -> getModelObject().getPropertyName().toString()), getExpressionFragment());
		when(() -> getModelObject().getObject().isSet())
				.thenAppend(staticContents(SPACE, FMLKeywords.In.getKeyword(), SPACE), getInFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getObject().toString()), getObjectFragment());
		append(staticContents(";"), getSemiFragment());

	}

	protected RawSourceFragment getNotifyFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwNotify());
		}
		return null;
	}

	protected RawSourceFragment getExpressionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

	protected RawSourceFragment getInFragment() {
		if (getASTNode() != null && getASTNode().getInClause() != null) {
			AInClause inClause = (AInClause) getASTNode().getInClause();
			return getFragment(inClause.getKwIn());
		}
		return null;
	}

	protected RawSourceFragment getObjectFragment() {
		if (getASTNode() != null && getASTNode().getInClause() != null) {
			AInClause inClause = (AInClause) getASTNode().getInClause();
			return getFragment(inClause.getExpression());
		}
		return null;
	}

}
