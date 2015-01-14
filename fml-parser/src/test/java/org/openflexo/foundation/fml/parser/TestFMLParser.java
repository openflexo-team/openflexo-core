package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

public class TestFMLParser extends OpenflexoTestCase {

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

	@Test
	public void test0() {
		testFMLCompilationUnit(ResourceLocator.locateResource("FMLExamples/Test0.fml"));
	}

	/*@Test
	public void test1() {
		testFMLCompilationUnit(ResourceLocator.locateResource("FMLExamples/Test1.fml"));
	}*/

	private void testFMLCompilationUnit(Resource fileResource) {
		try {
			FMLParser.parse(((FileResourceImpl) fileResource).getFile(), serviceManager);
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
