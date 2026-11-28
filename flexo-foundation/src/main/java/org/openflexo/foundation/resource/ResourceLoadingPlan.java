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

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.OpenflexoRuntimeException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * What loading a resource requires, computed before anything is loaded: the technology adapters to activate, then the FML compilation
 * units to load (those it depends on first, the resource itself last).<br>
 *
 * The plan is computed from what the resources already know of their dependencies (the metadata of a unit: its imports and its model
 * slots), without parsing anything. {@link #execute()} then runs each step as a task of its own, in the current thread, shown by the
 * task manager (see {@link FlexoTaskManager#followTask(FlexoTask)}): one per technology adapter to activate, one per unit to load. A
 * dependency that only becomes known once the technologies are active (a resource of a technology that was not active) is found by a
 * second computation, made after the activations.<br>
 *
 * Only the compilation units get a step of their own: any other resource (a model of a technology, a VirtualModel instance) is loaded
 * within the step of the unit that needs it, as before.
 *
 * @author sylvain
 */
public class ResourceLoadingPlan {

	private static final Logger logger = Logger.getLogger(ResourceLoadingPlan.class.getPackage().getName());

	private final FlexoResource<?> resource;
	private final List<TechnologyAdapter<?>> technologyAdapters;
	private final List<FlexoResource<?>> resources;

	private ResourceLoadingPlan(FlexoResource<?> resource, List<TechnologyAdapter<?>> technologyAdapters, List<FlexoResource<?>> resources) {
		this.resource = resource;
		this.technologyAdapters = technologyAdapters;
		this.resources = resources;
	}

	/**
	 * Compute what loading supplied resource requires, without loading or activating anything
	 *
	 * @param resource
	 *            the resource to load
	 * @return the plan, empty when the resource is already loaded
	 */
	public static ResourceLoadingPlan compute(FlexoResource<?> resource) {
		Set<TechnologyAdapter<?>> technologyAdapters = new LinkedHashSet<>();
		List<FlexoResource<?>> resources = new ArrayList<>();
		if (!resource.isLoaded()) {
			TechnologyAdapterService taService = resource.getServiceManager() != null
					? resource.getServiceManager().getTechnologyAdapterService()
					: null;
			new Walk(resource, taService, technologyAdapters, resources).visit(resource);
		}
		List<TechnologyAdapter<?>> toActivate = new ArrayList<>();
		for (TechnologyAdapter<?> ta : technologyAdapters) {
			if (ta != null && !ta.isActivated()) {
				toActivate.add(ta);
			}
		}
		return new ResourceLoadingPlan(resource, toActivate, resources);
	}

	/**
	 * Depth-first walk of the unloaded units the resource depends on, grouping the units that import each other (strongly connected
	 * components, Tarjan): a group is complete once all the units it depends on are, so groups come out dependencies first.<br>
	 *
	 * A unit that is in no import cycle is a step of its own. The units of an import cycle are not: loading one of them alone would make
	 * it the root of the two-pass loading of the whole cycle, and a cycle loaded from one of its own units may leave references
	 * unresolved (CORE-D-14, seen on `CrossImports`). They are loaded, as before, within the step of the first unit outside the cycle that
	 * needs them, the resource itself at last.
	 */
	private static class Walk {
		private final FlexoResource<?> root;
		private final TechnologyAdapterService taService;
		private final Set<TechnologyAdapter<?>> technologyAdapters;
		private final List<FlexoResource<?>> resources;
		private final Map<FlexoResource<?>, Integer> index = new HashMap<>();
		private final Map<FlexoResource<?>, Integer> lowLink = new HashMap<>();
		private final Deque<FlexoResource<?>> stack = new ArrayDeque<>();
		private final Set<FlexoResource<?>> onStack = new HashSet<>();

		Walk(FlexoResource<?> root, TechnologyAdapterService taService, Set<TechnologyAdapter<?>> technologyAdapters,
				List<FlexoResource<?>> resources) {
			this.root = root;
			this.taService = taService;
			this.technologyAdapters = technologyAdapters;
			this.resources = resources;
		}

		private boolean isWalked(FlexoResource<?> r) {
			return r == root || (!r.isLoaded() && r instanceof CompilationUnitResource);
		}

		void visit(FlexoResource<?> r) {
			index.put(r, index.size());
			lowLink.put(r, index.get(r));
			stack.push(r);
			onStack.add(r);
			if (r instanceof CompilationUnitResource && taService != null) {
				// What CompilationUnitResource.activateRequiredTechnologies() activates when the unit loads
				technologyAdapters.add(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));
				for (Class<? extends ModelSlot<?, ?>> modelSlotClass : ((CompilationUnitResource) r).getUsedModelSlots()) {
					technologyAdapters.add(taService.getTechnologyAdapterForModelSlot(modelSlotClass));
				}
			}
			boolean importsItself = false;
			for (FlexoResource<?> dependency : new ArrayList<>(r.getDependencies())) {
				if (dependency == r) {
					importsItself = true;
				}
				else if (isWalked(dependency)) {
					if (!index.containsKey(dependency)) {
						visit(dependency);
						lowLink.put(r, Math.min(lowLink.get(r), lowLink.get(dependency)));
					}
					else if (onStack.contains(dependency)) {
						lowLink.put(r, Math.min(lowLink.get(r), index.get(dependency)));
					}
				}
			}
			if (lowLink.get(r).equals(index.get(r))) {
				// r closes a group: the units above it on the stack
				List<FlexoResource<?>> group = new ArrayList<>();
				FlexoResource<?> member;
				do {
					member = stack.pop();
					onStack.remove(member);
					group.add(member);
				} while (member != r);
				if (group.contains(root)) {
					resources.add(root);
				}
				else if (group.size() == 1 && !importsItself) {
					resources.add(r);
				}
				// else: an import cycle without the resource, loaded within a later step
			}
		}
	}

	/**
	 * Return the resource this plan loads
	 */
	public FlexoResource<?> getResource() {
		return resource;
	}

	/**
	 * Return the technology adapters to activate, not active when the plan was computed
	 */
	public List<TechnologyAdapter<?>> getTechnologyAdapters() {
		return Collections.unmodifiableList(technologyAdapters);
	}

	/**
	 * Return the resources to load, in the order they are loaded: the compilation units the resource depends on, then the resource
	 */
	public List<FlexoResource<?>> getResources() {
		return Collections.unmodifiableList(resources);
	}

	/**
	 * Activate the technology adapters and load the resources of this plan, each as a task run in the current thread and followed by
	 * the task manager of the resource (shown in the task panel of the application). A step that fails stops the loading: the steps
	 * left are cancelled and the failure is thrown.
	 *
	 * @return the data of the resource
	 */
	public ResourceData<?> execute() throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
		if (resource.isLoaded()) {
			return resource.getLoadedResourceData();
		}
		FlexoServiceManager serviceManager = resource.getServiceManager();
		FlexoTaskManager taskManager = serviceManager != null ? serviceManager.getTaskManager() : null;

		List<FlexoTask> pending = new ArrayList<>();
		List<ActivationStep> activations = new ArrayList<>();
		for (TechnologyAdapter<?> ta : technologyAdapters) {
			activations.add(new ActivationStep(ta, title(serviceManager, "activating_technology_($0)", ta.getName())));
		}
		List<LoadingStep> loadings = new ArrayList<>();
		for (FlexoResource<?> r : resources) {
			loadings.add(new LoadingStep(r, title(serviceManager, "loading_resource_($0)", r.getName())));
		}
		pending.addAll(activations);
		pending.addAll(loadings);
		follow(taskManager, pending);

		// A technology is activated once at most, even when it stays inactive (otherwise the plan would ask for it again forever)
		Set<TechnologyAdapter<?>> attempted = new HashSet<>(technologyAdapters);
		try {
			while (!activations.isEmpty()) {
				for (ActivationStep step : activations) {
					run(step, pending, taskManager);
				}
				// The resources of the technologies just activated are now registered: a dependency that was pending may be resolved,
				// and it may need other technologies
				ResourceLoadingPlan again = compute(resource);
				activations = new ArrayList<>();
				for (TechnologyAdapter<?> ta : again.technologyAdapters) {
					if (!attempted.add(ta)) {
						continue;
					}
					activations.add(new ActivationStep(ta, title(serviceManager, "activating_technology_($0)", ta.getName())));
				}
				List<LoadingStep> added = new ArrayList<>();
				for (FlexoResource<?> r : again.resources) {
					if (loadings.stream().noneMatch(step -> step.resource == r)) {
						added.add(new LoadingStep(r, title(serviceManager, "loading_resource_($0)", r.getName())));
					}
				}
				follow(taskManager, activations);
				follow(taskManager, added);
				pending.addAll(0, activations);
				pending.addAll(added);
				loadings.addAll(loadings.size() - 1, added);
			}
			for (LoadingStep step : loadings) {
				run(step, pending, taskManager);
			}
		} finally {
			// What was not run because a step failed, or because the loading was interrupted
			for (FlexoTask step : pending) {
				step.abandon();
				if (taskManager != null) {
					taskManager.unfollowTask(step);
				}
			}
		}
		return resource.getLoadedResourceData();
	}

	private static void follow(FlexoTaskManager taskManager, List<? extends FlexoTask> tasks) {
		if (taskManager != null) {
			for (FlexoTask task : tasks) {
				taskManager.followTask(task);
			}
		}
	}

	private static void run(PlanStep step, List<FlexoTask> pending, FlexoTaskManager taskManager)
			throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
		// The work running the plan (a task that may be cancelled) is interrupted: the steps left are not run
		if (Thread.currentThread().isInterrupted()) {
			throw new ResourceLoadingCancelledException();
		}
		pending.remove(step);
		// A milestone of the work running the plan, which shows its progress from one step to the next
		Progress.progress(step.getTaskTitle());
		boolean failed = true;
		boolean done = false;
		step.beginInCurrentThread();
		try {
			done = step.perform();
			failed = false;
		} finally {
			if (!failed && !done) {
				// Nothing was left to do (a unit loaded by an earlier step): this is not the duration of the step
				step.discardProgressHistory();
			}
			step.endInCurrentThread(failed);
			if (taskManager != null) {
				taskManager.unfollowTask(step);
			}
		}
	}

	private static String title(FlexoServiceManager serviceManager, String key, String name) {
		if (serviceManager != null && serviceManager.getLocalizationService() != null
				&& serviceManager.getLocalizationService().getFlexoLocalizer() != null) {
			return serviceManager.getLocalizationService().getFlexoLocalizer().localizedForKeyWithParams(key, name);
		}
		return name;
	}

	private interface Step {
		/**
		 * Do the work of the step
		 * 
		 * @return false when there was nothing left to do
		 */
		boolean perform() throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException;
	}

	/**
	 * A step of the plan is a task run by {@link ResourceLoadingPlan#execute()}, never by a task manager
	 */
	private static abstract class PlanStep extends FlexoTask implements Step {
		PlanStep(String taskId, String title) {
			super(taskId, title);
		}

		@Override
		public void performTask() {
			throw new UnsupportedOperationException("A step of a resource loading plan is run by the plan, not by a task manager");
		}
	}

	private static class ActivationStep extends PlanStep {
		private final TechnologyAdapter<?> technologyAdapter;

		ActivationStep(TechnologyAdapter<?> technologyAdapter, String title) {
			super("ActivateTechnologyAdapter", title);
			this.technologyAdapter = technologyAdapter;
		}

		@Override
		public boolean perform() {
			if (technologyAdapter.isActivated()) {
				return false;
			}
			logger.info("Activating " + technologyAdapter.getName() + " before loading");
			activate(technologyAdapter);
			return true;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static void activate(TechnologyAdapter technologyAdapter) {
			technologyAdapter.getTechnologyAdapterService().activateTechnologyAdapter(technologyAdapter, true);
		}

		@Override
		public String getProgressHistoryKey() {
			return "ActivateTechnologyAdapter." + technologyAdapter.getIdentifier();
		}
	}

	private static class LoadingStep extends PlanStep {
		private final FlexoResource<?> resource;

		LoadingStep(FlexoResource<?> resource, String title) {
			super("LoadResource", title);
			this.resource = resource;
		}

		@Override
		public boolean perform() throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
			// Already loaded by an earlier step (it needed this unit)
			if (resource.isLoaded()) {
				return false;
			}
			resource.getResourceData();
			return true;
		}

		@Override
		public String getProgressHistoryKey() {
			return "LoadResource." + resource.getURI();
		}
	}
}
