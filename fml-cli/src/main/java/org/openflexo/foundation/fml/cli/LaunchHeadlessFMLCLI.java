/*
 * BASIC.java -  BASIC Interpreter in Java.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.jreadline.console.settings.Settings;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLInvalidCommandException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * The FML command-line interpreter, headless.<br>
 * 
 * Without a script, it runs the interactive interpreter. With a script ({@code fml-cli [options] script.fmlscript}), it runs the script
 * and exits with a code telling how it went (CORE-F-11): {@link #EXIT_SUCCESS}, {@link #EXIT_FML_ERROR}, {@link #EXIT_INVALID},
 * {@link #EXIT_INTERNAL_ERROR}. A failure is described on the error stream: where the failing command is, the kind of the failure, its
 * message and its FML stack.
 * 
 * Launching in a terminal: cd /Users/sylvain/GIT-1.9.0/openflexo-core/fml-cli/bin/main
 * 
 * java org.openflexo.foundation.fml.cli.LaunchHeadlessFMLCLI
 * 
 * or
 * 
 * java -cp "Java/*" org.openflexo.foundation.fml.cli.LaunchHeadlessFMLCLI from .app
 * 
 * 
 * @author sylvain
 *
 */
public class LaunchHeadlessFMLCLI {

	private static final Logger logger = Logger.getLogger(LaunchHeadlessFMLCLI.class.getPackage().getName());

	/** The script has run to its end, or to a top-level return; the interactive interpreter was quit */
	public static final int EXIT_SUCCESS = 0;
	/** A command of the script failed on an error foreseen by FML (the script or its data are at fault), or an assertion failed */
	public static final int EXIT_FML_ERROR = 1;
	/** The script cannot be read or parsed, one of its commands is invalid, or the options are wrong */
	public static final int EXIT_INVALID = 2;
	/** A defect of the platform (an unforeseen failure): the interpreter, or the script, could not go on */
	public static final int EXIT_INTERNAL_ERROR = 3;

	public static class Options {

		public boolean verbose = false;

		public boolean enableDirectoryWatching = true;

		public boolean devMode = false;

		public final List<String> rcPaths = new ArrayList<>();

		/** The script to run, null to run the interactive interpreter */
		public String scriptPath = null;

		/** Only print the usage */
		public boolean help = false;

	}

	public static FlexoServiceManager createServiceManager(Options options) {
		FlexoServiceManager manager = new DefaultFlexoServiceManager(null, options.enableDirectoryWatching, options.devMode);
		TechnologyAdapterService technologyAdapterService = manager.getTechnologyAdapterService();
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLRTTechnologyAdapter.class),
				true);
		return manager;
	}

	private static String usage() {
		StringBuilder usage = new StringBuilder();
		usage.append("Usage: fml-cli [options] [script.fmlscript]\n");
		usage.append("\n");
		usage.append("Without a script, runs the interactive interpreter. With a script, runs it and exits with:\n");
		usage.append("  0 success, 1 FML error or failed assertion, 2 invalid script or options, 3 internal error.\n");
		usage.append("\n");
		usage.append("- -h|--help: show this help.\n");
		usage.append("- -v|--verbose: verbose mode.\n");
		usage.append("- --rc path: resource center to register (may have several).\n");
		usage.append("\n");
		return usage.toString();
	}

	/**
	 * Parse supplied arguments
	 * 
	 * @throws IllegalArgumentException
	 *             with the text to show the user when they are wrong
	 */
	public static Options parseOptions(String[] args) {
		Options options = new Options();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "--help":
					options.help = true;
					break;
				case "--verbose":
					options.verbose = true;
					break;
				case "--rc":
					if (i + 1 < args.length) {
						options.rcPaths.add(args[++i]);
					}
					else {
						throw new IllegalArgumentException("Option " + arg + " needs an argument.");
					}
					break;
				default: {
					if (arg.length() >= 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
						for (int j = 1; j < arg.length(); j++) {
							switch (arg.charAt(j)) {
								case 'h':
									options.help = true;
									break;
								case 'v':
									options.verbose = true;
									break;
								default:
									throw new IllegalArgumentException("Unknown short option '" + arg.charAt(j) + "'");
							}
						}
					}
					else if (arg.startsWith("-")) {
						throw new IllegalArgumentException("Unknown long option '" + arg + "'");
					}
					else if (options.scriptPath == null) {
						options.scriptPath = arg;
					}
					else {
						throw new IllegalArgumentException("Only one script may be given: '" + arg + "'");
					}
				}

			}
		}

		return options;
	}

	public static void main(String[] args) {
		System.exit(run(args));
	}

	/**
	 * Run the interpreter as the command line asks
	 * 
	 * @return the exit code
	 */
	public static int run(String[] args) {
		Options options;
		try {
			options = parseOptions(args);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println(usage());
			return EXIT_INVALID;
		}
		if (options.help) {
			System.out.println(usage());
			return EXIT_SUCCESS;
		}

		try {
			FlexoLoggingManager.initialize(-1, true, null, options.verbose ? Level.INFO : Level.WARNING, null);

			FlexoServiceManager serviceManager = createServiceManager(options);

			for (String path : options.rcPaths) {
				FlexoResourceCenterService centerService = serviceManager.getResourceCenterService();
				DirectoryResourceCenter center = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(new File(path),
						centerService);
				centerService.addToResourceCenters(center);
			}

			Settings.getInstance().setReadInputrc(false);

			CommandInterpreter ci = new CommandInterpreter(serviceManager, System.in, System.out, System.err,
					new File(System.getProperty("user.dir")));

			if (options.scriptPath != null) {
				return runScript(ci, new File(options.scriptPath));
			}

			ci.start();
			System.out.println("Exiting application...");
			return EXIT_SUCCESS;
		} catch (IOException | RuntimeException | Error e) {
			// The interpreter itself could not go on
			System.err.println(FMLErrorReporter.describe("fml-cli failed", e));
			logger.log(Level.SEVERE, "fml-cli failed", e);
			return EXIT_INTERNAL_ERROR;
		}
	}

	/**
	 * Run supplied script with supplied interpreter, describing its failure, if any, on the error stream of the interpreter
	 * 
	 * @return the exit code
	 */
	public static int runScript(AbstractCommandInterpreter ci, File scriptFile) {
		PrintStream err = ci.getErrStream();
		FMLScript script;
		try (InputStream in = new FileInputStream(scriptFile)) {
			script = new FMLScriptParser().parse(in, ci.getModelFactory(), ci);
		} catch (ParseException e) {
			err.println(scriptFile.getName() + ": cannot parse script: " + e.getMessage());
			return EXIT_INVALID;
		} catch (IOException e) {
			err.println(scriptFile.getName() + ": cannot read script: " + e.getMessage());
			return EXIT_INVALID;
		} catch (ModelDefinitionException e) {
			err.println(FMLErrorReporter.describe(scriptFile.getName() + ": cannot parse script", e));
			logger.log(Level.SEVERE, "Cannot parse script " + scriptFile, e);
			return EXIT_INTERNAL_ERROR;
		}
		try {
			script.execute();
			return EXIT_SUCCESS;
		} catch (FMLInvalidCommandException e) {
			e.setSource(scriptFile.getName());
			err.println(e.describe());
			return EXIT_INVALID;
		} catch (FMLCommandExecutionException e) {
			e.setSource(scriptFile.getName());
			err.println(e.describe());
			if (!e.isForeseen()) {
				logger.log(Level.SEVERE, "Unexpected failure in script " + scriptFile, e);
				return EXIT_INTERNAL_ERROR;
			}
			return EXIT_FML_ERROR;
		} catch (RuntimeException | Error e) {
			// A defect of the platform, or of the JVM (a StackOverflowError of a recursive behaviour): the script is over
			err.println(FMLErrorReporter.describe(scriptFile.getName() + ": unexpected failure", e));
			logger.log(Level.SEVERE, "Unexpected failure in script " + scriptFile, e);
			return EXIT_INTERNAL_ERROR;
		}
	}

}
