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

package org.openflexo.fml.controller.widget;

import java.util.logging.Logger;

import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.view.controller.FlexoController;

/**
 * Represents a controller with basic FML edition facilities<br>
 * Extends FlexoFIBController by supporting features relative to FML edition
 * 
 * @author sylvain
 */
public class FMLValidationPanelFIBController extends FMLFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(FMLValidationPanelFIBController.class.getPackage().getName());

	private FMLValidationPanel validationPanel;

	public FMLValidationPanelFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);

	}

	public FMLValidationPanelFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory, controller);
	}

	public FMLValidationPanel getValidationPanel() {
		return validationPanel;
	}

	public void setValidationPanel(FMLValidationPanel validationPanel) {
		this.validationPanel = validationPanel;
	}

	public FMLValidationReport getValidationReport() {
		return validationPanel.getDataObject();
	}

	@Override
	public void showIssue(ValidationIssue<?, ?> issue) {
		super.showIssue(issue);
		validationPanel.getFMLEditor().clearHighlights();
		int lineNb = getValidationReport().getLineNumber(issue);
		if (lineNb > -1) {
			validationPanel.getFMLEditor().highlightLine(lineNb);
		}
		if (issue.getValidable() instanceof FMLPrettyPrintable && !(issue.getValidable() instanceof FMLCompilationUnit)) {
			validationPanel.getFMLEditor().highlightObject((FMLPrettyPrintable) issue.getValidable());
		}
	}

	public void parseImmediately() {
		validationPanel.getFMLEditor().parseImmediately();
	}

	/**
	 * Called to force pretty-print of textual FML from internal representation<br>
	 * ("synchronize" in GUI)
	 */
	public void synchronizeTextualFML() {
		validationPanel.getFMLEditor().synchronizeTextualFML();
	}

	@Override
	public void fixIssue(ValidationIssue<?, ?> issue) {
		super.fixIssue(issue);
	}
}
