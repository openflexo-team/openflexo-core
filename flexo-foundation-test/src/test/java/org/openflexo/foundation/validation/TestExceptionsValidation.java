/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation-test, a component of the software infrastructure 
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
package org.openflexo.foundation.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoException;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.controlgraph.CatchClause;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.controlgraph.TryStatement;
import org.openflexo.foundation.fml.rt.error.FMLError;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Exceptions defined in FML, throw and try / catch / finally as seen by the analysis and the validation (CORE-F-11 phase 7): the valid
 * fixture {@code FML/Exceptions.fml} validates without error, and each behaviour of {@code FML/InvalidExceptions.fml} raises its issue
 */
@RunWith(OrderedRunner.class)
public class TestExceptionsValidation extends OpenflexoTestCase {

	static final String VALID_URI = "http://openflexo.org/test/TestResourceCenter/Exceptions.fml";
	static final String INVALID_URI = "http://openflexo.org/test/TestResourceCenter/InvalidExceptions.fml";

	static VirtualModelLibrary vmLibrary;

	@Test
	@TestOrder(1)
	public void loadServiceManager() throws Exception {
		instanciateTestServiceManager();
		vmLibrary = serviceManager.getVirtualModelLibrary();
		assertNotNull(vmLibrary);
	}

	@Test
	@TestOrder(2)
	public void validFixture() throws Exception {
		VirtualModel vm = vmLibrary.getVirtualModel(VALID_URI);
		assertNotNull(vm);
		assertFalse("Exceptions.fml most likely failed to parse", vm.getFlexoConcepts().isEmpty());

		ValidationReport report = vmLibrary.getFMLValidationModel().validate(vm.getCompilationUnit());
		assertEquals(describe(vmLibrary.getFMLValidationModel(), report), 0, report.getErrorsCount());

		// The exceptions and their hierarchy
		FlexoConcept stockError = vm.getFlexoConcept("StockError");
		FlexoConcept outOfStock = vm.getFlexoConcept("OutOfStock");
		assertTrue(stockError instanceof FlexoException);
		assertTrue(stockError.isAbstract());
		assertTrue(outOfStock instanceof FlexoException);
		assertTrue(stockError.isAssignableFrom(outOfStock));

		// throws clauses: parsed, written back
		assertEquals(1, vm.getFlexoBehaviour("take", Integer.TYPE).getThrownTypes().size());
		assertEquals(outOfStock, ((FlexoConceptInstanceType) vm.getFlexoBehaviour("take", Integer.TYPE).getThrownTypes().get(0)).getFlexoConcept());
		assertTrue(vm.getCompilationUnit().getFMLPrettyPrint().contains("public take(int n) throws OutOfStock {"));
		assertTrue(vm.getCompilationUnit().getNormalizedFML().contains("throws OutOfStock"));

		// An exception declared in a concept is embedded in it
		FlexoConcept box = vm.getFlexoConcept("Box");
		assertNotNull(box);
		assertTrue(box.getEmbeddedFlexoConcepts().stream().anyMatch(c -> c instanceof FlexoException && c.getName().equals("Locked")));

		// The try statement of firstMatchingClause(): three clauses, in order, the second naming an FML exception
		TryStatement tryStatement = findTry(vm.getFlexoBehaviour("firstMatchingClause"));
		assertNotNull(tryStatement);
		assertEquals(3, tryStatement.getCatchClauses().size());
		CatchClause second = tryStatement.getCatchClauses().get(1);
		assertSame(tryStatement, second.getTryStatement());
		assertEquals(outOfStock, ((FlexoConceptInstanceType) second.getCatchType()).getFlexoConcept());
		assertSame(FMLError.class, tryStatement.getCatchClauses().get(2).getCatchType());
		assertTrue(tryStatement.getCatchClauses().get(2).catches(new FMLNullReferenceError("x")));
		assertFalse(tryStatement.getCatchClauses().get(0).catches(new FMLNullReferenceError("x")));

		// The variable of a clause is visible in its block, not in the try block
		assertNotNull(second.getControlGraph().getBindingModel().bindingVariableNamed("e"));
		assertEquals(null, tryStatement.getTryControlGraph().getBindingModel().bindingVariableNamed("e"));

		// A finally block, and no import added for the implicit error types
		assertNotNull(findTry(vm.getFlexoBehaviour("returnThroughTry")).getFinallyControlGraph());
		assertFalse(vm.getCompilationUnit().getFMLPrettyPrint().contains("import org.openflexo.foundation.fml.rt.error"));
	}

	@Test
	@TestOrder(3)
	public void invalidUsesAreReported() throws Exception {
		VirtualModel vm = vmLibrary.getVirtualModel(INVALID_URI);
		assertNotNull(vm);
		assertFalse("InvalidExceptions.fml most likely failed to parse", vm.getFlexoConcepts().isEmpty());

		ValidationModel validationModel = vmLibrary.getFMLValidationModel();
		ValidationReport report = validationModel.validate(vm.getCompilationUnit());
		System.out.println(describe(validationModel, report));

		Set<String> found = new TreeSet<>();
		for (ValidationIssue<?, ?> issue : report.getAllIssues()) {
			found.add(ownerName(issue.getValidable()) + ":" + issue.getCause().getClass().getSimpleName());
		}
		assertTrue(found.toString(), found.contains("catchInternalError:CatchTypeMustBeCatchable"));
		assertTrue(found.toString(), found.contains("catchCancellation:CatchTypeMustBeCatchable"));
		assertTrue(found.toString(), found.contains("catchString:CatchTypeMustBeCatchable"));
		assertTrue(found.toString(), found.contains("throwString:ThrownValueMustBeAnException"));
		assertTrue(found.toString(), found.contains("unreachableClause:CatchClauseMustBeReachable"));
		assertTrue(found.toString(), found.contains("redeclaredVariable:LocalVariableMustNotRedeclareAVisibleLocal"));
		assertTrue(found.toString(), found.stream().anyMatch(s -> s.startsWith("variableOutsideItsBlock:")));
		assertTrue(found.toString(), found.contains("BadException:ExceptionHierarchyMustBeConsistent"));
		assertTrue(found.toString(), found.contains("BadConcept:ExceptionHierarchyMustBeConsistent"));
		assertFalse(found.toString(), found.contains("Failure:ExceptionHierarchyMustBeConsistent"));

		// Checked exceptions (CORE-F-11 phase 7)
		assertTrue(found.toString(), found.contains("unhandledThrow:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("unhandledCall:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("catchesTheWrongType:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("makeRisky:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("fireUnhandled:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("computed:ExpressionMayNotRaiseCheckedExceptions"));
		assertTrue(found.toString(), found.contains("throwsNotAnException:ThrowsClauseMustBeValid"));
		assertTrue(found.toString(), found.contains("step:ThrowsClauseMustBeValid"));
		assertFalse(found.toString(), found.contains("declaresFailure:CheckedExceptionsMustBeCaughtOrDeclared"));
		// A built-in error type in throws covers no FML exception; an unresolved type in throws is reported as such
		assertTrue(found.toString(), found.contains("declaresOnlyFMLError:CheckedExceptionsMustBeCaughtOrDeclared"));
		assertTrue(found.toString(), found.contains("throwsAnUnresolvedType:ThrowsClauseMustBeValid"));
	}

	private static String ownerName(Object validable) {
		if (validable instanceof FMLControlGraph && ((FMLControlGraph) validable).getRootOwner() instanceof FlexoBehaviour) {
			return ((FlexoBehaviour) ((FMLControlGraph) validable).getRootOwner()).getName();
		}
		if (validable instanceof FlexoBehaviour) {
			return ((FlexoBehaviour) validable).getName();
		}
		if (validable instanceof FlexoConcept) {
			return ((FlexoConcept) validable).getName();
		}
		if (validable instanceof FlexoProperty) {
			return ((FlexoProperty<?>) validable).getName();
		}
		return String.valueOf(validable);
	}

	private static String describe(ValidationModel validationModel, ValidationReport report) {
		StringBuilder sb = new StringBuilder();
		for (ValidationIssue<?, ?> issue : report.getAllIssues()) {
			sb.append(issue.getClass().getSimpleName()).append(' ').append(ownerName(issue.getValidable())).append(' ')
					.append(issue.getCause().getClass().getSimpleName()).append(": ").append(validationModel.localizedIssueMessage(issue))
					.append('\n');
		}
		return sb.toString();
	}

	private static TryStatement findTry(FlexoBehaviour behaviour) {
		TryStatement[] found = new TryStatement[1];
		behaviour.getControlGraph().accept(new FMLControlGraphVisitor() {
			@Override
			public void visit(FMLControlGraph controlGraph) {
				if (found[0] == null && controlGraph instanceof TryStatement) {
					found[0] = (TryStatement) controlGraph;
				}
			}
		});
		return found[0];
	}
}
