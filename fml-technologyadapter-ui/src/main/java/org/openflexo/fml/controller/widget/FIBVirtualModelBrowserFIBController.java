/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
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

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.view.widget.JFIBImageWidget;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;

public class FIBVirtualModelBrowserFIBController extends FMLFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(FIBVirtualModelBrowserFIBController.class.getPackage().getName());

	public enum ViewMode {
		Embedding, Hierarchical, Flat
	}

	private ViewMode viewMode;

	public FIBVirtualModelBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setViewMode(ViewMode.Embedding);
			}
		});
	}

	public FIBVirtualModelBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory, controller);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setViewMode(ViewMode.Embedding);
			}
		});
	}

	public void setViewModeToEmbedding() {
		setViewMode(ViewMode.Embedding);
	}

	public void setViewModeToHierarchical() {
		setViewMode(ViewMode.Hierarchical);
	}

	public void setViewModeToFlat() {
		setViewMode(ViewMode.Flat);
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(ViewMode viewMode) {
		if ((viewMode == null && this.viewMode != null) || (viewMode != null && !viewMode.equals(this.viewMode))) {
			ViewMode oldValue = this.viewMode;
			this.viewMode = viewMode;
			getPropertyChangeSupport().firePropertyChange("viewMode", oldValue, viewMode);

			JFIBImageWidget flatIconWidget = (JFIBImageWidget) viewForComponent("FlatIcon");
			JFIBImageWidget hierarchicalIconWidget = (JFIBImageWidget) viewForComponent("HierarchicalIcon");
			JFIBImageWidget embeddingIconWidget = (JFIBImageWidget) viewForComponent("EmbeddingIcon");

			if (flatIconWidget != null)
				flatIconWidget.getJComponent()
						.setBorder(viewMode == ViewMode.Flat ? BorderFactory.createEtchedBorder() : BorderFactory.createEmptyBorder());
			if (hierarchicalIconWidget != null)
				hierarchicalIconWidget.getJComponent().setBorder(
						viewMode == ViewMode.Hierarchical ? BorderFactory.createEtchedBorder() : BorderFactory.createEmptyBorder());
			if (embeddingIconWidget != null)
				embeddingIconWidget.getJComponent()
						.setBorder(viewMode == ViewMode.Embedding ? BorderFactory.createEtchedBorder() : BorderFactory.createEmptyBorder());
		}
	}

}
