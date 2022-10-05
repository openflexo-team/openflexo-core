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

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.ABlockStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AExpressionStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.ANoTrailStatement;
import org.openflexo.foundation.fml.parser.node.AStatementWithoutTrailingSubstatementStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.foundation.fml.parser.node.PStatementNoShortIf;
import org.openflexo.foundation.fml.parser.node.PStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.TLBrc;
import org.openflexo.foundation.fml.parser.node.TRBrc;
import org.openflexo.foundation.fml.parser.node.TSemi;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public abstract class ControlGraphNode<N extends Node, T extends FMLControlGraph> extends FMLObjectNode<N, T, ControlGraphFactory> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlGraphNode.class.getPackage().getName());

	public ControlGraphNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ControlGraphNode(T property, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(property, analyzer);
	}

	@Override
	public ControlGraphNode<N, T> deserialize() {
		// System.out.println("deserialize for " + getParent() + " modelObject: " + getModelObject());
		if (getParent() instanceof SequenceNode) {
			// Nothing to to here: this is done in SequenceNode
		}
		return this;
	}

	protected TSemi getSemi() {
		if (getASTNode() != null) {
			Node current = getASTNode();
			while (current.parent() != null) {
				if (current instanceof AExpressionStatementStatementWithoutTrailingSubstatement) {
					return ((AExpressionStatementStatementWithoutTrailingSubstatement) current).getSemi();
				}
				current = current.parent();
			}
		}
		return null;
	}

	protected RawSourceFragment getSemiFragment() {
		if (getSemi() != null) {
			return getFragment(getSemi());
		}
		return null;
	}

	/**
	 * Return LBrc token of supplied statement, if any
	 * 
	 * @param statement
	 * @return
	 */
	protected TLBrc getLBrc(PStatement statement) {
		if (statement instanceof ANoTrailStatement) {
			PStatementWithoutTrailingSubstatement statementWithoutTrailingSubstatement = ((ANoTrailStatement) statement)
					.getStatementWithoutTrailingSubstatement();
			if (statementWithoutTrailingSubstatement instanceof ABlockStatementWithoutTrailingSubstatement) {
				return ((ABlockStatementWithoutTrailingSubstatement) statementWithoutTrailingSubstatement).getLBrc();
			}
		}
		return null;
	}

	/**
	 * Return RBrc token of supplied statement, if any
	 * 
	 * @param statement
	 * @return
	 */
	protected TRBrc getRBrc(PStatement statement) {
		if (statement instanceof ANoTrailStatement) {
			PStatementWithoutTrailingSubstatement statementWithoutTrailingSubstatement = ((ANoTrailStatement) statement)
					.getStatementWithoutTrailingSubstatement();
			if (statementWithoutTrailingSubstatement instanceof ABlockStatementWithoutTrailingSubstatement) {
				return ((ABlockStatementWithoutTrailingSubstatement) statementWithoutTrailingSubstatement).getRBrc();
			}
		}
		return null;
	}

	/**
	 * Return LBrc token of supplied statement, if any
	 * 
	 * @param statement
	 * @return
	 */
	protected TLBrc getLBrc(PStatementNoShortIf statement) {
		if (statement instanceof AStatementWithoutTrailingSubstatementStatementNoShortIf) {
			PStatementWithoutTrailingSubstatement statementWithoutTrailingSubstatement = ((AStatementWithoutTrailingSubstatementStatementNoShortIf) statement)
					.getStatementWithoutTrailingSubstatement();
			if (statementWithoutTrailingSubstatement instanceof ABlockStatementWithoutTrailingSubstatement) {
				return ((ABlockStatementWithoutTrailingSubstatement) statementWithoutTrailingSubstatement).getLBrc();
			}
		}
		return null;
	}

	/**
	 * Return RBrc token of supplied statement, if any
	 * 
	 * @param statement
	 * @return
	 */
	protected TRBrc getRBrc(PStatementNoShortIf statement) {
		if (statement instanceof AStatementWithoutTrailingSubstatementStatementNoShortIf) {
			PStatementWithoutTrailingSubstatement statementWithoutTrailingSubstatement = ((AStatementWithoutTrailingSubstatementStatementNoShortIf) statement)
					.getStatementWithoutTrailingSubstatement();
			if (statementWithoutTrailingSubstatement instanceof ABlockStatementWithoutTrailingSubstatement) {
				return ((ABlockStatementWithoutTrailingSubstatement) statementWithoutTrailingSubstatement).getRBrc();
			}
		}
		return null;
	}

}
