package org.openflexo.components.wizard;

import java.awt.Image;

import org.openflexo.foundation.fml.annotations.FIBPanel;
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