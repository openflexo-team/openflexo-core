/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.components.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.widget.FIBBrowser;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent;
import org.openflexo.gina.model.widget.FIBList;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.gina.view.FIBView;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.gina.view.widget.FIBListWidget;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.swing.TextFieldCustomPopup;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Default base implementation for a widget allowing to select a {@link FlexoObject}<br>
 * 
 * Default scope is provided by a {@link FlexoServiceManager}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBFlexoObjectSelector<T extends FlexoObject> extends TextFieldCustomPopup<T>
		implements FIBCustomComponent<T>, HasPropertyChangeSupport {

	static final Logger logger = Logger.getLogger(FIBFlexoObjectSelector.class.getPackage().getName());

	private static final String DELETED = "deleted";

	public abstract Resource getFIBResource();

	private T _revertValue;

	protected SelectorDetailsPanel _selectorPanel;

	private FlexoServiceManager serviceManager;
	private Object selectedObject;
	private T selectedValue;
	private final List<T> matchingValues;
	private T candidateValue;

	private FIBCustom component;
	private FIBController controller;

	private PropertyChangeSupport pcSupport;

	private FlexoController flexoController;

	private boolean isFiltered = false;

	private boolean showReset = true;

	public FIBFlexoObjectSelector(T editedObject) {
		super(editedObject);
		pcSupport = new PropertyChangeSupport(this);
		setRevertValue(editedObject);
		setFocusable(true);
		matchingValues = new ArrayList<>();
		getTextField().setEditable(true);
		getTextField().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (!textIsBeeingProgrammaticallyEditing()) {
					updateMatchingValues();
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				if (!textIsBeeingProgrammaticallyEditing()) {
					updateMatchingValues();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (!textIsBeeingProgrammaticallyEditing()) {
					updateMatchingValues();
				}
			}
		});
		getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					SwingUtilities.invokeLater(() -> {
						updateMatchingValues();
						if (matchingValues.size() > 0) {
							setSelectedValue(matchingValues.get(0));
							apply();
						}
					});
				}
				else if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (getCustomPanel() != null) {
						getCustomPanel().getFIBListWidget().requestFocusInWindow();
						getCustomPanel().getFIBListWidget()
								.setSelectedIndex(getCustomPanel().getFIBListWidget().getMultipleValueModel().getSize() - 1);
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (getCustomPanel() != null) {
						getCustomPanel().getFIBListWidget().requestFocusInWindow();
						getCustomPanel().getFIBListWidget().setSelectedIndex(0);
					}
				}

			}

			@Override
			public void keyTyped(KeyEvent e) {

				// if command-key is pressed, do not open popup
				if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown() || e.isMetaDown()) {
					return;
				}

				boolean requestFocus = getTextField().hasFocus();
				final int selectionStart = getTextField().getSelectionStart() + 1;
				final int selectionEnd = getTextField().getSelectionEnd() + 1;
				if (!popupIsShown()) {
					openPopup();
				}
				// updateMatchingValues();
				if (requestFocus) {
					SwingUtilities.invokeLater(() -> {
						getTextField().requestFocusInWindow();
						getTextField().select(selectionStart, selectionEnd);
					});
				}

				/*SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int selectionStart = getTextField().getSelectionStart();
						int selectionEnd = getTextField().getSelectionEnd();
						System.out.println("Was selected: " + selectionStart + " and " + selectionEnd);
						if (!popupIsShown()) {
							openPopup();
						}
						updateMatchingValues();
						System.out.println("Now select: " + selectionStart + " and " + selectionEnd);
						getTextField().select(selectionStart, selectionEnd);
					}
				});*/
			}
		});
	}

	@Override
	public void delete() {
		super.delete();
		if (pcSupport != null) {
			pcSupport.firePropertyChange(DELETED, false, true);
		}
		matchingValues.clear();
		pcSupport = null;
		selectedObject = null;
		selectedValue = null;
		serviceManager = null;
	}

	@Override
	public void init(FIBCustom component, FIBController controller) {
		this.component = component;
		this.controller = controller;
	}

	public FIBController getFIBController() {
		return controller;
	}

	@Override
	public void openPopup() {
		super.openPopup();
		// System.out.println("Request focus now");
		getTextField().requestFocusInWindow();
	}

	public boolean isShowReset() {
		return showReset;
	}

	public void setShowReset(boolean showReset) {
		this.showReset = showReset;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED;
	}

	public boolean isFiltered() {
		return StringUtils.isNotEmpty(getFilteredName()) && isFiltered;
	}

	public String getFilteredName() {
		// return filteredName;
		return getTextField().getText();
	}

	public void setFilteredName(String aString) {
		// logger.info("setFilteredName with "+aString);
		getTextField().setText(aString);
		// filteredName = aString;
		// updateMatchingValues();
	}

	public Object getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(Object selectedObject) {
		// System.out.println("set selected object: "+selectedObject);
		Object old = getSelectedObject();
		if (old != selectedObject) {
			this.selectedObject = selectedObject;
			pcSupport.firePropertyChange("selectedObject", old, selectedObject);
			if (isAcceptableValue(selectedObject)) {
				setSelectedValue((T) selectedObject);
			}
			else {
				setSelectedValue(null);
			}
		}
	}

	public T getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(T selectedValue) {
		// System.out.println("set selected value: "+selectedValue);
		T old = getSelectedValue();
		if (old != selectedValue) {
			this.selectedValue = selectedValue;
			pcSupport.firePropertyChange("selectedValue", old, selectedValue);
			if (getSelectedObject() != getSelectedValue()) {
				setSelectedObject(selectedValue);
			}
		}
	}

	private void updateMatchingValues() {
		final List<T> oldMatchingValues = new ArrayList<>(getMatchingValues());
		// System.out.println("updateMatchingValues() with " + getFilteredName());
		matchingValues.clear();
		if (getAllSelectableValues() != null && getFilteredName() != null) {
			isFiltered = true;
			for (T next : getAllSelectableValues()) {
				if (isAcceptableValue(next) && matches(next, getFilteredName())) {
					matchingValues.add(next);
				}
			}
		}
		logger.fine("Objects matching with " + getFilteredName() + " found " + matchingValues.size() + " values");

		SwingUtilities.invokeLater(() -> {
			pcSupport.firePropertyChange("matchingValues", oldMatchingValues, getMatchingValues());
			if (matchingValues.size() == 1) {
				setSelectedValue(matchingValues.get(0));
			}
		});

		/*pcSupport.firePropertyChange("matchingValues", oldMatchingValues, getMatchingValues());
		
		if (matchingValues.size() == 1) {
			setSelectedValue(matchingValues.get(0));
		}*/
	}

	private void clearMatchingValues() {
		isFiltered = false;
		List<T> oldMatchingValues = new ArrayList<>(getMatchingValues());
		matchingValues.clear();
		pcSupport.firePropertyChange("matchingValues", oldMatchingValues, null);
	}

	/**
	 * This method is used to retrieve all potential values when implementing completion<br>
	 * Completion will be performed on that selectable values<br>
	 * Default implementation is to iterate on all values of browser, please take care to infinite loops.<br>
	 * 
	 * Override when required
	 */
	protected Collection<T> getAllSelectableValues() {
		return getCustomPanel().getAllSelectableValues();
	}

	/**
	 * Override when required
	 */
	protected boolean matches(T o, String filteredName) {
		return o != null && StringUtils.isNotEmpty(renderedString(o))
				&& (renderedString(o)).toUpperCase().indexOf(filteredName.toUpperCase()) > -1;
		/*if (o instanceof FlexoObject) {
		return ((FlexoObject) o).getName() != null
		&& ((FlexoObject) o).getName().toUpperCase().indexOf(filteredName.toUpperCase()) > -1;
		}*/
		// return false;
	}

	public List<T> getMatchingValues() {
		return matchingValues;
	}

	public FlexoServiceManager getServiceManager() {
		if (serviceManager == null && getFlexoController() != null) {
			return getFlexoController().getApplicationContext();
		}
		return serviceManager;
	}

	// FIBModelObjectSelector is applicable to something else than objects in a project
	@CustomComponentParameter(name = "serviceManager", type = CustomComponentParameter.Type.OPTIONAL)
	public void setServiceManager(FlexoServiceManager serviceManager) {
		if (this.serviceManager != serviceManager) {
			FlexoServiceManager oldServiceManager = this.serviceManager;
			if (serviceManager == null) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Passing null serviceManager. If you rely on serviceManager this is unlikely to work");
				}
			}
			this.serviceManager = serviceManager;
			pcSupport.firePropertyChange("serviceManager", oldServiceManager, serviceManager);
		}
	}

	public FlexoController getFlexoController() {
		return flexoController;
	}

	@CustomComponentParameter(name = "flexoController", type = CustomComponentParameter.Type.OPTIONAL)
	public void setFlexoController(FlexoController flexoController) {
		this.flexoController = flexoController;
		if (_selectorPanel != null) {
			_selectorPanel.getController().setFlexoController(flexoController);
		}

		if (flexoController != null && getFIBComponent() != null) {
			getFIBComponent().setCustomTypeManager(flexoController.getApplicationContext().getTechnologyAdapterService());
			getFIBComponent().setCustomTypeEditorProvider(flexoController.getApplicationContext().getTechnologyAdapterControllerService());
		}

	}

	@Override
	public void setRevertValue(T oldValue) {
		if (oldValue != null) {
			_revertValue = oldValue;
		}
		else {
			_revertValue = null;
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Sets revert value to " + _revertValue);
		}
	}

	@Override
	public T getRevertValue() {
		return _revertValue;
	}

	@Override
	protected SelectorDetailsPanel createCustomPanel(T editedObject) {
		_selectorPanel = makeCustomPanel(editedObject);
		if (flexoController != null) {
			_selectorPanel.getController().setFlexoController(flexoController);
			getFIBComponent().setCustomTypeManager(flexoController.getApplicationContext().getTechnologyAdapterService());
			getFIBComponent().setCustomTypeEditorProvider(flexoController.getApplicationContext().getTechnologyAdapterControllerService());
		}
		return _selectorPanel;
	}

	protected SelectorDetailsPanel makeCustomPanel(T editedObject) {
		return new SelectorDetailsPanel(editedObject);
	}

	@Override
	public void updateCustomPanel(T editedObject) {
		// logger.info("updateCustomPanel with " + editedObject + " _selectorPanel=" + _selectorPanel);
		setSelectedObject(editedObject);
		if (_selectorPanel != null) {
			_selectorPanel.update();
		}
	}

	@Override
	public SelectorDetailsPanel getCustomPanel() {
		return (SelectorDetailsPanel) super.getCustomPanel();
	}

	protected SelectorFIBController makeCustomFIBController(FIBComponent fibComponent) {
		return new SelectorFIBController(fibComponent, FIBFlexoObjectSelector.this);
	}

	protected void initFIBComponent(FIBComponent component) {

	}

	public class SelectorDetailsPanel extends ResizablePanel {
		private final FIBContainer fibComponent;
		private final JFIBView<?, ?> fibView;
		private final SelectorFIBController controller;

		private JFIBBrowserWidget<T> browserWidget = null;
		private FIBListWidget<?, ?> listWidget = null;

		protected SelectorDetailsPanel(T anObject) {
			super();

			fibComponent = (FIBContainer) ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(getFIBResource());
			initFIBComponent(fibComponent);
			controller = makeCustomFIBController(fibComponent);
			fibView = (JFIBView<?, ?>) controller.buildView(fibComponent, null, true);

			controller.setDataObject(FIBFlexoObjectSelector.this, true);

			setLayout(new BorderLayout());
			add(fibView.getResultingJComponent(), BorderLayout.CENTER);

			selectValue(anObject);
		}

		protected void selectValue(T value) {
			JFIBBrowserWidget<T> browserWidget = retrieveFIBBrowserWidget();
			if (browserWidget != null) {
				// Force reselect value because tree may have been recomputed
				browserWidget.setSelected(value);
			}
		}

		public void update() {
			controller.setDataObject(FIBFlexoObjectSelector.this);
			// logger.info("update() selectedValue=" + getSelectedValue() + " selectedObject=" + getSelectedObject());
			selectValue(getSelectedValue());
		}

		@Override
		public Dimension getDefaultSize() {
			return new Dimension(fibComponent.getWidth(), fibComponent.getHeight());
		}

		public void delete() {
		}

		protected Set<T> getAllSelectableValues() {
			Set<T> returned = new HashSet<>();
			JFIBBrowserWidget<T> browserWidget = retrieveFIBBrowserWidget();
			if (browserWidget == null) {
				return null;
			}
			Iterator<Object> it = browserWidget.getBrowserModel().recursivelyExploreModelToRetrieveContents();
			while (it.hasNext()) {
				Object o = it.next();
				if (getRepresentedType().isAssignableFrom(o.getClass())) {
					returned.add((T) o);
				}
			}
			return returned;
		}

		public JFIBBrowserWidget<T> getFIBBrowserWidget() {
			if (browserWidget == null) {
				browserWidget = retrieveFIBBrowserWidget();
			}
			return browserWidget;
		}

		public FIBListWidget<?, ?> getFIBListWidget() {
			if (listWidget == null) {
				listWidget = retrieveFIBListWidget();
			}
			return listWidget;
		}

		private JFIBBrowserWidget<T> retrieveFIBBrowserWidget() {
			List<FIBComponent> listComponent = fibComponent.getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBBrowser) {
					return (JFIBBrowserWidget) controller.viewForComponent(c);
				}
			}
			return null;
		}

		private FIBListWidget<?, ?> retrieveFIBListWidget() {
			List<FIBComponent> listComponent = fibComponent.getAllSubComponents();
			for (FIBComponent c : listComponent) {
				if (c instanceof FIBList) {
					return (FIBListWidget) controller.viewForComponent(c);
				}
			}
			return null;
		}

		public FIBContainer getFIBComponent() {
			return fibComponent;
		}

		public SelectorFIBController getController() {
			return controller;
		}

		public FIBView<?, ?> getFIBView() {
			return fibView;
		}
	}

	public static class SelectorFIBController extends FlexoFIBController {
		private FIBFlexoObjectSelector selector;

		public SelectorFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public SelectorFIBController(FIBComponent component, FIBFlexoObjectSelector selector) {
			super(component, SwingViewFactory.INSTANCE);
			this.selector = selector;
		}

		public void selectedObjectChanged() {
			if (selector != null) {
				selector.setEditedObject(selector.selectedValue);
			}
		}

		public void apply() {
			selector.apply();
		}

		public void cancel() {
			selector.cancel();
		}

		public void reset() {
			selector.setEditedObject(null);
			selector.setSelectedObject(null);
			selector.setSelectedValue(null);
			selector.apply();
		}

		public <I> RepositoryFolder<?, I> createNewFolder(RepositoryFolder<?, I> parentFolder) throws IOException {
			String name = FlexoController.askForString(selector, FlexoLocalization.getMainLocalizer().localizedForKey("new_folder_name"));
			ResourceRepository<?, I> repo = parentFolder.getResourceRepository();
			FlexoResourceCenter<I> rc = repo.getResourceCenter();
			I newFolderArtefact = rc.createDirectory(name, parentFolder.getSerializationArtefact());
			RepositoryFolder<?, I> newRepositoryFolder = repo.getRepositoryFolder(newFolderArtefact, true);
			selector.openPopup();
			selector.setEditedObject(newRepositoryFolder);
			return newRepositoryFolder;
		}

		protected final Icon decorateIcon(FlexoObject object, Icon returned) {
			/*if (getFlexoController() != null
					&& getFlexoController().getApplicationContext().getAdvancedPrefs().getHightlightUncommentedItem() && object != null
					&& !object.hasDescription()) {
				if (returned instanceof ImageIcon) {
					returned = IconFactory.getImageIcon((ImageIcon) returned, new IconMarker[] { IconLibrary.WARNING });
				}
				else {
					logger.severe("CANNOT decorate a non ImageIcon for " + this);
				}
			}*/
			return returned;
		}

		class ColorSwapFilter extends RGBImageFilter {
			private final int target1;
			private final int replacement1;
			private final int target2;
			private final int replacement2;

			public ColorSwapFilter(Color target1, Color replacement1, Color target2, Color replacement2) {
				this.target1 = target1.getRGB();
				this.replacement1 = replacement1.getRGB();
				this.target2 = target2.getRGB();
				this.replacement2 = replacement2.getRGB();
			}

			@Override
			public int filterRGB(int x, int y, int rgb) {
				if (rgb == target1) {
					return replacement1;
				}
				else if (rgb == target2) {
					return replacement2;
				}
				return rgb;
			}
		}

	}

	/*
	  @Override public void setEditedObject(BackgroundStyle object) {
	  logger.info("setEditedObject with "+object);
	  super.setEditedObject(object); }
	 */

	@Override
	public void apply() {
		clearMatchingValues();
		setEditedObject(getSelectedValue());
		setRevertValue(getEditedObject());
		closePopup();
		super.apply();
	}

	@Override
	public void cancel() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("CANCEL: revert to " + getRevertValue());
		}
		setEditedObject(getRevertValue());
		closePopup();
		super.cancel();
	}

	@Override
	protected void deletePopup() {
		if (_selectorPanel != null) {
			_selectorPanel.delete();
		}
		_selectorPanel = null;
		super.deletePopup();
	}

	/*
	 * protected void pointerLeavesPopup() { cancel(); }
	 */

	public FIBContainer getFIBComponent() {
		if (getSelectorPanel() != null) {
			return getSelectorPanel().getFIBComponent();
		}
		return null;
	}

	protected SelectorFIBController getController() {
		if (getSelectorPanel() == null) {
			return null;
		}
		return getSelectorPanel().getController();
	}

	/*protected FIBBrowser getFIBBrowser() {
		if (getFIBComponent() == null) {
			return null;
		}
		List<FIBComponent> listComponent = getFIBComponent().getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if (c instanceof FIBBrowser) {
				return (FIBBrowser) c;
			}
		}
		return null;
	}
	
	protected FIBBrowserWidget retrieveFIBBrowserWidget() {
		if (getFIBComponent() == null) {
			return null;
		}
		if (getController() == null) {
			return null;
		}
		List<FIBComponent> listComponent = getFIBComponent().getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if (c instanceof FIBBrowser) {
				return (FIBBrowserWidget) getController().viewForComponent(c);
			}
		}
		return null;
	}*/

	public SelectorDetailsPanel getSelectorPanel() {
		return _selectorPanel;
	}

	@Override
	public String renderedString(T editedObject) {
		if (editedObject == null) {
			return "";
		}
		if (editedObject instanceof FlexoObject) {
			return ((FlexoObject) editedObject).toString();
		}
		return editedObject.toString();
	}

	/**
	 * Override when required
	 */
	protected boolean isAcceptableValue(Object o) {
		// System.out.println("acceptable ? " + o);
		if (o == null) {
			return false;
		}

		if (!getRepresentedType().isAssignableFrom(o.getClass())) {
			return false;
		}
		return evaluateSelectableCondition((T) o);
	}

	private String _selectableConditionAsString = null;
	private DataBinding<Boolean> _selectableCondition;

	public DataBinding<Boolean> getSelectableConditionDataBinding() {
		if (_selectableCondition != null) {
			return _selectableCondition;
		}
		if (_selectableConditionAsString == null || StringUtils.isEmpty(_selectableConditionAsString)) {
			return null;
		}
		_selectableCondition = new DataBinding<>(_selectableConditionAsString, component, Boolean.class, BindingDefinitionType.GET);
		// System.out.println("setSelectableCondition with "+_selectableCondition+" valid ? "+_selectableCondition.isValid());
		return _selectableCondition;
	}

	public String getSelectableCondition() {
		return _selectableConditionAsString;
	}

	@CustomComponentParameter(name = "selectableCondition", type = CustomComponentParameter.Type.OPTIONAL)
	public void setSelectableCondition(String aCondition) {
		_selectableConditionAsString = aCondition;
		_selectableCondition = null;
	}

	public boolean evaluateSelectableCondition(T candidateValue) {
		if (getSelectableConditionDataBinding() == null) {
			return true;
		}
		setCandidateValue(candidateValue);
		boolean returned = true;
		try {
			returned = getSelectableConditionDataBinding().getBindingValue(getSelectorPanel().getFIBView().getBindingEvaluationContext());
		} catch (TypeMismatchException e) {
			e.printStackTrace();
		} catch (NullReferenceException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return returned;
	}

	// Used for computation of "isAcceptableValue()?"
	public T getCandidateValue() {
		return candidateValue;
	}

	// Used for computation of "isAcceptableValue()?"
	public void setCandidateValue(T candidateValue) {
		this.candidateValue = candidateValue;
	}

}
