/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.foundation.FlexoException;
import org.openflexo.localization.FlexoLocalization;

/**
 * Thrown when attempting to create or rename an ProjectExternalRepository whose name is duplicated
 * 
 * @author gpolet
 * 
 */
@SuppressWarnings("serial")
public class DuplicateExternalRepositoryNameException extends FlexoException {

	private final ProjectExternalRepository repository;
	private final String name;

	/**
	 * @param folder
	 * @param name
	 */
	public DuplicateExternalRepositoryNameException(ProjectExternalRepository repository, String name) {
		this.repository = repository;// may be null
		this.name = name;
	}

	public ProjectExternalRepository getRepository() {
		return repository;
	}

	public String getName() {
		return name;
	}

	/**
	 * Overrides getLocalizedMessage
	 * 
	 * @see org.openflexo.foundation.FlexoException#getLocalizedMessage()
	 */
	@Override
	public String getLocalizedMessage() {
		return FlexoLocalization.localizedForKey("duplicate_external_repository_name");
	}
}
