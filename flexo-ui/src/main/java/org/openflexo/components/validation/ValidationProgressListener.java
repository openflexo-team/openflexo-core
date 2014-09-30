/*
 * (c) Copyright 2014 Openflexo
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

import org.openflexo.components.ProgressWindow;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.model.validation.ValidationRule;

/**
 * Listen to validation process and control ProgressWindow
 * 
 * @author sguerin
 * 
 */
public class ValidationProgressListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ValidationReport.VALIDATION_START)) {
			ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("validating") + " " + evt.getOldValue().toString(),
					(Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_OBJECT)) {
			ProgressWindow.setProgressInstance(FlexoLocalization.localizedForKey("validating") + " " + evt.getNewValue().toString());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATION_END)) {
			ProgressWindow.hideProgressWindow();
		} else if (evt.getPropertyName().equals(ValidationReport.OBJECT_VALIDATION_START)) {
			ProgressWindow.resetSecondaryProgressInstance((Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals(ValidationReport.VALIDATE_WITH_RULE)) {
			ProgressWindow.setSecondaryProgressInstance(((ValidationRule) evt.getNewValue()).getRuleName());
		}
	}

}
