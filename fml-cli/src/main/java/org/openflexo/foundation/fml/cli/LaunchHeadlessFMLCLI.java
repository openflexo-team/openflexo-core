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
import java.io.IOException;
//import dlib.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLoggingManager;

/**
 * Launching in a terminal: cd /Users/sylvain/GIT-1.9.0/openflexo-core/fml-cli/bin/main
 * 
 * java org.openflexo.foundation.fml.cli.LaunchHeadlessFMLCLI
 * 
 * 
 * @author sylvain
 *
 */
public class LaunchHeadlessFMLCLI {

	public static class Options {

		public boolean verbose = false;

		public boolean devMode = false;

		public final List<String> rcPaths = new ArrayList<>();

	}

	public static FlexoServiceManager createServiceManager(Options options) {
		FlexoServiceManager manager = new DefaultFlexoServiceManager(null, options.devMode);
		TechnologyAdapterService technologyAdapterService = manager.getTechnologyAdapterService();
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLRTTechnologyAdapter.class),
				true);
		return manager;
	}

	private static void usage() {
		StringBuilder usage = new StringBuilder();
		usage.append("Usage: fml-cli [options]\n");
		usage.append("\n");
		usage.append("- -h|--help: show this help.\n");
		usage.append("- -v|--verbose: verbose mode.\n");
		usage.append("- --rc path: resource center to register (may have several).\n");
		usage.append("\n");
		usage.append("\n");

		System.out.println(usage);
		System.exit(0);
	}

	private static Options parseOptions(String[] args) {
		Options options = new Options();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "--help":
					usage();
					break;
				case "--verbose":
					options.verbose = true;
					break;
				case "--rc":
					if (i + 1 < args.length) {
						options.rcPaths.add(args[++i]);
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				default: {
					if (arg.length() >= 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
						for (int j = 1; j < arg.length(); j++) {
							switch (arg.charAt(j)) {
								case 'h':
									usage();
									break;
								case 'v':
									options.verbose = true;
									break;
								default:
									System.err.println("Unknown short option '" + arg.charAt(j) + "'");
									System.exit(1);

							}
						}
					}
					else {
						System.err.println("Unknown long option '" + arg + "'");
						System.exit(1);
					}
				}

			}
		}

		return options;
	}

	public static void main(String[] args) throws IOException {
		Options options = parseOptions(args);

		FlexoLoggingManager.initialize(-1, true, null, options.verbose ? Level.INFO : Level.WARNING, null);

		FlexoServiceManager serviceManager = createServiceManager(options);
		// manager.registerService(new HttpService(options.serverOptions));

		for (String path : options.rcPaths) {
			FlexoResourceCenterService centerService = serviceManager.getResourceCenterService();
			DirectoryResourceCenter center = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(new File(path), centerService);
			centerService.addToResourceCenters(center);
		}

		CommandInterpreter ci = new CommandInterpreter(serviceManager, System.in, System.out, new File(System.getProperty("user.dir")));

		try {
			ci.start();
		} catch (Exception e) {
			System.out.println("Caught an Exception :");
			e.printStackTrace();
			try {
				System.out.println("Press enter to continue.");
				int c = System.in.read();
			} catch (IOException xx) {
			}
		}

	}

}
