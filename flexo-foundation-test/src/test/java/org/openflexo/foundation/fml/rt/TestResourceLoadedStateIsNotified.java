/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A browser shows a resource greyed until it is loaded, through a binding such as <code>enabled="resource.isLoaded"</code> - and a FIB
 * binding is re-read on a PropertyChangeSupport event only. This asserts that the loaded state of a resource is notified that way, on each
 * of the three paths it changes by: created with empty contents, unloaded, loaded from disk.
 *
 * <p>
 * The creation path is the one a user meets first, and the one that showed the defect: <code>FlexoResourceFactory.makeResource</code>
 * REGISTERS the resource - which is when a browser builds its row, and reads it as unloaded - and only then gives it its contents. The test
 * reproduces that exact sequence: it listens to the repository folder, and attaches its own listener to the resource the moment the folder
 * receives it, before it holds any data.
 *
 * <p>
 * What this does not cover: the repaint of the browser itself, which needs a running application.
 */
@RunWith(OrderedRunner.class)
public class TestResourceLoadedStateIsNotified extends OpenflexoProjectAtRunTimeTestCase {

	/** The name a binding such as <code>resource.isLoaded</code> listens to. */
	private static final String IS_LOADED = "isLoaded";

	private static VirtualModel virtualModel;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	private static FMLRTVirtualModelInstanceResource newResource;
	private static Boolean loadedWhenFirstSeen;
	private static final List<Boolean> loadedStates = new ArrayList<>();

	/**
	 * Records what a binding reads when it is told about <code>isLoaded</code>: a FIB binding does not use the value an event carries, it
	 * re-reads the getter. (Some events of that name do carry the resource data rather than a boolean.)
	 */
	private static final PropertyChangeListener RESOURCE_LISTENER = evt -> {
		if (IS_LOADED.equals(evt.getPropertyName()) && evt.getSource() instanceof FMLRTVirtualModelInstanceResource) {
			loadedStates.add(((FMLRTVirtualModelInstanceResource) evt.getSource()).isLoaded());
		}
	};

	@Test
	@TestOrder(1)
	public void test0LoadVirtualModel() throws Exception {

		instanciateTestServiceManager();
		virtualModel = serviceManager.getVirtualModelLibrary().getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelB.fml");
		assertNotNull(virtualModel);
	}

	@Test
	@TestOrder(2)
	public void test1CreateProject() {

		editor = createStandaloneProject("TestResourceLoadedState");
		project = (FlexoProject<File>) editor.getProject();
		assertNotNull(project);
	}

	/**
	 * The path of the reported defect: a VirtualModelInstance created from the UI stayed greyed in the main browser.
	 */
	@Test
	@TestOrder(3)
	public void test2CreatedResourceNotifiesItIsLoaded() {

		RepositoryFolder<?, ?> folder = project.getVirtualModelInstanceRepository().getRootFolder();

		// What a browser does: build the row of a resource when its folder announces it, and listen to it from then on
		PropertyChangeListener folderListener = evt -> {
			if (RepositoryFolder.RESOURCES_KEY.equals(evt.getPropertyName()) && evt.getNewValue() instanceof FMLRTVirtualModelInstanceResource
					&& newResource == null) {
				newResource = (FMLRTVirtualModelInstanceResource) evt.getNewValue();
				loadedWhenFirstSeen = newResource.isLoaded();
				newResource.getPropertyChangeSupport().addPropertyChangeListener(RESOURCE_LISTENER);
			}
		};

		folder.getPropertyChangeSupport().addPropertyChangeListener(folderListener);
		try {
			CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(folder, null, editor);
			action.setNewVirtualModelInstanceName("MyInstance");
			action.setNewVirtualModelInstanceTitle("MyInstance");
			action.setVirtualModel(virtualModel);
			action.doAction();
			assertTrue(action.hasActionExecutionSucceeded());
		} finally {
			folder.getPropertyChangeSupport().removePropertyChangeListener(folderListener);
		}

		assertNotNull("The repository folder never announced the new resource", newResource);
		// Otherwise the sequence reproduced here is not the one a browser goes through, and the assertions below prove nothing
		assertEquals("The resource was already loaded when its folder announced it", Boolean.FALSE, loadedWhenFirstSeen);

		assertTrue(newResource.isLoaded());
		assertEquals("No 'isLoaded' event reached a listener of the new resource: a browser keeps its row greyed", Boolean.TRUE,
				last(loadedStates));
	}

	@Test
	@TestOrder(4)
	public void test3UnloadedResourceNotifiesItIsNotLoaded() {

		loadedStates.clear();
		newResource.unloadResourceData(false);

		assertFalse(newResource.isLoaded());
		assertEquals("No 'isLoaded' event on unloading: a browser would keep the row enabled", Boolean.FALSE, last(loadedStates));
	}

	/** Loading from disk goes through neither of the two paths above: the data is assigned in place, then notified. */
	@Test
	@TestOrder(5)
	public void test4ReloadedResourceNotifiesItIsLoaded() throws Exception {

		loadedStates.clear();
		assertNotNull(newResource.getResourceData());

		assertTrue(newResource.isLoaded());
		assertEquals("No 'isLoaded' event on loading from disk", Boolean.TRUE, last(loadedStates));
	}

	private static Boolean last(List<Boolean> states) {
		return states.isEmpty() ? null : states.get(states.size() - 1);
	}
}
