/*
 * CommandInterpreter.java -  Provide the basic command line interface.
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.openflexo.foundation.fml.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.SettableBindingEvaluationContext;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.DeclareCommand;
import org.openflexo.foundation.fml.cli.command.DeclareCommands;
import org.openflexo.foundation.fml.cli.command.DeclareDirective;
import org.openflexo.foundation.fml.cli.command.DeclareDirectives;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * FML command-line interpreter<br>
 * 
 * An interpreter must be instantiated using a {@link FlexoServiceManager}
 * 
 */
public class AbstractCommandInterpreter extends PropertyChangedSupportDefaultImplementation
		implements Bindable, SettableBindingEvaluationContext {

	private File workingDirectory;
	private FlexoServiceManager serviceManager;

	private FlexoObject focusedObject;

	@SuppressWarnings("unused")
	private PrintStream errStream;
	private PrintStream outStream;

	private BindingModel bindingModel = new BindingModel();
	private BindingVariable focusedBV;

	private JavaBindingFactory JAVA_BINDING_FACTORY = new JavaBindingFactory();

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 * 
	 * @throws IOException
	 */
	public AbstractCommandInterpreter(FlexoServiceManager serviceManager, OutputStream out, OutputStream err, File workingDirectory)
			throws IOException {

		this.serviceManager = serviceManager;

		if (out instanceof PrintStream) {
			outStream = (PrintStream) out;
		}
		else {
			outStream = new PrintStream(out);
		}

		if (err instanceof PrintStream) {
			errStream = (PrintStream) err;
		}
		else {
			errStream = new PrintStream(err);
		}

		if (workingDirectory != null) {
			this.workingDirectory = workingDirectory;
		}
		else {
			this.workingDirectory = new File(System.getProperty("user.dir"));
		}

	}

	protected String getWelcomeMessage() {
		return "FML command-line interpreter, (c) 2019 Openflexo.\n" + "Ready";
	}

	public PrintStream getOutStream() {
		return outStream;
	}

	public PrintStream getErrStream() {
		return errStream;
	}

	public String getPrompt() {
		return (getFocusedObject() != null ? CLIUtils.renderObject(getFocusedObject()) + "@" : "") + workingDirectory.getName();
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		if ((workingDirectory == null && this.workingDirectory != null)
				|| (workingDirectory != null && !workingDirectory.equals(this.workingDirectory))) {
			File oldValue = this.workingDirectory;
			this.workingDirectory = workingDirectory;
			getPropertyChangeSupport().firePropertyChange("workingDirectory", oldValue, workingDirectory);
			System.setProperty("user.dir", this.workingDirectory.getAbsolutePath());
		}
	}

	private List<Class<? extends Directive>> availableDirectives = null;

	public List<Class<? extends Directive>> getAvailableDirectives() {
		if (availableDirectives == null) {
			availableDirectives = new ArrayList<>();
			if (Directive.class.isAnnotationPresent(DeclareDirectives.class)) {
				DeclareDirectives allDirectives = Directive.class.getAnnotation(DeclareDirectives.class);
				for (DeclareDirective declareDirective : allDirectives.value()) {
					if (!availableDirectives.contains(declareDirective.value())) {
						availableDirectives.add(declareDirective.value());
					}
				}
			}
		}
		return availableDirectives;
	}

	private List<Class<? extends FMLCommand>> availableCommands = null;

	public List<Class<? extends FMLCommand>> getAvailableCommands() {
		if (availableCommands == null) {
			availableCommands = new ArrayList<>();
			if (FMLCommand.class.isAnnotationPresent(DeclareCommands.class)) {
				DeclareCommands allCommands = FMLCommand.class.getAnnotation(DeclareCommands.class);
				for (DeclareCommand declareCommand : allCommands.value()) {
					if (!availableCommands.contains(declareCommand.value())) {
						availableCommands.add(declareCommand.value());
					}
				}
			}
		}
		return availableCommands;
	}

	public AbstractCommand executeCommand(String commandString) throws ParseException {
		AbstractCommand command = makeCommand(commandString);
		// System.out.println("Typed: " + commandString + " command=" + command);
		if (command != null) {
			if (command.isValid()) {
				command.execute();
			}
			else {
				System.err.println(command.invalidCommandReason());
			}
			return command;
		}
		return null;
	}

	protected AbstractCommand makeCommand(String commandString) throws ParseException {
		return CommandParser.parse(commandString, this);
	}

	private static List<String> tokenize(String startingBuffer) {
		List<String> returned = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(startingBuffer);
		while (st.hasMoreTokens()) {
			returned.add(st.nextToken());
		}
		if (startingBuffer.endsWith(" ")) {
			returned.add("");
		}
		return returned;
	}

	public String getCommonCompletion(List<String> availableCompletion) {
		if (availableCompletion.size() == 0) {
			return "";
		}
		else if (availableCompletion.size() == 1) {
			return availableCompletion.get(0);
		}
		else {
			StringBuffer sb = new StringBuffer();
			String base = availableCompletion.get(0);
			boolean allMatch = true;
			int index = 0;
			while (allMatch && index < base.length()) {
				char c = base.charAt(index);
				for (int i = 1; i < availableCompletion.size(); i++) {
					if (index >= availableCompletion.get(i).length() || availableCompletion.get(i).charAt(index) != c) {
						allMatch = false;
						break;
					}
				}
				if (allMatch) {
					sb.append(c);
					index++;
				}
			}
			return sb.toString();
		}
	}

	public List<String> getAvailableCompletion(String startingBuffer) {
		List<String> tokens = tokenize(startingBuffer);
		if (tokens.size() == 0) {
			return Collections.emptyList();
		}
		if (tokens.size() == 1) {
			// completion for a unique token
			List<String> commands = new ArrayList<String>();
			for (Class<? extends Directive> directiveClass : getAvailableDirectives()) {
				String keyword = directiveClass.getAnnotation(DirectiveDeclaration.class).keyword();
				if (keyword.startsWith(tokens.get(0))) {
					commands.add(keyword);
				}
			}
			for (Class<? extends FMLCommand> commandClass : getAvailableCommands()) {
				String keyword = commandClass.getAnnotation(FMLCommandDeclaration.class).keyword();
				if (keyword.startsWith(tokens.get(0))) {
					commands.add(keyword);
				}
			}
			return commands;
		}

		DirectiveDeclaration directiveClassDeclaration = null;
		for (Class<? extends Directive> directiveClass : getAvailableDirectives()) {
			String keyword = directiveClass.getAnnotation(DirectiveDeclaration.class).keyword();
			if (keyword.equals(tokens.get(0))) {
				directiveClassDeclaration = directiveClass.getAnnotation(DirectiveDeclaration.class);
			}
		}
		if (directiveClassDeclaration != null) {
			return getAvailableCompletion(directiveClassDeclaration, startingBuffer);
		}
		else {
			@SuppressWarnings("unused")
			FMLCommandDeclaration commandClassDeclaration = null;
			for (Class<? extends FMLCommand> commandClass : getAvailableCommands()) {
				String keyword = commandClass.getAnnotation(FMLCommandDeclaration.class).keyword();
				if (keyword.equals(tokens.get(0))) {
					commandClassDeclaration = commandClass.getAnnotation(FMLCommandDeclaration.class);
				}
			}
			return Collections.emptyList();
		}
	}

	private List<String> getAvailableCompletion(DirectiveDeclaration directiveDeclaration, String startingBuffer) {
		// System.out.println("Hop, on essaie de faire une completion pour " + directiveDeclaration);

		List<String> tokens = tokenize(startingBuffer);
		String syntax = directiveDeclaration.syntax();
		List<String> expectedSyntax = tokenize(syntax);

		int index = tokens.size() - 1;

		/*System.out.println("index=" + index);
		
		System.out.println("syntax=" + syntax);
		System.out.println("expectedSyntax=" + expectedSyntax);
		System.out.println("expectedSyntax.size=" + expectedSyntax.size());
		System.out.println("tokens.size=" + tokens.size());*/

		if (expectedSyntax.size() < tokens.size()) {
			// Nothing else expected
			return Collections.emptyList();
		}

		String currentToken = tokens.get(index);
		String expectedTokenSyntax = expectedSyntax.get(index);

		// System.out.println("Maintenant, on cherche a completer " + currentToken + " pour " + expectedTokenSyntax);

		if (expectedTokenSyntax.equals("<path>")) {
			List<String> returned = new ArrayList<>();
			for (File f : getWorkingDirectory().listFiles()) {
				// System.out.println("On rajoute " + f.getName());
				if (f.getName().startsWith(currentToken)) {
					returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + f.getName());
				}
			}
			return returned;
		}

		if (expectedTokenSyntax.equals("<service>")) {
			List<String> returned = new ArrayList<>();
			for (FlexoService service : serviceManager.getRegisteredServices()) {
				if (service.getServiceName().startsWith(currentToken)) {
					returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + service.getServiceName());
				}
			}
			return returned;
		}

		if (expectedTokenSyntax.equals("<operation>")) {
			// System.out.println("Plus dur, une operation pour " + tokens.get(index - 1));
			FlexoService service = null;
			for (FlexoService s : serviceManager.getRegisteredServices()) {
				if (s.getServiceName().equals(tokens.get(index - 1))) {
					service = s;
				}
			}
			if (service != null) {
				List<String> returned = new ArrayList<>();
				for (ServiceOperation<?> serviceOperation : service.getAvailableServiceOperations()) {
					if (serviceOperation.getOperationName().startsWith(currentToken)) {
						returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length())
								+ serviceOperation.getOperationName());
					}
				}
				return returned;
			}
		}

		if (expectedTokenSyntax.equals("<ta>")) {
			List<String> returned = new ArrayList<>();
			for (TechnologyAdapter ta : serviceManager.getTechnologyAdapterService().getTechnologyAdapters()) {
				if (ta.getIdentifier().startsWith(currentToken)) {
					returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + ta.getIdentifier());
				}
			}
			return returned;
		}

		if (expectedTokenSyntax.equals("<rc>")) {
			String utileToken = currentToken;
			if (utileToken.startsWith("[")) {
				utileToken = utileToken.substring(1);
			}
			List<String> returned = new ArrayList<>();
			for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
				if (rc.getDefaultBaseURI().startsWith(utileToken)) {
					returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + "[" + rc.getDefaultBaseURI()
							+ "]");
				}
			}
			return returned;
		}

		if (expectedTokenSyntax.equals("<resource>")) {
			String utileToken = currentToken;
			if (utileToken.startsWith("[")) {
				utileToken = utileToken.substring(1);
			}
			List<String> returned = new ArrayList<>();
			for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
				for (FlexoResource<?> r : rc.getAllResources()) {
					if (r.getURI().startsWith(utileToken)) {
						returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + "[" + r.getURI() + "]");
					}
					/*if (r.getName().startsWith(utileToken)) {
						returned.add(
								startingBuffer.substring(0, startingBuffer.length() - currentToken.length()) + "[" + r.getName() + "]");
					}*/
				}
			}
			return returned;
		}

		return Collections.emptyList();
	}

	@Override
	public BindingFactory getBindingFactory() {
		return JAVA_BINDING_FACTORY;
	}

	@Override
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	@Override
	public Object getValue(BindingVariable variable) {
		if (variable == focusedBV) {
			return focusedObject;
		}
		return values.get(variable);
	}

	@Override
	public void setValue(Object value, BindingVariable variable) {
		if (value != null) {
			values.put(variable, value);
		}
		else {
			values.remove(variable);
		}
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
	}

	private Map<BindingVariable, Object> values = new HashMap<>();

	public void declareVariable(String variableName, Type type, Object value) {
		System.out.println(variableName + "<-" + value + "[" + TypeUtils.simpleRepresentation(type) + "]");
		BindingVariable variable = getBindingModel().getBindingVariableNamed(variableName);
		if (variable == null) {
			variable = new BindingVariable(variableName, type);
			getBindingModel().addToBindingVariables(variable);
		}
		setValue(value, variable);
	}

	/**
	 * Return object on which we are currently working
	 * 
	 * @return
	 */
	public FlexoObject getFocusedObject() {
		return focusedObject;
	}

	public void setFocusedObject(FlexoObject focusedObject) {
		if ((focusedObject == null && this.focusedObject != null) || (focusedObject != null && !focusedObject.equals(this.focusedObject))) {
			FlexoObject oldValue = this.focusedObject;
			this.focusedObject = focusedObject;
			getPropertyChangeSupport().firePropertyChange("focusedObject", oldValue, focusedObject);
			if (focusedObject != null) {
				if (focusedBV == null) {
					focusedBV = new BindingVariable("this", FlexoObject.class);
					getBindingModel().addToBindingVariables(focusedBV);
				}
				if (focusedObject instanceof VirtualModel) {
					focusedBV.setType(VirtualModel.class);
				}
				else if (focusedObject instanceof FlexoConcept) {
					focusedBV.setType(FlexoConcept.class);
				}
				else if (focusedObject instanceof VirtualModelInstance) {
					focusedBV.setType(VirtualModelInstance.class);
				}
				else if (focusedObject instanceof FlexoConceptInstance) {
					focusedBV.setType(FlexoConceptInstance.class);
				}
			}
			else { // focusedObject=null
				if (focusedBV != null) {
					getBindingModel().removeFromBindingVariables(focusedBV);
					focusedBV = null;
				}
			}
		}
	}

	/**
	 * Starts the interactive session
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		getOutStream().println(getWelcomeMessage());
	}

	public void stop() {
		System.out.println("Stopping CommandInterpreter");
	}
}
