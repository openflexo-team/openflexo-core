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

package org.openflexo.foundation.fml;

import java.util.Collection;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLObject.BindingIsRequiredAndMustBeValid;
import org.openflexo.foundation.fml.editionaction.AbstractAssignationAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.model.validation.InformationIssue;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.model.validation.ValidationWarning;

/**
 * This is the {@link ValidationReport} for a {@link VirtualModel}
 * 
 * We maintain here a collection of {@link ValidationReport} dedicated to each object found in the {@link VirtualModel}
 * <ul>
 * <li>for each {@link FlexoConcept} found
 * <li>
 * <li>for each {@link FlexoProperty} found
 * <li>
 * <li>for each {@link FlexoBehaviour} found
 * <li>
 * <li>for each {@link FlexoConceptInspector} found
 * <li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public class FMLValidationReport extends ValidationReport {

	private static final Logger logger = Logger.getLogger(FMLValidationReport.class.getPackage().getName());

	private VirtualModel virtualModel;

	public FMLValidationReport(ValidationModel validationModel, VirtualModel virtualModel) {
		super(validationModel, virtualModel);
		this.virtualModel = virtualModel;
	}

	public boolean hasErrors(FMLObject object) {
		Collection<ValidationError<?, ? super FMLObject>> errors = getErrors(object);
		if (errors.size() == 0 && object instanceof AbstractAssignationAction) {
			errors = getErrors(((AbstractAssignationAction<?>) object).getAssignableAction());
			return errors.size() > 0;
		}
		return errors.size() > 0;
	}

	public boolean hasWarnings(FMLObject object) {
		Collection<ValidationWarning<?, ? super FMLObject>> warnings = getWarnings(object);
		if (warnings.size() == 0 && object instanceof AbstractAssignationAction) {
			warnings = getWarnings(((AbstractAssignationAction<?>) object).getAssignableAction());
			return warnings.size() > 0;
		}
		return warnings.size() > 0;
	}

	public boolean hasInfoIssues(FMLObject object) {
		Collection<InformationIssue<?, ? super FMLObject>> infos = getInformationIssues(object);
		if (infos.size() == 0 && object instanceof AbstractAssignationAction) {
			infos = getInformationIssues(((AbstractAssignationAction<?>) object).getAssignableAction());
			return infos.size() > 0;
		}
		return infos.size() > 0;
	}

	public Collection<ValidationError<?, ? super FMLObject>> getErrors(FMLObject object) {
		return errorIssuesRegarding(object);
	}

	public Collection<ValidationWarning<?, ? super FMLObject>> getWarnings(FMLObject object) {
		return warningIssuesRegarding(object);
	}

	public Collection<InformationIssue<?, ? super FMLObject>> getInformationIssues(FMLObject object) {
		return infoIssuesRegarding(object);
	}

	// private long startTime;
	// private long intermediateTime;
	// private long endTime;

	private static <C extends FMLObject> void reanalyzeBinding(ValidationIssue<? extends BindingIsRequiredAndMustBeValid<C>, C> issue) {
		DataBinding<?> db = issue.getCause().getBinding(issue.getValidable());
		db.markedAsToBeReanalized();
	}

	@Override
	public void revalidate() throws InterruptedException {

		for (ValidationIssue issue : getAllIssues()) {
			if (issue.getCause() instanceof BindingIsRequiredAndMustBeValid) {
				reanalyzeBinding(issue);
			}
		}

		super.revalidate();
	}

}
