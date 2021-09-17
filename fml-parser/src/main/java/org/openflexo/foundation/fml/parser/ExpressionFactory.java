package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.expr.FMLArithmeticBinaryOperator;
import org.openflexo.foundation.fml.expr.FMLArithmeticUnaryOperator;
import org.openflexo.foundation.fml.expr.FMLBinaryOperatorExpression;
import org.openflexo.foundation.fml.expr.FMLBooleanBinaryOperator;
import org.openflexo.foundation.fml.expr.FMLBooleanUnaryOperator;
import org.openflexo.foundation.fml.expr.FMLCastExpression;
import org.openflexo.foundation.fml.expr.FMLConditionalExpression;
import org.openflexo.foundation.fml.expr.FMLConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.CharConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectSymbolicConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.StringConstant;
import org.openflexo.foundation.fml.expr.FMLInstanceOfExpression;
import org.openflexo.foundation.fml.expr.FMLUnaryOperatorExpression;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.IntegerConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PlusExpressionNode;
import org.openflexo.foundation.fml.parser.node.AAmpAmpConditionalAndExp;
import org.openflexo.foundation.fml.parser.node.AAmpAndExp;
import org.openflexo.foundation.fml.parser.node.ABarBarConditionalOrExp;
import org.openflexo.foundation.fml.parser.node.ABarInclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ACaretExclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ACastUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ACharacterLiteral;
import org.openflexo.foundation.fml.parser.node.AConditionalExpression;
import org.openflexo.foundation.fml.parser.node.AEmarkUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.AEqEqualityExp;
import org.openflexo.foundation.fml.parser.node.AExpressionPrimaryNoId;
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
import org.openflexo.foundation.fml.parser.node.ALiteralPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ALtRelationalExp;
import org.openflexo.foundation.fml.parser.node.ALteqRelationalExp;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AMinusAddExp;
import org.openflexo.foundation.fml.parser.node.AMinusUnaryExp;
import org.openflexo.foundation.fml.parser.node.ANeqEqualityExp;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANullLiteral;
import org.openflexo.foundation.fml.parser.node.APercentMultExp;
import org.openflexo.foundation.fml.parser.node.APlusAddExp;
import org.openflexo.foundation.fml.parser.node.APlusUnaryExp;
import org.openflexo.foundation.fml.parser.node.APostDecrExp;
import org.openflexo.foundation.fml.parser.node.APostDecrementPostfixExp;
import org.openflexo.foundation.fml.parser.node.APostIncrExp;
import org.openflexo.foundation.fml.parser.node.APostIncrementPostfixExp;
import org.openflexo.foundation.fml.parser.node.APostfixUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.APreDecrExp;
import org.openflexo.foundation.fml.parser.node.APreDecrementUnaryExp;
import org.openflexo.foundation.fml.parser.node.APreIncrExp;
import org.openflexo.foundation.fml.parser.node.APreIncrementUnaryExp;
import org.openflexo.foundation.fml.parser.node.APrimaryNoIdPrimary;
import org.openflexo.foundation.fml.parser.node.APrimaryPostfixExp;
import org.openflexo.foundation.fml.parser.node.AQmarkConditionalExp;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AShlShiftExp;
import org.openflexo.foundation.fml.parser.node.AShrShiftExp;
import org.openflexo.foundation.fml.parser.node.ASimpleAddExp;
import org.openflexo.foundation.fml.parser.node.ASimpleAndExp;
import org.openflexo.foundation.fml.parser.node.ASimpleConditionalAndExp;
import org.openflexo.foundation.fml.parser.node.ASimpleConditionalExp;
import org.openflexo.foundation.fml.parser.node.ASimpleConditionalOrExp;
import org.openflexo.foundation.fml.parser.node.ASimpleEqualityExp;
import org.openflexo.foundation.fml.parser.node.ASimpleExclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ASimpleInclusiveOrExp;
import org.openflexo.foundation.fml.parser.node.ASimpleMultExp;
import org.openflexo.foundation.fml.parser.node.ASimpleRelationalExp;
import org.openflexo.foundation.fml.parser.node.ASimpleShiftExp;
import org.openflexo.foundation.fml.parser.node.ASlashMultExp;
import org.openflexo.foundation.fml.parser.node.AStarMultExp;
import org.openflexo.foundation.fml.parser.node.AStringLiteral;
import org.openflexo.foundation.fml.parser.node.ATildeUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ATrueLiteral;
import org.openflexo.foundation.fml.parser.node.AUnaryUnaryExp;
import org.openflexo.foundation.fml.parser.node.AUshrShiftExp;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate {@link FMLControlGraph} from AST
 * 
 * @author sylvain
 *
 */
public class ExpressionFactory extends FMLSemanticsAnalyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpressionFactory.class.getPackage().getName());

	@Deprecated
	private final Map<Node, Expression> expressionNodes;
	@Deprecated
	private Node topLevel = null;

	private Bindable bindable;
	private final AbstractFMLTypingSpace typingSpace;
	private final MainSemanticsAnalyzer mainAnalyzer;
	private final DataBindingNode dataBindingNode;

	private int depth = -1;

	private boolean weAreDealingWithTheRightBindingPath() {
		return depth == 0;
	}

	public static Expression makeExpression(Node node, Bindable bindable, FMLCompilationUnit compilationUnit) {
		return _makeExpression(node, bindable, compilationUnit.getTypingSpace(), null, null);
	}

	public static Expression makeExpression(Node node, Bindable bindable, MainSemanticsAnalyzer mainAnalyzer, DataBindingNode parentNode) {
		return _makeExpression(node, bindable, mainAnalyzer.getTypingSpace(), mainAnalyzer, parentNode);
	}

	public static Expression makeExpression(Node node, Bindable bindable, AbstractFMLTypingSpace typingSpace) {
		return _makeExpression(node, bindable, typingSpace, null, null);
	}

	public static <T> DataBinding<T> makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, FMLCompilationUnit compilationUnit) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, compilationUnit.getTypingSpace(), null, null);
	}

	public static <T> DataBinding<T> makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, MainSemanticsAnalyzer mainAnalyzer, ObjectNode<?, ?, ?> parentNode) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, mainAnalyzer.getTypingSpace(), mainAnalyzer,
				parentNode);
	}

	private static Expression _makeExpression(Node node, Bindable bindable, AbstractFMLTypingSpace typingSpace,
			MainSemanticsAnalyzer mainAnalyzer, DataBindingNode dataBindingNode) {
		// return new DataBinding(analyzer.getText(node), bindable, expectedType, BindingDefinitionType.GET);

		ExpressionFactory factory = new ExpressionFactory(node, bindable, typingSpace, mainAnalyzer, dataBindingNode);
		factory.push(dataBindingNode);
		node.apply(factory);
		factory.pop();

		return factory.getExpression();
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> DataBinding<T> _makeDataBinding(Node node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, AbstractFMLTypingSpace typingSpace, MainSemanticsAnalyzer mainAnalyzer, ObjectNode<?, ?, ?> parentNode) {

		DataBindingNode dataBindingNode = mainAnalyzer.retrieveFMLNode(node,
				n -> new DataBindingNode(n, bindable, bindingDefinitionType, expectedType, mainAnalyzer));

		parentNode.addToChildren(dataBindingNode);

		_makeExpression(node, bindable, typingSpace, mainAnalyzer, dataBindingNode);

		return (DataBinding<T>) dataBindingNode.getModelObject();
	}

	private ExpressionFactory(Node rootNode, Bindable aBindable, AbstractFMLTypingSpace typingSpace, MainSemanticsAnalyzer mainAnalyzer,
			DataBindingNode dataBindingNode) {
		super(mainAnalyzer.getFactory(), rootNode);
		expressionNodes = new Hashtable<>();
		this.bindable = aBindable;
		this.typingSpace = typingSpace;
		this.mainAnalyzer = mainAnalyzer;
		this.dataBindingNode = dataBindingNode;
	}

	public DataBindingNode getDataBindingNode() {
		return dataBindingNode;
	}

	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	public Expression getExpression() {
		if (topLevel != null) {
			return expressionNodes.get(topLevel);
		}
		return null;
	}

	public Bindable getBindable() {
		return bindable;
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return mainAnalyzer;
	}

	@Override
	protected void push(ObjectNode<?, ?, ?> fmlNode) {
		super.push(fmlNode);
		if (fmlNode.getModelObject() instanceof Expression) {
			expressionNodes.put(fmlNode.getASTNode(), (Expression) fmlNode.getModelObject());
		}
	}

	@Deprecated
	private void registerExpressionNode(Node n, Expression e) {
		// System.out.println("REGISTER " + e + " for node " + n + " as " + n.getClass());
		expressionNodes.put(n, e);
		topLevel = n;
		/*if (n.parent() != null) {
			registerExpressionNode(n.parent(), e);
		}*/
		/*if (e instanceof Constant && weAreDealingWithTheRightBindingPath()) {
			if (getMainAnalyzer() != null && parentNode != null) {
				ConstantNode constantNode = getMainAnalyzer().registerFMLNode(n, new ConstantNode(n, getMainAnalyzer()));
				constantNode.setModelObject((Constant) e);
				parentNode.addToChildren(constantNode);
			}
		}*/
	}

	public Expression getExpression(Node n) {
		if (n != null) {
			Expression returned = expressionNodes.get(n);

			if (returned == null) {
				if (n instanceof AConditionalExpression) {
					return getExpression(((AConditionalExpression) n).getConditionalExp());
				}
				if (n instanceof ASimpleConditionalExp) {
					return getExpression(((ASimpleConditionalExp) n).getConditionalOrExp());
				}
				if (n instanceof ASimpleConditionalOrExp) {
					return getExpression(((ASimpleConditionalOrExp) n).getConditionalAndExp());
				}
				if (n instanceof ASimpleConditionalAndExp) {
					return getExpression(((ASimpleConditionalAndExp) n).getInclusiveOrExp());
				}
				if (n instanceof ASimpleInclusiveOrExp) {
					return getExpression(((ASimpleInclusiveOrExp) n).getExclusiveOrExp());
				}
				if (n instanceof ASimpleExclusiveOrExp) {
					return getExpression(((ASimpleExclusiveOrExp) n).getAndExp());
				}
				if (n instanceof ASimpleAndExp) {
					return getExpression(((ASimpleAndExp) n).getEqualityExp());
				}
				if (n instanceof ASimpleEqualityExp) {
					return getExpression(((ASimpleEqualityExp) n).getRelationalExp());
				}
				if (n instanceof ASimpleRelationalExp) {
					return getExpression(((ASimpleRelationalExp) n).getShiftExp());
				}
				if (n instanceof ASimpleShiftExp) {
					return getExpression(((ASimpleShiftExp) n).getAddExp());
				}
				if (n instanceof ASimpleAddExp) {
					return getExpression(((ASimpleAddExp) n).getMultExp());
				}
				if (n instanceof ASimpleMultExp) {
					return getExpression(((ASimpleMultExp) n).getUnaryExp());
				}
				if (n instanceof AUnaryUnaryExp) {
					return getExpression(((AUnaryUnaryExp) n).getUnaryExpNotPlusMinus());
				}
				if (n instanceof APostfixUnaryExpNotPlusMinus) {
					return getExpression(((APostfixUnaryExpNotPlusMinus) n).getPostfixExp());
				}
				if (n instanceof APrimaryPostfixExp) {
					return getExpression(((APrimaryPostfixExp) n).getPrimary());
				}
				if (n instanceof APrimaryNoIdPrimary) {
					return getExpression(((APrimaryNoIdPrimary) n).getPrimaryNoId());
				}
				if (n instanceof ALiteralPrimaryNoId) {
					return getExpression(((ALiteralPrimaryNoId) n).getLiteral());
				}
				if (n instanceof AExpressionPrimaryNoId) {
					return getExpression(((AExpressionPrimaryNoId) n).getExpression());
				}
				if (n instanceof APreIncrementUnaryExp) {
					return getExpression(((APreIncrementUnaryExp) n).getPreIncrExp());
				}
				if (n instanceof APreDecrementUnaryExp) {
					return getExpression(((APreDecrementUnaryExp) n).getPreDecrExp());
				}
				if (n instanceof APostIncrementPostfixExp) {
					return getExpression(((APostIncrementPostfixExp) n).getPostIncrExp());
				}
				if (n instanceof APostDecrementPostfixExp) {
					return getExpression(((APostDecrementPostfixExp) n).getPostDecrExp());
				}

				System.out.println("No expression registered for " + n + " of  " + n.getClass());
			}
			return returned;
		}
		return null;
	}

	int ident = 0;

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		ident++;
		// System.out.println(StringUtils.buildWhiteSpaceIndentation(ident) + " > " + node.getClass().getSimpleName() + " : " + node);
	}

	@Override
	public void defaultOut(Node node) {
		super.defaultOut(node);
		ident--;
	}

	private BindingPathNode pushBindingPathNode(Node node) {
		depth++;
		if (weAreDealingWithTheRightBindingPath()) {
			BindingPathNode returned;
			System.out.println("********** On construit un BindingPath pour " + node);
			push(returned = retrieveFMLNode(node, n -> new BindingPathNode(n, this)));
			return returned;
		}
		return null;
	}

	private BindingPathNode popBindingPathNode(Node node) {
		try {
			if (weAreDealingWithTheRightBindingPath()) {
				BindingPathNode bindingPathNode = peek();
				BindingPathFactory.makeBindingPath(node, this, bindingPathNode, true);
				pop();
				expressionNodes.put(node, bindingPathNode.getModelObject());
				return bindingPathNode;
			}
			return null;
		} finally {
			depth--;
		}
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

	@Override
	public void outANullLiteral(ANullLiteral node) {
		super.outANullLiteral(node);
		registerExpressionNode(node, ObjectSymbolicConstant.NULL);
	}

	@Override
	public void outATrueLiteral(ATrueLiteral node) {
		super.outATrueLiteral(node);
		registerExpressionNode(node, BooleanConstant.TRUE);
	}

	@Override
	public void outAFalseLiteral(AFalseLiteral node) {
		super.outAFalseLiteral(node);
		registerExpressionNode(node, BooleanConstant.FALSE);
	}

	@Override
	public void outAStringLiteral(AStringLiteral node) {
		super.outAStringLiteral(node);
		String value = node.getLitString().getText();
		value = value.substring(1, value.length() - 1);
		registerExpressionNode(node, new StringConstant(value));
	}

	@Override
	public void outACharacterLiteral(ACharacterLiteral node) {
		super.outACharacterLiteral(node);
		String value = node.getLitCharacter().getText();
		Character c = value.charAt(1);
		registerExpressionNode(node, new CharConstant(c));
	}

	/*@Override
	public void inAConceptDecl(AConceptDecl node) {
		super.inAConceptDecl(node);
		push(retrieveFMLNode(node, n -> new FlexoConceptNode(n, getMainAnalyzer())));
	}
	
	@Override
	public void outAConceptDecl(AConceptDecl node) {
		super.outAConceptDecl(node);
		pop();
	}*/

	private Map<Node, ObjectNode> nodesForAST = new HashMap<>();

	/*public Map<Node, FMLObjectNode> getNodesForAST() {
		return nodesForAST;
	}*/

	public <N extends Node, FMLN extends ObjectNode> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function) {
		FMLN returned = (FMLN) nodesForAST.get(astNode);
		if (returned == null) {
			returned = function.apply(astNode);
			nodesForAST.put(astNode, returned);
		}
		return returned;
	}

	@Override
	public void inAIntegerLiteral(AIntegerLiteral node) {
		super.inAIntegerLiteral(node);

		push(retrieveFMLNode(node, n -> new IntegerConstantNode(n, this)));

		/*if (e instanceof Constant && weAreDealingWithTheRightBindingPath()) {
			if (getMainAnalyzer() != null && parentNode != null) {
				ConstantNode constantNode = getMainAnalyzer().registerFMLNode(n, new ConstantNode(n, getMainAnalyzer()));
				constantNode.setModelObject((Constant) e);
				parentNode.addToChildren(constantNode);
			}
		}*/

	}

	@Override
	public void outAIntegerLiteral(AIntegerLiteral node) {
		super.outAIntegerLiteral(node);

		/*String valueText = node.getLitInteger().getText();
		Number value;
		
		if (valueText.startsWith("0x") || valueText.startsWith("0X")) {
			valueText = valueText.substring(2);
			try {
				value = Integer.parseInt(valueText, 16);
			} catch (NumberFormatException e) {
				value = Long.parseLong(valueText, 16);
			}
		}
		else if (valueText.startsWith("0") && valueText.length() > 1) {
			valueText = valueText.substring(1);
			try {
				value = Integer.parseInt(valueText, 8);
			} catch (NumberFormatException e) {
				value = Long.parseLong(valueText, 8);
			}
		}
		else if (valueText.endsWith("L") || valueText.endsWith("l")) {
			valueText = valueText.substring(0, valueText.length() - 1);
			value = Long.parseLong(valueText);
		}
		else {
			value = Integer.parseInt(valueText);
			// value = NumberFormat.getNumberInstance().parse(valueText);
			// System.out.println("Pour " + valueText + " j'obtiens " + value + " of " + value.getClass());
		}
		registerExpressionNode(node, FMLConstant.makeConstant(value));*/

		pop();
	}

	@Override
	public void outAFloatingPointLiteral(AFloatingPointLiteral node) {
		super.outAFloatingPointLiteral(node);

		Number value = null;
		String valueText = node.getLitFloat().getText();
		if (valueText.endsWith("F") || valueText.endsWith("f")) {
			valueText = valueText.substring(0, valueText.length() - 1);
			try {
				value = Float.parseFloat(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if (valueText.endsWith("D") || valueText.endsWith("d")) {
			valueText = valueText.substring(0, valueText.length() - 1);
			try {
				value = Double.parseDouble(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				value = Double.parseDouble(valueText);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		registerExpressionNode(node, FMLConstant.makeConstant(value));

	}

	// conditional_exp =
	// {simple} conditional_or_exp
	// | {qmark} conditional_or_exp qmark expression colon conditional_exp
	// ;

	@Override
	public void outAQmarkConditionalExp(AQmarkConditionalExp node) {
		// TODO Auto-generated method stub
		super.outAQmarkConditionalExp(node);
		registerExpressionNode(node, new FMLConditionalExpression(getExpression(node.getConditionalOrExp()),
				getExpression(node.getExpression()), getExpression(node.getConditionalExp())));
	}

	// conditional_or_exp =
	// {simple} conditional_and_exp
	// | {bar_bar} conditional_or_exp bar_bar conditional_and_exp
	// ;

	@Override
	public void outABarBarConditionalOrExp(ABarBarConditionalOrExp node) {
		super.outABarBarConditionalOrExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.OR, getExpression(node.getConditionalOrExp()),
				getExpression(node.getConditionalAndExp())));
	}

	// conditional_and_exp =
	// {simple} inclusive_or_exp
	// | {amp_amp} conditional_and_exp amp_amp inclusive_or_exp
	// ;

	@Override
	public void outAAmpAmpConditionalAndExp(AAmpAmpConditionalAndExp node) {
		super.outAAmpAmpConditionalAndExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.AND,
				getExpression(node.getConditionalAndExp()), getExpression(node.getInclusiveOrExp())));
	}

	// inclusive_or_exp =
	// {simple} exclusive_or_exp
	// | {bar} inclusive_or_exp bar exclusive_or_exp
	// ;

	@Override
	public void outABarInclusiveOrExp(ABarInclusiveOrExp node) {
		super.outABarInclusiveOrExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_OR,
				getExpression(node.getInclusiveOrExp()), getExpression(node.getExclusiveOrExp())));
	}

	// exclusive_or_exp =
	// {simple} and_exp
	// | {caret} exclusive_or_exp caret and_exp
	// ;

	@Override
	public void outACaretExclusiveOrExp(ACaretExclusiveOrExp node) {
		super.outACaretExclusiveOrExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_XOR,
				getExpression(node.getExclusiveOrExp()), getExpression(node.getAndExp())));
	}

	// and_exp =
	// {simple} equality_exp
	// | {amp} and_exp amp equality_exp
	// ;

	@Override
	public void outAAmpAndExp(AAmpAndExp node) {
		super.outAAmpAndExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.BITWISE_AND,
				getExpression(node.getAndExp()), getExpression(node.getEqualityExp())));
	}

	// equality_exp =
	// {simple} relational_exp
	// | {eq} equality_exp eq relational_exp
	// | {neq} equality_exp neq relational_exp
	// ;

	@Override
	public void outAEqEqualityExp(AEqEqualityExp node) {
		super.outAEqEqualityExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.EQUALS, getExpression(node.getEqualityExp()),
				getExpression(node.getRelationalExp())));
	}

	@Override
	public void outANeqEqualityExp(ANeqEqualityExp node) {
		super.outANeqEqualityExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.NOT_EQUALS,
				getExpression(node.getEqualityExp()), getExpression(node.getRelationalExp())));
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
	public void outALtRelationalExp(ALtRelationalExp node) {
		super.outALtRelationalExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.LESS_THAN, getExpression(node.getShiftExp1()),
				getExpression(node.getShiftExpression2())));
	}

	@Override
	public void outAGtRelationalExp(AGtRelationalExp node) {
		super.outAGtRelationalExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.GREATER_THAN,
				getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
	}

	@Override
	public void outALteqRelationalExp(ALteqRelationalExp node) {
		super.outALteqRelationalExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.LESS_THAN_OR_EQUALS,
				getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
	}

	@Override
	public void outAGteqRelationalExp(AGteqRelationalExp node) {
		super.outAGteqRelationalExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLBooleanBinaryOperator.GREATER_THAN_OR_EQUALS,
				getExpression(node.getShiftExpression1()), getExpression(node.getShiftExpression2())));
	}

	@Override
	public void outAInstanceofRelationalExp(AInstanceofRelationalExp node) {
		super.outAInstanceofRelationalExp(node);
		// Type type = TypeAnalyzer.makeType(node.getType(), this);
		Type type = TypeFactory.makeType(node.getType(), getTypingSpace());
		registerExpressionNode(node, new FMLInstanceOfExpression(getExpression(node.getShiftExp()), type));
	}

	// shift_exp =
	// {simple} add_exp
	// | {shl} shift_exp shl add_exp
	// | {shr} shift_exp shr add_exp
	// | {ushr} shift_exp ushr add_exp
	// ;

	@Override
	public void outAShlShiftExp(AShlShiftExp node) {
		super.outAShlShiftExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_LEFT,
				getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
	}

	@Override
	public void outAShrShiftExp(AShrShiftExp node) {
		super.outAShrShiftExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_RIGHT,
				getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
	}

	@Override
	public void outAUshrShiftExp(AUshrShiftExp node) {
		super.outAUshrShiftExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SHIFT_RIGHT_2,
				getExpression(node.getShiftExp()), getExpression(node.getAddExp())));
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

		// registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.ADDITION,
		// getExpression(node.getAddExp()),
		// getExpression(node.getMultExp())));
	}

	@Override
	public void outAMinusAddExp(AMinusAddExp node) {
		super.outAMinusAddExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.SUBSTRACTION,
				getExpression(node.getAddExp()), getExpression(node.getMultExp())));
	}

	// mult_exp =
	// {simple} unary_exp
	// | {star} mult_exp star unary_exp
	// | {slash} mult_exp slash unary_exp
	// | {percent} mult_exp percent unary_exp
	// ;

	@Override
	public void outAStarMultExp(AStarMultExp node) {
		super.outAStarMultExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.MULTIPLICATION,
				getExpression(node.getMultExp()), getExpression(node.getUnaryExp())));
	}

	@Override
	public void outASlashMultExp(ASlashMultExp node) {
		super.outASlashMultExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.DIVISION, getExpression(node.getMultExp()),
				getExpression(node.getUnaryExp())));
	}

	@Override
	public void outAPercentMultExp(APercentMultExp node) {
		super.outAPercentMultExp(node);
		registerExpressionNode(node, new FMLBinaryOperatorExpression(FMLArithmeticBinaryOperator.MOD, getExpression(node.getMultExp()),
				getExpression(node.getUnaryExp())));
	}

	// unary_exp =
	// {pre_increment} pre_incr_exp
	// | {pre_decrement} pre_decr_exp
	// | {plus} plus unary_exp
	// | {minus} minus unary_exp
	// | {unary} unary_exp_not_plus_minus
	// ;

	@Override
	public void outAPlusUnaryExp(APlusUnaryExp node) {
		super.outAPlusUnaryExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.UNARY_PLUS, getExpression(node.getUnaryExp())));
	}

	@Override
	public void outAMinusUnaryExp(AMinusUnaryExp node) {
		super.outAMinusUnaryExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.UNARY_MINUS, getExpression(node.getUnaryExp())));
	}

	// pre_incr_exp = plus_plus unary_exp;
	// pre_decr_exp = minus_minus unary_exp;

	@Override
	public void outAPreIncrExp(APreIncrExp node) {
		super.outAPreIncrExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.PRE_INCREMENT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void outAPreDecrExp(APreDecrExp node) {
		super.outAPreDecrExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.PRE_DECREMENT, getExpression(node.getUnaryExp())));
	}

	// unary_exp_not_plus_minus =
	// {postfix} postfix_exp
	// | {tilde} tilde unary_exp
	// | {emark} emark unary_exp
	// | {cast} l_par type [dims]:dim* r_par unary_exp
	// ;

	@Override
	public void outATildeUnaryExpNotPlusMinus(ATildeUnaryExpNotPlusMinus node) {
		super.outATildeUnaryExpNotPlusMinus(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.BITWISE_COMPLEMENT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void outAEmarkUnaryExpNotPlusMinus(AEmarkUnaryExpNotPlusMinus node) {
		super.outAEmarkUnaryExpNotPlusMinus(node);
		registerExpressionNode(node, new FMLUnaryOperatorExpression(FMLBooleanUnaryOperator.NOT, getExpression(node.getUnaryExp())));
	}

	@Override
	public void outACastUnaryExpNotPlusMinus(ACastUnaryExpNotPlusMinus node) {
		super.outACastUnaryExpNotPlusMinus(node);
		// Type type = TypeAnalyzer.makeType(node.getType(), this);
		Type type = TypeFactory.makeType(node.getType(), getTypingSpace());
		System.out.println("Found type " + type);
		registerExpressionNode(node, new FMLCastExpression(type, getExpression(node.getUnaryExp())));
	}

	// post_incr_exp = postfix_exp plus_plus;
	// post_decr_exp = postfix_exp minus_minus;

	@Override
	public void outAPostDecrExp(APostDecrExp node) {
		super.outAPostDecrExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.POST_DECREMENT, getExpression(node.getPostfixExp())));
	}

	@Override
	public void outAPostIncrExp(APostIncrExp node) {
		super.outAPostIncrExp(node);
		registerExpressionNode(node,
				new FMLUnaryOperatorExpression(FMLArithmeticUnaryOperator.POST_INCREMENT, getExpression(node.getPostfixExp())));
	}

}
