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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
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

	// char data[] = new char[256];

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 */
	public CommandInterpreter(FlexoServiceManager serviceManager, InputStream in, OutputStream out, File workingDirectory) {

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
	}

	/**
	 * Starts the interactive session. When running the user should see the "Ready." prompt. The session ends when the user types the
	 * <code>byte</code> command.
	 */
	public void start() {
		// LexicalTokenizer lt = new LexicalTokenizer(data);
		// Program pgm = new Program();
		DataInputStream dis = inStream;
		String lineData;

		outStream.println("FML command-line interpreter, (c) 2018 Openflexo.");
		outStream.println("Ready.");
		printPrompt();

		while (true) {
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

		}
	}

	private void printPrompt() {
		outStream.print(workingDirectory.getName() + " > ");
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
}
