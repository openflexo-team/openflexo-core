/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.cli.tools;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.cli.LaunchHeadlessFMLCLI;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.logging.FlexoLoggingManager;

/**
 * Migrate the FML of resource centers from the legacy notation of the arguments of a behaviour (<code>parameters.aValue</code>) to the
 * current one (<code>aValue</code>) (CORE-F-14).<br>
 * Each VirtualModel is loaded, and the model decides what to rewrite (see {@link BehaviourArgumentsRewriter}); the text of its file is then
 * edited where it must be, nothing else is touched (no re-serialization).<br>
 * Run it with the classpath of the technology adapters whose FML is migrated, for instance the one of the headless package:
 * 
 * <pre>
 * ./gradlew :packages:openflexo-headless:migrateLegacyParameters -Proots=/path/a/resources,/path/b/resources [-Papply=true]
 * </pre>
 * 
 * Without <code>--apply</code> nothing is written: the tool reports what it would change. It lists the bindings that keep the legacy
 * notation, which a person has to look at.
 * 
 * @author sylvain
 */
public class MigrateLegacyParametersNotation {

	/** Fragments of URIs of the resources left as they are (fixtures written on purpose in the legacy notation) */
	private static final List<String> skipped = new ArrayList<>();

	public static void main(String[] args) {
		boolean apply = false;
		List<File> roots = new ArrayList<>();
		for (String arg : args) {
			if ("--apply".equals(arg)) {
				apply = true;
			}
			else if (arg.startsWith("--skip=")) {
				skipped.add(arg.substring("--skip=".length()));
			}
			else {
				roots.add(new File(arg));
			}
		}
		if (roots.isEmpty()) {
			System.err.println("Usage: MigrateLegacyParametersNotation [--apply] <resource-center-directory>...");
			System.exit(2);
		}
		System.exit(run(roots, apply));
	}

	public static int run(List<File> roots, boolean apply) {
		FlexoLoggingManager.initialize(-1, true, null, Level.WARNING, null);
		LaunchHeadlessFMLCLI.Options options = new LaunchHeadlessFMLCLI.Options();
		options.enableDirectoryWatching = false;
		FlexoServiceManager serviceManager = LaunchHeadlessFMLCLI.createServiceManager(options);
		FlexoResourceCenterService centerService = serviceManager.getResourceCenterService();
		List<FlexoResourceCenter<?>> centers = new ArrayList<>();
		for (File root : roots) {
			try {
				DirectoryResourceCenter center = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(root, centerService);
				centerService.addToResourceCenters(center);
				centers.add(center);
			} catch (Exception e) {
				System.err.println("Cannot open " + root + ": " + e);
				return 2;
			}
		}

		int migrated = 0, unchanged = 0, failed = 0, remaining = 0;
		Collection<CompilationUnitResource> resources = new ArrayList<>(
				serviceManager.getVirtualModelLibrary().getCompilationUnitResources());
		for (CompilationUnitResource resource : resources) {
			if (!centers.contains(resource.getResourceCenter()) || isBuildOutput(resource) || isSkipped(resource)) {
				continue;
			}
			try {
				VirtualModel virtualModel = resource.getCompilationUnit() != null ? resource.getCompilationUnit().getVirtualModel() : null;
				if (virtualModel == null) {
					System.err.println("NOT LOADED  " + resource.getURI());
					failed++;
					continue;
				}
				List<FlexoConcept> concepts = new ArrayList<>();
				concepts.add(virtualModel);
				concepts.addAll(virtualModel.getAllFlexoConcepts());
				File file = sourceFile(resource);
				if (file == null) {
					System.err.println("NO FILE     " + resource.getURI());
					failed++;
					continue;
				}
				String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
				BehaviourArgumentsRewriter rewriter = new BehaviourArgumentsRewriter();
				List<BehaviourArgumentsRewriter.Edit> edits = rewriter.computeEdits(concepts, source);
				for (String report : rewriter.getReports()) {
					System.out.println("  LOOK AT     " + resource.getURI() + ": " + report);
					remaining++;
				}
				if (edits.isEmpty()) {
					unchanged++;
					continue;
				}
				System.out.println((apply ? "MIGRATED    " : "WOULD MIGRATE ") + resource.getURI() + " (" + edits.size() + " edit(s))");
				if (apply) {
					Files.write(file.toPath(), BehaviourArgumentsRewriter.apply(source, edits).getBytes(StandardCharsets.UTF_8));
				}
				migrated++;
			} catch (Exception | StackOverflowError e) {
				System.err.println("FAILED      " + resource.getURI() + ": " + e);
				failed++;
			}
		}
		System.out.println();
		System.out.println("Migrated: " + migrated + ", unchanged: " + unchanged + ", failed: " + failed + ", to look at: "
				+ remaining + (apply ? "" : " (dry run: nothing written, use --apply)"));
		return failed > 0 ? 1 : 0;
	}

	private static boolean isBuildOutput(CompilationUnitResource resource) {
		String path = String.valueOf(resource.getIODelegate());
		return path.contains("/bin/") || path.contains("/build/") || path.contains("/target/");
	}

	private static boolean isSkipped(CompilationUnitResource resource) {
		for (String fragment : skipped) {
			if (resource.getURI() != null && resource.getURI().contains(fragment)) {
				return true;
			}
		}
		return false;
	}

	/** The file holding the FML text of supplied resource, or null */
	private static File sourceFile(CompilationUnitResource resource) {
		if (resource.getIODelegate() == null || !(resource.getIODelegate().getSerializationArtefact() instanceof File)) {
			return null;
		}
		File file = (File) resource.getIODelegate().getSerializationArtefact();
		if (file.isDirectory()) {
			file = new File(file, file.getName());
		}
		return file.isFile() ? file : null;
	}

}
