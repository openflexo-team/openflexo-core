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

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.cli.CommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AMoreDirective;
import org.openflexo.foundation.fml.cli.parser.node.AObjectMoreDirective;
import org.openflexo.foundation.fml.cli.parser.node.APlainMoreDirective;
import org.openflexo.foundation.fml.cli.parser.node.AResourceMoreDirective;
import org.openflexo.foundation.fml.cli.parser.node.PBinding;
import org.openflexo.foundation.fml.cli.parser.node.PMoreDirective;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;

/**
 * Represents more resource directive in FML command-line interpreter
 * 
 * Usage: display <resource> where <resource> represents a resource
 * 
 * @author sylvain
 * 
 */
@DirectiveDeclaration(
		keyword = "more",
		usage = "more [<expression> | -r <resource>]",
		description = "Print informations on current object, or expression or resource when supplied",
		syntax = "more | <reference> | <expression> | -r <resource>")
public class MoreDirective extends Directive {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MoreDirective.class.getPackage().getName());

	private FlexoResource<?> resource;
	private Object object;
	private boolean isPlain = false;

	public MoreDirective(AMoreDirective node, CommandSemanticsAnalyzer commandSemanticsAnalyzer) {
		super(node, commandSemanticsAnalyzer);
		System.out.println("New EnterDirective");

		PMoreDirective moreDirective = node.getMoreDirective();

		if (moreDirective instanceof APlainMoreDirective) {
			isPlain = true;
		}
		else if (moreDirective instanceof AResourceMoreDirective) {
			resource = getResource(((AResourceMoreDirective) moreDirective).getResourceUri().getText());
		}
		else if (moreDirective instanceof AObjectMoreDirective) {
			PBinding referencedObject = ((AObjectMoreDirective) moreDirective).getBinding();
			// System.out.println("On entre dans l'objet: " + referencedObject);
			object = evaluate(referencedObject, CommandTokenType.LocalReference);
			// System.out.println("Found as local reference: " + object);
			if (object == null) {
				object = evaluate(referencedObject, CommandTokenType.Expression);
				// System.out.println("Found as expression: " + object);
			}
		}
	}

	public FlexoResource<?> getResource() {
		return resource;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public void execute() {
		if (isPlain) {
			if (getCommandInterpreter().getFocusedObject() == null) {
				getErrStream().println("No focused object");
			}
			else {
				getOutStream().println("Tiens on fait un truc ?");
			}
		}
		else if (resource != null) {
			if (!resource.isLoaded()) {
				try {
					resource.loadResourceData();
				} catch (FileNotFoundException e) {
					getErrStream().println("Cannot find resource " + resource.getURI());
				} catch (ResourceLoadingCancelledException e) {
				} catch (FlexoException e) {
					getErrStream().println("Cannot load resource " + resource.getURI() + " : " + e.getMessage());
				}
			}
			if (resource instanceof VirtualModelResource) {
				getOutStream().println(((VirtualModelResource) resource).getVirtualModel().getFMLRepresentation());
			}
			else {
				getErrStream().println("No textual renderer for such data.");
			}
		}
		else if (object != null) {
			getOutStream().println("Tiens on fait un truc la non ?");
		}
		else {
			getErrStream().println("Cannot access to object");
		}
	}
}
