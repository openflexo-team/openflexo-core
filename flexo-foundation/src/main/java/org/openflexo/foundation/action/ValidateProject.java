/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.model.validation.ValidationReport;

public class ValidateProject extends FlexoAction<ValidateProject, FlexoProjectObject<?>, FlexoProjectObject<?>> {

	static final Logger logger = Logger.getLogger(ValidateProject.class.getPackage().getName());

	public static FlexoActionFactory<ValidateProject, FlexoProjectObject<?>, FlexoProjectObject<?>> actionType = new FlexoActionFactory<ValidateProject, FlexoProjectObject<?>, FlexoProjectObject<?>>(
			"validate_project") {

		/**
		 * Factory method
		 */
		@Override
		public ValidateProject makeNewAction(FlexoProjectObject<?> object, Vector<FlexoProjectObject<?>> globalSelection,
				FlexoEditor editor) {
			return new ValidateProject(object, globalSelection, editor);
		}

		// TODO: revalidated action when FlexoProject will be refactored
		@Override
		public boolean isVisibleForSelection(FlexoProjectObject<?> focusedObject, Vector<FlexoProjectObject<?>> globalSelection) {
			return false;
		}

		@Override
		public boolean isEnabledForSelection(FlexoProjectObject<?> focusedObject, Vector<FlexoProjectObject<?>> globalSelection) {
			return false;
		}

	};

	static {
		// FlexoObject.addActionForClass(ValidateProject.actionType, FlexoProject.class);
	}

	private ValidateProject(FlexoProjectObject<?> focusedObject, Vector<FlexoProjectObject<?>> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public FlexoProject<?> getProject() {
		return getFocusedObject().getProject();
	}

	@Override
	protected void doAction(Object context) {
		logger.info("Validate project");
		// makeFlexoProgress(getLocales().localizedForKey("check_model_consistency"), 5);
		// setProgress(getLocales().localizedForKey("loading_required_resources"));

		/*if (getProject().getFlexoComponentLibrary(false) != null) {
			// We validate the component library model
			IEValidationModel ieValidationModel = new IEValidationModel(getProject(), CodeType.PROTOTYPE);
			ieValidationModel.addObserver(ieValidationObserver);
			ieValidationReport = getProject().getFlexoComponentLibrary().validate(ieValidationModel);
			ieValidationModel.deleteObserver(ieValidationObserver);
		}
		if (getProject().getFlexoWorkflow(false) != null) {
			// We validate the workflow model
			WKFValidationModel wkfValidationModel = new WKFValidationModel(getProject(), CodeType.PROTOTYPE);
			wkfValidationModel.addObserver(wkfValidationObserver);
			wkfValidationReport = getProject().getFlexoWorkflow().validate(wkfValidationModel);
			wkfValidationModel.deleteObserver(wkfValidationObserver);
		}
		if (getProject().getDKVModel(false) != null) {
			// We validate the dkv model
			DKVValidationModel dkvValidationModel = new DKVValidationModel(getProject(), CodeType.PROTOTYPE);
			dkvValidationModel.addObserver(dkvValidationObserver);
			dkvValidationReport = getProject().getDKVModel().validate(dkvValidationModel);
			dkvValidationModel.deleteObserver(dkvValidationObserver);
		}
		if (getProject().getDataModel(false) != null) {
			DMValidationModel dmValidationModel = new DMValidationModel(getProject(), CodeType.PROTOTYPE);
			dmValidationModel.addObserver(dmValidationObserver);
			dmValidationReport = getProject().getDataModel().validate(dmValidationModel);
			dmValidationModel.deleteObserver(dmValidationObserver);
		}*/
		// hideFlexoProgress();
	}

	private ValidationReport ieValidationReport = null;
	private ValidationReport wkfValidationReport = null;
	private ValidationReport dkvValidationReport = null;
	private ValidationReport dmValidationReport = null;

	public boolean isProjectValid() {
		return getErrorsNb() == 0;
	}

	public int getErrorsNb() {
		int errorsNb = 0;
		if (ieValidationReport != null) {
			errorsNb += ieValidationReport.getErrorsCount();
		}
		if (wkfValidationReport != null) {
			errorsNb += wkfValidationReport.getErrorsCount();
		}
		if (dkvValidationReport != null) {
			errorsNb += dkvValidationReport.getErrorsCount();
		}
		if (dmValidationReport != null) {
			errorsNb += dmValidationReport.getErrorsCount();
		}
		return errorsNb;
	}

	/*private final FlexoObserver ieValidationObserver = new FlexoObserver() {
		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification instanceof ValidationNotification) {
				if (dataModification instanceof ValidationInitNotification) {
					ValidationInitNotification initNotification = (ValidationInitNotification) dataModification;
					setProgress(FlexoLocalization.localizedForKey("validating_ie_model"));
					resetSecondaryProgress(initNotification.getNbOfObjectToValidate());
					logger.info("validating_ie_model " + initNotification.getNbOfObjectToValidate());
				} else if (dataModification instanceof ValidationProgressNotification) {
					ValidationProgressNotification progressNotification = (ValidationProgressNotification) dataModification;
					setSecondaryProgress(FlexoLocalization.localizedForKey("validating") + " "
							+ progressNotification.getValidatedObject().toString());
				}
			} else if (dataModification instanceof ValidationFinishedNotification) {
				// Nothing
			}
		}
	};
	
	private final FlexoObserver wkfValidationObserver = new FlexoObserver() {
		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification instanceof ValidationNotification) {
				if (dataModification instanceof ValidationInitNotification) {
					ValidationInitNotification initNotification = (ValidationInitNotification) dataModification;
					setProgress(FlexoLocalization.localizedForKey("validating_wkf_model"));
					resetSecondaryProgress(initNotification.getNbOfObjectToValidate());
					logger.info("validating_wkf_model " + initNotification.getNbOfObjectToValidate());
				} else if (dataModification instanceof ValidationProgressNotification) {
					ValidationProgressNotification progressNotification = (ValidationProgressNotification) dataModification;
					setSecondaryProgress(FlexoLocalization.localizedForKey("validating") + " "
							+ progressNotification.getValidatedObject().toString());
				} else if (dataModification instanceof ValidationFinishedNotification) {
					// Nothing
				}
	
			}
		}
	};
	
	private final FlexoObserver dkvValidationObserver = new FlexoObserver() {
		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification instanceof ValidationNotification) {
				if (dataModification instanceof ValidationInitNotification) {
					ValidationInitNotification initNotification = (ValidationInitNotification) dataModification;
					setProgress(FlexoLocalization.localizedForKey("validating_dkv_model"));
					resetSecondaryProgress(initNotification.getNbOfObjectToValidate());
					logger.info("validating_dkv_model " + initNotification.getNbOfObjectToValidate());
				} else if (dataModification instanceof ValidationProgressNotification) {
					ValidationProgressNotification progressNotification = (ValidationProgressNotification) dataModification;
					setSecondaryProgress(FlexoLocalization.localizedForKey("validating") + " "
							+ progressNotification.getValidatedObject().toString());
				} else if (dataModification instanceof ValidationFinishedNotification) {
					// Nothing
				}
	
			}
		}
	};
	
	private final FlexoObserver dmValidationObserver = new FlexoObserver() {
		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification instanceof ValidationNotification) {
				if (dataModification instanceof ValidationInitNotification) {
					ValidationInitNotification initNotification = (ValidationInitNotification) dataModification;
					setProgress(FlexoLocalization.localizedForKey("validating_dm_model"));
					resetSecondaryProgress(initNotification.getNbOfObjectToValidate());
					logger.info("validating_dm_model " + initNotification.getNbOfObjectToValidate());
				} else if (dataModification instanceof ValidationProgressNotification) {
					ValidationProgressNotification progressNotification = (ValidationProgressNotification) dataModification;
					setSecondaryProgress(FlexoLocalization.localizedForKey("validating") + " "
							+ progressNotification.getValidatedObject().toString());
				} else if (dataModification instanceof ValidationFinishedNotification) {
					// Nothing
				}
	
			}
		}
	};*/

	public ValidationReport getDmValidationReport() {
		return dmValidationReport;
	}

	public void setDmValidationReport(ValidationReport dmValidationReport) {
		this.dmValidationReport = dmValidationReport;
	}

	public ValidationReport getIeValidationReport() {
		return ieValidationReport;
	}

	public void setIeValidationReport(ValidationReport ieValidationReport) {
		this.ieValidationReport = ieValidationReport;
	}

	public ValidationReport getWkfValidationReport() {
		return wkfValidationReport;
	}

	public void setWkfValidationReport(ValidationReport wkfValidationReport) {
		this.wkfValidationReport = wkfValidationReport;
	}

	public ValidationReport getDkvValidationReport() {
		return dkvValidationReport;
	}

	public void setDkvValidationReport(ValidationReport dkvValidationReport) {
		this.dkvValidationReport = dkvValidationReport;
	}

	public String readableValidationErrors() {
		StringBuffer bf = new StringBuffer();
		if (getWkfValidationReport() != null) {
			bf.append(getWkfValidationReport().toString());
		}
		if (getIeValidationReport() != null) {
			bf.append(getIeValidationReport().toString());
		}
		if (getDkvValidationReport() != null) {
			bf.append(getDkvValidationReport().toString());
		}
		if (getDmValidationReport() != null) {
			bf.append(getDmValidationReport().toString());
		}
		return bf.toString();
	}
}
