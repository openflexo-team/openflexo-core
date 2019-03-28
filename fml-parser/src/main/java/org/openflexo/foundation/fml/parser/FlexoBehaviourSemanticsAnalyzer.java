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

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.ActionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.node.ABehaviourDeclarationInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.PBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.p2pp.RawSource;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourSemanticsAnalyzer extends FlexoPropertySemanticsAnalyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoBehaviourSemanticsAnalyzer.class.getPackage().getName());

	public FlexoBehaviourSemanticsAnalyzer(FMLModelFactory factory, Start tree, RawSource rawSource) {
		super(factory, tree, rawSource);
	}

	@Override
	public void inABehaviourDeclarationInnerConceptDecl(ABehaviourDeclarationInnerConceptDecl node) {
		super.inABehaviourDeclarationInnerConceptDecl(node);
		push(makeBehaviourNode(node.getBehaviourDecl()));
	}

	@Override
	public void outABehaviourDeclarationInnerConceptDecl(ABehaviourDeclarationInnerConceptDecl node) {
		super.outABehaviourDeclarationInnerConceptDecl(node);
		pop();
	}

	// See fml.sablecc l.367
	private FlexoBehaviourNode<?, ?> makeBehaviourNode(PBehaviourDecl node) {
		System.out.println("Tiens tiens, je tombe sur un " + node.getClass());

		if (node instanceof AMethodBehaviourDecl) {
			return new ActionSchemeNode((AMethodBehaviourDecl) node, (FMLSemanticsAnalyzer) this);
		}

		/*
		if (node instanceof AAbstractPropertyInnerConceptDecl) {
			return new AbstractPropertyNode((AAbstractPropertyInnerConceptDecl) node, (FMLSemanticsAnalyzer) this);
		}
		else if (node instanceof AJavaInnerConceptDecl) {
			Type type = getTypeFactory().makeType(((AJavaInnerConceptDecl) node).getType());
			// System.out.println("Tiens une basic property declaration java: " + node + " type=" + type);
			if (getTypeFactory().getPrimitiveType(type) != null) {
				return new PrimitiveRoleNode((AJavaInnerConceptDecl) node, (FMLSemanticsAnalyzer) this);
			}
			return new JavaRoleNode((AJavaInnerConceptDecl) node, (FMLSemanticsAnalyzer) this);
		}
		else if (node instanceof AFmlInnerConceptDecl) {
			// System.out.println("Tiens une basic property declaration FML: " + node);
		}
		else if (node instanceof AFmlFullyQualifiedInnerConceptDecl) {
			// System.out.println("Tiens une basic property declaration FML fully-qualified: " + node);
		}
		else if (node instanceof AExpressionPropertyInnerConceptDecl) {
		
		}
		else if (node instanceof AGetSetPropertyInnerConceptDecl) {
		
		}*/

		logger.warning("Unexpected node: " + node + " of " + node.getClass());
		Thread.dumpStack();
		return null;

	}

}
