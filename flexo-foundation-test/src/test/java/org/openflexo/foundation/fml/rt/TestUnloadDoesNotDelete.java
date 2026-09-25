/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.io.File;
import java.lang.ref.WeakReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.binding.BindingPathChangeListener;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Unloading a resource releases its objects without executing any business logic (CORE-D-21, CORE-D-22, CORE-D-23).
 *
 * <p>
 * An <code>Item</code> of an <code>UnloadProbe</code> instance has a deletion scheme that increments <code>deletions</code> in an
 * <code>UnloadLedger</code> instance, and a renderer that reads the ledger. The ledger lives in its own resource center, the probe in a
 * project: closing the project removes its resource center, which unloads the probe while the ledger stays loaded and observable.
 *
 * <ul>
 * <li>closing the project must not run the deletion scheme, and the probe must load clean again;</li>
 * <li>unloading must release what the items registered on the ledger, and let them be garbage collected;</li>
 * <li>deleting an item, or the whole probe resource, still runs the deletion scheme - with its container still there.</li>
 * </ul>
 */
@RunWith(OrderedRunner.class)
public class TestUnloadDoesNotDelete extends OpenflexoProjectAtRunTimeTestCase {

	private static final String LEDGER_URI = "http://openflexo.org/test/TestResourceCenter/UnloadLedger.fml";
	private static final String PROBE_URI = "http://openflexo.org/test/TestResourceCenter/UnloadProbe.fml";

	private static VirtualModel ledgerModel;
	private static VirtualModel probeModel;
	private static FlexoConcept itemConcept;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	private static FMLRTVirtualModelInstance ledger;
	private static String probeResourceURI;

	@Test
	@TestOrder(1)
	public void testLoadVirtualModels() throws Exception {
		instanciateTestServiceManager();
		ledgerModel = serviceManager.getVirtualModelLibrary().getVirtualModel(LEDGER_URI);
		probeModel = serviceManager.getVirtualModelLibrary().getVirtualModel(PROBE_URI);
		assertNotNull(ledgerModel);
		assertNotNull(probeModel);
		itemConcept = probeModel.getFlexoConcept("Item");
		assertNotNull(itemConcept);
		assertVirtualModelIsValid(ledgerModel);
		assertVirtualModelIsValid(probeModel);
	}

	@Test
	@TestOrder(2)
	public void testCreateInstances() throws Exception {
		DirectoryResourceCenter ledgerRC = makeNewDirectoryResourceCenter();
		editor = createStandaloneProject("TestUnloadDoesNotDelete");
		project = (FlexoProject<File>) editor.getProject();

		ledger = createVMI(ledgerRC.getVirtualModelInstanceRepository().getRootFolder(), "Ledger", ledgerModel, "main");
		FMLRTVirtualModelInstance probe = createVMI(project.getVirtualModelInstanceRepository().getRootFolder(), "Probe", probeModel,
				ledger);
		probeResourceURI = probe.getResource().getURI();

		FlexoConceptInstance item = createItem(probe, "i1");
		assertEquals("i1@main", item.getStringRepresentation());
		assertTrue("Rendering an item should make it listen to the ledger", listenersLeftByItems() > 0);
		assertEquals(0, deletions());

		ledger.getResource().save();
		probe.getResource().save();
	}

	/**
	 * The reported defect: closing a project ran the deletion schemes of every loaded instance
	 */
	@Test
	@TestOrder(3)
	public void testClosingProjectDoesNotRunDeletionSchemes() throws Exception {
		assertTrue(probeResource().isLoaded());

		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();

		assertEquals("Closing the project ran the deletion scheme of an item", 0, deletions());
		assertTrue(ledger.getResource().isLoaded());
		assertEquals("Items of the closed project still listen to the ledger", 0, listenersLeftByItems());
	}

	@Test
	@TestOrder(4)
	public void testReloadedResourceLoadsClean() throws Exception {
		FMLRTVirtualModelInstanceResource probeResource = probeResource();
		assertNotNull(probeResource);
		assertFalse(probeResource.isLoaded());

		FMLRTVirtualModelInstance probe = probeResource.getResourceData();
		assertEquals(1, probe.getFlexoConceptInstances(itemConcept).size());
		FlexoConceptInstance item = probe.getFlexoConceptInstances(itemConcept).get(0);
		assertEquals("i1", item.execute("name"));
		assertEquals("i1@main", item.getStringRepresentation());
		assertEquals(0, deletions());
	}

	/**
	 * What unloading must release, asserted directly: the listeners registered on the ledger, and every reference that would keep an item
	 * alive
	 */
	@Test
	@TestOrder(5)
	public void testUnloadReleasesItems() throws Exception {
		FMLRTVirtualModelInstanceResource probeResource = probeResource();
		FlexoConceptInstance item = probeResource.getResourceData().getFlexoConceptInstances(itemConcept).get(0);
		assertEquals("i1@main", item.getStringRepresentation());
		assertTrue(listenersLeftByItems() > 0);

		WeakReference<FlexoConceptInstance> itemRef = new WeakReference<>(item);
		item = null;

		probeResource.unloadResourceData(false);

		assertFalse(probeResource.isLoaded());
		assertEquals("Unloading ran the deletion scheme of an item", 0, deletions());
		assertEquals("Unloaded items still listen to the ledger", 0, listenersLeftByItems());
		assertTrue("An unloaded item is still referenced", isCollected(itemRef));
	}

	@Test
	@TestOrder(6)
	public void testDeletingAnItemStillRunsItsDeletionScheme() throws Exception {
		FMLRTVirtualModelInstance probe = probeResource().getResourceData();
		FlexoConceptInstance item = probe.getFlexoConceptInstances(itemConcept).get(0);
		assertEquals("i1@main", item.getStringRepresentation());

		item.delete();

		assertTrue(item.isDeleted());
		assertEquals(1, deletions());
		assertEquals("A deleted item still listens to the ledger", 0, listenersLeftByItems());
	}

	/**
	 * Deleting the resource deletes its data, and PAMELA cascades that deletion over the embedded instances. Their deletion schemes must
	 * still find their container (CORE-D-22).
	 */
	@Test
	@TestOrder(7)
	public void testDeletingTheResourceRunsDeletionSchemesWithTheirContainer() throws Exception {
		FMLRTVirtualModelInstance probe = probeResource().getResourceData();
		createItem(probe, "i2");
		createItem(probe, "i3");
		assertEquals(1, deletions());

		probeResource().delete();

		assertEquals("The deletion schemes of the items did not run, or not with their container", 3, deletions());
	}

	/**
	 * FlexoConceptInstance.execute() builds a DataBinding for one evaluation and deletes it: it must not leave a listener behind (CORE-D-23)
	 */
	@Test
	@TestOrder(8)
	public void testExecuteLeavesNoListener() throws Exception {
		deletions();
		int before = ledger.getPropertyChangeSupport().getPropertyChangeListeners().length;
		for (int i = 0; i < 5; i++) {
			deletions();
		}
		assertEquals(before, ledger.getPropertyChangeSupport().getPropertyChangeListeners().length);
	}

	/** The probe resource of the current project: reloading the project builds a new resource object */
	private static FMLRTVirtualModelInstanceResource probeResource() {
		return (FMLRTVirtualModelInstanceResource) project.getResource(probeResourceURI);
	}

	private static int deletions() throws Exception {
		return ((Number) ledger.execute("this.deletions")).intValue();
	}

	/**
	 * Number of binding listeners registered on the ledger that are not the ledger's own: a live one of an item, or a deleted one still
	 * registered
	 */
	private static int listenersLeftByItems() {
		int count = 0;
		for (PropertyChangeListener l : ledger.getPropertyChangeSupport().getPropertyChangeListeners()) {
			Object listener = l instanceof PropertyChangeListenerProxy ? ((PropertyChangeListenerProxy) l).getListener() : l;
			if (listener instanceof BindingPathChangeListener) {
				Object context = ((BindingPathChangeListener<?>) listener).getContext();
				if (context == null || (context instanceof FlexoConceptInstance
						&& ((FlexoConceptInstance) context).getFlexoConcept() == itemConcept)) {
					count++;
				}
			}
		}
		return count;
	}

	private static boolean isCollected(WeakReference<?> ref) throws InterruptedException {
		for (int i = 0; i < 20 && ref.get() != null; i++) {
			System.gc();
			Thread.sleep(50);
		}
		return ref.get() == null;
	}

	private static FMLRTVirtualModelInstance createVMI(RepositoryFolder<?, ?> folder, String name, VirtualModel vm, Object argument) {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(folder, null, editor);
		action.setNewVirtualModelInstanceName(name);
		action.setNewVirtualModelInstanceTitle(name);
		action.setVirtualModel(vm);
		CreationScheme cs = vm.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.setParameterValue(cs.getParameters().get(0), argument);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertNull(action.getThrownException());
		return action.getNewVirtualModelInstance();
	}

	private static FlexoConceptInstance createItem(FMLRTVirtualModelInstance probe, String name) {
		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(probe, null, editor);
		action.setFlexoConcept(itemConcept);
		CreationScheme cs = itemConcept.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.setParameterValue(cs.getParameter("name"), name);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}
}
