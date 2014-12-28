package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AMainDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamespaceDeclaration;

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
	public void caseANamespaceDeclaration(ANamespaceDeclaration node) {
		super.caseANamespaceDeclaration(node);
		System.out.println("Tiens, un namespace: " + node);
	}

	@Override
	public void caseAMainDeclaration(AMainDeclaration node) {
		super.caseAMainDeclaration(node);
		System.out.println("Tiens, une MainDeclaration: " + node);
	}
}