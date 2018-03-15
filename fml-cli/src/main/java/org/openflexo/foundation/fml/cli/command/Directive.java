/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation.fml.cli.command;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.parser.node.ADotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPath;
import org.openflexo.foundation.fml.cli.parser.node.ADoubleDotPathPath;
import org.openflexo.foundation.fml.cli.parser.node.AIdentifierPath;
import org.openflexo.foundation.fml.cli.parser.node.APathPath;
import org.openflexo.foundation.fml.cli.parser.node.Node;
import org.openflexo.foundation.fml.cli.parser.node.PPath;

/**
 * Represents a directive in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
public abstract class Directive extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(Directive.class.getPackage().getName());

	public Directive(Node node, CommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);
	}

	protected String retrievePath(PPath path) {
		if (path instanceof ADoubleDotPath) {
			return "..";
		}
		else if (path instanceof ADoubleDotPathPath) {
			return ".." + File.separator + retrievePath(((ADoubleDotPathPath) path).getPath());
		}
		else if (path instanceof ADotPath) {
			return ".";
		}
		else if (path instanceof ADotPathPath) {
			return "." + File.separator + retrievePath(((ADotPathPath) path).getPath());
		}
		else if (path instanceof AIdentifierPath) {
			return ((AIdentifierPath) path).getIdentifier().getText();
		}
		else if (path instanceof APathPath) {
			return ((APathPath) path).getIdentifier().getText() + File.separator + retrievePath(((APathPath) path).getPath());
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String invalidCommandReason() {
		return null;
	}

}
