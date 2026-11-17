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

package org.openflexo.foundation.validation;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProject.AllResourcesMustBeFreeOfIssues;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceIssue;
import org.openflexo.pamela.validation.ValidationError;

/**
 * The validation error reporting a {@link ResourceIssue} of a {@link FlexoResource}: the resource carries the problem, which is detected at
 * the resource layer without loading it, and is the object to select when the error is selected in the validation window.
 * 
 * @author sylvain
 */
public class ResourceIssueError extends ValidationError<AllResourcesMustBeFreeOfIssues, FlexoProject> {

	private final FlexoResource<?> resource;
	private final ResourceIssue issue;

	public ResourceIssueError(AllResourcesMustBeFreeOfIssues rule, FlexoProject<?> project, FlexoResource<?> resource, ResourceIssue issue) {
		super(rule, project, issue.getKind().getValidationKey());
		this.resource = resource;
		this.issue = issue;
	}

	/** The resource that carries the issue: what is selected with this error */
	public FlexoResource<?> getResource() {
		return resource;
	}

	public ResourceIssue getResourceIssue() {
		return issue;
	}

	/** The URI or class name that cannot be found */
	public String getSubject() {
		return issue.getSubject();
	}
}
