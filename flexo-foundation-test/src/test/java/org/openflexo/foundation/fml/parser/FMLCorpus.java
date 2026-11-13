package org.openflexo.foundation.fml.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * The FML sources of the edit matrix: the parsing examples and the initial models of the pretty-print tests that p2pp reproduces when they are
 * untouched (those listed in {@code fml-invariants-known-defects.txt} are left out: their own differences would be in every case).
 */
final class FMLCorpus {

	private static final String[] DIRECTORIES = { "FMLParsingExamples", "TestFMLPrettyPrint1", "TestFMLPrettyPrint3", "TestFMLPrettyPrint4",
			"TestFMLPrettyPrint5", "TestFMLPrettyPrint6", "TestFMLPrettyPrint8", "TestFMLPrettyPrint9", "TestFMLPrettyPrint10" };

	private FMLCorpus() {
	}

	static Map<String, String> load() throws IOException {
		Set<String> excluded = knownDifferences();
		Map<String, String> returned = new LinkedHashMap<>();
		for (String directory : DIRECTORIES) {
			for (Object[] example : Resources.getMatchingResource(ResourceLocator.locateResource(directory), ".fml")) {
				String name = directory + "/" + example[1];
				String file = (String) example[1];
				boolean initial = directory.equals("FMLParsingExamples") || file.startsWith("Initial");
				if (initial && !excluded.contains(name)) {
					try (InputStream in = ((Resource) example[0]).openInputStream()) {
						returned.put(name, new String(in.readAllBytes(), StandardCharsets.UTF_8));
					}
				}
			}
		}
		return returned;
	}

	private static Set<String> knownDifferences() throws IOException {
		Set<String> returned = new HashSet<>();
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(FMLCorpus.class.getResourceAsStream("/fml-invariants-known-defects.txt"), StandardCharsets.UTF_8))) {
			String row;
			while ((row = r.readLine()) != null) {
				row = row.trim();
				if (!row.isEmpty() && !row.startsWith("#")) {
					returned.add(row.split("\\s+")[0]);
				}
			}
		}
		return returned;
	}
}
