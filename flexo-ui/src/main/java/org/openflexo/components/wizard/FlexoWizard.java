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
package org.openflexo.components.wizard;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.logging.FlexoLogger;

/**
 * Abstract generic class used to encodes a wizard in Openflexo GUI environment.<br>
 * 
 * A {@link FlexoWizard} is composed of some {@link WizardStep} which are sequentially executed.
 * 
 * @author guillaume, sylvain
 * 
 */
public abstract class FlexoWizard {

	private static final Logger logger = FlexoLogger.getLogger(FlexoWizard.class.getPackage().getName());

	private final List<WizardStep> steps;

	private WizardStep currentStep;

	public FlexoWizard() {
		steps = new ArrayList<WizardStep>();
	}

	public void addStep(WizardStep step) {
		if (step == null) {
			return;
		}
		steps.add(step);
		if (steps.size() == 1) {
			currentStep = step;
		}
	}

	public boolean canFinish() {
		for (WizardStep page : steps) {
			if (!page.isValid()) {
				return false;
			}
		}
		return true;
	}

	public boolean needsPreviousAndNext() {
		return steps.size() > 1;
	}

	public boolean isPreviousEnabled() {
		return getPreviousStep(currentStep) != null;
	}

	public WizardStep getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(WizardStep currentPage) {
		this.currentStep = currentPage;
	}

	public WizardStep getPreviousStep(WizardStep page) {
		if (page.isPreviousEnabled() && steps.indexOf(page) > 0) {
			return steps.get(steps.indexOf(page) - 1);
		} else {
			return null;
		}
	}

	public boolean isNextEnabled() {
		return getNextPage(currentStep) != null && currentStep.isValid();
	}

	public WizardStep getNextPage(WizardStep page) {
		if (page.isNextEnabled() && steps.indexOf(page) > -1 && steps.indexOf(page) < steps.size() - 1) {
			return steps.get(steps.indexOf(page) + 1);
		} else {
			return null;
		}
	}

	public Image getPageImage() {
		if (currentStep.getPageImage() != null) {
			return currentStep.getPageImage();
		} else {
			return getDefaultPageImage();
		}
	}

	public abstract String getWizardTitle();

	public Image getDefaultPageImage() {
		return null;
	}

	public abstract void performFinish();

	public abstract void performCancel();

}
