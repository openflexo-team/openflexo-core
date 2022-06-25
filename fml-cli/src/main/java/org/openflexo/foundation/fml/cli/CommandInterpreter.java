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
import java.util.List;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.console.ConsoleOutput;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.util.ANSI;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.AbstractCommand.ExecutionException;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;

/**
 * FML command-line interpreter<br>
 * 
 * An interpreter must be instantiated using a {@link FlexoServiceManager}
 * 
 */
public class CommandInterpreter extends AbstractCommandInterpreter {

	private Console console;
	private ConsoleCommand consoleCommand;

	private DataInputStream inStream;

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 * 
	 * @throws IOException
	 */
	public CommandInterpreter(FlexoServiceManager serviceManager, InputStream in, OutputStream out, OutputStream err, File workingDirectory)
			throws IOException {

		super(serviceManager, out, err, workingDirectory);
		console = new Console();

		if (in instanceof DataInputStream) {
			inStream = (DataInputStream) in;
		}
		else {
			inStream = new DataInputStream(in);
		}

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
	@Override
	public void start() throws IOException {
		// LexicalTokenizer lt = new LexicalTokenizer(data);
		// Program pgm = new Program();
		// DataInputStream dis = inStream;
		// String lineData;

		super.start();

		ConsoleOutput line;
		// console.pushToStdOut(ANSI.GREEN_TEXT());
		while (!isStopping && (line = console.read(getPrompt() + " > ")) != null) {
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
				AbstractCommand command = executeCommand(line.getBuffer());
				if (isStopping) {
					break;
				}
			} catch (ParseException e) {
				getErrStream().println(e.getMessage());
			} catch (ExecutionException e) {
				getErrStream().println(e.getMessage());
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

	@Override
	public void stop() {
		System.out.println("Exiting command interpreter");
		isStopping = true;
		try {
			console.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Console getConsole() {
		return console;
	}

	public ConsoleCommand getConsoleCommand() {
		return consoleCommand;
	}

	@Override
	public void displayHistory() {
		for (AbstractCommand command : getHistory()) {
			getOutStream().println(command.toString());
		}
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}
}
