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
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.icon.IconLibrary;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Abstract generic class used to encodes a wizard in Openflexo GUI environment.<br>
 * 
 * A {@link Wizard} is composed of some {@link WizardStep} which are sequentially executed.
 * 
 * @author guillaume, sylvain
 * 
 */
public abstract class Wizard implements HasPropertyChangeSupport {

	private static final Logger logger = FlexoLogger.getLogger(Wizard.class.getPackage().getName());

	private final List<WizardStep> steps;
	private WizardStep currentStep;

	private final PropertyChangeSupport pcSupport;

	public Wizard() {
		steps = new ArrayList<WizardStep>();
		pcSupport = new PropertyChangeSupport(this);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public void addStep(WizardStep step) {
		if (step == null) {
			return;
		}
		steps.add(step);
		step.setWizard(this);
		if (steps.size() == 1) {
			currentStep = step;
		}
	}

	public void removeStep(WizardStep step) {
		if (step == null) {
			return;
		}
		int index = (currentStep != null ? currentStep.getIndex() - 1 : -1);
		step.setWizard(null);
		steps.remove(step);
		if (step == currentStep && index - 1 < steps.size()) {
			currentStep = steps.get(index - 1);
		}
	}

	public boolean canFinish() {
		for (WizardStep page : steps) {
			if (!page.isValid()) {
				return false;
			}
		}
		if (currentStep != null && currentStep.isTransitionalStep()) {
			return false;
		}
		return true;
	}

	public boolean needsPreviousAndNext() {
		return steps.size() > 1;
	}

	public boolean isPreviousEnabled() {
		if (currentStep == null) {
			return false;
		}
		return getPreviousStep(currentStep) != null;
	}

	public List<WizardStep> getSteps() {
		return steps;
	}

	public WizardStep getCurrentStep() {
		return currentStep;
	}

	public WizardStep getPreviousStep(WizardStep page) {
		if (page.isPreviousEnabled() && steps.indexOf(page) > 0) {
			return steps.get(steps.indexOf(page) - 1);
		} else {
			return null;
		}
	}

	public boolean isNextEnabled() {
		if (currentStep == null) {
			return false;
		}

		if (!currentStep.isValid()) {
			return false;
		}

		if (currentStep.isTransitionalStep()) {
			return true;
		}
		return getNextStep(currentStep) != null;
	}

	public WizardStep getNextStep(WizardStep page) {
		if (page.isNextEnabled() && steps.indexOf(page) > -1 && steps.indexOf(page) < steps.size() - 1) {
			return steps.get(steps.indexOf(page) + 1);
		} else {
			return null;
		}
	}

	public Image getPageImage() {
		if (currentStep == null) {
			return null;
		}
		if (currentStep.getPageImage() != null) {
			return currentStep.getPageImage();
		} else {
			return getDefaultPageImage();
		}
	}

	public abstract String getWizardTitle();

	public Image getDefaultPageImage() {
		return IconLibrary.OPENFLEXO_NOTEXT_64.getImage();
	}

	// public abstract void performFinish();

	// public abstract void performCancel();

	public final void performNext() {
		if (!isNextEnabled()) {
			return;
		}

		if (currentStep.isTransitionalStep()) {
			currentStep.performTransition();
		}

		WizardStep previousStep = currentStep;
		WizardStep nextStep = getNextStep(currentStep);
		nextStep.prepare(previousStep);
		currentStep = nextStep;
		getPropertyChangeSupport().firePropertyChange("currentStep", previousStep, nextStep);
		getPropertyChangeSupport().firePropertyChange("pageImage", null, getPageImage());
		updateStatus();
	}

	public final void performPrevious() {
		if (!isPreviousEnabled()) {
			return;
		}

		WizardStep oldStep = currentStep;
		WizardStep previousStep = getPreviousStep(currentStep);
		currentStep = previousStep;

		if (currentStep.isTransitionalStep()) {
			currentStep.discardTransition();
		}

		getPropertyChangeSupport().firePropertyChange("currentStep", oldStep, previousStep);
		getPropertyChangeSupport().firePropertyChange("pageImage", null, getPageImage());
		updateStatus();
	}

	protected void updateStatus() {
		getPropertyChangeSupport().firePropertyChange("canFinish", !canFinish(), canFinish());
		getPropertyChangeSupport().firePropertyChange("isPreviousEnabled", !isPreviousEnabled(), isPreviousEnabled());
		getPropertyChangeSupport().firePropertyChange("isNextEnabled", !isNextEnabled(), isNextEnabled());
	}

}
