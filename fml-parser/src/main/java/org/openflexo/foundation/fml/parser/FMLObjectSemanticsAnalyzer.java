package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * This class implements the semantics analyzer for a parsed FMLObject.<br>
 * Its main purpose is to structurally build a binding from a parsed AST.<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectSemanticsAnalyzer<N extends Node, T extends FMLObject> extends FMLSemanticsAnalyzer {

	private final N node;
	private final FMLSemanticsAnalyzer parentAnalyser;

	public FMLObjectSemanticsAnalyzer(N node, FMLSemanticsAnalyzer parentAnalyser, FlexoServiceManager serviceManager) {
		// System.out.println(">>>> node=" + node + " of " + node.getClass());
		super(parentAnalyser.getViewPoint(), serviceManager);
		this.node = node;
		this.parentAnalyser = parentAnalyser;
	}

	public N getNode() {
		return node;
	}

	public abstract T makeFMLObject();

	@Override
	public ViewPoint getViewPoint() {
		if (parentAnalyser != null) {
			System.out.println("Moi: " + this + " et lui " + parentAnalyser);
			return parentAnalyser.getViewPoint();
		}
		return super.getViewPoint();
	}
}