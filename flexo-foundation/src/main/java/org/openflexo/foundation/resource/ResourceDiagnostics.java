/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
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
 *          of EPL 1.0, the licensors of this Program grant you additional
 *          permission to convey the resulting work. *
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


package org.openflexo.foundation.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.Collection;

import org.openflexo.foundation.FlexoProject;

/**
 * TEMPORARY diagnostics of resource registration (intermittent reload failures of flexo-foundation-test on Jenkins only).<br>
 * Inactive unless the system property <code>openflexo.resourceDiagnostics</code> names a directory: each JVM then appends its events to
 * its own file there. Resources of a jar resource center are ignored, to keep the volume reasonable.
 * <p>
 * To be removed once the cause is found.
 */
public class ResourceDiagnostics {

	private static final String DIRECTORY = System.getProperty("openflexo.resourceDiagnostics");
	private static PrintWriter writer;

	private ResourceDiagnostics() {
	}

	public static boolean isEnabled() {
		return DIRECTORY != null;
	}

	/** Whether a resource is worth tracing: not one of a jar resource center */
	public static boolean isTraced(FlexoResource<?> resource) {
		return isEnabled() && resource != null && !(resource.getResourceCenter() instanceof JarResourceCenter);
	}

	public static void log(String event) {
		if (isEnabled()) {
			write(event, false);
		}
	}

	public static void logWithStack(String event) {
		if (isEnabled()) {
			write(event, true);
		}
	}

	public static String describe(FlexoResource<?> resource) {
		if (resource == null) {
			return "null";
		}
		// The stored URI only: computing the default one may cache it, and change what is observed
		String uri;
		try {
			uri = resource instanceof FlexoResourceImpl ? ((FlexoResourceImpl<?>) resource).getStoredURIForDiagnostics() : "?";
		} catch (RuntimeException e) {
			uri = "<" + e + ">";
		}
		String rc;
		try {
			rc = describe(resource.getResourceCenter());
		} catch (RuntimeException e) {
			rc = "<" + e + ">";
		}
		String loaded;
		try {
			loaded = String.valueOf(resource.isLoaded());
		} catch (RuntimeException e) {
			loaded = "<" + e + ">";
		}
		return resource.getClass().getSimpleName().replaceAll("_\\$\\$_.*", "") + "@" + id(resource) + "[uri=" + uri + " rc=" + rc
				+ " loaded=" + loaded + "]";
	}

	public static String describe(FlexoResourceCenter<?> rc) {
		if (rc == null) {
			return "null";
		}
		String kind = rc instanceof FlexoProject ? "Project" : rc.getClass().getSimpleName().replaceAll("_\\$\\$_.*", "");
		String base;
		try {
			base = rc.getDefaultBaseURI();
		} catch (RuntimeException e) {
			base = "<" + e + ">";
		}
		return kind + "@" + id(rc) + "(" + base + ")";
	}

	public static String describe(Collection<? extends FlexoResource<?>> resources) {
		StringBuilder sb = new StringBuilder();
		for (FlexoResource<?> r : resources) {
			sb.append("\n      ").append(describe(r));
		}
		return sb.toString();
	}

	public static String id(Object o) {
		return Integer.toHexString(System.identityHashCode(o));
	}

	private static synchronized void write(String event, boolean withStack) {
		try {
			if (writer == null) {
				File dir = new File(DIRECTORY);
				dir.mkdirs();
				String jvm = ManagementFactory.getRuntimeMXBean().getName().replaceAll("[^A-Za-z0-9.-]", "_");
				writer = new PrintWriter(new FileWriter(new File(dir, "diag-" + jvm + ".log"), true), true);
				writer.println(System.nanoTime() + " JVM " + jvm + " user.dir=" + System.getProperty("user.dir") + " java.io.tmpdir="
						+ System.getProperty("java.io.tmpdir") + " java.version=" + System.getProperty("java.version"));
			}
			writer.println(System.nanoTime() + " [" + Thread.currentThread().getName() + "] " + event);
			if (withStack) {
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				int printed = 0;
				for (int i = 3; i < stack.length && printed < 25; i++) {
					String frame = stack[i].toString();
					if (frame.startsWith("org.openflexo") && !frame.contains("ResourceDiagnostics")) {
						writer.println("        at " + frame);
						printed++;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
