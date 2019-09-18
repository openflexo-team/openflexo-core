package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ConditionalNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FetchRequestNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.LogActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.fml.parser.node.AAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ADoStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AEmptyStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AFmlActionExpressionStatementExpression;
import org.openflexo.foundation.fml.parser.node.AForBasicExpressionStatement;
import org.openflexo.foundation.fml.parser.node.AForBasicStatement;
import org.openflexo.foundation.fml.parser.node.AForEnhancedStatement;
import org.openflexo.foundation.fml.parser.node.AIfElseStatement;
import org.openflexo.foundation.fml.parser.node.AIfSimpleStatement;
import org.openflexo.foundation.fml.parser.node.ALogActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AReturnEmptyStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AReturnStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.ASelectActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationBlockStatement;
import org.openflexo.foundation.fml.parser.node.AWhileStatement;
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

	/*public static ControlGraphNode<?, ?> makeControlGraphNode(Node cgNode, MainSemanticsAnalyzer analyzer) {
		ControlGraphFactory f = new ControlGraphFactory(cgNode, analyzer);
		cgNode.apply(f);
		return f.rootControlGraphNode;
	}*/

	public ControlGraphFactory(Node cgNode, MainSemanticsAnalyzer analyzer) {
		super(analyzer.getFactory(), cgNode);
		this.mainAnalyzer = analyzer;
	}

	public ControlGraphNode<?, ?> makeControlGraphNode() {
		getRootNode().apply(this);
		return rootControlGraphNode;
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
	public void inAVariableDeclarationBlockStatement(AVariableDeclarationBlockStatement node) {
		super.inAVariableDeclarationBlockStatement(node);
		push(new DeclarationActionNode(node, this));
	}

	@Override
	public void outAVariableDeclarationBlockStatement(AVariableDeclarationBlockStatement node) {
		super.outAVariableDeclarationBlockStatement(node);
		pop();
	}

	/*
	 * <pre>
	 *  statement =
	 *      {no_trail}   statement_without_trailing_substatement
	 *    // if statements
	 *    | {if_simple} kw_if l_par expression r_par statement
	 *    | {if_else} kw_if l_par expression r_par statement_no_short_if kw_else statement
	 *    // while statement
	 *    | {while} kw_while l_par expression r_par statement
	 *    // for statement
	 *    | {for_basic}      kw_for l_par for_init? [semi1]:semi [semi2]:semi statement_expression? r_par statement
	 *    | {for_basic_expression} kw_for l_par for_init? [semi1]:semi expression [semi2]:semi statement_expression? r_par statement
	 *    | {for_enhanced} kw_for l_par type identifier colon expression r_par statement
	 *    ;
	 * </pre>
	 */

	@Override
	public void inAIfSimpleStatement(AIfSimpleStatement node) {
		super.inAIfSimpleStatement(node);
		push(new ConditionalNode(node, this));
	}

	@Override
	public void outAIfSimpleStatement(AIfSimpleStatement node) {
		super.outAIfSimpleStatement(node);
		pop();
	}

	@Override
	public void inAIfElseStatement(AIfElseStatement node) {
		super.inAIfElseStatement(node);
		push(new ConditionalNode(node, this));
	}

	@Override
	public void outAIfElseStatement(AIfElseStatement node) {
		super.outAIfElseStatement(node);
		pop();
	}

	@Override
	public void inAWhileStatement(AWhileStatement node) {
		super.inAWhileStatement(node);
	}

	@Override
	public void inAForBasicStatement(AForBasicStatement node) {
		// TODO Auto-generated method stub
		super.inAForBasicStatement(node);
	}

	@Override
	public void inAForBasicExpressionStatement(AForBasicExpressionStatement node) {
		// TODO Auto-generated method stub
		super.inAForBasicExpressionStatement(node);
	}

	@Override
	public void inAForEnhancedStatement(AForEnhancedStatement node) {
		// TODO Auto-generated method stub
		super.inAForEnhancedStatement(node);
	}

	/*
	 * <pre>
	 *   statement_without_trailing_substatement =
	 *       {block}                l_brc [block_statements]:block_statement* r_brc
	 *     | {empty_statement}      semi
	 *     | {expression_statement} statement_expression semi
	 *     | {do_statement}         kw_do statement kw_while l_par expression r_par semi
	 *     // return statement
	 *     | {return_empty}      kw_return semi
	 *     | {return} kw_return expression semi
	 *     ;
	 * </pre>
	 */

	@Override
	public void inAEmptyStatementStatementWithoutTrailingSubstatement(AEmptyStatementStatementWithoutTrailingSubstatement node) {
		super.inAEmptyStatementStatementWithoutTrailingSubstatement(node);
	}

	@Override
	public void inADoStatementStatementWithoutTrailingSubstatement(ADoStatementStatementWithoutTrailingSubstatement node) {
		super.inADoStatementStatementWithoutTrailingSubstatement(node);
	}

	@Override
	public void inAReturnStatementWithoutTrailingSubstatement(AReturnStatementWithoutTrailingSubstatement node) {
		super.inAReturnStatementWithoutTrailingSubstatement(node);
		push(new ReturnStatementNode(node, this));
	}

	@Override
	public void outAReturnStatementWithoutTrailingSubstatement(AReturnStatementWithoutTrailingSubstatement node) {
		super.outAReturnStatementWithoutTrailingSubstatement(node);
		pop();
	}

	@Override
	public void inAReturnEmptyStatementWithoutTrailingSubstatement(AReturnEmptyStatementWithoutTrailingSubstatement node) {
		super.inAReturnEmptyStatementWithoutTrailingSubstatement(node);
		push(new EmptyReturnStatementNode(node, this));
	}

	@Override
	public void outAReturnEmptyStatementWithoutTrailingSubstatement(AReturnEmptyStatementWithoutTrailingSubstatement node) {
		super.outAReturnEmptyStatementWithoutTrailingSubstatement(node);
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

	@Override
	public void inALogActionFmlActionExp(ALogActionFmlActionExp node) {
		super.inALogActionFmlActionExp(node);
		push(new LogActionNode(node, this));
	}

	@Override
	public void outALogActionFmlActionExp(ALogActionFmlActionExp node) {
		super.outALogActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inASelectActionFmlActionExp(ASelectActionFmlActionExp node) {
		super.inASelectActionFmlActionExp(node);
		push(new FetchRequestNode(node, this));
	}

	@Override
	public void outASelectActionFmlActionExp(ASelectActionFmlActionExp node) {
		super.outASelectActionFmlActionExp(node);
		pop();
	}

}
