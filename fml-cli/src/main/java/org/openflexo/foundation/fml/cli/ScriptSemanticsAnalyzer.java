/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.parser.FragmentManager;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.p2pp.RawSource;

/**
 * This class implements the main semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class ScriptSemanticsAnalyzer extends AbstractCommandSemanticsAnalyzer {

	private static final Logger logger = Logger.getLogger(ScriptSemanticsAnalyzer.class.getPackage().getName());

	private final AbstractFMLTypingSpace typingSpace;

	// Raw source as when this analyzer was last parsed
	private RawSource rawSource;

	private FragmentManager fragmentManager;

	private FMLBindingFactory bindingFactory; // = new FMLBindingFactory();

	private FMLScript script;

	public ScriptSemanticsAnalyzer(AbstractCommandInterpreter commandInterpreter, Start tree, RawSource rawSource) {
		super(commandInterpreter, tree);
		this.rawSource = rawSource;
		fragmentManager = new FragmentManager(rawSource);
		bindingFactory = new FMLBindingFactory(commandInterpreter.getModelFactory());
		typingSpace = new FMLScriptTypingSpace(this);
		script = new FMLScript(tree, this);
	}

	@Override
	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	@Override
	public FMLBindingFactory getFMLBindingFactory() {
		return bindingFactory;
	}

	@Override
	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	@Override
	public RawSource getRawSource() {
		return rawSource;
	}

	@Override
	public Start getRootNode() {
		return super.getRootNode();
	}

	public FMLScript getScript() {
		return script;
	}

	/*@Override
	public void outACommandInScript(ACommandInScript node) {
		// TODO Auto-generated method stub
		super.outACommandInScript(node);
		System.out.println(">>>> Found command: " + node.getCommand());
	}*/

	@Override
	protected void registerCommand(Node n, AbstractCommand command) {
		System.out.println("Register new command in script: " + command);
		script.addToCommands(command);
		/*if (command instanceof FMLAssignation) {
			((FMLAssignation) command).declareVariableWhenRequired();
		}*/
	}

}
