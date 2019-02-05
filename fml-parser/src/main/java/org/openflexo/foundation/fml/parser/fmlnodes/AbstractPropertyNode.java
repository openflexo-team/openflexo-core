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

package org.openflexo.foundation.fml.parser.fmlnodes;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;

/**
 * @author sylvain
 * 
 */
public class AbstractPropertyNode extends FlexoPropertyNode<AAbstractPropertyDeclaration, AbstractProperty<?>> {

	private static final Logger logger = Logger.getLogger(AbstractPropertyNode.class.getPackage().getName());

	public AbstractPropertyNode(AAbstractPropertyDeclaration astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public AbstractPropertyNode(AbstractProperty<?> property, FMLSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public AbstractProperty<?> buildFMLObjectFromAST(AAbstractPropertyDeclaration astNode) {
		AbstractProperty<?> returned = getFactory().newAbstractProperty();
		returned.setName(getName(astNode.getVariableDeclarator()).getText());
		returned.setType(getTypeFactory().makeType(astNode.getType()));
		return returned;
	}

	@Override
	protected void preparePrettyPrint() {
		super.preparePrettyPrint();
		RawSourceFragment abstractFragment = getFragment(getASTNode().getAbstract());
		RawSourceFragment typeFragment = getFragment(getASTNode().getType());
		RawSourceFragment nameFragment = null;
		PVariableDeclarator variableDeclarator = getASTNode().getVariableDeclarator();
		if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
			nameFragment = getFragment(((AIdentifierVariableDeclarator) variableDeclarator).getIdentifier());
		}
		else if (variableDeclarator instanceof AInitializerVariableDeclarator) {
			nameFragment = getFragment(((AInitializerVariableDeclarator) variableDeclarator).getIdentifier());
		}
		RawSourceFragment semiFragment = getFragment(getASTNode().getSemi());

		appendStaticContents("abstract", SPACE, abstractFragment);
		appendDynamicContents(() -> serializeType(getFMLObject().getType(), getCompilationUnit()), SPACE, typeFragment);
		appendDynamicContents(() -> getFMLObject().getName(), nameFragment);
		appendStaticContents(";", semiFragment);
	}

}
