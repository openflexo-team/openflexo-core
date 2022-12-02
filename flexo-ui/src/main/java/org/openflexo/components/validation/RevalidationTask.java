/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.components.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to perform a validation on a given object with a {@link ValidationModel}<br>
 * Progress monitoring is performed while listening to {@link ValidationModel}
 * 
 * @author sylvain
 *
 */
public class RevalidationTask extends FlexoApplicationTask implements PropertyChangeListener {

	private final ValidationReport validationReport;

	public RevalidationTask(ValidationReport validationReport) {
		super("RevalidateTask",
				getLocales(validationReport.getValidationModel()).localizedForKey("revalidating") + " " + validationReport.getRootObject(),
				null);
		this.validationReport = validationReport;
	}

	private static LocalizedDelegate getLocales(ValidationModel validationModel) {
		if (validationModel instanceof FlexoValidationModel) {
			return ((FlexoValidationModel) validationModel).getLocales();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	@Override
	public LocalizedDelegate getLocales() {
		return getLocales(validationReport.getValidationModel());
	}

	@Override
	public void performTask() throws InterruptedException {

		validationReport.getValidationModel().getPropertyChangeSupport().addPropertyChangeListener(this);

		validationReport.revalidate();

		validationReport.getValidationModel().getPropertyChangeSupport().removePropertyChangeListener(this);

	}

	@Override
	protected synchronized void stopExecution() {
		super.stopExecution();
		validationReport.getValidationModel().getPropertyChangeSupport().removePropertyChangeListener(this);
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ValidationReport.VALIDATION_START)) {
			Progress.setExpectedProgressSteps(((Number) evt.getNewValue()).intValue());
			// ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("validating") + " " + evt.getOldValue().toString(),
			// (Integer) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_OBJECT)) {
			// System.out.println(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
			Progress.progress(getLocales().localizedForKey("validating") + " " + evt.getNewValue().toString());
		}
		else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_END)) {
		}
		else if (evt.getPropertyName().equals(ValidationReport.OBJECT_VALIDATION_START)) {
			// System.out.println(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
			// Progress.progress(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
		}
		else if (evt.getPropertyName().equals(ValidationReport.VALIDATE_WITH_RULE)) {
		}
	}

}
