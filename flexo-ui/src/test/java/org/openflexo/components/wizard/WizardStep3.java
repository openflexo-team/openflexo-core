package org.openflexo.components.wizard;

import java.awt.Image;

import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.icon.IconLibrary;

@FIBPanel("Fib/TestFlexoWizardStep3.fib")
public class WizardStep3 extends WizardStep {

	@Override
	public String getTitle() {
		return "Wizard test, Step3";
	}

	@Override
	public Image getPageImage() {
		return IconLibrary.BUSINESS_PLUS_32_ICON.getImage();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}