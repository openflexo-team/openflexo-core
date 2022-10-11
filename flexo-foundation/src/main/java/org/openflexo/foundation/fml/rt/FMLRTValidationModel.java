/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.java.util.JavaBindingEvaluator;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance.ConstraintsShouldNotBeViolated.ViolatedConstraint;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * This is the ValidationModel for FML@runtime model (VirtualModelInstance, FlexoConceptInstance, etc...)
 * 
 * @author sylvain
 * 
 */
public class FMLRTValidationModel extends FlexoValidationModel {

	private static Resource fibValidationLocalizedDelegate = ResourceLocator.locateResource("FlexoLocalization/FMLRTValidation");
	private static LocalizedDelegate VALIDATION_LOCALIZATION = new LocalizedDelegateImpl(fibValidationLocalizedDelegate, null, true, true);

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as
	 * VirtualModelInstance parts, and return a newly created ModelContext dedicated to FMLModel@runtime validation
	 * 
	 * @param taService
	 * @return
	 * @throws ModelDefinitionException
	 */
	private static ModelContext computeModelContext(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = (taService != null
				? FMLRTVirtualModelInstanceModelFactory.allClassesForModelContext(FMLRTVirtualModelInstance.class, taService)
				: new ArrayList<>());
		return ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes.size()]));
	}

	public FMLRTValidationModel(TechnologyAdapterService taService) throws ModelDefinitionException {
		super(computeModelContext(taService), VALIDATION_LOCALIZATION);
	}

	/**
	 * Return a boolean indicating if validation of supplied object must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	@Override
	protected boolean shouldNotifyValidation(Validable next) {
		return true;
	}

	/**
	 * Overrides fixAutomaticallyIfOneFixProposal
	 * 
	 * @see org.openflexo.pamela.validation.ValidationModel#fixAutomaticallyIfOneFixProposal()
	 */
	@Override
	public boolean fixAutomaticallyIfOneFixProposal() {
		return false;
	}

	@Override
	public ValidationReport validate(Validable object) throws InterruptedException {
		if (object instanceof VirtualModelInstance)
			return new FMLRTValidationReport(this, (VirtualModelInstance) object);
		if (object != null)
			return super.validate(object);
		return null;
	}

	@Override
	public String localizedIssueMessage(ValidationIssue<?, ?> issue) {
		if (issue instanceof ViolatedConstraint) {
			// Special case for that, we use the FML dictionary
			ViolatedConstraint constraintIssue = (ViolatedConstraint) issue;
			LocalizedDelegate locales = ((ViolatedConstraint) issue).getConstraint().getDeclaringCompilationUnit().getLocales();
			String localized = locales.localizedForKeyAndLanguage(constraintIssue.getMessage(), FlexoLocalization.getCurrentLanguage(),
					true);
			String asBindingExpression = asBindingExpression(localized);
			try {
				return (String) JavaBindingEvaluator.evaluateBinding(asBindingExpression, constraintIssue, constraintIssue.getClass());
			} catch (Exception e) {
				e.printStackTrace();
				return localized;
			}
		}
		return super.localizedIssueMessage(issue);
	}

}
