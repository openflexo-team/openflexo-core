/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import java.awt.Dimension;
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

	private PropertyChangeSupport pcSupport;

	public Wizard() {
		steps = new ArrayList<WizardStep>();
		pcSupport = new PropertyChangeSupport(this);
	}

	public void delete() {
		for (WizardStep s : steps) {
			s.delete();
		}
		steps.clear();
		pcSupport = null;
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
		}
		else {
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
		}
		else {
			return null;
		}
	}

	public Image getPageImage() {
		if (currentStep == null) {
			return null;
		}
		if (currentStep.getPageImage() != null) {
			return currentStep.getPageImage();
		}
		else {
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

		currentStep.done();

		if (currentStep.isTransitionalStep()) {
			currentStep.performTransition();
		}

		WizardStep previousStep = currentStep;
		WizardStep nextStep = getNextStep(currentStep);
		if (nextStep != null) {
			nextStep.prepare(previousStep);
			currentStep = nextStep;
		}
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

		currentStep.reactivate();

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

	/**
	 * Please override when you want to get a wizard with a given size
	 * 
	 * @return
	 */
	public Dimension getPreferredSize() {
		return null;
	}

	public void cancel() {
		if (getCurrentStep() != null) {
			getCurrentStep().cancelled();
		}
	}

	public void finish() {
		if (canFinish()) {
			getCurrentStep().done();
		}
	}

}
