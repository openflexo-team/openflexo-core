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

@FIBPanel("FibWizard/TestFlexoWizardStep2.fib")
public class WizardStep2 extends WizardStep {

	private String fullName;
	private String confirmedFullName;

	@Override
	public String getTitle() {
		return "Wizard test, Step2";
	}

	@Override
	public Image getPageImage() {
		return IconLibrary.ENTERPRISE_32_ICON.getImage();
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(confirmedFullName)) {
			setIssueMessage("confirmedFullName is not set", IssueMessageType.ERROR);
			return false;
		}
		if (!fullName.equals(confirmedFullName)) {
			setIssueMessage("confirmedFullName does not match", IssueMessageType.WARNING);
			return false;
		}
		setIssueMessage("Confirmation successfull", IssueMessageType.INFO);
		return true;
	}

	@Override
	protected void prepare(WizardStep previous) {
		if (previous instanceof WizardStep1) {
			WizardStep1 step1 = (WizardStep1) previous;
			setFullName(step1.getFirstName() + " " + step1.getLastName());
			setConfirmedFullName(null);
		}
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		if (!fullName.equals(this.fullName)) {
			String oldValue = this.fullName;
			this.fullName = fullName;
			getPropertyChangeSupport().firePropertyChange("fullName", oldValue, fullName);
			checkValidity();
		}
	}

	public String getConfirmedFullName() {
		return confirmedFullName;
	}

	public void setConfirmedFullName(String confirmedFullName) {
		if (confirmedFullName == null || !confirmedFullName.equals(this.confirmedFullName)) {
			String oldValue = this.confirmedFullName;
			this.confirmedFullName = confirmedFullName;
			getPropertyChangeSupport().firePropertyChange("confirmedFullName", oldValue, confirmedFullName);
			checkValidity();
		}
	}

}
