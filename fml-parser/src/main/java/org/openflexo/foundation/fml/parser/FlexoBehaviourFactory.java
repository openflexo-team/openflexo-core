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

package org.openflexo.foundation.fml.parser;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.parser.fmlnodes.ActionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.CreationSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.DeletionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.EventListenerNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.node.AAnonymousConstructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AAnonymousDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AFmlBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AListenerBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ANamedConstructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ANamedDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.PBehaviourDecl;

/**
 * Handle {@link FlexoBehaviour} in the FML parser<br>
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourFactory extends SemanticsAnalyzerFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoBehaviourFactory.class.getPackage().getName());

	public FlexoBehaviourFactory(FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(analyzer);
	}

	FlexoBehaviourNode<?, ?> makeBehaviourNode(PBehaviourDecl node) {
		if (node instanceof AAnonymousConstructorBehaviourDecl) {
			return new CreationSchemeNode(node, getAnalyzer());
		}
		else if (node instanceof ANamedConstructorBehaviourDecl) {
			return new CreationSchemeNode(node, getAnalyzer());
		}
		else if (node instanceof AAnonymousDestructorBehaviourDecl) {
			return new DeletionSchemeNode(node, getAnalyzer());
		}
		else if (node instanceof ANamedDestructorBehaviourDecl) {
			return new DeletionSchemeNode(node, getAnalyzer());
		}
		else if (node instanceof AFmlBehaviourDecl) { // fml.sablecc l 411/412
			return new FMLBehaviourNode(node, getAnalyzer());
		}
		else if (node instanceof AFmlFullyQualifiedBehaviourDecl) {
			return new FMLBehaviourNode(node, getAnalyzer());
		}
		else if (node instanceof AListenerBehaviourDecl) {
			return new EventListenerNode((AListenerBehaviourDecl) node, getAnalyzer());
		}
		else if (node instanceof AMethodBehaviourDecl) {
			return new ActionSchemeNode((AMethodBehaviourDecl) node, getAnalyzer());
		}
		logger.warning("Unexpected node: " + node + " of " + node.getClass());
		Thread.dumpStack();
		return null;
	}

}
