/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Cartoeditor, a component of the software infrastructure 
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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;

/**
 * Provides a testing environment for testing FMLParser
 * 
 * @author sylvain
 *
 */
public abstract class FMLScriptParserTestCase extends OpenflexoTestCase {

	protected static FMLScript parseFMLScript(Resource fileResource, AbstractCommandInterpreter commandInterpreter)
			throws ModelDefinitionException, ParseException, IOException {

		System.out.println("Load " + fileResource);

		FMLModelFactory fmlModelFactory = new FMLModelFactory(null, serviceManager);
		FMLScriptParser parser = new FMLScriptParser();
		FMLScript script = parser.parse(fileResource.openInputStream(), fmlModelFactory, commandInterpreter);
		return script;
	}

	protected static FMLScript checkFMLScript(String scriptName, FMLScript script)
			throws ModelDefinitionException, ParseException, IOException {

		for (AbstractCommand command : script.getCommands()) {
			assertTrue("Script " + scriptName + ", line " + command.getLine() + " > invalid command " + command + " : "
					+ command.invalidCommandReason(), command.isSyntaxicallyValid());
		}
		return script;
	}

}
