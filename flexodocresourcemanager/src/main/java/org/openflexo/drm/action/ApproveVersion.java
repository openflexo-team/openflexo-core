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

package org.openflexo.drm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.drm.Author;
import org.openflexo.drm.DocItem;
import org.openflexo.drm.DocItemAction;
import org.openflexo.drm.DocItemVersion;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.localization.Language;

public class ApproveVersion extends FlexoAction<ApproveVersion, DocItem, DocItem> {

	private static final Logger logger = Logger.getLogger(ApproveVersion.class.getPackage().getName());

	public static FlexoActionFactory<ApproveVersion, DocItem, DocItem> actionType = new FlexoActionFactory<ApproveVersion, DocItem, DocItem>(
			"approve_version", FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public ApproveVersion makeNewAction(DocItem focusedObject, Vector<DocItem> globalSelection, FlexoEditor editor) {
			return new ApproveVersion(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DocItem object, Vector<DocItem> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(DocItem object, Vector<DocItem> globalSelection) {
			return object != null && getPendingActions(object).size() > 0;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, DocItem.class);
	}

	protected static List<DocItemAction> getPendingActions(DocItem item) {
		List<DocItemAction> returned = new ArrayList<>();
		for (Language lang : item.getDocResourceCenter().getLanguages()) {
			DocItemAction dia = item.getLastPendingActionForLanguage(lang);
			if (dia != null) {
				returned.add(dia);
			}
		}
		return returned;
	}

	private DocItem _docItem;
	private DocItemVersion _version;
	private Author _author;
	private DocItemAction _newAction;
	private String _note;
	private Vector<DocItemVersion> _versionsThatCanBeApproved;
	private DocItemVersion _newVersion;

	public Vector<DocItemVersion> getVersionsThatCanBeApproved() {
		if (_versionsThatCanBeApproved == null) {
			_versionsThatCanBeApproved = new Vector<>();
			for (DocItemAction next : getPendingActions(getDocItem())) {
				_versionsThatCanBeApproved.add(next.getVersion());
			}
		}
		return _versionsThatCanBeApproved;
	}

	private ApproveVersion(DocItem focusedObject, Vector<DocItem> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		if (getVersion() != null) {
			logger.info("ApproveVersion");
			if (_newVersion != null && !_newVersion.equals(getVersion())) {
				setVersion(_newVersion);
			}
			_newAction = getDocItem().approveVersion(getVersion(), getAuthor(), getDocItem().getDocResourceCenter());
			_newAction.setNote(getNote());
		}
	}

	public DocItem getDocItem() {
		if (_docItem == null) {
			if (getFocusedObject() != null) {
				_docItem = getFocusedObject();
			}
		}
		return _docItem;
	}

	public void setDocItem(DocItem docItem) {
		_docItem = docItem;
	}

	public Author getAuthor() {
		return _author;
	}

	public void setAuthor(Author author) {
		_author = author;
	}

	public DocItemAction getNewAction() {
		return _newAction;
	}

	public DocItemVersion getVersion() {
		return _version;
	}

	public void setVersion(DocItemVersion version) {
		_version = version;
	}

	public String getNote() {
		return _note;
	}

	public void setNote(String note) {
		_note = note;
	}

	public void setNewVersion(DocItemVersion version) {
		_newVersion = version;
	}

}
