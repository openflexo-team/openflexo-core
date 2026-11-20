package org.openflexo.foundation.fml.rt.editionaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class TestMatchingCriteriaValidation extends OpenflexoTestCase {

	private static long count(ValidationReport report) {
		long n = 0;
		for (ValidationIssue<?, ?> i : report.getAllErrors()) {
			if (i.getCause() instanceof MatchingCriteria.CriteriaMustAddressAPropertyOfTheMatchedConcept) {
				n++;
			}
		}
		return n;
	}

	private ValidationReport validate(String uri) throws Exception {
		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(uri);
		assertNotNull(vm);
		assertFalse(vm.getAllFlexoConcepts().isEmpty());
		ValidationModel vmodel = serviceManager.getVirtualModelLibrary().getFMLValidationModel();
		return vmodel.validate(vm.getCompilationUnit());
	}

	@Test
	@TestOrder(1)
	public void load() throws Exception {
		instanciateTestServiceManager();
	}

	@Test
	@TestOrder(2)
	public void validCriterionNotReported() throws Exception {
		assertEquals(0, count(validate("http://openflexo.org/test/TestResourceCenter/KDGh20b.fml")));
	}

	@Test
	@TestOrder(3)
	public void selectedCriterionReported() throws Exception {
		assertEquals(1, count(validate("http://openflexo.org/test/TestResourceCenter/KDGh20c.fml")));
	}

	@Test
	@TestOrder(4)
	public void inheritedPropertyNotReported() throws Exception {
		String text = String.join("\n", "@URI(\"http://openflexo.org/test/MatchingCriteriaValidation.fml\")", "public model MatchingCriteriaValidation {",
				"\tpublic concept Wrapper {", "\t}", "\tpublic concept Base {", "\t\tWrapper w;", "\t}",
				"\tpublic concept Holder extends Base {", "\t\tcreate(Wrapper w) {", "\t\t\tthis.w = w;", "\t\t}", "\t}",
				"\tpublic synchronize() {", "\t\tMatchingSet<Holder> ms = begin match Holder from this;",
				"\t\tfor (Wrapper wr : select Wrapper from this) {",
				"\t\t\tmatch Holder in ms from this where (w = wr) unmatched: new Holder(wr);", "\t\t}", "\t\tend match Holder in ms;",
				"\t}", "}", "");
		FMLCompilationUnit unit = new FMLCompilationUnitParser().parse(text, new FMLModelFactory(null, serviceManager),
				l -> null, true);
		ValidationModel vmodel = serviceManager.getVirtualModelLibrary().getFMLValidationModel();
		assertEquals(0, count(vmodel.validate(unit)));
	}
}
