package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;

/**
 * PAMELA-D-7 (openflexo-team/pamela#16): PAMELA's TypeConverter does not read back the type {@code void}, so cloning a concept that
 * declares a behaviour returning void fails, and copying it in the FML editor does nothing. Asserts the intended behaviour and fails
 * until the defect is fixed. Skipped unless the environment variable {@code OPENFLEXO_KNOWN_DEFECTS} is set:
 *
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-parser:cleanTest :fml-parser:test --tests '*KnownDefectsCloneVoidBehaviour*'
 * </pre>
 */
public class KnownDefectsCloneVoidBehaviourTest {

	@BeforeClass
	public static void initServiceManager() {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
		TestFMLReindent.initServiceManager();
	}

	@Test
	public void aConceptWithAVoidBehaviourCanBeCloned() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(String.join("\n", //
				"@URI(\"http://openflexo.org/test/CloneVoid.fml\")", //
				"public model CloneVoid {", //
				"	public concept Root1 {", //
				"		public void doIt() {", //
				"		}", //
				"	}", //
				"}", "")).getVirtualModel();
		FlexoConcept clone = (FlexoConcept) vm.getFlexoConcept("Root1").cloneObject();
		FlexoBehaviour doIt = clone.getFlexoBehaviour("doIt");
		assertNotNull(doIt);
		assertEquals(Void.TYPE, doIt.getReturnType());
	}
}
