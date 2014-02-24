package org.openflexo.foundation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;

public class TestVirtualModelTechnologyAdapter {

	@Test
	public void test() {
		FlexoResourceCenterService rcService = DefaultResourceCenterService
				.getNewInstance();
		TechnologyAdapterService taService = DefaultTechnologyAdapterService
				.getNewInstance(rcService);
		((DefaultTechnologyAdapterService) taService)
				.loadAvailableTechnologyAdapters();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			System.out.println("> " + ta);
		}
		assertTrue(taService.getTechnologyAdapters().size() > 0);
		assertNotNull(taService
				.getTechnologyAdapter(VirtualModelTechnologyAdapter.class));

	}
}
