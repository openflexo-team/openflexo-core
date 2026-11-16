/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of fml-cli-test, a component of the software infrastructure developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either version 1.1 of the License, or any later version ),
 * which is available at https://joinup.ec.europa.eu/software/page/eupl/licence-eupl and the GNU General Public License (GPL, either
 * version 3 of the License, or any later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * See http://www.openflexo.org/license.html for details.
 *
 */

package org.openflexo.foundation.fml.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import org.junit.Test;
import org.openflexo.foundation.fml.rt.error.FMLAssertionError;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLInvalidCommandException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.foundation.fml.rt.error.FMLHostError;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FileUtils.CopyStrategy;

/**
 * How fml-cli reports the failure of a script (CORE-F-11 phase 5): the line of the failing command and the FML stack, a top-level
 * {@code return}, the {@code execute} directive, and the exit codes of the headless launcher. Scripts of
 * {@code TestResourceCenter/ScriptFailures}.
 */
public class TestScriptFailureReporting extends FMLScriptParserTestCase {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	private CommandInterpreter newInterpreter() throws IOException {
		instanciateTestServiceManager();
		CommandInterpreter interpreter = new CommandInterpreter(serviceManager, System.in, new PrintStream(out, true),
				new PrintStream(err, true), HOME_DIR);
		// Scripts executed by a script are found next to it
		interpreter.setWorkingDirectory(scriptFile("TopLevelReturn.fmlscript").getParentFile());
		return interpreter;
	}

	private static File scriptsDir;

	/** The scripts, copied to a folder of their own: the resources may be in a jar */
	private static File scriptFile(String name) throws IOException {
		if (scriptsDir == null) {
			File dir = Files.createTempDirectory("ScriptFailures").toFile();
			scriptsDir = FileUtils.copyResourceToDir(ResourceLocator.locateResource("TestResourceCenter/ScriptFailures"), dir,
					CopyStrategy.REPLACE);
			assertNotNull("Scripts not copied", scriptsDir);
		}
		return new File(scriptsDir, name);
	}

	/** Execute supplied script of the folder, return its failure (null when it succeeds) */
	private FMLCommandExecutionException execute(String folder, String name) throws Exception {
		CommandInterpreter interpreter = newInterpreter();
		FMLScript script = parseFMLScript(ResourceLocator.locateResource("TestResourceCenter/" + folder + "/" + name), interpreter);
		try {
			script.execute();
		} catch (FMLCommandExecutionException e) {
			return e;
		}
		return null;
	}

	@Test
	public void topLevelReturnEndsTheScript() throws Exception {
		assertEquals(null, execute("ScriptFailures", "TopLevelReturn.fmlscript"));
		assertTrue(out.toString(), out.toString().contains("Returned 2"));
		assertEquals("", err.toString());
	}

	@Test
	public void bareReturnEndsTheScript() throws Exception {
		assertEquals(null, execute("ScriptFailures", "BareReturn.fmlscript"));
	}

	@Test
	public void failingCommandGivesItsLine() throws Exception {
		FMLCommandExecutionException failure = execute("ScriptFailures", "FailingCommand.fmlscript");
		assertNotNull(failure);
		assertEquals(3, failure.getLine());
		assertTrue(String.valueOf(failure.getFailure()), failure.getFailure() instanceof FMLHostError);
		assertTrue(failure.isForeseen());
		assertTrue(failure.getMessage(), failure.getMessage().startsWith("Line 3: "));
		assertTrue(failure.describe(), failure.describe().startsWith("Line 3: Failure of Java code: "));
		// Not printed by the script: its caller reports it
		assertEquals("", err.toString());
	}

	/** A failure in a behaviour gives the line of the command and the FML stack of the behaviour */
	@Test
	public void failingBehaviourGivesItsFMLStack() throws Exception {
		FMLCommandExecutionException failure = execute("ErrorKinds", "NullCondition.fmlscript");
		assertNotNull(failure);
		assertEquals(5, failure.getLine());
		assertTrue(String.valueOf(failure.getFailure()), failure.getFailure() instanceof FMLNullReferenceError);
		String description = failure.describe();
		System.out.println("Description of the failure:\n" + description);
		assertTrue(description, description.startsWith("Line 5: Null reference: "));
		assertTrue(description, description.contains("nullCondition()"));
		assertTrue(description, description.contains(", line 80"));
	}

	@Test
	public void failedAssertionGivesItsLineAndOperands() throws Exception {
		FMLCommandExecutionException failure = execute("ScriptFailures", "FailedAssertion.fmlscript");
		assertTrue(String.valueOf(failure), failure instanceof FMLAssertException);
		assertEquals("Line 3: assert failed: x[1] == 2", failure.getMessage());
		// The failure is an FMLAssertionError, a foreseen FML error (CORE-F-11 phase 7)
		assertTrue(String.valueOf(failure.getFailure()), failure.getFailure() instanceof FMLAssertionError);
		assertEquals("Line 3: Assertion failed: x[1] == 2", failure.describe());
	}

	@Test
	public void executedScriptIsNamed() throws Exception {
		FMLCommandExecutionException failure = execute("ScriptFailures", "ExecuteFailing.fmlscript");
		assertNotNull(failure);
		assertEquals("FailingCommand.fmlscript", failure.getSource());
		assertEquals(3, failure.getLine());
		assertTrue(failure.describe(), failure.describe().startsWith("FailingCommand.fmlscript, line 3: Failure of Java code: "));
		assertTrue(err.toString(), err.toString().isEmpty());
	}

	@Test
	public void executedScriptMissing() throws Exception {
		FMLCommandExecutionException failure = execute("ScriptFailures", "ExecuteMissing.fmlscript");
		assertTrue(String.valueOf(failure), failure instanceof FMLInvalidCommandException);
		// Located at the execute command of the outer script, which names the missing one
		assertEquals(2, failure.getLine());
		assertEquals(null, failure.getSource());
		assertTrue(failure.getMessage(), failure.getMessage().startsWith("Line 2: Script file not found: "));
		assertTrue(failure.getMessage(), failure.getMessage().contains("Missing.fmlscript"));
		assertTrue(out.toString(), !out.toString().contains("not found"));
	}

	@Test
	public void exitCodes() throws Exception {
		assertEquals(LaunchHeadlessFMLCLI.EXIT_SUCCESS, LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("TopLevelReturn.fmlscript")));
		assertEquals(LaunchHeadlessFMLCLI.EXIT_FML_ERROR,
				LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("FailingCommand.fmlscript")));
		assertTrue(err.toString(), err.toString().contains("FailingCommand.fmlscript, line 3: Failure of Java code: "));
		assertEquals(LaunchHeadlessFMLCLI.EXIT_FML_ERROR,
				LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("FailedAssertion.fmlscript")));
		assertEquals(LaunchHeadlessFMLCLI.EXIT_INVALID, LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("ParseError.fmlscript")));
		assertEquals(LaunchHeadlessFMLCLI.EXIT_INVALID,
				LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("InvalidCommand.fmlscript")));
		err.reset();
		assertEquals(LaunchHeadlessFMLCLI.EXIT_INVALID,
				LaunchHeadlessFMLCLI.runScript(newInterpreter(), scriptFile("ExecuteMissing.fmlscript")));
		assertTrue(err.toString(), err.toString().startsWith("ExecuteMissing.fmlscript, line 2: Script file not found: "));
		assertEquals(LaunchHeadlessFMLCLI.EXIT_INVALID, LaunchHeadlessFMLCLI.runScript(newInterpreter(), new File("NoSuchScript.fmlscript")));
	}

	@Test
	public void options() {
		LaunchHeadlessFMLCLI.Options options = LaunchHeadlessFMLCLI.parseOptions(new String[] { "-v", "--rc", "a", "s.fmlscript" });
		assertTrue(options.verbose);
		assertEquals("s.fmlscript", options.scriptPath);
		assertEquals(1, options.rcPaths.size());
		assertTrue(LaunchHeadlessFMLCLI.parseOptions(new String[] { "-h" }).help);
		for (String[] wrong : new String[][] { { "a.fmlscript", "b.fmlscript" }, { "-x" }, { "--rc" }, { "--wrong" } }) {
			try {
				LaunchHeadlessFMLCLI.parseOptions(wrong);
				fail("Wrong options accepted: " + String.join(" ", wrong));
			} catch (IllegalArgumentException e) {
				assertNotNull(e.getMessage());
			}
		}
		assertEquals(LaunchHeadlessFMLCLI.EXIT_INVALID, LaunchHeadlessFMLCLI.run(new String[] { "--wrong" }));
	}

}
