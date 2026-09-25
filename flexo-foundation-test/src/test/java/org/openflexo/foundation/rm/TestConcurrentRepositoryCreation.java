/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.rm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Two threads asking at the same time for a repository that does not exist yet must get the same one, the one registered in the resource
 * center (CORE-D-25).
 *
 * <p>
 * Both find no repository, both build one, only one can be registered. Before the fix the other one was returned all the same to its
 * caller, who then listened to an orphan repository: resources were registered in the other one, and the caller was never told. It happened
 * in the tests between the test thread and the Swing EDT, where <code>TechnologyAdapter.notifyRepositoryStructureChanged()</code> creates
 * the missing repositories.
 */
@RunWith(OrderedRunner.class)
public class TestConcurrentRepositoryCreation extends OpenflexoTestCase {

	private static final int ATTEMPTS = 50;

	@Test
	@TestOrder(1)
	public void testConcurrentCreationYieldsTheRegisteredRepository() throws Exception {
		instanciateTestServiceManager();
		FMLRTTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			for (int i = 0; i < ATTEMPTS; i++) {
				File directory = Files.createTempDirectory("TestConcurrentRepositoryCreation").toFile();
				directory.deleteOnExit();
				DirectoryResourceCenter rc = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(directory,
						serviceManager.getResourceCenterService());

				CyclicBarrier start = new CyclicBarrier(2);
				Callable<FMLRTVirtualModelInstanceRepository<File>> ask = () -> {
					start.await(10, TimeUnit.SECONDS);
					return ta.getVirtualModelInstanceRepository(rc);
				};
				Future<FMLRTVirtualModelInstanceRepository<File>> first = executor.submit(ask);
				Future<FMLRTVirtualModelInstanceRepository<File>> second = executor.submit(ask);

				FMLRTVirtualModelInstanceRepository<File> firstResult = first.get(30, TimeUnit.SECONDS);
				FMLRTVirtualModelInstanceRepository<File> secondResult = second.get(30, TimeUnit.SECONDS);
				FMLRTVirtualModelInstanceRepository<File> registered = rc.retrieveRepository(FMLRTVirtualModelInstanceRepository.class, ta);

				assertNotNull(registered);
				assertSame("Attempt " + i + ": a caller got a repository that is not the registered one", registered, firstResult);
				assertSame("Attempt " + i + ": a caller got a repository that is not the registered one", registered, secondResult);
			}
		} finally {
			executor.shutdownNow();
		}
	}
}
