package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.p2pp.testing.KnownDefects;
import org.openflexo.p2pp.testing.UntouchedChecks;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * The first level of the p2pp test harness on FML: the FML sources of the test resources that stand alone (the parsing examples, the files of
 * the pretty-print tests). The source parses; its derived pretty-print is the source, to the character; the derived and the normalized
 * pretty-prints parse back to the same model, which normalizes to the same text.
 * <p>
 * It complements the {@code TestFMLPrettyPrintN} tests, which edit a model and compare with expected files. See {@link KnownDefects} for the
 * defects the run can tolerate.
 */
@RunWith(Parameterized.class)
public class TestFMLPrettyPrintInvariants {

	private static final String[] DIRECTORIES = { "FMLParsingExamples", "TestFMLPrettyPrint1", "TestFMLPrettyPrint2", "TestFMLPrettyPrint3",
			"TestFMLPrettyPrint4", "TestFMLPrettyPrint5", "TestFMLPrettyPrint6", "TestFMLPrettyPrint7", "TestFMLPrettyPrint8",
			"TestFMLPrettyPrint9", "TestFMLPrettyPrint10", "TestFMLPrettyPrint11" };

	private static FlexoServiceManager serviceManager;

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		List<Object[]> returned = new ArrayList<>();
		for (String directory : DIRECTORIES) {
			for (Object[] example : Resources.getMatchingResource(ResourceLocator.locateResource(directory), ".fml")) {
				returned.add(new Object[] { example[0], directory + "/" + example[1] });
			}
		}
		return returned;
	}

	private final Resource fmlResource;
	private final String name;

	public TestFMLPrettyPrintInvariants(Resource fmlResource, String name) {
		this.fmlResource = fmlResource;
		this.name = name;
	}

	private String source() throws IOException {
		try (InputStream in = fmlResource.openInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/** The files that show a known defect, with their defects, as listed in fml-invariants-known-defects.txt */
	private static Map<String, String> knownDefects() throws IOException {
		Map<String, String> returned = new HashMap<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(
				TestFMLPrettyPrintInvariants.class.getResourceAsStream("/fml-invariants-known-defects.txt"), StandardCharsets.UTF_8))) {
			String row;
			while ((row = r.readLine()) != null) {
				row = row.trim();
				if (!row.isEmpty() && !row.startsWith("#")) {
					String[] parts = row.split("\\s+", 2);
					returned.put(parts[0], parts[1]);
				}
			}
		}
		return returned;
	}

	@Test
	public void invariantsHold() throws IOException {
		List<String> failures = UntouchedChecks.check(new FMLTarget(serviceManager), source());
		String known = knownDefects().get(name);
		if (known != null && KnownDefects.areTolerated()) {
			if (failures.isEmpty()) {
				fail(name + ": " + known + " seems fixed: remove the row of this file in fml-invariants-known-defects.txt");
			}
			KnownDefects.register(known, "TestFMLPrettyPrintInvariants." + name);
			return;
		}
		if (!failures.isEmpty()) {
			fail(name + ": " + failures.size() + " invariant(s) fail\n" + String.join("\n", failures));
		}
	}

	@BeforeClass
	public static void initServiceManager() {
		serviceManager = FMLTestServiceManager.create();
	}
}
