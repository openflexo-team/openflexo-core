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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.AIdentifierAssignment;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class InnerExpressionActionNode extends AssignableActionNode<Node, ExpressionAction<?>> {

	private static final Logger logger = Logger.getLogger(InnerExpressionActionNode.class.getPackage().getName());

	public InnerExpressionActionNode(Node astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);

		if (getExpressionFragment() != null) {
			setStartPosition(getExpressionFragment().getStartPosition());
			setEndPosition(getExpressionFragment().getEndPosition());
		}

	}

	public InnerExpressionActionNode(ExpressionAction<?> action, ControlGraphFactory cgFactory) {
		super(action, cgFactory);
	}

	@Override
	public ExpressionAction<?> buildModelObjectFromAST(Node astNode) {
		ExpressionAction<?> returned = getFactory().newExpressionAction();
		System.out.println(">>>>>> Inner Expression " + astNode);
		returned.setExpression(makeBinding(getIdentifier(), getAdditionalIdentifiers(), returned));
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		if (hasParsedVersion) {
			appendDynamicContents(() -> getModelObject().getExpression().toString(), getExpressionFragment());
		}
		else {
			appendDynamicContents(() -> getModelObject().getExpression().toString());
		}
	}

	private TIdentifier getIdentifier() {
		if (getASTNode() instanceof AIdentifierAssignment) {
			return ((AIdentifierAssignment) getASTNode()).getIdentifier();
		}
		return null;
	}

	private List<PAdditionalIdentifier> getAdditionalIdentifiers() {
		if (getASTNode() instanceof AIdentifierAssignment) {
			return ((AIdentifierAssignment) getASTNode()).getAdditionalIdentifiers();
		}
		return null;
	}

	private RawSourceFragment getExpressionFragment() {
		if (getIdentifier() != null && getAdditionalIdentifiers() != null) {
			return getFragment(getIdentifier(), getAdditionalIdentifiers());
		}
		return null;
	}

}
