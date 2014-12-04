package org.openflexo.components.wizard;

import java.awt.Image;

import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;

@FIBPanel("Fib/TestFlexoWizardStep1.fib")
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