/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Openflexo-core, a component of the software infrastructure
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

package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.editionaction.AbstractSelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteBehaviourParameter;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Merges an edited text into a loaded compilation unit with {@link FMLCompilationUnit#updateWith(Object)}, as the FML editor does at
 * each keystroke, and checks that every reference one FML object holds to another designates, after the merge, the object of the
 * loaded compilation unit, never the one of the compilation unit just parsed (which is thrown away).
 *
 * Each step retargets ONE more reference of the fixture {@code TestUpdateWithReferences.fml} from {@code Alpha} to {@code Gamma}, then
 * checks all of them. Covers the references that the deprecated String URI properties used to carry (CORE-F-8 phase 4): the parent
 * concepts, the concept of a role, of a select, of a 'begin match', a 'match' and an 'end match', the property of a matching criteria,
 * the behaviour and the arguments of an 'end match', the event of a listener.
 */
@RunWith(OrderedRunner.class)
public class TestUpdateWithReferences extends OpenflexoTestCase {

	private static final String URI = "http://openflexo.org/test/TestResourceCenter/TestUpdateWithReferences.fml";

	private static FMLCompilationUnitParser fmlParser = new FMLCompilationUnitParser();

	private static CompilationUnitResource fmlResource;
	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel vm;

	// Current value of each reference, as written in the text
	private static String role = "Alpha", select = "Alpha", parent = "Alpha", match = "Alpha", criteria = "x",
			unmatched = "markAlpha()", event = "AlphaEvent";

	/**
	 * The text of the fixture, with the current value of each reference
	 */
	private static String text() {
		return "import java.util.List;\n\n" //
				+ "@URI(\"" + URI + "\")\n" //
				+ "public model TestUpdateWithReferences {\n" //
				+ "	" + role + " myRole;\n" //
				+ "	public List<" + select + "> all() {\n" //
				+ "		return select " + select + " from this;\n" //
				+ "	}\n" //
				+ "	public int sync() {\n" //
				+ "		MatchingSet<" + match + "> matched = begin match " + match + " from this;\n" //
				+ "		for (" + match + " a : select " + match + " from this) {\n" //
				+ "			match " + match + " in matched from this where (" + criteria + " = a.x) unmatched: new " + match + "(a.x);\n" //
				+ "		}\n" //
				+ "		end match " + match + " in matched unmatched: " + unmatched + ";\n" //
				+ "		return 0;\n" //
				+ "	}\n" //
				+ "	listen " + event + " from this {\n" //
				+ "		log \"event\";\n" //
				+ "	}\n" //
				+ "	event AlphaEvent {\n" //
				+ "	}\n" //
				+ "	event GammaEvent {\n" //
				+ "	}\n" //
				+ "	public concept Alpha {\n" //
				+ "		String x;\n" //
				+ "		String y;\n" //
				+ "		create(String x) {\n" //
				+ "			x = parameters.x;\n" //
				+ "		}\n" //
				+ "		markAlpha() {\n" //
				+ "		}\n" //
				+ "		markGamma(String reason) {\n" //
				+ "		}\n" //
				+ "	}\n" //
				+ "	public concept Beta extends " + parent + " {\n" //
				+ "	}\n" //
				+ "	public concept Gamma {\n" //
				+ "		String x;\n" //
				+ "		String y;\n" //
				+ "		int z;\n" //
				+ "		create(String x) {\n" //
				+ "			x = parameters.x;\n" //
				+ "		}\n" //
				+ "		markAlpha() {\n" //
				+ "		}\n" //
				+ "		markGamma(String reason) {\n" //
				+ "		}\n" //
				+ "	}\n" //
				+ "}\n";
	}

	@Test
	@TestOrder(1)
	public void loadFixture() throws Exception {
		instanciateTestServiceManager();
		vm = serviceManager.getVirtualModelLibrary().getVirtualModel(URI);
		assertNotNull(vm);
		fmlResource = vm.getResource();
		compilationUnit = fmlResource.getCompilationUnit();
		assertNotNull(vm.getFlexoConcept("Gamma"));
		checkReferences();
	}

	@Test
	@TestOrder(2)
	public void retargetRole() throws Exception {
		role = "Gamma";
		mergeAndCheck();
	}

	@Test
	@TestOrder(3)
	public void retargetSelect() throws Exception {
		select = "Gamma";
		mergeAndCheck();
	}

	@Test
	@TestOrder(4)
	public void retargetParent() throws Exception {
		parent = "Gamma";
		mergeAndCheck();
	}

	@Test
	@TestOrder(5)
	public void retargetMatch() throws Exception {
		match = "Gamma";
		mergeAndCheck();
	}

	@Test
	@TestOrder(6)
	public void retargetCriteria() throws Exception {
		criteria = "y";
		mergeAndCheck();
	}

	/**
	 * The parser does not resolve the behaviour of 'unmatched: someMethod(...)' yet (CORE-D-2): from this step on, each merge first sets
	 * on the parsed action what the parser should, so that the merge of the behaviour and of its arguments is checked
	 */
	@Test
	@TestOrder(7)
	public void retargetUnmatchedBehaviour() throws Exception {
		unmatched = "markGamma(\"gone\")";
		mergeAndCheck();
	}

	/**
	 * Sets on the parsed 'end match' the behaviour and the argument of the text, which the parser does not do yet (CORE-D-2)
	 */
	private static void setUnmatchedBehaviourAsWritten(FMLCompilationUnit parsed) {
		FinalizeMatching parsedAction = action(parsed.getVirtualModel(), "sync", FinalizeMatching.class);
		FlexoBehaviour markGamma = parsed.getVirtualModel().getFlexoConcept("Gamma").getFlexoBehaviour("markGamma");
		parsedAction.setFlexoBehaviour(markGamma);
		assertEquals(1, parsedAction.getParameters().size());
		parsedAction.getParameters().get(0).setValue(new DataBinding<>("\"gone\""));
	}

	@Test
	@TestOrder(8)
	public void retargetEvent() throws Exception {
		event = "GammaEvent";
		mergeAndCheck();
	}

	/**
	 * Parses the current text, merges it into the loaded compilation unit and checks the references
	 */
	private static void mergeAndCheck() throws Exception {
		FMLCompilationUnit parsed = fmlParser.parse(text(), fmlResource.getFactory(),
				(modelSlotClasses) -> fmlResource.updateFMLModelFactory(modelSlotClasses), true);
		assertEquals(5, parsed.getVirtualModel().getFlexoConcepts().size());
		if (unmatched.startsWith("markGamma")) {
			setUnmatchedBehaviourAsWritten(parsed);
		}
		compilationUnit.updateWith(parsed);
		compilationUnit.getTypingSpace().resolveUnresolvedTypes();
		checkReferences();
	}

	/**
	 * Every reference designates the object named by the text, in the loaded compilation unit
	 */
	private static void checkReferences() {
		List<String> failures = checkAllReferences();
		assertTrue("Wrong references: " + failures, failures.isEmpty());
	}

	private static List<String> checkAllReferences() {
		System.out.println("Checking:\n" + compilationUnit.getFMLPrettyPrint());
		List<String> failures = new ArrayList<>();
		assertEquals(5, vm.getFlexoConcepts().size());
		for (FlexoConcept concept : vm.getFlexoConcepts()) {
			assertSame(compilationUnit, concept.getDeclaringCompilationUnit());
		}
		FlexoConcept beta = vm.getFlexoConcept("Beta");
		assertEquals(1, beta.getParentFlexoConcepts().size());
		check(failures, "extends", vm.getFlexoConcept(parent), beta.getParentFlexoConcepts().get(0));
		check(failures, "role", vm.getFlexoConcept(role), role().getFlexoConceptType());
		check(failures, "select", vm.getFlexoConcept(select), select().getFlexoConceptType());
		FlexoConcept matched = vm.getFlexoConcept(match);
		check(failures, "begin match", matched, initiateMatching().getFlexoConceptType());
		check(failures, "match", matched, match().getFlexoConceptType());
		check(failures, "criteria", matched.getAccessibleProperty(criteria), criteria().getFlexoProperty());
		check(failures, "end match", matched, finalizeMatching().getFlexoConceptType());
		FlexoBehaviour behaviour = matched.getFlexoBehaviour(unmatched.substring(0, unmatched.indexOf('(')));
		check(failures, "unmatched", behaviour, finalizeMatching().getFlexoBehaviour());
		int expectedArguments = behaviour.getParameters().size();
		if (finalizeMatching().getParameters().size() != expectedArguments) {
			failures.add("arguments: " + finalizeMatching().getParameters().size() + " instead of " + expectedArguments);
			expectedArguments = 0;
		}
		for (int i = 0; i < expectedArguments; i++) {
			ExecuteBehaviourParameter argument = finalizeMatching().getParameters().get(i);
			check(failures, "argument", behaviour.getParameters().get(i), argument.getParam());
			if (!"\"gone\"".equals(argument.getValue().toString())) {
				failures.add("argument value: " + argument.getValue());
			}
		}
		check(failures, "event", vm.getFlexoConcept(event), listener().getEventType().getFlexoConcept());
		System.out.println("Wrong references: " + failures);
		return failures;
	}

	private static void check(List<String> failures, String reference, Object expected, Object actual) {
		if (expected != actual) {
			failures.add(reference + ": " + actual + " instead of " + expected);
		}
	}

	private static FlexoConceptInstanceRole role() {
		FlexoProperty<?> property = vm.getDeclaredProperty("myRole");
		assertTrue("" + property, property instanceof FlexoConceptInstanceRole);
		return (FlexoConceptInstanceRole) property;
	}

	private static EventListener listener() {
		for (FlexoBehaviour behaviour : vm.getFlexoBehaviours()) {
			if (behaviour instanceof EventListener) {
				return (EventListener) behaviour;
			}
		}
		throw new AssertionError("No listener");
	}

	private static <T> T action(String behaviourName, Class<T> actionClass) {
		return action(vm, behaviourName, actionClass);
	}

	@SuppressWarnings("unchecked")
	private static <T> T action(VirtualModel virtualModel, String behaviourName, Class<T> actionClass) {
		List<FMLControlGraph> actions = new ArrayList<>();
		virtualModel.getFlexoBehaviour(behaviourName).getControlGraph().accept((FMLControlGraphVisitor) actions::add);
		for (FMLControlGraph action : actions) {
			if (actionClass.isInstance(action)) {
				return (T) action;
			}
		}
		throw new AssertionError("No " + actionClass.getSimpleName() + " in " + actions);
	}

	private static AbstractSelectFlexoConceptInstance<?, ?> select() {
		return action("all", AbstractSelectFlexoConceptInstance.class);
	}

	private static InitiateMatching initiateMatching() {
		return action("sync", InitiateMatching.class);
	}

	private static MatchFlexoConceptInstance match() {
		return action("sync", MatchFlexoConceptInstance.class);
	}

	private static MatchingCriteria criteria() {
		assertEquals(1, match().getMatchingCriterias().size());
		return match().getMatchingCriterias().get(0);
	}

	private static FinalizeMatching finalizeMatching() {
		return action("sync", FinalizeMatching.class);
	}
}
