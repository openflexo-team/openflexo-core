/*
 * (c) Copyright 2010-2013 AgileBirds
 * (c) Copyright 2014 Openflexo
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
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;

/**
 * Encodes a step of a {@link Wizard}.<br>
 * This class is generally enriched with configuration data<br>
 * isValid() method should return true if and only if the configuration data is valid regarding the purpose of the wizard.<br>
 * Assuming validity of this step causes the "next" and/or "finish" button of the wizard to be enabled.
 * 
 * @author guillaume, sylvain
 * 
 */
public abstract class WizardStep implements HasPropertyChangeSupport {

	private static final Logger logger = FlexoLogger.getLogger(WizardStep.class.getPackage().getName());

	private Wizard wizard;

	private Boolean lastValidityStatus = null;
	private IssueMessageType lastNotifiedValidity = null;
	private String issueMessage;
	private IssueMessageType issueMessageType = null;

	private final PropertyChangeSupport pcSupport;

	protected WizardStep() {
		pcSupport = new PropertyChangeSupport(this);
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	public int getIndex() {
		return getWizard().getSteps().indexOf(this) + 1;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	/**
	 * The title of this page.
	 * 
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * Return the current wizard that is using this page
	 * 
	 * @param wizard
	 */
	protected Wizard getWizard() {
		return wizard;
	}

	/**
	 * Sets the current wizard that is using this page
	 * 
	 * @param wizard
	 */
	protected void setWizard(Wizard wizard) {
		this.wizard = wizard;
	}

	/**
	 * The next page of this wizard page. Can be null.
	 * 
	 * @return
	 */
	public WizardStep getNextStep() {
		return wizard.getNextStep(this);
	}

	/**
	 * The previous page of this wizard page. Can be null.
	 * 
	 * @return
	 */
	public WizardStep getPreviousStep() {
		return wizard.getPreviousStep(this);
	}

	/**
	 * Called just before to be displayed.<br>
	 * This is a hook that might be overriden
	 * 
	 * @param previous
	 */
	protected void prepare(WizardStep previous) {
	}

	public Image getPageImage() {
		return null;
	}

	public abstract boolean isValid();

	/**
	 * Return true if this step is a transitional step<br>
	 * This means that some other steps might be added/removed just AFTER this step is validated<br>
	 * Please override this method when required. Note that default value return false.
	 * 
	 * @return
	 */
	public boolean isTransitionalStep() {
		return false;
	}

	/**
	 * Hook used when this step is a transitional step<br>
	 * This means that some other steps might be added/removed just AFTER this step is validated<br>
	 * Please override this method when required (when declared as transitional step). Note that default value does nothing.
	 * 
	 */
	public void performTransition() {
	}

	/**
	 * Hook used when this step is a transitional step<br>
	 * This means that some other steps might be added/removed just AFTER this step is validated<br>
	 * Please override this method when required (when declared as transitional step). Note that default value does nothing.
	 * 
	 */
	public void discardTransition() {
	}

	public boolean isNextEnabled() {
		return true;
	}

	// public boolean isFinishEnabled();

	public boolean isPreviousEnabled() {
		return true;
	}

	private Resource fibComponentResource = null;

	public Resource getFibComponentResource() {
		if (fibComponentResource == null) {
			Class<?> current = getClass();
			while (fibComponentResource == null && current != null) {
				if (current.getAnnotation(FIBPanel.class) != null) {
					// System.out.println("Found annotation " + getClass().getAnnotation(FIBPanel.class));
					String fibPanelName = current.getAnnotation(FIBPanel.class).value();
					fibComponentResource = ResourceLocator.locateResource(fibPanelName);
				}
				current = current.getSuperclass();
			}
		}
		return fibComponentResource;
	}

	protected void checkValidity() {

		// System.out.println("-------------- checkValidity called");
		// System.out.println("message=" + getIssueMessage());
		// System.out.println("messageType=" + getIssueMessageType());
		// System.out.println("-------------- on s'interroge a nouveau");

		// We we call isValid() by setting issueMessageChange to false
		// If no call to either setIssueMessage() or setIssueMessageType() is fired, then we consider that no message is to be displayed
		issueMessageChangeRequested = false;
		lastValidityStatus = isValid();
		if (!issueMessageChangeRequested) {
			setIssueMessage(null, null);
		}
		IssueMessageType validity = getIssueMessageType();

		// System.out.println("issueMessageChangeRequested=" + issueMessageChangeRequested);
		// System.out.println("lastValidityStatus=" + lastValidityStatus);
		// System.out.println("validity=" + validity);

		if (lastNotifiedValidity == null || (!lastNotifiedValidity.equals(validity))) {
			getPropertyChangeSupport().firePropertyChange("isValid", !lastValidityStatus, (boolean) lastValidityStatus);
			getPropertyChangeSupport().firePropertyChange("messageTypeIsToBeDisplayed", !messageTypeIsToBeDisplayed(),
					messageTypeIsToBeDisplayed());
			getPropertyChangeSupport().firePropertyChange("issueMessage", null, getIssueMessage());
			getPropertyChangeSupport().firePropertyChange("issueMessageType", null, getIssueMessageType());
			getPropertyChangeSupport().firePropertyChange("issueMessageIcon", null, getIssueMessageIcon());
			wizard.updateStatus();
		} else {
			// Nothing to do
		}
		lastNotifiedValidity = validity;
	}

	private boolean issueMessageChangeRequested = false;

	public String getIssueMessage() {
		return issueMessage;
	}

	private void setIssueMessage(String issueMessage) {
		if ((issueMessage == null && this.issueMessage != null) || (issueMessage != null && !issueMessage.equals(this.issueMessage))) {
			String oldValue = this.issueMessage;
			this.issueMessage = issueMessage;
			getPropertyChangeSupport().firePropertyChange("issueMessage", oldValue, issueMessage);
		}
	}

	public void setIssueMessage(String issueMessage, IssueMessageType messageType) {
		issueMessageChangeRequested = true;
		setIssueMessageType(messageType);
		setIssueMessage(issueMessage);
	}

	public enum IssueMessageType {
		ERROR, WARNING, INFO
	}

	public IssueMessageType getIssueMessageType() {
		return issueMessageType;
	}

	private void setIssueMessageType(IssueMessageType issueMessageType) {
		if (issueMessageType != this.issueMessageType) {
			IssueMessageType oldValue = this.issueMessageType;
			this.issueMessageType = issueMessageType;
			getPropertyChangeSupport().firePropertyChange("issueMessageType", oldValue, issueMessageType);
			getPropertyChangeSupport().firePropertyChange("issueMessageIcon", null, getIssueMessageIcon());
		}
	}

	public boolean messageTypeIsToBeDisplayed() {
		// return StringUtils.isNotEmpty(getIssueMessage());
		/*return (!isValid() && StringUtils.isNotEmpty(getIssueMessage()))
				|| (isValid() && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.INFO)
				|| (isValid() && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.WARNING);*/
		if (lastValidityStatus == null) {
			lastValidityStatus = isValid();
		}
		return (!lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()))
				|| (lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.INFO)
				|| (lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.WARNING);
	}

	public ImageIcon getIssueMessageIcon() {
		if (StringUtils.isEmpty(getIssueMessage())) {
			return null;
		}
		if (getIssueMessageType() == null) {
			return null;
		}
		switch (getIssueMessageType()) {
		case ERROR:
			return UtilsIconLibrary.ERROR_ICON;
		case WARNING:
			return UtilsIconLibrary.WARNING_ICON;
		case INFO:
			return UtilsIconLibrary.OK_ICON;
		default:
			return null;
		}
	}

	/**
	 * Hook executed when a step has finished
	 */
	public void done() {
	}

	/**
	 * Hook executed when a step has been reactivated after having finished
	 */
	public void reactivate() {
	}

}
