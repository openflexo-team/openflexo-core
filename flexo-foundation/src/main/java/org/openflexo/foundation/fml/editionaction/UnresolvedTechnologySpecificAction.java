/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * 
 * Represents an {@link EditionAction} which address a specific technology referenced by a {@link ModelSlot} class<br>
 * 
 * @author sylvain
 *
 * @param <MS>
 *            Type of model slot which contractualize access to a given technology resource on which this action applies
 * @param <T>
 *            Type of assigned value
 */
@ModelEntity
@ImplementationClass(UnresolvedTechnologySpecificAction.UnresolvedTechnologySpecificActionImpl.class)
public abstract interface UnresolvedTechnologySpecificAction extends TechnologySpecificAction<ModelSlot<?>, Object> {

	@PropertyIdentifier(type = String.class)
	public static final String TA_ID_KEY = "TAId";
	@PropertyIdentifier(type = String.class)
	public static final String EDITION_ACTION_NAME_KEY = "editionActionName";

	@Getter(value = TA_ID_KEY)
	public String getTAId();

	@Setter(TA_ID_KEY)
	public void setTAId(String TAId);

	@Getter(value = EDITION_ACTION_NAME_KEY)
	public String getEditionActionName();

	@Setter(EDITION_ACTION_NAME_KEY)
	public void setEditionActionName(String editionActionName);

	public UseModelSlotDeclaration getUseModelSlotDeclaration();

	public static abstract class UnresolvedTechnologySpecificActionImpl extends TechnologySpecificActionImpl<ModelSlot<?>, Object>
			implements UnresolvedTechnologySpecificAction {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(UnresolvedTechnologySpecificAction.class.getPackage().getName());

		@Override
		public UseModelSlotDeclaration getUseModelSlotDeclaration() {
			FMLCompilationUnit compilationUnit = getDeclaringCompilationUnit();
			if (compilationUnit != null) {
				for (UseModelSlotDeclaration useModelSlotDeclaration : compilationUnit.getUseDeclarations()) {
					if (getTAId().equals(useModelSlotDeclaration.getAbbrev())) {
						return useModelSlotDeclaration;
					}
				}
			}
			return null;
		}

		@Override
		public Type getAssignableType() {
			return Object.class;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			throw new FMLExecutionException("UnresolvedTechnologySpecificAction");
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getImplementedInterface().getSimpleName() + getParametersStringRepresentation();
		}

	}

	@DefineValidationRule
	public static class EditionActionMustBeResolved
			extends ValidationRule<EditionActionMustBeResolved, UnresolvedTechnologySpecificAction> {

		public EditionActionMustBeResolved() {
			super(UnresolvedTechnologySpecificAction.class, "edition_action_must_be_resolved");
		}

		@Override
		public ValidationIssue<EditionActionMustBeResolved, UnresolvedTechnologySpecificAction> applyValidation(
				UnresolvedTechnologySpecificAction action) {
			if (action.getUseModelSlotDeclaration() == null) {
				return new ValidationError<>(this, action, "cannot_resolve_technology_adapter_($validable.tAId)");
			}
			return new ValidationError<>(this, action, "cannot_resolve_action_($validable.editionActionName)");
		}
	}

}
