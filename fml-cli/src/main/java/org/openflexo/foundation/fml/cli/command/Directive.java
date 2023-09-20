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
import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.cli.command.directive.ActivateTA;
import org.openflexo.foundation.fml.cli.command.directive.CdDirective;
import org.openflexo.foundation.fml.cli.command.directive.EnterDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExecuteDirective;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.HelpDirective;
import org.openflexo.foundation.fml.cli.command.directive.HistoryDirective;
import org.openflexo.foundation.fml.cli.command.directive.LoadResource;
import org.openflexo.foundation.fml.cli.command.directive.LsDirective;
import org.openflexo.foundation.fml.cli.command.directive.MoreDirective;
import org.openflexo.foundation.fml.cli.command.directive.OpenProject;
import org.openflexo.foundation.fml.cli.command.directive.PwdDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.foundation.fml.cli.command.directive.ResourcesDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServiceDirective;
import org.openflexo.foundation.fml.cli.command.directive.ServicesDirective;
import org.openflexo.foundation.fml.parser.node.ABindingPath;
import org.openflexo.foundation.fml.parser.node.ACharacterLiteral;
import org.openflexo.foundation.fml.parser.node.AConcatenedSimplePath;
import org.openflexo.foundation.fml.parser.node.AConstantIdentifier;
import org.openflexo.foundation.fml.parser.node.ADirectoryPathDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.ADotPath;
import org.openflexo.foundation.fml.parser.node.ADotPathPath;
import org.openflexo.foundation.fml.parser.node.ADotSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.ADoubleDotPath;
import org.openflexo.foundation.fml.parser.node.ADoubleDotPathPath;
import org.openflexo.foundation.fml.parser.node.AExpressionDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.AFalseLiteral;
import org.openflexo.foundation.fml.parser.node.AFilePathDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.AFloatingPointLiteral;
import org.openflexo.foundation.fml.parser.node.AIntegerLiteral;
import org.openflexo.foundation.fml.parser.node.ALiteralSimplePathTerminal;
import org.openflexo.foundation.fml.parser.node.ALitteralUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.ALowerIdentifier;
import org.openflexo.foundation.fml.parser.node.AMinusSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.AMinusdSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.AMinusfSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.AMinusrSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.ANullLiteral;
import org.openflexo.foundation.fml.parser.node.AObjectInResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.APathPath;
import org.openflexo.foundation.fml.parser.node.APlainSimplePath;
import org.openflexo.foundation.fml.parser.node.APlainSimplePathTerminal;
import org.openflexo.foundation.fml.parser.node.APrefixedSimplePath;
import org.openflexo.foundation.fml.parser.node.APrimaryUriExpression;
import org.openflexo.foundation.fml.parser.node.AResourceCenterDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.AResourceDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.AResourceReferenceByUri;
import org.openflexo.foundation.fml.parser.node.AResourcesSimplePathTerminal;
import org.openflexo.foundation.fml.parser.node.ARootPathPath;
import org.openflexo.foundation.fml.parser.node.AStringLiteral;
import org.openflexo.foundation.fml.parser.node.ATrueLiteral;
import org.openflexo.foundation.fml.parser.node.AUpperIdentifier;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PDirectiveArgument;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PIdentifier;
import org.openflexo.foundation.fml.parser.node.PLiteral;
import org.openflexo.foundation.fml.parser.node.PPath;
import org.openflexo.foundation.fml.parser.node.PReferenceByUri;
import org.openflexo.foundation.fml.parser.node.PSimplePath;
import org.openflexo.foundation.fml.parser.node.PSimplePathPrefix;
import org.openflexo.foundation.fml.parser.node.PSimplePathTerminal;
import org.openflexo.foundation.fml.parser.node.PUriExpression;
import org.openflexo.foundation.fml.parser.node.PUriExpressionPrimary;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents a directive in FML command-line interpreter
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(Directive.DirectiveImpl.class)
@DeclareDirectives({ @DeclareDirective(value = HelpDirective.class), @DeclareDirective(HistoryDirective.class),
		@DeclareDirective(CdDirective.class), @DeclareDirective(PwdDirective.class), @DeclareDirective(LsDirective.class),
		@DeclareDirective(QuitDirective.class), @DeclareDirective(ServicesDirective.class), @DeclareDirective(ServiceDirective.class),
		@DeclareDirective(ActivateTA.class), @DeclareDirective(ResourcesDirective.class), @DeclareDirective(OpenProject.class),
		@DeclareDirective(LoadResource.class), @DeclareDirective(MoreDirective.class), @DeclareDirective(EnterDirective.class),
		@DeclareDirective(ExitDirective.class), @DeclareDirective(ExecuteDirective.class) })
public interface Directive<N extends Node> extends AbstractCommand<N> {

	public static abstract class DirectiveImpl<N extends Node> extends AbstractCommandImpl<N> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(Directive.class.getPackage().getName());

		@Override
		public boolean isSyntaxicallyValid() {
			return true;
		}

		@Override
		public boolean isValidInThatContext() {
			return isSyntaxicallyValid();
		}

		@Override
		public String invalidCommandReason() {
			return null;
		}

		protected String getFileName(File f) {
			if (f == null) {
				return null;
			}
			if (f.isDirectory() && !f.getName().endsWith(CompilationUnitResourceFactory.FML_SUFFIX)
					&& !f.getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)
					&& !f.getName().endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX)) {
				return f.getName() + File.separator;
			}
			return f.getName();
		}

		protected TechnologyAdapter getTechnologyAdapter(String technologyAdapterName) {
			for (TechnologyAdapter ta : getCommandInterpreter().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
				if (ta.getIdentifier().equals(technologyAdapterName)) {
					return ta;
				}
			}
			return null;
		}

		protected FlexoResourceCenter<?> getResourceCenter(String rcURI) {
			if (rcURI.startsWith("[")) {
				rcURI = rcURI.substring(1);
			}
			if (rcURI.endsWith("]")) {
				rcURI = rcURI.substring(0, rcURI.length() - 1);
			}
			for (FlexoResourceCenter<?> rc : getCommandInterpreter().getServiceManager().getResourceCenterService().getResourceCenters()) {
				if (rc.getDefaultBaseURI().equals(rcURI)) {
					return rc;
				}
			}
			return null;
		}

		private String getText(PExpression e) {
			// TODO
			logger.warning("Faire mieux que ca !");
			return e.toString();
		}

		@Deprecated
		protected Object evaluateArgument(PDirectiveArgument value, CommandTokenType tokenType) {
			if (value instanceof AExpressionDirectiveArgument) {
				return evaluate(((AExpressionDirectiveArgument) value).getExpression(), tokenType);
			}
			else if (value instanceof ADirectoryPathDirectiveArgument) {
				if (tokenType == CommandTokenType.Path) {
					String pathAsString = retrievePath(((ADirectoryPathDirectiveArgument) value).getPath());
					return new File(pathAsString);
				}
			}
			else if (value instanceof AFilePathDirectiveArgument) {
				if (tokenType == CommandTokenType.Path) {
					String pathAsString = retrievePath(((AFilePathDirectiveArgument) value).getPath());
					return new File(pathAsString);
				}
			}
			else if (value instanceof AResourceCenterDirectiveArgument) {
				if (tokenType == CommandTokenType.RC) {
					FlexoResourceCenter<?> rc = retrieveResourceCenter(((AResourceCenterDirectiveArgument) value).getReferenceByUri());
					return rc;
				}
			}
			else if (value instanceof AResourceDirectiveArgument) {
				if (tokenType == CommandTokenType.RC) {
					FlexoResource<?> res = retrieveResource(((AResourceDirectiveArgument) value).getReferenceByUri());
					return res;
				}
			}
			getErrStream().println("Unexpected " + value + " in evaluateArgument() for tokenType=" + tokenType);
			return null;
		}

		@Deprecated
		protected Object evaluate(PExpression value, CommandTokenType tokenType) {

			switch (tokenType) {
				case Expression:
					System.out.println("On doit evaluer " + value);
					System.out.println("En tant que " + tokenType);
					DataBinding<?> toEvaluate = new DataBinding<>(getText(value), this, Object.class, BindingDefinitionType.GET);
					System.out.println("Donc " + toEvaluate);
					System.out.println("Valide: " + toEvaluate.isValid());
					try {
						return toEvaluate.getBindingValue(getCommandInterpreter());
					} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException e1) {
						getErrStream().println("Error evaluating " + toEvaluate);
						e1.printStackTrace();
						return null;
					}

					/*case LocalReference:
						String valueAsString = getText(value);
						// System.out.println("Hop: " + valueAsString);
						File referencedFile = new File(getCommandInterpreter().getWorkingDirectory(), valueAsString);
						// System.out.println("Fichier ? " + referencedFile);
						if (referencedFile != null && referencedFile.exists()) {
							// System.out.println("existe ? " + (referencedFile != null ? referencedFile.exists() : "???"));
							ResourceManager rm = getCommandInterpreter().getServiceManager().getResourceManager();
							List<FlexoResource<?>> resources = rm.getResources(referencedFile);
							// System.out.println("found: " + resources);
							if (resources.size() > 0) {
								FlexoResource<?> resource = resources.get(0);
								try {
									if (!resource.isLoaded()) {
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												getOutStream().println("Loading resource " + referencedFile.getName() + "...");
											}
										});
									}
									return resource.getResourceData();
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ResourceLoadingCancelledException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (FlexoException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						return null;*/
				case Path:
					return null;
				case Service:
					return null;
				case Operation:
					return null;
				case TA:
					return null;
				case RC:
					return null;
				case Resource:
					return null;
			}

			/*if (valueType.equals("<path>") && pDirectiveOption instanceof APathDirectiveOption) {
				return new File(getCommandInterpreter().getWorkingDirectory(),
						retrievePath(((APathDirectiveOption) pDirectiveOption).getPath()));
			}
			if (valueType.equals("<ta>") && pDirectiveOption instanceof APathDirectiveOption) {
				String taName = retrievePath(((APathDirectiveOption) pDirectiveOption).getPath());
				for (TechnologyAdapter ta : getCommandInterpreter().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
					if (ta.getClass().getSimpleName().equals(taName)) {
						return ta;
					}
				}
			}
			return pDirectiveOption.toString();*/

			return null;
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
			else if (path instanceof ABindingPath) {
				return getText(((ABindingPath) path).getSimplePath());
			}
			else if (path instanceof APathPath) {
				return getText(((APathPath) path).getSimplePath()) + File.separator + retrievePath(((APathPath) path).getPath());
			}
			else if (path instanceof ARootPathPath) {
				return File.separator + retrievePath(((ARootPathPath) path).getPath());
			}
			return null;
		}

		private String getText(PSimplePathPrefix aSimplePathPrefix) {
			if (aSimplePathPrefix instanceof ADotSimplePathPrefix) {
				return getText(((ADotSimplePathPrefix) aSimplePathPrefix).getSimplePathTerminal()) + ".";
			}
			else if (aSimplePathPrefix instanceof AMinusSimplePathPrefix) {
				return getText(((AMinusSimplePathPrefix) aSimplePathPrefix).getSimplePathTerminal()) + "-";
			}
			else if (aSimplePathPrefix instanceof AMinusdSimplePathPrefix) {
				return getText(((AMinusdSimplePathPrefix) aSimplePathPrefix).getSimplePathTerminal()) + "-d";
			}
			else if (aSimplePathPrefix instanceof AMinusfSimplePathPrefix) {
				return getText(((AMinusfSimplePathPrefix) aSimplePathPrefix).getSimplePathTerminal()) + "-f";
			}
			else if (aSimplePathPrefix instanceof AMinusrSimplePathPrefix) {
				return getText(((AMinusrSimplePathPrefix) aSimplePathPrefix).getSimplePathTerminal()) + "-r";
			}
			return null;
		}

		private String getText(PSimplePath aSimplePath) {
			if (aSimplePath instanceof APlainSimplePath) {
				return getText(((APlainSimplePath) aSimplePath).getSimplePathTerminal());
			}
			else if (aSimplePath instanceof AConcatenedSimplePath) {
				return getText(((AConcatenedSimplePath) aSimplePath).getSimplePathTerminal())
						+ getText(((AConcatenedSimplePath) aSimplePath).getSimplePath());
			}
			else if (aSimplePath instanceof APrefixedSimplePath) {
				return getText(((APrefixedSimplePath) aSimplePath).getPrefix())
						+ getText(((APrefixedSimplePath) aSimplePath).getSimplePath());
			}
			return null;
		}

		private String getText(PSimplePathTerminal aSimplePath) {
			if (aSimplePath instanceof APlainSimplePathTerminal) {
				return getText(((APlainSimplePathTerminal) aSimplePath).getIdentifier());
			}
			else if (aSimplePath instanceof ALiteralSimplePathTerminal) {
				return getText(((ALiteralSimplePathTerminal) aSimplePath).getLiteral());
			}
			else if (aSimplePath instanceof AResourcesSimplePathTerminal) {
				return ((AResourcesSimplePathTerminal) aSimplePath).getResources().getText();
			}
			return null;
		}

		protected String getText(PIdentifier identifier) {
			if (identifier instanceof ALowerIdentifier) {
				return ((ALowerIdentifier) identifier).getLidentifier().getText();
			}
			else if (identifier instanceof AUpperIdentifier) {
				return ((AUpperIdentifier) identifier).getUidentifier().getText();
			}
			else if (identifier instanceof AConstantIdentifier) {
				return ((AConstantIdentifier) identifier).getCidentifier().getText();
			}
			return null;
		}

		private String getText(PLiteral literal) {
			if (literal instanceof AIntegerLiteral) {
				return ((AIntegerLiteral) literal).getLitInteger().getText();
			}
			else if (literal instanceof AFloatingPointLiteral) {
				return ((AFloatingPointLiteral) literal).getLitFloat().getText();
			}
			else if (literal instanceof ACharacterLiteral) {
				return ((ACharacterLiteral) literal).getLitCharacter().getText();
			}
			else if (literal instanceof AFalseLiteral) {
				return ((AFalseLiteral) literal).getLitFalse().getText();
			}
			else if (literal instanceof ATrueLiteral) {
				return ((ATrueLiteral) literal).getLitTrue().getText();
			}
			else if (literal instanceof ANullLiteral) {
				return ((ANullLiteral) literal).getLitNull().getText();
			}
			else if (literal instanceof AStringLiteral) {
				return ((AStringLiteral) literal).getLitString().getText();
			}
			return null;
		}

		public FlexoResource<?> retrieveResourceFromPath(String resourcePath) {
			if (StringUtils.isNotEmpty(resourcePath)) {
				File resourceFile;
				if (resourcePath.startsWith("/")) {
					resourceFile = new File(resourcePath);
				}
				else {
					resourceFile = new File(getCommandInterpreter().getWorkingDirectory(), resourcePath);
				}
				try {
					resourceFile = new File(resourceFile.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<FlexoResource<?>> resources = getCommandInterpreter().getServiceManager().getResourceManager()
						.getResources(resourceFile);
				if (resources.size() == 0) {
					getErrStream().println("Cannot load as a resource " + resourceFile);
				}
				else if (resources.size() > 1) {
					getErrStream().println("Multiple resources for " + resourceFile);
					return resources.get(0);
				}
				else {
					return resources.get(0);
				}

			}
			return null;
		}

		protected FlexoObject retrieveObjectOrResource(PReferenceByUri pRefURI) {

			if (pRefURI instanceof AResourceReferenceByUri) {
				return retrieveResource(((AResourceReferenceByUri) pRefURI).getResource());
			}
			else if (pRefURI instanceof AObjectInResourceReferenceByUri) {
				return retrieveObject(((AObjectInResourceReferenceByUri) pRefURI).getResource(),
						((AObjectInResourceReferenceByUri) pRefURI).getObject());
			}
			return null;
		}

		protected FlexoResource<?> retrieveResource(PReferenceByUri pRefURI) {

			if (pRefURI instanceof AResourceReferenceByUri) {
				return retrieveResource(((AResourceReferenceByUri) pRefURI).getResource());
			}
			logger.warning("Unexpected " + pRefURI);
			return null;
		}

		protected FlexoResourceCenter<?> retrieveResourceCenter(PReferenceByUri pRefURI) {

			if (pRefURI instanceof AResourceReferenceByUri) {
				return retrieveResourceCenter(((AResourceReferenceByUri) pRefURI).getResource());
			}
			logger.warning("Unexpected " + pRefURI);
			return null;
		}

		protected FlexoObject retrieveObject(PReferenceByUri pRefURI) {

			if (pRefURI instanceof AObjectInResourceReferenceByUri) {
				return retrieveObject(((AObjectInResourceReferenceByUri) pRefURI).getResource(),
						((AObjectInResourceReferenceByUri) pRefURI).getObject());
			}
			return null;
		}

		protected FlexoObject retrieveObject(PUriExpression resourceURIExpression, PUriExpression objectURIExpression) {
			FlexoResource<?> resource = retrieveResource(resourceURIExpression);
			logger.warning("On cherche un objet dans la resource avec l'uri " + objectURIExpression);
			// TODO
			return null;
		}

		protected FlexoResource<?> retrieveResource(PUriExpression resourceURIExpression) {
			String uri = retrieveURI(resourceURIExpression);
			return retrieveResource(uri);
		}

		protected FlexoResourceCenter<?> retrieveResourceCenter(PUriExpression resourceURIExpression) {
			String uri = retrieveURI(resourceURIExpression);
			return retrieveResourceCenter(uri);
		}

		protected String retrieveURI(PUriExpression uriExpression) {
			if (uriExpression instanceof APrimaryUriExpression) {
				return retrieveURI(((APrimaryUriExpression) uriExpression).getUriExpressionPrimary());
			}
			return null;
		}

		protected String retrieveURI(PUriExpressionPrimary uriExpressionPrimary) {
			if (uriExpressionPrimary instanceof ALitteralUriExpressionPrimary) {
				return ((ALitteralUriExpressionPrimary) uriExpressionPrimary).getLitString().getText();
			}
			return null;
		}

		protected FlexoResource<?> retrieveResource(String resourceURI) {
			if (resourceURI.startsWith("[\"")) {
				resourceURI = resourceURI.substring(2);
			}
			if (resourceURI.endsWith("\"]")) {
				resourceURI = resourceURI.substring(0, resourceURI.length() - 2);
			}
			if (resourceURI.startsWith("\"")) {
				resourceURI = resourceURI.substring(1);
			}
			if (resourceURI.endsWith("\"")) {
				resourceURI = resourceURI.substring(0, resourceURI.length() - 1);
			}
			return getCommandInterpreter().getServiceManager().getResourceManager().getResource(resourceURI);
		}

		protected FlexoResourceCenter<?> retrieveResourceCenter(String rcURI) {
			if (rcURI.startsWith("[\"")) {
				rcURI = rcURI.substring(2);
			}
			if (rcURI.endsWith("\"]")) {
				rcURI = rcURI.substring(0, rcURI.length() - 2);
			}
			if (rcURI.startsWith("\"")) {
				rcURI = rcURI.substring(1);
			}
			if (rcURI.endsWith("\"")) {
				rcURI = rcURI.substring(0, rcURI.length() - 1);
			}
			return getCommandInterpreter().getServiceManager().getResourceCenterService().getFlexoResourceCenter(rcURI);
		}
	}
}
