package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
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
 * Closing a project unloads its resources without deleting their data (CORE-D-21): the objects of that generation of the data stay alive
 * wherever something still points at them. A reference to such an object, held by a model that outlives the project, must not keep
 * answering it once the project is reopened: it must answer the object of the CURRENT data, otherwise the same conceptual object exists
 * twice and an identity comparison fails for some objects and not for others (CORE-D-28).
 *
 * <p>
 * The <code>UnloadHolder</code> instance lives in a resource center of its own, which stays loaded, and its role points at an
 * <code>UnloadProbe</code> instance of the project.
 */
@RunWith(OrderedRunner.class)
public class TestReloadedResourceReferences extends OpenflexoProjectAtRunTimeTestCase {

	private static final String LEDGER_URI = "http://openflexo.org/test/TestResourceCenter/UnloadLedger.fml";
	private static final String PROBE_URI = "http://openflexo.org/test/TestResourceCenter/UnloadProbe.fml";
	private static final String HOLDER_URI = "http://openflexo.org/test/TestResourceCenter/UnloadHolder.fml";

	private static VirtualModel ledgerModel;
	private static VirtualModel probeModel;
	private static VirtualModel holderModel;
	private static FlexoConcept itemConcept;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	private static FMLRTVirtualModelInstance holder;
	private static FMLRTVirtualModelInstance firstGenerationProbe;
	private static FlexoConceptInstance firstGenerationItem;
	private static String probeResourceURI;

	@Test
	@TestOrder(1)
	public void testCreateInstances() throws Exception {
		instanciateTestServiceManager();
		ledgerModel = serviceManager.getVirtualModelLibrary().getVirtualModel(LEDGER_URI);
		probeModel = serviceManager.getVirtualModelLibrary().getVirtualModel(PROBE_URI);
		holderModel = serviceManager.getVirtualModelLibrary().getVirtualModel(HOLDER_URI);
		assertNotNull(ledgerModel);
		assertNotNull(probeModel);
		assertNotNull(holderModel);
		itemConcept = probeModel.getFlexoConcept("Item");

		DirectoryResourceCenter rc = makeNewDirectoryResourceCenter();
		editor = createStandaloneProject("TestReloadedResourceReferences");
		project = (FlexoProject<File>) editor.getProject();

		RepositoryFolder<?, ?> rcFolder = rc.getVirtualModelInstanceRepository().getRootFolder();
		FMLRTVirtualModelInstance ledger = createVMI(rcFolder, "Ledger", ledgerModel, "main");
		firstGenerationProbe = createVMI(project.getVirtualModelInstanceRepository().getRootFolder(), "Probe", probeModel, ledger);
		probeResourceURI = firstGenerationProbe.getResource().getURI();
		firstGenerationItem = createItem(firstGenerationProbe, "i1");
		holder = createVMI(rcFolder, "Holder", holderModel, firstGenerationItem);

		ledger.getResource().save();
		firstGenerationProbe.getResource().save();
		holder.getResource().save();
	}

	/** Resolves the reference once, against the first generation of the project: this is what caches the object */
	@Test
	@TestOrder(2)
	public void testReferenceResolvesBeforeReload() throws Exception {
		assertSame(firstGenerationItem, holder.execute("item"));
	}

	@Test
	@TestOrder(3)
	public void testReferenceAnswersTheCurrentDataAfterReload() throws Exception {
		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();

		FMLRTVirtualModelInstanceResource probeResource = probeResource();
		assertNotNull(probeResource);
		assertFalse("Closing the project should have unloaded the probe", probeResource.isLoaded());
		assertTrue(holder.getResource().isLoaded());

		FMLRTVirtualModelInstance probe = probeResource.getResourceData();
		assertNotSame("Loading the resource again should build new data", firstGenerationProbe, probe);
		FlexoConceptInstance item = probe.getFlexoConceptInstances(itemConcept).get(0);
		assertNotSame(firstGenerationItem, item);

		Object referenced = holder.execute("item");
		assertNotNull(referenced);
		assertSame("The reference still answers an object of the data dropped when the project was closed", item, referenced);
	}

	@Test
	@TestOrder(4)
	public void testIdentityHoldsBetweenInstancesOfTheReloadedProject() throws Exception {
		FMLRTVirtualModelInstance probe = probeResource().getResourceData();
		FlexoConceptInstance item = probe.getFlexoConceptInstances(itemConcept).get(0);
		FlexoConceptInstance referenced = (FlexoConceptInstance) holder.execute("item");
		assertSame(item, referenced);
		assertSame(probe, referenced.getVirtualModelInstance());
		assertEquals("i1", referenced.execute("name"));
	}

	/** The probe resource of the current project: reloading the project builds a new resource object */
	private static FMLRTVirtualModelInstanceResource probeResource() {
		return (FMLRTVirtualModelInstanceResource) project.getResource(probeResourceURI);
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
