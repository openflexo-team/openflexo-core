/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.node.AModelSlotDeclaration;
import org.openflexo.foundation.fml.parser.node.AVirtualModelDeclaration;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * This class implements the semantics analyzer for a parsed VirtualModel.<br>
 * Its main purpose is to structurally build a binding from a parsed AST.<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
class VirtualModelSemanticsAnalyzer extends FMLObjectSemanticsAnalyzer<AVirtualModelDeclaration, VirtualModel> {

	private final FMLModelFactory factory;

	public VirtualModelSemanticsAnalyzer(AVirtualModelDeclaration node, FMLSemanticsAnalyzer parentAnalyser,
			FlexoServiceManager serviceManager) throws ModelDefinitionException {
		super(node, parentAnalyser, serviceManager);
		factory = new FMLModelFactory(null, serviceManager);
	}

	@Override
	public VirtualModel makeFMLObject() {
		VirtualModel vm = factory.newVirtualModel();
		/*try {
			vm = VirtualModelImpl.newVirtualModel(getNode().getIdentifier().getText(), getViewPoint());
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}*/
		return vm;
	}

	@Override
	public void outAVirtualModelDeclaration(AVirtualModelDeclaration node) {
		defaultOut(node);
	}

	@Override
	public void outAModelSlotDeclaration(AModelSlotDeclaration node) {
		super.outAModelSlotDeclaration(node);
		System.out.println("******** Tiens, un ModelSlotDeclaration: " + node + " pour le VM " + getNode().getIdentifier());
		System.out.println("line=" + node.getModelslot().getLine());
		System.out.println("pos=" + node.getModelslot().getPos());

	}
}
