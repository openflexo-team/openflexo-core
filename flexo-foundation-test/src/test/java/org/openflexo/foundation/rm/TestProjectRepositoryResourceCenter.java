/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.rm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A repository of a project belongs to the delegate resource center of the project, whichever of the two it was asked for (CORE-D-38).
 *
 * <p>
 * Which one asks first is a race: the test thread asks the project for its repository, which goes to the delegate; the Swing event
 * thread, through <code>FMLRTTechnologyAdapter.getVirtualModelInstanceRepositories()</code>, asks the technology adapter for the
 * repository of every resource center, the project among them. A repository built for the project gave its resources the project as
 * resource center: closing the project then took their URI away, while the reopened project keeps the delegate and the stale resources
 * in it. This test makes the second path win, deterministically, by holding the event thread back.
 */
@RunWith(OrderedRunner.class)
public class TestProjectRepositoryResourceCenter extends OpenflexoProjectAtRunTimeTestCase {

	private static final String LEDGER_URI = "http://openflexo.org/test/TestResourceCenter/UnloadLedger.fml";
	private static final String PROBE_URI = "http://openflexo.org/test/TestResourceCenter/UnloadProbe.fml";

	private static VirtualModel ledgerModel;
	private static VirtualModel probeModel;
	private static FlexoConcept itemConcept;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static DirectoryResourceCenter ledgerRC;
	private static String probeResourceURI;

	/** The repository is first asked for the project itself, not for its delegate */
	@Test
	@TestOrder(1)
	public void testRepositoryAskedForTheProjectBelongsToItsDelegate() throws Exception {
		instanciateTestServiceManager();
		ledgerModel = serviceManager.getVirtualModelLibrary().getVirtualModel(LEDGER_URI);
		probeModel = serviceManager.getVirtualModelLibrary().getVirtualModel(PROBE_URI);
		assertNotNull(ledgerModel);
		assertNotNull(probeModel);
		itemConcept = probeModel.getFlexoConcept("Item");
		FMLRTTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);

		// Hold the event thread back while the project is created: it would otherwise build the repository through the delegate
		CountDownLatch gate = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> {
			try {
				gate.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		FMLRTVirtualModelInstanceRepository<?> repository;
		try {
			ledgerRC = makeNewDirectoryResourceCenter();
			editor = createStandaloneProject("TestProjectRepositoryResourceCenter");
			project = (FlexoProject<File>) editor.getProject();
			repository = ta.getVirtualModelInstanceRepository(project);
		} finally {
			gate.countDown();
		}
		SwingUtilities.invokeAndWait(() -> {
		});

		assertSame(project.getDelegateResourceCenter(), repository.getResourceCenter());
		assertSame(repository, project.getVirtualModelInstanceRepository());
	}

	@Test
	@TestOrder(2)
	public void testResourcesOfTheProjectBelongToItsDelegate() throws Exception {
		FMLRTVirtualModelInstance ledger = createVMI(ledgerRC.getVirtualModelInstanceRepository().getRootFolder(), "Ledger", ledgerModel,
				"main");
		FMLRTVirtualModelInstance probe = createVMI(project.getVirtualModelInstanceRepository().getRootFolder(), "Probe", probeModel,
				ledger);
		createItem(probe, "i1");
		probeResourceURI = probe.getResource().getURI();

		assertSame(project.getDelegateResourceCenter(), probe.getResource().getResourceCenter());
		assertTrue(probeResourceURI.startsWith(project.getProjectURI()));

		ledger.getResource().save();
		probe.getResource().save();
	}

	/** Closing the project must not take the URI of its resources away: the reopened project finds them again by it */
	@Test
	@TestOrder(3)
	public void testReopenedProjectFindsItsResourceByItsURI() throws Exception {
		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();

		FMLRTVirtualModelInstanceResource probeResource = (FMLRTVirtualModelInstanceResource) project.getResource(probeResourceURI);
		assertNotNull("The reopened project does not find its resource by its URI", probeResource);
		assertEquals(probeResourceURI, probeResource.getURI());
		assertSame(project.getDelegateResourceCenter(), probeResource.getResourceCenter());
		assertFalse("Closing the project should have unloaded the resource", probeResource.isLoaded());

		FMLRTVirtualModelInstance probe = probeResource.getResourceData();
		assertEquals(1, probe.getFlexoConceptInstances(itemConcept).size());
		assertEquals("i1", probe.getFlexoConceptInstances(itemConcept).get(0).execute("name"));
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
