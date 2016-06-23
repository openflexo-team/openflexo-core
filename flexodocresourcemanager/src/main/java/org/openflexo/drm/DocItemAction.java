/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

package org.openflexo.drm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.openflexo.localization.FlexoLocalization;

public class DocItemAction extends DRMObject {

	private DocItemVersion itemVersion;
	private String authorId;
	private Date actionDate;
	private ActionType actionType;
	private String note;

	public DocItemAction() {
		super();
	}

	public static DocItemAction createSubmitAction(DocItemVersion version, Author author, DocResourceCenter docResourceCenter) {
		DocItemAction newAction = new DocItemAction();
		newAction.itemVersion = version;
		newAction.authorId = author.getIdentifier();
		newAction.actionDate = new Date();
		newAction.actionType = ActionType.SUBMITTED;
		return newAction;
	}

	public static DocItemAction createReviewAction(DocItemVersion version, Author author, DocResourceCenter docResourceCenter) {
		DocItemAction newAction = new DocItemAction();
		newAction.itemVersion = version;
		newAction.authorId = author.getIdentifier();
		newAction.actionDate = new Date();
		newAction.actionType = ActionType.REVIEWED;
		return newAction;
	}

	public static DocItemAction createApproveAction(DocItemVersion version, Author author, DocResourceCenter docResourceCenter) {
		DocItemAction newAction = new DocItemAction();
		newAction.itemVersion = version;
		newAction.authorId = author.getIdentifier();
		newAction.actionDate = new Date();
		newAction.actionType = ActionType.APPROVED;
		return newAction;
	}

	public static DocItemAction createRefuseAction(DocItemVersion version, Author author, DocResourceCenter docResourceCenter) {
		DocItemAction newAction = new DocItemAction();
		newAction.itemVersion = version;
		newAction.authorId = author.getIdentifier();
		newAction.actionDate = new Date();
		newAction.actionType = ActionType.REFUSED;
		return newAction;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
		setChanged();
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
		setChanged();
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
		setChanged();
	}

	public DocItem getItem() {
		return itemVersion.getDocItem();
	}

	public DocItemVersion getVersion() {
		return itemVersion;
	}

	public void setVersion(DocItemVersion version) {
		itemVersion = version;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
		setChanged();
	}

	public String getLocalizedName() {
		return FlexoLocalization.getMainLocalizer().localizedForKeyWithParams(
				"($version.version)/($version.languageId)_($localizedActionType)_on_($localizedSmallActionDate)_by_($authorId)_($statusName)",
				this);
	}

	public String getLocalizedActionType() {
		return FlexoLocalization.getMainLocalizer().localizedForKey(getActionType().getName());
	}

	public String getLocalizedSmallActionDate() {
		// Typically "dd/MM/yyyy" in french, "MM/dd, yyyy" in english
		return new SimpleDateFormat(FlexoLocalization.getMainLocalizer().localizedForKey("doc_item_action_date_format_simple"))
				.format(getActionDate());
	}

	public String getLocalizedFullActionDate() {
		// Typically "dd/MM/yyyy" in french, "MM/dd, yyyy" in english
		return new SimpleDateFormat(FlexoLocalization.getMainLocalizer().localizedForKey("doc_item_action_date_format_extended"))
				.format(getActionDate());
	}

	public boolean isApproved() {
		for (Enumeration en = getItem().getActions().elements(); en.hasMoreElements();) {
			DocItemAction next = (DocItemAction) en.nextElement();
			if (next.getVersion() == getVersion() && next.getActionType() == ActionType.APPROVED) {
				return true;
			}
		}
		return false;
	}

	public boolean isPending() {
		if (isProposal()) {
			return !isApproved() && !isRefused();
		}
		return false;
	}

	public boolean isProposal() {
		return getActionType() == ActionType.SUBMITTED || getActionType() == ActionType.REVIEWED;
	}

	public boolean isRefused() {
		for (Enumeration en = getItem().getActions().elements(); en.hasMoreElements();) {
			DocItemAction next = (DocItemAction) en.nextElement();
			if (next.getVersion() == getVersion() && next.getActionType() == ActionType.REFUSED) {
				return true;
			}
		}
		return false;
	}

	public String getStatusName() {
		if (!isProposal()) {
			return "";
		}
		if (isApproved()) {
			return FlexoLocalization.getMainLocalizer().localizedForKey("[approved]");
		}
		if (isRefused()) {
			return FlexoLocalization.getMainLocalizer().localizedForKey("[refused]");
		}
		if (isPending()) {
			return FlexoLocalization.getMainLocalizer().localizedForKey("[pending]");
		}
		return "";
	}

	/**
	 * Overrides getIdentifier
	 * 
	 * @see org.openflexo.drm.DRMObject#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return getActionType().getName() + "_ON_" + getVersion();
	}

}
