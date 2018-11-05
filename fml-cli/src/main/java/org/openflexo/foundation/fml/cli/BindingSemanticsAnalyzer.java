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
import java.util.List;

import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.cli.parser.node.AAdditionalArg;
import org.openflexo.foundation.fml.cli.parser.node.ABindingTerm;
import org.openflexo.foundation.fml.cli.parser.node.ACall;
import org.openflexo.foundation.fml.cli.parser.node.ACallBinding;
import org.openflexo.foundation.fml.cli.parser.node.AIdentifierBinding;
import org.openflexo.foundation.fml.cli.parser.node.ANonEmptyListArgList;
import org.openflexo.foundation.fml.cli.parser.node.ATail1Binding;
import org.openflexo.foundation.fml.cli.parser.node.ATail2Binding;
import org.openflexo.foundation.fml.cli.parser.node.PAdditionalArg;
import org.openflexo.foundation.fml.cli.parser.node.PArgList;
import org.openflexo.foundation.fml.cli.parser.node.PBinding;
import org.openflexo.foundation.fml.cli.parser.node.TIdentifier;

/**
 * This class implements the semantics analyzer for a parsed AnTAR binding.<br>
 * Its main purpose is to structurally build a binding from a parsed AST.<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
class BindingSemanticsAnalyzer extends CommandSemanticsAnalyzer {

	private final List<BindingValue.AbstractBindingPathElement> path;

	/**
	 * This flag is used to escape binding processing that may happen in call args handling
	 */
	// private boolean weAreDealingWithTheRightBinding = true;

	public BindingSemanticsAnalyzer(PBinding node, CommandInterpreter commandInterpreter) {
		super(commandInterpreter);
		path = new ArrayList<>();
		// System.out.println(">>>> node=" + node + " of " + node.getClass());
	}

	public List<BindingValue.AbstractBindingPathElement> getPath() {
		return path;
	};

	/* call = 
	  identifier arg_list ;
	
	 arg_list = 
	  l_par expr [additional_args]:additional_arg* r_par;
	
	 additional_arg = 
	  comma expr;
	
	 binding = 
	  {identifier} identifier |
	  {call} call |
	  {tail} identifier dot binding;*/

	protected BindingValue.NormalBindingPathElement makeNormalBindingPathElement(TIdentifier identifier) {
		BindingValue.NormalBindingPathElement returned = new BindingValue.NormalBindingPathElement(identifier.getText());
		if (weAreDealingWithTheRightBinding()) {
			path.add(0, returned);
		}
		return returned;
	}

	public BindingValue.MethodCallBindingPathElement makeMethodCallBindingPathElement(ACall node) {
		PArgList argList = node.getArgList();
		List<Expression> args = new ArrayList<>();
		if (argList instanceof ANonEmptyListArgList) {
			args.add(getExpression(((ANonEmptyListArgList) argList).getExpr()));
			for (PAdditionalArg aa : ((ANonEmptyListArgList) argList).getAdditionalArgs()) {
				AAdditionalArg additionalArg = (AAdditionalArg) aa;
				args.add(getExpression(additionalArg.getExpr()));
			}
		}
		BindingValue.MethodCallBindingPathElement returned = new BindingValue.MethodCallBindingPathElement(node.getIdentifier().getText(),
				args);
		if (weAreDealingWithTheRightBinding()) {
			path.add(0, returned);
		}
		return returned;
	}

	@Override
	public void outAIdentifierBinding(AIdentifierBinding node) {
		super.outAIdentifierBinding(node);
		if (weAreDealingWithTheRightBinding()) {
			makeNormalBindingPathElement(node.getIdentifier());
		}
	}

	@Override
	public void outACallBinding(ACallBinding node) {
		super.outACallBinding(node);
		if (weAreDealingWithTheRightBinding()) {
			makeMethodCallBindingPathElement((ACall) node.getCall());
		}
	}

	@Override
	public void outATail1Binding(ATail1Binding node) {
		super.outATail1Binding(node);
		if (weAreDealingWithTheRightBinding()) {
			makeNormalBindingPathElement(node.getIdentifier());
		}
	}

	@Override
	public void outATail2Binding(ATail2Binding node) {
		super.outATail2Binding(node);
		if (weAreDealingWithTheRightBinding()) {
			makeMethodCallBindingPathElement((ACall) node.getCall());
		}
	}

	private int depth = 0;

	@Override
	public void inABindingTerm(ABindingTerm node) {
		super.inABindingTerm(node);
		// System.out.println("IN binding " + node);
		// weAreDealingWithTheRightBinding = false;
		depth++;
	}

	@Override
	public void outABindingTerm(ABindingTerm node) {
		super.outABindingTerm(node);
		// System.out.println("OUT binding " + node);
		// weAreDealingWithTheRightBinding = true;
		depth--;
	}

	private boolean weAreDealingWithTheRightBinding() {
		return depth == 0;
	}
}
