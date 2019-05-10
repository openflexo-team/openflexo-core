/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.action.transformation.TransformationAction;
import org.openflexo.foundation.action.transformation.TransformationStrategy;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractTransformationWizard<A extends TransformationAction<A, ?, ?>> extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractTransformationWizard.class.getPackage().getName());

	public AbstractTransformationWizard(A action, FlexoController controller) {
		super(action, controller);
	}

	public abstract class TransformationConfigurationStep<S extends TransformationStrategy<A>> extends WizardStep {

		private final S strategy;

		public TransformationConfigurationStep(S strategy) {
			this.strategy = strategy;
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractTransformationWizard.this.getAction();
		}

		public S getStrategy() {
			return strategy;
		}

		@Override
		public boolean isValid() {

			if (!getStrategy().isValid()) {
				setIssueMessage(getStrategy().getIssueMessage(), getStrategy().getIssueMessageType());
				return false;
			}
			return true;
		}

	}

}
