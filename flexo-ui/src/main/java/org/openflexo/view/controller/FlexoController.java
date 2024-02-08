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

package org.openflexo.view.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.ws.Holder;

import org.openflexo.ApplicationContext;
import org.openflexo.FlexoCst;
import org.openflexo.components.ReviewUnsavedDialog;
import org.openflexo.components.validation.ValidationWindow;
import org.openflexo.components.widget.FIBResourceManagerBrowser;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.editor.SelectAndFocusObjectTask;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.action.LoadResourceAction;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoFacet;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept.ParentFlexoConceptEntry;
import org.openflexo.foundation.fml.action.AbstractCreateVirtualModel.ModelSlotEntry;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.ta.FMLModelSlot;
import org.openflexo.foundation.nature.FlexoNature;
import org.openflexo.foundation.project.FlexoProjectReference;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ITechnologySpecificFlexoResourceFactory;
import org.openflexo.foundation.resource.ProjectClosedNotification;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlotObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterPlugin;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBMouseEvent;
import org.openflexo.gina.swing.editor.validation.ComponentValidationWindow;
import org.openflexo.gina.swing.utils.localization.LocalizedEditor;
import org.openflexo.gina.utils.FIBIconLibrary;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.IconMarker;
import org.openflexo.inspector.ModuleInspectorController;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.FlexoModule.WelcomePanel;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.pamela.undo.AddCommand;
import org.openflexo.pamela.undo.AtomicEdit;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.undo.DeleteCommand;
import org.openflexo.pamela.undo.RemoveCommand;
import org.openflexo.pamela.undo.SetCommand;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationRuleFilter;
import org.openflexo.prefs.ApplicationFIBLibraryService;
import org.openflexo.prefs.FlexoPreferences;
import org.openflexo.prefs.GeneralPreferences;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.selection.MouseSelectionManager;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.utils.CancelException;
import org.openflexo.utils.TooManyFailedAttemptException;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.FlexoMainPane;
import org.openflexo.view.FlexoRelativeWindow;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.ControllerModel;
import org.openflexo.view.controller.model.FlexoPerspective;
import org.openflexo.view.controller.model.Location;
import org.openflexo.view.menu.FlexoMenuBar;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * General controller managing an application module (see {@link FlexoModule}).<br>
 * 
 * @author sylvain
 */
public abstract class FlexoController implements PropertyChangeListener, HasPropertyChangeSupport {

	static final Logger logger = Logger.getLogger(FlexoController.class.getPackage().getName());

	public static final String DISPOSED = "disposed";
	public static final String EDITOR = "editor";

	private PropertyChangeSupport propertyChangeSupport;

	private boolean disposed = false;

	// This map stores ModuleViews associated to their perspective and master object
	private final Map<FlexoPerspective, Map<FlexoObject, ModuleView<?>>> moduleViews = new HashMap<>();

	private LocalizedEditor mainLocalizedEditor;
	private ValidationWindow validationWindow;

	protected FlexoModule<?> module;
	protected FlexoMenuBar menuBar;
	protected MouseSelectionManager selectionManager;
	private final ControllerActionInitializer controllerActionInitializer;

	protected FlexoFrame flexoFrame;
	private FlexoMainPane mainPane;
	private final ControllerModel controllerModel;
	private final List<FlexoMenuBar> registeredMenuBar = new ArrayList<>();
	private ModuleInspectorController mainInspectorController;
	protected PropertyChangeListenerRegistrationManager manager = new PropertyChangeListenerRegistrationManager();

	// private Map<TechnologyAdapter, FIBTechnologyBrowser<?>> sharedBrowsers = new HashMap<>();

	/**
	 * Constructor
	 */
	protected FlexoController(FlexoModule<?> module) {
		super();

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("init_module_controller"));

		this.module = module;
		controllerModel = new ControllerModel(module.getApplicationContext(), module);
		propertyChangeSupport = new PropertyChangeSupport(this);
		manager.new PropertyChangeListenerRegistration(this, controllerModel);
		flexoFrame = createFrame();

		controllerActionInitializer = createControllerActionInitializer();
		registerShortcuts(controllerActionInitializer);

		menuBar = createAndRegisterNewMenuBar();
		selectionManager = createSelectionManager();
		flexoFrame.setJMenuBar(menuBar);
		flexoFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				"escape");
		flexoFrame.getRootPane().getActionMap().put("escape", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelCurrentAction();
			}
		});
		mainPane = createMainPane();
		getFlexoFrame().getContentPane().add(mainPane, BorderLayout.CENTER);
		((JComponent) getFlexoFrame().getContentPane()).revalidate();

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("init_inspectors"));
		initInspectors();

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("init_perspectives"));
		initializePerspectives();

		if (getApplicationContext().getGeneralPreferences() != null) {
			getApplicationContext().getGeneralPreferences().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		if (getModuleLoader().getLastActiveEditor() != null) {
			controllerModel.setCurrentEditor(getModuleLoader().getLastActiveEditor());
		}
		else {
			controllerModel.setCurrentEditor(getApplicationContext().getApplicationEditor());
		}

	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	protected abstract void initializePerspectives();

	/**
	 * Called when a specific technology should be focused in module<br>
	 * 
	 * Implementation is module-specific
	 * 
	 * Please override when required
	 * 
	 * @param technologyAdapter
	 */
	public void focusOnTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
	}

	private FIBResourceManagerBrowser sharedBrowser;

	public FIBResourceManagerBrowser getSharedBrowser() {
		if (sharedBrowser == null) {
			sharedBrowser = new FIBResourceManagerBrowser(getApplicationContext(), this, getModuleLocales());
		}
		return sharedBrowser;
	}

	/**
	 * Return {@link FMLTechnologyAdapter}
	 * 
	 * @return
	 */
	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getApplicationContext().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	/**
	 * Return {@link FMLRTTechnologyAdapter}
	 * 
	 * @return
	 */
	public FMLRTTechnologyAdapter getFMLRTTechnologyAdapter() {
		return getApplicationContext().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
	}

	/**
	 * Return {@link TechnologyAdapterController} specific to {@link FMLTechnologyAdapter}
	 * 
	 * @return
	 */
	public TechnologyAdapterController<FMLTechnologyAdapter> getFMLTechnologyAdapterController() {
		FMLTechnologyAdapter fmlTA = getApplicationContext().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		return getApplicationContext().getTechnologyAdapterControllerService().getTechnologyAdapterController(fmlTA);
	}

	/**
	 * Return {@link TechnologyAdapterController} specific to {@link FMLRTTechnologyAdapter}
	 * 
	 * @return
	 */
	public TechnologyAdapterController<FMLRTTechnologyAdapter> getFMLRTTechnologyAdapterController() {
		FMLRTTechnologyAdapter fmlRTTA = getApplicationContext().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		return getApplicationContext().getTechnologyAdapterControllerService().getTechnologyAdapterController(fmlRTTA);
	}

	public final ControllerModel getControllerModel() {
		return controllerModel;
	}

	protected abstract MouseSelectionManager createSelectionManager();

	/**
	 * Creates a new instance of MenuBar for the module this controller refers to
	 * 
	 * @return
	 */
	protected abstract FlexoMenuBar createNewMenuBar();

	protected FlexoFrame createFrame() {
		return new FlexoFrame(this);
	}

	public final MouseSelectionManager getSelectionManager() {
		return selectionManager;
	}

	public final FlexoMenuBar getMenuBar() {
		return menuBar;
	}

	public final ApplicationContext getApplicationContext() {
		if (getModule() != null) {
			return getModule().getApplicationContext();
		}
		return null;
	}

	public final ProjectLoader getProjectLoader() {
		return getApplicationContext().getProjectLoader();
	}

	public final ModuleLoader getModuleLoader() {
		return getApplicationContext().getModuleLoader();
	}

	/**
	 * Creates a new instance of MenuBar for the module this controller refers to
	 * 
	 * @return
	 */
	protected ControllerActionInitializer createControllerActionInitializer() {
		return new ControllerActionInitializer(this);
	}

	public ControllerActionInitializer getControllerActionInitializer() {
		return controllerActionInitializer;
	}

	/**
	 * Creates and register a new instance of MenuBar for the module this controller refers to
	 * 
	 * @return
	 */
	public final FlexoMenuBar createAndRegisterNewMenuBar() {
		FlexoMenuBar returned = createNewMenuBar();
		registeredMenuBar.add(returned);
		if (getFlexoFrame() != null) {
			for (FlexoRelativeWindow next : getFlexoFrame().getRelativeWindows()) {
				returned.getWindowMenu().addFlexoRelativeWindowMenu(next);
			}
		}
		return returned;
	}

	public void notifyNewFlexoRelativeWindow(FlexoRelativeWindow w) {
		for (FlexoMenuBar next : registeredMenuBar) {
			next.getWindowMenu().addFlexoRelativeWindowMenu(w);
		}
	}

	public void notifyRemoveFlexoRelativeWindow(FlexoRelativeWindow w) {
		for (FlexoMenuBar next : registeredMenuBar) {
			next.getWindowMenu().removeFlexoRelativeWindowMenu(w);
		}
	}

	public void notifyRenameFlexoRelativeWindow(FlexoRelativeWindow w, String title) {
		for (FlexoMenuBar next : registeredMenuBar) {
			next.getWindowMenu().renameFlexoRelativeWindowMenu(w, title);
		}
	}

	private InspectorGroup moduleInspectorGroup;

	public InspectorGroup getModuleInspectorGroup() {
		return moduleInspectorGroup;
	}

	public InspectorGroup getCoreInspectorGroup() {
		return getModuleInspectorController().getCoreInspectorGroup();
	}

	/**
	 *
	 */
	public void initInspectors() {
		moduleInspectorGroup = loadInspectorGroup(getModule().getShortName().toUpperCase(), getModuleLocales(), getCoreInspectorGroup());
		getSelectionManager().addObserver(getModuleInspectorController());
	}

	public ModuleInspectorController getModuleInspectorController() {
		if (mainInspectorController == null) {
			mainInspectorController = new ModuleInspectorController(this);
		}
		return mainInspectorController;
	}

	public InspectorGroup loadInspectorGroup(String inspectorGroup, LocalizedDelegate locales, InspectorGroup... parentInspectorGroups) {

		Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/" + inspectorGroup);

		if (inspectorsDir == null) {
			logger.warning("Could not find Resource Inspectors/" + inspectorGroup);
			return null;
		}

		return getModuleInspectorController().loadDirectory(inspectorsDir, locales, parentInspectorGroups);
	}

	public FlexoFrame getFlexoFrame() {
		return flexoFrame;
	}

	public FlexoModule<?> getModule() {
		return module;
	}

	protected final void setEditor(FlexoEditor editor) {
		controllerModel.setCurrentEditor(editor);
	}

	protected void updateEditor(FlexoEditor from, FlexoEditor to) {
		if (from instanceof InteractiveFlexoEditor) {
			((InteractiveFlexoEditor) from).unregisterControllerActionInitializer(getControllerActionInitializer());
		}
		if (to instanceof InteractiveFlexoEditor) {
			((InteractiveFlexoEditor) to).registerControllerActionInitializer(getControllerActionInitializer());
		}
		getPropertyChangeSupport().firePropertyChange(EDITOR, from, to);

		for (FlexoPerspective perspective : getControllerModel().getPerspectives()) {
			perspective.updateEditor(from, to);
		}

	}

	public abstract FlexoObject getDefaultObjectToSelect(FlexoProject<?> project);

	public FlexoProject<?> getProject() {
		if (getEditor() != null) {
			return getEditor().getProject();
		}
		return null;
	}

	public Object getProjectDirectory() {
		if (getProject() != null) {
			return getProject().getProjectDirectory();
		}
		else {
			return null;
		}
	}

	private FlexoMenuBar inspectorMenuBar;

	public FlexoMenuBar getInspectorMenuBar() {
		if (inspectorMenuBar == null) {
			inspectorMenuBar = createAndRegisterNewMenuBar();
		}
		return inspectorMenuBar;
	}

	public static void showError(String msg) throws HeadlessException {
		showError(FlexoLocalization.getMainLocalizer().localizedForKey("error"), msg);
	}

	public static void showError(String title, String msg) throws HeadlessException {
		showMessageDialog(msg, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void notify(String msg) throws HeadlessException {
		showMessageDialog(msg, FlexoLocalization.getMainLocalizer().localizedForKey("confirmation"), JOptionPane.INFORMATION_MESSAGE);
	}

	public static boolean notifyWithCheckbox(String title, String msg, String checkboxText, boolean defaultValue) {
		JPanel root = new JPanel(new BorderLayout());
		Icon msgIcon = UIManager.getDefaults().getIcon("OptionPane.informationIcon");
		JLabel notifyIcon = new JLabel(msgIcon);
		notifyIcon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel label = new JLabel(msg);
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		JCheckBox box = new JCheckBox(checkboxText, defaultValue);
		box.setBorder(BorderFactory.createEmptyBorder(1, 20, 10, 10));
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(label, BorderLayout.NORTH);
		centerPanel.add(box, BorderLayout.SOUTH);
		JButton ok = new JButton("ok"/*FlexoLocalization.localizedForKey("ok")*/);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(ok);
		root.add(notifyIcon, BorderLayout.WEST);
		root.add(centerPanel, BorderLayout.CENTER);
		root.add(buttonPanel, BorderLayout.SOUTH);
		final FlexoDialog dialog = new FlexoDialog();
		if (title != null) {
			dialog.setTitle(title);
		}
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}

		});
		dialog.setResizable(false);
		dialog.add(root);
		dialog.pack();
		dialog.setVisible(true);
		return box.isSelected();
	}

	public static int ask(String msg) throws HeadlessException {
		return showConfirmDialog(msg, FlexoLocalization.getMainLocalizer().localizedForKey("information"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}

	public static boolean confirmWithWarning(String msg) throws HeadlessException {
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.getMainLocalizer().localizedForKey("information"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] { FlexoLocalization.getMainLocalizer().localizedForKey("yes"),
						FlexoLocalization.getMainLocalizer().localizedForKey("no") },
				FlexoLocalization.getMainLocalizer().localizedForKey("no")) == JOptionPane.YES_OPTION;
	}

	public static boolean confirm(String msg) throws HeadlessException {
		return ask(msg) == JOptionPane.YES_OPTION;
	}

	public static int confirmYesNoCancel(String localizedMessage, String localizedTitle) {
		return showOptionDialog(FlexoFrame.getActiveFrame(), localizedMessage, localizedTitle, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
	}

	public static int confirmWithWarningYesNoCancel(String localizedMessage, String localizedTitle) {
		return showOptionDialog(FlexoFrame.getActiveFrame(), localizedMessage, localizedTitle, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, null, null);
	}

	public static String askForString(String msg) throws HeadlessException {
		return showInputDialog(msg, FlexoLocalization.getMainLocalizer().localizedForKey("information"), JOptionPane.QUESTION_MESSAGE);
	}

	public static String askForString(Component parentComponent, String msg) throws HeadlessException {
		return showInputDialog(parentComponent, msg, FlexoLocalization.getMainLocalizer().localizedForKey("information"),
				JOptionPane.OK_CANCEL_OPTION);
	}

	public static String askForStringMatchingPattern(String msg, Pattern pattern, String localizedPattern) {
		String result = askForString(msg);
		while (result != null && !pattern.matcher(result).matches()) {
			notify(localizedPattern);
			result = askForString(msg);
		}
		return result;
	}

	public static int selectOption(String msg, String[] options, String initialOption) {
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.getMainLocalizer().localizedForKey("confirmation"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialOption);
	}

	public static int selectOption(String msg, String initialOption, String... options) {
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.getMainLocalizer().localizedForKey("confirmation"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialOption);
	}

	public void showInspector() {
		getModuleInspectorController().getInspectorDialog().setVisible(true);
	}

	public void hideInspector() {
		getModuleInspectorController().getInspectorDialog().setVisible(false);
		;
	}

	public void resetInspector() {
		getModuleInspectorController().resetInspector();
	}

	public void registerShortcuts(ControllerActionInitializer controllerInitializer) {
		controllerInitializer.getActionInitializers().registerShortcuts(this);
	}

	private static class CompoundAction extends AbstractAction {
		private final List<Action> actions = new ArrayList<>();

		void addToAction(Action action) {
			actions.add(action);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (Action action : actions) {
				action.actionPerformed(e);
			}
		}
	}

	public void registerActionForKeyStroke(AbstractAction action, KeyStroke accelerator, String actionName) {
		String key = actionName;
		Object object = getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(accelerator);
		Action action2 = null;
		if (object != null) {
			action2 = getFlexoFrame().getRootPane().getActionMap().get(object);
		}
		if (action2 != null) {
			if (action2 instanceof CompoundAction) {
				((CompoundAction) action2).addToAction(action);
				return;
			}
			else {
				CompoundAction compoundAction = new CompoundAction();
				compoundAction.addToAction(action2);
				compoundAction.addToAction(action);
				action = compoundAction;
				key = "compound-" + accelerator.toString();
			}
		}
		getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(accelerator, key);
		getFlexoFrame().getRootPane().getActionMap().put(key, action);
		if (accelerator.getKeyCode() == FlexoCst.DELETE_KEY_CODE) {
			int keyCode = FlexoCst.BACKSPACE_DELETE_KEY_CODE;
			getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke(keyCode, accelerator.getModifiers()), key);
		}
	}

	/**
	 * Return LocalizedEditor for main localization<br>
	 * Each module defines its own LocalizedEditor for main localization (this is not very optimal) TODO: refactor this when localization
	 * will be a service
	 * 
	 * @return
	 */
	public LocalizedEditor getMainLocalizedEditor() {
		if (mainLocalizedEditor == null) {
			mainLocalizedEditor = new LocalizedEditor(getFlexoFrame(), "localized_editor", FlexoLocalization.getMainLocalizer(),
					FlexoLocalization.getMainLocalizer(), true, false);
		}
		return mainLocalizedEditor;
	}

	/**
	 * Return non-modal {@link ComponentValidationWindow} declared for this module<br>
	 * Note that one {@link ComponentValidationWindow} is declared for each {@link FlexoModule}<br>
	 * Force the creation of {@link ComponentValidationWindow} if non existant.
	 * 
	 * @return
	 */
	public ValidationWindow getValidationWindow() {
		return getValidationWindow(true);
	}

	/**
	 * Return non-modal {@link ComponentValidationWindow} declared for this module<br>
	 * Note that one {@link ComponentValidationWindow} is declared for each {@link FlexoModule}<br>
	 * 
	 * @param create
	 *            flag indicating if ComponentValidationWindow must be created when unexistant
	 * @return
	 */
	public ValidationWindow getValidationWindow(boolean create) {
		if (create) {
			if (validationWindow == null || validationWindow.isDisposed()) {
				validationWindow = new ValidationWindow(getFlexoFrame(), this);
			}
		}
		return validationWindow;
	}

	/**
	 * Perform consistency check for supplied object<br>
	 * The right {@link ValidationModel} is first retrieved and set with the rule enability as defined in preferences.<br>
	 * Then, the validation is run and results are displayed in the module's {@link ComponentValidationWindow}
	 * 
	 * @param objectToValidate
	 */
	public void consistencyCheck(FlexoObject objectToValidate) {
		FlexoValidationModel validationModel = getValidationModelForObject(objectToValidate);
		if (validationModel == null) {
			logger.warning("No ValidationModel found for " + objectToValidate);
			return;
		}
		if (validationModel.getRuleFilter() == null) {
			validationModel.setRuleFilter(new ValidationRuleFilter() {
				@Override
				public boolean accept(ValidationRule<?, ?> rule) {
					return getApplicationContext().getGeneralPreferences().isValidationRuleEnabled(rule);
				}
			});
		}
		getValidationWindow(true).setVisible(true);
		getValidationWindow(true).validateAndDisplayReportForObject(objectToValidate, validationModel);

	}

	/**
	 * Brings up a dialog with a specified icon, where the initial choice is determined by the <code>initialValue</code> parameter and the
	 * number of choices is determined by the <code>optionType</code> parameter.
	 * <p>
	 * If <code>optionType</code> is <code>YES_NO_OPTION</code>, or <code>YES_NO_CANCEL_OPTION</code> and the <code>options</code> parameter
	 * is <code>null</code>, then the options are supplied by the look and feel.
	 * <p>
	 * The <code>messageType</code> parameter is primarily used to supply a default icon from the look and feel.
	 * 
	 * @param parentComponent
	 *            determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>, or if the
	 *            <code>parentComponent</code> has no <code>Frame</code>, a default <code>Frame</code> is used
	 * @param message
	 *            the <code>Object</code> to display
	 * @param title
	 *            the title string for the dialog
	 * @param optionType
	 *            an integer designating the options available on the dialog: <code>YES_NO_OPTION</code>, or
	 *            <code>YES_NO_CANCEL_OPTION</code>
	 * @param messageType
	 *            an integer designating the kind of message this is, primarily used to determine the icon from the pluggable Look and Feel:
	 *            <code>ERROR_MESSAGE</code>, <code>INFORMATION_MESSAGE</code>, <code>WARNING_MESSAGE</code>, <code>QUESTION_MESSAGE</code>,
	 *            or <code>PLAIN_MESSAGE</code>
	 * @param icon
	 *            the icon to display in the dialog
	 * @param options
	 *            an array of objects indicating the possible choices the user can make; if the objects are components, they are rendered
	 *            properly; non-<code>String</code> objects are rendered using their <code>toString</code> methods; if this parameter is
	 *            <code>null</code>, the options are determined by the Look and Feel
	 * @param initialValue
	 *            the object that represents the default selection for the dialog; only meaningful if <code>options</code> is used; can be
	 *            <code>null</code>
	 * @return an integer indicating the option chosen by the user, or <code>CLOSED_OPTION</code> if the user closed the dialog
	 * @exception HeadlessException
	 *                if <code>GraphicsEnvironment.isHeadless</code> returns <code>true</code>
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 */
	private static synchronized int showOptionDialog(Component parentComponent, Object message, String title, int optionType,
			int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
		final Component parent = parentComponent;
		JOptionPane pane = null;
		boolean isLocalized = false;
		Object[] availableOptions = null;
		if (optionType == JOptionPane.OK_CANCEL_OPTION && options == null) {
			availableOptions = new Object[] { FlexoLocalization.getMainLocalizer().localizedForKey("OK"),
					FlexoLocalization.getMainLocalizer().localizedForKey("cancel") };
			pane = new JOptionPane(message, messageType, optionType, icon, availableOptions, availableOptions[0]) {
				@Override
				public int getMaxCharactersPerLineCount() {
					return FlexoController.getMaxCharactersPerLine(parent, this);
				}
			};
			isLocalized = true;
			// pane.setInitialSelectionValue();
		}
		else if (optionType == JOptionPane.YES_NO_OPTION && options == null) {
			availableOptions = new Object[] { FlexoLocalization.getMainLocalizer().localizedForKey("yes"),
					FlexoLocalization.getMainLocalizer().localizedForKey("no") };
			pane = new JOptionPane(message, messageType, optionType, icon, availableOptions, availableOptions[0]) {
				@Override
				public int getMaxCharactersPerLineCount() {
					return FlexoController.getMaxCharactersPerLine(parent, this);
				}
			};
			isLocalized = true;
			// pane.setInitialSelectionValue(availableOptions[1]);
		}
		else if (optionType == JOptionPane.YES_NO_CANCEL_OPTION && options == null) {
			availableOptions = new Object[] { FlexoLocalization.getMainLocalizer().localizedForKey("yes"),
					FlexoLocalization.getMainLocalizer().localizedForKey("no"),
					FlexoLocalization.getMainLocalizer().localizedForKey("cancel") };
			pane = new JOptionPane(message, messageType, optionType, icon, availableOptions, availableOptions[0]) {
				@Override
				public int getMaxCharactersPerLineCount() {
					return FlexoController.getMaxCharactersPerLine(parent, this);
				}
			};
			isLocalized = true;
			// pane.setInitialSelectionValue(availableOptions[1]);
		}
		else {
			pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue) {
				@Override
				public int getMaxCharactersPerLineCount() {
					return FlexoController.getMaxCharactersPerLine(parent, this);
				}
			};
			pane.setInitialValue(initialValue);
		}

		pane.setComponentOrientation((parentComponent == null ? FlexoFrame.getActiveFrame() : parentComponent).getComponentOrientation());

		pane.setMessageType(messageType);
		final JDialog dialog = pane.createDialog(parentComponent, title);
		Container content = dialog.getContentPane();
		JScrollPane scroll = new JScrollPane(content, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		dialog.setContentPane(scroll);
		dialog.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
				}
			}
		});
		dialog.validate();
		dialog.pack();

		Window window = null;
		if (parentComponent instanceof Window) {
			window = (Window) parentComponent;
		}
		if (window == null && parentComponent != null) {
			window = SwingUtilities.getWindowAncestor(parentComponent);
		}
		Dimension maxDim;
		if (window != null && window.isVisible()) {
			maxDim = new Dimension(
					Math.min(dialog.getWidth(), window.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().width),
					Math.min(dialog.getHeight(),
							window.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().height));
		}
		else {
			maxDim = new Dimension(Math.min(dialog.getWidth(), Toolkit.getDefaultToolkit().getScreenSize().width),
					Math.min(dialog.getHeight(), Toolkit.getDefaultToolkit().getScreenSize().height));
		}
		dialog.setSize(maxDim);
		dialog.setLocationRelativeTo(window);
		dialog.setVisible(true);
		// pane.selectInitialValue();
		dialog.dispose();
		Object selectedValue = pane.getValue();

		if (selectedValue == null) {
			return JOptionPane.CLOSED_OPTION;
		}
		if (isLocalized) {
			for (int counter = 0, maxCounter = availableOptions.length; counter < maxCounter; counter++) {
				if (optionType == JOptionPane.OK_CANCEL_OPTION) {
					if (availableOptions[counter].equals(selectedValue)) {
						if (counter == 0) {
							return JOptionPane.OK_OPTION;
						}
						if (counter == 1) {
							return JOptionPane.CANCEL_OPTION;
						}
					}
				}
				else if (optionType == JOptionPane.YES_NO_OPTION) {
					if (availableOptions[counter].equals(selectedValue)) {
						if (counter == 0) {
							return JOptionPane.YES_OPTION;
						}
						if (counter == 1) {
							return JOptionPane.NO_OPTION;
						}
					}
				}
				else if (optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
					if (availableOptions[counter].equals(selectedValue)) {
						if (counter == 0) {
							return JOptionPane.YES_OPTION;
						}
						if (counter == 1) {
							return JOptionPane.NO_OPTION;
						}
						if (counter == 2) {
							return JOptionPane.CANCEL_OPTION;
						}
					}
				}
			}
		}
		if (options == null) {
			if (selectedValue instanceof Integer) {
				return ((Integer) selectedValue).intValue();
			}
			return JOptionPane.CLOSED_OPTION;
		}
		for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
			if (options[counter].equals(selectedValue)) {
				return counter;
			}
		}
		return JOptionPane.CLOSED_OPTION;
	}

	protected static int getMaxCharactersPerLine(Component parent, JOptionPane pane) {
		if (pane.getMessage() instanceof String && ((String) pane.getMessage()).startsWith("<html>")) {
			return Integer.MAX_VALUE;
		}
		Window w = null;
		if (parent != null) {
			w = SwingUtilities.getWindowAncestor(parent);
		}
		int width = 0;
		if (w != null) {
			width = w.getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();
		}
		else {
			width = Toolkit.getDefaultToolkit().getScreenSize().width;
		}
		int availableWidth = width;
		if (pane.getIcon() != null) {
			availableWidth -= pane.getIcon().getIconWidth();
		}
		else {
			Icon icon = UIManager.getIcon("OptionPane.errorIcon");
			availableWidth -= icon != null ? icon.getIconWidth() : 0;
		}
		return availableWidth / pane.getFontMetrics(UIManager.getFont("Label.font")).charWidth('W');
	}

	private static void showMessageDialog(Object message, String title, int messageType) throws HeadlessException {
		showMessageDialog(message, title, messageType, null);
	}

	private static void showMessageDialog(Object message, String title, int messageType, Icon icon) throws HeadlessException {
		showOptionDialog(FlexoFrame.getActiveFrame(), message, title, JOptionPane.DEFAULT_OPTION, messageType, icon, null, null);
	}

	private static int showConfirmDialog(Object message, String title, int optionType, int messageType) throws HeadlessException {
		return showConfirmDialog(message, title, optionType, messageType, null);
	}

	private static int showConfirmDialog(Object message, String title, int optionType, int messageType, Icon icon)
			throws HeadlessException {
		return showOptionDialog(FlexoFrame.getActiveFrame(), message, title, optionType, messageType, icon, null, null);
	}

	private static String showInputDialog(Object message, String title, int messageType) throws HeadlessException {
		return (String) showInputDialog(FlexoFrame.getActiveFrame(), message, title, messageType, null, null, null);
	}

	private static String showInputDialog(Component parentComponent, Object message, String title, int messageType)
			throws HeadlessException {
		return (String) showInputDialog(parentComponent, message, title, messageType, null, null, null);
	}

	private static Object showInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon,
			Object[] selectionValues, Object initialSelectionValue) throws HeadlessException {
		Object[] availableOptions = new Object[] { FlexoLocalization.getMainLocalizer().localizedForKey("OK"),
				FlexoLocalization.getMainLocalizer().localizedForKey("cancel") };
		JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, icon, availableOptions, availableOptions[0]);
		pane.setWantsInput(true);
		pane.setSelectionValues(selectionValues);
		pane.setInitialSelectionValue(initialSelectionValue);
		pane.setComponentOrientation((parentComponent == null ? FlexoFrame.getActiveFrame() : parentComponent).getComponentOrientation());
		pane.setMessageType(messageType);
		JDialog dialog = pane.createDialog(parentComponent, title);
		pane.selectInitialValue();

		dialog.validate();
		dialog.pack();
		if (parentComponent == null) {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation((dim.width - dialog.getSize().width) / 2, (dim.height - dialog.getSize().height) / 2);
		}

		dialog.setVisible(true);
		dialog.dispose();

		Object val = pane.getValue();

		for (int counter = 0, maxCounter = availableOptions.length; counter < maxCounter; counter++) {
			if (availableOptions[counter].equals(val)) {
				if (counter == 1) {
					return null;
				}
			}

		}

		Object value = pane.getInputValue();
		if (value == JOptionPane.UNINITIALIZED_VALUE) {
			return null;
		}
		return value;
	}

	public void switchToPerspective(FlexoPerspective perspective) {
		if (perspective != null) {
			perspective.willShow();
			controllerModel.setCurrentPerspective(perspective);
		}
	}

	public void switchToPerspective(FlexoNature<?> nature) {
	}

	/**
	 * Return current displayed object, assuming that current displayed view represents returned object (for example the process for WKF
	 * module)
	 * 
	 * @return the FlexoObject
	 */
	public FlexoObject getCurrentDisplayedObjectAsModuleView() {
		// logger.info("getCurrentModuleView()="+getCurrentModuleView());
		if (getCurrentModuleView() != null) {
			return getCurrentModuleView().getRepresentedObject();
		}
		return null;
	}

	public ModuleView<?> moduleViewForLocation(Location location, boolean createViewIfRequired) {
		if (location == null) {
			return null;
		}
		if (location.getObject() == null) {
			return null;
		}
		if (location.getPerspective() == null) {
			return null;
		}
		if (location.getEditor() == null) {
			return null;
		}

		Object lock = flexoFrame.getTreeLock();
		// Should only create view if I am the AWT event Thread
		//

		synchronized (lock) {

			ModuleView<?> moduleView = lookupViewForLocation(location);

			if (moduleView == null) {
				// if (createViewIfRequired && location.getPerspective().hasModuleViewForObject(location.getObject())) {
				if (createViewIfRequired && location.getPerspective().isRepresentableInModuleView(location.getObject())) {
					Progress.progress("load_module_view");
					moduleView = createModuleViewForMasterObjectAndPerspective(location.getMasterObject(), location.getPerspective(),
							location.isEditable());
					if (moduleView != null) {
						// Stores the newly created view
						getModuleViewsForPerspective(location.getPerspective()).put(location.getMasterObject(), moduleView);
						// viewsForLocation.put(location, moduleView);
						// locationsForView.put(moduleView, location);
						FlexoObject representedMasterObject = moduleView.getRepresentedObject();
						if (representedMasterObject == null) {
							if (logger.isLoggable(Level.WARNING)) {
								logger.warning(
										"Module view: " + moduleView.getClass().getName() + " does not return its represented object");
							}
							representedMasterObject = location.getObject();
						}
						manager.new PropertyChangeListenerRegistration(representedMasterObject.getDeletedProperty(), this,
								representedMasterObject);
						if (representedMasterObject instanceof FlexoProjectObject
								&& ((FlexoProjectObject) representedMasterObject).getProject() != null
								&& !manager.hasListener(ProjectClosedNotification.CLOSE, this,
										((FlexoProjectObject) representedMasterObject).getProject())) {
							manager.new PropertyChangeListenerRegistration(ProjectClosedNotification.CLOSE, this,
									((FlexoProjectObject) representedMasterObject).getProject());
						}
					}
				}
			}
			return moduleView;
		}
	}

	private Map<FlexoObject, ModuleView<?>> getModuleViewsForPerspective(FlexoPerspective perspective) {
		Map<FlexoObject, ModuleView<?>> returned = moduleViews.get(perspective);
		if (returned == null) {
			returned = new HashMap<FlexoObject, ModuleView<?>>();
			moduleViews.put(perspective, returned);
		}
		return returned;
	}

	/**
	 * Return the {@link ModuleView} used to represent supplied location (a specific {@link FlexoObject} in a given
	 * {@link FlexoPerspective})
	 * 
	 * @param location
	 * @return
	 */
	private ModuleView<?> lookupViewForLocation(Location location) {
		Map<FlexoObject, ModuleView<?>> moduleViewsForPerspective = getModuleViewsForPerspective(location.getPerspective());
		return moduleViewsForPerspective.get(location.getMasterObject());
	}

	/**
	 * Returns an initialized view (build and initialize a new one, or return the stored one) representing supplied object. An additional
	 * flag indicates if this view must be build if not already existent.
	 * 
	 * @param object
	 * @param createViewIfRequired
	 * @return an initialized ModuleView instance
	 */
	public ModuleView<?> moduleViewForObject(FlexoObject object, boolean createViewIfRequired) {
		Map<FlexoObject, ModuleView<?>> moduleViewsForPerspective = getModuleViewsForPerspective(getCurrentPerspective());
		return moduleViewsForPerspective.get(getCurrentPerspective().getRepresentableMasterObject(object));
	}

	/**
	 * Returns an initialized view (build and initialize a new one, or return the stored one) representing supplied object.If not already
	 * existent, build the view.
	 * 
	 * @param object
	 * @return an initialized ModuleView instance
	 */
	public ModuleView<?> moduleViewForObject(FlexoObject object) {
		return moduleViewForObject(object, true);
	}

	/**
	 * Creates a new view for supplied object, or null if this object is not representable in this module
	 * 
	 * @param object
	 * @param perspective
	 *            TODO
	 * @return a newly created and initialized ModuleView instance
	 */
	private static ModuleView<?> createModuleViewForMasterObjectAndPerspective(FlexoObject object, FlexoPerspective perspective,
			boolean editable) {
		if (perspective == null) {
			return null;
		}
		else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Creating module view for " + object + " in perspective " + perspective.getName()
						+ (editable ? " (editable)" : " (read-only)"));
			}
			return perspective.createModuleViewForMasterObject(object, editable);
		}
	}

	public boolean isEditable(Object object) {
		if (isDisposed()) {
			return false;
		}
		return /*!getModule().getModule().requireProject() ||*/!(object instanceof FlexoProjectObject)
				|| ((FlexoProjectObject) object).getProject() == getProject();
	}

	/**
	 * Sets supplied object to be the object beeing represented in current perspective Switch to the {@link ModuleView} representing master
	 * object when required
	 * 
	 * Does nothing if supplied object is not representable in current perspective
	 * 
	 * @param object
	 */
	public void setCurrentEditedObject(FlexoObject object) {
		getControllerModel().setCurrentObject(object);
	}

	/**
	 * Sets supplied object to be the object beeing represented in supplied perspective Switch to the {@link ModuleView} representing master
	 * object when required
	 * 
	 * Does nothing if supplied object is not representable in current perspective
	 * 
	 * @param object
	 */
	public void setCurrentEditedObject(FlexoObject object, FlexoPerspective perspective) {
		controllerModel.setCurrentPerspective(perspective);
		controllerModel.setCurrentObject(object);
	}

	/**
	 * Add perspective to this {@link FlexoController}
	 * 
	 * @param perspective
	 */
	public void addToPerspectives(FlexoPerspective perspective) {
		controllerModel.addToPerspectives(perspective);
	}

	/**
	 * Return currently displayed ModuleView
	 * 
	 * @return
	 */
	public ModuleView<?> getCurrentModuleView() {
		if (mainPane != null) {
			return mainPane.getModuleView();
		}
		return null;
	}

	/**
	 * Returns MainPane for this module
	 * 
	 * @return a FlexoMainPane instance
	 */
	public FlexoMainPane getMainPane() {
		return mainPane;
	}

	public boolean hasMainPane() {
		return mainPane != null;
	}

	/**
	 * Creates FlexoMainPane instance for this module.
	 * 
	 * @return a newly created FlexoMainPane instance
	 */
	protected abstract FlexoMainPane createMainPane();

	/*********
	 * VIEWS *
	 *********/

	/**
	 * Handle removing of supplied ModuleView from the control panel
	 * 
	 * @param aView
	 *            the view to remove
	 */
	public void removeModuleView(ModuleView<?> aView) {

		// System.out.println("removeModuleView for " + aView + " of " + aView.getClass());

		Location toBeRemoved = null;

		for (Location location : getControllerModel().getLocations()) {
			if (location.getPerspective() == aView.getPerspective() && (location.getMasterObject() == aView.getRepresentedObject()
					|| location.getObject() == aView.getRepresentedObject())) {
				toBeRemoved = location;
			}
		}

		Map<FlexoObject, ModuleView<?>> moduleViewsForPerspective = getModuleViewsForPerspective(aView.getPerspective());
		moduleViewsForPerspective.remove(aView.getRepresentedObject());

		if (toBeRemoved != null) {
			getControllerModel().removeFromLocations(toBeRemoved);
		}

		// What about the locations ???
		/*Collection<Location> locations = locationsForView.get(aView);
		if (locations != null) {
		for (Location location : new ArrayList<>(locations)) {
			viewsForLocation.remove(location);
			// Do not forget to remove location from ControllerModel !!!
			getControllerModel().removeFromLocations(location);
		}
		}
		locationsForView.removeAll(aView);*/
	}

	/**
	 * Returns all the views currently loaded.
	 * 
	 * @return all the views currently loaded.
	 */
	protected List<ModuleView<?>> getAllViews() {
		List<ModuleView<?>> returned = new ArrayList<>();
		for (FlexoPerspective flexoPerspective : moduleViews.keySet()) {
			returned.addAll(getModuleViewsForPerspective(flexoPerspective).values());
		}
		return returned;
	}

	@SuppressWarnings("unchecked")
	protected <T extends ModuleView<?>> Collection<T> getViews(final Class<T> klass) {
		return (Collection<T>) Collections2.filter(getAllViews(), new Predicate<ModuleView<?>>() {
			@Override
			public boolean apply(ModuleView<?> input) {
				return klass.isAssignableFrom(input.getClass());
			};
		});
	}

	/**
	 * Shows control panel
	 */
	public void showControlPanel() {
		if (mainPane != null) {
			mainPane.showControlPanel();
		}
	}

	/**
	 * Hides control panel
	 */
	public void hideControlPanel() {
		if (mainPane != null) {
			mainPane.hideControlPanel();
		}
	}

	public void updateRecentProjectMenu() {
		if (menuBar != null) {
			menuBar.getFileMenu(this).updateRecentProjectMenu();
		}
	}

	/**
	 * Returns a custom component to be added to control panel in main pane Default implementation returns null, override it when required
	 * 
	 * @return
	 */
	public static final JComponent getCustomActionPanel() {
		return null;
	}

	public final String getWindowTitleforObject(FlexoObject object) {
		if (getCurrentPerspective() != null) {
			return getCurrentPerspective().getWindowTitleforObject(object, this);
		}
		return object.toString();
	}

	public String getWindowTitle() {
		String projectTitle = /*getModule().getModule().requireProject() &&*/getProject() != null
				? " - " + getProject().getProjectName() + " - " + getProjectDirectory()
				: "";
		if (getCurrentModuleView() != null) {
			return getModule().getName() + " : " + getWindowTitleforObject(getCurrentDisplayedObjectAsModuleView()) + projectTitle;
		}
		else {
			if (getModule() == null) {
				return FlexoCst.BUSINESS_APPLICATION_VERSION_NAME + projectTitle;
			}
			else {
				return FlexoCst.BUSINESS_APPLICATION_VERSION_NAME + " - " + getModule().getName() + projectTitle;
			}
		}
	}

	public void cancelCurrentAction() {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Escape was pressed but the current controller does not do anything about it");
		}
	}

	public boolean isDisposed() {
		return disposed;
	}

	public void dispose() {

		getSelectionManager().deleteObserver(getModuleInspectorController());

		manager.delete();
		getApplicationContext().getGeneralPreferences().getPropertyChangeSupport().removePropertyChangeListener(this);
		mainPane.dispose();
		if (validationWindow != null && !validationWindow.isDisposed()) {
			validationWindow.dispose();
		}
		if (mainInspectorController != null) {
			mainInspectorController.delete();
		}
		for (ModuleView<?> view : new ArrayList<>(getAllViews())) {
			try {
				view.deleteModuleView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		registeredMenuBar.clear();
		/*if (PreferencesController.hasInstance()) {
			PreferencesController.instance().getPreferencesWindow().setVisible(false);
		}*/
		if (flexoFrame != null) {
			flexoFrame.disposeAll();
		}
		if (menuBar != null) {
			menuBar.dispose();
		}
		if (getEditor() instanceof InteractiveFlexoEditor) {
			((InteractiveFlexoEditor) getEditor()).unregisterControllerActionInitializer(getControllerActionInitializer());
		}
		controllerModel.delete();
		moduleViews.clear();
		disposed = true;
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange(DISPOSED, false, true);
		}
		setEditor(null);
		propertyChangeSupport = null;
		inspectorMenuBar = null;
		validationWindow = null;
		flexoFrame = null;
		mainPane = null;
		menuBar = null;
		module = null;
	}

	@Override
	protected void finalize() throws Throwable {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Finalizing controller " + getClass().getSimpleName());
		}
		super.finalize();
	}

	public boolean handleWSException(final Exception e) throws InterruptedException {
		if (!SwingUtilities.isEventDispatchThread()) {
			final Holder<Boolean> returned = new Holder<>();
			try {
				SwingUtilities.invokeAndWait(() -> returned.value = _handleWSException(e));
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				return false;
			}
			return returned.value != null && returned.value;
		}
		return _handleWSException(e);
	}

	private static boolean _handleWSException(Throwable e) {
		if (e instanceof RuntimeException && e.getCause() != null) {
			e = e.getCause();
		}
		if (e.getCause() instanceof TooManyFailedAttemptException) {
			throw (TooManyFailedAttemptException) e.getCause();
		}
		if (e.getCause() instanceof CancelException) {
			throw (CancelException) e.getCause();
		}
		if (logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, "An error ocurred " + (e.getMessage() == null ? "no message" : e.getMessage()), e);
		}
		if (e.getMessage() != null && e.getMessage().startsWith("redirect")) {
			String location = null;
			if (e.getMessage().indexOf("Location") > -1) {
				location = e.getMessage().substring(e.getMessage().indexOf("Location") + 9).trim();
			}
			FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_connect_to_web_sevice") + ": "
					+ FlexoLocalization.getMainLocalizer().localizedForKey("the_url_seems_incorrect")
					+ (location != null ? "\n" + FlexoLocalization.getMainLocalizer().localizedForKey("try_with_this_one") + " " + location
							: ""));
			return false;
		} /*
			if (e instanceof WebApplicationException) {
			WebApplicationException wae = (WebApplicationException) e;
			Object entity = wae.getResponse().getEntity();
			switch (wae.getResponse().getStatus()) {
			case 500:
				return FlexoController.confirm(FlexoLocalization.localizedForKey("webservice_remote_error") + entity + "\n"
						+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
			case 401:
				return FlexoController.confirm(FlexoLocalization.localizedForKey("unauthorized_action_on_the_server") + entity + "\n"
						+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
			default:
				if (entity != null) {
					return FlexoController.confirm(entity.toString() + "\n"
							+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
				} else {
					return FlexoController.confirm(FlexoLocalization.localizedForKey("unexpected_error_occured_while_connecting_to_server")
							+ "\n" + FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
				}
			}
			}
			*/
		if (e.getCause() instanceof ConnectException) {
			return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("connection_error")
					+ (e.getCause().getMessage() != null ? " (" + e.getCause().getMessage() + ")" : "") + "\n"
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e.getMessage() != null && "(500)Apple WebObjects".equals(e.getMessage())
				|| e.getMessage().startsWith("No such operation")) {
			return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_connect_to_web_sevice") + ": "
					+ FlexoLocalization.getMainLocalizer().localizedForKey("the_url_seems_incorrect") + "\n"
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e.toString() != null && e.toString().startsWith("javax.net.ssl.SSLHandshakeException")) {
			return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("connection_error") + ": " + e + "\n"
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e instanceof SocketTimeoutException) {
			return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("connection_timeout") + "\n"
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e instanceof IOException || e.getCause() instanceof IOException) {
			IOException ioEx = (IOException) (e instanceof IOException ? e : e.getCause());
			return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("connection_error") + ": "
					+ FlexoLocalization.getMainLocalizer().localizedForKey(ioEx.getClass().getSimpleName()) + " " + ioEx.getMessage() + "\n"
					+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
		}
		else {
			if (e.getMessage() != null && e.getMessage().indexOf("Content is not allowed in prolog") > -1) {
				FlexoController
						.notify("Check your connection url in FlexoPreferences > Advanced.\n It seems wrong.\nsee logs for details.");
				return false;
			}
			else {
				return FlexoController.confirm(FlexoLocalization.getMainLocalizer().localizedForKey("webservice_remote_error") + " \n"
						+ (e.getMessage() == null || "java.lang.NullPointerException".equals(e.getMessage())
								? "Check your connection parameters.\nThe service may be temporary unavailable."
								: e.getMessage())
						+ "\n" + FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_try_again?"));
			}
		}
		/*FlexoController.notify(FlexoLocalization.localizedForKey("webservice_connection_failed"));
		FlexoController.notify(FlexoLocalization.localizedForKey("webservice_authentification_failed") + ": "
				+ FlexoLocalization.localizedForKey(e.getMessage1()));*/
	}

	/**
	 * This method is used to specialize in a {@link FlexoModule} object beeing managed with the selection.
	 * 
	 * Generic behaviour is to manage an indirection with resources: resource data is used instead of resource if resource is loaded
	 * 
	 * {@link TechnologyAdapterPlugin} also provide this abstraction
	 * 
	 * @param object
	 * @return
	 */
	public FlexoObject getRelevantObject(FlexoObject object) {
		if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
			return (FlexoObject) ((FlexoResource<?>) object).getLoadedResourceData();
		}

		TechnologyAdapterControllerService tacService = getApplicationContext().getTechnologyAdapterControllerService();
		for (TechnologyAdapterPluginController<?> plugin : tacService.getActivatedPlugins()) {
			if (plugin.handleObject(object)) {
				return plugin.getRelevantObject(object);
			}
		}

		return object;
	}

	public void objectWasClicked(Object object) {
		// logger.info("Object was clicked: " + object);
		// logger.info("Current selection=" +
		// getSelectionManager().getSelection());
		if (getCurrentPerspective() != null) {
			if (object instanceof FlexoObject) {
				getCurrentPerspective().objectWasClicked(getRelevantObject((FlexoObject) object), this);
			}
			else {
				getCurrentPerspective().objectWasClicked(object, this);
			}
		}
	}

	public void objectWasRightClicked(Object object, FIBMouseEvent e) {
		// logger.info("Object was right-clicked: " + object + "event=" + e);
		if (object instanceof FlexoObject) {
			FlexoObject relevantObject = getRelevantObject((FlexoObject) object);
			getSelectionManager().getContextualMenuManager().showPopupMenuForObject(relevantObject, (Component) e.getSource(),
					e.getPoint());
		}
		if (getCurrentPerspective() != null) {
			if (object instanceof FlexoObject) {
				getCurrentPerspective().objectWasRightClicked(getRelevantObject((FlexoObject) object), this);
			}
			else {
				getCurrentPerspective().objectWasRightClicked(object, this);
			}
		}
	}

	public void objectWasDoubleClicked(Object object) {
		logger.info("Object was double-clicked: " + object);
		if (object instanceof FlexoResource<?>) {
			if (((FlexoResource<?>) object).isLoadable() && !((FlexoResource<?>) object).isLoaded()) {
				LoadResourceAction action = LoadResourceAction.actionType.makeNewAction((FlexoResource<?>) object, null, getEditor());
				action.doAction();
			}
			else {
				ResourceData<?> rd = ((FlexoResource<?>) object).getLoadedResourceData();
				if (rd instanceof FlexoObject) {
					selectAndFocusObjectAsTask((FlexoObject) rd);
				}
			}
		}
		if (getCurrentPerspective() != null) {
			if (object instanceof FlexoObject && getCurrentPerspective().isRepresentableInModuleView((FlexoObject) object)) {
				// Try to display object in view
				selectAndFocusObjectAsTask((FlexoObject) object);
			}
			if (object instanceof FlexoObject) {
				getCurrentPerspective().objectWasDoubleClicked(getRelevantObject((FlexoObject) object), this);
			}
			else {
				getCurrentPerspective().objectWasDoubleClicked(object, this);
			}
		}
	}

	public boolean displayInspectorTabForContext(String context) {
		// logger.info("Enquiring inspector tab display for context=" + context
		// + "... Answering NO");
		return false;
	}

	public void willLoad(Resource fibResource) {

		if (!getApplicationFIBLibraryService().componentIsLoaded(fibResource)) {
			Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("loading_component") + " " + fibResource);

			// FlexoProgress progress = ProgressWindow.makeProgressWindow(FlexoLocalization.localizedForKey("loading_interface..."), 3);
			// progress.setProgress("loading_component");
			getApplicationFIBLibraryService().retrieveFIBComponent(fibResource);
			// progress.setProgress("build_interface");
			// return progress;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getControllerModel()) {
			if (evt.getPropertyName().equals(ControllerModel.CURRENT_EDITOR)) {
				FlexoEditor oldEditor = (FlexoEditor) evt.getOldValue();
				FlexoEditor newEditor = (FlexoEditor) evt.getNewValue();
				if (oldEditor != null || newEditor != null) {
					updateEditor(oldEditor, newEditor);
				}
			}
			else if (evt.getPropertyName().equals(ControllerModel.LOCATIONS)) {
				if (evt.getOldValue() != null) {
					Location location = (Location) evt.getOldValue();
					ModuleView<?> moduleViewForLocation = moduleViewForLocation(location, false);
					// TODO: reimplement this
					logger.warning("Un truc a faire ici ???");
					/*if (moduleViewForLocation != null) {
						if (locationsForView.get(moduleViewForLocation).size() < 2) {
							moduleViewForLocation.deleteModuleView();
						}
						else {
							locationsForView.remove(moduleViewForLocation, location);
						}
					}*/
				}
			}
			else if (evt.getPropertyName().equals(ControllerModel.CURRENT_OBJECT)) {
				getSelectionManager().setSelectedObject(getControllerModel().getCurrentObject());
			}
		}
		else if (evt.getSource() instanceof FlexoProject && evt.getPropertyName().equals(ProjectClosedNotification.CLOSE)) {
			FlexoProject<?> project = (FlexoProject<?>) evt.getSource();
			for (ModuleView<?> view : new ArrayList<>(getAllViews())) {
				if (view.getRepresentedObject() instanceof FlexoProjectObject) {
					if (project.equals(((FlexoProjectObject) view.getRepresentedObject()).getProject())) {
						view.deleteModuleView();
					}
				}
			}
			manager.removeListener(ProjectClosedNotification.CLOSE, this, project);
		}
		else if (evt.getSource() == getApplicationContext().getGeneralPreferences()) {
			String key = evt.getPropertyName();
			if (GeneralPreferences.LANGUAGE_KEY.equals(key)) {
				getFlexoFrame().updateTitle();
			}
			else if (GeneralPreferences.LAST_OPENED_PROJECTS_1.equals(key) || GeneralPreferences.LAST_OPENED_PROJECTS_2.equals(key)
					|| GeneralPreferences.LAST_OPENED_PROJECTS_3.equals(key) || GeneralPreferences.LAST_OPENED_PROJECTS_4.equals(key)
					|| GeneralPreferences.LAST_OPENED_PROJECTS_5.equals(key)) {
				updateRecentProjectMenu();
			}
		}
	}

	/**
	 * Select the supplied object. Also try to select (create if not exists) a main view representing supplied object, if this view exists.
	 * <br>
	 * Try all to really display supplied object, even if required view is not the current displayed view
	 * 
	 * @param object
	 *            the object to focus on
	 */
	public void selectAndFocusObject(FlexoObject object) {
		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("select_and_focus") + " " + object);
		if (object instanceof FlexoProject) {
			getControllerModel().setCurrentProject((FlexoProject<?>) object);
		}
		else {
			setCurrentEditedObject(object);
		}
		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("selecting") + " " + object);
		getSelectionManager().setSelectedObject(object);
	}

	// Stores currently loading/selecting tasks
	private final Map<FlexoObject, SelectAndFocusObjectTask> selectAndFocusObjectTasks = new Hashtable<>();

	/**
	 * Select the supplied object in a dedicated task
	 * 
	 * @param object
	 *            the object to focus on
	 */
	public void selectAndFocusObjectAsTask(final FlexoObject object, FlexoTask... tasksToBeExecutedBefore) {
		if (selectAndFocusObjectTasks.get(object) == null) {
			SelectAndFocusObjectTask task = new SelectAndFocusObjectTask(this, object) {
				@Override
				protected synchronized void finishedExecution() {
					super.finishedExecution();
					selectAndFocusObjectTasks.remove(object);
				}
			};
			for (FlexoTask before : tasksToBeExecutedBefore) {
				task.addToDependantTasks(before);
			}
			selectAndFocusObjectTasks.put(object, task);
			getApplicationContext().getTaskManager().scheduleExecution(task);
		}
		else {
			logger.info("selectAndFocusObjectAsTask called for " + object
					+ " : will not proceed as this request has already been registered and beeing processed");
		}
	}

	public FlexoValidationModel getValidationModelForObject(FlexoObject object) {
		return null;
	}

	public final FlexoPerspective getCurrentPerspective() {
		return getControllerModel().getCurrentPerspective();
	}

	public FlexoPerspective getDefaultPerspective() {
		if (getControllerModel() != null && getControllerModel().getPerspectives() != null
				&& getControllerModel().getPerspectives().size() > 0) {
			return getControllerModel().getPerspectives().get(0);
		}
		return null;
	}

	public final FlexoEditor getEditor() {
		return getControllerModel().getCurrentEditor();
	}

	public final FlexoEditingContext getEditingContext() {
		if (getApplicationContext() != null) {
			return getApplicationContext().getEditingContext();
		}
		return null;
	}

	/**
	 * Return the technology-specific controller for supplied technology adapter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public static <TA extends TechnologyAdapter<TA>> TechnologyAdapterController<TA> getTechnologyAdapterController(TA technologyAdapter) {
		if (technologyAdapter != null) {
			FlexoServiceManager sm = technologyAdapter.getTechnologyAdapterService().getServiceManager();
			if (sm != null) {
				TechnologyAdapterControllerService service = sm.getService(TechnologyAdapterControllerService.class);
				if (service != null) {
					return service.getTechnologyAdapterController(technologyAdapter);
				}
			}
		}
		return null;
	}

	/**
	 * Return the technology-specific controller for supplied technology adapter class
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> TechnologyAdapterController<TA> getTechnologyAdapterController(
			Class<TA> technologyAdapterClass) {
		TechnologyAdapterService taService = getApplicationContext().getTechnologyAdapterService();
		TA ta = taService.getTechnologyAdapter(technologyAdapterClass);
		TechnologyAdapterControllerService tacService = getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(ta);
	}

	/**
	 * Return the technology-specific controller for supplied technology adapter class
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> TA getTechnologyAdapter(Class<TA> technologyAdapterClass) {
		TechnologyAdapterService taService = getApplicationContext().getTechnologyAdapterService();
		return taService.getTechnologyAdapter(technologyAdapterClass);
	}

	public ApplicationFIBLibraryService getApplicationFIBLibraryService() {
		return getApplicationContext().getApplicationFIBLibraryService();
	}

	// ================================================
	// ============ Icons management ==============
	// ================================================

	@NotificationUnsafe
	public ImageIcon iconForObject(Object object) {

		if (object instanceof String) {
			return null;
		}

		if (object instanceof ITechnologySpecificFlexoResourceFactory) {
			Class<? extends TechnologyAdapter> taClass = ((ITechnologySpecificFlexoResourceFactory<?, ?, ?>) object)
					.getTechnologyAdapterClass();
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(taClass);
			return tac.getIconForTechnologyObject(((ITechnologySpecificFlexoResourceFactory<?, ?, ?>) object).getResourceDataClass());
		}

		ImageIcon iconForObject = statelessIconForObject(object);
		if (iconForObject != null) {
			if (/*getModule().getModule().requireProject() &&*/object instanceof FlexoProjectObject && getProject() != null
					&& ((FlexoProjectObject) object).getProject() != getProject() && ((FlexoProjectObject) object).getProject() != null
					&& (!(object instanceof FlexoProject) || !getProjectLoader().getRootProjects().contains(object))) {
				iconForObject = IconFactory.getImageIcon(iconForObject, new IconMarker[] { IconLibrary.IMPORT });
			}
			else if (object instanceof FlexoProjectReference) {
				iconForObject = IconFactory.getImageIcon(iconForObject, new IconMarker[] { IconLibrary.IMPORT });
			}
		}
		return iconForObject;
	}

	public static <TA extends TechnologyAdapter<TA>> ImageIcon statelessIconForTechnologyObject(TechnologyObject<TA> object) {
		// prevent NPE
		if (object != null) {
			TechnologyAdapterController<?> tac;
			if (object instanceof ModelSlotObject) {
				tac = getTechnologyAdapterController(((ModelSlotObject<?>) object).getModelSlotTechnologyAdapter());
			}
			else {
				tac = getTechnologyAdapterController(object.getTechnologyAdapter());
			}

			if (tac != null) {
				return tac.getIconForTechnologyObject(object);
			}
			else {
				logger.warning("Could not find TechnologyAdapterController for technology " + object.getTechnologyAdapter());
			}
		}
		return null;
	}

	public static <TA extends TechnologyAdapter<TA>> ImageIcon statelessIconForTechnologyAdapterResource(
			TechnologyAdapterResource<?, TA> resource) {
		TechnologyAdapterController<TA> tac = getTechnologyAdapterController(resource.getTechnologyAdapter());
		if (tac != null) {
			return tac.getIconForTechnologyObject(resource.getResourceDataClass());
		}
		else {
			logger.warning("Could not find TechnologyAdapterController for technology "
					+ ((TechnologyAdapterResource<?, ?>) resource).getTechnologyAdapter());
		}
		return null;
	}

	public static ImageIcon statelessIconForObject(Object object) {

		if (object == null) {
			return null;
		}

		if (object instanceof FlexoServiceManager) {
			return IconLibrary.OPENFLEXO_NOTEXT_16;
		}

		if (object instanceof Module) {
			return ((Module<?>) object).getSmallIcon();
		}

		if (object instanceof FlexoActionCompoundEdit) {
			return IconLibrary.INFO_ICON;
		}

		else if (object instanceof FlexoAction) {
			return IconLibrary.INFO_ICON;
		}

		else if (object instanceof AtomicEdit) {
			ImageIcon baseIcon = IconLibrary.QUESTION_ICON;
			if (((AtomicEdit<?>) object).getModelFactory() instanceof PamelaResourceModelFactory) {
				baseIcon = statelessIconForObject(
						((PamelaResourceModelFactory<?>) ((AtomicEdit<?>) object).getModelFactory()).getResource());
			}
			if (object instanceof CreateCommand) {
				return IconFactory.getImageIcon(baseIcon, IconLibrary.DUPLICATE);
			}
			else if (object instanceof SetCommand) {
				return IconFactory.getImageIcon(baseIcon, IconLibrary.SYNC);
			}
			else if (object instanceof DeleteCommand) {
				return IconFactory.getImageIcon(baseIcon, IconLibrary.DELETE);
			}
			else if (object instanceof AddCommand) {
				return IconFactory.getImageIcon(baseIcon, IconLibrary.POSITIVE_MARKER);
			}
			else if (object instanceof RemoveCommand) {
				return IconFactory.getImageIcon(baseIcon, IconLibrary.NEGATIVE_MARKER);
			}
		}

		if (object instanceof ModelSlotEntry) {
			return FMLIconLibrary.iconForModelSlot(((ModelSlotEntry) object).getTechnologyAdapter());
		}
		else if (object instanceof ParentFlexoConceptEntry) {
			return FMLIconLibrary.FLEXO_CONCEPT_ICON;
		}
		else if (object instanceof BehaviourParameterEntry) {
			return FMLIconLibrary.FLEXO_CONCEPT_PARAMETER_ICON;
		}

		if (object instanceof FlexoProjectResource) {
			return IconLibrary.OPENFLEXO_NOTEXT_16;
		}

		// If object is a resource and if this resource is loaded, use icon of
		// loaded resource data
		if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
			return statelessIconForObject(((FlexoResource<?>) object).getLoadedResourceData());
		}
		else if (object instanceof CompilationUnitResource) {
			return FMLIconLibrary.FML_ICON;
		}
		else if (object instanceof FMLRTVirtualModelInstanceResource) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		}
		else if (object instanceof TechnologyAdapterResource<?, ?>) {
			return statelessIconForTechnologyAdapterResource((TechnologyAdapterResource<?, ?>) object);
		}
		else if (object instanceof ResourceManager) {
			return IconLibrary.INFORMATION_SPACE_ICON;
		}
		else if (object instanceof FlexoFacet) {
			return IconLibrary.FOLDER_ICON;
		}
		else if (object instanceof FlexoProject) {
			return IconLibrary.OPENFLEXO_NOTEXT_16;
		}
		else if (object instanceof FlexoPreferences) {
			return IconLibrary.OPENFLEXO_NOTEXT_16;
		}
		else if (object instanceof FlexoResourceCenter) {
			return IconLibrary.RESOURCE_CENTER_ICON;
		}
		else if (object instanceof FlexoResourceCenterService) {
			return IconLibrary.INFORMATION_SPACE_ICON;
		}
		else if (object instanceof TechnologyAdapterService) {
			return FMLIconLibrary.TECHNOLOGY_ADAPTER_ICON;
		}
		else if (object instanceof VirtualModelLibrary) {
			return FMLIconLibrary.VIRTUAL_MODEL_LIBRARY_ICON;
		}
		else if (object instanceof UseModelSlotDeclaration) {
			UseModelSlotDeclaration useDeclaration = (UseModelSlotDeclaration) object;
			if (useDeclaration.getCompilationUnit() != null && useDeclaration.getCompilationUnit().getTechnologyAdapterService() != null) {
				TechnologyAdapter modelSlotTA = useDeclaration.getCompilationUnit().getTechnologyAdapterService()
						.getTechnologyAdapterForModelSlot(useDeclaration.getModelSlotClass());
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(modelSlotTA);
				if (tac != null) {
					return IconFactory.getImageIcon(tac.getIconForModelSlot(useDeclaration.getModelSlotClass()), IconLibrary.IMPORT);
				}
			}
			if (useDeclaration.getModelSlotClass() != null) {
				if (useDeclaration.getModelSlotClass().equals(FMLModelSlot.class)) {
					return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_ICON, IconLibrary.IMPORT);
				}
				if (useDeclaration.getModelSlotClass().equals(FMLRTVirtualModelInstanceModelSlot.class)) {
					return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
				}
			}
			logger.warning("Could not find TechnologyAdapterController for technology " + object);
		}
		else if (object instanceof ElementImportDeclaration) {
			if (((ElementImportDeclaration) object).getReferencedObject() != null) {
				return IconFactory.getImageIcon(statelessIconForObject(((ElementImportDeclaration) object).getReferencedObject()),
						IconLibrary.IMPORT);
			}
			return IconFactory.getImageIcon(FMLIconLibrary.UNKNOWN_ICON, IconLibrary.IMPORT);
		}
		else if (object instanceof JavaImportDeclaration) {
			return FIBIconLibrary.PACKAGE_ICON;
		}
		else if (object instanceof FMLObject) {
			return FMLIconLibrary.iconForObject((FMLObject) object);
		}
		else if (object instanceof CompilationUnitResource) {
			return FMLIconLibrary.iconForObject((CompilationUnitResource) object);
		}
		else if (object instanceof FMLRTVirtualModelInstanceResource) {
			return FMLRTIconLibrary.iconForObject((FMLRTVirtualModelInstanceResource) object);
		}
		else if (object instanceof VirtualModelInstanceObject) {
			return FMLRTIconLibrary.iconForObject((VirtualModelInstanceObject) object);
		}
		else if (object instanceof RepositoryFolder) {
			if (((RepositoryFolder<?, ?>) object).isRootFolder()) {
				FlexoResourceCenter<?> rc = ((RepositoryFolder<?, ?>) object).getResourceRepository().getResourceCenter();
				if (rc.getDelegatingProjectResource() != null) {
					// This is the delegate RC of a FlexoProject
					return IconLibrary.OPENFLEXO_NOTEXT_16;
				}
				else {
					return statelessIconForObject(rc);
				}
			}
			return IconLibrary.FOLDER_ICON;
		}
		else if (object instanceof TechnologyAdapter) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController((TechnologyAdapter) object);
			if (tac != null) {
				return tac.getTechnologyIcon();
			}
			else {
				logger.warning("Could not find TechnologyAdapterController for technology " + object);
			}
		}
		else if (object instanceof FlexoModel<?, ?>) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoModel<?, ?>) object).getTechnologyAdapter());
			if (tac != null) {
				return tac.getModelIcon();
			}
		}
		else if (object instanceof FlexoModelResource<?, ?, ?, ?>) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(
					(TechnologyAdapter) ((FlexoModelResource<?, ?, ?, ?>) object).getTechnologyAdapter());
			if (tac != null) {
				return tac.getModelIcon();
			}
		}
		else if (object instanceof FlexoMetaModel<?>) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoMetaModel<?>) object).getTechnologyAdapter());
			if (tac != null) {
				return tac.getMetaModelIcon();
			}
		}
		else if (object instanceof FlexoMetaModelResource<?, ?, ?>) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(
					(TechnologyAdapter) ((FlexoMetaModelResource<?, ?, ?>) object).getTechnologyAdapter());
			if (tac != null) {
				return tac.getMetaModelIcon();
			}
		}
		else if (object instanceof FlexoProjectReference) {
			return IconLibrary.OPENFLEXO_NOTEXT_16;
		}

		// If object is a TechnologyObject, we delegate this to the right
		// TechnologyAdapterController
		if (object instanceof TechnologyObject<?>) {
			return statelessIconForTechnologyObject((TechnologyObject<?>) object);
		}

		logger.warning("Sorry, no icon defined for " + object + " " + (object != null ? object.getClass() : ""));
		return null;
	}

	// ================================================
	// ============ Resources management ==============
	// ================================================

	public void saveModifiedResources() {
		System.out.println("registered resources: " + getApplicationContext().getResourceManager().getRegisteredResources().size() + " : "
				+ getApplicationContext().getResourceManager().getRegisteredResources());
		System.out.println("loaded resources: " + getApplicationContext().getResourceManager().getLoadedResources().size() + " : "
				+ getApplicationContext().getResourceManager().getLoadedResources());
		System.out.println("unsaved resources: " + getApplicationContext().getResourceManager().getUnsavedResources().size() + " : "
				+ getApplicationContext().getResourceManager().getUnsavedResources());
		System.out.println("TODO: implement this");
	}

	public boolean reviewModifiedResources() {
		System.out.println("reviewModifiedResources()");
		/*ResourceSavingInfo savingInfo = getResourceSavingInfo();
		savingInfo.update();
		for (ResourceSavingEntryInfo e : savingInfo.getEntries()) {
			System.out.println(" > " + e + " resource=" + e.getName() + " type=" + e.getType() + " save=" + e.saveThisResource());
		}*/

		ReviewUnsavedDialog dialog = new ReviewUnsavedDialog(getApplicationContext(), getApplicationContext().getResourceManager());
		dialog.showDialog();

		// FIBDialog<ResourceSavingInfo> dialog =
		// FIBDialog.instanciateAndShowDialog(CommonFIB.REVIEW_UNSAVED_DIALOG_FIB,
		// savingInfo,
		// FlexoFrame.getActiveFrame(), true,
		// FlexoLocalization.getMainLocalizer());
		if (dialog.getStatus() == Status.VALIDATED) {
			try {
				dialog.saveSelection();
			} catch (SaveResourcePermissionDeniedException e) {
				e.printStackTrace();
			} catch (SaveResourceExceptionList e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	private String infoMessage;
	private String tempInfoMessage;
	private int temporaryThreadCount = 0;
	private final List<JLabel> infoLabels = new ArrayList<>();

	public JLabel makeInfoLabel() {
		JLabel returned = new JLabel(getInfoMessage());
		returned.setFont(FlexoCst.SMALL_FONT);
		infoLabels.add(returned);
		return returned;
	}

	public String getInfoMessage() {
		if (tempInfoMessage != null) {
			return tempInfoMessage;
		}
		if (infoMessage == null) {
			return getModule().getName();
		}
		return infoMessage;
	}

	public void setInfoMessage(String infoMessage) {
		setInfoMessage(infoMessage, false);
	}

	public void setInfoMessage(final String infoMessage, boolean temporary) {
		final String oldInfoMessage = this.infoMessage;
		if (!infoMessage.equals(oldInfoMessage)) {

			if (temporary) {
				this.tempInfoMessage = infoMessage;
				final Thread t = new Thread(() -> {
					// Unused String localTempInfoMessage = infoMessage;
					// System.out.println("START " + localTempInfoMessage + " temporaryThreadCount=" + temporaryThreadCount);
					try {
						Thread.sleep(FlexoCst.TEMPORARY_MESSAGE_PERSISTENCY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					temporaryThreadCount--;
					// System.out.println("END " + tempInfoMessage + " temporaryThreadCount=" + temporaryThreadCount);
					if (temporaryThreadCount == 0) {
						// System.out.println("Back to " + infoMessage);
						tempInfoMessage = null;
						for (JLabel label : infoLabels) {
							// System.out.println("Setting again label to " + getInfoMessage());
							label.setText(getInfoMessage());
						}
					}
				});
				temporaryThreadCount++;
				t.start();
			}
			else {
				// System.out.println("REALLY set infoMessage to " + infoMessage);
				this.infoMessage = infoMessage;
			}
			for (JLabel label : infoLabels) {
				label.setText(getInfoMessage());
			}
		}
	}

	/**
	 * Hook called before execution of supplied FlexoBehaviourAction
	 * 
	 * @param action
	 */
	public void willExecute(FlexoBehaviourAction<?, ?, ?> action) {
	}

	/**
	 * Hook called after execution of supplied FlexoBehaviourAction
	 * 
	 * @param action
	 */
	public void hasExecuted(FlexoBehaviourAction<?, ?, ?> action) {
	}

	/**
	 * Return the flexo locales (general locales for Openflexo application)
	 * 
	 * @return
	 */
	public LocalizedDelegate getFlexoLocales() {
		if (getApplicationContext() != null) {
			return getApplicationContext().getLocalizationService().getFlexoLocalizer();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	/**
	 * Return the locales relative to the module of this FlexoController
	 * 
	 * @return
	 */
	public LocalizedDelegate getModuleLocales() {
		if (getModule() != null) {
			return getModule().getLocales();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	public abstract ModuleView<?> makeWelcomePanel(WelcomePanel<?> welcomePanel, FlexoPerspective perspective);

	public abstract ModuleView<?> makeDefaultProjectView(FlexoProject<?> project, FlexoPerspective perspective);
}
