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

package org.openflexo.components.wizard;

import java.awt.Image;

import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;

@FIBPanel("FibWizard/TestFlexoWizardStep1.fib")
public class WizardStep1 extends WizardStep {

	private String firstName;
	private String lastName;

	@Override
	public String getTitle() {
		return "Wizard test, Step1";
	}

	@Override
	public Image getPageImage() {
		return IconLibrary.OPENFLEXO_NOTEXT_64.getImage();
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(firstName)) {
			setIssueMessage("firstName is not set", IssueMessageType.ERROR);
			return false;
		}
		if (StringUtils.isEmpty(lastName)) {
			setIssueMessage("lastName is not set", IssueMessageType.ERROR);
			return false;
		}
		if (firstName.equals(lastName)) {
			setIssueMessage("lastName equals firstName", IssueMessageType.WARNING);
		}
		return true;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		if (!firstName.equals(this.firstName)) {
			String oldValue = this.firstName;
			this.firstName = firstName;
			getPropertyChangeSupport().firePropertyChange("firstName", oldValue, firstName);
			checkValidity();
		}
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		if (!lastName.equals(this.lastName)) {
			String oldValue = this.lastName;
			this.lastName = lastName;
			getPropertyChangeSupport().firePropertyChange("lastName", oldValue, lastName);
			checkValidity();
		}
	}

}
