package org.openflexo.foundation.fml.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.p2pp.testing.matrix.EditCase;
import org.openflexo.p2pp.testing.matrix.EditMatrix;

/**
 * The edit matrix of the p2pp test harness on FML: atomic edits of the model of the FML sources of the corpus (see {@link FMLEditGenerator}),
 * checked against the invariants of the harness (see {@link EditMatrix}). One test per kind of edit and position
 * ({@code rename-concept/first}, {@code visibility-behaviour/last}...). The groups that show a known difference are listed in
 * {@code fml-matrix-known-defects.txt} and tolerated by default in this module.
 */
@RunWith(Parameterized.class)
public class TestFMLEditMatrix {

	private static FlexoServiceManager serviceManager;

	@BeforeClass
	public static void initServiceManager() {
		serviceManager = FMLTestServiceManager.create();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> groups() throws Exception {
		FlexoServiceManager sm = FMLTestServiceManager.create();
		List<EditCase> cases = new ArrayList<>();
		for (Map.Entry<String, String> source : FMLCorpus.load().entrySet()) {
			FMLCompilationUnit unit = new FMLCompilationUnitParser().parse(source.getValue(), new FMLModelFactory(null, sm),
					modelSlotClasses -> null, true);
			cases.addAll(FMLEditGenerator.generate(source.getKey(), source.getValue(), unit));
		}
		return EditMatrix.group(cases);
	}

	private final String signature;
	private final List<EditCase> cases;

	public TestFMLEditMatrix(String signature, List<EditCase> cases) {
		this.signature = signature;
		this.cases = cases;
	}

	@Test
	public void invariantsHold() throws IOException, Exception {
		EditMatrix.run(new FMLTarget(serviceManager), signature, cases,
				EditMatrix.knownDefects(TestFMLEditMatrix.class, "/fml-matrix-known-defects.txt").get(signature));
	}
}
