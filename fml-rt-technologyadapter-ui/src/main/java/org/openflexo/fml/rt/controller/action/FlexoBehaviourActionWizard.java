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

package org.openflexo.fml.rt.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconMarker;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class FlexoBehaviourActionWizard<A extends FlexoBehaviourAction<A, FB, ?>, FB extends FlexoBehaviour>
		extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoBehaviourActionWizard.class.getPackage().getName());

	private ConfigureFlexoBehaviour configureFlexoBehaviour;

	private static final Dimension DIMENSIONS = new Dimension(600, 400);

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	public FlexoBehaviourActionWizard(A action, FlexoController controller) {
		super(action, controller);

		if (!isSkipable()) {
			addStep(configureFlexoBehaviour = new ConfigureFlexoBehaviour());
		}
	}

	public boolean isSkipable() {

		boolean successfullyRetrievedDefaultParameters = getAction().retrieveDefaultParameters();

		if (successfullyRetrievedDefaultParameters && getAction().getFlexoBehaviour().getSkipConfirmationPanel()) {
			return true;
		}

		return false;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocalizedName();
	}

	@Override
	public Image getDefaultPageImage() {
		if (getAction().getFlexoBehaviour().getFlexoConcept().getBigIcon() != null) {
			return IconFactory.getImageIcon(getAction().getFlexoBehaviour().getFlexoConcept().getBigIcon(), getIconMarker()).getImage();
		}
		if (getAction().getFlexoBehaviour().getFlexoConcept() instanceof VirtualModel) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_BIG_ICON, getIconMarker()).getImage();
		}
		return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_BIG_ICON, getIconMarker()).getImage();
	}

	public abstract IconMarker getIconMarker();

	/*public IconMarker getIconMarker() {
		return IconLibrary.NEW_32_32;
	}*/

	public ConfigureFlexoBehaviour getConfigureFlexoBehaviour() {
		return configureFlexoBehaviour;
	}

	@FIBPanel("Fib/Wizard/FlexoBehaviourAction/ConfigureFlexoBehaviour.fib")
	public class ConfigureFlexoBehaviour extends WizardStep implements FlexoObserver {

		public ConfigureFlexoBehaviour() {

			if (getAction() != null) {
				getAction().addObserver(this);
			}
		}

		@Override
		public void delete() {
			if (getAction() != null) {
				getAction().deleteObserver(this);
			}
			super.delete();
		}

		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification.propertyName().equals(FlexoBehaviourAction.PARAMETER_VALUE_CHANGED)) {
				checkValidity();
			}
		}

		public A getAction() {
			return FlexoBehaviourActionWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return StringUtils.isNotEmpty(getAction().getLocalizedDescription()) ? getAction().getLocalizedDescription()
					: getAction().getLocalizedName();
		}

		@Override
		public boolean isValid() {
			if (getFlexoBehaviour() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_behaviour"), IssueMessageType.ERROR);
				return false;
			}

			for (FlexoBehaviourParameter parameter : getFlexoBehaviour().getParameters()) {

				if (!parameter.isValid(getAction(), getAction().getParameterValue(parameter))) {
					// System.out.println(
					// "Invalid parameter: " + parameter + " value=" + action.getCreationSchemeAction().getParameterValue(parameter));
					setIssueMessage(getAction().getLocales().localizedForKey("invalid_parameter") + " : " + parameter.getName(),
							IssueMessageType.ERROR);
					return false;
				}
			}

			return true;
		}

		public FB getFlexoBehaviour() {
			return getAction().getFlexoBehaviour();
		}

	}

}
