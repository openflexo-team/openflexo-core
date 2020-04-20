/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.action.GenerateUnimplementedPropertiesAndBehaviours;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class GenerateUnimplementedPropertiesAndBehavioursWizard extends FlexoActionWizard<GenerateUnimplementedPropertiesAndBehaviours> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GenerateUnimplementedPropertiesAndBehavioursWizard.class.getPackage().getName());

	private static final String NO_PROPERTIES_NOR_BEHAVIOURS_SELECTED = "please_select_property(ies)_and/or_behaviour(s)_to_implement";
	private static final Dimension DIMENSIONS = new Dimension(700, 400);

	private final GenerateUnimplementedPropertiesAndBehavioursInfo generateUnimplementedBehavioursInfo;

	public GenerateUnimplementedPropertiesAndBehavioursWizard(GenerateUnimplementedPropertiesAndBehaviours action,
			FlexoController controller) {
		super(action, controller);
		addStep(generateUnimplementedBehavioursInfo = new GenerateUnimplementedPropertiesAndBehavioursInfo());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("generate_unimplemented_properties_and_behaviours");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	@FIBPanel("Fib/Wizard/Refactor/GenerateUnimplementedPropertiesAndBehaviours.fib")
	public class GenerateUnimplementedPropertiesAndBehavioursInfo extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public GenerateUnimplementedPropertiesAndBehaviours getAction() {
			return GenerateUnimplementedPropertiesAndBehavioursWizard.this.getAction();
		}

		public FlexoConcept getFlexoConcept() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_properties_and_bevaviours_to_implement");
		}

		@Override
		public boolean isValid() {

			if (getAction().getSelectedProperties().isEmpty() && getAction().getSelectedBehaviours().isEmpty()) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_PROPERTIES_NOR_BEHAVIOURS_SELECTED), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

	}

}
