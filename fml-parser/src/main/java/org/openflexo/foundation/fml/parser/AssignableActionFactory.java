package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AddFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.node.AFmlInstanceCreationFmlActionExpression;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlActionExpression;
import org.openflexo.toolbox.StringUtils;

/**
 * 
 * @author sylvain
 *
 */
// TODO reimplement this properly using BindingFactory
public class AssignableActionFactory extends FMLSemanticsAnalyzer {

	public boolean debug = false;

	private ControlGraphFactory cgFactory;

	private ControlGraphNode<?, ?> rootControlGraphNode = null;

	private PFmlActionExpression pFMLActionExpression;

	public AssignableActionFactory(Node cgNode, ControlGraphFactory cgFactory) {
		super(cgFactory.getFactory(), cgNode);
		this.cgFactory = cgFactory;
		cgNode.apply(this);
		if (pFMLActionExpression != null) {
			// We consider only this FML action and ignore everything else
		}
		else {
			push(new ExpressionActionNode(cgNode, cgFactory));
			pop();
		}
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return cgFactory.getMainAnalyzer();
	}

	public ControlGraphNode<?, ?> getRootControlGraphNode() {
		return rootControlGraphNode;
	}

	public AssignableAction<?> getAssignableAction() {
		if (getRootControlGraphNode() != null) {
			return (AssignableAction<?>) getRootControlGraphNode().getModelObject();
		}
		else {
			return null;
		}
	}

	@Override
	protected void push(FMLObjectNode<?, ?, ?> fmlNode) {
		if (rootControlGraphNode == null && fmlNode instanceof ControlGraphNode) {
			rootControlGraphNode = (ControlGraphNode<?, ?>) fmlNode;
		}
		super.push(fmlNode);
		// analyzer.push(fmlNode);
	}

	@Override
	protected <N extends FMLObjectNode<?, ?, ?>> N pop() {
		N returned = super.pop();
		// analyzer.pop();
		return returned;
	}

	int indent = 0;

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		if (debug) {
			System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + "> " + node.getClass().getSimpleName());
			indent++;
		}
	}

	@Override
	public void defaultOut(Node node) {
		super.defaultOut(node);
		if (debug) {
			indent--;
		}
	}

	/*
	 * <pre>
	primary =
	    {literal}                literal |
	    //   {primitive}  primitive_type                                             [dims]:dim* dot class_token |
	    //   {reference}  identifier [additional_identifiers]:additional_identifier* [dims]:dim* dot class_token |
	    //   {void}       void dot class_token |
	    //   {this}       this |
	    //   {class}      identifier [additional_identifiers]:additional_identifier* dot this |
	    {expression}             l_par expression                                                 r_par |
	    {identifier}             l_par identifier [additional_identifiers]:additional_identifier* r_par |
	    {field}                  field_access |
	    {method}                 method_invocation |
	    {fml_action_expression}  fml_action_expression;
	 * </pre>
	 */

	/*@Override
	public void inALiteralPrimary(ALiteralPrimary node) {
		super.inALiteralPrimary(node);
		push(new ExpressionActionNode(node, analyzer));
	}
	
	@Override
	public void outALiteralPrimary(ALiteralPrimary node) {
		super.outALiteralPrimary(node);
		pop();
	}
	
	@Override
	public void inAExpressionPrimary(AExpressionPrimary node) {
		super.inAExpressionPrimary(node);
		push(new ExpressionActionNode(node, analyzer));
	}
	
	@Override
	public void outAExpressionPrimary(AExpressionPrimary node) {
		super.outAExpressionPrimary(node);
		pop();
	}
	
	@Override
	public void inAIdentifierPrimary(AIdentifierPrimary node) {
		super.inAIdentifierPrimary(node);
		push(new ExpressionActionNode(node, analyzer));
	}
	
	@Override
	public void outAIdentifierPrimary(AIdentifierPrimary node) {
		super.outAIdentifierPrimary(node);
		pop();
	}
	
	@Override
	public void inAFieldPrimary(AFieldPrimary node) {
		super.inAFieldPrimary(node);
		push(new ExpressionActionNode(node, analyzer));
	}
	
	@Override
	public void outAFieldPrimary(AFieldPrimary node) {
		super.outAFieldPrimary(node);
		pop();
	}
	
	@Override
	public void inAMethodPrimary(AMethodPrimary node) {
		super.inAMethodPrimary(node);
		push(new ExpressionActionNode(node, analyzer));
	}
	
	@Override
	public void outAMethodPrimary(AMethodPrimary node) {
		super.outAMethodPrimary(node);
		pop();
	}*/

	@Override
	public void inAFmlInstanceCreationFmlActionExpression(AFmlInstanceCreationFmlActionExpression node) {
		super.inAFmlInstanceCreationFmlActionExpression(node);
		pFMLActionExpression = node;
		push(new AddFlexoConceptInstanceNode(node.getConceptInstanceCreationExpression(), cgFactory));
	}

	@Override
	public void outAFmlInstanceCreationFmlActionExpression(AFmlInstanceCreationFmlActionExpression node) {
		super.outAFmlInstanceCreationFmlActionExpression(node);
		pop();
	}

}
