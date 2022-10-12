/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.fml.rt.controller.validation;

import java.util.logging.Logger;

import org.openflexo.fml.rt.controller.FMLRTTechnologyAdapterController;
import org.openflexo.foundation.fml.rt.FMLRTValidationModel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ProblemIssue;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.view.controller.FlexoController;

public class IssueFixing<R extends ValidationRule<R, V>, V extends Validable> extends PropertyChangedSupportDefaultImplementation {

	private static final Logger logger = FlexoLogger.getLogger(IssueFixing.class.getPackage().getName());

	private ProblemIssue<R, V> issue;
	private FMLRTValidationModel validationModel;
	private FlexoController controller;

	public IssueFixing(ProblemIssue<R, V> issue, FlexoController controller) {
		super();
		this.issue = issue;
		this.controller = controller;
		if (controller != null && controller.getApplicationContext() != null) {
			FMLRTTechnologyAdapterController tac = controller.getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(FMLRTTechnologyAdapterController.class);
			validationModel = tac.getFMLRTValidationModel();
		}
	}

	public ProblemIssue<?, ?> getIssue() {
		return issue;
	}

	public FMLRTValidationModel getValidationModel() {
		return validationModel;
	}

	public FlexoController getController() {
		return controller;
	}

	public boolean isFixable() {
		return fixProposal != null;
	}

	public boolean isIgnorable() {
		return issue instanceof ValidationWarning;
	}

	public void fix() {
		System.out.println("Applying fix proposal " + fixProposal);
		fixProposal.apply(true);
	}

	public void ignore() {
		System.out.println("Ignoring");
	}

	FixProposal<R, V> fixProposal;

	public FixProposal<R, V> getFixProposal() {
		return fixProposal;
	}

	public void setFixProposal(FixProposal<R, V> fixProposal) {
		System.out.println("On set le fixProposal a " + fixProposal);
		if ((fixProposal == null && this.fixProposal != null) || (fixProposal != null && !fixProposal.equals(this.fixProposal))) {
			FixProposal<R, V> oldValue = this.fixProposal;
			this.fixProposal = fixProposal;
			getPropertyChangeSupport().firePropertyChange("fixProposal", oldValue, fixProposal);
			getPropertyChangeSupport().firePropertyChange("isFixable", !isFixable(), isFixable());
			getPropertyChangeSupport().firePropertyChange("isIgnorable", !isIgnorable(), isIgnorable());
		}
	}
}
