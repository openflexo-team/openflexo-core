package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.fml.parser.node.AAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ADoStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AEmptyStatement;
import org.openflexo.foundation.fml.parser.node.AFmlActionExpressionStatementExpression;
import org.openflexo.foundation.fml.parser.node.AForLoopStatement;
import org.openflexo.foundation.fml.parser.node.AIfElseStatement;
import org.openflexo.foundation.fml.parser.node.AIfStatement;
import org.openflexo.foundation.fml.parser.node.ALocalVariableDeclarationStatement;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AReturnStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AWhileLoopStatement;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.toolbox.StringUtils;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate {@link FMLControlGraph} from AST
 * 
 * @author sylvain
 *
 */
public class ControlGraphFactory extends FMLSemanticsAnalyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlGraphFactory.class.getPackage().getName());

	public boolean debug = false;

	private MainSemanticsAnalyzer mainAnalyzer;

	private ControlGraphNode<?, ?> rootControlGraphNode = null;

	public static ControlGraphNode<?, ?> makeControlGraphNode(Node cgNode, MainSemanticsAnalyzer analyzer) {
		ControlGraphFactory f = new ControlGraphFactory(cgNode, analyzer);
		cgNode.apply(f);
		return f.rootControlGraphNode;
	}

	private ControlGraphFactory(Node cgNode, MainSemanticsAnalyzer analyzer) {
		super(analyzer.getFactory(), cgNode);
		this.mainAnalyzer = analyzer;
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return mainAnalyzer;
	}

	public ControlGraphNode<?, ?> getRootControlGraphNode() {
		return rootControlGraphNode;
	}

	public FMLControlGraph getControlGraph() {
		if (getRootControlGraphNode() != null) {
			return getRootControlGraphNode().getModelObject();
		}
		else {
			return null;
		}
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
	 * 	block =
	 *	    l_brc [block_statements]:block_statement* r_brc;
	 * </pre>
	 */

	// private int totalStatementsInCurrentSequence = 0;
	// private int remainingStatementsInCurrentSequence = 0;
	private ABlock currentBlockNode;

	private List<ControlGraphNode<?, ?>> currentSequenceNodes;

	@Override
	public void inABlock(ABlock node) {
		// TODO Auto-generated method stub
		super.inABlock(node);
		System.out.println("Nouveau block de " + node.getBlockStatements().size() + " statements " + " avec " + node);
		if (node.getBlockStatements().size() > 1) {
			// totalStatementsInCurrentSequence = node.getBlockStatements().size();
			// remainingStatementsInCurrentSequence = node.getBlockStatements().size();
			// push(new SequenceNode(node, this));
			currentSequenceNodes = new ArrayList<>();
			currentBlockNode = node;
		}
		if (node.getBlockStatements().size() == 0) {
			push(new EmptyControlGraphNode(node, this));
		}
	}

	// private SequenceNode builtSequenceNode;
	// private SequenceNode rootSequenceNode;

	/*private void appendInSequence(ControlGraphNode<?, ?> n, boolean isLast) {
	
		System.out.println("************ On ajoute en sequence " + n.getASTNode());
		System.out.println("builtSequenceNode=" + builtSequenceNode);
	
		if (isLast) {
			builtSequenceNode.addToChildren(n);
			builtSequenceNode.getModelObject().setControlGraph2(n.getModelObject());
		}
		else {
			SequenceNode newSequenceNode = new SequenceNode(currentBlockNode, this);
			newSequenceNode.addToChildren(n);
			newSequenceNode.getModelObject().setControlGraph1(n.getModelObject());
			if (builtSequenceNode == null) {
				rootSequenceNode = newSequenceNode;
			}
			else {
				builtSequenceNode.addToChildren(newSequenceNode);
				builtSequenceNode.getModelObject().setControlGraph2(newSequenceNode.getModelObject());
			}
			builtSequenceNode = newSequenceNode;
		}
	
	}*/

	@Override
	public void outABlock(ABlock node) {
		super.outABlock(node);
		if (node.getBlockStatements().size() > 1) {
			System.out.println("IL faut maintenant gerer " + currentSequenceNodes);
			SequenceNode builtSequenceNode = null;
			SequenceNode rootSequenceNode = null;
			if (currentSequenceNodes.size() == node.getBlockStatements().size()) {
				ControlGraphNode<?, ?> lastNode = currentSequenceNodes.get(currentSequenceNodes.size() - 1);
				for (int i = 0; i < currentSequenceNodes.size(); i++) {
					ControlGraphNode<?, ?> n = currentSequenceNodes.get(i);
					if (i == currentSequenceNodes.size() - 1) {
						builtSequenceNode.addToChildren(n);
						builtSequenceNode.getModelObject().setControlGraph2(n.getModelObject());
					}
					else {
						SequenceNode newSequenceNode = new SequenceNode(currentBlockNode, n, lastNode, this);
						newSequenceNode.addToChildren(n);
						newSequenceNode.getModelObject().setControlGraph1(n.getModelObject());
						if (builtSequenceNode == null) {
							rootSequenceNode = newSequenceNode;
						}
						else {
							builtSequenceNode.addToChildren(newSequenceNode);
							builtSequenceNode.getModelObject().setControlGraph2(newSequenceNode.getModelObject());
						}
						builtSequenceNode = newSequenceNode;
					}
				}
				currentBlockNode = null;
				push(rootSequenceNode);
				pop();
			}
			else {
				logger.warning("Expecting to find " + node.getBlockStatements().size() + " statements but having parsed only "
						+ currentSequenceNodes.size() + ". Aborting");
				System.err.println("currentBlockNode=" + currentBlockNode);
				currentBlockNode = null;
			}
		}
		if (node.getBlockStatements().size() == 0) {
			pop();
		}
		currentBlockNode = null;
	}

	@Override
	protected void push(FMLObjectNode<?, ?, ?> fmlNode) {
		if (fmlNode instanceof ControlGraphNode) {
			if (currentBlockNode != null) {
				System.out.println("*************** Tiens on cree " + fmlNode.getASTNode() + " dans " + currentBlockNode + " current="
						+ getCurrentNode());
				currentSequenceNodes.add((ControlGraphNode<?, ?>) fmlNode);
			}
			else {
				if (rootControlGraphNode == null) {
					rootControlGraphNode = (ControlGraphNode<?, ?>) fmlNode;
				}
				super.push(fmlNode);
			}
		}
		else {
			super.push(fmlNode);
		}
		// analyzer.push(fmlNode);
	}

	@Override
	protected <N extends FMLObjectNode<?, ?, ?>> N pop() {
		if (currentBlockNode == null) {
			return super.pop();
		}
		return null;
	}

	/*
	 * <pre>
	 * block_statement =
	 *	   {variable_declaration} local_variable_declaration_statement |
	 *	   {statement}            statement;
	 * </pre>
	 */

	@Override
	public void inALocalVariableDeclarationStatement(ALocalVariableDeclarationStatement node) {
		// TODO Auto-generated method stub
		super.inALocalVariableDeclarationStatement(node);
		push(new DeclarationActionNode(node, this));
	}

	@Override
	public void outALocalVariableDeclarationStatement(ALocalVariableDeclarationStatement node) {
		super.outALocalVariableDeclarationStatement(node);
		pop();
	}

	/*
	 * <pre>
	 *  statement =
	 *      {no_trail}   statement_without_trailing_substatement |
	 *      {if}         if_then_statement |
	 *      {if_else}    if_then_else_statement |
	 *      {while_loop} while_statement |
	 *      {for_loop}   for_statement;
	 * </pre>
	 */

	@Override
	public void inAIfStatement(AIfStatement node) {
		// TODO Auto-generated method stub
		super.inAIfStatement(node);
		System.out.println("Nouvelle conditionnelle avec " + node);
	}

	@Override
	public void inAIfElseStatement(AIfElseStatement node) {
		// TODO Auto-generated method stub
		super.inAIfElseStatement(node);
	}

	@Override
	public void inAWhileLoopStatement(AWhileLoopStatement node) {
		// TODO Auto-generated method stub
		super.inAWhileLoopStatement(node);
	}

	@Override
	public void inAForLoopStatement(AForLoopStatement node) {
		// TODO Auto-generated method stub
		super.inAForLoopStatement(node);
	}

	/*
	 * <pre>
	 * statement_without_trailing_substatement =
	 *     {block}                  block |
	 *     {empty_statement}        empty_statement |
	 *     {expression_statement}   expression_statement |
	 *     {do_statement}           do_statement |
	 *     {return_statement}       return_statement;
	 * </pre>
	 */

	@Override
	public void inAEmptyStatement(AEmptyStatement node) {
		// TODO Auto-generated method stub
		super.inAEmptyStatement(node);
	}

	@Override
	public void inADoStatementStatementWithoutTrailingSubstatement(ADoStatementStatementWithoutTrailingSubstatement node) {
		// TODO Auto-generated method stub
		super.inADoStatementStatementWithoutTrailingSubstatement(node);
	}

	@Override
	public void inAReturnStatementStatementWithoutTrailingSubstatement(AReturnStatementStatementWithoutTrailingSubstatement node) {
		super.inAReturnStatementStatementWithoutTrailingSubstatement(node);
		push(new ReturnStatementNode(node.getReturnStatement(), this));
	}

	@Override
	public void outAReturnStatementStatementWithoutTrailingSubstatement(AReturnStatementStatementWithoutTrailingSubstatement node) {
		super.outAReturnStatementStatementWithoutTrailingSubstatement(node);
		pop();
	}

	/*
	 * <pre>
	 *  expression_statement =
	 *       statement_expression semi;
	 *	
	 *	statement_expression =
	 *        {assignment}             assignment |
	 *        {pre_increment}          pre_increment_expression |
	 *        {pre_decrement}          pre_decrement_expression |
	 *        {post_increment}         post_increment_expression |
	 *        {post_decrement}         post_decrement_expression |
	 *        {method_invocation}      method_invocation |
	 *        {fml_action_expression}  fml_action_expression;
	 * </pre>
	 */

	@Override
	public void inAAssignmentStatementExpression(AAssignmentStatementExpression node) {
		// TODO Auto-generated method stub
		super.inAAssignmentStatementExpression(node);
		push(new AssignationActionNode(node, this));
	}

	@Override
	public void outAAssignmentStatementExpression(AAssignmentStatementExpression node) {
		super.outAAssignmentStatementExpression(node);
		pop();
	}

	@Override
	public void inAMethodInvocationStatementExpression(AMethodInvocationStatementExpression node) {
		// TODO Auto-generated method stub
		super.inAMethodInvocationStatementExpression(node);
	}

	@Override
	public void inAFmlActionExpressionStatementExpression(AFmlActionExpressionStatementExpression node) {
		// TODO Auto-generated method stub
		super.inAFmlActionExpressionStatementExpression(node);
	}

	/*
	 * <pre>
	 * assignment_expression =
	 *    {conditional} conditional_expression |
	 *    {assignment}  assignment ;
	 * </pre>
	 */

	/*@Override
	public void inAConditionalAssignmentExpression(AConditionalAssignmentExpression node) {
		super.inAConditionalAssignmentExpression(node);
		System.out.println("-----> YES-1 je choppe " + node + " of "+node.getClass().getSimpleName());
	}
	
	@Override
	public void inAAssignmentAssignmentExpression(AAssignmentAssignmentExpression node) {
		// TODO Auto-generated method stub
		super.inAAssignmentAssignmentExpression(node);
		System.out.println("-----> YES-2 je choppe " + node+ " of "+node.getClass().getSimpleName());
	}*/

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

}
