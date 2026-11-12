package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConcept;

/**
 * Printing a unit holding a concept created in memory (never parsed), whose behaviour has an empty body, must notify nothing (CORE-D-59).
 * Printing links a new pretty-print node to each such object; that link marked the owners of the empty control graph modified and fired
 * "FMLPrettyPrint", and the FML editor, which prints again on that notification, looped until the stack overflowed (creating a concept
 * in the Free Modelling Editor froze the application).
 */
public class TestPrintNewConceptNotifiesNothing {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	@Test
	public void printingANewConceptWithAnEmptyBehaviourFiresNoPrettyPrintNotification() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(String.join("\n", //
				"@URI(\"http://openflexo.org/test/PrintNewConcept.fml\")", //
				"public model TestPrintNewConcept {", //
				"}", ""));
		FMLModelFactory factory = unit.getFMLModelFactory();
		FlexoConcept concept = factory.newFlexoConcept();
		concept.setName("NewConcept");
		ActionScheme behaviour = factory.newActionScheme();
		behaviour.setName("doIt");
		behaviour.setControlGraph(factory.newEmptyControlGraph());
		concept.addToFlexoBehaviours(behaviour);
		unit.getVirtualModel().addToFlexoConcepts(concept);

		List<String> fired = new ArrayList<>();
		PropertyChangeListener listener = evt -> {
			if ("FMLPrettyPrint".equals(evt.getPropertyName())) {
				fired.add(String.valueOf(evt.getSource().getClass().getSimpleName()));
			}
		};
		unit.getPropertyChangeSupport().addPropertyChangeListener(listener);
		String printed = unit.getFMLPrettyPrint();
		String printedAgain = unit.getFMLPrettyPrint();
		unit.getPropertyChangeSupport().removePropertyChangeListener(listener);

		assertTrue(printed, printed.contains("NewConcept"));
		assertEquals(printed, printedAgain);
		assertEquals("notifications fired while printing: " + fired, 0, fired.size());
	}
}
