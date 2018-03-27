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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.console.ConsoleOutput;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.util.ANSI;
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
public class CommandInterpreter extends PropertyChangedSupportDefaultImplementation implements Bindable, SettableBindingEvaluationContext {

	private File workingDirectory;
	private FlexoServiceManager serviceManager;

	private FlexoObject focusedObject;

	@SuppressWarnings("unused")
	private DataInputStream inStream;
	private PrintStream outStream;

	private Console console;
	private ConsoleCommand consoleCommand;
	// char data[] = new char[256];

	private BindingModel bindingModel = new BindingModel();
	private BindingVariable focusedBV;

	private JavaBindingFactory JAVA_BINDING_FACTORY = new JavaBindingFactory();

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 * 
	 * @throws IOException
	 */
	public CommandInterpreter(FlexoServiceManager serviceManager, InputStream in, OutputStream out, File workingDirectory)
			throws IOException {

		this.serviceManager = serviceManager;

		if (in instanceof DataInputStream) {
			inStream = (DataInputStream) in;
		}
		else {
			inStream = new DataInputStream(in);
		}
		if (out instanceof PrintStream) {
			outStream = (PrintStream) out;
		}
		else {
			outStream = new PrintStream(out);
		}

		if (workingDirectory != null) {
			this.workingDirectory = workingDirectory;
		}
		else {
			this.workingDirectory = new File(System.getProperty("user.dir"));
		}

		console = new Console();

		consoleCommand = new ConsoleCommand(console) {

			@Override
			protected void afterAttach() throws IOException {
				if (!hasRedirectOut()) {
					console.pushToStdOut(ANSI.getAlternateBufferScreen());
				}

				readFromFile();

				// detach after init if hasRedirectOut()
				if (hasRedirectOut()) {
					detach();
				}
			}

			@Override
			protected void afterDetach() throws IOException {
				if (!hasRedirectOut())
					console.pushToStdOut(ANSI.getMainBufferScreen());
			}

			private void readFromFile() throws IOException {
				if (getConsoleOutput().getStdOut() != null && getConsoleOutput().getStdOut().length() > 0) {
					console.pushToStdOut("FROM STDOUT: " + getConsoleOutput().getStdOut());
				}
				else
					console.pushToStdOut("here should we present some text... press 'q' to quit");
			}

			@Override
			public void processOperation(Operation operation) throws IOException {
				if (operation.getInput()[0] == 'q') {
					detach();
				}
				else if (operation.getInput()[0] == 'a') {
					readFromFile();
				}
				else {

				}
			}
		};

		Completion completer = new Completion() {
			@Override
			public void complete(CompleteOperation co) {

				List<String> commands = getAvailableCompletion(co.getBuffer());

				// very simple completor
				/*List<String> commands = new ArrayList<String>();
				if (co.getBuffer().equals("fo") || co.getBuffer().equals("foo")) {
					commands.add("foo");
					commands.add("foobaa");
					commands.add("foobar");
					commands.add("foobaxxxxxx");
					commands.add("foobbx");
					commands.add("foobcx");
					commands.add("foobdx");
				}
				if (co.getBuffer().equals("--")) {
					commands.add("--help-");
				}
				if (co.getBuffer().startsWith("--help-") || co.getBuffer().startsWith("--help-m")) {
					commands.add("--help-me");
				}
				if (co.getBuffer().equals("fooba")) {
					commands.add("foobaa");
					commands.add("foobar");
					commands.add("foobaxxxxxx");
				}
				if (co.getBuffer().equals("foobar")) {
					commands.add("foobar");
				}
				if (co.getBuffer().equals("bar")) {
					commands.add("bar/");
				}
				if (co.getBuffer().equals("h")) {
					commands.add("help.history");
					commands.add("help");
				}
				if (co.getBuffer().equals("help")) {
					commands.add("help.history");
					commands.add("help");
				}
				if (co.getBuffer().equals("help.")) {
					commands.add("help.history");
				}
				if (co.getBuffer().equals("deploy")) {
					commands.add("deploy /home/blabla/foo/bar/alkdfe/en/to/tre");
				}
				if (co.getBuffer().equals("testing")) {
					commands.add("testing YAY");
				}*/
				co.setCompletionCandidates(commands);
			}
		};

		console.addCompletion(completer);

	}

	/**
	 * Starts the interactive session. When running the user should see the "Ready." prompt. The session ends when the user types the
	 * <code>byte</code> command.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		// LexicalTokenizer lt = new LexicalTokenizer(data);
		// Program pgm = new Program();
		// DataInputStream dis = inStream;
		// String lineData;

		outStream.println("FML command-line interpreter, (c) 2018 Openflexo.");
		outStream.println("Ready.");

		ConsoleOutput line;
		// console.pushToStdOut(ANSI.GREEN_TEXT());
		while (!isStopping && (line = console.read(getPrompt())) != null) {
			// exampleConsole.pushToStdOut("======>" + line.getBuffer() + "\n");

			// exit on eof of the input stream
			if (line.getBuffer() == null)
				return;

			// ignore blank lines.
			if (line.getBuffer().length() == 0) {
				// printPrompt();
				continue;
			}

			try {
				AbstractCommand command = CommandParser.parse(line.getBuffer(), this);
				// System.out.println("Typed: " + lineData + " command=" + command);
				if (command != null) {
					if (command.isValid()) {
						command.execute();
					}
					else {
						System.err.println(command.invalidCommandReason());
					}

					if (isStopping) {
						break;
					}
				}
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			}

			/*if (line.getBuffer().equalsIgnoreCase("quit") || line.getBuffer().equalsIgnoreCase("exit")
					|| line.getBuffer().equalsIgnoreCase("reset")) {
				break;
			}
			if (line.getBuffer().equalsIgnoreCase("password")) {
				line = exampleConsole.read("password: ", Character.valueOf((char) 0));
				exampleConsole.pushToStdOut("password typed:" + line + "\n");
			
			}
			// test stdErr
			if (line.getBuffer().startsWith("blah")) {
				exampleConsole.pushToStdErr("blah. command not found.\n");
			}
			if (line.getBuffer().equals("clear"))
				exampleConsole.clear();
			if (line.getBuffer().startsWith("man")) {
				// exampleConsole.attachProcess(test);
				test.attach(line);
			 */
		}
		/*if (line != null && line.getBuffer().equals("reset")) {
			exampleConsole.stop();
			exampleConsole = new Console();
		
			while ((line = exampleConsole.read("> ")) != null) {
				exampleConsole.pushToStdOut("======>\"" + line + "\"\n");
				if (line.getBuffer().equalsIgnoreCase("quit") || line.getBuffer().equalsIgnoreCase("exit")
						|| line.getBuffer().equalsIgnoreCase("reset")) {
					break;
				}
		
			}
		}*/

		/*while (true) {
			// Statement s = null;
			try {
		
				BufferedReader in = new BufferedReader(new InputStreamReader(dis));
				lineData = in.readLine();
		
				// lineData = dis.readLine();
			} catch (IOException ioe) {
				outStream.println("Caught an IO exception reading the input stream!");
				return;
			}
		
			// exit on eof of the input stream
			if (lineData == null)
				return;
		
			// ignore blank lines.
			if (lineData.length() == 0) {
				printPrompt();
				continue;
			}
		
			try {
				AbstractCommand command = CommandParser.parse(lineData, this);
				// System.out.println("Typed: " + lineData + " command=" + command);
				if (command != null) {
					if (command.isValid()) {
						command.execute();
					}
					else {
						System.err.println(command.invalidCommandReason());
					}
				}
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			}
		
			printPrompt();
		
		}*/
	}

	/*private void printPrompt() {
		outStream.print(workingDirectory.getName() + " > ");
	}*/

	private boolean isStopping = false;

	public void stop() {
		System.out.println("Exiting command interpreter");
		isStopping = true;
		try {
			console.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Console getConsole() {
		return console;
	}

	public ConsoleCommand getConsoleCommand() {
		return consoleCommand;
	}

	private String getPrompt() {
		return (getFocusedObject() != null ? CLIUtils.renderObject(getFocusedObject()) + "@" : "") + workingDirectory.getName() + " > ";
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

	private List<String> tokenize(String startingBuffer) {
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

	private List<String> getAvailableCompletion(String startingBuffer) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

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

}