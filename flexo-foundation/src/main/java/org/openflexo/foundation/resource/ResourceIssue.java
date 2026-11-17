/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.resource;

import java.util.Objects;

import org.openflexo.localization.FlexoLocalization;

/**
 * A problem of a {@link FlexoResource} that is detected at the resource layer (from the metadata of the resource, with no need to load
 * it): a missing VirtualModel, a missing dependency, a missing technology adapter.<br>
 * A resource carrying an issue of severity {@link Severity#ERROR} is displayed as in error. Issues are identified by their
 * {@link Kind} and their subject (the URI or the class name that cannot be found), and disappear when the cause is fixed.
 * 
 * @author sylvain
 */
public class ResourceIssue {

	public enum Severity {
		ERROR, WARNING
	}

	public enum Kind {
		/** The VirtualModel of a VirtualModelInstance cannot be found */
		MISSING_VIRTUAL_MODEL("resource_issue_missing_virtual_model_($0)", "resource_($resource.name)_virtual_model_($subject)_not_found"),
		/** A resource this resource depends on cannot be found */
		MISSING_DEPENDENCY("resource_issue_missing_dependency_($0)", "resource_($resource.name)_required_resource_($subject)_not_found"),
		/** The technology adapter of a model slot used by this resource cannot be found */
		MISSING_TECHNOLOGY_ADAPTER("resource_issue_missing_technology_adapter_for_($0)", "resource_($resource.name)_technology_adapter_for_($subject)_not_found");

		private final String localizationKey;
		private final String validationKey;

		Kind(String localizationKey, String validationKey) {
			this.localizationKey = localizationKey;
			this.validationKey = validationKey;
		}

		/** Key of the message in a validation report (bindings on the validation error: resource and subject) */
		public String getValidationKey() {
			return validationKey;
		}

		/** Key of the message, in the dictionary of the platform (one parameter: the subject) */
		public String getLocalizationKey() {
			return localizationKey;
		}
	}

	private final Severity severity;
	private final Kind kind;
	private final String subject;

	public ResourceIssue(Severity severity, Kind kind, String subject) {
		this.severity = severity;
		this.kind = kind;
		this.subject = subject;
	}

	public Severity getSeverity() {
		return severity;
	}

	public Kind getKind() {
		return kind;
	}

	/** The URI or class name that cannot be found */
	public String getSubject() {
		return subject;
	}

	public boolean isError() {
		return severity == Severity.ERROR;
	}

	/** Localized message of this issue, in the language of the platform */
	public String getMessage(FlexoResource<?> resource) {
		// The main localizer, not the one of the resource: a technology-specific resource has no technology adapter yet when it is discovered
		return FlexoLocalization.getMainLocalizer().localizedForKeyWithParams(kind.getLocalizationKey(), subject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ResourceIssue)) {
			return false;
		}
		ResourceIssue other = (ResourceIssue) obj;
		return kind == other.kind && severity == other.severity && Objects.equals(subject, other.subject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, severity, subject);
	}

	@Override
	public String toString() {
		return severity + " " + kind + " " + subject;
	}
}
