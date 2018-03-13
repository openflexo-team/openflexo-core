/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.view.controller.action;

import java.util.EventObject;

import javax.swing.Icon;

import org.openflexo.action.SubmitDocumentationAction;
import org.openflexo.drm.DocItem;
import org.openflexo.drm.DocItemAction;
import org.openflexo.drm.action.SubmitVersion;
import org.openflexo.drm.ui.SubmitNewVersionPopup;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.Language;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class SubmitDocumentationActionizer extends ActionInitializer<SubmitDocumentationAction, FlexoObject, FlexoObject> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(SubmitDocumentationActionizer.class.getPackage().getName());

	public SubmitDocumentationActionizer(ControllerActionInitializer actionInitializer) {
		super(SubmitDocumentationAction.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<SubmitDocumentationAction, FlexoObject, FlexoObject> getDefaultInitializer() {
		return new FlexoActionInitializer<SubmitDocumentationAction, FlexoObject, FlexoObject>() {
			@Override
			public boolean run(EventObject e, SubmitDocumentationAction anAction) {
				DocItem docItem;
				if (anAction.getFocusedObject() instanceof DocItem) {
					docItem = (DocItem) anAction.getFocusedObject();
				}
				else {
					docItem = getController().getApplicationContext().getDocResourceManager().getDocItemFor(anAction.getFocusedObject());
				}
				if (docItem == null) {
					return false;
				}
				Language language = null;
				if (docItem.getDocResourceCenter().getLanguages().size() > 1) {
					logger.warning("Please reimplement this");
					// TODO: reimplement this
					/*ParameterDefinition[] langParams = new ParameterDefinition[1];
					langParams[0] = new DynamicDropDownParameter("language", "language", docItem.getDocResourceCenter().getLanguages(),
							docItem.getDocResourceCenter().getLanguages().firstElement());
					langParams[0].addParameter("format", "name");
					AskParametersDialog dialog = AskParametersDialog.createAskParametersDialog(getProject(), null,
							FlexoLocalization.localizedForKey("choose_language"),
							FlexoLocalization.localizedForKey("define_submission_language"), langParams);
					if (dialog.getStatus() == AskParametersDialog.VALIDATE) {
						language = (Language) dialog.parameterValueWithName("language");
					} else {
						return false;
					}*/

				}
				else if (docItem.getDocResourceCenter().getLanguages().size() == 1) {
					language = docItem.getDocResourceCenter().getLanguages().firstElement();
				}
				if (language == null) {
					return false;
				}
				SubmitVersion action = SubmitVersion.actionType.makeNewAction(docItem, null,
						getController().getApplicationContext().getDocResourceManager().getEditor());
				SubmitNewVersionPopup editVersionPopup = new SubmitNewVersionPopup(action.getDocItem(), language,
						getController().getFlexoFrame(), getController(),
						getController().getApplicationContext().getDocResourceManager().getEditor());
				action.setVersion(editVersionPopup.getVersionToSubmit());
				if (action.getVersion() == null) {
					return false;
				}
				String title;
				DocItemAction lastAction = action.getDocItem().getLastActionForLanguage(action.getVersion().getLanguage());
				if (lastAction == null) {
					title = FlexoLocalization.getMainLocalizer().localizedForKey("submit_documentation");
				}
				else {
					title = FlexoLocalization.getMainLocalizer().localizedForKey("review_documentation");
					action.getVersion().setVersion(FlexoVersion.versionByIncrementing(lastAction.getVersion().getVersion(), 0, 0, 1));
				}
				logger.warning("Please reimplement this");
				// TODO: reimplement this
				/*ParameterDefinition[] parameters = new ParameterDefinition[4];
				parameters[0] = new ReadOnlyTextFieldParameter("user", "username", DocResourceManager.instance().getUser().getIdentifier());
				parameters[1] = new ReadOnlyTextFieldParameter("language", "language", action.getVersion().getLanguageId());
				parameters[2] = new TextFieldParameter("version", "version", action.getVersion().getVersion().toString());
				parameters[3] = new TextAreaParameter("note", "note", "", 25, 3);
				AskParametersDialog dialog = AskParametersDialog.createAskParametersDialog(getProject(), null, title,
						FlexoLocalization.localizedForKey("define_submission_parameters"), parameters);
				if (dialog.getStatus() == AskParametersDialog.VALIDATE) {
					action.setAuthor(DocResourceManager.instance().getUser());
					String versionId = (String) dialog.parameterValueWithName("version");
					action.getVersion().setVersion(new DocItemVersion.Version(versionId));
					action.setNote((String) dialog.parameterValueWithName("note"));
					anAction.setContext(action);
					action.setContext(anAction);
					return true;
				} else {
					return false;
				}*/
				return true;
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<SubmitDocumentationAction, FlexoObject, FlexoObject> getDefaultFinalizer() {
		return new FlexoActionFinalizer<SubmitDocumentationAction, FlexoObject, FlexoObject>() {
			@Override
			public boolean run(EventObject e, SubmitDocumentationAction action) {
				if (action.getContext() != null && action.getContext() instanceof SubmitVersion) {
					((SubmitVersion) action.getContext()).doAction();
					FlexoController
							.notify(FlexoLocalization.getMainLocalizer().localizedForKey("submission_has_been_successfully_recorded"));
					return true;
				}
				return false;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory actionType) {
		return IconLibrary.HELP_ICON;
	}
}
