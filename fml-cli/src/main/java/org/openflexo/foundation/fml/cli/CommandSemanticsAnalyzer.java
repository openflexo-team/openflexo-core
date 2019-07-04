/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.cli;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.expr.ArithmeticBinaryOperator;
import org.openflexo.connie.expr.ArithmeticUnaryOperator;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BooleanBinaryOperator;
import org.openflexo.connie.expr.BooleanUnaryOperator;
import org.openflexo.connie.expr.CastExpression;
import org.openflexo.connie.expr.ConditionalExpression;
import org.openflexo.connie.expr.Constant.BooleanConstant;
import org.openflexo.connie.expr.Constant.FloatConstant;
import org.openflexo.connie.expr.Constant.FloatSymbolicConstant;
import org.openflexo.connie.expr.Constant.IntegerConstant;
import org.openflexo.connie.expr.Constant.ObjectSymbolicConstant;
import org.openflexo.connie.expr.Constant.StringConstant;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.TypeReference;
import org.openflexo.connie.expr.UnaryOperatorExpression;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.DisplayResource;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.HelpDirective;
import org.openflexo.foundation.fml.cli.command.directive.LoadResource;
import org.openflexo.foundation.fml.cli.command.directive.LsDirective;
import org.openflexo.foundation.fml.cli.command.directive.OpenProject;
import org.openflexo.foundation.fml.cli.command.directive.PwdDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.foundation.fml.cli.command.directive.ResourcesDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServiceDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServicesDirective;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssignation;
import org.openflexo.foundation.fml.cli.command.fml.FMLContextCommand;
import org.openflexo.foundation.fml.cli.command.fml.FMLExpression;
import org.openflexo.foundation.fml.cli.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.cli.parser.node.AAcosFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.AActivateTaDirective;
import org.openflexo.foundation.fml.cli.parser.node.AAddExprExpr2;
import org.openflexo.foundation.fml.cli.parser.node.AAnd2ExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.AAndExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.AAsinFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.AAssignationFmlCommand;
import org.openflexo.foundation.fml.cli.parser.node.AAtanFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.ABasicTypeReference;
import org.openflexo.foundation.fml.cli.parser.node.ABindingTerm;
import org.openflexo.foundation.fml.cli.parser.node.ACastTerm;
import org.openflexo.foundation.fml.cli.parser.node.ACdDirective;
import org.openflexo.foundation.fml.cli.parser.node.ACharsValueTerm;
import org.openflexo.foundation.fml.cli.parser.node.ACondExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AConstantNumber;
import org.openflexo.foundation.fml.cli.parser.node.AContextFmlCommand;
import org.openflexo.foundation.fml.cli.parser.node.ACosFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.ADecimalNumberNumber;
import org.openflexo.foundation.fml.cli.parser.node.ADisplayDirective;
import org.openflexo.foundation.fml.cli.parser.node.ADivExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.AEnterDirective;
import org.openflexo.foundation.fml.cli.parser.node.AEq2ExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AEqExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AExitDirective;
import org.openflexo.foundation.fml.cli.parser.node.AExpFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.AExpr2Expr;
import org.openflexo.foundation.fml.cli.parser.node.AExpr3Expr2;
import org.openflexo.foundation.fml.cli.parser.node.AExprFmlCommand;
import org.openflexo.foundation.fml.cli.parser.node.AExprTerm;
import org.openflexo.foundation.fml.cli.parser.node.AFalseConstant;
import org.openflexo.foundation.fml.cli.parser.node.AFunctionTerm;
import org.openflexo.foundation.fml.cli.parser.node.AGtExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AGteExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AHelpDirective;
import org.openflexo.foundation.fml.cli.parser.node.AIdentifierTypeReferencePath;
import org.openflexo.foundation.fml.cli.parser.node.ALoadDirective;
import org.openflexo.foundation.fml.cli.parser.node.ALogFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.ALsDirective;
import org.openflexo.foundation.fml.cli.parser.node.ALtExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.ALteExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.AModExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.AMultExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.ANegativeTerm;
import org.openflexo.foundation.fml.cli.parser.node.ANeqExprExpr;
import org.openflexo.foundation.fml.cli.parser.node.ANotExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.ANullConstant;
import org.openflexo.foundation.fml.cli.parser.node.ANumberTerm;
import org.openflexo.foundation.fml.cli.parser.node.AOpenDirective;
import org.openflexo.foundation.fml.cli.parser.node.AOr2ExprExpr2;
import org.openflexo.foundation.fml.cli.parser.node.AOrExprExpr2;
import org.openflexo.foundation.fml.cli.parser.node.AParameteredTypeReference;
import org.openflexo.foundation.fml.cli.parser.node.APiConstant;
import org.openflexo.foundation.fml.cli.parser.node.APowerExprExpr3;
import org.openflexo.foundation.fml.cli.parser.node.APreciseNumberNumber;
import org.openflexo.foundation.fml.cli.parser.node.APwdDirective;
import org.openflexo.foundation.fml.cli.parser.node.AQuitDirective;
import org.openflexo.foundation.fml.cli.parser.node.AResourcesDirective;
import org.openflexo.foundation.fml.cli.parser.node.AScientificNotationNumberNumber;
import org.openflexo.foundation.fml.cli.parser.node.AServiceDirective;
import org.openflexo.foundation.fml.cli.parser.node.AServicesDirective;
import org.openflexo.foundation.fml.cli.parser.node.ASinFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.ASqrtFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.AStringValueTerm;
import org.openflexo.foundation.fml.cli.parser.node.ASubExprExpr2;
import org.openflexo.foundation.fml.cli.parser.node.ATailTypeReferencePath;
import org.openflexo.foundation.fml.cli.parser.node.ATanFuncFunction;
import org.openflexo.foundation.fml.cli.parser.node.ATermExpr3;
import org.openflexo.foundation.fml.cli.parser.node.ATrueConstant;
import org.openflexo.foundation.fml.cli.parser.node.ATypeReferenceAdditionalArg;
import org.openflexo.foundation.fml.cli.parser.node.ATypeReferenceArgList;
import org.openflexo.foundation.fml.cli.parser.node.Node;
import org.openflexo.foundation.fml.cli.parser.node.PBinding;
import org.openflexo.foundation.fml.cli.parser.node.PTypeReference;
import org.openflexo.foundation.fml.cli.parser.node.PTypeReferenceAdditionalArg;
import org.openflexo.foundation.fml.cli.parser.node.PTypeReferenceArgList;
import org.openflexo.foundation.fml.cli.parser.node.PTypeReferencePath;
import org.openflexo.foundation.fml.cli.parser.node.TCharsValue;
import org.openflexo.foundation.fml.cli.parser.node.TDecimalNumber;
import org.openflexo.foundation.fml.cli.parser.node.TPreciseNumber;
import org.openflexo.foundation.fml.cli.parser.node.TScientificNotationNumber;
import org.openflexo.foundation.fml.cli.parser.node.TStringValue;

/**
 * This class implements the semantics analyzer for a parsed FML command.<br>
 * Its main purpose is to build a syntax tree with AnTAR expression model from a parsed AST.
 * 
 * @author sylvain
 * 
 */
public class CommandSemanticsAnalyzer extends DepthFirstAdapter {

	private final Map<Node, Expression> expressionNodes;
	private AbstractCommand command;
	private AbstractCommandInterpreter commandInterpreter;

	public CommandSemanticsAnalyzer(AbstractCommandInterpreter commandInterpreter) {
		expressionNodes = new Hashtable<>();
		this.commandInterpreter = commandInterpreter;
	}

	public AbstractCommand getCommand() {
		return command;
	}

	private void registerExpressionNode(Node n, Expression e) {
		// System.out.println("REGISTER " + e + " for node " + n + " as " + n.getClass());
		expressionNodes.put(n, e);
	}

	private void registerCommand(Node n, AbstractCommand command) {
		this.command = command;
	}

	public Expression getExpression(Node n) {
		if (n != null) {
			Expression returned = expressionNodes.get(n);
			if (returned == null) {
				System.out.println("No expression registered for " + n + " of  " + n.getClass());
			}
			return returned;
		}
		return null;
	}

	private BindingValue makeBinding(PBinding node) {
		// System.out.println("Make binding with " + node);

		// Apply the translation.
		BindingSemanticsAnalyzer bsa = new BindingSemanticsAnalyzer(node, commandInterpreter);

		// System.out.println("Built bsa as " + bsa.getPath());

		node.apply(bsa);

		// System.out.println("Make binding value with bsa as " + bsa.getPath());

		BindingValue returned = new BindingValue(bsa.getPath());
		// System.out.println("Made binding as " + bsa.getPath());

		registerExpressionNode(node, returned);
		return returned;
	}

	private TypeReference makeTypeReference(PTypeReference node) {
		if (node instanceof ABasicTypeReference) {
			return makeBasicTypeReference((ABasicTypeReference) node);
		}
		else if (node instanceof AParameteredTypeReference) {
			return makeParameteredTypeReference((AParameteredTypeReference) node);
		}
		System.err.println("Unexpected " + node);
		return null;
	}

	private String makeReferencePath(PTypeReferencePath path) {
		if (path instanceof AIdentifierTypeReferencePath) {
			return ((AIdentifierTypeReferencePath) path).getIdentifier().getText();
		}
		else if (path instanceof ATailTypeReferencePath) {
			return ((ATailTypeReferencePath) path).getIdentifier().getText() + "."
					+ makeReferencePath(((ATailTypeReferencePath) path).getTypeReferencePath());
		}
		System.err.println("Unexpected " + path);
		return null;
	}

	private TypeReference makeBasicTypeReference(ABasicTypeReference node) {
		return new TypeReference(makeReferencePath(node.getTypeReferencePath()));
	}

	private TypeReference makeParameteredTypeReference(AParameteredTypeReference node) {
		PTypeReferenceArgList argList = node.getTypeReferenceArgList();
		List<TypeReference> args = new ArrayList<>();
		if (argList instanceof ATypeReferenceArgList) {
			args.add(makeTypeReference(((ATypeReferenceArgList) argList).getTypeReference()));
			for (PTypeReferenceAdditionalArg aa : ((ATypeReferenceArgList) argList).getTypeReferenceAdditionalArgs()) {
				ATypeReferenceAdditionalArg additionalArg = (ATypeReferenceAdditionalArg) aa;
				args.add(makeTypeReference(additionalArg.getTypeReference()));
			}
		}
		return new TypeReference(makeReferencePath(node.getTypeReferencePath()), args);
	}

	private IntegerConstant makeDecimalNumber(TDecimalNumber node) {
		// System.out.println("Make decimal number with " + node + " as " + Long.parseLong(node.getText()));
		IntegerConstant returned = new IntegerConstant(Long.parseLong(node.getText()));
		registerExpressionNode(node, returned);
		return returned;
	}

	private FloatConstant makePreciseNumber(TPreciseNumber node) {
		// System.out.println("Make precise number with " + node + " as " + Double.parseDouble(node.getText()));
		FloatConstant returned = new FloatConstant(Double.parseDouble(node.getText()));
		registerExpressionNode(node, returned);
		return returned;
	}

	private FloatConstant makeScientificNotationNumber(TScientificNotationNumber node) {
		// System.out.println("Make scientific notation number with " + node + " as " + Double.parseDouble(node.getText()));
		FloatConstant returned = new FloatConstant(Double.parseDouble(node.getText()));
		registerExpressionNode(node, returned);
		return returned;
	}

	private StringConstant makeStringValue(TStringValue node) {
		// System.out.println("Make string value with " + node);
		StringConstant returned = new StringConstant(node.getText().substring(1, node.getText().length() - 1));
		registerExpressionNode(node, returned);
		return returned;
	}

	private StringConstant makeCharsValue(TCharsValue node) {
		// System.out.println("Make chars value with " + node);
		StringConstant returned = new StringConstant(node.getText().substring(1, node.getText().length() - 1));
		registerExpressionNode(node, returned);
		return returned;
	}

	// Following methods manage following grammar fragment
	/*expr =
	  {expr2} expr2 |
	  {cond_expr} [condition]:expr if_token [then]:expr2 else_token [else]:expr2 |
	  {eq_expr} [left]:expr eq [right]:expr2 |
	  {eq2_expr} [left]:expr eq2 [right]:expr2 |
	  {neq_expr} [left]:expr neq [right]:expr2 |
	  {lt_expr} [left]:expr lt [right]:expr2 |
	  {gt_expr} [left]:expr gt [right]:expr2 |
	  {lte_expr} [left]:expr lte [right]:expr2 |
	  {gte_expr} [left]:expr gte [right]:expr2 ;*/

	@Override
	public void outAExpr2Expr(AExpr2Expr node) {
		super.outAExpr2Expr(node);
		registerExpressionNode(node, getExpression(node.getExpr2()));
	}

	@Override
	public void outACondExprExpr(ACondExprExpr node) {
		super.outACondExprExpr(node);
		// System.out.println("On chope une conditionnelle avec cond:" + node.getCondition() + " then:" + node.getThen() + " else:"+
		// node.getElse());
		registerExpressionNode(node, new ConditionalExpression(getExpression(node.getCondition()), getExpression(node.getThen()),
				getExpression(node.getElse())));
	}

	@Override
	public void outAEqExprExpr(AEqExprExpr node) {
		super.outAEqExprExpr(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.EQUALS, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAEq2ExprExpr(AEq2ExprExpr node) {
		super.outAEq2ExprExpr(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.EQUALS, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outANeqExprExpr(ANeqExprExpr node) {
		super.outANeqExprExpr(node);
		registerExpressionNode(node, new BinaryOperatorExpression(BooleanBinaryOperator.NOT_EQUALS, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outALtExprExpr(ALtExprExpr node) {
		super.outALtExprExpr(node);
		registerExpressionNode(node, new BinaryOperatorExpression(BooleanBinaryOperator.LESS_THAN, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outALteExprExpr(ALteExprExpr node) {
		super.outALteExprExpr(node);
		registerExpressionNode(node, new BinaryOperatorExpression(BooleanBinaryOperator.LESS_THAN_OR_EQUALS, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outAGtExprExpr(AGtExprExpr node) {
		super.outAGtExprExpr(node);
		registerExpressionNode(node, new BinaryOperatorExpression(BooleanBinaryOperator.GREATER_THAN, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outAGteExprExpr(AGteExprExpr node) {
		super.outAGteExprExpr(node);
		registerExpressionNode(node, new BinaryOperatorExpression(BooleanBinaryOperator.GREATER_THAN_OR_EQUALS,
				getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	// Following methods manage following grammar fragment
	/* expr2 =
	  {expr3} expr3 |
	  {or_expr} [left]:expr2 or [right]:expr3 |
	  {or2_expr} [left]:expr2 or2 [right]:expr3 |
	  {add_expr} [left]:expr2 plus [right]:expr3 |
	  {sub_expr} [left]:expr2 minus [right]:expr3; */

	@Override
	public void outAExpr3Expr2(AExpr3Expr2 node) {
		// System.out.println("OUT Expr3-Expr2 with " + node);
		super.outAExpr3Expr2(node);
		registerExpressionNode(node, getExpression(node.getExpr3()));
		// System.out.println("***** AExpr3Expr2 " + node + "expression=" + getExpression(node.getExpr3()));
	}

	@Override
	public void outAOrExprExpr2(AOrExprExpr2 node) {
		super.outAOrExprExpr2(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.OR, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAOr2ExprExpr2(AOr2ExprExpr2 node) {
		super.outAOr2ExprExpr2(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.OR, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAAddExprExpr2(AAddExprExpr2 node) {
		super.outAAddExprExpr2(node);
		// System.out.println("OUT add with " + node);
		registerExpressionNode(node, new BinaryOperatorExpression(ArithmeticBinaryOperator.ADDITION, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outASubExprExpr2(ASubExprExpr2 node) {
		super.outASubExprExpr2(node);
		registerExpressionNode(node, new BinaryOperatorExpression(ArithmeticBinaryOperator.SUBSTRACTION, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	// Following methods manage following grammar fragment
	/* expr3 =
		  {term} term |
		  {and_expr} [left]:expr3 and [right]:term |
		  {and2_expr} [left]:expr3 and2 [right]:term |
		  {mult_expr} [left]:expr3 mult [right]:term |
		  {div_expr} [left]:expr3 div [right]:term |
		  {mod_expr} [left]:expr3 mod [right]:term |
	      {power_expr} [left]:expr3 power [right]:term |
		  {not_expr} not term; */

	@Override
	public void outATermExpr3(ATermExpr3 node) {
		// System.out.println("OUT Term-Expr3 with " + node + " term=" + node.getTerm() + " of " + node.getTerm().getClass());
		super.outATermExpr3(node);
		registerExpressionNode(node, getExpression(node.getTerm()));
		// System.out.println("***** ATermExpr3 " + node + "expression=" + getExpression(node.getTerm()));
	}

	@Override
	public void outAAndExprExpr3(AAndExprExpr3 node) {
		super.outAAndExprExpr3(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.AND, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAAnd2ExprExpr3(AAnd2ExprExpr3 node) {
		super.outAAnd2ExprExpr3(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(BooleanBinaryOperator.AND, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAMultExprExpr3(AMultExprExpr3 node) {
		super.outAMultExprExpr3(node);
		// System.out.println("OUT mult with " + node);
		registerExpressionNode(node, new BinaryOperatorExpression(ArithmeticBinaryOperator.MULTIPLICATION, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outADivExprExpr3(ADivExprExpr3 node) {
		super.outADivExprExpr3(node);
		registerExpressionNode(node, new BinaryOperatorExpression(ArithmeticBinaryOperator.DIVISION, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outAModExprExpr3(AModExprExpr3 node) {
		super.outAModExprExpr3(node);
		registerExpressionNode(node,
				new BinaryOperatorExpression(ArithmeticBinaryOperator.MOD, getExpression(node.getLeft()), getExpression(node.getRight())));
	}

	@Override
	public void outAPowerExprExpr3(APowerExprExpr3 node) {
		super.outAPowerExprExpr3(node);
		registerExpressionNode(node, new BinaryOperatorExpression(ArithmeticBinaryOperator.POWER, getExpression(node.getLeft()),
				getExpression(node.getRight())));
	}

	@Override
	public void outANotExprExpr3(ANotExprExpr3 node) {
		super.outANotExprExpr3(node);
		registerExpressionNode(node, new UnaryOperatorExpression(BooleanUnaryOperator.NOT, getExpression(node.getTerm())));
	}

	// Following methods manage following grammar fragment
	/* function =
	  {cos_func} cos l_par expr2 r_par |
	  {acos_func} acos l_par expr2 r_par |
	  {sin_func} sin l_par expr2 r_par |
	  {asin_func} asin l_par expr2 r_par |
	  {tan_func} tan l_par expr2 r_par |
	  {atan_func} atan l_par expr2 r_par |
	  {exp_func} exp l_par expr2 r_par |
	  {log_func} log l_par expr2 r_par |
	  {sqrt_func} sqrt l_par expr2 r_par; */

	@Override
	public void outACosFuncFunction(ACosFuncFunction node) {
		super.outACosFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.COS, getExpression(node.getExpr2())));
	}

	@Override
	public void outAAcosFuncFunction(AAcosFuncFunction node) {
		super.outAAcosFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.ACOS, getExpression(node.getExpr2())));
	}

	@Override
	public void outASinFuncFunction(ASinFuncFunction node) {
		super.outASinFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.SIN, getExpression(node.getExpr2())));
	}

	@Override
	public void outAAsinFuncFunction(AAsinFuncFunction node) {
		super.outAAsinFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.ASIN, getExpression(node.getExpr2())));
	}

	@Override
	public void outATanFuncFunction(ATanFuncFunction node) {
		super.outATanFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.TAN, getExpression(node.getExpr2())));
	}

	@Override
	public void outAAtanFuncFunction(AAtanFuncFunction node) {
		super.outAAtanFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.ATAN, getExpression(node.getExpr2())));
	}

	@Override
	public void outAExpFuncFunction(AExpFuncFunction node) {
		super.outAExpFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.EXP, getExpression(node.getExpr2())));
	}

	@Override
	public void outALogFuncFunction(ALogFuncFunction node) {
		super.outALogFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.LOG, getExpression(node.getExpr2())));
	}

	@Override
	public void outASqrtFuncFunction(ASqrtFuncFunction node) {
		super.outASqrtFuncFunction(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.SQRT, getExpression(node.getExpr2())));
	}

	// Following methods manage following grammar fragment
	/* constant = 
		  {true} true |
		  {false} false |
		  {null} null |
		  {this} this |
		  {pi} pi;*/

	@Override
	public void outATrueConstant(ATrueConstant node) {
		super.outATrueConstant(node);
		registerExpressionNode(node, BooleanConstant.TRUE);
	}

	@Override
	public void outAFalseConstant(AFalseConstant node) {
		super.outAFalseConstant(node);
		registerExpressionNode(node, BooleanConstant.FALSE);
	}

	@Override
	public void outAPiConstant(APiConstant node) {
		super.outAPiConstant(node);
		registerExpressionNode(node, FloatSymbolicConstant.PI);
	}

	@Override
	public void outANullConstant(ANullConstant node) {
		super.outANullConstant(node);
		registerExpressionNode(node, ObjectSymbolicConstant.NULL);
	}

	// Following methods manage following grammar fragment
	/* number =
		  {decimal_number} decimal_number |
		  {precise_number} precise_number |
		  {scientific_notation_number} scientific_notation_number |
		  {constant} constant; */

	@Override
	public void outADecimalNumberNumber(ADecimalNumberNumber node) {
		super.outADecimalNumberNumber(node);
		registerExpressionNode(node, makeDecimalNumber(node.getDecimalNumber()));
	}

	@Override
	public void outAPreciseNumberNumber(APreciseNumberNumber node) {
		super.outAPreciseNumberNumber(node);
		registerExpressionNode(node, makePreciseNumber(node.getPreciseNumber()));
	}

	@Override
	public void outAScientificNotationNumberNumber(AScientificNotationNumberNumber node) {
		super.outAScientificNotationNumberNumber(node);
		registerExpressionNode(node, makeScientificNotationNumber(node.getScientificNotationNumber()));
	}

	@Override
	public void outAConstantNumber(AConstantNumber node) {
		super.outAConstantNumber(node);
		registerExpressionNode(node, getExpression(node.getConstant()));
	}

	// Following methods manage following grammar fragment
	/* term =
		  {negative} minus term |
		  {number} number |
		  {string_value} string_value |
		  {chars_value} chars_value |
		  {function} function |
		  {binding} binding |
		  {expr} l_par expr r_par |
		  {cast} l_par type_reference r_par term;*/

	@Override
	public void outACastTerm(ACastTerm node) {
		super.outACastTerm(node);
		registerExpressionNode(node, new CastExpression(makeTypeReference(node.getTypeReference()), getExpression(node.getTerm())));
	}

	@Override
	public void outANegativeTerm(ANegativeTerm node) {
		super.outANegativeTerm(node);
		registerExpressionNode(node, new UnaryOperatorExpression(ArithmeticUnaryOperator.UNARY_MINUS, getExpression(node.getTerm())));
	}

	@Override
	public void outANumberTerm(ANumberTerm node) {
		super.outANumberTerm(node);
		registerExpressionNode(node, getExpression(node.getNumber()));
	}

	@Override
	public void outAStringValueTerm(AStringValueTerm node) {
		super.outAStringValueTerm(node);
		registerExpressionNode(node, makeStringValue(node.getStringValue()));
	}

	@Override
	public void outACharsValueTerm(ACharsValueTerm node) {
		super.outACharsValueTerm(node);
		registerExpressionNode(node, makeCharsValue(node.getCharsValue()));
	}

	@Override
	public void outAFunctionTerm(AFunctionTerm node) {
		super.outAFunctionTerm(node);
		registerExpressionNode(node, getExpression(node.getFunction()));
	}

	@Override
	public void outABindingTerm(ABindingTerm node) {
		super.outABindingTerm(node);
		registerExpressionNode(node, makeBinding(node.getBinding()));
	}

	@Override
	public void outAExprTerm(AExprTerm node) {
		super.outAExprTerm(node);
		registerExpressionNode(node, getExpression(node.getExpr()));
	}

	// DIRECTIVES

	@Override
	public void outAPwdDirective(APwdDirective node) {
		super.outAPwdDirective(node);
		registerCommand(node, new PwdDirective(node, commandInterpreter));
	}

	@Override
	public void outALsDirective(ALsDirective node) {
		super.outALsDirective(node);
		registerCommand(node, new LsDirective(node, commandInterpreter));
	}

	@Override
	public void outACdDirective(ACdDirective node) {
		super.outACdDirective(node);
		registerCommand(node, new CdDirective(node, commandInterpreter));
	}

	@Override
	public void outAServicesDirective(AServicesDirective node) {
		super.outAServicesDirective(node);
		registerCommand(node, new ServicesDirective(node, commandInterpreter));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void outAServiceDirective(AServiceDirective node) {
		super.outAServiceDirective(node);
		registerCommand(node, new ServiceDirective(node, commandInterpreter));
	}

	@Override
	public void outAActivateTaDirective(AActivateTaDirective node) {
		super.outAActivateTaDirective(node);
		registerCommand(node, new ActivateTA(node, commandInterpreter));
	}

	@Override
	public void outAResourcesDirective(AResourcesDirective node) {
		super.outAResourcesDirective(node);
		registerCommand(node, new ResourcesDirective(node, commandInterpreter));
	}

	@Override
	public void outAOpenDirective(AOpenDirective node) {
		super.outAOpenDirective(node);
		registerCommand(node, new OpenProject(node, commandInterpreter));
	}

	@Override
	public void outALoadDirective(ALoadDirective node) {
		super.outALoadDirective(node);
		registerCommand(node, new LoadResource(node, commandInterpreter));
	}

	@Override
	public void outADisplayDirective(ADisplayDirective node) {
		super.outADisplayDirective(node);
		registerCommand(node, new DisplayResource(node, commandInterpreter));
	}

	@Override
	public void outAEnterDirective(AEnterDirective node) {
		super.outAEnterDirective(node);
		registerCommand(node, new EnterDirective(node, commandInterpreter));
	}

	@Override
	public void outAExitDirective(AExitDirective node) {
		super.outAExitDirective(node);
		registerCommand(node, new ExitDirective(node, commandInterpreter));
	}

	@Override
	public void outAQuitDirective(AQuitDirective node) {
		super.outAQuitDirective(node);
		registerCommand(node, new QuitDirective(node, commandInterpreter));
	}

	@Override
	public void outAHelpDirective(AHelpDirective node) {
		super.outAHelpDirective(node);
		registerCommand(node, new HelpDirective(node, commandInterpreter));
	}

	// COMMANDS

	@Override
	public void outAContextFmlCommand(AContextFmlCommand node) {
		super.outAContextFmlCommand(node);
		registerCommand(node, new FMLContextCommand(node, commandInterpreter, this));
	}

	@Override
	public void outAAssignationFmlCommand(AAssignationFmlCommand node) {
		super.outAAssignationFmlCommand(node);
		registerCommand(node, new FMLAssignation(node, commandInterpreter, this));
	}

	@Override
	public void outAExprFmlCommand(AExprFmlCommand node) {
		super.outAExprFmlCommand(node);
		registerCommand(node, new FMLExpression(node, commandInterpreter, this));
	}
}
