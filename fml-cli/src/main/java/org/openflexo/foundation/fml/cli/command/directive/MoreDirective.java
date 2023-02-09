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
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AMoreDirective;
import org.openflexo.foundation.fml.parser.node.AObjectMoreDirective;
import org.openflexo.foundation.fml.parser.node.APathMoreDirective;
import org.openflexo.foundation.fml.parser.node.APlainMoreDirective;
import org.openflexo.foundation.fml.parser.node.AResourceMoreDirective;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PMoreDirective;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents more resource directive in FML command-line interpreter
 * 
 * Usage: display <resource> where <resource> represents a resource
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(MoreDirective.MoreDirectiveImpl.class)
@DirectiveDeclaration(
		keyword = "more",
		usage = "more [<expression> | -r <resource>]",
		description = "Print informations on current object, or expression or resource when supplied",
		syntax = "more | <reference> | <expression> | -r <resource>")
public interface MoreDirective extends Directive<AMoreDirective> {

	public FlexoResource<?> getResource();

	public Object getAddressedObject();

	public static abstract class MoreDirectiveImpl extends DirectiveImpl<AMoreDirective> implements MoreDirective {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(MoreDirective.class.getPackage().getName());

		private boolean isPlain = false;

		private FlexoResource<?> resource;
		private String path;
		private DataBinding<?> expression;

		@Override
		public void create(AMoreDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			PMoreDirective moreDirective = node.getMoreDirective();

			if (moreDirective instanceof APlainMoreDirective) {
				isPlain = true;
			}
			else if (moreDirective instanceof AResourceMoreDirective) {
				resource = retrieveResource(((AResourceMoreDirective) moreDirective).getReferenceByUri());
			}
			else if (moreDirective instanceof APathMoreDirective) {
				path = retrievePath(((APathMoreDirective) moreDirective).getPath());
			}
			else if (moreDirective instanceof AObjectMoreDirective) {
				PExpression referencedObject = ((AObjectMoreDirective) moreDirective).getExpression();
				expression = retrieveExpression(referencedObject);
			}
		}

		@Override
		public String toString() {
			if (isPlain) {
				return "more";
			}
			if (StringUtils.isNotEmpty(path)) {
				return "more -f " + path;
			}
			else if (resource != null) {
				return "more -r [\"" + resource.getURI() + "\"]";
			}
			else if (expression != null) {
				return "more " + expression;
			}
			return "more ?";
		}

		@Override
		public FlexoResource<?> getResource() {
			return resource;
		}

		@Override
		public Object getAddressedObject() {
			if (isPlain) {
				if (getCommandInterpreter().getFocusedObject() == null) {
					getErrStream().println("No focused object");
					return null;
				}
				else {
					return getCommandInterpreter().getFocusedObject();
				}
			}
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
		public Object execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();

			Object object = getAddressedObject();
			if (object instanceof FlexoObject) {
				renderObject((FlexoObject) object);
				return getAddressedObject();
			}
			else {
				String cmdOutput = "No textual renderer for such data.";

				output.add(cmdOutput);
				throw new FMLCommandExecutionException(cmdOutput);
			}
		}

		private void renderObject(FlexoObject object) {
			String cmdOutput = object.render().trim();

			output.add(cmdOutput);
			getOutStream().println(cmdOutput);
		}
	}
}
