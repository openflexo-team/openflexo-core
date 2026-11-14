/**
 * 
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.foundation.fml.rt.error;

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;

/**
 * The two points where an FML execution enriches a failure that goes through it (CORE-F-11):
 * <ul>
 * <li>{@link #executeStatement(FMLControlGraph, RunTimeEvaluationContext)}, used by the control graphs to run each of their
 * statements: records the statement in the FML stack of the failure;</li>
 * <li>{@link #executeBehaviourBody(FMLControlGraph, RunTimeEvaluationContext, FlexoBehaviour, FlexoConceptInstance)}, used wherever the
 * body of a behaviour runs, and {@link #executePropertyBody(FMLControlGraph, RunTimeEvaluationContext, String, FlexoConceptInstance)}
 * for a getter or a setter: take a {@code return} as the returned value and close the frame of the body.</li>
 * </ul>
 * An unchecked exception crossing a statement is converted once ({@link #convertUnchecked(RuntimeException)}) into an
 * {@link FMLInternalError}, which carries the FML stack. Nothing here logs: reporting is done once,
 * by the editor that runs the outermost action.
 */
public class FMLExecution {

	private FMLExecution() {
	}

	/**
	 * Execute supplied statement in supplied context, recording it in the FML stack of a failure going through it
	 */
	public static Object executeStatement(FMLControlGraph statement, RunTimeEvaluationContext evaluationContext)
			throws ReturnException, FMLExecutionException {
		try {
			return statement.execute(evaluationContext);
		} catch (FMLExecutionException e) {
			e.getFMLStack().recordStatement(statement);
			throw e;
		} catch (FMLInternalError e) {
			e.getFMLStack().recordStatement(statement);
			throw e;
		} catch (RuntimeException e) {
			try {
				throw convertUnchecked(e);
			} catch (FMLExecutionException | FMLInternalError converted) {
				((FMLExecutionFailure) converted).getFMLStack().recordStatement(statement);
				throw converted;
			}
		}
	}

	/**
	 * Convert supplied unchecked exception, crossing an FML execution, into an {@link FMLInternalError} (itself when it already is one), for
	 * the caller to throw: {@code throw FMLExecution.convertUnchecked(e);}. It stays unchecked, so it propagates as it did before.
	 * <p>
	 * Declared to throw {@link FMLExecutionException}: phase 3 of CORE-F-11 will throw from here the checked {@link FMLError} that a foreseen
	 * one stands for (an {@link FMLHostError} for a failure of a Java accessor called from FML, an {@link FMLResolutionError} for a property
	 * that a Java object does not have). Not before: the many {@code catch (FlexoException)} that still turn a failure into null would
	 * swallow it, where the unchecked exception used to reach the caller.
	 */
	public static FMLInternalError convertUnchecked(RuntimeException e) throws FMLExecutionException {
		if (e instanceof FMLInternalError) {
			return (FMLInternalError) e;
		}
		return new FMLInternalError(e);
	}

	/**
	 * Execute supplied body of supplied behaviour, run on supplied instance
	 * 
	 * @return the value given by a {@code return} statement, null when the body ends without one
	 */
	public static Object executeBehaviourBody(FMLControlGraph body, RunTimeEvaluationContext evaluationContext, FlexoBehaviour behaviour,
			FlexoConceptInstance instance) throws FMLExecutionException {
		if (body == null) {
			return null;
		}
		try {
			executeStatement(body, evaluationContext);
			return null;
		} catch (ReturnException e) {
			return e.getReturnedValue();
		} catch (FMLExecutionException e) {
			e.getFMLStack().pushBehaviourFrame(behaviour, instance);
			throw e;
		} catch (FMLInternalError e) {
			e.getFMLStack().pushBehaviourFrame(behaviour, instance);
			throw e;
		}
	}

	/**
	 * Execute supplied body which is not a behaviour (the getter or the setter of a property, named by supplied text), run on supplied
	 * instance
	 * 
	 * @return the value given by a {@code return} statement, null when the body ends without one
	 */
	public static Object executePropertyBody(FMLControlGraph body, RunTimeEvaluationContext evaluationContext, String bodyName,
			FlexoConceptInstance instance) throws FMLExecutionException {
		if (body == null) {
			return null;
		}
		String conceptName = instance != null && instance.getFlexoConcept() != null ? instance.getFlexoConcept().getName() : null;
		try {
			executeStatement(body, evaluationContext);
			return null;
		} catch (ReturnException e) {
			return e.getReturnedValue();
		} catch (FMLExecutionException e) {
			e.getFMLStack().pushFrame(bodyName, conceptName, instance);
			throw e;
		} catch (FMLInternalError e) {
			e.getFMLStack().pushFrame(bodyName, conceptName, instance);
			throw e;
		}
	}
}
