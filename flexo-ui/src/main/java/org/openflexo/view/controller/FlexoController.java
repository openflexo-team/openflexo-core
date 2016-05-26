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
import java.io.File;
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
import java.util.Map.Entry;
import java.util.Vector;
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
import org.openflexo.components.ProgressWindow;
import org.openflexo.components.ReviewUnsavedDialog;
import org.openflexo.components.validation.ValidationWindow;
import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.editor.SelectAndFocusObjectTask;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.ProjectData;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.action.LoadResourceAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoFacet;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept.ParentFlexoConceptEntry;
import org.openflexo.foundation.fml.action.AbstractCreateVirtualModel.ModelSlotEntry;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.ViewObject;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
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
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBMouseEvent;
import org.openflexo.gina.swing.editor.ComponentValidationWindow;
import org.openflexo.gina.swing.utils.localization.LocalizedEditor;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.IconMarker;
import org.openflexo.inspector.ModuleInspectorController;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.undo.AddCommand;
import org.openflexo.model.undo.AtomicEdit;
import org.openflexo.model.undo.CreateCommand;
import org.openflexo.model.undo.DeleteCommand;
import org.openflexo.model.undo.RemoveCommand;
import org.openflexo.model.undo.SetCommand;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationRuleFilter;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.prefs.ApplicationFIBLibraryService;
import org.openflexo.prefs.FlexoPreferences;
import org.openflexo.prefs.GeneralPreferences;
import org.openflexo.project.InteractiveProjectLoader;
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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;

/**
 * General controller managing an application module (see {@link FlexoModule}).<br>
 * 
 * 
 * @author benoit, sylvain
 */
public abstract class FlexoController implements PropertyChangeListener, HasPropertyChangeSupport {

	static final Logger logger = Logger.getLogger(FlexoController.class.getPackage().getName());

	public static final String DISPOSED = "disposed";
	public static final String EDITOR = "editor";

	private PropertyChangeSupport propertyChangeSupport;

	private boolean disposed = false;

	private final Map<Location, ModuleView<?>> viewsForLocation;
	private final Multimap<ModuleView<?>, Location> locationsForView;

	private LocalizedEditor mainLocalizedEditor;
	private ValidationWindow validationWindow;

	protected FlexoModule<?> module;
	protected FlexoMenuBar menuBar;
	protected MouseSelectionManager selectionManager;
	private final ControllerActionInitializer controllerActionInitializer;

	protected FlexoFrame flexoFrame;
	private FlexoMainPane mainPane;
	private final ControllerModel controllerModel;
	private final List<FlexoMenuBar> registeredMenuBar = new ArrayList<FlexoMenuBar>();
	private ModuleInspectorController mainInspectorController;
	protected PropertyChangeListenerRegistrationManager manager = new PropertyChangeListenerRegistrationManager();

	private FIBTechnologyBrowser<FMLRTTechnologyAdapter> sharedFMLRTBrowser;
	private FIBTechnologyBrowser<FMLTechnologyAdapter> sharedFMLBrowser;

	/**
	 * Constructor
	 */
	protected FlexoController(FlexoModule module) {
		super();
		// ProgressWindow.setProgressInstance(FlexoLocalization.localizedForKey("init_module_controller"));

		Progress.progress(FlexoLocalization.localizedForKey("init_module_controller"));

		this.module = module;
		locationsForView = ArrayListMultimap.create();
		viewsForLocation = new HashMap<Location, ModuleView<?>>();
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

		Progress.progress(FlexoLocalization.localizedForKey("init_inspectors"));
		initInspectors();

		Progress.progress(FlexoLocalization.localizedForKey("init_perspectives"));
		initializePerspectives();

		if (getApplicationContext().getGeneralPreferences() != null) {
			getApplicationContext().getGeneralPreferences().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		// controllerActionInitializer = createControllerActionInitializer();
		// registerShortcuts(controllerActionInitializer);

		// if (getModule().getModule().requireProject()) {
		if (getModuleLoader().getLastActiveEditor() != null) {
			controllerModel.setCurrentEditor(getModuleLoader().getLastActiveEditor());
			// }
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
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract void initializePerspectives();

	protected void initializeAllAvailableTechnologyPerspectives(boolean includeFML, boolean includeFMLRT) {
		for (TechnologyAdapter ta : getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(ta);
			if (tac != null) {
				boolean includeTA = true;
				if (tac.getTechnologyAdapter() instanceof FMLTechnologyAdapter) {
					includeTA = includeFML;
				}
				if (tac.getTechnologyAdapter() instanceof FMLRTTechnologyAdapter) {
					includeTA = includeFMLRT;
				}
				if (includeTA) {
					tac.installTechnologyPerspective(this);
				}
			}
		}
	}

	/**
	 * Initialized technology perspective for FML technology adapter
	 */
	protected void initializeFMLTechnologyPerspective() {
		initializeTechnologyPerspective(getFMLTechnologyAdapter());
	}

	/**
	 * Initialized technology perspective for FML@RT technology adapter
	 */
	protected void initializeFMLRTTechnologyPerspective() {
		initializeTechnologyPerspective(getFMLRTTechnologyAdapter());
	}

	/**
	 * Initialize technology perspective for supplied technology adapter
	 * 
	 * @param ta
	 */
	private void initializeTechnologyPerspective(TechnologyAdapter ta) {
		TechnologyAdapterController<?> tac = getTechnologyAdapterController(ta);
		if (tac != null) {
			tac.installTechnologyPerspective(this);
		}
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

	/**
	 * Install all perspectives related to {@link FMLTechnologyAdapter}<br>
	 * We install generic perspective, and we iterate on each technology adapter to install technology-specific natures<br>
	 * Note that all those perspective must share the same browser (see {@link #getSharedFMLBrowser()}).<br>
	 * 
	 */
	protected void initializeFMLTechnologyAdapterPerspectives() {
		// We first install generic perspective
		TechnologyPerspective<FMLTechnologyAdapter> genericPerspective = getFMLTechnologyAdapterController().getTechnologyPerspectives()
				.get(this);
		if (genericPerspective == null) {
			// We do not use generic code to retrieve the browser, because we want to use the same
			// browser for all perspectives, so we have to override the creation of this browser
			genericPerspective = new TechnologyPerspective<FMLTechnologyAdapter>(getFMLTechnologyAdapter(), this) {
				@Override
				protected FIBTechnologyBrowser<FMLTechnologyAdapter> makeTechnologyBrowser() {
					return getSharedFMLBrowser();
				}
			};
		}
		addToPerspectives(genericPerspective);

		// getFMLTechnologyAdapterController().installTechnologyPerspective(this);

		// Then we iterate on all technology adapters
		for (TechnologyAdapter ta : getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			if (tac != null) {
				tac.installFMLNatureSpecificPerspectives(this);
			}
			else {
				logger.warning("Could not load TechnologyAdapterController for " + ta);
			}
		}
	}

	/**
	 * Install all perspectives related to {@link FMLRTTechnologyAdapter}<br>
	 * We install generic perspective, and we iterate on each technology adapter to install technology-specific natures<br>
	 * Note that all those perspective must share the same browser (see {@link #getSharedFMLRTBrowser()}).<br>
	 * 
	 */
	protected void initializeFMLRTTechnologyAdapterPerspectives() {

		// We first install generic perspective
		TechnologyPerspective<FMLRTTechnologyAdapter> genericPerspective = getFMLRTTechnologyAdapterController().getTechnologyPerspectives()
				.get(this);
		if (genericPerspective == null) {
			// We do not use generic code to retrieve the browser, because we want to use the same
			// browser for all perspectives, so we have to override the creation of this browser
			genericPerspective = new TechnologyPerspective<FMLRTTechnologyAdapter>(getFMLRTTechnologyAdapter(), this) {
				@Override
				protected FIBTechnologyBrowser<FMLRTTechnologyAdapter> makeTechnologyBrowser() {
					return getSharedFMLRTBrowser();
				}
			};
		}
		addToPerspectives(genericPerspective);

		// Then we iterate on all technology adapters
		for (TechnologyAdapter ta : getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(ta);
			if (tac != null) {
				tac.installFMLRTNatureSpecificPerspectives(this);
			}
			else {
				logger.warning("Could not load TechnologyAdapterController for " + ta);
			}
		}
	}

	public FIBTechnologyBrowser<FMLRTTechnologyAdapter> getSharedFMLRTBrowser() {
		if (sharedFMLRTBrowser == null) {
			sharedFMLRTBrowser = getFMLRTTechnologyAdapterController().makeTechnologyBrowser(this);
		}
		return sharedFMLRTBrowser;
	}

	public FIBTechnologyBrowser<FMLTechnologyAdapter> getSharedFMLBrowser() {
		if (sharedFMLBrowser == null) {
			sharedFMLBrowser = getFMLTechnologyAdapterController().makeTechnologyBrowser(this);
		}
		return sharedFMLBrowser;
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

	public final InteractiveProjectLoader getProjectLoader() {
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
	public ControllerActionInitializer createControllerActionInitializer() {
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
		moduleInspectorGroup = loadInspectorGroup(getModule().getShortName().toUpperCase(), getCoreInspectorGroup());
		getSelectionManager().addObserver(getModuleInspectorController());
	}

	public ModuleInspectorController getModuleInspectorController() {
		if (mainInspectorController == null) {
			mainInspectorController = new ModuleInspectorController(this);
		}
		return mainInspectorController;
	}

	public InspectorGroup loadInspectorGroup(String inspectorGroup, InspectorGroup... parentInspectorGroups) {
		// TODO : To be optimized
		Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/" + inspectorGroup);
		return getModuleInspectorController().loadDirectory(inspectorsDir, parentInspectorGroups);
	}

	public FlexoFrame getFlexoFrame() {
		return flexoFrame;
	}

	public FlexoModule getModule() {
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

	public abstract FlexoObject getDefaultObjectToSelect(FlexoProject project);

	public FlexoProject getProject() {
		if (getEditor() != null) {
			return getEditor().getProject();
		}
		return null;
	}

	public File getProjectDirectory() {
		return getProject().getProjectDirectory();
	}

	private FlexoMenuBar inspectorMenuBar;

	public FlexoMenuBar getInspectorMenuBar() {
		if (inspectorMenuBar == null) {
			inspectorMenuBar = createAndRegisterNewMenuBar();
		}
		return inspectorMenuBar;
	}

	public static void showError(String msg) throws HeadlessException {
		showError(FlexoLocalization.localizedForKey("error"), msg);
	}

	public static void showError(String title, String msg) throws HeadlessException {
		showMessageDialog(msg, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void notify(String msg) throws HeadlessException {
		showMessageDialog(msg, FlexoLocalization.localizedForKey("confirmation"), JOptionPane.INFORMATION_MESSAGE);
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
		return showConfirmDialog(msg, FlexoLocalization.localizedForKey("information"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}

	public static boolean confirmWithWarning(String msg) throws HeadlessException {
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("information"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
				new Object[] { FlexoLocalization.localizedForKey("yes"), FlexoLocalization.localizedForKey("no") },
				FlexoLocalization.localizedForKey("no")) == JOptionPane.YES_OPTION;
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
		return showInputDialog(msg, FlexoLocalization.localizedForKey("information"), JOptionPane.QUESTION_MESSAGE);
	}

	public static String askForString(Component parentComponent, String msg) throws HeadlessException {
		return showInputDialog(parentComponent, msg, FlexoLocalization.localizedForKey("information"), JOptionPane.OK_CANCEL_OPTION);
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
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("confirmation"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialOption);
	}

	public static int selectOption(String msg, String initialOption, String... options) {
		return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("confirmation"),
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

	/*public PreferencesWindow getPreferencesWindow(boolean create) {
		return PreferencesController.instance().getPreferencesWindow(create);
	}
	
	public void showPreferences() {
		PreferencesController.instance().showPreferences();
	}*/

	public void registerShortcuts(ControllerActionInitializer controllerInitializer) {
		for (final Entry<FlexoActionType<?, ?, ?>, ActionInitializer<?, ?, ?>> entry : controllerInitializer.getActionInitializers()
				.entrySet()) {
			KeyStroke accelerator = entry.getValue().getShortcut();
			if (accelerator != null) {
				registerActionForKeyStroke(new AbstractAction() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("actionPerformed() with " + entry.getKey());
						FlexoObject focusedObject = getSelectionManager().getFocusedObject();
						Vector<FlexoObject> globalSelection = getSelectionManager().getSelection();
						FlexoActionType actionType = entry.getKey();
						if (TypeUtils.isAssignableTo(focusedObject, actionType.getFocusedObjectType()) && (globalSelection == null
								|| TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
							getEditor().performActionType(actionType, focusedObject, globalSelection, e);
						}
					}
				}, accelerator, entry.getKey().getUnlocalizedName());
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
			class CompoundAction extends AbstractAction {

				private final List<Action> actions = new ArrayList<Action>();

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
				public boolean accept(ValidationRule rule) {
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
		if (parentComponent == null) {
			if (ProgressWindow.hasInstance()) {
				parentComponent = ProgressWindow.instance();
			}
		}
		final Component parent = parentComponent;
		JOptionPane pane = null;
		boolean isLocalized = false;
		Object[] availableOptions = null;
		if (optionType == JOptionPane.OK_CANCEL_OPTION && options == null) {
			availableOptions = new Object[] { FlexoLocalization.localizedForKey("OK"), FlexoLocalization.localizedForKey("cancel") };
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
			availableOptions = new Object[] { FlexoLocalization.localizedForKey("yes"), FlexoLocalization.localizedForKey("no") };
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
			availableOptions = new Object[] { FlexoLocalization.localizedForKey("yes"), FlexoLocalization.localizedForKey("no"),
					FlexoLocalization.localizedForKey("cancel") };
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
		if (parentComponent == null) {
			if (ProgressWindow.hasInstance()) {
				parentComponent = ProgressWindow.instance();
			}
		}
		Object[] availableOptions = new Object[] { FlexoLocalization.localizedForKey("OK"), FlexoLocalization.localizedForKey("cancel") };
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
				if (createViewIfRequired && location.getPerspective().hasModuleViewForObject(location.getObject())) {
					Progress.progress("load_module_view");
					moduleView = createModuleViewForObjectAndPerspective(location.getObject(), location.getPerspective(),
							location.isEditable());
					if (moduleView != null) {
						viewsForLocation.put(location, moduleView);
						locationsForView.put(moduleView, location);

						FlexoObject representedObject = moduleView.getRepresentedObject();
						if (representedObject == null) {
							if (logger.isLoggable(Level.WARNING)) {
								logger.warning(
										"Module view: " + moduleView.getClass().getName() + " does not return its represented object");
							}
							representedObject = location.getObject();
						}
						manager.new PropertyChangeListenerRegistration(representedObject.getDeletedProperty(), this, representedObject);
						if (representedObject instanceof FlexoProjectObject && ((FlexoProjectObject) representedObject).getProject() != null
								&& !manager.hasListener(ProjectClosedNotification.CLOSE, this,
										((FlexoProjectObject) representedObject).getProject())) {
							manager.new PropertyChangeListenerRegistration(ProjectClosedNotification.CLOSE, this,
									((FlexoProjectObject) representedObject).getProject());
						}
					}
				}
			}
			return moduleView;
		}
	}

	private ModuleView<?> lookupViewForLocation(Location location) {
		for (Entry<Location, ModuleView<?>> e : viewsForLocation.entrySet()) {
			Location l = e.getKey();
			if (l.getObject().equals(location.getObject()) && l.getPerspective().equals(location.getPerspective())
					&& l.isEditable() == location.isEditable()) {
				return e.getValue();
			}
		}

		return null;
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
		return moduleViewForLocation(new Location(controllerModel.getCurrentEditor(), object, getCurrentPerspective()),
				createViewIfRequired);
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
	private ModuleView<?> createModuleViewForObjectAndPerspective(FlexoObject object, FlexoPerspective perspective, boolean editable) {
		if (perspective == null) {
			return null;
		}
		else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Creating module view for " + object + " in perspective " + perspective.getName()
						+ (editable ? " (editable)" : " (read-only)"));
			}
			return perspective.createModuleViewForObject(object, editable);
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
	 * Sets supplied object to be the main object represented as the current view for this module (for example the process for WKF module).
	 * Does nothing if supplied object is not representable in this module
	 * 
	 * @param object
	 * @return an initialized ModuleView instance
	 */
	@Deprecated
	public final void setObjectAsModuleView(Object object) {
		// This hack is introduced to support double click in imported workflow
		// tree.
		// This should be removed and imported wofklow tree should be updated to
		// support casting
		if (object instanceof FlexoObject) {
			setCurrentEditedObjectAsModuleView((FlexoObject) object);
		}
	}

	/**
	 * Sets supplied object to be the main object represented as the current view for this module (for example the process for WKF module).
	 * Does nothing if supplied object is not representable in this module
	 * 
	 * @param object
	 * @return an initialized ModuleView instance
	 */
	public void setCurrentEditedObjectAsModuleView(FlexoObject object) {
		getControllerModel().setCurrentObject(object);
	}

	/**
	 * Sets supplied object to be the main object represented as the current view for this module (for example the process for WKF module)
	 * and supplied perspective. Does nothing if supplied object is not representable in this module
	 * 
	 * @param object
	 * @return an initialized ModuleView instance
	 */
	public void setCurrentEditedObjectAsModuleView(FlexoObject object, FlexoPerspective perspective) {
		controllerModel.setCurrentPerspective(perspective);
		controllerModel.setCurrentObject(object);
	}

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
		Collection<Location> locations = locationsForView.get(aView);
		if (locations != null) {
			for (Location location : locations) {
				viewsForLocation.remove(location);
				// Do not forget to remove location from ControllerModel !!!
				getControllerModel().removeFromLocations(location);
			}
		}
		locationsForView.removeAll(aView);
	}

	/**
	 * Returns all the views currently loaded.
	 * 
	 * @return all the views currently loaded.
	 */
	protected Collection<ModuleView<?>> getViews() {
		return viewsForLocation.values();
	}

	@SuppressWarnings("unchecked")
	protected <T extends ModuleView<?>> Collection<T> getViews(final Class<T> klass) {
		return (Collection<T>) Collections2.filter(viewsForLocation.values(), new Predicate<ModuleView<?>>() {
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
	public final JComponent getCustomActionPanel() {
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
				? " - " + getProject().getProjectName() + " - " + getProjectDirectory().getAbsolutePath() : "";
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
		for (ModuleView<?> view : new ArrayList<ModuleView<?>>(getViews())) {
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
		viewsForLocation.clear();
		locationsForView.clear();
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
			final Holder<Boolean> returned = new Holder<Boolean>();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						returned.value = _handleWSException(e);
					}
				});
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				return false;
			}
			return returned.value != null && returned.value;
		}
		return _handleWSException(e);
	}

	private boolean _handleWSException(Throwable e) {
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
			FlexoController.notify(FlexoLocalization.localizedForKey("could_not_connect_to_web_sevice") + ": "
					+ FlexoLocalization.localizedForKey("the_url_seems_incorrect")
					+ (location != null ? "\n" + FlexoLocalization.localizedForKey("try_with_this_one") + " " + location : ""));
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
			return FlexoController.confirm(FlexoLocalization.localizedForKey("connection_error")
					+ (e.getCause().getMessage() != null ? " (" + e.getCause().getMessage() + ")" : "") + "\n"
					+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e.getMessage() != null && "(500)Apple WebObjects".equals(e.getMessage())
				|| e.getMessage().startsWith("No such operation")) {
			return FlexoController.confirm(FlexoLocalization.localizedForKey("could_not_connect_to_web_sevice") + ": "
					+ FlexoLocalization.localizedForKey("the_url_seems_incorrect") + "\n"
					+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e.toString() != null && e.toString().startsWith("javax.net.ssl.SSLHandshakeException")) {
			return FlexoController.confirm(FlexoLocalization.localizedForKey("connection_error") + ": " + e + "\n"
					+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e instanceof SocketTimeoutException) {
			return FlexoController.confirm(FlexoLocalization.localizedForKey("connection_timeout") + "\n"
					+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
		}
		else if (e instanceof IOException || e.getCause() instanceof IOException) {
			IOException ioEx = (IOException) (e instanceof IOException ? e : e.getCause());
			return FlexoController.confirm(FlexoLocalization.localizedForKey("connection_error") + ": "
					+ FlexoLocalization.localizedForKey(ioEx.getClass().getSimpleName()) + " " + ioEx.getMessage() + "\n"
					+ FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
		}
		else {
			if (e.getMessage() != null && e.getMessage().indexOf("Content is not allowed in prolog") > -1) {
				FlexoController
						.notify("Check your connection url in FlexoPreferences > Advanced.\n It seems wrong.\nsee logs for details.");
				return false;
			}
			else {
				return FlexoController.confirm(FlexoLocalization.localizedForKey("webservice_remote_error") + " \n"
						+ (e.getMessage() == null || "java.lang.NullPointerException".equals(e.getMessage())
								? "Check your connection parameters.\nThe service may be temporary unavailable." : e.getMessage())
						+ "\n" + FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
			}
		}
		/*FlexoController.notify(FlexoLocalization.localizedForKey("webservice_connection_failed"));
		FlexoController.notify(FlexoLocalization.localizedForKey("webservice_authentification_failed") + ": "
				+ FlexoLocalization.localizedForKey(e.getMessage1()));*/
	}

	/**
	 * We manage here an indirection with resources: resource data is used instead of resource if resource is loaded
	 * 
	 * @param object
	 * @return
	 */
	private FlexoObject getRelevantObject(FlexoObject object) {
		/*if (object instanceof FlexoResource<?>) {
			logger.info("Resource " + object + " loaded=" + ((FlexoResource<?>) object).isLoaded());
		}*/
		if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
			return (FlexoObject) ((FlexoResource<?>) object).getLoadedResourceData();
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
			// FlexoObject resourceData = null;
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
		if (object instanceof FlexoObject && getCurrentPerspective().hasModuleViewForObject((FlexoObject) object)) {
			// Try to display object in view
			selectAndFocusObjectAsTask((FlexoObject) object);
		}
		if (getCurrentPerspective() != null) {
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

	/*
	 * File moved to Resource
	public FlexoProgress willLoad(File fibFile) {
	
	
		if (!FIBLibrary.instance().componentIsLoaded(fibFile)) {
			FlexoProgress progress = ProgressWindow.makeProgressWindow(FlexoLocalization.localizedForKey("loading_interface..."), 3);
			progress.setProgress("loading_component");
			FIBLibrary.instance().retrieveFIBComponent(fibFile);
			progress.setProgress("build_interface");
			return progress;
		}
		return null;
	}*/

	public FlexoProgress willLoad(Resource fibResource) {

		if (!getApplicationFIBLibraryService().componentIsLoaded(fibResource)) {
			Progress.progress(FlexoLocalization.localizedForKey("loading_component") + " " + fibResource);

			FlexoProgress progress = ProgressWindow.makeProgressWindow(FlexoLocalization.localizedForKey("loading_interface..."), 3);
			// progress.setProgress("loading_component");
			getApplicationFIBLibraryService().retrieveFIBComponent(fibResource);
			// progress.setProgress("build_interface");
			// return progress;
			return null;
		}
		return null;
	}

	/*
	public FlexoProgress willLoad(String fibResourcePath) {
	
		if (!FIBLibrary.instance().componentIsLoaded(fibResourcePath)) {
			FlexoProgress progress = ProgressWindow.makeProgressWindow(FlexoLocalization.localizedForKey("loading_interface..."), 3);
			progress.setProgress("loading_component");
			FIBLibrary.instance().retrieveFIBComponent(fibResourcePath);
			progress.setProgress("build_interface");
			return progress;
		}
		return null;
	}
	 */

	// toto

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
					if (moduleViewForLocation != null) {
						if (locationsForView.get(moduleViewForLocation).size() < 2) {
							moduleViewForLocation.deleteModuleView();
						}
						else {
							locationsForView.remove(moduleViewForLocation, location);
						}
					}
				}
			}
			else if (evt.getPropertyName().equals(ControllerModel.CURRENT_OBJECT)) {
				getSelectionManager().setSelectedObject(getControllerModel().getCurrentObject());
			}
		}
		else if (evt.getSource() instanceof FlexoProject && evt.getPropertyName().equals(ProjectClosedNotification.CLOSE)) {
			FlexoProject project = (FlexoProject) evt.getSource();
			for (ModuleView<?> view : new ArrayList<ModuleView>(getViews())) {
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
		Progress.progress(FlexoLocalization.localizedForKey("select_and_focus") + " " + object);
		if (object instanceof FlexoProject) {
			getControllerModel().setCurrentProject((FlexoProject) object);
		}
		else {
			setCurrentEditedObjectAsModuleView(object);
		}
		Progress.progress(FlexoLocalization.localizedForKey("selecting") + " " + object);
		getSelectionManager().setSelectedObject(object);
	}

	// Stores currently loading/selecting tasks
	private final Map<FlexoObject, SelectAndFocusObjectTask> selectAndFocusObjectTasks = new Hashtable<FlexoObject, SelectAndFocusObjectTask>();

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
	public static <TA extends TechnologyAdapter> TechnologyAdapterController<TA> getTechnologyAdapterController(TA technologyAdapter) {
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
	public <TA extends TechnologyAdapter> TechnologyAdapterController<TA> getTechnologyAdapterController(Class<TA> technologyAdapterClass) {
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
	public <TA extends TechnologyAdapter> TA getTechnologyAdapter(Class<TA> technologyAdapterClass) {
		TechnologyAdapterService taService = getApplicationContext().getTechnologyAdapterService();
		return taService.getTechnologyAdapter(technologyAdapterClass);
	}

	public ApplicationFIBLibraryService getApplicationFIBLibraryService() {
		return getApplicationContext().getApplicationFIBLibraryService();
	}

	// ================================================
	// ============ Icons management ==============
	// ================================================

	public ImageIcon iconForObject(Object object) {
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

	public static <TA extends TechnologyAdapter> ImageIcon statelessIconForTechnologyObject(TechnologyObject<TA> object) {
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

	public static <TA extends TechnologyAdapter> ImageIcon statelessIconForTechnologyAdapterResource(
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

		if (object instanceof FlexoActionCompoundEdit) {
			return IconLibrary.INFO_ICON;
		}

		else if (object instanceof FlexoAction) {
			return IconLibrary.INFO_ICON;
		}

		else if (object instanceof AtomicEdit) {
			ImageIcon baseIcon = IconLibrary.QUESTION_ICON;
			if (((AtomicEdit) object).getModelFactory() instanceof PamelaResourceModelFactory) {
				baseIcon = statelessIconForObject(((PamelaResourceModelFactory) ((AtomicEdit) object).getModelFactory()).getResource());
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

		// If object is a resource and if this resource is loaded, use icon of
		// loaded resource data
		if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
			return statelessIconForObject(((FlexoResource<?>) object).getLoadedResourceData());
		}
		else if (object instanceof ViewResource) {
			return FMLRTIconLibrary.VIEW_ICON;
		}
		else if (object instanceof VirtualModelInstanceResource) {
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
		else if (object instanceof ProjectData) {
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
		else if (object instanceof ViewPointLibrary) {
			return FMLIconLibrary.VIEWPOINT_LIBRARY_ICON;
		}
		else if (object instanceof FMLObject) {
			return FMLIconLibrary.iconForObject((FMLObject) object);
		}
		else if (object instanceof ViewPointResource) {
			return FMLIconLibrary.iconForObject((ViewPointResource) object);
		}
		else if (object instanceof VirtualModelResource) {
			return FMLIconLibrary.iconForObject((VirtualModelResource) object);
		}
		else if (object instanceof ViewResource) {
			return FMLRTIconLibrary.iconForObject((ViewResource) object);
		}
		else if (object instanceof VirtualModelInstanceResource) {
			return FMLRTIconLibrary.iconForObject((VirtualModelInstanceResource) object);
		}
		else if (object instanceof ViewLibrary) {
			return FMLRTIconLibrary.VIEW_LIBRARY_ICON;
		}
		else if (object instanceof ViewObject) {
			return FMLRTIconLibrary.iconForObject((ViewObject) object);
		}
		else if (object instanceof RepositoryFolder) {
			if (((RepositoryFolder) object).isRootFolder()) {
				return statelessIconForObject(((RepositoryFolder) object).getResourceRepository().getResourceCenter());
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
					((FlexoModelResource<?, ?, ?, ?>) object).getTechnologyAdapter());
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
					((FlexoMetaModelResource<?, ?, ?>) object).getTechnologyAdapter());
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
				dialog.saveSelection(getEditor().getFlexoProgressFactory());
			} catch (SaveResourcePermissionDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SaveResourceExceptionList e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	private String infoMessage;
	private String tempInfoMessage;
	private int temporaryThreadCount = 0;
	private final List<JLabel> infoLabels = new ArrayList<JLabel>();

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
				final Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						String localTempInfoMessage = infoMessage;
						// System.out.println("START " + localTempInfoMessage + " temporaryThreadCount=" + temporaryThreadCount);
						try {
							Thread.sleep(FlexoCst.TEMPORARY_MESSAGE_PERSISTENCY);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
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

}
