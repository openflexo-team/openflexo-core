/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * {@link ResourceLoadingPlan}: what loading a FML unit requires is computed from the metadata, then each unit is loaded as a task of its
 * own, followed by the task manager.<br>
 * The fixture: `TestMetaData` imports `TestCrossReferences1` and `TestCrossReferences2`, which import each other.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestResourceLoadingPlan extends OpenflexoProjectAtRunTimeTestCase {

	private static final String RC = "http://openflexo.org/test/TestResourceCenter/";

	private static CompilationUnitResource metaData, crossReferences1, crossReferences2;
	private static ResourceLoadingPlan plan;

	private static CompilationUnitResource unit(String name) {
		CompilationUnitResource returned = (CompilationUnitResource) serviceManager.getResourceManager().getResource(RC + name + ".fml");
		assertNotNull(name, returned);
		return returned;
	}

	@Test
	@TestOrder(1)
	public void testComputePlan() {
		instanciateTestServiceManager();
		metaData = unit("TestMetaData");
		crossReferences1 = unit("TestCrossReferences1");
		crossReferences2 = unit("TestCrossReferences2");
		assertFalse(metaData.isLoaded());
		assertFalse(crossReferences1.isLoaded());
		assertFalse(crossReferences2.isLoaded());

		plan = ResourceLoadingPlan.compute(metaData);

		// Nothing is loaded by the computation
		assertFalse(metaData.isLoaded());
		assertFalse(crossReferences1.isLoaded());
		assertFalse(crossReferences2.isLoaded());

		// The units it imports import each other: they are not a step of their own (loading one of them alone would make it the root of
		// the loading of the cycle), they are loaded within the step of the unit
		List<FlexoResource<?>> resources = plan.getResources();
		assertEquals(resources.toString(), 1, resources.size());
		assertEquals(metaData, resources.get(0));
		for (TechnologyAdapter<?> ta : plan.getTechnologyAdapters()) {
			assertFalse("A technology already active is not in the plan: " + ta,
					ta.isActivated());
		}
	}

	@Test
	@TestOrder(2)
	public void testExecutePlan() throws Exception {
		FlexoTaskManager taskManager = serviceManager.getTaskManager();
		List<FlexoTask> followed = new ArrayList<>();
		PropertyChangeListener listener = evt -> {
			if (FlexoTaskManager.FOLLOWED_TASKS_PROPERTY.equals(evt.getPropertyName()) && evt.getNewValue() instanceof FlexoTask) {
				followed.add((FlexoTask) evt.getNewValue());
			}
		};
		taskManager.getPropertyChangeSupport().addPropertyChangeListener(listener);
		try {
			ResourceData<?> data = plan.execute();
			assertNotNull(data);
		} finally {
			taskManager.getPropertyChangeSupport().removePropertyChangeListener(listener);
		}

		assertTrue(metaData.isLoaded());
		assertTrue(crossReferences1.isLoaded());
		assertTrue(crossReferences2.isLoaded());

		// One task per technology to activate and per unit to load, all shown before the first one runs, all ended and no longer
		// followed
		assertEquals(followed.toString(), plan.getTechnologyAdapters().size() + plan.getResources().size(), followed.size());
		for (FlexoTask task : followed) {
			assertEquals(task.toString(), TaskStatus.FINISHED, task.getTaskStatus());
		}
		assertTrue(taskManager.getFollowedTasks().isEmpty());

		// A loaded resource needs nothing more
		ResourceLoadingPlan again = ResourceLoadingPlan.compute(metaData);
		assertTrue(again.getResources().isEmpty());
		assertTrue(again.getTechnologyAdapters().isEmpty());
	}

	/**
	 * `UnloadHolder` imports `UnloadProbe`, which imports `UnloadLedger`: no cycle, each unit is a step, dependencies first
	 */
	@Test
	@TestOrder(3)
	public void testPlanOfAChainOfImports() throws Exception {
		CompilationUnitResource holder = unit("UnloadHolder");
		CompilationUnitResource probe = unit("UnloadProbe");
		CompilationUnitResource ledger = unit("UnloadLedger");
		assertFalse(holder.isLoaded());
		assertFalse(probe.isLoaded());
		assertFalse(ledger.isLoaded());

		ResourceLoadingPlan chain = ResourceLoadingPlan.compute(holder);
		assertEquals(List.of(ledger, probe, holder), chain.getResources());

		chain.execute();
		assertTrue(holder.isLoaded());
		assertTrue(probe.isLoaded());
		assertTrue(ledger.isLoaded());
		assertFalse(holder.getLoadedResourceData().isModified());
		assertFalse(probe.getLoadedResourceData().isModified());
		assertFalse(ledger.getLoadedResourceData().isModified());
		assertTrue(serviceManager.getTaskManager().getFollowedTasks().isEmpty());
	}
}
