package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
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
		factory = new FMLModelFactory(null, serviceManager.getEditingContext(), serviceManager.getTechnologyAdapterService());
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