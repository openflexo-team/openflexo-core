package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceIssue;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.validation.ResourceIssueError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A resource whose VirtualModel (or dependency, or technology adapter) cannot be found carries the error itself, from its metadata, without
 * being loaded (CORE-F: errors at the resource layer).
 */
@RunWith(OrderedRunner.class)
public class TestResourceIssues extends OpenflexoProjectAtRunTimeTestCase {

	private static final String MISSING_VM_URI = "http://openflexo.org/test/Nowhere/MissingVirtualModel.fml";

	private static VirtualModel virtualModel;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstanceResource original;
	private static FMLRTVirtualModelInstanceResource orphanResource;

	@Test
	@TestOrder(1)
	public void test0Setup() throws Exception {
		instanciateTestServiceManager();
		virtualModel = serviceManager.getVirtualModelLibrary()
				.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelB.fml");
		assertNotNull(virtualModel);
		editor = createStandaloneProject("TestResourceIssues");
		project = (FlexoProject<File>) editor.getProject();

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("Original");
		action.setNewVirtualModelInstanceTitle("Original");
		action.setVirtualModel(virtualModel);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		original = (FMLRTVirtualModelInstanceResource) action.getNewVirtualModelInstance().getResource();
		original.save();
		assertTrue("A resource with its VirtualModel carries no issue", original.getIssues().isEmpty());
		assertFalse(original.hasErrors());
		assertNull(original.getIssuesMessage());
	}

	/** The API: issues are notified, are not duplicated, and are removed by kind and subject. */
	@Test
	@TestOrder(2)
	public void test1IssuesApi() {
		List<String> events = new ArrayList<>();
		PropertyChangeListener l = evt -> events.add(evt.getPropertyName());
		original.getPropertyChangeSupport().addPropertyChangeListener(l);
		try {
			ResourceIssue issue = new ResourceIssue(ResourceIssue.Severity.ERROR, ResourceIssue.Kind.MISSING_DEPENDENCY, "http://x/Dep.fml");
			original.addIssue(issue);
			original.addIssue(issue);
			assertEquals(1, original.getIssues().size());
			assertTrue(original.hasErrors());
			assertTrue(events.contains(FlexoResource.ISSUES));
			assertTrue(events.contains("hasErrors"));
			assertNotNull(original.getIssuesMessage());
			assertTrue(original.getIssuesMessage().contains("http://x/Dep.fml"));

			original.removeIssues(ResourceIssue.Kind.MISSING_DEPENDENCY, "http://other");
			assertEquals(1, original.getIssues().size());
			original.removeIssues(ResourceIssue.Kind.MISSING_DEPENDENCY, "http://x/Dep.fml");
			assertTrue(original.getIssues().isEmpty());
			assertFalse(original.hasErrors());
		} finally {
			original.getPropertyChangeSupport().removePropertyChangeListener(l);
		}
	}

	/**
	 * A .fml.rt on disk naming a VirtualModel that exists nowhere: retrieved by the resource layer, it is in error, and nothing was loaded.
	 */
	@Test
	@TestOrder(3)
	public void test2MissingVirtualModelMarksTheResource() throws Exception {
		// The resource is the file Original.fml.rt, in a directory of the same name (the container of the instance)
		File originalFile = ((FileIODelegate) original.getIODelegate()).getFile();
		File originalDir = originalFile.isDirectory() ? originalFile : originalFile.getParentFile();
		File copyDir = new File(originalDir.getParentFile(), "Orphan.fml.rt");
		copyTree(originalDir.toPath(), copyDir.toPath());
		assertTrue(new File(copyDir, "Original.fml.rt.xml").renameTo(new File(copyDir, "Orphan.fml.rt.xml")));
		// Rewrite the identity and the VirtualModel URI of the copy
		try (Stream<Path> files = Files.walk(copyDir.toPath())) {
			for (Path f : files.filter(Files::isRegularFile).collect(Collectors.toList())) {
				String content = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
				if (content.contains("virtualModelURI=\"")) {
					content = content.replaceAll("virtualModelURI=\"[^\"]*\"", "virtualModelURI=\"" + MISSING_VM_URI + "\"");
					content = content.replaceAll(" uri=\"[^\"]*\"", " uri=\"" + original.getURI() + "Orphan\"");
					Files.write(f, content.getBytes(StandardCharsets.UTF_8));
				}
			}
		}

		FMLRTVirtualModelInstanceResourceFactory factory = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter.class)
				.getFMLRTVirtualModelInstanceResourceFactory();
		FMLRTVirtualModelInstanceResource orphan = factory.retrieveFMLRTVirtualModelInstanceResource(copyDir,
				project.getDelegateResourceCenter());
		assertNotNull(orphan);

		assertFalse("The check must not need the resource to be loaded", orphan.isLoaded());
		assertTrue(orphan.hasErrors());
		assertEquals(1, orphan.getIssues().size());
		ResourceIssue issue = orphan.getIssues().get(0);
		assertEquals(ResourceIssue.Kind.MISSING_VIRTUAL_MODEL, issue.getKind());
		assertEquals(MISSING_VM_URI, issue.getSubject());
		assertTrue(orphan.getIssuesMessage().contains(MISSING_VM_URI));
		assertTrue("The healthy instance is untouched", original.getIssues().isEmpty());
		orphanResource = orphan;
	}

	/** The validation of the project reports the error of the resource, and selects the resource that carries it. */
	@Test
	@TestOrder(4)
	public void test3ProjectValidationReportsTheResourceError() throws Exception {
		assertNotNull(orphanResource);
		ValidationReport report = project.getProjectValidationModel().validate(project);
		assertNotNull(report);
		ResourceIssueError found = null;
		for (ValidationIssue<?, ?> issue : report.getAllErrors()) {
			if (issue instanceof ResourceIssueError && ((ResourceIssueError) issue).getResource() == orphanResource) {
				found = (ResourceIssueError) issue;
			}
		}
		assertNotNull("The project validation does not report the missing VirtualModel of the resource", found);
		assertEquals(MISSING_VM_URI, found.getSubject());
		String message = project.getProjectValidationModel().localizedIssueMessage(found);
		assertTrue(message, message.contains(MISSING_VM_URI));
		assertTrue(message, message.contains(orphanResource.getName()));
		assertFalse("The validation must not load the resource", orphanResource.isLoaded());
	}

	/**
	 * The healthy resources of the test resource center carry no issue: an import or a use that is only resolved later must not leave a
	 * permanent error (false positive).
	 */
	@Test
	@TestOrder(5)
	public void test4NoIssueOnHealthyResources() {
		List<String> withIssues = new ArrayList<>();
		for (FlexoResource<?> r : new ArrayList<>(serviceManager.getResourceManager().getRegisteredResources())) {
			if (r != orphanResource && !r.getIssues().isEmpty()) {
				withIssues.add(r.getURI() + " -> " + r.getIssues());
			}
		}
		assertTrue("Resources in error: " + withIssues, withIssues.isEmpty());
	}

	private static void copyTree(Path from, Path to) throws Exception {
		try (Stream<Path> files = Files.walk(from)) {
			for (Path f : files.collect(Collectors.toList())) {
				Path target = to.resolve(from.relativize(f).toString());
				if (Files.isDirectory(f)) {
					Files.createDirectories(target);
				}
				else {
					Files.copy(f, target);
				}
			}
		}
	}
}
