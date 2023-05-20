package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BeginMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ConditionalNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeleteActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EndMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FMLEditionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FetchRequestNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FireEventNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.IterationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.LogActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.MatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.NotifyActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.WhileActionNode;
import org.openflexo.foundation.fml.parser.node.AAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.ABeginMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ABlockStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.ADeleteActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.ADoStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AEmptyStatementStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AEndMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AFireActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AForBasicExpressionStatement;
import org.openflexo.foundation.fml.parser.node.AForBasicStatement;
import org.openflexo.foundation.fml.parser.node.AForEnhancedExpressionStatement;
import org.openflexo.foundation.fml.parser.node.AForEnhancedFmlActionStatement;
import org.openflexo.foundation.fml.parser.node.AIfElseStatement;
import org.openflexo.foundation.fml.parser.node.AIfSimpleStatement;
import org.openflexo.foundation.fml.parser.node.ALogActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AMatchActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.ANewInstanceStatementExpression;
import org.openflexo.foundation.fml.parser.node.ANoTrailStatement;
import org.openflexo.foundation.fml.parser.node.ANotifyActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AReturnEmptyStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.AReturnValueStatementWithoutTrailingSubstatement;
import org.openflexo.foundation.fml.parser.node.ASelectActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.ATaEditionActionFmlActionExp;
import org.openflexo.foundation.fml.parser.node.AVariableDeclarationBlockStatement;
import org.openflexo.foundation.fml.parser.node.AWhileStatement;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PBlockStatement;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.foundation.fml.parser.node.PStatement;
import org.openflexo.foundation.fml.parser.node.PStatementNoShortIf;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate {@link FMLControlGraph} from AST
 * 
 * Such a factory works with a parent {@link FMLCompilationUnitSemanticsAnalyzer}
 * 
 * @author sylvain
 *
 */
/**
 * @author sylvainguerin
 *
 */
public class ControlGraphFactory extends FMLSemanticsAnalyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlGraphFactory.class.getPackage().getName());

	public boolean debug = false;

	private FMLCompilationUnitSemanticsAnalyzer compilationUnitAnalyzer;
	private ControlGraphNode<?, ?> rootControlGraphNode = null;

	public static ControlGraphNode<?, ?> makeControlGraphNode(PFlexoBehaviourBody cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		return _makeControlGraphNode(cgNode, analyzer);
	}

	public static ControlGraphNode<?, ?> makeControlGraphNode(PStatement cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		return _makeControlGraphNode(cgNode, analyzer);
	}

	public static ControlGraphNode<?, ?> makeControlGraphNode(PStatementNoShortIf cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		return _makeControlGraphNode(cgNode, analyzer);
	}

	public static ControlGraphNode<?, ?> makeControlGraphNode(PExpression cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		return _makeControlGraphNode(cgNode, analyzer);
	}

	public static ControlGraphNode<?, ?> makeControlGraphNode(PFmlActionExp cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		return _makeControlGraphNode(cgNode, analyzer);
	}

	private static ControlGraphNode<?, ?> _makeControlGraphNode(Node cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {

		// Dont rebuild a new ControlGraphNode when already existing
		ControlGraphNode<?, ?> alreadyExistingNode = (ControlGraphNode<?, ?>) analyzer.getFMLNode(cgNode);
		if (alreadyExistingNode != null) {
			return alreadyExistingNode;
		}

		// OK, we have to build it
		ControlGraphFactory f = new ControlGraphFactory(cgNode, analyzer);
		cgNode.apply(f);
		if (f.rootControlGraphNode == null) {
			f.rootControlGraphNode = new ExpressionActionNode(cgNode, analyzer);
		}

		// Register this new node
		analyzer.registerFMLNode(cgNode, f.rootControlGraphNode);
		// A special case to register both for ANoTrailStatement and AStatementWithoutTrailingSubstatement
		if (cgNode instanceof ANoTrailStatement) {
			analyzer.registerFMLNode(((ANoTrailStatement) cgNode).getStatementWithoutTrailingSubstatement(), f.rootControlGraphNode);
		}

		return f.rootControlGraphNode;
	}

	private ControlGraphFactory(Node cgNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(analyzer.getModelFactory(), cgNode);
		this.compilationUnitAnalyzer = analyzer;
	}

	@Override
	public FMLCompilationUnitSemanticsAnalyzer getCompilationUnitAnalyzer() {
		return compilationUnitAnalyzer;
	}

	@Override
	public FMLCompilationUnit getCompilationUnit() {
		return getCompilationUnitAnalyzer().getCompilationUnit();
	}

	@Override
	public AbstractFMLTypingSpace getTypingSpace() {
		return getCompilationUnitAnalyzer().getTypingSpace();
	}

	@Override
	public FMLBindingFactory getFMLBindingFactory() {
		return getCompilationUnitAnalyzer().getFMLBindingFactory();
	}

	@Override
	public FragmentManager getFragmentManager() {
		return getCompilationUnitAnalyzer().getFragmentManager();
	}

	@Override
	public RawSource getRawSource() {
		return getCompilationUnitAnalyzer().getRawSource();
	}

	@Override
	public <N extends Node, FMLN extends ObjectNode<?, ?, ?>> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function) {
		return getCompilationUnitAnalyzer().retrieveFMLNode(astNode, function);
	}

	@Override
	public void throwIssue(Object modelObject, String errorMessage, RawSourceFragment fragment, RawSourcePosition startPosition) {
		getCompilationUnitAnalyzer().throwIssue(modelObject, errorMessage, fragment, startPosition);
	}

	@Override
	public List<SemanticAnalysisIssue> getSemanticAnalysisIssues() {
		return getCompilationUnitAnalyzer().getSemanticAnalysisIssues();
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

	private Stack<BlockSequenceInfo> blocks = new Stack<>();

	/**
	 * Internal data structure used to manage alignment between Block sequences obtained from parsing, the {@link Sequence} FML structure
	 * (chained Sequence objects)
	 * 
	 * @author sylvain
	 *
	 */
	class BlockSequenceInfo {

		private Node currentBlockNode; // Either ABlock or ABlockStatementWithoutTrailingSubstatement
		private List<ControlGraphNode<?, ?>> currentSequenceNodes;
		private List<PBlockStatement> expectedBlockStatements;
		private List<PBlockStatement> initialBlockStatements;

		BlockSequenceInfo(ABlock blockNode) {
			currentBlockNode = blockNode;
			currentSequenceNodes = new ArrayList<>();
			initialBlockStatements = new ArrayList<>(blockNode.getBlockStatements());
			expectedBlockStatements = new ArrayList<>(blockNode.getBlockStatements());
			/*System.out.println("-------------------> AU DEBUT:");
			for (PBlockStatement pBlockStatement : expectedBlockStatements) {
				System.out.println("   > " + pBlockStatement);
			}*/
		}

		BlockSequenceInfo(ABlockStatementWithoutTrailingSubstatement blockNode) {
			currentBlockNode = blockNode;
			currentSequenceNodes = new ArrayList<>();
			initialBlockStatements = new ArrayList<>(blockNode.getBlockStatements());
			expectedBlockStatements = new ArrayList<>(blockNode.getBlockStatements());
			/*System.out.println("-------------------> AU DEBUT:");
			for (PBlockStatement pBlockStatement : expectedBlockStatements) {
				System.out.println("   > " + pBlockStatement);
			}*/
		}

		boolean handleNode(ControlGraphNode<?, ?> controlGraphNode) {
			// System.out.println("Handle node with " + controlGraphNode.getASTNode());

			PBlockStatement matchedBlockStatement = matchExpectedBlockStatements(controlGraphNode.getASTNode());
			if (matchedBlockStatement != null) {
				expectedBlockStatements.remove(matchedBlockStatement);
				// System.out.println("Handle node " + controlGraphNode + " for " + controlGraphNode.getASTNode());
				currentSequenceNodes.add(controlGraphNode);
				return true;
			}
			// else {
			/*System.out.println("expectedBlockStatements:");
			for (PBlockStatement pBlockStatement : expectedBlockStatements) {
				System.out.println("   > " + pBlockStatement);
			}*/
			// }
			return false;
		}

		private PBlockStatement matchExpectedBlockStatements(Node n) {
			if (n == null) {
				return null;
			}
			if (n == getRootNode()) {
				return null;
			}
			if (expectedBlockStatements.contains(n)) {
				return (PBlockStatement) n;
			}
			return matchExpectedBlockStatements(n.parent());
		}

		void finalizeBlockStatements() {
			// System.out.println("finalizeBlockStatements() for " + currentSequenceNodes);
			SequenceNode builtSequenceNode = null;
			SequenceNode rootSequenceNode = null;
			if (currentSequenceNodes.size() == initialBlockStatements.size()) {
				ControlGraphNode<?, ?> lastNode = currentSequenceNodes.get(currentSequenceNodes.size() - 1);
				for (int i = 0; i < currentSequenceNodes.size(); i++) {
					ControlGraphNode<?, ?> n = currentSequenceNodes.get(i);
					if (i == currentSequenceNodes.size() - 1) {
						builtSequenceNode.addToChildren(n);
						builtSequenceNode.getModelObject().setControlGraph2(n.getModelObject());
					}
					else {
						SequenceNode newSequenceNode = new SequenceNode(currentBlockNode, n, lastNode, getCompilationUnitAnalyzer());

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
				// System.out.println("finalizeBlockStatements() DONE for "+currentBlockNode+" of "+currentBlockNode.getClass());
				getCompilationUnitAnalyzer().registerFMLNode(currentBlockNode, rootSequenceNode);
				currentBlockNode = null;
				push(rootSequenceNode);
				pop();
			}
			else {
				logger.warning("Expecting to find " + initialBlockStatements.size() + " statements but having parsed only "
						+ currentSequenceNodes.size() + ". Aborting");
				System.err.println("currentBlockNode=" + currentBlockNode);
				currentBlockNode = null;
			}

		}
	}

	@Override
	public void inABlock(ABlock node) {
		super.inABlock(node);
		// System.out.println("Nouveau block de " + node.getBlockStatements().size() + " statements " + " avec " + node);
		ObjectNode<?, ?, ?> alreadyExisting = getCompilationUnitAnalyzer().getFMLNode(node);
		// Don't handle again, this is already registered
		if (alreadyExisting == null) {
			// This block was not handled yet, initialize its computing
			if (node.getBlockStatements().size() > 1) {
				BlockSequenceInfo bsInfo = new BlockSequenceInfo(node);
				blocks.push(bsInfo);
			}
			if (node.getBlockStatements().size() == 0) {
				push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new EmptyControlGraphNode(n, getCompilationUnitAnalyzer())));
			}
		}
	}

	@Override
	public void outABlock(ABlock node) {
		super.outABlock(node);
		if (!blocks.isEmpty() && blocks.peek().currentBlockNode == node) {
			// This block was handled during this visit, finalizes it now
			if (node.getBlockStatements().size() > 1) {
				blocks.pop().finalizeBlockStatements();
			}
			if (node.getBlockStatements().size() == 0) {
				pop();
			}
		}
	}

	@Override
	public void inABlockStatementWithoutTrailingSubstatement(ABlockStatementWithoutTrailingSubstatement node) {
		super.inABlockStatementWithoutTrailingSubstatement(node);
		// System.out.println("2 -Nouveau block de " + node.getBlockStatements().size() + " statements " + " avec " + node);
		ObjectNode<?, ?, ?> alreadyExisting = getCompilationUnitAnalyzer().getFMLNode(node);
		// Don't handle again, this is already registered
		if (alreadyExisting == null) {
			// This block was not handled yet, initialize its computing
			if (node.getBlockStatements().size() > 1) {
				BlockSequenceInfo bsInfo = new BlockSequenceInfo(node);
				blocks.push(bsInfo);
			}
			if (node.getBlockStatements().size() == 0) {
				push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new EmptyControlGraphNode(n, getCompilationUnitAnalyzer())));
			}
		}

	}

	@Override
	public void outABlockStatementWithoutTrailingSubstatement(ABlockStatementWithoutTrailingSubstatement node) {
		super.outABlockStatementWithoutTrailingSubstatement(node);
		if (!blocks.isEmpty() && blocks.peek().currentBlockNode == node) {
			// This block was handled during this visit, finalizes it now
			if (node.getBlockStatements().size() > 1) {
				blocks.pop().finalizeBlockStatements();
			}
			if (node.getBlockStatements().size() == 0) {
				pop();
			}
		}
	}

	// Stores current ControlGraphNode beeing deserialized
	// It is a little bit tricky :
	// If we are deserializing a sequence, nodes stack is not consistent
	// and peek() may return null value for children nodes.
	// To be able to reference parent node, we use that variable
	// TODO: may be we should use a stack instead ????
	private ControlGraphNode currentCGNode;

	@Override
	protected void push(ObjectNode<?, ?, ?> fmlNode) {
		// System.out.println("PUSH " + fmlNode);
		if (fmlNode instanceof ControlGraphNode) {

			if (!blocks.isEmpty()) {
				BlockSequenceInfo bsInfo = blocks.peek();
				bsInfo.handleNode((ControlGraphNode<?, ?>) fmlNode);
				currentCGNode = (ControlGraphNode) fmlNode;
			}
			else {
				if (rootControlGraphNode == null) {
					rootControlGraphNode = (ControlGraphNode<?, ?>) fmlNode;
					// System.out.println("SET rootControlGraphNode with " + fmlNode);
					// Thread.dumpStack();
				}
				super.push(fmlNode);
				currentCGNode = null;
			}

		}
		else {
			if (peek() == null && currentCGNode != null) {
				currentCGNode.addToChildren(fmlNode);
			}
			super.push(fmlNode);
		}
	}

	@Override
	protected <N extends ObjectNode<?, ?, ?>> N pop() {
		ObjectNode<?, ?, ?> peek = peek();
		if (peek != null && !(peek instanceof ControlGraphNode)) {
			return super.pop();
		}
		if (blocks.isEmpty()) {
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
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new DeclarationActionNode(n, getCompilationUnitAnalyzer())));
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
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new ConditionalNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAIfSimpleStatement(AIfSimpleStatement node) {
		super.outAIfSimpleStatement(node);
		pop();
	}

	@Override
	public void inAIfElseStatement(AIfElseStatement node) {
		super.inAIfElseStatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new ConditionalNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAIfElseStatement(AIfElseStatement node) {
		super.outAIfElseStatement(node);
		pop();
	}

	@Override
	public void inAWhileStatement(AWhileStatement node) {
		super.inAWhileStatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new WhileActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAWhileStatement(AWhileStatement node) {
		super.outAWhileStatement(node);
		pop();
	}

	@Override
	public void inAForBasicStatement(AForBasicStatement node) {
		super.inAForBasicStatement(node);
		logger.warning("AForBasicStatement not implemented YET");
		// See TestIterations.fml, l9
	}

	@Override
	public void outAForBasicStatement(AForBasicStatement node) {
		super.outAForBasicStatement(node);
	}

	@Override
	public void inAForBasicExpressionStatement(AForBasicExpressionStatement node) {
		super.inAForBasicExpressionStatement(node);
		logger.warning("AForBasicExpressionStatement not implemented YET");
		// See TestIterations.fml, l16
	}

	@Override
	public void outAForBasicExpressionStatement(AForBasicExpressionStatement node) {
		super.outAForBasicExpressionStatement(node);
	}

	@Override
	public void inAForEnhancedExpressionStatement(AForEnhancedExpressionStatement node) {
		super.inAForEnhancedExpressionStatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new IterationActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAForEnhancedExpressionStatement(AForEnhancedExpressionStatement node) {
		super.outAForEnhancedExpressionStatement(node);
		pop();
	}

	@Override
	public void inAForEnhancedFmlActionStatement(AForEnhancedFmlActionStatement node) {
		super.inAForEnhancedFmlActionStatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new IterationActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAForEnhancedFmlActionStatement(AForEnhancedFmlActionStatement node) {
		super.outAForEnhancedFmlActionStatement(node);
		pop();
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

	/*
	 * <pre>
	 *  return_value_statement = 
	 *      {expression}   kw_return expression semi
	 *    | {fml_action}   kw_return fml_action_exp semi
	 *    ;
	 * </pre>
	 */

	@Override
	public void inAReturnValueStatementWithoutTrailingSubstatement(AReturnValueStatementWithoutTrailingSubstatement node) {
		super.inAReturnValueStatementWithoutTrailingSubstatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node.getReturnValueStatement(),
				n -> new ReturnStatementNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAReturnValueStatementWithoutTrailingSubstatement(AReturnValueStatementWithoutTrailingSubstatement node) {
		super.outAReturnValueStatementWithoutTrailingSubstatement(node);
		pop();
	}

	/*@Override
	public void inAReturnStatementWithoutTrailingSubstatement(AReturnStatementWithoutTrailingSubstatement node) {
		super.inAReturnStatementWithoutTrailingSubstatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new ReturnStatementNode(n, getCompilationUnitAnalyzer())));
	}
	
	@Override
	public void outAReturnStatementWithoutTrailingSubstatement(AReturnStatementWithoutTrailingSubstatement node) {
		super.outAReturnStatementWithoutTrailingSubstatement(node);
		pop();
	}*/

	@Override
	public void inAReturnEmptyStatementWithoutTrailingSubstatement(AReturnEmptyStatementWithoutTrailingSubstatement node) {
		super.inAReturnEmptyStatementWithoutTrailingSubstatement(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new EmptyReturnStatementNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAReturnEmptyStatementWithoutTrailingSubstatement(AReturnEmptyStatementWithoutTrailingSubstatement node) {
		super.outAReturnEmptyStatementWithoutTrailingSubstatement(node);
		pop();
	}

	/*	 
	 * 	 <pre>  
	 *   statement_expression =
	 *	       {assignment}             assignment_statement_expression
	 *	     | {pre_increment}          pre_incr_exp
	 *	     | {pre_decrement}          pre_decr_exp
	 *	     | {post_increment}         post_incr_exp
	 *	     | {post_decrement}         post_decr_exp
	 *	     | {method_invocation}      method_invocation
	 *	     | {new_instance}           new_instance
	 *	     | {fml_action_expression}  fml_action_exp
	 *	     ;
	 *
	 *	   assignment_statement_expression =
	 *	       {expression}  [left]:left_hand_side assignment_operator [right]:expression
	 *	     | {fml_action}  [left]:left_hand_side assignment_operator [right]:fml_action_exp
	 *	     ;
	 * </pre>
	 */
	@Override
	public void inAAssignmentStatementExpression(AAssignmentStatementExpression node) {
		super.inAAssignmentStatementExpression(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node.getAssignmentStatementExpression(),
				n -> new AssignationActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAAssignmentStatementExpression(AAssignmentStatementExpression node) {
		super.outAAssignmentStatementExpression(node);
		pop();
	}

	@Override
	public void inANewInstanceStatementExpression(ANewInstanceStatementExpression node) {
		super.inANewInstanceStatementExpression(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new ExpressionActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outANewInstanceStatementExpression(ANewInstanceStatementExpression node) {
		super.outANewInstanceStatementExpression(node);
		pop();
	}

	@Override
	public void inAMethodInvocationStatementExpression(AMethodInvocationStatementExpression node) {
		super.inAMethodInvocationStatementExpression(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new ExpressionActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAMethodInvocationStatementExpression(AMethodInvocationStatementExpression node) {
		super.outAMethodInvocationStatementExpression(node);
		pop();
	}

	@Override
	public void inATaEditionActionFmlActionExp(ATaEditionActionFmlActionExp node) {
		super.inATaEditionActionFmlActionExp(node);
		// System.out.println(">>>>> FMLEditionActionNode - ENTER in " + peek() + " with " + node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new FMLEditionActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outATaEditionActionFmlActionExp(ATaEditionActionFmlActionExp node) {
		super.outATaEditionActionFmlActionExp(node);
		// System.out.println("<<<<< FMLEditionActionNode - EXIT from " + peek() + " with " + node);
		pop();
	}

	@Override
	public void inADeleteActionFmlActionExp(ADeleteActionFmlActionExp node) {
		super.inADeleteActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new DeleteActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outADeleteActionFmlActionExp(ADeleteActionFmlActionExp node) {
		super.outADeleteActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inALogActionFmlActionExp(ALogActionFmlActionExp node) {
		super.inALogActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new LogActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outALogActionFmlActionExp(ALogActionFmlActionExp node) {
		super.outALogActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inAFireActionFmlActionExp(AFireActionFmlActionExp node) {
		super.inAFireActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new FireEventNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAFireActionFmlActionExp(AFireActionFmlActionExp node) {
		super.outAFireActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inANotifyActionFmlActionExp(ANotifyActionFmlActionExp node) {
		super.inANotifyActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new NotifyActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outANotifyActionFmlActionExp(ANotifyActionFmlActionExp node) {
		super.outANotifyActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inASelectActionFmlActionExp(ASelectActionFmlActionExp node) {
		super.inASelectActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new FetchRequestNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outASelectActionFmlActionExp(ASelectActionFmlActionExp node) {
		super.outASelectActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inABeginMatchActionFmlActionExp(ABeginMatchActionFmlActionExp node) {
		super.inABeginMatchActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new BeginMatchActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outABeginMatchActionFmlActionExp(ABeginMatchActionFmlActionExp node) {
		super.outABeginMatchActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inAMatchActionFmlActionExp(AMatchActionFmlActionExp node) {
		super.inAMatchActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new MatchActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAMatchActionFmlActionExp(AMatchActionFmlActionExp node) {
		super.outAMatchActionFmlActionExp(node);
		pop();
	}

	@Override
	public void inAEndMatchActionFmlActionExp(AEndMatchActionFmlActionExp node) {
		super.inAEndMatchActionFmlActionExp(node);
		push(getCompilationUnitAnalyzer().retrieveFMLNode(node, n -> new EndMatchActionNode(n, getCompilationUnitAnalyzer())));
	}

	@Override
	public void outAEndMatchActionFmlActionExp(AEndMatchActionFmlActionExp node) {
		super.outAEndMatchActionFmlActionExp(node);
		pop();
	}

}
