/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.fml.controller.view;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openflexo.fml.controller.CommonFIB;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.gina.swing.view.widget.JFIBEditorWidget;
import org.openflexo.rm.Resource;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This is the module view representing a {@link VirtualModel}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLVirtualModelView extends FlexoConceptView<VirtualModel> {

	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/fml", "org.openflexo.fml.controller.view.FMLTokenMaker");
	}

	public FMLVirtualModelView(VirtualModel virtualModel, FlexoController controller, FlexoPerspective perspective) {
		super(virtualModel, CommonFIB.FML_VIRTUAL_MODEL_VIEW_FIB, controller, perspective);
		updateFMLStyle();
	}

	public FMLVirtualModelView(VirtualModel virtualModel, Resource fibFile, FlexoController controller, FlexoPerspective perspective) {
		super(virtualModel, fibFile, controller, perspective);
		updateFMLStyle();
	}

	private void updateFMLStyle() {
		JFIBEditorWidget editor = (JFIBEditorWidget) getFIBView("FMLEditor");
		RTextScrollPane rTextScrollPane = editor.getTechnologyComponent();
		((RSyntaxTextArea) rTextScrollPane.getTextArea()).setSyntaxEditingStyle("text/fml");
	}

}
