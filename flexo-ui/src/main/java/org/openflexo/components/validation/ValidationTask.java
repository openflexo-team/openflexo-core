/*
 * (c) Copyright 2014-2015 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.components.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.validation.Validable;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to perform a validation on a given object with a {@link ValidationModel}<br>
 * Progress monitoring is performed while listening to {@link ValidationModel}
 * 
 * @author sylvain
 *
 */
public class ValidationTask extends FlexoApplicationTask implements PropertyChangeListener {

	private final ValidationModel validationModel;
	private final Validable objectToValidate;
	private final FlexoFIBValidationController controller;

	public ValidationTask(ValidationModel validationModel, Validable objectToValidate, FlexoFIBValidationController controller) {
		super(FlexoLocalization.localizedForKey("validating") + " " + objectToValidate, null);
		this.validationModel = validationModel;
		this.objectToValidate = objectToValidate;
		this.controller = controller;
	}

	@Override
	public void performTask() throws InterruptedException {

		validationModel.getPropertyChangeSupport().addPropertyChangeListener(this);

		ValidationReport report = validationModel.validate(objectToValidate);

		validationModel.getPropertyChangeSupport().removePropertyChangeListener(this);

		Progress.progress(FlexoLocalization.localizedForKey("displaying_validation_report"));
		controller.setDataObject(report);
	}

	@Override
	protected synchronized void stopExecution() {
		super.stopExecution();
		validationModel.getPropertyChangeSupport().removePropertyChangeListener(this);
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ValidationReport.VALIDATION_START)) {
			Progress.setExpectedProgressSteps((Integer) evt.getNewValue());
			// ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("validating") + " " + evt.getOldValue().toString(),
			// (Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_OBJECT)) {
			// System.out.println(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
			Progress.progress(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_END)) {
		} else if (evt.getPropertyName().equals(ValidationReport.OBJECT_VALIDATION_START)) {
			// System.out.println(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
			// Progress.progress(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATE_WITH_RULE)) {
		}
	}

}