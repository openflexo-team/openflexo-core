package org.openflexo.foundation.lsp;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class TestLSPService extends OpenflexoTestCase {

	static FlexoEditor editor;
	private static CompilationUnitResource fmlResource;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
	}

	@Test
	@TestOrder(3)
	public void loadFMLResource() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		VirtualModelLibrary vmLibrary = serviceManager.getVirtualModelLibrary();
		assertNotNull(vmLibrary);
		VirtualModel virtualModel = vmLibrary.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(virtualModel);

		fmlResource = virtualModel.getResource();
		assertNotNull(fmlResource);
	}

	@Test
	@TestOrder(4)
	public void testInitLSP() {

		// LSPService initialization
		LSPService lspService = serviceManager.getService(LSPService.class);
		assertNotNull(lspService);
		
		//TODO find a better way to lock the test
		while(true) {
			
		}

	}

}
