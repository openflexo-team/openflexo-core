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
import java.util.List;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.AbstractPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.PrimitiveRoleNode;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.ABasicPropertyPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AExpressionPropertyPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AFmlBasicRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedBasicRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AGetSetPropertyPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AJavaBasicRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.APropertyDeclarationInnerConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.PBasicRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.PPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.Start;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class FlexoPropertySemanticsAnalyzer extends FlexoConceptSemanticsAnalyzer {

	public FlexoPropertySemanticsAnalyzer(FMLModelFactory factory, Start tree, List<String> rawSource) {
		super(factory, tree, rawSource);
	}

	private FlexoPropertyNode<?, ?> makePropertyNode(PPropertyDeclaration node) {
		if (node instanceof AAbstractPropertyPropertyDeclaration) {
			return new AbstractPropertyNode(
					(AAbstractPropertyDeclaration) ((AAbstractPropertyPropertyDeclaration) node).getAbstractPropertyDeclaration(),
					(FMLSemanticsAnalyzer) this);
		}
		else if (node instanceof ABasicPropertyPropertyDeclaration) {
			PBasicRoleDeclaration basicRoleDeclaration = ((ABasicPropertyPropertyDeclaration) node).getBasicRoleDeclaration();
			if (basicRoleDeclaration instanceof AJavaBasicRoleDeclaration) {
				Type type = getTypeFactory().makeType(((AJavaBasicRoleDeclaration) basicRoleDeclaration).getType());
				System.out.println("Tiens une basic property declaration java: " + node + " type=" + type);
				if (getTypeFactory().getPrimitiveType(type) != null) {
					return new PrimitiveRoleNode((AJavaBasicRoleDeclaration) basicRoleDeclaration, (FMLSemanticsAnalyzer) this);
				}
				else {
					return new JavaRoleNode((AJavaBasicRoleDeclaration) basicRoleDeclaration, (FMLSemanticsAnalyzer) this);
				}
			}
			else if (basicRoleDeclaration instanceof AFmlBasicRoleDeclaration) {
				System.out.println("Tiens une basic property declaration FML: " + node);
			}
			else if (basicRoleDeclaration instanceof AFmlFullyQualifiedBasicRoleDeclaration) {
				System.out.println("Tiens une basic property declaration FML fully-qualified: " + node);
			}
		}
		else if (node instanceof AExpressionPropertyPropertyDeclaration) {

		}
		else if (node instanceof AGetSetPropertyPropertyDeclaration) {

		}
		return null;
	}

	@Override
	public void inAPropertyDeclarationInnerConceptDeclaration(APropertyDeclarationInnerConceptDeclaration node) {
		super.inAPropertyDeclarationInnerConceptDeclaration(node);
		push(makePropertyNode(node.getPropertyDeclaration()));
	}

	@Override
	public void outAPropertyDeclarationInnerConceptDeclaration(APropertyDeclarationInnerConceptDeclaration node) {
		super.outAPropertyDeclarationInnerConceptDeclaration(node);
		pop();
	}

}
