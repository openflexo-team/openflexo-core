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

import java.lang.reflect.Type;
import java.util.Date;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.fmlnodes.AbstractPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.BasicPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ExpressionPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoRolePropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.GetSetPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ModelSlotPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.PrimitiveRoleNode;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AExpressionPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AGetSetPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AJavaInnerConceptDecl;

/**
 * Handle {@link FlexoProperty} in the FML parser<br>
 * 
 * @author sylvain
 * 
 */
public class FlexoPropertyFactory extends SemanticsAnalyzerFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoPropertyFactory.class.getPackage().getName());

	public FlexoPropertyFactory(MainSemanticsAnalyzer analyzer) {
		super(analyzer);
	}

	AbstractPropertyNode makeAbstractPropertyNode(AAbstractPropertyInnerConceptDecl node) {
		return new AbstractPropertyNode(node, getAnalyzer());
	}

	BasicPropertyNode<?> makeBasicPropertyNode(AJavaInnerConceptDecl node) {
		Type type = TypeFactory.makeType(node.getType(), getAnalyzer().getTypingSpace());
		if (TypeUtils.isPrimitive(type) || type.equals(String.class) || type.equals(Date.class)) {
			return new PrimitiveRoleNode(node, getAnalyzer());
		}
		else {
			return new JavaRoleNode(node, getAnalyzer());
		}
	}

	ModelSlotPropertyNode<?, ?> makeModelSlotPropertyNode(AFmlFullyQualifiedInnerConceptDecl node) {
		return new ModelSlotPropertyNode(node, getAnalyzer());
	}

	ModelSlotPropertyNode<?, ?> makeModelSlotPropertyNode(AFmlInnerConceptDecl node) {
		return new ModelSlotPropertyNode(node, getAnalyzer());
	}

	FlexoRolePropertyNode<?, ?> makeFlexoRolePropertyNode(AFmlFullyQualifiedInnerConceptDecl node) {
		return new FlexoRolePropertyNode(node, getAnalyzer());
	}

	FlexoRolePropertyNode<?, ?> makeFlexoRolePropertyNode(AFmlInnerConceptDecl node) {
		return new FlexoRolePropertyNode(node, getAnalyzer());
	}

	ExpressionPropertyNode makeExpressionPropertyNode(AExpressionPropertyInnerConceptDecl node) {
		return new ExpressionPropertyNode(node, getAnalyzer());
	}

	GetSetPropertyNode makeGetSetPropertyNode(AGetSetPropertyInnerConceptDecl node) {
		return new GetSetPropertyNode(node, getAnalyzer());
	}

	/*FlexoPropertyNode<?, ?> makePropertyNode(PInnerConceptDecl node) {
		if (node instanceof ABasicPropertyPropertyDeclaration) {
			PBasicRoleDeclaration basicRoleDeclaration = ((ABasicPropertyPropertyDeclaration) node).getBasicRoleDeclaration();
			if (basicRoleDeclaration instanceof AJavaBasicRoleDeclaration) {
				Type type = getTypeFactory().makeType(((AJavaBasicRoleDeclaration) basicRoleDeclaration).getType());
				// System.out.println("Tiens une basic property declaration java: " + node + " type=" + type);
				if (getTypeFactory().getPrimitiveType(type) != null) {
					return new PrimitiveRoleNode((AJavaBasicRoleDeclaration) basicRoleDeclaration, getAnalyzer());
				}
				else {
					return new JavaRoleNode((AJavaBasicRoleDeclaration) basicRoleDeclaration, getAnalyzer());
				}
			}
			else if (basicRoleDeclaration instanceof AFmlBasicRoleDeclaration) {
				// System.out.println("Tiens une basic property declaration FML: " + node);
			}
			else if (basicRoleDeclaration instanceof AFmlFullyQualifiedBasicRoleDeclaration) {
				// System.out.println("Tiens une basic property declaration FML fully-qualified: " + node);
			}
			return new JavaRoleNode((AJavaInnerConceptDecl) node, (FMLSemanticsAnalyzer) this);
		}
		else if (node instanceof AFmlInnerConceptDecl) {
			// System.out.println("Tiens une basic property declaration FML: " + node);
		}
		else if (node instanceof AFmlFullyQualifiedInnerConceptDecl) {
			// System.out.println("Tiens une basic property declaration FML fully-qualified: " + node);
		}
		else if (node instanceof AExpressionPropertyPropertyDeclaration) {
			PExpressionPropertyDeclaration expressionPropertyDeclaration = ((AExpressionPropertyPropertyDeclaration) node)
					.getExpressionPropertyDeclaration();
			return new ExpressionPropertyNode(expressionPropertyDeclaration, getAnalyzer());
	
		}
		else if (node instanceof AGetSetPropertyPropertyDeclaration) {
			AGetSetPropertyDeclaration getSetPropertyDeclaration = (AGetSetPropertyDeclaration) ((AGetSetPropertyPropertyDeclaration) node)
					.getGetSetPropertyDeclaration();
			return new GetSetPropertyNode(getSetPropertyDeclaration, getAnalyzer());
	
		}
		logger.warning("Unexpected node: " + node + " of " + node.getClass());
		Thread.dumpStack();
		return null;
	}*/
}
