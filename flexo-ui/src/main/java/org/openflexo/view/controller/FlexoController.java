/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2013-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
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
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.openflexo.GeneralPreferences;
import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.components.ProgressWindow;
import org.openflexo.components.ReviewUnsavedDialog;
import org.openflexo.components.validation.ConsistencyCheckDialog;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.ProjectData;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ProjectClosedNotification;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.foundation.validation.Validable;
import org.openflexo.foundation.validation.ValidationModel;
import org.openflexo.foundation.validation.ValidationRule;
import org.openflexo.foundation.validation.ValidationRuleSet;
import org.openflexo.foundation.view.ViewLibrary;
import org.openflexo.foundation.view.ViewObject;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.viewpoint.FlexoFacet;
import org.openflexo.foundation.viewpoint.ViewPointLibrary;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.IconMarker;
import org.openflexo.icon.VEIconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.inspector.ModuleInspectorController;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.undo.AddCommand;
import org.openflexo.model.undo.AtomicEdit;
import org.openflexo.model.undo.CreateCommand;
import org.openflexo.model.undo.DeleteCommand;
import org.openflexo.model.undo.RemoveCommand;
import org.openflexo.model.undo.SetCommand;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ProjectLoader;
import org.openflexo.prefs.FlexoPreferences;
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

// import javax.ws.rs.WebApplicationException;

/**
 * Abstract controller defined for an application module
 * 
 * @author benoit, sylvain
 */
public abstract class FlexoController implements PropertyChangeListener, HasPropertyChangeSupport {

    static final Logger                                 logger            = Logger.getLogger(FlexoController.class.getPackage().getName());

    public static final String                          DISPOSED          = "disposed";
    public static final String                          EDITOR            = "editor";

    private PropertyChangeSupport                       propertyChangeSupport;

    private boolean                                     disposed          = false;

    private final Map<Location, ModuleView<?>>          viewsForLocation;

    private final Multimap<ModuleView<?>, Location>     locationsForView;

    private ConsistencyCheckDialog                      consistencyCheckWindow;

    protected FlexoModule                               module;

    protected FlexoMenuBar                              menuBar;

    protected MouseSelectionManager                     selectionManager;

    private final ControllerActionInitializer           controllerActionInitializer;

    protected FlexoFrame                                flexoFrame;

    private FlexoMainPane                               mainPane;

    private final ControllerModel                       controllerModel;

    private final List<FlexoMenuBar>                    registeredMenuBar = new ArrayList<FlexoMenuBar>();

    private ModuleInspectorController                   mainInspectorController;

    protected PropertyChangeListenerRegistrationManager manager           = new PropertyChangeListenerRegistrationManager();

    /**
     * Constructor
     */
    protected FlexoController(final FlexoModule module) {
        super();
        ProgressWindow.setProgressInstance(FlexoLocalization.localizedForKey("init_module_controller"));
        this.module = module;
        this.locationsForView = ArrayListMultimap.create();
        this.viewsForLocation = new HashMap<Location, ModuleView<?>>();
        this.controllerModel = new ControllerModel(module.getApplicationContext(), module);
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.manager.new PropertyChangeListenerRegistration(this, this.controllerModel);
        this.flexoFrame = this.createFrame();

        this.controllerActionInitializer = this.createControllerActionInitializer();
        this.registerShortcuts(this.controllerActionInitializer);

        this.menuBar = this.createAndRegisterNewMenuBar();
        this.selectionManager = this.createSelectionManager();
        this.flexoFrame.setJMenuBar(this.menuBar);
        this.flexoFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        this.flexoFrame.getRootPane().getActionMap().put("escape", new AbstractAction() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                FlexoController.this.cancelCurrentAction();
            }
        });
        this.mainPane = this.createMainPane();
        this.getFlexoFrame().getContentPane().add(this.mainPane, BorderLayout.CENTER);
        ((JComponent) this.getFlexoFrame().getContentPane()).revalidate();
        this.initInspectors();
        this.initializePerspectives();

        if (this.getApplicationContext().getGeneralPreferences() != null) {
            this.getApplicationContext().getGeneralPreferences().getPropertyChangeSupport().addPropertyChangeListener(this);
        }

        // controllerActionInitializer = createControllerActionInitializer();
        // registerShortcuts(controllerActionInitializer);

        // if (getModule().getModule().requireProject()) {
        if (this.getModuleLoader().getLastActiveEditor() != null) {
            this.controllerModel.setCurrentEditor(this.getModuleLoader().getLastActiveEditor());
            // }
        }
        else {
            this.controllerModel.setCurrentEditor(this.getApplicationContext().getApplicationEditor());
        }

    }

    @Override
    public PropertyChangeSupport getPropertyChangeSupport() {
        return this.propertyChangeSupport;
    }

    @Override
    public String getDeletedProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract void initializePerspectives();

    public final ControllerModel getControllerModel() {
        return this.controllerModel;
    }

    protected abstract MouseSelectionManager createSelectionManager();

    /**
     * Creates a new instance of MenuBar for the module this controller refers
     * to
     * 
     * @return
     */
    protected abstract FlexoMenuBar createNewMenuBar();

    protected FlexoFrame createFrame() {
        return new FlexoFrame(this);
    }

    public final MouseSelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    public final FlexoMenuBar getMenuBar() {
        return this.menuBar;
    }

    public final ApplicationContext getApplicationContext() {
        if (this.getModule() != null) {
            return this.getModule().getApplicationContext();
        }
        return null;
    }

    public final ProjectLoader getProjectLoader() {
        return this.getApplicationContext().getProjectLoader();
    }

    public final ModuleLoader getModuleLoader() {
        return this.getApplicationContext().getModuleLoader();
    }

    /**
     * Creates a new instance of MenuBar for the module this controller refers
     * to
     * 
     * @return
     */
    public ControllerActionInitializer createControllerActionInitializer() {
        return new ControllerActionInitializer(this);
    }

    public ControllerActionInitializer getControllerActionInitializer() {
        return this.controllerActionInitializer;
    }

    /**
     * Creates and register a new instance of MenuBar for the module this
     * controller refers to
     * 
     * @return
     */
    public final FlexoMenuBar createAndRegisterNewMenuBar() {
        final FlexoMenuBar returned = this.createNewMenuBar();
        this.registeredMenuBar.add(returned);
        if (this.getFlexoFrame() != null) {
            for (final FlexoRelativeWindow next : this.getFlexoFrame().getRelativeWindows()) {
                returned.getWindowMenu().addFlexoRelativeWindowMenu(next);
            }
        }
        return returned;
    }

    public void notifyNewFlexoRelativeWindow(final FlexoRelativeWindow w) {
        for (final FlexoMenuBar next : this.registeredMenuBar) {
            next.getWindowMenu().addFlexoRelativeWindowMenu(w);
        }
    }

    public void notifyRemoveFlexoRelativeWindow(final FlexoRelativeWindow w) {
        for (final FlexoMenuBar next : this.registeredMenuBar) {
            next.getWindowMenu().removeFlexoRelativeWindowMenu(w);
        }
    }

    public void notifyRenameFlexoRelativeWindow(final FlexoRelativeWindow w, final String title) {
        for (final FlexoMenuBar next : this.registeredMenuBar) {
            next.getWindowMenu().renameFlexoRelativeWindowMenu(w, title);
        }
    }

    /**
	 *
	 */
    public void initInspectors() {
        this.loadInspectorGroup(this.getModule().getShortName().toUpperCase());
        this.getSelectionManager().addObserver(this.getModuleInspectorController());
    }

    public ModuleInspectorController getModuleInspectorController() {
        if (this.mainInspectorController == null) {
            this.mainInspectorController = new ModuleInspectorController(this);
        }
        return this.mainInspectorController;
    }

    protected void loadInspectorGroup(final String inspectorGroup) {
        // TODO : To be optimized
        final Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/" + inspectorGroup);
        this.getModuleInspectorController().loadDirectory(inspectorsDir);
    }

    public FlexoFrame getFlexoFrame() {
        return this.flexoFrame;
    }

    public FlexoModule getModule() {
        return this.module;
    }

    protected final void setEditor(final FlexoEditor editor) {
        this.controllerModel.setCurrentEditor(editor);
    }

    protected void updateEditor(final FlexoEditor from, final FlexoEditor to) {
        if (from instanceof InteractiveFlexoEditor) {
            ((InteractiveFlexoEditor) from).unregisterControllerActionInitializer(this.getControllerActionInitializer());
        }
        if (to instanceof InteractiveFlexoEditor) {
            ((InteractiveFlexoEditor) to).registerControllerActionInitializer(this.getControllerActionInitializer());
        }
        this.getPropertyChangeSupport().firePropertyChange(EDITOR, from, to);
    }

    public abstract FlexoObject getDefaultObjectToSelect(FlexoProject project);

    public FlexoProject getProject() {
        if (this.getEditor() != null) {
            return this.getEditor().getProject();
        }
        return null;
    }

    public File getProjectDirectory() {
        return this.getProject().getProjectDirectory();
    }

    private FlexoMenuBar inspectorMenuBar;

    public FlexoMenuBar getInspectorMenuBar() {
        if (this.inspectorMenuBar == null) {
            this.inspectorMenuBar = this.createAndRegisterNewMenuBar();
        }
        return this.inspectorMenuBar;
    }

    public static void showError(final String msg) throws HeadlessException {
        showError(FlexoLocalization.localizedForKey("error"), msg);
    }

    public static void showError(final String title, final String msg) throws HeadlessException {
        showMessageDialog(msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void notify(final String msg) throws HeadlessException {
        showMessageDialog(msg, FlexoLocalization.localizedForKey("confirmation"), JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean notifyWithCheckbox(final String title, final String msg, final String checkboxText, final boolean defaultValue) {
        final JPanel root = new JPanel(new BorderLayout());
        final Icon msgIcon = UIManager.getDefaults().getIcon("OptionPane.informationIcon");
        final JLabel notifyIcon = new JLabel(msgIcon);
        notifyIcon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        final JLabel label = new JLabel(msg);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        final JCheckBox box = new JCheckBox(checkboxText, defaultValue);
        box.setBorder(BorderFactory.createEmptyBorder(1, 20, 10, 10));
        final JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(label, BorderLayout.NORTH);
        centerPanel.add(box, BorderLayout.SOUTH);
        final JButton ok = new JButton("ok"/*FlexoLocalization.localizedForKey("ok")*/);
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
            public void actionPerformed(final ActionEvent e) {
                dialog.dispose();
            }

        });
        dialog.setResizable(false);
        dialog.add(root);
        dialog.pack();
        dialog.setVisible(true);
        return box.isSelected();
    }

    public static int ask(final String msg) throws HeadlessException {
        return showConfirmDialog(msg, FlexoLocalization.localizedForKey("information"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    public static boolean confirmWithWarning(final String msg) throws HeadlessException {
        return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("information"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] { FlexoLocalization.localizedForKey("yes"),
                        FlexoLocalization.localizedForKey("no") }, FlexoLocalization.localizedForKey("no")) == JOptionPane.YES_OPTION;
    }

    public static boolean confirm(final String msg) throws HeadlessException {
        return ask(msg) == JOptionPane.YES_OPTION;
    }

    public static int confirmYesNoCancel(final String localizedMessage) {
        return showOptionDialog(FlexoFrame.getActiveFrame(), localizedMessage, localizedMessage, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
    }

    public static String askForString(final String msg) throws HeadlessException {
        return showInputDialog(msg, FlexoLocalization.localizedForKey("information"), JOptionPane.QUESTION_MESSAGE);
    }

    public static String askForString(final Component parentComponent, final String msg) throws HeadlessException {
        return showInputDialog(parentComponent, msg, FlexoLocalization.localizedForKey("information"), JOptionPane.OK_CANCEL_OPTION);
    }

    public static String askForStringMatchingPattern(final String msg, final Pattern pattern, final String localizedPattern) {
        String result = askForString(msg);
        while (result != null && !pattern.matcher(result).matches()) {
            notify(localizedPattern);
            result = askForString(msg);
        }
        return result;
    }

    public static int selectOption(final String msg, final String[] options, final String initialOption) {
        return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("confirmation"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialOption);
    }

    public static int selectOption(final String msg, final String initialOption, final String... options) {
        return showOptionDialog(FlexoFrame.getActiveFrame(), msg, FlexoLocalization.localizedForKey("confirmation"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, initialOption);
    }

    public void showInspector() {
        this.getModuleInspectorController().getInspectorDialog().setVisible(true);
    }

    public void hideInspector() {
        this.getModuleInspectorController().getInspectorDialog().setVisible(false);
        ;
    }

    public void resetInspector() {
        this.getModuleInspectorController().resetInspector();
    }

    /*public PreferencesWindow getPreferencesWindow(boolean create) {
    	return PreferencesController.instance().getPreferencesWindow(create);
    }

    public void showPreferences() {
    	PreferencesController.instance().showPreferences();
    }*/

    public void registerShortcuts(final ControllerActionInitializer controllerInitializer) {
        for (final Entry<FlexoActionType<?, ?, ?>, ActionInitializer<?, ?, ?>> entry : controllerInitializer.getActionInitializers()
                .entrySet()) {
            final KeyStroke accelerator = entry.getValue().getShortcut();
            if (accelerator != null) {
                this.registerActionForKeyStroke(new AbstractAction() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        System.out.println("actionPerformed() with " + entry.getKey());
                        final FlexoObject focusedObject = FlexoController.this.getSelectionManager().getFocusedObject();
                        final Vector<FlexoObject> globalSelection = FlexoController.this.getSelectionManager().getSelection();
                        final FlexoActionType actionType = entry.getKey();
                        if (TypeUtils.isAssignableTo(focusedObject, actionType.getFocusedObjectType())
                                && (globalSelection == null || TypeUtils.isAssignableTo(globalSelection,
                                        actionType.getGlobalSelectionType()))) {
                            FlexoController.this.getEditor().performActionType(actionType, focusedObject, globalSelection, e);
                        }
                    }
                }, accelerator, entry.getKey().getUnlocalizedName());
            }
        }
    }

    public void registerActionForKeyStroke(AbstractAction action, final KeyStroke accelerator, final String actionName) {
        String key = actionName;
        final Object object = this.getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(accelerator);
        Action action2 = null;
        if (object != null) {
            action2 = this.getFlexoFrame().getRootPane().getActionMap().get(object);
        }
        if (action2 != null) {
            class CompoundAction extends AbstractAction {

                private final List<Action> actions = new ArrayList<Action>();

                void addToAction(final Action action) {
                    this.actions.add(action);
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    for (final Action action : this.actions) {
                        action.actionPerformed(e);
                    }
                }
            }
            if (action2 instanceof CompoundAction) {
                ((CompoundAction) action2).addToAction(action);
                return;
            }
            else {
                final CompoundAction compoundAction = new CompoundAction();
                compoundAction.addToAction(action2);
                compoundAction.addToAction(action);
                action = compoundAction;
                key = "compound-" + accelerator.toString();
            }
        }
        this.getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(accelerator, key);
        this.getFlexoFrame().getRootPane().getActionMap().put(key, action);
        if (accelerator.getKeyCode() == FlexoCst.DELETE_KEY_CODE) {
            final int keyCode = FlexoCst.BACKSPACE_DELETE_KEY_CODE;
            this.getFlexoFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, accelerator.getModifiers()), key);
        }
    }

    public ConsistencyCheckDialog getConsistencyCheckWindow() {
        return this.getConsistencyCheckWindow(true);
    }

    public ConsistencyCheckDialog getConsistencyCheckWindow(final boolean create) {
        if (create && this.getDefaultValidationModel() != null) {
            if (this.consistencyCheckWindow == null || this.consistencyCheckWindow.isDisposed()) {
                this.consistencyCheckWindow = new ConsistencyCheckDialog(this);
            }
        }
        return this.consistencyCheckWindow;
    }

    public void consistencyCheck(final Validable objectToValidate) {
        if (this.getDefaultValidationModel() != null) {
            this.initializeValidationModel();
            this.getConsistencyCheckWindow(true).setVisible(true);
            this.getConsistencyCheckWindow(true).consistencyCheck(objectToValidate);
        }
    }

    public void initializeValidationModel() {
        final ValidationModel validationModel = this.getDefaultValidationModel();
        if (validationModel != null) {
            for (int i = 0; i < validationModel.getSize(); i++) {
                final ValidationRuleSet ruleSet = validationModel.getElementAt(i);
                for (final ValidationRule<?, ?> rule : ruleSet.getRules()) {
                    rule.setIsEnabled(this.getApplicationContext().getGeneralPreferences().isValidationRuleEnabled(rule));
                }
            }
        }
    }

    /**
     * Brings up a dialog with a specified icon, where the initial choice is
     * determined by the <code>initialValue</code> parameter and the number of
     * choices is determined by the <code>optionType</code> parameter.
     * <p>
     * If <code>optionType</code> is <code>YES_NO_OPTION</code>, or
     * <code>YES_NO_CANCEL_OPTION</code> and the <code>options</code> parameter
     * is <code>null</code>, then the options are supplied by the look and feel.
     * <p>
     * The <code>messageType</code> parameter is primarily used to supply a
     * default icon from the look and feel.
     * 
     * @param parentComponent
     *            determines the <code>Frame</code> in which the dialog is
     *            displayed; if <code>null</code>, or if the
     *            <code>parentComponent</code> has no <code>Frame</code>, a
     *            default <code>Frame</code> is used
     * @param message
     *            the <code>Object</code> to display
     * @param title
     *            the title string for the dialog
     * @param optionType
     *            an integer designating the options available on the dialog:
     *            <code>YES_NO_OPTION</code>, or
     *            <code>YES_NO_CANCEL_OPTION</code>
     * @param messageType
     *            an integer designating the kind of message this is, primarily
     *            used to determine the icon from the pluggable Look and Feel:
     *            <code>ERROR_MESSAGE</code>, <code>INFORMATION_MESSAGE</code>,
     *            <code>WARNING_MESSAGE</code>, <code>QUESTION_MESSAGE</code>,
     *            or <code>PLAIN_MESSAGE</code>
     * @param icon
     *            the icon to display in the dialog
     * @param options
     *            an array of objects indicating the possible choices the user
     *            can make; if the objects are components, they are rendered
     *            properly; non-<code>String</code> objects are rendered using
     *            their <code>toString</code> methods; if this parameter is
     *            <code>null</code>, the options are determined by the Look and
     *            Feel
     * @param initialValue
     *            the object that represents the default selection for the
     *            dialog; only meaningful if <code>options</code> is used; can
     *            be <code>null</code>
     * @return an integer indicating the option chosen by the user, or
     *         <code>CLOSED_OPTION</code> if the user closed the dialog
     * @exception HeadlessException
     *                if <code>GraphicsEnvironment.isHeadless</code> returns
     *                <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private static synchronized int showOptionDialog(Component parentComponent, final Object message, final String title,
            final int optionType, final int messageType, final Icon icon, final Object[] options, final Object initialValue)
            throws HeadlessException {
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
        final Container content = dialog.getContentPane();
        final JScrollPane scroll = new JScrollPane(content, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dialog.setContentPane(scroll);
        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
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
            maxDim = new Dimension(Math.min(dialog.getWidth(), window.getGraphicsConfiguration().getDevice().getDefaultConfiguration()
                    .getBounds().width), Math.min(dialog.getHeight(), window.getGraphicsConfiguration().getDevice()
                    .getDefaultConfiguration().getBounds().height));
        }
        else {
            maxDim = new Dimension(Math.min(dialog.getWidth(), Toolkit.getDefaultToolkit().getScreenSize().width), Math.min(
                    dialog.getHeight(), Toolkit.getDefaultToolkit().getScreenSize().height));
        }
        dialog.setSize(maxDim);
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
        // pane.selectInitialValue();
        dialog.dispose();
        final Object selectedValue = pane.getValue();

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

    protected static int getMaxCharactersPerLine(final Component parent, final JOptionPane pane) {
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
            final Icon icon = UIManager.getIcon("OptionPane.errorIcon");
            availableWidth -= icon != null ? icon.getIconWidth() : 0;
        }
        return availableWidth / pane.getFontMetrics(UIManager.getFont("Label.font")).charWidth('W');
    }

    private static void showMessageDialog(final Object message, final String title, final int messageType) throws HeadlessException {
        showMessageDialog(message, title, messageType, null);
    }

    private static void showMessageDialog(final Object message, final String title, final int messageType, final Icon icon)
            throws HeadlessException {
        showOptionDialog(FlexoFrame.getActiveFrame(), message, title, JOptionPane.DEFAULT_OPTION, messageType, icon, null, null);
    }

    private static int showConfirmDialog(final Object message, final String title, final int optionType, final int messageType)
            throws HeadlessException {
        return showConfirmDialog(message, title, optionType, messageType, null);
    }

    private static int showConfirmDialog(final Object message, final String title, final int optionType, final int messageType,
            final Icon icon) throws HeadlessException {
        return showOptionDialog(FlexoFrame.getActiveFrame(), message, title, optionType, messageType, icon, null, null);
    }

    private static String showInputDialog(final Object message, final String title, final int messageType) throws HeadlessException {
        return (String) showInputDialog(FlexoFrame.getActiveFrame(), message, title, messageType, null, null, null);
    }

    private static String showInputDialog(final Component parentComponent, final Object message, final String title, final int messageType)
            throws HeadlessException {
        return (String) showInputDialog(parentComponent, message, title, messageType, null, null, null);
    }

    private static Object showInputDialog(Component parentComponent, final Object message, final String title, final int messageType,
            final Icon icon, final Object[] selectionValues, final Object initialSelectionValue) throws HeadlessException {
        if (parentComponent == null) {
            if (ProgressWindow.hasInstance()) {
                parentComponent = ProgressWindow.instance();
            }
        }
        final Object[] availableOptions = new Object[] { FlexoLocalization.localizedForKey("OK"),
                FlexoLocalization.localizedForKey("cancel") };
        final JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, icon, availableOptions,
                availableOptions[0]);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);
        pane.setComponentOrientation((parentComponent == null ? FlexoFrame.getActiveFrame() : parentComponent).getComponentOrientation());
        pane.setMessageType(messageType);
        final JDialog dialog = pane.createDialog(parentComponent, title);
        pane.selectInitialValue();

        dialog.validate();
        dialog.pack();
        if (parentComponent == null) {
            final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation((dim.width - dialog.getSize().width) / 2, (dim.height - dialog.getSize().height) / 2);
        }

        dialog.setVisible(true);
        dialog.dispose();

        final Object val = pane.getValue();

        for (int counter = 0, maxCounter = availableOptions.length; counter < maxCounter; counter++) {
            if (availableOptions[counter].equals(val)) {
                if (counter == 1) {
                    return null;
                }
            }

        }

        final Object value = pane.getInputValue();
        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return value;
    }

    public void switchToPerspective(final FlexoPerspective perspective) {
        this.controllerModel.setCurrentPerspective(perspective);
    }

    /**
     * Return current displayed object, assuming that current displayed view
     * represents returned object (for example the process for WKF module)
     * 
     * @return the FlexoObject
     */
    public FlexoObject getCurrentDisplayedObjectAsModuleView() {
        // logger.info("getCurrentModuleView()="+getCurrentModuleView());
        if (this.getCurrentModuleView() != null) {
            return this.getCurrentModuleView().getRepresentedObject();
        }
        return null;
    }

    public ModuleView<?> moduleViewForLocation(final Location location, final boolean createViewIfRequired) {
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
        ModuleView<?> moduleView = this.viewsForLocation.get(location);
        if (moduleView == null) {
            moduleView = this.lookupViewForLocation(location);
            if (createViewIfRequired && location.getPerspective().hasModuleViewForObject(location.getObject())) {
                moduleView = this.createModuleViewForObjectAndPerspective(location.getObject(), location.getPerspective(),
                        location.isEditable());
                if (moduleView != null) {
                    FlexoObject representedObject = moduleView.getRepresentedObject();
                    if (representedObject == null) {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.warning("Module view: " + moduleView.getClass().getName() + " does not return its represented object");
                        }
                        representedObject = location.getObject();
                    }
                    this.manager.new PropertyChangeListenerRegistration(representedObject.getDeletedProperty(), this, representedObject);
                    if (representedObject instanceof FlexoProjectObject
                            && ((FlexoProjectObject) representedObject).getProject() != null
                            && !this.manager.hasListener(ProjectClosedNotification.CLOSE, this,
                                    ((FlexoProjectObject) representedObject).getProject())) {
                        this.manager.new PropertyChangeListenerRegistration(ProjectClosedNotification.CLOSE, this,
                                ((FlexoProjectObject) representedObject).getProject());
                    }
                    this.viewsForLocation.put(location, moduleView);
                    this.locationsForView.put(moduleView, location);
                }
            }
        }
        return moduleView;
    }

    private ModuleView<?> lookupViewForLocation(final Location location) {
        for (final Entry<Location, ModuleView<?>> e : this.viewsForLocation.entrySet()) {
            final Location l = e.getKey();
            if (l.getObject().equals(location.getObject()) && l.getPerspective().equals(location.getPerspective())
                    && l.isEditable() == location.isEditable()) {
                return e.getValue();
            }
        }

        return null;
    }

    /**
     * Returns an initialized view (build and initialize a new one, or return
     * the stored one) representing supplied object. An additional flag
     * indicates if this view must be build if not already existent.
     * 
     * @param object
     * @param createViewIfRequired
     * @return an initialized ModuleView instance
     */
    public ModuleView<?> moduleViewForObject(final FlexoObject object, final boolean createViewIfRequired) {
        return this.moduleViewForLocation(new Location(this.controllerModel.getCurrentEditor(), object, this.getCurrentPerspective()),
                createViewIfRequired);
    }

    /**
     * Returns an initialized view (build and initialize a new one, or return
     * the stored one) representing supplied object.If not already existent,
     * build the view.
     * 
     * @param object
     * @return an initialized ModuleView instance
     */
    public ModuleView<?> moduleViewForObject(final FlexoObject object) {
        return this.moduleViewForObject(object, true);
    }

    /**
     * Creates a new view for supplied object, or null if this object is not
     * representable in this module
     * 
     * @param object
     * @param perspective
     *            TODO
     * @return a newly created and initialized ModuleView instance
     */
    private ModuleView<?> createModuleViewForObjectAndPerspective(final FlexoObject object, final FlexoPerspective perspective,
            final boolean editable) {
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

    public boolean isEditable(final Object object) {
        if (this.isDisposed()) {
            return false;
        }
        return /*!getModule().getModule().requireProject() ||*/!(object instanceof FlexoProjectObject)
                || ((FlexoProjectObject) object).getProject() == this.getProject();
    }

    /**
     * Sets supplied object to be the main object represented as the current
     * view for this module (for example the process for WKF module). Does
     * nothing if supplied object is not representable in this module
     * 
     * @param object
     * @return an initialized ModuleView instance
     */
    @Deprecated
    public final void setObjectAsModuleView(final Object object) {
        // This hack is introduced to support double click in imported workflow
        // tree.
        // This should be removed and imported wofklow tree should be updated to
        // support casting
        if (object instanceof FlexoObject) {
            this.setCurrentEditedObjectAsModuleView((FlexoObject) object);
        }
    }

    /**
     * Sets supplied object to be the main object represented as the current
     * view for this module (for example the process for WKF module). Does
     * nothing if supplied object is not representable in this module
     * 
     * @param object
     * @return an initialized ModuleView instance
     */
    public void setCurrentEditedObjectAsModuleView(final FlexoObject object) {
        this.getControllerModel().setCurrentObject(object);
    }

    /**
     * Sets supplied object to be the main object represented as the current
     * view for this module (for example the process for WKF module) and
     * supplied perspective. Does nothing if supplied object is not
     * representable in this module
     * 
     * @param object
     * @return an initialized ModuleView instance
     */
    public void setCurrentEditedObjectAsModuleView(final FlexoObject object, final FlexoPerspective perspective) {
        this.controllerModel.setCurrentPerspective(perspective);
        this.controllerModel.setCurrentObject(object);
    }

    public void addToPerspectives(final FlexoPerspective perspective) {
        this.controllerModel.addToPerspectives(perspective);
    }

    /**
     * Return currently displayed ModuleView
     * 
     * @return
     */
    public ModuleView<?> getCurrentModuleView() {
        if (this.mainPane != null) {
            return this.mainPane.getModuleView();
        }
        return null;
    }

    /**
     * Returns MainPane for this module
     * 
     * @return a FlexoMainPane instance
     */
    public FlexoMainPane getMainPane() {
        return this.mainPane;
    }

    public boolean hasMainPane() {
        return this.mainPane != null;
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
    public void removeModuleView(final ModuleView<?> aView) {
        final Collection<Location> locations = this.locationsForView.get(aView);
        if (locations != null) {
            for (final Location location : locations) {
                this.viewsForLocation.remove(location);
            }
        }
        this.locationsForView.removeAll(aView);
    }

    /**
     * Returns all the views currently loaded.
     * 
     * @return all the views currently loaded.
     */
    protected Collection<ModuleView<?>> getViews() {
        return this.viewsForLocation.values();
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModuleView<?>> Collection<T> getViews(final Class<T> klass) {
        return (Collection<T>) Collections2.filter(this.viewsForLocation.values(), new Predicate<ModuleView<?>>() {
            @Override
            public boolean apply(final ModuleView<?> input) {
                return klass.isAssignableFrom(input.getClass());
            };
        });
    }

    /**
     * Shows control panel
     */
    public void showControlPanel() {
        if (this.mainPane != null) {
            this.mainPane.showControlPanel();
        }
    }

    /**
     * Hides control panel
     */
    public void hideControlPanel() {
        if (this.mainPane != null) {
            this.mainPane.hideControlPanel();
        }
    }

    public void updateRecentProjectMenu() {
        if (this.menuBar != null) {
            this.menuBar.getFileMenu(this).updateRecentProjectMenu();
        }
    }

    /**
     * Returns a custom component to be added to control panel in main pane
     * Default implementation returns null, override it when required
     * 
     * @return
     */
    public final JComponent getCustomActionPanel() {
        return null;
    }

    public final String getWindowTitleforObject(final FlexoObject object) {
        if (this.getCurrentPerspective() != null) {
            return this.getCurrentPerspective().getWindowTitleforObject(object, this);
        }
        return object.toString();
    }

    public String getWindowTitle() {
        final String projectTitle = /*getModule().getModule().requireProject() &&*/this.getProject() != null ? " - "
                + this.getProject().getProjectName() + " - " + this.getProjectDirectory().getAbsolutePath() : "";
        if (this.getCurrentModuleView() != null) {
            return this.getModule().getName() + " : " + this.getWindowTitleforObject(this.getCurrentDisplayedObjectAsModuleView())
                    + projectTitle;
        }
        else {
            if (this.getModule() == null) {
                return FlexoCst.BUSINESS_APPLICATION_VERSION_NAME + projectTitle;
            }
            else {
                return FlexoCst.BUSINESS_APPLICATION_VERSION_NAME + " - " + this.getModule().getName() + projectTitle;
            }
        }
    }

    public void cancelCurrentAction() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Escape was pressed but the current controller does not do anything about it");
        }
    }

    // TODO: reimplement this !
    /*public class FlexoControllerInspectorDelegate implements InspectorDelegate {

    	private KeyValueCoding target;

    	private String key;

    	private String localizedPropertyName;

    	@Override
    	public boolean setObjectValue(Object value) {

    		if (target != null) {
    			if (target instanceof FlexoObject) {
    				SetPropertyAction action = SetPropertyAction.actionType.makeNewAction((FlexoObject) target, new Vector<FlexoObject>(),
    						getEditor());
    				action.setKey(key);
    				action.setValue(value);
    				action.setLocalizedPropertyName(localizedPropertyName);
    				action.doAction();
    				return action.hasActionExecutionSucceeded() && action.getThrownException() == null;
    			} else if (target != null) {
    				target.setObjectForKey(value, key);
    			} else {
    				if (logger.isLoggable(Level.SEVERE)) {
    					logger.severe("Target object is not a FlexoObject, I cannot set the value for that object");
    				}
    			}
    		} else if (logger.isLoggable(Level.WARNING)) {
    			logger.warning("Target object is null for key " + key + ". We should definitely investigate this.");
    		}
    		return false;
    	}

    	@Override
    	public boolean handlesObjectOfClass(Class<?> c) {
    		return KeyValueCoding.class.isAssignableFrom(c);
    	}

    	@Override
    	public void setKey(String path) {
    		this.key = path;
    	}

    	@Override
    	public void setTarget(KeyValueCoding object) {
    		this.target = object;
    	}

    	@Override
    	public boolean performAction(ActionEvent e, String actionName, Object object) {
    		if (object instanceof FlexoObject) {
    			FlexoObject m = (FlexoObject) object;
    			for (FlexoActionType<?, ?, ?> actionType : m.getActionList()) {
    				if (actionType.getUnlocalizedName().equals(actionName)) {
    					return getEditor().performActionType((FlexoActionType<?, FlexoObject, FlexoObject>) actionType, m,
    							(Vector<FlexoObject>) null, e).hasActionExecutionSucceeded();
    				}
    			}
    		}
    		return false;
    	}

    	@Override
    	public void setLocalizedPropertyName(String name) {
    		localizedPropertyName = name;
    	}

    }*/

    public boolean isDisposed() {
        return this.disposed;
    }

    public void dispose() {

        this.getSelectionManager().deleteObserver(this.getModuleInspectorController());

        this.manager.delete();
        this.getApplicationContext().getGeneralPreferences().getPropertyChangeSupport().removePropertyChangeListener(this);
        this.mainPane.dispose();
        if (this.consistencyCheckWindow != null && !this.consistencyCheckWindow.isDisposed()) {
            this.consistencyCheckWindow.dispose();
        }
        if (this.mainInspectorController != null) {
            this.mainInspectorController.delete();
        }
        for (final ModuleView<?> view : new ArrayList<ModuleView<?>>(this.getViews())) {
            try {
                view.deleteModuleView();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        this.registeredMenuBar.clear();
        /*if (PreferencesController.hasInstance()) {
        	PreferencesController.instance().getPreferencesWindow().setVisible(false);
        }*/
        if (this.flexoFrame != null) {
            this.flexoFrame.disposeAll();
        }
        if (this.menuBar != null) {
            this.menuBar.dispose();
        }
        if (this.getEditor() instanceof InteractiveFlexoEditor) {
            ((InteractiveFlexoEditor) this.getEditor()).unregisterControllerActionInitializer(this.getControllerActionInitializer());
        }
        this.controllerModel.delete();
        this.viewsForLocation.clear();
        this.locationsForView.clear();
        this.disposed = true;
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.firePropertyChange(DISPOSED, false, true);
        }
        this.setEditor(null);
        this.propertyChangeSupport = null;
        this.inspectorMenuBar = null;
        this.consistencyCheckWindow = null;
        this.flexoFrame = null;
        this.mainPane = null;
        this.menuBar = null;
        this.module = null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Finalizing controller " + this.getClass().getSimpleName());
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
                        returned.value = FlexoController.this._handleWSException(e);
                    }
                });
            } catch (final InvocationTargetException e1) {
                e1.printStackTrace();
                return false;
            }
            return returned.value != null && returned.value;
        }
        return this._handleWSException(e);
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
        }/*
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
        else if (e.getMessage() != null && "(500)Apple WebObjects".equals(e.getMessage()) || e.getMessage().startsWith("No such operation")) {
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
            final IOException ioEx = (IOException) (e instanceof IOException ? e : e.getCause());
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
                return FlexoController
                        .confirm(FlexoLocalization.localizedForKey("webservice_remote_error")
                                + " \n"
                                + (e.getMessage() == null || "java.lang.NullPointerException".equals(e.getMessage()) ? "Check your connection parameters.\nThe service may be temporary unavailable."
                                        : e.getMessage()) + "\n" + FlexoLocalization.localizedForKey("would_you_like_to_try_again?"));
            }
        }
        /*FlexoController.notify(FlexoLocalization.localizedForKey("webservice_connection_failed"));
        FlexoController.notify(FlexoLocalization.localizedForKey("webservice_authentification_failed") + ": "
        		+ FlexoLocalization.localizedForKey(e.getMessage1()));*/
    }

    /**
     * We manage here an indirection with resources: resource data is used
     * instead of resource if resource is loaded
     * 
     * @param object
     * @return
     */
    private FlexoObject getRelevantObject(final FlexoObject object) {
        /*if (object instanceof FlexoResource<?>) {
        	logger.info("Resource " + object + " loaded=" + ((FlexoResource<?>) object).isLoaded());
        }*/
        if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
            return (FlexoObject) ((FlexoResource<?>) object).getLoadedResourceData();
        }
        return object;
    }

    public void objectWasClicked(final Object object) {
        // logger.info("Object was clicked: " + object);
        // logger.info("Current selection=" +
        // getSelectionManager().getSelection());
        if (this.getCurrentPerspective() != null) {
            if (object instanceof FlexoObject) {
                this.getCurrentPerspective().objectWasClicked(this.getRelevantObject((FlexoObject) object), this);
            }
            else {
                this.getCurrentPerspective().objectWasClicked(object, this);
            }
        }
    }

    public void objectWasRightClicked(final Object object, final MouseEvent e) {
        // logger.info("Object was right-clicked: " + object + "event=" + e);
        if (object instanceof FlexoObject) {
            final FlexoObject relevantObject = this.getRelevantObject((FlexoObject) object);
            this.getSelectionManager().getContextualMenuManager()
                    .showPopupMenuForObject(relevantObject, (Component) e.getSource(), e.getPoint());
        }
        if (this.getCurrentPerspective() != null) {
            if (object instanceof FlexoObject) {
                this.getCurrentPerspective().objectWasRightClicked(this.getRelevantObject((FlexoObject) object), this);
            }
            else {
                this.getCurrentPerspective().objectWasRightClicked(object, this);
            }
        }
    }

    public void objectWasDoubleClicked(final Object object) {
        // logger.info("Object was double-clicked: " + object);
        if (object instanceof FlexoResource<?>) {
            FlexoObject resourceData = null;
            if (((FlexoResource<?>) object).isLoadable() && !((FlexoResource<?>) object).isLoaded()) {
                final FlexoProgress progress = this.getEditor().getFlexoProgressFactory().makeFlexoProgress("loading_resource", 3);
                try {
                    resourceData = (FlexoObject) ((FlexoResource<?>) object).getResourceData(progress);
                } catch (final FileNotFoundException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                } catch (final ResourceLoadingCancelledException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                } catch (final FlexoException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                }
                progress.hideWindow();
            }
            else {
                try {
                    resourceData = (FlexoObject) ((FlexoResource<?>) object).getResourceData(null);
                } catch (final FileNotFoundException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                } catch (final ResourceLoadingCancelledException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                } catch (final FlexoException e) {
                    notify("Cannot load resource: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (resourceData != null) {
                this.selectAndFocusObject(resourceData);
            }
        }
        if (object instanceof FlexoObject && this.getCurrentPerspective().hasModuleViewForObject((FlexoObject) object)) {
            // Try to display object in view
            this.selectAndFocusObject((FlexoObject) object);
        }
        if (this.getCurrentPerspective() != null) {
            if (object instanceof FlexoObject) {
                this.getCurrentPerspective().objectWasDoubleClicked(this.getRelevantObject((FlexoObject) object), this);
            }
            else {
                this.getCurrentPerspective().objectWasDoubleClicked(object, this);
            }
        }
    }

    public boolean displayInspectorTabForContext(final String context) {
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

    public FlexoProgress willLoad(final Resource fibResource) {

        if (!FIBLibrary.instance().componentIsLoaded(fibResource)) {
            final FlexoProgress progress = ProgressWindow.makeProgressWindow(FlexoLocalization.localizedForKey("loading_interface..."), 3);
            progress.setProgress("loading_component");
            FIBLibrary.instance().retrieveFIBComponent(fibResource);
            progress.setProgress("build_interface");
            return progress;
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

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource() == this.getControllerModel()) {
            if (evt.getPropertyName().equals(ControllerModel.CURRENT_EDITOR)) {
                final FlexoEditor oldEditor = (FlexoEditor) evt.getOldValue();
                final FlexoEditor newEditor = (FlexoEditor) evt.getNewValue();
                if (oldEditor != null || newEditor != null) {
                    this.updateEditor(oldEditor, newEditor);
                }
            }
            else if (evt.getPropertyName().equals(ControllerModel.LOCATIONS)) {
                if (evt.getOldValue() != null) {
                    final Location location = (Location) evt.getOldValue();
                    final ModuleView<?> moduleViewForLocation = this.moduleViewForLocation(location, false);
                    if (moduleViewForLocation != null) {
                        if (this.locationsForView.get(moduleViewForLocation).size() < 2) {
                            moduleViewForLocation.deleteModuleView();
                        }
                        else {
                            this.locationsForView.remove(moduleViewForLocation, location);
                        }
                    }
                }
            }
            else if (evt.getPropertyName().equals(ControllerModel.CURRENT_OBJECT)) {
                this.getSelectionManager().setSelectedObject(this.getControllerModel().getCurrentObject());
            }
        }
        else if (evt.getSource() instanceof FlexoProject && evt.getPropertyName().equals(ProjectClosedNotification.CLOSE)) {
            final FlexoProject project = (FlexoProject) evt.getSource();
            for (final ModuleView<?> view : new ArrayList<ModuleView>(this.getViews())) {
                if (view.getRepresentedObject() instanceof FlexoProjectObject) {
                    if (project.equals(((FlexoProjectObject) view.getRepresentedObject()).getProject())) {
                        view.deleteModuleView();
                    }
                }
            }
            this.manager.removeListener(ProjectClosedNotification.CLOSE, this, project);
        }
        else if (evt.getSource() == this.getApplicationContext().getGeneralPreferences()) {
            final String key = evt.getPropertyName();
            if (GeneralPreferences.LANGUAGE_KEY.equals(key)) {
                this.getFlexoFrame().updateTitle();
            }
            else if (GeneralPreferences.LAST_OPENED_PROJECTS_1.equals(key) || GeneralPreferences.LAST_OPENED_PROJECTS_2.equals(key)
                    || GeneralPreferences.LAST_OPENED_PROJECTS_3.equals(key) || GeneralPreferences.LAST_OPENED_PROJECTS_4.equals(key)
                    || GeneralPreferences.LAST_OPENED_PROJECTS_5.equals(key)) {
                this.updateRecentProjectMenu();
            }
        }
    }

    /**
     * Select the supplied object. Also try to select (create if not exists) a
     * main view representing supplied object, if this view exists.<br>
     * Try all to really display supplied object, even if required view is not
     * the current displayed view
     * 
     * @param object
     *            the object to focus on
     */
    public void selectAndFocusObject(final FlexoObject object) {
        if (object instanceof FlexoProject) {
            this.getControllerModel().setCurrentProject((FlexoProject) object);
        }
        else {
            this.setCurrentEditedObjectAsModuleView(object);
        }
        this.getSelectionManager().setSelectedObject(object);
    }

    public ValidationModel getDefaultValidationModel() {
        return null;
    }

    public final FlexoPerspective getCurrentPerspective() {
        return this.getControllerModel().getCurrentPerspective();
    }

    public final FlexoEditor getEditor() {
        return this.getControllerModel().getCurrentEditor();
    }

    public final FlexoEditingContext getEditingContext() {
        return this.getApplicationContext().getEditingContext();
    }

    /**
     * Return the technology-specific controller for supplied technology adapter
     * 
     * @param technologyAdapter
     * @return
     */
    public static <TA extends TechnologyAdapter> TechnologyAdapterController<TA> getTechnologyAdapterController(final TA technologyAdapter) {
        if (technologyAdapter != null) {
            final FlexoServiceManager sm = technologyAdapter.getTechnologyAdapterService().getServiceManager();
            if (sm != null) {
                final TechnologyAdapterControllerService service = sm.getService(TechnologyAdapterControllerService.class);
                if (service != null) {
                    return service.getTechnologyAdapterController(technologyAdapter);
                }
            }
        }
        return null;
    }

    // ================================================
    // ============ Icons management ==============
    // ================================================

    public ImageIcon iconForObject(final Object object) {
        ImageIcon iconForObject = statelessIconForObject(object);
        if (iconForObject != null) {
            if (/*getModule().getModule().requireProject() &&*/object instanceof FlexoProjectObject && this.getProject() != null
                    && ((FlexoProjectObject) object).getProject() != this.getProject()
                    && ((FlexoProjectObject) object).getProject() != null
                    && (!(object instanceof FlexoProject) || !this.getProjectLoader().getRootProjects().contains(object))) {
                iconForObject = IconFactory.getImageIcon(iconForObject, new IconMarker[] { IconLibrary.IMPORT });
            }
            else if (object instanceof FlexoProjectReference) {
                iconForObject = IconFactory.getImageIcon(iconForObject, new IconMarker[] { IconLibrary.IMPORT });
            }
        }
        return iconForObject;
    }

    public static <TA extends TechnologyAdapter> ImageIcon statelessIconForTechnologyObject(final TechnologyObject<TA> object) {
        // prevent NPE
        if (object != null) {
            final TechnologyAdapterController<TA> tac = getTechnologyAdapterController(object.getTechnologyAdapter());
            if (tac != null) {
                return tac.getIconForTechnologyObject((Class<TechnologyObject<TA>>) object.getClass());
            }
            else {
                logger.warning("Could not find TechnologyAdapterController for technology " + object.getTechnologyAdapter());
            }
        }
        return null;
    }

    public static <TA extends TechnologyAdapter> ImageIcon statelessIconForTechnologyAdapterResource(
            final TechnologyAdapterResource<?, TA> resource) {
        final TechnologyAdapterController<TA> tac = getTechnologyAdapterController(resource.getTechnologyAdapter());
        if (tac != null) {
            return tac.getIconForTechnologyObject(resource.getResourceDataClass());
        }
        else {
            logger.warning("Could not find TechnologyAdapterController for technology "
                    + ((TechnologyAdapterResource<?, ?>) resource).getTechnologyAdapter());
        }
        return null;
    }

    public static ImageIcon statelessIconForObject(final Object object) {

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
            if (object instanceof CreateCommand) {
                return IconLibrary.SMALL_PLUS_ICON;
            }
            else if (object instanceof SetCommand) {
                return IconLibrary.SMALL_SET_ICON;
            }
            else if (object instanceof DeleteCommand) {
                return IconLibrary.SMALL_DELETE_ICON;
            }
            else if (object instanceof AddCommand) {
                return IconLibrary.SMALL_PLUS_ICON;
            }
            else if (object instanceof RemoveCommand) {
                return IconLibrary.SMALL_MINUS_ICON;
            }
        }

        // If object is a TechnologyObject, we delegate this to the right
        // TechnologyAdapterController
        if (object instanceof TechnologyObject<?>) {
            return statelessIconForTechnologyObject((TechnologyObject<?>) object);
        }

        // If object is a resource and if this resource is loaded, use icon of
        // loaded resource data
        if (object instanceof FlexoResource<?> && ((FlexoResource<?>) object).isLoaded()) {
            return statelessIconForObject(((FlexoResource<?>) object).getLoadedResourceData());
        }
        else if (object instanceof ViewResource) {
            return VEIconLibrary.VIEW_ICON;
        }
        else if (object instanceof VirtualModelInstanceResource) {
            return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
        }
        else if (object instanceof TechnologyAdapterResource<?, ?>) {
            return statelessIconForTechnologyAdapterResource((TechnologyAdapterResource<?, ?>) object);
        }
        else if (object instanceof InformationSpace) {
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
            return VPMIconLibrary.VIEWPOINT_LIBRARY_ICON;
        }
        else if (object instanceof ViewPointObject) {
            return VPMIconLibrary.iconForObject((ViewPointObject) object);
        }
        else if (object instanceof ViewPointResource) {
            return VPMIconLibrary.iconForObject((ViewPointResource) object);
        }
        else if (object instanceof VirtualModelResource) {
            return VPMIconLibrary.iconForObject((VirtualModelResource) object);
        }
        else if (object instanceof ViewResource) {
            return VEIconLibrary.iconForObject((ViewResource) object);
        }
        else if (object instanceof VirtualModelInstanceResource) {
            return VEIconLibrary.iconForObject((VirtualModelInstanceResource) object);
        }
        else if (object instanceof ViewLibrary) {
            return VEIconLibrary.VIEW_LIBRARY_ICON;
        }
        else if (object instanceof ViewObject) {
            return VEIconLibrary.iconForObject((ViewObject) object);
        }
        else if (object instanceof RepositoryFolder) {
            if (((RepositoryFolder) object).isRootFolder()) {
                return statelessIconForObject(((RepositoryFolder) object).getResourceRepository().getOwner());
            }
            return IconLibrary.FOLDER_ICON;
        }
        else if (object instanceof TechnologyAdapter) {
            final TechnologyAdapterController<?> tac = getTechnologyAdapterController((TechnologyAdapter) object);
            if (tac != null) {
                return tac.getTechnologyIcon();
            }
            else {
                logger.warning("Could not find TechnologyAdapterController for technology " + object);
            }
        }
        else if (object instanceof FlexoModel<?, ?>) {
            final TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoModel<?, ?>) object).getTechnologyAdapter());
            if (tac != null) {
                return tac.getModelIcon();
            }
        }
        else if (object instanceof FlexoModelResource<?, ?, ?>) {
            final TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoModelResource<?, ?, ?>) object)
                    .getTechnologyAdapter());
            if (tac != null) {
                return tac.getModelIcon();
            }
        }
        else if (object instanceof FlexoMetaModel<?>) {
            final TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoMetaModel<?>) object).getTechnologyAdapter());
            if (tac != null) {
                return tac.getMetaModelIcon();
            }
        }
        else if (object instanceof FlexoMetaModelResource<?, ?, ?>) {
            final TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoMetaModelResource<?, ?, ?>) object)
                    .getTechnologyAdapter());
            if (tac != null) {
                return tac.getMetaModelIcon();
            }
        }
        else if (object instanceof FlexoProjectReference) {
            return IconLibrary.OPENFLEXO_NOTEXT_16;
        }
        logger.warning("Sorry, no icon defined for " + object + " " + (object != null ? object.getClass() : ""));
        return null;
    }

    // ================================================
    // ============ Resources management ==============
    // ================================================

    public void saveModifiedResources() {
        System.out.println("registered resources: " + this.getApplicationContext().getResourceManager().getRegisteredResources().size()
                + " : " + this.getApplicationContext().getResourceManager().getRegisteredResources());
        System.out.println("loaded resources: " + this.getApplicationContext().getResourceManager().getLoadedResources().size() + " : "
                + this.getApplicationContext().getResourceManager().getLoadedResources());
        System.out.println("unsaved resources: " + this.getApplicationContext().getResourceManager().getUnsavedResources().size() + " : "
                + this.getApplicationContext().getResourceManager().getUnsavedResources());
        System.out.println("TODO: implement this");
    }

    public boolean reviewModifiedResources() {
        System.out.println("reviewModifiedResources()");
        /*ResourceSavingInfo savingInfo = getResourceSavingInfo();
        savingInfo.update();
        for (ResourceSavingEntryInfo e : savingInfo.getEntries()) {
        	System.out.println(" > " + e + " resource=" + e.getName() + " type=" + e.getType() + " save=" + e.saveThisResource());
        }*/

        final ReviewUnsavedDialog dialog = new ReviewUnsavedDialog(this.getApplicationContext().getResourceManager());
        dialog.showDialog();

        // FIBDialog<ResourceSavingInfo> dialog =
        // FIBDialog.instanciateAndShowDialog(CommonFIB.REVIEW_UNSAVED_DIALOG_FIB,
        // savingInfo,
        // FlexoFrame.getActiveFrame(), true,
        // FlexoLocalization.getMainLocalizer());
        if (dialog.getStatus() == Status.VALIDATED) {
            try {
                dialog.saveSelection(this.getEditor().getFlexoProgressFactory());
            } catch (final SaveResourcePermissionDeniedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final SaveResourceExceptionList e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private String             infoMessage;
    private String             tempInfoMessage;
    private int                temporaryThreadCount = 0;
    private final List<JLabel> infoLabels           = new ArrayList<JLabel>();

    public JLabel makeInfoLabel() {
        final JLabel returned = new JLabel(this.getInfoMessage());
        returned.setFont(FlexoCst.SMALL_FONT);
        this.infoLabels.add(returned);
        return returned;
    }

    public String getInfoMessage() {
        if (this.tempInfoMessage != null) {
            return this.tempInfoMessage;
        }
        if (this.infoMessage == null) {
            return this.getModule().getName();
        }
        return this.infoMessage;
    }

    public void setInfoMessage(final String infoMessage) {
        this.setInfoMessage(infoMessage, false);
    }

    public void setInfoMessage(final String infoMessage, final boolean temporary) {
        final String oldInfoMessage = this.infoMessage;
        if (!infoMessage.equals(oldInfoMessage)) {

            if (temporary) {
                this.tempInfoMessage = infoMessage;
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // System.out.println("START " + localTempInfoMessage +
                        // " temporaryThreadCount=" + temporaryThreadCount);
                        try {
                            Thread.sleep(FlexoCst.TEMPORARY_MESSAGE_PERSISTENCY);
                        } catch (final InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        FlexoController.this.temporaryThreadCount--;
                        // System.out.println("END " + tempInfoMessage +
                        // " temporaryThreadCount=" + temporaryThreadCount);
                        if (FlexoController.this.temporaryThreadCount == 0) {
                            // System.out.println("Back to " + infoMessage);
                            FlexoController.this.tempInfoMessage = null;
                            for (final JLabel label : FlexoController.this.infoLabels) {
                                // System.out.println("Setting again label to "
                                // + getInfoMessage());
                                label.setText(FlexoController.this.getInfoMessage());
                            }
                        }
                    }
                });
                this.temporaryThreadCount++;
                t.start();
            }
            else {
                // System.out.println("REALLY set infoMessage to " +
                // infoMessage);
                this.infoMessage = infoMessage;
            }
            for (final JLabel label : this.infoLabels) {
                label.setText(this.getInfoMessage());
            }
        }
    }

}
