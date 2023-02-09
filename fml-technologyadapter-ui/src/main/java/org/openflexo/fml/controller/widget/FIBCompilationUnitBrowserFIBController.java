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

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.view.widget.JFIBImageWidget;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;

public class FIBCompilationUnitBrowserFIBController extends FMLFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(FIBCompilationUnitBrowserFIBController.class.getPackage().getName());

	public enum ViewMode {
		Embedding, Hierarchical, Flat
	}

	private ViewMode viewMode = ViewMode.Hierarchical;

	public FIBCompilationUnitBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					setViewMode(ViewMode.Hierarchical);
				} catch (Exception e) {
					// Ignore
				}
			}
		});
	}

	public FIBCompilationUnitBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory, controller);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setViewMode(ViewMode.Hierarchical);
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
		setViewMode(viewMode, false);
	}

	public void setViewMode(ViewMode viewMode, boolean force) {
		if (force || (viewMode == null && this.viewMode != null) || (viewMode != null && !viewMode.equals(this.viewMode))) {
			ViewMode oldValue = this.viewMode;
			this.viewMode = viewMode;
			getPropertyChangeSupport().firePropertyChange("viewMode", oldValue, viewMode);
			JFIBImageWidget flatIconWidget = (JFIBImageWidget) viewForComponent("FlatIcon");
			JFIBImageWidget hierarchicalIconWidget = (JFIBImageWidget) viewForComponent("HierarchicalIcon");
			JFIBImageWidget embeddingIconWidget = (JFIBImageWidget) viewForComponent("EmbeddingIcon");

			if (flatIconWidget != null) {
				if (flatButtonAdapter == null) {
					flatButtonAdapter = new ButtonMouseAdapter(flatIconWidget);
					flatIconWidget.getJComponent().addMouseListener(flatButtonAdapter);
				}
				flatButtonAdapter.setSelected(viewMode == ViewMode.Flat);
			}
			if (hierarchicalIconWidget != null) {
				if (hierarchicalButtonAdapter == null) {
					hierarchicalButtonAdapter = new ButtonMouseAdapter(hierarchicalIconWidget);
					hierarchicalIconWidget.getJComponent().addMouseListener(hierarchicalButtonAdapter);
				}
				hierarchicalButtonAdapter.setSelected(viewMode == ViewMode.Hierarchical);
			}
			if (embeddingIconWidget != null) {
				if (embeddingButtonAdapter == null) {
					embeddingButtonAdapter = new ButtonMouseAdapter(embeddingIconWidget);
					embeddingIconWidget.getJComponent().addMouseListener(embeddingButtonAdapter);
				}
				embeddingButtonAdapter.setSelected(viewMode == ViewMode.Embedding);
			}

		}
	}

	// private static Color selectionColor = UIManager.getLookAndFeelDefaults().getColor("Table.selectionInactiveBackground");
	// private static Color selectionColor = UIManager.getLookAndFeelDefaults().getColor("Table.selectionBackground");
	// private static Color focusColor = new Color(173, 215, 255);

	private static Color focusColor = new Color(212, 212, 212);
	private static Color selectionColor = new Color(180, 180, 180);

	private ButtonMouseAdapter flatButtonAdapter = null;
	private ButtonMouseAdapter hierarchicalButtonAdapter = null;
	private ButtonMouseAdapter embeddingButtonAdapter = null;

	class ButtonMouseAdapter extends MouseAdapter {
		private JFIBImageWidget imageWidget;
		private boolean selected;

		public ButtonMouseAdapter(JFIBImageWidget imageWidget) {
			this.imageWidget = imageWidget;

			// System.out.println("selectionColor: " + selectionColor.toString());
			// System.out.println("focusColor: " + focusColor.toString());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (imageWidget.getJComponent().isEnabled()) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(selected ? selectionColor : focusColor);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (selected) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(selectionColor);
			}
			else {
				imageWidget.getJComponent().setOpaque(false);
				imageWidget.getJComponent().setBackground(null);
			}
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			if (selected) {
				imageWidget.getJComponent().setOpaque(true);
				imageWidget.getJComponent().setBackground(selectionColor);
			}
			else {
				imageWidget.getJComponent().setOpaque(false);
				imageWidget.getJComponent().setBackground(null);
			}
		}
	}

}
