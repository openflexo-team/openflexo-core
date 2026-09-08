/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.rm.Resource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test what a container component looks like ON DISK, in both of the two shapes the platform has to handle.
 *
 * <p>
 * The <code>TestContainerUI.fml</code> fixture deliberately ships both:
 * <ul>
 * <li>the components OF THE VIRTUAL MODEL (<code>TestContainerUI.fib</code> / <code>TestContainerUI.inspector</code>) are stored in the
 * <b>canonical</b> shape, i.e. exactly the bytes {@link FIBComponentResource#save()} produces. This is what an author gets back the first
 * time a component is saved from the FIB editor, and asserting it here is what catches a serialization that silently degrades - the
 * <code>p:modelEntity</code> attribute a factory that cannot name {@link FIBInspector} writes instead of the entity itself;</li>
 * <li>the components of the CONCEPTS (<code>Simple.fib</code> / <code>Simple.inspector</code>, and the variants) are kept in the
 * <b>legacy</b> shape - a <code>&lt;Panel className="…FIBInspector" dataClassName="…"&gt;</code> - which is how the 400-odd
 * <code>.inspector</code> files of the infrastructure are written, and what a migration to the container mechanism will keep feeding us.
 * Nothing else covers the reading of that shape, whose deserialization lives in an explicitly untrusted corner of
 * <code>XMLSaxDeserializer</code>.</li>
 * </ul>
 *
 * <p>
 * The canonical shape asserted here is the one of an <b>unbound</b> component: its <code>data</code> variable carries the
 * <code>FlexoConceptInstance</code> class its former <code>dataClassName</code> named. Showing a component in the application binds it -
 * <code>FMLControlledComponent.bindToConcept</code> retypes <code>data</code> by the driving concept, in place - so saving one of these two
 * files from the FIB editor legitimately rewrites it, and turns this test red until the fixture is regenerated. That is the intended
 * warning, not a defect: it is the only thing that says the editor has written on a fixture.
 *
 * <p>
 * What this does NOT cover: that {@link FIBComponentResource#save()} itself passes {@link FIBComponentResource#makeModelFactory()} to the
 * library. Asserting that would mean writing on the fixture, so the test asserts what the factory produces, not the plumbing that calls it.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestContainerUIComponentSerialization extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		assertNotNull(serviceManager);

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 10, virtualModel.getFlexoConcepts().size());
	}

	/**
	 * The view of the VirtualModel is stored exactly as it is written back.
	 */
	@Test
	@TestOrder(2)
	public void test1CanonicalViewRoundTripsByteForByte() throws Exception {
		assertSerializesToItsOwnBytes(virtualModel.getUIComponentFlexoResource());
	}

	/**
	 * Same for its inspector, which is the interesting one: a {@link FIBInspector} is not reachable from {@link FIBComponent}, so only a
	 * factory declaring it can name it. Serialized by any other, it comes back as a <code>&lt;Inspector&gt;</code> carrying a
	 * <code>p:modelEntity</code> attribute - and its own properties, should it ever declare any, are dropped in silence.
	 */
	@Test
	@TestOrder(3)
	public void test2CanonicalInspectorRoundTripsByteForByte() throws Exception {
		assertSerializesToItsOwnBytes(virtualModel.getInspectorComponentFlexoResource());
	}

	/** And what a canonical inspector declares is read back: the entity itself, not the FIBPanel it extends. */
	@Test
	@TestOrder(4)
	public void test3CanonicalInspectorIsAGenuineInspector() {

		FIBComponent component = virtualModel.getInspectorComponentFlexoResource().getComponent();

		assertNotNull(component);
		assertTrue("Expected a FIBInspector, got " + component.getClass(), component instanceof FIBInspector);
	}

	/**
	 * The legacy shape still loads: <code>className</code> is what makes a <code>&lt;Panel&gt;</code> deserialize as a
	 * {@link FIBInspector}, and <code>dataClassName</code> is translated into the type of the <code>data</code> variable.
	 */
	@Test
	@TestOrder(5)
	public void test4LegacyShapeStillLoadsAsAnInspector() {

		FIBComponentResource resource = concept("Simple").getInspectorComponentFlexoResource();
		assertNotNull(resource);

		FIBComponent component = resource.getComponent();
		assertNotNull("The legacy shape did not load", component);
		assertTrue("The legacy className attribute was not honoured: got " + component.getClass(), component instanceof FIBInspector);
		assertEquals("SimpleInspector", component.getName());

		// dataClassName is deprecated, and read as the type of the 'data' variable - which is what getInspectedClass() reads back
		assertEquals(org.openflexo.foundation.fml.rt.FlexoConceptInstance.class, ((FIBContainer) component).getDataClass());
	}

	/**
	 * Serialize supplied component with the factory its resource reads and writes with, and compare with the artefact on disk.
	 *
	 * <p>
	 * The comparison is on the bytes, which is the point: a difference of shape - an attribute the serializer adds, an entity it cannot
	 * name - is exactly what has to be seen here rather than in a diff, months later, on the day someone saves a component from the editor.
	 */
	private void assertSerializesToItsOwnBytes(FIBComponentResource resource) throws FlexoException, IOException {

		assertNotNull(resource);

		Resource artefact = resource.getIODelegate().getSerializationArtefactAsResource();
		FIBComponent component = resource.getComponent();
		assertNotNull("Could not load " + resource.getURI(), component);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ApplicationFIBLibraryImpl.instance(serviceManager.getTechnologyAdapterService()).saveComponentToStream(component, artefact, out,
				resource.makeModelFactory());

		String serialized = normalize(out.toString(StandardCharsets.UTF_8.name()));
		String onDisk = normalize(read(artefact));

		if (!serialized.equals(onDisk)) {
			// The fixture is meant to BE the canonical form, so a mismatch is most often a fixture to refresh: hand over what the
			// serializer produced rather than a diff of two 400-character lines
			File produced = new File(System.getProperty("java.io.tmpdir"), artefact.getRelativePath().replace('/', '_'));
			Files.write(produced.toPath(), serialized.getBytes(StandardCharsets.UTF_8));
			assertEquals(artefact.getRelativePath() + " is not what the serializer produces - produced content written to " + produced,
					onDisk, serialized);
		}
	}

	private static String read(Resource artefact) throws IOException {
		try (InputStream in = artefact.openInputStream()) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int read;
			while ((read = in.read(chunk)) > 0) {
				buffer.write(chunk, 0, read);
			}
			return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	/** Line endings only: the fixture is stored with LF, and this is not what the test is about. */
	private static String normalize(String content) throws UnsupportedEncodingException {
		return content.replace("\r\n", "\n").trim();
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}
}
