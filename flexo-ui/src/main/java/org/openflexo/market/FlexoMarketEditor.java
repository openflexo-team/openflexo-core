package org.openflexo.market;

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.components.ProgressWindow;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.remoteresources.DefaultFlexoBundle;
import org.openflexo.foundation.remoteresources.FlexoBundle;
import org.openflexo.foundation.remoteresources.FlexoBundle.FlexoBundleType;
import org.openflexo.foundation.remoteresources.FlexoMarketRemoteRepository;
import org.openflexo.foundation.remoteresources.FlexoRemoteRepository;
import org.openflexo.foundation.remoteresources.FlexoRemoteRepositoryImpl;
import org.openflexo.foundation.remoteresources.FlexoUpdateService;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoFIBController;


public class FlexoMarketEditor implements HasPropertyChangeSupport{

	private List<FlexoRemoteRepository> repositories;
	
	private FlexoRemoteRepository repository;
	
	private final PropertyChangeSupport _pcSupport;
	
	private Window owner;
	
	private List<FlexoBundle> selectedBundles;

	private final FlexoUpdateService service;
	
	private String newURL;
	
	public Window getOwner() {
		return owner;
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}
	
	public FlexoMarketEditor(FlexoUpdateService service){
		 _pcSupport = new PropertyChangeSupport(this);
		repositories = new ArrayList<FlexoRemoteRepository>();
		this.service = service;
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
		ProgressWindow.showProgressWindow(owner,FlexoLocalization.localizedForKey(stepname), 1);
		ProgressWindow.instance().setProgress(FlexoLocalization.localizedForKey(stepname));
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
	
	public void addToSelection(FlexoBundle bundle){
		selectedBundles.add(bundle);
		getPropertyChangeSupport().firePropertyChange("selectedBundles", null, null);
	}
	
	public void removeFromSelection(FlexoBundle bundle){
		selectedBundles.remove(bundle);
		getPropertyChangeSupport().firePropertyChange("selectedBundles", null, null);
	}
	
	boolean isInSelection(FlexoBundle bundle){
		return selectedBundles.contains(bundle);
	}
	
	public void update(){
		for(FlexoBundle bundle : getSelectedBundles()){
			service.updateFromRemoteBundle(bundle);
		}
		selectedBundles.clear();
	}

	public void addRepository(){
		getRepositories().add(new FlexoRemoteRepositoryImpl("newRepository", "http://"));
	}
	
	public void addBundle(FlexoRemoteRepository repository){
		repository.getFlexoBundles().add(new DefaultFlexoBundle());
	}

	public static final Resource FLEXO_CREATE_URL_FIB = ResourceLocator.locateResource("Fib/FlexoCreateURL.fib");
	
	public void addURL(FlexoBundle bundle, FlexoRemoteRepository repository){
		try {
			
			FIBComponent fibComponent = FIBLibrary.instance().retrieveFIBComponent(FLEXO_CREATE_URL_FIB);
			FIBDialog dialog = FIBDialog.instanciateAndShowDialog(fibComponent, this, FlexoFrame.getActiveFrame(), true,
					new FlexoFIBController(fibComponent, null));
			if(dialog.getStatus().equals(Status.VALIDATED)){
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
	
