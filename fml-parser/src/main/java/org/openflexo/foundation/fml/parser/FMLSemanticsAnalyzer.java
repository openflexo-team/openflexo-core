package org.openflexo.foundation.fml.parser;

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AFlexoBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AImportDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamespaceDeclaration;
import org.openflexo.foundation.fml.parser.node.APrimitiveFormalArgument;
import org.openflexo.foundation.fml.parser.node.AReferenceFormalArgument;
import org.openflexo.foundation.fml.parser.node.ATechnologySpecificFormalArgument;
import org.openflexo.foundation.fml.parser.node.AUseDeclaration;
import org.openflexo.foundation.fml.parser.node.AViewpointDeclaration;
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

	private final ViewPoint viewPoint;
	private final FlexoServiceManager serviceManager;
	private final Map<Node, FMLObject> parsedFMLObjects;

	public FMLSemanticsAnalyzer(ViewPoint viewPoint, FlexoServiceManager serviceManager) {
		this.viewPoint = viewPoint;
		this.serviceManager = serviceManager;
		parsedFMLObjects = new HashMap<Node, FMLObject>();
	}

	public ViewPoint getViewPoint() {
		return viewPoint;
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
	public void outANamespaceDeclaration(ANamespaceDeclaration node) {
		super.outANamespaceDeclaration(node);
		System.out.println("Tiens, un namespace: " + node);
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
	}

	@Override
	public void outAViewpointDeclaration(AViewpointDeclaration node) {
		super.outAViewpointDeclaration(node);
		System.out.println("Tiens, un ViewPointDeclaration: " + node);
		System.out.println("Annotations = " + node.getAnnotations());
	}

	@Override
	public void outAFlexoConceptDeclaration(AFlexoConceptDeclaration node) {
		super.outAFlexoConceptDeclaration(node);
		System.out.println("Tiens, un FlexoConceptDeclaration: " + node);
		System.out.println("Annotations du FlexoConcept = " + node.getAnnotations());
	}

	@Override
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
}