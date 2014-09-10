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
		return "FlexoWizard test, Step1";
	}

	@Override
	public Image getPageImage() {
		return IconLibrary.OPENFLEXO_NOTEXT_64.getImage();
	}

	@Override
	public boolean isValid() {
		return StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(lastName);
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}