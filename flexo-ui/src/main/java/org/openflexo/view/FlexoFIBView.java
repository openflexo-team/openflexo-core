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

package org.openflexo.view;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.listener.FIBMouseClickListener;
import org.openflexo.gina.model.widget.FIBBrowser;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.FIBView;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.PropertyChangeListenerRegistrationManager;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Please comment this class
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FlexoFIBView extends JPanel implements GraphicalFlexoObserver, HasPropertyChangeSupport, PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoFIBView.class.getPackage().getName());

	private static final String DELETED = "deleted";

	private Object dataObject;

	final FlexoController controller;
	private final JFIBView<?, ?> fibView;
	private FlexoFIBController fibController;
	private final FIBComponent fibComponent;

	private final PropertyChangeSupport pcSupport;

	protected PropertyChangeListenerRegistrationManager manager = new PropertyChangeListenerRegistrationManager();

	public FlexoFIBView(Object representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales) {
		this(representedObject, controller, fibResource, locales, false);
	}

	public FlexoFIBView(Object representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales,
			boolean addScrollBar) {
		this(representedObject, controller, controller.getApplicationFIBLibraryService().retrieveFIBComponent(fibResource), locales,
				addScrollBar);
	}

	protected FlexoFIBView(Object dataObject, FlexoController controller, FIBComponent fibComponent, LocalizedDelegate locales,
			boolean addScrollBar) {
		super(new BorderLayout());
		this.controller = controller;
		this.fibComponent = fibComponent;

		// TODO: try to remove following lines
		/*if (dataObject instanceof HasPropertyChangeSupport) {
			manager.addListener(this, (HasPropertyChangeSupport) dataObject);
		}
		else if (dataObject instanceof FlexoObservable) {
			((FlexoObservable) dataObject).addObserver(this);
		}*/
		// TODO: try to remove previous lines

		pcSupport = new PropertyChangeSupport(this);

		// Important to set dataObject now
		this.dataObject = dataObject;
		initializeFIBComponent();

		if (getFlexoController() != null) {
			getFIBComponent().setCustomTypeManager(getFlexoController().getApplicationContext().getTechnologyAdapterService());
			getFIBComponent()
					.setCustomTypeEditorProvider(getFlexoController().getApplicationContext().getTechnologyAdapterControllerService());
		}
		fibController = createFibController(fibComponent, controller, locales);

		Progress.progress("builing_view");

		fibView = (JFIBView<?, ?>) fibController.buildView(fibComponent, null, true);

		Progress.progress("init_view");

		setDataObject(dataObject);

		// fibController.setDataObject(dataObject);

		if (this instanceof FIBMouseClickListener) {
			fibView.getController().addMouseClickListener((FIBMouseClickListener) this);
		}

		if (addScrollBar) {
			add(new JScrollPane(fibView.getJComponent()), BorderLayout.CENTER);
		}
		else {
			add(fibView.getJComponent(), BorderLayout.CENTER);
		}

		validate();
		revalidate();

	}

	/**
	 * Create the Fib Controller to be used for this view. Can be overrided to add functionalities to this Fib View.
	 * 
	 * @param fibComponent
	 * @param controller
	 * @return the newly created FlexoFIBController
	 */
	protected FlexoFIBController createFibController(FIBComponent fibComponent, FlexoController controller, LocalizedDelegate locales) {
		FIBController returned = FIBController.instanciateController(fibComponent, SwingViewFactory.INSTANCE,
				locales != null ? locales : FlexoLocalization.getMainLocalizer());
		if (returned instanceof FlexoFIBController) {
			((FlexoFIBController) returned).setFlexoController(controller);
			return (FlexoFIBController) returned;
		}
		else if (fibComponent.getControllerClass() != null) {
			logger.warning("Controller for component " + fibComponent + " is not an instanceof FlexoFIBController");
		}
		return fibController = new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, controller);
	}

	public FlexoController getFlexoController() {
		return controller;
	}

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {
		/*
		 * if (dataModification instanceof ObjectDeleted) { if (dataModification.oldValue() == getOntologyObject()) { deleteModuleView(); }
		 * } else if (dataModification.propertyName()!=null && dataModification.propertyName().equals("name")) {
		 * getOEController().getFlexoFrame().updateTitle(); updateTitlePanel(); }
		 */

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO !
		// logger.info("propertyChange in FlexoFIBView: " + evt);
	}

	public Object getDataObject() {
		return dataObject;
	}

	public void setDataObject(Object object) {
		// logger.info(">>>>>>> setDataObject with " + object);
		if (this.dataObject instanceof HasPropertyChangeSupport) {
			manager.removeListener(this, (HasPropertyChangeSupport) this.dataObject);
		}
		else if (this.dataObject instanceof FlexoObservable) {
			((FlexoObservable) this.dataObject).deleteObserver(this);
		}
		dataObject = object;
		if (dataObject instanceof HasPropertyChangeSupport) {
			manager.addListener(this, (HasPropertyChangeSupport) this.dataObject);
		}
		else if (dataObject instanceof FlexoObservable) {
			((FlexoObservable) dataObject).addObserver(this);
		}
		fibController.setDataObject(object, true);
	}

	public FIBComponent getFIBComponent() {
		return fibComponent;
	}

	public FIBView<?, ?> getFIBView() {
		return fibView;
	}

	public FlexoFIBController getFIBController() {
		return fibController;
	}

	public FIBView<?, ?> getFIBView(String componentName) {
		if (fibController != null) {
			return fibController.viewForComponent(componentName);
		}
		return null;
	}

	public void deleteView() {
		if (this instanceof FIBMouseClickListener && fibView.getController() != null) {
			fibView.getController().removeMouseClickListener((FIBMouseClickListener) this);
		}
		fibView.delete();
		if (dataObject instanceof FlexoObservable) {
			((FlexoObservable) dataObject).deleteObserver(this);
		}
		if (dataObject instanceof HasPropertyChangeSupport) {
			manager.removeListener(this, (HasPropertyChangeSupport) dataObject);
		}
		manager.delete();
		getPropertyChangeSupport().firePropertyChange(DELETED, false, true);
	}

	/**
	 * Returns flag indicating if this view is itself responsible for scroll management When not, Flexo will manage it's own scrollbar for
	 * you
	 * 
	 * @return
	 */
	public boolean isAutoscrolled() {
		return false;
	}

	/**
	 * This method is a hook which is called just before to initialize FIBViewImpl and FIBController, and allow to programmatically define,
	 * check or redefine component
	 */
	public void initializeFIBComponent() {
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return DELETED;
	}

	/**
	 * Explore the whole hierarchy of supplied component and return first component of supplied component class
	 * 
	 * @param component
	 *            component to explore
	 * @param componentClass
	 * @return
	 */
	public static <C extends FIBComponent> C retrieveFIBComponent(FIBContainer component, Class<C> componentClass) {
		if (component == null) {
			return null;
		}
		List<FIBComponent> listComponent = component.getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if (componentClass.isAssignableFrom(c.getClass())) {
				return (C) c;
			}
		}
		return null;
	}

	/**
	 * Explore the whole hierarchy of supplied component, and return first component of supplied component class and matching supplied name
	 * 
	 * @param component
	 *            component to explore
	 * @param componentClass
	 * @param name
	 * @return
	 */
	public static <C extends FIBComponent> C retrieveFIBComponentNamed(FIBContainer component, Class<C> componentClass, String name) {
		if (component == null) {
			return null;
		}
		List<FIBComponent> listComponent = component.getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if (componentClass.isAssignableFrom(c.getClass()) && (c.getName() != null) && (c.getName().equals(name))) {
				return (C) c;
			}
		}
		return null;
	}

	/**
	 * Explore the whole hierarchy of component represented by this view, and return first component of supplied component class
	 * 
	 * @param componentClass
	 * @return
	 */
	protected <C extends FIBComponent> C retrieveFIBComponent(Class<C> componentClass) {
		if (getFIBComponent() instanceof FIBContainer) {
			return retrieveFIBComponent((FIBContainer) getFIBComponent(), componentClass);
		}
		if (componentClass.isAssignableFrom(getFIBComponent().getClass())) {
			return (C) getFIBComponent();
		}

		return null;
	}

	/**
	 * Explore the whole hierarchy of component represented by this view, and return first component of supplied component class and
	 * matching supplied name
	 * 
	 * @param componentClass
	 * @param name
	 * @return
	 */
	protected <C extends FIBComponent> C retrieveFIBComponentNamed(Class<C> componentClass, String name) {
		if (getFIBComponent() instanceof FIBContainer) {
			return retrieveFIBComponentNamed((FIBContainer) getFIBComponent(), componentClass, name);
		}
		if (componentClass.isAssignableFrom(getFIBComponent().getClass()) && (getFIBComponent().getName() != null)
				&& (getFIBComponent().getName().equals(name))) {
			return (C) getFIBComponent();
		}

		return null;
	}

	/**
	 * Return {@link FIBBrowser} contained in component represented by this view, when any, asserting only one {@link FIBBrowser} resides in
	 * it
	 * 
	 * @return
	 */
	public static FIBBrowser retrieveFIBBrowser(FIBContainer component) {
		return retrieveFIBComponent(component, FIBBrowser.class);
	}

	/**
	 * Return {@link FIBBrowser} contained in component represented by this view, with supplied name
	 * 
	 * @param name
	 * @return
	 */
	public static FIBBrowser retrieveFIBBrowserNamed(FIBContainer component, String name) {
		return retrieveFIBComponentNamed(component, FIBBrowser.class, name);
	}

	/**
	 * Return {@link FIBBrowser} contained in component represented by this view, when any, asserting only one {@link FIBBrowser} resides in
	 * it
	 * 
	 * @return
	 */
	protected FIBBrowser retrieveFIBBrowserNamed() {
		return retrieveFIBComponent(FIBBrowser.class);
	}

	/**
	 * Return {@link FIBBrowser} contained in component represented by this view, with supplied name
	 * 
	 * @param name
	 * @return
	 */
	protected FIBBrowser retrieveFIBBrowserNamed(String name) {
		return retrieveFIBComponentNamed(FIBBrowser.class, name);
	}

	/**
	 * Semantics of this method is not trivial: the goal is to aggregate some notifications within a given time (supplied as a
	 * aggregationTimeOut), to do it only once.<br>
	 * Within this delay, all requests to this method will simply reinitialize time-out, and will be ignored. Only the first call will be
	 * executed in a new thread which will die immediately after.
	 * 
	 * @param r
	 *            runnable to run after last request + timeout
	 * @param aggregationTimeOut
	 *            in milliseconds
	 */
	public void invokeLater(final Runnable r, final long aggregationTimeOut) {
		synchronized (this) {
			lastSchedule = System.currentTimeMillis();
			if (!invokeLaterScheduled) {
				invokeLaterScheduled = true;
				Thread invokeLaterThread = new Thread(() -> {
					while (System.currentTimeMillis() < lastSchedule + aggregationTimeOut) {
						// We need to wait
						try {
							Thread.sleep(aggregationTimeOut);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					synchronized (FlexoFIBView.this) {
						invokeLaterScheduled = false;
					}
					r.run();
				}, "InvokeLaterThread");
				invokeLaterThread.start();
			}
			else {
				System.out.println("Ignoring invokeLater");
			}
		}
	}

	private boolean invokeLaterScheduled = false;
	private long lastSchedule = -1;

}
