package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AImportDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamespaceDeclaration;
import org.openflexo.foundation.fml.parser.node.AUseDeclaration;
import org.openflexo.foundation.fml.parser.node.AViewpointDeclaration;
import org.openflexo.foundation.fml.parser.node.AVirtualModelDeclaration;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	public FMLCompilationUnit getCompilationUnit() {
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
		System.out.println("Tiens, un VirtualModelDeclaration: " + node);
		System.out.println("Annotations = " + node.getAnnotations());
	}

	@Override
	public void outAViewpointDeclaration(AViewpointDeclaration node) {
		super.outAViewpointDeclaration(node);
		System.out.println("Tiens, un ViewPointDeclaration: " + node);
		System.out.println("Annotations = " + node.getAnnotations());
	}

}