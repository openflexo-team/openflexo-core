/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.action.transformation;

import java.beans.PropertyChangeSupport;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;

/**
 * This abstract class is the base class for a transformation strategy, as it is exposed in the {@link TransformationAction} class
 * 
 * @author sylvain
 *
 */
public abstract class TransformationStrategy<A extends TransformationAction<A, ?, ?>> implements HasPropertyChangeSupport {

	private PropertyChangeSupport pcSupport;

	private Boolean lastValidityStatus = null;
	private IssueMessageType lastNotifiedValidity = null;
	private String issueMessage;
	private IssueMessageType issueMessageType = null;

	private final A transformationAction;

	public TransformationStrategy(A transformationAction) {
		pcSupport = new PropertyChangeSupport(this);
		this.transformationAction = transformationAction;
	}

	public A getTransformationAction() {
		return transformationAction;
	}

	public LocalizedDelegate getLocales() {
		return getTransformationAction().getLocales();
	}

	public void delete() {
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

	/**
	 * Indicates if this transformation strategy is valid and might be applied
	 * 
	 * @see #performStrategy()
	 * @return
	 */
	public abstract boolean isValid();

	/**
	 * Called to execute transformation beeing encoded by this {@link TransformationStrategy}, asserting that current strategy is valid
	 * 
	 * @see #isValid()
	 * @return
	 */
	public abstract Object performStrategy();

	protected void checkValidity() {

		// We we call isValid() by setting issueMessageChange to false
		// If no call to either setIssueMessage() or setIssueMessageType() is fired, then we consider that no message is to be displayed
		issueMessageChangeRequested = false;
		lastValidityStatus = isValid();
		if (!issueMessageChangeRequested) {
			setIssueMessage(null, null);
		}
		IssueMessageType validity = getIssueMessageType();

		if (lastNotifiedValidity == null || (!lastNotifiedValidity.equals(validity))) {
			getPropertyChangeSupport().firePropertyChange("isValid", !lastValidityStatus, (boolean) lastValidityStatus);
			getPropertyChangeSupport().firePropertyChange("messageTypeIsToBeDisplayed", !messageTypeIsToBeDisplayed(),
					messageTypeIsToBeDisplayed());
			getPropertyChangeSupport().firePropertyChange("issueMessage", null, getIssueMessage());
			getPropertyChangeSupport().firePropertyChange("issueMessageType", null, getIssueMessageType());
			//getPropertyChangeSupport().firePropertyChange("issueMessageIcon", null, getIssueMessageIcon());
		}
		else {
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
			//getPropertyChangeSupport().firePropertyChange("issueMessageIcon", null, getIssueMessageIcon());
		}
	}

	public boolean messageTypeIsToBeDisplayed() {
		if (lastValidityStatus == null) {
			lastValidityStatus = isValid();
		}
		return (!lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()))
				|| (lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.INFO)
				|| (lastValidityStatus && StringUtils.isNotEmpty(getIssueMessage()) && getIssueMessageType() == IssueMessageType.WARNING);
	}
}
