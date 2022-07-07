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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.CLIUtils;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AEnterDirective;
import org.openflexo.foundation.fml.parser.node.AObjectEnterDirective;
import org.openflexo.foundation.fml.parser.node.APathEnterDirective;
import org.openflexo.foundation.fml.parser.node.AResourceEnterDirective;
import org.openflexo.foundation.fml.parser.node.PEnterDirective;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents enter directive in FML command-line interpreter
 * 
 * Usage: enter <resource>|<expression> where <resource> represents a resource or <expression> represent an expression pointing on the
 * object in which to enter
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(EnterDirective.EnterDirectiveImpl.class)
@DirectiveDeclaration(
		keyword = "enter",
		usage = "enter <expression> | -r <resource> | -f <path>",
		description = "Enter in a given object, denoted by a resource or an expression",
		syntax = "enter <expression> | -r <resource> | -f <path>")
public interface EnterDirective extends Directive<AEnterDirective> {

	public static abstract class EnterDirectiveImpl extends DirectiveImpl<AEnterDirective> implements EnterDirective {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(EnterDirective.class.getPackage().getName());

		private FlexoResource<?> resource;
		private String path;
		private DataBinding<?> expression;

		@Override
		public void create(AEnterDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			PEnterDirective enterDirective = node.getEnterDirective();

			if (enterDirective instanceof AResourceEnterDirective) {
				resource = retrieveResource(((AResourceEnterDirective) enterDirective).getReferenceByUri());
			}
			else if (enterDirective instanceof APathEnterDirective) {
				path = retrievePath(((APathEnterDirective) enterDirective).getPath());
			}
			else if (enterDirective instanceof AObjectEnterDirective) {
				PExpression referencedObject = ((AObjectEnterDirective) enterDirective).getExpression();
				expression = retrieveExpression(referencedObject);
			}
		}

		@Override
		public String toString() {
			if (StringUtils.isNotEmpty(path)) {
				return "enter -f " + path;
			}
			else if (resource != null) {
				return "enter -r [\"" + resource.getURI() + "\"]";
			}
			else if (expression != null) {
				return "enter " + expression;
			}
			return "enter ?";
		}

		public FlexoResource<?> getResource() {
			return resource;
		}

		public Object getAddressedObject() {
			if (StringUtils.isNotEmpty(path)) {
				FlexoResource<?> adressedResource = retrieveResourceFromPath(path);
				if (adressedResource != null) {
					try {
						return adressedResource.getResourceData();
					} catch (FileNotFoundException e) {
						getErrStream().println("Cannot enter into " + path + " : file not found");
					} catch (ResourceLoadingCancelledException e) {
						getErrStream().println("Cannot enter into " + path + " : cancelled loading");
					} catch (FlexoException e) {
						getErrStream().println("Cannot enter into " + path + " : unexpected exception");
						e.printStackTrace();
					}
					return null;
				}
				getErrStream().println("Cannot enter into " + path + " : not a resource");
			}
			else if (resource != null) {
				try {
					return resource.getResourceData();
				} catch (FileNotFoundException e) {
					getErrStream().println("Cannot enter into " + path + " : file not found");
				} catch (ResourceLoadingCancelledException e) {
					getErrStream().println("Cannot enter into " + path + " : cancelled loading");
				} catch (FlexoException e) {
					getErrStream().println("Cannot enter into " + path + " : unexpected exception");
					e.printStackTrace();
				}
				return null;
			}
			else if (expression != null) {
				try {
					return expression.getBindingValue(getCommandInterpreter());
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return "enter ?";
		}

		@Override
		public FlexoObject execute() throws FMLCommandExecutionException {
			super.execute();

			Object object = getAddressedObject();
			if (object instanceof FlexoObject) {
				getOutStream().println("Entering in context " + CLIUtils.denoteObject(object));
				getCommandInterpreter().enterFocusedObject((FlexoObject) object);
				return (FlexoObject) object;
			}
			else {
				throw new FMLCommandExecutionException("Cannot enter into " + path + " : not a FlexoObject");
			}
		}
	}
}
