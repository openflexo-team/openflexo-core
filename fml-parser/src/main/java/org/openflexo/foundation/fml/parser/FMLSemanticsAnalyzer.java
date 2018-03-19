/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AFlexoConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AImportDeclaration;
import org.openflexo.foundation.fml.parser.node.AUseDeclaration;
import org.openflexo.foundation.fml.parser.node.AVirtualModelDeclaration;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	private final VirtualModel virtualModel;
	private final FlexoServiceManager serviceManager;
	private final Map<Node, FMLObject> parsedFMLObjects;

	public FMLSemanticsAnalyzer(VirtualModel viewPoint, FlexoServiceManager serviceManager) {
		this.virtualModel = viewPoint;
		this.serviceManager = serviceManager;
		parsedFMLObjects = new HashMap<>();
	}

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public FMLCompilationUnit getCompilationUnit() {
		return null;
	}

	private void registerVirtualModel(AVirtualModelDeclaration node, VirtualModel vm) {
		// TODO
		parsedFMLObjects.put(node, vm);
	}

	private VirtualModel handleVirtualModel(AVirtualModelDeclaration node) {
		System.out.println("Tiens, un VirtualModelDeclaration: " + node);
		System.out.println("Name = " + node.getIdentifier());
		System.out.println("Annotations = " + node.getAnnotations());

		try {
			VirtualModelSemanticsAnalyzer vmsa = new VirtualModelSemanticsAnalyzer(node, this, serviceManager);
			node.apply(vmsa);
			VirtualModel vm = vmsa.makeFMLObject();
			registerVirtualModel(node, vm);
			return vm;
		} catch (ModelDefinitionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}

	@Override
	public void outAUseDeclaration(AUseDeclaration node) {
		super.outAUseDeclaration(node);
		System.out.println("Tiens, un use: " + node + " ta=" + node.getTechnologyAdapter()
				+ node.getTechnologyAdapterAdditionalIdentifiers() + " id=" + node.getTaId());
	}

	@Override
	public void outAImportDeclaration(AImportDeclaration node) {
		super.outAImportDeclaration(node);
		System.out.println("Tiens, un import: " + node);
	}

	@Override
	public void outAVirtualModelDeclaration(AVirtualModelDeclaration node) {
		super.outAVirtualModelDeclaration(node);
		handleVirtualModel(node);
		System.out.println("Annotations = " + node.getAnnotations());
	}

	@Override
	public void outAFlexoConceptDeclaration(AFlexoConceptDeclaration node) {
		super.outAFlexoConceptDeclaration(node);
		System.out.println("Tiens, un FlexoConceptDeclaration: " + node);
		System.out.println("Annotations du FlexoConcept = " + node.getAnnotations());
	}

	/*@Override
	public void outAFlexoRoleDeclaration(AFlexoRoleDeclaration node) {
		super.outAFlexoRoleDeclaration(node);
		System.out.println("Tiens, un FlexoRoleDeclaration: " + node);
		System.out.println("Annotations du FlexoRole = " + node.getAnnotations());
	}
	
	@Override
	public void outAFlexoBehaviourDeclaration(AFlexoBehaviourDeclaration node) {
		super.outAFlexoBehaviourDeclaration(node);
		System.out.println("Tiens, un FlexoBehaviourDeclaration: " + node);
		System.out.println("Annotations du FlexoBehaviour = " + node.getAnnotations());
	}
	
	@Override
	public void outAPrimitiveFormalArgument(APrimitiveFormalArgument node) {
		super.outAPrimitiveFormalArgument(node);
		System.out.println("arg1:" + node);
	}
	
	@Override
	public void outAReferenceFormalArgument(AReferenceFormalArgument node) {
		super.outAReferenceFormalArgument(node);
		System.out.println("arg2:" + node);
	}
	
	@Override
	public void outATechnologySpecificFormalArgument(ATechnologySpecificFormalArgument node) {
		super.outATechnologySpecificFormalArgument(node);
		System.out.println("arg3:" + node);
	}
	
	@Override
	public void outABlock(ABlock node) {
		super.outABlock(node);
		System.out.println("########## BLOCK:" + node);
	}*/
}
