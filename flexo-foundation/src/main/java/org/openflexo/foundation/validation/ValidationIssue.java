/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.validation;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.utils.FlexoListModel;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.localization.FlexoLocalization;

/**
 * Represents a validation issue embedded in a validation report
 * 
 * @author sguerin
 * 
 */
public abstract class ValidationIssue<R extends ValidationRule<R, V>, V extends Validable> extends FlexoListModel implements FlexoObserver {

	public static final String DELETED_PROPERTY = "deleted";

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ValidationIssue.class.getPackage().getName());

	private V object;
	private String message;
	private String detailedMessage;
	private String localizedMessage;
	private ValidationReport validationReport;
	private R cause;
	private boolean isLocalized;

	public ValidationIssue(V anObject, String aLocalizedMessage) {
		this(anObject, aLocalizedMessage, true);
	}

	public ValidationIssue(V anObject, String aMessage, String aDetailedMessage) {
		this(anObject, aMessage, aDetailedMessage, true);
	}

	public ValidationIssue(V anObject, String aMessage, boolean isLocalized) {
		this(anObject, aMessage, null, isLocalized);
	}

	public ValidationIssue(V anObject, String aMessage, String aDetailedMessage, boolean isLocalized) {
		super();
		object = anObject;
		message = aMessage;
		detailedMessage = aDetailedMessage;
		this.isLocalized = isLocalized;
		if (!isLocalized) {
			localizedMessage = aMessage;
		}
		if (object instanceof FlexoObservable) {
			((FlexoObservable) object).addObserver(this);
		}
	}

	public FlexoProject getProject() {
		if (object != null && object instanceof FlexoProjectObject) {
			return ((FlexoProjectObject) object).getProject();
		}
		return null;
	}

	public String getMessage() {
		return message;
	}

	public String getDetailedMessage() {
		return detailedMessage;
	}

	public String getLocalizedMessage() {
		if (localizedMessage == null && message != null && isLocalized) {
			localizedMessage = FlexoLocalization.localizedForKeyWithParams(message, this);
		}
		return localizedMessage;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public V getObject() {
		return object;
	}

	// TODO : Check if this is ok => generalized to fix a bug in selection of ViewPointObjects in Viewpoint validation tool
	public FlexoObject getSelectableObject() {
		if (object instanceof FlexoObject || object instanceof ViewPointObject) {
			return (FlexoObject) object;
		}
		return null;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public Object getElementAt(int index) {
		return null;
	}

	public void setValidationReport(ValidationReport report) {
		validationReport = report;
	}

	public ValidationReport getValidationReport() {
		return validationReport;
	}

	private String _typeName;

	public String getTypeName() {
		if (_typeName == null) {
			StringTokenizer st = new StringTokenizer(getObject().getClass().getName(), ".");
			while (st.hasMoreTokens()) {
				_typeName = st.nextToken();
			}
		}
		return _typeName;
	}

	@Override
	public abstract String toString();

	public void setCause(R rule) {
		cause = rule;
	}

	public R getCause() {
		return cause;
	}

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {
		if (observable instanceof FlexoObject) {
			if (((FlexoObject) observable).isDeleted()) {
				delete();
			}
		}

	}

	public void delete() {
		if (object instanceof FlexoObservable) {
			((FlexoObservable) object).deleteObserver(this);
		}
		validationReport.removeFromValidationIssues(this);
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange(DELETED_PROPERTY, this, null);
		}
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public void setLocalized(boolean localized) {
		isLocalized = localized;
		if (!localized) {
			localizedMessage = null;
		} else {
			localizedMessage = message;
		}
	}
}
