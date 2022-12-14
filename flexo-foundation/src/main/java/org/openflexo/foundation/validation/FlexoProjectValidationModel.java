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

package org.openflexo.foundation.validation;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.Validable;

/**
 * @author gpolet, sylvain
 * 
 */
public class FlexoProjectValidationModel extends FlexoValidationModel {

	// private static Resource fibValidationLocalizedDelegate = ResourceLocator.locateResource("Localized/ProjectValidation");
	// private static LocalizedDelegate VALIDATION_LOCALIZATION = FlexoLocalization.getLocalizedDelegate(fibValidationLocalizedDelegate,
	// null,
	// true, true);

	public FlexoProjectValidationModel() throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(FlexoProject.class), null/*VALIDATION_LOCALIZATION*/);
		// registerRule(new FlexoProject.AllResourcesMustBeDefinedInProject());
		// registerRule(new FlexoProject.FlexoIDMustBeUnique());
		// registerRule(new FlexoProject.NameOfResourceMustBeKeyOfHashtableEntry());
		// registerRule(new FlexoProject.RebuildDependancies());
		// registerRule(new FlexoProject.ComponentInstancesMustDefineAComponent());
		// registerRule(new FlexoProject.GeneratedResourcesMustHaveCGFile());
		// registerRule(new FlexoProject.ModelObjectReferenceMustDefineAnEnclosingProjectID());
		// registerRule(new AbstractActivityNode.ActivityCouldNotDefineOperationPetriGraphWhenNotAllowed());

		// Notify that the validation model is complete and that inheritance
		// computation could be performed
		// update();
	}

	/**
	 * Overrides shouldNotifyValidation
	 * 
	 * @see org.openflexo.pamela.validation.ValidationModel#shouldNotifyValidation(org.openflexo.pamela.validation.Validable)
	 */
	@Override
	protected boolean shouldNotifyValidation(Validable next) {
		return true;
	}

	/**
	 * Return a boolean indicating if validation of each rule must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	@Override
	protected boolean shouldNotifyValidationRules() {
		return true;
	}

	/**
	 * Overrides fixAutomaticallyIfOneFixProposal
	 * 
	 * @see org.openflexo.pamela.validation.ValidationModel#fixAutomaticallyIfOneFixProposal()
	 */
	@Override
	public boolean fixAutomaticallyIfOneFixProposal() {
		return true;
	}

}
