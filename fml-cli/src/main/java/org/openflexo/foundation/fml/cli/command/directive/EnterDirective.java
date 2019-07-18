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

package org.openflexo.foundation.fml.cli.command.directive;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AEnterDirective;
import org.openflexo.foundation.fml.cli.parser.node.AObjectEnterDirective;
import org.openflexo.foundation.fml.cli.parser.node.AResourceEnterDirective;
import org.openflexo.foundation.fml.cli.parser.node.PEnterDirective;
import org.openflexo.foundation.fml.cli.parser.node.PExpr;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResource;

/**
 * Represents enter directive in FML command-line interpreter
 * 
 * Usage: enter <resource>|<expression> where <resource> represents a resource or <expression> represent an expression pointing on the
 * object in which to enter
 * 
 * @author sylvain
 * 
 */
@DirectiveDeclaration(
		keyword = "enter",
		usage = "enter <instance> | <expression> | -r <resource>",
		description = "Enter in a given object, denoted by a resource, an expression or a local reference to a FML instance",
		syntax = "enter <instance> | <expression> | -r <resource>")
public class EnterDirective extends Directive {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EnterDirective.class.getPackage().getName());

	private FlexoResource<?> resource;
	private FlexoObject object;

	public EnterDirective(AEnterDirective node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer);

		System.out.println("New EnterDirective");

		PEnterDirective enterDirective = node.getEnterDirective();

		if (enterDirective instanceof AResourceEnterDirective) {
			resource = getResource(((AResourceEnterDirective) enterDirective).getResourceUri().getText());
		}
		else if (enterDirective instanceof AObjectEnterDirective) {
			PExpr expr = ((AObjectEnterDirective) enterDirective).getExpr();
			System.out.println("On cherche l'objet: " + expr);
		}
	}

	public FlexoResource<?> getResource() {
		return resource;
	}

	@Override
	public void execute() {
		if (getResource() instanceof VirtualModelResource) {
			object = ((VirtualModelResource) getResource()).getVirtualModel();
		}
		if (getResource() instanceof AbstractVirtualModelInstanceResource) {
			object = ((AbstractVirtualModelInstanceResource) getResource()).getVirtualModelInstance();
		}
		if (object != null) {
			getOutStream().println("Entering in context " + object);
			getCommandInterpreter().setFocusedObject(object);
		}
		else {
			getErrStream().println("Cannot access denoted context");
		}
	}
}
