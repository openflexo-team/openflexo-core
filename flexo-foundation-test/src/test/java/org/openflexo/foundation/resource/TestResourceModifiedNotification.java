package org.openflexo.foundation.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;

/**
 * A resource notifies its modified state under <code>isModified</code>, the property a binding such as
 * <code>resource.isModified</code> (a browser label) listens to: once when it becomes modified, once when it no longer is (here
 * by unloading a PAMELA resource, whose unloading does not go through {@link FlexoResourceImpl#unloadResourceData(boolean)}).
 */
public class TestResourceModifiedNotification extends OpenflexoTestCase {

	@Test
	public void testModifiedStateIsNotifiedOncePerTransition() throws Exception {
		instanciateTestServiceManager();
		DirectoryResourceCenter rc = makeNewDirectoryResourceCenter();
		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResource resource = fmlTA.getCompilationUnitResourceFactory().makeTopLevelCompilationUnitResource("NotifiedVM",
				"http://openflexo.org/test/TestResourceModifiedNotification/NotifiedVM.fml", fmlTA.getGlobalRepository(rc).getRootFolder(),
				true);
		assertFalse(resource.isModified());

		List<PropertyChangeEvent> events = new ArrayList<>();
		resource.getPropertyChangeSupport().addPropertyChangeListener("isModified", events::add);

		resource.getLoadedResourceData().getVirtualModel().setDescription("changed");
		assertTrue(resource.isModified());
		assertEquals(1, events.size());
		assertEquals(Boolean.TRUE, events.get(0).getNewValue());

		// A second edit of modified data is no transition
		resource.getLoadedResourceData().getVirtualModel().setDescription("changed again");
		assertEquals(1, events.size());

		resource.unloadResourceData(false);
		assertFalse(resource.isModified());
		assertEquals(2, events.size());
		assertEquals(Boolean.FALSE, events.get(1).getNewValue());
	}
}
