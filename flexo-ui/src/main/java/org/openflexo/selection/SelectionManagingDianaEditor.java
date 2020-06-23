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

package org.openflexo.selection;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.diana.DianaModelFactory;
import org.openflexo.diana.DianaUtils;
import org.openflexo.diana.Drawing;
import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.diana.control.MouseClickControlAction;
import org.openflexo.diana.control.MouseControlContext;
import org.openflexo.diana.control.actions.MouseClickControlActionImpl;
import org.openflexo.diana.control.actions.MouseClickControlImpl;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.diana.swing.JDianaInteractiveEditor;
import org.openflexo.diana.swing.control.SwingToolFactory;
import org.openflexo.diana.swing.view.JDianaView;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.toolbox.ToolBox;

/**
 * Default implementation for a DianaEditor synchronized with a SelectionManager
 * 
 * @author sylvain
 * 
 * @param <D>
 */
public class SelectionManagingDianaEditor<M extends FlexoObject> extends JDianaInteractiveEditor<M> implements SelectionListener {

	private static final Logger logger = Logger.getLogger(SelectionManagingDianaEditor.class.getPackage().getName());

	private SelectionManager _selectionManager;

	public SelectionManagingDianaEditor(Drawing<M> drawing, SelectionManager selectionManager, DianaModelFactory factory,
			SwingToolFactory toolFactory) {
		super(drawing, factory, toolFactory);
		_selectionManager = selectionManager;
		if (_selectionManager != null) {
			selectionManager.addToSelectionListeners(this);
		}
	}

	@Override
	public void delete() {
		super.delete();
		if (_selectionManager != null) {
			_selectionManager.removeFromSelectionListeners(this);
		}
		_selectionManager = null;
	}

	public SelectionManager getSelectionManager() {
		return _selectionManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		if (_selectionManager != null) {
			_selectionManager.removeFromSelectionListeners(this);
		}
		_selectionManager = selectionManager;
		if (_selectionManager != null) {
			selectionManager.addToSelectionListeners(this);
		}
	}

	@Override
	public void setSelectedObjects(List<? extends DrawingTreeNode<?, ?>> someSelectedObjects) {
		if (someSelectedObjects == null) {
			someSelectedObjects = Collections.emptyList();
		}
		// If the current selection is the same as requested, just return
		if (someSelectedObjects.equals(getSelectedObjects())) {
			return;
		}
		if (_selectionManager != null) {
			_selectionManager.resetSelection(true);
		}
		super.setSelectedObjects(someSelectedObjects);
	}

	/**
	 * Return the {@link FlexoObject} beeing represented through the supplied {@link DrawingTreeNode}.<br>
	 * This hook allows to implement a disalignment between the representation and the represented object<br>
	 * To do so, please override this method.<br>
	 * 
	 * Default representation just return the drawable associated with supplied {@link DrawingTreeNode}
	 * 
	 * @param node
	 * @return
	 */
	protected FlexoObject getDrawableForDrawingTreeNode(DrawingTreeNode<?, ?> node) {
		if (node.getDrawable() instanceof FlexoObject) {
			return (FlexoObject) node.getDrawable();
		}
		return null;
	}

	/**
	 * Return the {@link FlexoObject} which is used as drawable in DrawingTreeNode<br>
	 * This hook allows to implement a disalignment between the representation and the represented object<br>
	 * To do so, please override this method.<br>
	 * 
	 * Default representation just return supplied object
	 * 
	 * @param object
	 * @return
	 */
	protected FlexoObject getRepresentedFlexoObject(FlexoObject object) {
		return object;
	}

	@Override
	public void addToSelectedObjects(DrawingTreeNode<?, ?> anObject) {
		// logger.info("_selectionManager="+_selectionManager);
		// logger.info("anObject.getDrawable()="+anObject.getDrawable());
		if (_selectionManager != null) {
			for (FlexoObject o : new ArrayList<>(_selectionManager.getSelection())) {
				if (!mayRepresent(o)) {
					_selectionManager.removeFromSelected(o);
				}
			}
		}
		super.addToSelectedObjects(anObject);
		if (_selectionManager != null) {
			FlexoObject flexoObject = getDrawableForDrawingTreeNode(anObject);
			if (flexoObject != null) {
				_selectionManager.addToSelected(flexoObject);
			}
		}
	}

	@Override
	public void removeFromSelectedObjects(DrawingTreeNode<?, ?> anObject) {
		if (_selectionManager != null) {
			for (FlexoObject o : new ArrayList<>(_selectionManager.getSelection())) {
				if (!mayRepresent(o)) {
					_selectionManager.removeFromSelected(o);
				}
			}
		}
		super.removeFromSelectedObjects(anObject);
		if (_selectionManager != null) {
			FlexoObject flexoObject = getDrawableForDrawingTreeNode(anObject);
			if (flexoObject != null) {
				_selectionManager.removeFromSelected(flexoObject);
			}
		}
	}

	/*@Override
	public void clearSelection() {
		super.clearSelection();
		notifyObservers(new EmptySelection());
	}*/

	@Override
	public void selectDrawing() {
		super.selectDrawing();
		if (_selectionManager != null) {
			_selectionManager.setSelectedObject(getDrawing().getModel());
		}
	}

	public static class ShowContextualMenuControl extends MouseClickControlImpl<SelectionManagingDianaEditor<?>> {

		public ShowContextualMenuControl(EditingContext editingContext) {
			this(editingContext, false);
		}

		public ShowContextualMenuControl(EditingContext editingContext, boolean controlDown) {

			super("Show contextual menu", MouseButton.RIGHT, 1, null, false, controlDown, false, false, editingContext);

			MouseClickControlAction<SelectionManagingDianaEditor<?>> action = new MouseClickControlActionImpl<SelectionManagingDianaEditor<?>>() {

				@Override
				public boolean handleClick(DrawingTreeNode<?, ?> node, SelectionManagingDianaEditor<?> controller,
						MouseControlContext context) {

					JDianaView<?, ?> view = controller.getDrawingView().viewForNode(node);

					Point newPoint = getPointInView(node, controller, context);
					controller.setLastSelectedNode(node);
					controller.setLastClickedPoint(
							node.convertLocalViewCoordinatesToRemoteNormalizedPoint(newPoint, node, controller.getScale()), node);

					if (controller.getDrawableForDrawingTreeNode(node) == null) {
						return false;
					}
					if (!(controller instanceof SelectionManagingDianaEditor)) {
						logger.warning("Cannot show contextual menu: controller " + controller
								+ " does not implement SelectionManagingDianaEditor");
						return false;
					}
					FlexoObject o = controller.getDrawableForDrawingTreeNode(node);

					SelectionManager selectionManager = ((SelectionManagingDianaEditor<?>) controller).getSelectionManager();
					if (ToolBox.isMacOS()) {
						if (!selectionManager.selectionContains(o)) {
							if (selectionManager.getSelectionSize() < 2) {
								selectionManager.setSelectedObject(o);
							}
							else {
								selectionManager.addToSelected(o);
							}
						}
					}
					else {
						boolean isControlDown = context.isControlDown();
						if (isControlDown) {
							if (!selectionManager.selectionContains(o)) {
								selectionManager.addToSelected(o);
							}
						}
						else {
							if (!selectionManager.selectionContains(o)) {
								selectionManager.setSelectedObject(o);
							}
						}
					}
					selectionManager.getContextualMenuManager().showPopupMenuForObject(selectionManager.getFocusedObject(),
							(Component) view, newPoint);
					return true;
				}
			};

			setControlAction(action);

		}
	}

	@Override
	public void fireBeginMultipleSelection() {

	}

	@Override
	public void fireEndMultipleSelection() {

	}

	private boolean mayRepresent(FlexoObject o) {
		return getDrawing().getDrawingTreeNode(getRepresentedFlexoObject(o)) != null;
	}

	@Override
	public void fireObjectDeselected(FlexoObject object) {
		if (mayRepresent(getRepresentedFlexoObject(object))) {
			super.removeFromSelectedObjects(getDrawing().getDrawingTreeNode(getRepresentedFlexoObject(object)));
		}
	}

	@Override
	public void fireObjectSelected(FlexoObject object) {
		if (mayRepresent(getRepresentedFlexoObject(object))) {
			super.addToSelectedObjects(getDrawing().getDrawingTreeNode(getRepresentedFlexoObject(object)));
		}
	}

	@Override
	public void fireResetSelection() {
		super.clearSelection();
	}

	@Override
	public void setLastClickedPoint(DianaPoint lastClickedPoint, DrawingTreeNode<?, ?> node) {
		super.setLastClickedPoint(lastClickedPoint, node);
		DianaPoint unnormalizedPoint = DianaUtils.convertNormalizedPoint(node, lastClickedPoint, getDrawing().getRoot());
		if (_selectionManager instanceof MouseSelectionManager) {
			((MouseSelectionManager) _selectionManager)
					.setLastClickedPoint(new Point((int) unnormalizedPoint.getX(), (int) unnormalizedPoint.getY()));
			if (getDrawableForDrawingTreeNode(node) != null) {
				((MouseSelectionManager) _selectionManager).setLastSelectedObject(getDrawableForDrawingTreeNode(node));
			}
		}
		// SGU: Following code is used to force request focus when a click has been performed
		// We do this to get keyboard accelerators to work again after having switched to an other component getting the focus
		SwingUtilities.invokeLater(() -> {
			getDrawingView().requestFocus();
			getDrawingView().requestFocusInWindow();
		});
	}
}
