/*
 * (c) Copyright 2012-2014 Openflexo
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
package org.openflexo.foundation.validation;

import org.openflexo.fib.utils.LocalizedDelegateGUIImpl;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.Validable;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * @author gpolet, sylvain
 * 
 */
public class FlexoProjectValidationModel extends FlexoValidationModel {

	private static Resource fibValidationLocalizedDelegate = ResourceLocator.locateResource("ProjectValidationLocalized");
	private static LocalizedDelegate VALIDATION_LOCALIZATION = LocalizedDelegateGUIImpl.getLocalizedDelegate(
			fibValidationLocalizedDelegate, null, true);

	public FlexoProjectValidationModel() throws ModelDefinitionException {
		super(ModelContextLibrary.getCompoundModelContext(FlexoProject.class), VALIDATION_LOCALIZATION);
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
	 * @see org.openflexo.model.validation.ValidationModel#shouldNotifyValidation(org.openflexo.model.validation.Validable)
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
	 * @see org.openflexo.model.validation.ValidationModel#fixAutomaticallyIfOneFixProposal()
	 */
	@Override
	public boolean fixAutomaticallyIfOneFixProposal() {
		return true;
	}

}
