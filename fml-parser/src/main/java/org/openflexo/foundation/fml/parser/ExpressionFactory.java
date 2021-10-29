package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AndExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AssignmentExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BitwiseAndExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BitwiseComplementExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BitwiseOrExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BitwiseXOrExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.CharConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DivisionExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.EqualsExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.FMLCastExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.FMLConditionalExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.FMLInstanceOfExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.FalseConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.FloatingPointConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.GreaterThanExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.GreaterThanOrEqualsExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.IntegerConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.LessThanExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.LessThanOrEqualsExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MinusExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ModExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MultiplicationExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NotEqualsExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NotExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NullConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.OrExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PlusExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PostDecrementExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PostIncrementExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PreDecrementExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PreIncrementExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ShiftLeftExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ShiftRight2ExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ShiftRightExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.StringConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.TrueConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.UnaryMinusExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.UnaryPlusExpressionNode;
import org.openflexo.foundation.fml.parser.node.AAmpAmpConditionalAndExp;
import org.openflexo.foundation.fml.parser.node.AAmpAndExp;
import org.openflexo.foundation.fml.parser.node.AAssignmentExpression;
import org.openflexo.foundation.fml.parser.node.ABarBarConditionalOrExp;
import org.openflexo.foundation.fml.parser.node.ABarInclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ACaretExclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ACastUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ACharacterLiteral;
import org.openflexo.foundation.fml.parser.node.AEmarkUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.AEqEqualityExp;
import org.openflexo.foundation.fml.parser.node.AFalseLiteral;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AFloatingPointLiteral;
import org.openflexo.foundation.fml.parser.node.AGtRelationalExp;
import org.openflexo.foundation.fml.parser.node.AGteqRelationalExp;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.AInstanceofRelationalExp;
import org.openflexo.foundation.fml.parser.node.AIntegerLiteral;
import org.openflexo.foundation.fml.parser.node.ALtRelationalExp;
import org.openflexo.foundation.fml.parser.node.ALteqRelationalExp;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AMinusAddExp;
import org.openflexo.foundation.fml.parser.node.AMinusUnaryExp;
import org.openflexo.foundation.fml.parser.node.ANeqEqualityExp;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewInstanceStatementExpression;
import org.openflexo.foundation.fml.parser.node.ANullLiteral;
import org.openflexo.foundation.fml.parser.node.APercentMultExp;
import org.openflexo.foundation.fml.parser.node.APlusAddExp;
import org.openflexo.foundation.fml.parser.node.APlusUnaryExp;
import org.openflexo.foundation.fml.parser.node.APostDecrExp;
import org.openflexo.foundation.fml.parser.node.APostIncrExp;
import org.openflexo.foundation.fml.parser.node.APreDecrExp;
import org.openflexo.foundation.fml.parser.node.APreIncrExp;
import org.openflexo.foundation.fml.parser.node.AQmarkConditionalExp;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AShlShiftExp;
import org.openflexo.foundation.fml.parser.node.AShrShiftExp;
import org.openflexo.foundation.fml.parser.node.ASlashMultExp;
import org.openflexo.foundation.fml.parser.node.AStarMultExp;
import org.openflexo.foundation.fml.parser.node.AStringLiteral;
import org.openflexo.foundation.fml.parser.node.ATildeUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ATrueLiteral;
import org.openflexo.foundation.fml.parser.node.AUshrShiftExp;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.RawSource;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate a {@link DataBinding} or an {@link Expression} from AST
 * 
 * @author sylvain
 *
 */
public class ExpressionFactory extends AbstractExpressionFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpressionFactory.class.getPackage().getName());

	public static Expression makeExpression(Node node, Bindable bindable, FMLCompilationUnit compilationUnit) {
		return _makeExpression(node, bindable, /*compilationUnit.getTypingSpace(),*/ compilationUnit.getFMLModelFactory(), null, null);
	}

	public static Expression makeExpression(Node node, Bindable bindable, FMLSemanticsAnalyzer parentAnalyzer, DataBindingNode parentNode) {
		return _makeExpression(node, bindable, /*parentAnalyzer.getTypingSpace(),*/ parentAnalyzer.getModelFactory(), parentAnalyzer,
				parentNode);
	}

	public static Expression makeExpression(Node node, Bindable bindable, AbstractFMLTypingSpace typingSpace, FMLModelFactory modelFactory,
			RawSource rawSource) {
		FMLCompilationUnitSemanticsAnalyzer localAnalyzer = new FMLCompilationUnitSemanticsAnalyzer(modelFactory, node, rawSource);
		localAnalyzer.setTypingSpace(typingSpace);
		DataBindingNode dataBindingNode = localAnalyzer.retrieveFMLNode(node,
				n -> new DataBindingNode(n, bindable, BindingDefinitionType.GET, Object.class, localAnalyzer));
		return _makeExpression(node, bindable, /*typingSpace,*/ modelFactory, localAnalyzer, dataBindingNode);
	}

	public static <T> DataBinding<T> makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, FMLCompilationUnit compilationUnit) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, compilationUnit.getTypingSpace(),
				compilationUnit.getFMLModelFactory(), null, null);
	}

	/*public static <T> DataBinding<T> makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, AbstractFMLTypingSpace typingSpace, FMLModelFactory modelFactory) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, typingSpace, modelFactory, null, null);
	}*/

	public static <T> DataBinding<T> makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, FMLCompilationUnitSemanticsAnalyzer mainAnalyzer, ObjectNode<?, ?, ?> parentNode) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, mainAnalyzer.getTypingSpace(),
				mainAnalyzer.getModelFactory(), mainAnalyzer, parentNode);
	}

	private static Expression _makeExpression(Node node, Bindable bindable, /*AbstractFMLTypingSpace typingSpace,*/
			FMLModelFactory modelFactory, FMLSemanticsAnalyzer parentAnalyzer, DataBindingNode dataBindingNode) {
		// return new DataBinding(analyzer.getText(node), bindable, expectedType, BindingDefinitionType.GET);

		ExpressionFactory factory = new ExpressionFactory(node, bindable, /*typingSpace,*/ modelFactory, parentAnalyzer, dataBindingNode);

		// System.out.println(">>>>>>> " + Integer.toHexString(factory.hashCode()) + ": Make expression for " + node);
		// Thread.dumpStack();
		// System.out.println("current: " + factory.fmlNodes.peek());
		// ASTDebugger.debug(node);

		factory.push(dataBindingNode);
		node.apply(factory);
		factory.pop();

		/*System.out.println("Hop, on retourne " + factory.getExpression());
		
		if (factory.getExpression().toString().equals("super.init")) {
			System.out.println("J'ai mon probleme");
			BindingValue bv = (BindingValue) factory.getExpression();
			System.out.println("Je fais " + bv + " avec " + node);
			ASTDebugger.debug(node);
			Thread.dumpStack();
			System.exit(-1);
		}*/

		return factory.getExpression();
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> DataBinding<T> _makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, AbstractFMLTypingSpace typingSpace, FMLModelFactory modelFactory, FMLSemanticsAnalyzer parentAnalyzer,
			ObjectNode<?, ?, ?> parentNode) {

		DataBindingNode dataBindingNode = parentAnalyzer.retrieveFMLNode(node,
				n -> new DataBindingNode(n, bindable, bindingDefinitionType, expectedType, parentAnalyzer));

		if (parentNode != null) {
			parentNode.addToChildren(dataBindingNode);
		}

		_makeExpression(node, bindable, /*typingSpace,*/ modelFactory, parentAnalyzer, dataBindingNode);

		return (DataBinding<T>) dataBindingNode.getModelObject();
	}

	private ExpressionFactory(Node rootNode, Bindable aBindable, /*AbstractFMLTypingSpace typingSpace,*/ FMLModelFactory fmlModelFactory,
			FMLSemanticsAnalyzer parentAnalyzer, DataBindingNode dataBindingNode) {
		super(rootNode, aBindable, /*typingSpace,*/ fmlModelFactory, parentAnalyzer, dataBindingNode);
	}

	@Override
	public void inAIdentifierLeftHandSide(AIdentifierLeftHandSide node) {
		super.inAIdentifierLeftHandSide(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAIdentifierLeftHandSide(AIdentifierLeftHandSide node) {
		super.outAIdentifierLeftHandSide(node);
		popBindingPathNode(node);
	}

	@Override
	public void inAFieldLeftHandSide(AFieldLeftHandSide node) {
		super.inAFieldLeftHandSide(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAFieldLeftHandSide(AFieldLeftHandSide node) {
		super.outAFieldLeftHandSide(node);
		popBindingPathNode(node);
	}

	@Override
	public void inAIdentifierPrimary(AIdentifierPrimary node) {
		super.inAIdentifierPrimary(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAIdentifierPrimary(AIdentifierPrimary node) {
		super.outAIdentifierPrimary(node);
		popBindingPathNode(node);
	}

	@Override
	public void inAMethodPrimaryNoId(AMethodPrimaryNoId node) {
		super.inAMethodPrimaryNoId(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAMethodPrimaryNoId(AMethodPrimaryNoId node) {
		super.outAMethodPrimaryNoId(node);
		popBindingPathNode(node);
	}

	@Override
	public void inAFieldPrimaryNoId(AFieldPrimaryNoId node) {
		super.inAFieldPrimaryNoId(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAFieldPrimaryNoId(AFieldPrimaryNoId node) {
		super.outAFieldPrimaryNoId(node);
		popBindingPathNode(node);
	}

	/*@Override
	public void inAOneArgumentList(AOneArgumentList node) {
		// TODO Auto-generated method stub
		super.inAOneArgumentList(node);
		depth++;
	}
	
	@Override
	public void outAOneArgumentList(AOneArgumentList node) {
		// TODO Auto-generated method stub
		super.outAOneArgumentList(node);
		depth--;
	}*/

	// When we enter in a type, increase level
	@Override
	public void inAReferenceType(AReferenceType node) {
		super.inAReferenceType(node);
		depth++;
	}

	// When we exit out a type, decrease level
	@Override
	public void outAReferenceType(AReferenceType node) {
		super.outAReferenceType(node);
		depth--;
	}

	@Override
	public void inANewInstancePrimaryNoId(ANewInstancePrimaryNoId node) {
		super.inANewInstancePrimaryNoId(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outANewInstancePrimaryNoId(ANewInstancePrimaryNoId node) {
		super.outANewInstancePrimaryNoId(node);
		popBindingPathNode(node);
	}

	// BEGIN Alternative 1

	@Override
	public void inAMethodInvocationStatementExpression(AMethodInvocationStatementExpression node) {
		super.inAMethodInvocationStatementExpression(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAMethodInvocationStatementExpression(AMethodInvocationStatementExpression node) {
		super.outAMethodInvocationStatementExpression(node);
		popBindingPathNode(node);
	}

	@Override
	public void inANewInstanceStatementExpression(ANewInstanceStatementExpression node) {
		super.inANewInstanceStatementExpression(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outANewInstanceStatementExpression(ANewInstanceStatementExpression node) {
		super.outANewInstanceStatementExpression(node);
		popBindingPathNode(node);
	}

	// END Alternative 1

	// BEGIN Alternative 2

	/*@Override
	public void inAPrimaryMethodInvocation(APrimaryMethodInvocation node) {
		super.inAPrimaryMethodInvocation(node);
		pushBindingPathNode(node);
	}
	
	@Override
	public void outAPrimaryMethodInvocation(APrimaryMethodInvocation node) {
		super.outAPrimaryMethodInvocation(node);
		popBindingPathNode(node);
	}
	
	@Override
	public void inASuperMethodInvocation(ASuperMethodInvocation node) {
		super.inASuperMethodInvocation(node);
		pushBindingPathNode(node);
	}
	
	@Override
	public void outASuperMethodInvocation(ASuperMethodInvocation node) {
		super.outASuperMethodInvocation(node);
		popBindingPathNode(node);
	}
	
	@Override
	public void inAClassMethodMethodInvocation(AClassMethodMethodInvocation node) {
		super.inAClassMethodMethodInvocation(node);
		pushBindingPathNode(node);
	}
	
	@Override
	public void outAClassMethodMethodInvocation(AClassMethodMethodInvocation node) {
		super.outAClassMethodMethodInvocation(node);
		popBindingPathNode(node);
	}*/

	// END Alternative 2

	@Override
	public void inANullLiteral(ANullLiteral node) {
		super.inANullLiteral(node);
		pushConstantNode(node, n -> new NullConstantNode(n, this));
	}

	@Override
	public void outANullLiteral(ANullLiteral node) {
		super.outANullLiteral(node);
		popConstantNode(node);
	}

	@Override
	public void inATrueLiteral(ATrueLiteral node) {
		super.inATrueLiteral(node);
		pushConstantNode(node, n -> new TrueConstantNode(n, this));
	}

	@Override
	public void outATrueLiteral(ATrueLiteral node) {
		super.outATrueLiteral(node);
		popConstantNode(node);
	}

	@Override
	public void inAFalseLiteral(AFalseLiteral node) {
		super.inAFalseLiteral(node);
		pushConstantNode(node, n -> new FalseConstantNode(n, this));
	}

	@Override
	public void outAFalseLiteral(AFalseLiteral node) {
		super.outAFalseLiteral(node);
		popConstantNode(node);
	}

	@Override
	public void inAStringLiteral(AStringLiteral node) {
		super.inAStringLiteral(node);
		pushConstantNode(node, n -> new StringConstantNode(n, this));
	}

	@Override
	public void outAStringLiteral(AStringLiteral node) {
		super.outAStringLiteral(node);
		popConstantNode(node);
	}

	@Override
	public void inACharacterLiteral(ACharacterLiteral node) {
		super.inACharacterLiteral(node);
		pushConstantNode(node, n -> new CharConstantNode(n, this));
	}

	@Override
	public void outACharacterLiteral(ACharacterLiteral node) {
		super.outACharacterLiteral(node);
		popConstantNode(node);
	}

	@Override
	public void inAIntegerLiteral(AIntegerLiteral node) {
		super.inAIntegerLiteral(node);
		pushConstantNode(node, n -> new IntegerConstantNode(n, this));
		/*depth++;
		if (weAreDealingWithTheRightBindingPath()) {
			super.inAIntegerLiteral(node);
			System.out.println("Nouveau IntegerLiteral " + node + " for " + Integer.toHexString(hashCode()) + " depth=" + depth);
			push(retrieveFMLNode(node, n -> new IntegerConstantNode(n, this)));
		}*/
	}

	@Override
	public void outAIntegerLiteral(AIntegerLiteral node) {
		super.outAIntegerLiteral(node);
		popConstantNode(node);
		/*if (weAreDealingWithTheRightBindingPath()) {
			super.outAIntegerLiteral(node);
			pop();
		}
		depth--;*/
	}

	@Override
	public void inAFloatingPointLiteral(AFloatingPointLiteral node) {
		super.inAFloatingPointLiteral(node);
		pushConstantNode(node, n -> new FloatingPointConstantNode(n, this));
	}

	@Override
	public void outAFloatingPointLiteral(AFloatingPointLiteral node) {
		super.outAFloatingPointLiteral(node);
		popConstantNode(node);
	}

	// conditional_exp =
	// {simple} conditional_or_exp
	// | {qmark} conditional_or_exp qmark expression colon conditional_exp
	// ;

	@Override
	public void inAQmarkConditionalExp(AQmarkConditionalExp node) {
		super.inAQmarkConditionalExp(node);
		push(retrieveFMLNode(node, n -> new FMLConditionalExpressionNode(n, this)));
	}

	@Override
	public void outAQmarkConditionalExp(AQmarkConditionalExp node) {
		super.outAQmarkConditionalExp(node);
		pop();
		// registerExpressionNode(node, new FMLConditionalExpression(getExpression(node.getConditionalOrExp()),
		// getExpression(node.getExpression()), getExpression(node.getConditionalExp())));
	}

	// conditional_or_exp =
	// {simple} conditional_and_exp
	// | {bar_bar} conditional_or_exp bar_bar conditional_and_exp
	// ;

	@Override
	public void inABarBarConditionalOrExp(ABarBarConditionalOrExp node) {
		super.inABarBarConditionalOrExp(node);
		push(retrieveFMLNode(node, n -> new OrExpressionNode(n, this)));
	}

	@Override
	public void outABarBarConditionalOrExp(ABarBarConditionalOrExp node) {
		super.outABarBarConditionalOrExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.OR,
		// getExpression(node.getConditionalOrExp()),
		// getExpression(node.getConditionalAndExp())));
	}

	// conditional_and_exp =
	// {simple} inclusive_or_exp
	// | {amp_amp} conditional_and_exp amp_amp inclusive_or_exp
	// ;

	@Override
	public void inAAmpAmpConditionalAndExp(AAmpAmpConditionalAndExp node) {
		super.inAAmpAmpConditionalAndExp(node);
		push(retrieveFMLNode(node, n -> new AndExpressionNode(n, this)));
	}

	@Override
	public void outAAmpAmpConditionalAndExp(AAmpAmpConditionalAndExp node) {
		super.outAAmpAmpConditionalAndExp(node);
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.AND,
		// getExpression(node.getConditionalAndExp()), getExpression(node.getInclusiveOrExp())));
		pop();
	}

	// inclusive_or_exp =
	// {simple} exclusive_or_exp
	// | {bar} inclusive_or_exp bar exclusive_or_exp
	// ;

	@Override
	public void inABarInclusiveOrExp(ABarInclusiveOrExp node) {
		super.inABarInclusiveOrExp(node);
		push(retrieveFMLNode(node, n -> new BitwiseOrExpressionNode(n, this)));
	}

	@Override
	public void outABarInclusiveOrExp(ABarInclusiveOrExp node) {
		super.outABarInclusiveOrExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_OR,
		// getExpression(node.getInclusiveOrExp()), getExpression(node.getExclusiveOrExp())));
	}

	// exclusive_or_exp =
	// {simple} and_exp
	// | {caret} exclusive_or_exp caret and_exp
	// ;

	@Override
	public void inACaretExclusiveOrExp(ACaretExclusiveOrExp node) {
		super.inACaretExclusiveOrExp(node);
		push(retrieveFMLNode(node, n -> new BitwiseXOrExpressionNode(n, this)));
	}

	@Override
	public void outACaretExclusiveOrExp(ACaretExclusiveOrExp node) {
		super.outACaretExclusiveOrExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_XOR,
		// getExpression(node.getExclusiveOrExp()), getExpression(node.getAndExp())));
	}

	// and_exp =
	// {simple} equality_exp
	// | {amp} and_exp amp equality_exp
	// ;

	@Override
	public void inAAmpAndExp(AAmpAndExp node) {
		super.inAAmpAndExp(node);
		push(retrieveFMLNode(node, n -> new BitwiseAndExpressionNode(n, this)));
	}

	@Override
	public void outAAmpAndExp(AAmpAndExp node) {
		super.outAAmpAndExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_AND,
		// getExpression(node.getAndExp()), getExpression(node.getEqualityExp())));
	}

	// equality_exp =
	// {simple} relational_exp
	// | {eq} equality_exp eq relational_exp
	// | {neq} equality_exp neq relational_exp
	// ;

	@Override
	public void inAEqEqualityExp(AEqEqualityExp node) {
		super.inAEqEqualityExp(node);
		push(retrieveFMLNode(node, n -> new EqualsExpressionNode(n, this)));
	}

	@Override
	public void outAEqEqualityExp(AEqEqualityExp node) {
		super.outAEqEqualityExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.EQUALS,
		// getExpression(node.getEqualityExp()),
		// getExpression(node.getRelationalExp())));
	}

	@Override
	public void inANeqEqualityExp(ANeqEqualityExp node) {
		super.inANeqEqualityExp(node);
		push(retrieveFMLNode(node, n -> new NotEqualsExpressionNode(n, this)));
	}

	@Override
	public void outANeqEqualityExp(ANeqEqualityExp node) {
		super.outANeqEqualityExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.NOT_EQUALS,
		// getExpression(node.getEqualityExp()), getExpression(node.getRelationalExp())));
	}

	// relational_exp =
	// {simple} shift_exp
	// | {lt} [shift_exp1]:shift_exp lt [shift_expression2]:shift_exp
	// | {gt} [shift_expression1]:shift_exp gt [shift_expression2]:shift_exp
	// | {lteq} [shift_expression1]:shift_exp lteq [shift_expression2]:shift_exp
	// | {gteq} [shift_expression1]:shift_exp gteq [shift_expression2]:shift_exp
	// | {instanceof} shift_exp kw_instanceof type [dims]:dim*
	// ;

	@Override
	public void inALtRelationalExp(ALtRelationalExp node) {
		super.inALtRelationalExp(node);
		push(retrieveFMLNode(node, n -> new LessThanExpressionNode(n, this)));
	}

	@Override
	public void outALtRelationalExp(ALtRelationalExp node) {
		super.outALtRelationalExp(node);
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.LESS_THAN,
		// getExpression(node.getShiftExp1()),
		// getExpression(node.getShiftExpression2())));
		pop();
	}

	@Override
	public void inAGtRelationalExp(AGtRelationalExp node) {
		super.inAGtRelationalExp(node);
		push(retrieveFMLNode(node, n -> new GreaterThanExpressionNode(n, this)));
	}

	@Override
	public void outAGtRelationalExp(AGtRelationalExp node) {
		super.outAGtRelationalExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.GREATER_THAN,
		// getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
	}

	@Override
	public void inALteqRelationalExp(ALteqRelationalExp node) {
		super.inALteqRelationalExp(node);
		push(retrieveFMLNode(node, n -> new LessThanOrEqualsExpressionNode(n, this)));
	}

	@Override
	public void outALteqRelationalExp(ALteqRelationalExp node) {
		super.outALteqRelationalExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.LESS_THAN_OR_EQUALS,
		// getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
	}

	@Override
	public void inAGteqRelationalExp(AGteqRelationalExp node) {
		super.inAGteqRelationalExp(node);
		push(retrieveFMLNode(node, n -> new GreaterThanOrEqualsExpressionNode(n, this)));
	}

	@Override
	public void outAGteqRelationalExp(AGteqRelationalExp node) {
		super.outAGteqRelationalExp(node);
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.GREATER_THAN_OR_EQUALS,
		// getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
		pop();
	}

	@Override
	public void inAInstanceofRelationalExp(AInstanceofRelationalExp node) {
		super.inAInstanceofRelationalExp(node);
		push(retrieveFMLNode(node, n -> new FMLInstanceOfExpressionNode(n, this)));
	}

	@Override
	public void outAInstanceofRelationalExp(AInstanceofRelationalExp node) {
		super.outAInstanceofRelationalExp(node);
		pop();
		// Type type = TypeFactory.makeType(node.getType(), getTypingSpace());
		// registerExpressionNode(node, new FMLInstanceOfExpression(getExpression(node.getShiftExp()), type));
	}

	// shift_exp =
	// {simple} add_exp
	// | {shl} shift_exp shl add_exp
	// | {shr} shift_exp shr add_exp
	// | {ushr} shift_exp ushr add_exp
	// ;

	@Override
	public void inAShlShiftExp(AShlShiftExp node) {
		super.inAShlShiftExp(node);
		push(retrieveFMLNode(node, n -> new ShiftLeftExpressionNode(n, this)));
	}

	@Override
	public void outAShlShiftExp(AShlShiftExp node) {
		super.outAShlShiftExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_LEFT,
		// getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
	}

	@Override
	public void inAShrShiftExp(AShrShiftExp node) {
		super.inAShrShiftExp(node);
		push(retrieveFMLNode(node, n -> new ShiftRightExpressionNode(n, this)));
	}

	@Override
	public void outAShrShiftExp(AShrShiftExp node) {
		super.outAShrShiftExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_RIGHT,
		// getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
	}

	@Override
	public void inAUshrShiftExp(AUshrShiftExp node) {
		super.inAUshrShiftExp(node);
		push(retrieveFMLNode(node, n -> new ShiftRight2ExpressionNode(n, this)));
	}

	@Override
	public void outAUshrShiftExp(AUshrShiftExp node) {
		super.outAUshrShiftExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_RIGHT_2,
		// getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
	}

	// add_exp =
	// {simple} mult_exp
	// | {plus} add_exp plus mult_exp
	// | {minus} add_exp minus mult_exp
	// ;

	@Override
	public void inAPlusAddExp(APlusAddExp node) {
		super.inAPlusAddExp(node);
		push(retrieveFMLNode(node, n -> new PlusExpressionNode(n, this)));
	}

	@Override
	public void outAPlusAddExp(APlusAddExp node) {
		super.outAPlusAddExp(node);
		pop();
	}

	@Override
	public void inAMinusAddExp(AMinusAddExp node) {
		super.inAMinusAddExp(node);
		push(retrieveFMLNode(node, n -> new MinusExpressionNode(n, this)));
	}

	@Override
	public void outAMinusAddExp(AMinusAddExp node) {
		super.outAMinusAddExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SUBSTRACTION,
		// getExpression(node.getAddExp()), getExpression(node.getMultExp())));
	}

	// mult_exp =
	// {simple} unary_exp
	// | {star} mult_exp star unary_exp
	// | {slash} mult_exp slash unary_exp
	// | {percent} mult_exp percent unary_exp
	// ;

	@Override
	public void inAStarMultExp(AStarMultExp node) {
		super.inAStarMultExp(node);
		push(retrieveFMLNode(node, n -> new MultiplicationExpressionNode(n, this)));
	}

	@Override
	public void outAStarMultExp(AStarMultExp node) {
		super.outAStarMultExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.MULTIPLICATION,
		// getExpression(node.getMultExp()), getExpression(node.getUnaryExp())));
	}

	@Override
	public void inASlashMultExp(ASlashMultExp node) {
		super.inASlashMultExp(node);
		push(retrieveFMLNode(node, n -> new DivisionExpressionNode(n, this)));
	}

	@Override
	public void outASlashMultExp(ASlashMultExp node) {
		super.outASlashMultExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.DIVISION,
		// getExpression(node.getMultExp()),
		// getExpression(node.getUnaryExp())));
	}

	@Override
	public void inAPercentMultExp(APercentMultExp node) {
		super.inAPercentMultExp(node);
		push(retrieveFMLNode(node, n -> new ModExpressionNode(n, this)));
	}

	@Override
	public void outAPercentMultExp(APercentMultExp node) {
		super.outAPercentMultExp(node);
		pop();
		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.MOD, getExpression(node.getMultExp()),
		// getExpression(node.getUnaryExp())));
	}

	// unary_exp =
	// {pre_increment} pre_incr_exp
	// | {pre_decrement} pre_decr_exp
	// | {plus} plus unary_exp
	// | {minus} minus unary_exp
	// | {unary} unary_exp_not_plus_minus
	// ;

	@Override
	public void inAPlusUnaryExp(APlusUnaryExp node) {
		super.inAPlusUnaryExp(node);
		push(retrieveFMLNode(node, n -> new UnaryPlusExpressionNode(n, this)));
	}

	@Override
	public void outAPlusUnaryExp(APlusUnaryExp node) {
		super.outAPlusUnaryExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.UNARY_PLUS, getExpression(node.getUnaryExp())));
	}

	@Override
	public void inAMinusUnaryExp(AMinusUnaryExp node) {
		super.inAMinusUnaryExp(node);
		push(retrieveFMLNode(node, n -> new UnaryMinusExpressionNode(n, this)));
	}

	@Override
	public void outAMinusUnaryExp(AMinusUnaryExp node) {
		super.outAMinusUnaryExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.UNARY_MINUS, getExpression(node.getUnaryExp())));
	}

	// pre_incr_exp = plus_plus unary_exp;
	// pre_decr_exp = minus_minus unary_exp;

	@Override
	public void inAPreIncrExp(APreIncrExp node) {
		super.inAPreIncrExp(node);
		push(retrieveFMLNode(node, n -> new PreIncrementExpressionNode(n, this)));
	}

	@Override
	public void outAPreIncrExp(APreIncrExp node) {
		super.outAPreIncrExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.PRE_INCREMENT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void inAPreDecrExp(APreDecrExp node) {
		super.inAPreDecrExp(node);
		push(retrieveFMLNode(node, n -> new PreDecrementExpressionNode(n, this)));
	}

	@Override
	public void outAPreDecrExp(APreDecrExp node) {
		super.outAPreDecrExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.PRE_DECREMENT, getExpression(node.getUnaryExp())));
	}

	// unary_exp_not_plus_minus =
	// {postfix} postfix_exp
	// | {tilde} tilde unary_exp
	// | {emark} emark unary_exp
	// | {cast} l_par type [dims]:dim* r_par unary_exp
	// ;

	@Override
	public void inATildeUnaryExpNotPlusMinus(ATildeUnaryExpNotPlusMinus node) {
		super.inATildeUnaryExpNotPlusMinus(node);
		push(retrieveFMLNode(node, n -> new BitwiseComplementExpressionNode(n, this)));
	}

	@Override
	public void outATildeUnaryExpNotPlusMinus(ATildeUnaryExpNotPlusMinus node) {
		super.outATildeUnaryExpNotPlusMinus(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.BITWISE_COMPLEMENT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void inAEmarkUnaryExpNotPlusMinus(AEmarkUnaryExpNotPlusMinus node) {
		super.inAEmarkUnaryExpNotPlusMinus(node);
		push(retrieveFMLNode(node, n -> new NotExpressionNode(n, this)));
	}

	@Override
	public void outAEmarkUnaryExpNotPlusMinus(AEmarkUnaryExpNotPlusMinus node) {
		super.outAEmarkUnaryExpNotPlusMinus(node);
		pop();
		// registerExpressionNode(node, new FMLUnaryOperatorExpression(FMLBooleanUnaryOperator.NOT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void inACastUnaryExpNotPlusMinus(ACastUnaryExpNotPlusMinus node) {
		super.inACastUnaryExpNotPlusMinus(node);
		push(retrieveFMLNode(node, n -> new FMLCastExpressionNode(n, this)));
	}

	@Override
	public void outACastUnaryExpNotPlusMinus(ACastUnaryExpNotPlusMinus node) {
		super.outACastUnaryExpNotPlusMinus(node);
		pop();
		// Type type = TypeFactory.makeType(node.getType(), getTypingSpace());
		// System.out.println("Found type " + type);
		// registerExpressionNode(node, new FMLCastExpression(type, getExpression(node.getUnaryExp())));
	}

	// post_incr_exp = postfix_exp plus_plus;
	// post_decr_exp = postfix_exp minus_minus;

	@Override
	public void inAPostDecrExp(APostDecrExp node) {
		super.inAPostDecrExp(node);
		push(retrieveFMLNode(node, n -> new PostDecrementExpressionNode(n, this)));
	}

	@Override
	public void outAPostDecrExp(APostDecrExp node) {
		super.outAPostDecrExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.POST_DECREMENT, getExpression(node.getPostfixExp())));
	}

	@Override
	public void inAPostIncrExp(APostIncrExp node) {
		super.inAPostIncrExp(node);
		push(retrieveFMLNode(node, n -> new PostIncrementExpressionNode(n, this)));
	}

	@Override
	public void outAPostIncrExp(APostIncrExp node) {
		super.outAPostIncrExp(node);
		pop();
		// registerExpressionNode(node,
		// new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.POST_INCREMENT, getExpression(node.getPostfixExp())));
	}

	@Override
	public void inAAssignmentExpression(AAssignmentExpression node) {
		super.inAAssignmentExpression(node);
		push(retrieveFMLNode(node, n -> new AssignmentExpressionNode(n, this)));
	}

	@Override
	public void outAAssignmentExpression(AAssignmentExpression node) {
		super.outAAssignmentExpression(node);
		pop();
	}

}
