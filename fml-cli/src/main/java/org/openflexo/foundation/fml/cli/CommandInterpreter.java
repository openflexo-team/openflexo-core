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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.console.ConsoleOutput;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.util.ANSI;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceAction;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.DeclareDirective;
import org.openflexo.foundation.fml.cli.command.DeclareDirectives;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * This class is an "interactive" BASIC environment. You can think of it as BASIC debug mode. Using the streams you passed in to create the
 * object, it hosts an interactive session allowing the user to enter BASIC programs, run them, save them, and load them.
 */
public class CommandInterpreter extends PropertyChangedSupportDefaultImplementation {

	private File workingDirectory;
	private FlexoServiceManager serviceManager;

	private DataInputStream inStream;
	private PrintStream outStream;

	private Console console;
	private ConsoleCommand consoleCommand;
	// char data[] = new char[256];

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
		DataInputStream dis = inStream;
		String lineData;

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

	private String getPrompt() {
		return workingDirectory.getName() + " > ";
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
			// if (!startingBuffer.endsWith(" ")) {
			return commands;
			// }
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
				for (ServiceAction<?> serviceAction : service.getAvailableServiceActions()) {
					if (serviceAction.getActionName().startsWith(currentToken)) {
						returned.add(startingBuffer.substring(0, startingBuffer.length() - currentToken.length())
								+ serviceAction.getActionName());
					}
				}
				return returned;
			}
		}

		return Collections.emptyList();
	}

}
