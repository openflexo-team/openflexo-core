/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.drm.ui;

import java.awt.BorderLayout;

import org.openflexo.drm.DocItem;
import org.openflexo.drm.DocItemAction;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.localization.Language;

public class SubmitNewVersionView extends AbstractDocItemView {

	public boolean showDetails = false;

	public SubmitNewVersionView(DocItem docItem, Language language, FlexoEditor editor) {
		super(docItem, null, editor);
		DocItemAction lastAction = docItem.getLastActionForLanguage(language);
		languageCB.setSelectedItem(language);
		if (lastAction != null) {
			getDocResourceManager().beginVersionReview(lastAction.getVersion());
			updateViewFromModel();
			setCurrentAction(lastAction);
		} else {
			getDocResourceManager().beginVersionSubmission(docItem, language);
			updateViewFromModel();
		}
		inheritanceChildsListView.setEnabled(false);
		embeddingChildsListView.setEnabled(false);
		relatedToListView.setEnabled(false);
		generalInfoPanel.parentItemRelatedToInheritanceDIS.setEnabled(false);
		generalInfoPanel.parentItemRelatedToInheritanceDIS.setEnabled(false);
		hideDetails();
	}

	protected void hideDetails() {
		showDetails = false;
		remove(bottomPanel);
		remove(rightPanel);
		revalidate();
		repaint();
	}

	protected void showDetails() {
		showDetails = true;
		add(bottomPanel, BorderLayout.SOUTH);
		add(rightPanel, BorderLayout.EAST);
		revalidate();
		repaint();
	}

	@Override
	protected HistoryPanel makeHistoryPanel() {
		return new SubmitNewVersionHistoryPanel();
	}

	protected class SubmitNewVersionHistoryPanel extends HistoryPanel {

		protected SubmitNewVersionHistoryPanel() {
			super();
			actionList.setEnabled(false);
			actionPanel.remove(editButton);
			actionPanel.remove(submitReviewButton);
			actionPanel.remove(approveButton);
			actionPanel.remove(refuseButton);
		}
	}

}
