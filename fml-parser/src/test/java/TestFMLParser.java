import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.foundation.fml.parser.FMLParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

public class TestFMLParser {

	@Test
	public void test1() {
		testFMLCompilationUnit(ResourceLocator.locateResource("FMLExamples/Test0.fml"));
	}

	/*@Test
	public void test2() {
		testFMLCompilationUnit(ResourceLocator.locateResource("FMLExamples/Test1.fml"));
	}*/

	private void testFMLCompilationUnit(Resource fileResource) {
		try {
			FMLParser.parse(((FileResourceImpl) fileResource).getFile());
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
