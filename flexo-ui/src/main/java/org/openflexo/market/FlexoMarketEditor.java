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

package org.openflexo.market;

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.ApplicationContext;
import org.openflexo.components.ProgressWindow;
import org.openflexo.foundation.remoteresources.DefaultFlexoBundle;
import org.openflexo.foundation.remoteresources.FlexoBundle;
import org.openflexo.foundation.remoteresources.FlexoMarketRemoteRepository;
import org.openflexo.foundation.remoteresources.FlexoRemoteRepository;
import org.openflexo.foundation.remoteresources.FlexoRemoteRepositoryImpl;
import org.openflexo.foundation.remoteresources.FlexoUpdateService;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoFIBController;

public class FlexoMarketEditor implements HasPropertyChangeSupport {

	private List<FlexoRemoteRepository> repositories;

	private FlexoRemoteRepository repository;

	private final PropertyChangeSupport _pcSupport;

	private Window owner;

	private List<FlexoBundle> selectedBundles;

	private final ApplicationContext applicationContext;
	private final FlexoUpdateService service;

	private String newURL;

	public Window getOwner() {
		return owner;
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}

	public FlexoMarketEditor(FlexoUpdateService service, ApplicationContext applicationContext) {
		_pcSupport = new PropertyChangeSupport(this);
		repositories = new ArrayList<FlexoRemoteRepository>();
		this.service = service;
		this.applicationContext = applicationContext;
		FlexoMarketRemoteRepository flexoRepository = new FlexoMarketRemoteRepository("OPENFLEXO MARKET", null);
		repositories.add(flexoRepository);
		repository = repositories.get(0);
		selectedBundles = new ArrayList<FlexoBundle>();
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return _pcSupport;
	}

	public List<FlexoRemoteRepository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<FlexoRemoteRepository> repositories) {
		this.repositories = repositories;
	}

	public FlexoRemoteRepository getRepository() {
		return repository;
	}

	public void setRepository(FlexoRemoteRepository repository) {
		this.repository = repository;
	}

	private void showProgress(String stepname) {
		ProgressWindow.showProgressWindow(owner, applicationContext.getLocalizationService().getFlexoLocalizer().localizedForKey(stepname),
				1);
		ProgressWindow.instance().setProgress(applicationContext.getLocalizationService().getFlexoLocalizer().localizedForKey(stepname));
	}

	private void hideProgress() {
		ProgressWindow.hideProgressWindow();
	}

	public List<FlexoBundle> getSelectedBundles() {
		return selectedBundles;
	}

	public void setSelectedBundles(List<FlexoBundle> selectedBundles) {
		this.selectedBundles = selectedBundles;
	}

	public void addToSelection(FlexoBundle bundle) {
		selectedBundles.add(bundle);
		getPropertyChangeSupport().firePropertyChange("selectedBundles", null, null);
	}

	public void removeFromSelection(FlexoBundle bundle) {
		selectedBundles.remove(bundle);
		getPropertyChangeSupport().firePropertyChange("selectedBundles", null, null);
	}

	boolean isInSelection(FlexoBundle bundle) {
		return selectedBundles.contains(bundle);
	}

	public void update() {
		for (FlexoBundle bundle : getSelectedBundles()) {
			service.updateFromRemoteBundle(bundle);
		}
		selectedBundles.clear();
	}

	public void addRepository() {
		getRepositories().add(new FlexoRemoteRepositoryImpl("newRepository", "http://"));
	}

	public void addBundle(FlexoRemoteRepository repository) {
		repository.getFlexoBundles().add(new DefaultFlexoBundle());
	}

	public static final Resource FLEXO_CREATE_URL_FIB = ResourceLocator.locateResource("Fib/FlexoCreateURL.fib");

	public void addURL(FlexoBundle bundle, FlexoRemoteRepository repository) {
		try {

			FIBComponent fibComponent = applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(FLEXO_CREATE_URL_FIB);
			JFIBDialog dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, this, FlexoFrame.getActiveFrame(), true,
					new FlexoFIBController(fibComponent, null));
			if (dialog.getStatus().equals(Status.VALIDATED)) {
				bundle.addToURLs(new URL(newURL));
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getNewURL() {
		return newURL;
	}

	public void setNewURL(String newURL) {
		this.newURL = newURL;
	}

}
