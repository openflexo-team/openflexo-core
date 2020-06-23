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
import java.util.Stack;
import java.util.StringTokenizer;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.SettableBindingEvaluationContext;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceOperation;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.AbstractCommand.CommandTokenType;
import org.openflexo.foundation.fml.cli.command.DeclareCommand;
import org.openflexo.foundation.fml.cli.command.DeclareCommands;
import org.openflexo.foundation.fml.cli.command.DeclareDirective;
import org.openflexo.foundation.fml.cli.command.DeclareDirectives;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandDeclaration;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * FML command-line interpreter<br>
 * 
 * An interpreter must be instantiated using a {@link FlexoServiceManager}
 * 
 */
public abstract class AbstractCommandInterpreter extends PropertyChangedSupportDefaultImplementation
		implements Bindable, SettableBindingEvaluationContext {

	private File workingDirectory;
	private FlexoServiceManager serviceManager;

	private Stack<FlexoObject> focusedObjects;

	@SuppressWarnings("unused")
	private PrintStream errStream;
	private PrintStream outStream;

	private BindingModel bindingModel = new BindingModel();
	private BindingVariable focusedBindingVariable;
	private List<BindingVariable> containedBindingVariables;

	private JavaBindingFactory JAVA_BINDING_FACTORY = new JavaBindingFactory();

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 * 
	 * @throws IOException
	 */
	public AbstractCommandInterpreter(FlexoServiceManager serviceManager, OutputStream out, OutputStream err, File workingDirectory)
			throws IOException {

		this.serviceManager = serviceManager;

		focusedObjects = new Stack<>();
		containedBindingVariables = new ArrayList<>();

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

		declareVariable("sm", serviceManager.getClass(), serviceManager);
		declareVariable("rm", ResourceManager.class, serviceManager.getResourceManager());
		declareVariable("taService", TechnologyAdapterService.class, serviceManager.getTechnologyAdapterService());
		declareVariable("rcService", FlexoResourceCenterService.class, serviceManager.getResourceCenterService());
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

		if (getFocusedObject() == null) {
			return workingDirectory.getName();
		}

		if (workingDirectory.equals(getExpectedDirectoryForObject(getFocusedObject()))) {
			// Expected directory is the same
			return CLIUtils.denoteObject(getFocusedObject());
		}

		// Otherwise prompt is computed using object and path
		return CLIUtils.denoteObject(getFocusedObject()) + "@" + workingDirectory.getName();
	}

	private File getExpectedDirectoryForObject(FlexoObject object) {
		if (object instanceof ResourceData) {
			if (((ResourceData<?>) object).getResource().getIODelegate() instanceof DirectoryBasedIODelegate) {
				return ((DirectoryBasedIODelegate) ((ResourceData<?>) object).getResource().getIODelegate()).getDirectory();
			}
		}
		return null;
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
				getErrStream().println(command.invalidCommandReason());
			}
			return command;
		}
		return null;
	}

	protected AbstractCommand makeCommand(String commandString) throws ParseException {
		// System.out.println("makeCommand with " + commandString);
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
		// System.out.println("tokens=" + tokens);
		if (tokens.size() == 0) {
			return getBindingModel().getBindingValueAvailableCompletion("", Object.class, this);

			/*List<String> returned = new ArrayList<>();
			for (BindingVariable bindingVariable : getBindingModel().getAccessibleBindingVariables()) {
				returned.add(bindingVariable.getVariableName());
			}
			return returned;*/
		}

		if (tokens.size() == 3 && tokens.get(1).equals(":=")) {
			System.out.println("Yes une assignation");
		}

		if (tokens.size() == 3 && tokens.get(1).equals("=")) {
			System.out.println("Yes une assignation2");
		}

		if (tokens.size() == 1 && tokens.get(0).contains("=")) {
			System.out.println("Yes une assignation3");
		}

		if (tokens.size() == 1) {
			// completion for a unique token
			List<String> returned = new ArrayList<String>();
			for (Class<? extends Directive> directiveClass : getAvailableDirectives()) {
				String keyword = directiveClass.getAnnotation(DirectiveDeclaration.class).keyword();
				if (keyword.startsWith(tokens.get(0))) {
					returned.add(keyword);
				}
			}
			for (Class<? extends FMLCommand> commandClass : getAvailableCommands()) {
				String keyword = commandClass.getAnnotation(FMLCommandDeclaration.class).keyword();
				if (keyword.startsWith(tokens.get(0))) {
					returned.add(keyword);
				}
			}
			returned.addAll(getBindingModel().getBindingValueAvailableCompletion(tokens.get(0), Object.class, this));
			return returned;
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
		String syntax = directiveDeclaration.syntax();
		List<String> globalSyntax = tokenize(syntax);

		// System.out.println("OK on cherche toutes les completions pour toutes les syntaxes possibles");
		// System.out.println("syntax=" + syntax);
		// System.out.println("expectedSyntax=" + globalSyntax);
		// System.out.println("expectedSyntax.size=" + globalSyntax.size());

		if (globalSyntax.get(0).equals(directiveDeclaration.keyword())) {
			int i = 1;
			List<List<String>> expectedSyntaxes = new ArrayList<>();
			List<String> current = new ArrayList<>();
			while (i < globalSyntax.size()) {
				String t = globalSyntax.get(i);
				if (t.equals("|")) {
					expectedSyntaxes.add(current);
					current = new ArrayList<>();
				}
				else {
					current.add(t);
				}
				i++;
			}
			if (current.size() > 0) {
				expectedSyntaxes.add(current);
			}
			// System.out.println("Et donc: " + expectedSyntaxes);
			List<String> returned = new ArrayList<>();
			for (List<String> expectedSyntax : expectedSyntaxes) {
				List<String> someCompletions = getAvailableCompletionForSyntax(directiveDeclaration, startingBuffer, expectedSyntax);
				// System.out.println("A ajouter: " + someCompletions);
				returned.addAll(someCompletions);
			}
			return returned;
		}
		else {
			getErrStream().println("Unexpected syntax");
			return Collections.emptyList();
		}

	}

	private List<String> getAvailableCompletionForSyntax(DirectiveDeclaration directiveDeclaration, String startingBuffer,
			List<String> expectedSyntax) {
		// System.out.println("Hop, on essaie de faire une completion pour " + directiveDeclaration);

		List<String> tokens = tokenize(startingBuffer);
		int index = tokens.size() - 1;

		// System.out.println("Ok on cherche....");
		// System.out.println("index=" + index);
		// System.out.println("expectedSyntax=" + expectedSyntax);
		// System.out.println("expectedSyntax.size=" + expectedSyntax.size());
		// System.out.println("tokens.size=" + tokens.size());

		if (expectedSyntax.size() < tokens.size() - 1) {
			// Nothing else expected
			return Collections.emptyList();
		}

		String currentToken = tokens.get(index);
		String expectedTokenSyntax = expectedSyntax.get(index - 1);

		// System.out.println("Maintenant, on cherche a completer " + currentToken + " pour " + expectedTokenSyntax);

		return getAvailableCompletionForToken(currentToken, expectedTokenSyntax, directiveDeclaration, startingBuffer,
				tokens.get(index - 1));

	}

	private List<String> getAvailableCompletionForToken(String currentToken, String expectedTokenSyntax,
			DirectiveDeclaration directiveDeclaration, String startingBuffer, String previousToken) {
		// System.out.println("Hop, on essaie de faire une completion pour " + directiveDeclaration);

		if (expectedTokenSyntax.contains(("|"))) {
			StringTokenizer st = new StringTokenizer(expectedTokenSyntax, "|");
			List<String> returned = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				String nextToken = st.nextToken();
				returned.addAll(
						getAvailableCompletionForToken(currentToken, nextToken, directiveDeclaration, startingBuffer, previousToken));
			}
			return returned;
		}

		CommandTokenType tokenType = CommandTokenType.getType(expectedTokenSyntax);
		if (tokenType != null) {
			return getAvailableCompletionForTokenTypeAndStartingBuffer(currentToken, tokenType, startingBuffer, previousToken);
		}
		else {
			return Collections.emptyList();
		}
	}

	private List<String> getAvailableCompletionForTokenTypeAndStartingBuffer(String currentToken, CommandTokenType tokenType,
			String startingBuffer, String previousToken) {

		List<String> returned = new ArrayList<>();
		String startString = startingBuffer.substring(0, startingBuffer.length() - currentToken.length());

		for (String completion : getAvailableCompletionForTokenType(currentToken, tokenType, previousToken)) {
			returned.add(startString + completion);
		}
		return returned;
	}

	private List<String> getAvailableCompletionForTokenType(String currentToken, CommandTokenType tokenType, String previousToken) {

		List<String> returned = new ArrayList<>();
		String utileToken = currentToken;

		switch (tokenType) {
			case Expression:
				for (String completion : getBindingModel().getBindingValueAvailableCompletion(currentToken, Object.class, this)) {
					returned.add(completion);
				}
				return returned;
			case LocalReference:
				// if (getFocusedObject() == null) {
				for (File f : getWorkingDirectory().listFiles()) {
					// System.out.println("On rajoute " + f.getName());
					if (f.getName().startsWith(currentToken)
							&& f.getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)) {
						returned.add(f.getName());
					}
				}
				// }
				/*else {
					for (FlexoObject contained : CLIUtils.getContainedObjects(getFocusedObject())) {
						if (CLIUtils.denoteObject(contained).startsWith(currentToken)) {
							returned.add(CLIUtils.denoteObject(contained));
						}
					}
				}*/
				return returned;
			case Path:
				for (File f : getWorkingDirectory().listFiles()) {
					// System.out.println("On rajoute " + f.getName());
					if (f.getName().startsWith(currentToken)) {
						returned.add(f.getName());
					}
				}
				return returned;
			case Service:
				for (FlexoService service : serviceManager.getRegisteredServices()) {
					if (service.getServiceName().startsWith(currentToken)) {
						returned.add(service.getServiceName());
					}
				}
				return returned;
			case Operation:
				FlexoService service = null;
				for (FlexoService s : serviceManager.getRegisteredServices()) {
					if (s.getServiceName().equals(previousToken)) {
						service = s;
					}
				}
				if (service != null) {
					for (ServiceOperation<?> serviceOperation : service.getAvailableServiceOperations()) {
						if (serviceOperation.getOperationName().startsWith(currentToken)) {
							returned.add(serviceOperation.getOperationName());
						}
					}
				}
				return returned;
			case TA:
				for (TechnologyAdapter ta : serviceManager.getTechnologyAdapterService().getTechnologyAdapters()) {
					if (ta.getIdentifier().startsWith(currentToken)) {
						returned.add(ta.getIdentifier());
					}
				}
				return returned;
			case RC:
				if (utileToken.startsWith("[")) {
					utileToken = utileToken.substring(1);
				}
				for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
					if (rc.getDefaultBaseURI().startsWith(utileToken)) {
						returned.add("[" + rc.getDefaultBaseURI() + "]");
					}
				}
				return returned;
			case Resource:
				if (utileToken.startsWith("[")) {
					utileToken = utileToken.substring(1);
				}
				for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
					for (FlexoResource<?> r : rc.getAllResources()) {
						if (r.getURI().startsWith(utileToken)) {
							returned.add("[" + r.getURI() + "]");
						}
					}
				}
				return returned;
			default:
				break;
		}

		return returned;
	}

	@Override
	public BindingFactory getBindingFactory() {
		if (getFocusedObject() instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) getFocusedObject()).getBindingFactory();
		}
		return JAVA_BINDING_FACTORY;
	}

	@Override
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	@Override
	public Object getValue(BindingVariable variable) {
		if (variable == focusedBindingVariable) {
			return getFocusedObject();
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
		// System.out.println(variableName + "<-" + value + "[" + TypeUtils.simpleRepresentation(type) + "]");
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
		if (!focusedObjects.isEmpty()) {
			return focusedObjects.peek();
		}
		return null;
	}

	public void enterFocusedObject(FlexoObject focusedObject) {
		if (focusedObject != getFocusedObject() && focusedObject != null) {
			FlexoObject oldValue = getFocusedObject();
			focusedObjects.push(focusedObject);
			updateFocusedBindingVariable();
			getPropertyChangeSupport().firePropertyChange("focusedObject", oldValue, focusedObject);
		}
	}

	public void exitFocusedObject() {
		if (getFocusedObject() != null) {
			FlexoObject oldValue = focusedObjects.pop();
			updateFocusedBindingVariable();
			getPropertyChangeSupport().firePropertyChange("focusedObject", oldValue, getFocusedObject());
		}
	}

	private void updateFocusedBindingVariable() {
		for (BindingVariable bv : containedBindingVariables) {
			getBindingModel().removeFromBindingVariables(bv);
			setValue(null, bv);
		}
		containedBindingVariables.clear();
		if (getFocusedObject() != null) {
			if (focusedBindingVariable == null) {
				focusedBindingVariable = new BindingVariable("this", FlexoObject.class);
				getBindingModel().addToBindingVariables(focusedBindingVariable);
			}
			focusedBindingVariable.setType(CLIUtils.typeOf(getFocusedObject()));
			if (CLIUtils.getContainedObjects(getFocusedObject()) != null) {
				for (FlexoObject contained : CLIUtils.getContainedObjects(getFocusedObject())) {
					BindingVariable bv = new BindingVariable(CLIUtils.denoteObject(contained), CLIUtils.typeOf(contained));
					getBindingModel().addToBindingVariables(bv);
					containedBindingVariables.add(bv);
					setValue(contained, bv);
				}
			}
		}
		else {
			getBindingModel().removeFromBindingVariables(focusedBindingVariable);
		}
		// Also change to right directory
		if (getFocusedObject() instanceof ResourceData) {
			FlexoResource<?> resource = (((ResourceData<?>) getFocusedObject()).getResource());
			if (resource.getIODelegate() instanceof DirectoryBasedIODelegate) {
				setWorkingDirectory(((DirectoryBasedIODelegate) resource.getIODelegate()).getDirectory());
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

	public abstract void displayHistory();
}
