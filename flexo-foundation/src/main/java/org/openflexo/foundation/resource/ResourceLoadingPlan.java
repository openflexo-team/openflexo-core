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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
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
 * slots), without parsing anything. {@link #execute()} then runs each step as a task of its own, shown by the task manager (see
 * {@link FlexoTaskManager#followTask(FlexoTask)}): one per technology adapter to activate, one per unit to load. The units are loaded
 * in the current thread; the technologies are activated in the background meanwhile, and a unit waits only for the technologies it
 * needs. A dependency that only becomes known once the technologies are active (a resource of a technology that was not active) is
 * found by a second computation, made after the activations.<br>
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
	// For each resource to load: the technologies needed by what its step loads (the unit and the units it imports)
	private final Map<FlexoResource<?>, Set<TechnologyAdapter<?>>> needs;

	private ResourceLoadingPlan(FlexoResource<?> resource, List<TechnologyAdapter<?>> technologyAdapters, List<FlexoResource<?>> resources,
			Map<FlexoResource<?>, Set<TechnologyAdapter<?>>> needs) {
		this.resource = resource;
		this.technologyAdapters = technologyAdapters;
		this.resources = resources;
		this.needs = needs;
	}

	/**
	 * Compute what loading supplied resource requires, without loading or activating anything
	 *
	 * @param resource
	 *            the resource to load
	 * @return the plan, empty when the resource is already loaded
	 */
	public static ResourceLoadingPlan compute(FlexoResource<?> resource) {
		List<FlexoResource<?>> resources = new ArrayList<>();
		Map<FlexoResource<?>, Set<TechnologyAdapter<?>>> needs = new HashMap<>();
		// The technologies to activate, in the order the steps need them
		Set<TechnologyAdapter<?>> technologyAdapters = new LinkedHashSet<>();
		if (!resource.isLoaded()) {
			TechnologyAdapterService taService = resource.getServiceManager() != null
					? resource.getServiceManager().getTechnologyAdapterService()
					: null;
			Walk walk = new Walk(resource, taService, resources);
			walk.visit(resource);
			List<FlexoResource<?>> waitingForAll = new ArrayList<>();
			for (FlexoResource<?> step : resources) {
				Set<TechnologyAdapter<?>> stepNeeds = new LinkedHashSet<>();
				for (TechnologyAdapter<?> ta : walk.closure(step)) {
					if (ta != null && !ta.isActivated()) {
						stepNeeds.add(ta);
					}
				}
				needs.put(step, stepNeeds);
				technologyAdapters.addAll(stepNeeds);
				if (walk.closureHasPendingDependencies(step)) {
					waitingForAll.add(step);
				}
			}
			// A unit importing a resource not registered yet (a resource of a technology not active yet, whatever its `use` lines say)
			// may need any technology of the plan: its step waits for all of them, so that it never resolves its imports while one of
			// them is being activated
			for (FlexoResource<?> step : waitingForAll) {
				needs.put(step, new LinkedHashSet<>(technologyAdapters));
			}
		}
		return new ResourceLoadingPlan(resource, new ArrayList<>(technologyAdapters), resources, needs);
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
		private final List<FlexoResource<?>> resources;
		private final Map<FlexoResource<?>, Integer> index = new HashMap<>();
		private final Map<FlexoResource<?>, Integer> lowLink = new HashMap<>();
		private final Deque<FlexoResource<?>> stack = new ArrayDeque<>();
		private final Set<FlexoResource<?>> onStack = new HashSet<>();
		// What each walked unit needs by itself, and the walked units it imports
		private final Map<FlexoResource<?>, Set<TechnologyAdapter<?>>> ownNeeds = new HashMap<>();
		private final Map<FlexoResource<?>, List<FlexoResource<?>>> walkedDependencies = new HashMap<>();

		Walk(FlexoResource<?> root, TechnologyAdapterService taService, List<FlexoResource<?>> resources) {
			this.root = root;
			this.taService = taService;
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
			Set<TechnologyAdapter<?>> own = new LinkedHashSet<>();
			if (r instanceof CompilationUnitResource && taService != null) {
				// What CompilationUnitResource.activateRequiredTechnologies() activates when the unit loads
				own.add(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));
				for (Class<? extends ModelSlot<?, ?>> modelSlotClass : ((CompilationUnitResource) r).getUsedModelSlots()) {
					own.add(taService.getTechnologyAdapterForModelSlot(modelSlotClass));
				}
			}
			ownNeeds.put(r, own);
			List<FlexoResource<?>> walked = new ArrayList<>();
			walkedDependencies.put(r, walked);
			boolean importsItself = false;
			for (FlexoResource<?> dependency : new ArrayList<>(r.getDependencies())) {
				if (dependency == r) {
					importsItself = true;
				}
				else if (isWalked(dependency)) {
					walked.add(dependency);
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

		/**
		 * Tell whether a unit, or a walked unit it imports, directly or not, waits for a resource not registered yet
		 */
		boolean closureHasPendingDependencies(FlexoResource<?> unit) {
			if (unit.getServiceManager() == null || unit.getServiceManager().getResourceManager() == null) {
				return false;
			}
			ResourceManager resourceManager = unit.getServiceManager().getResourceManager();
			Set<FlexoResource<?>> seen = new HashSet<>();
			Deque<FlexoResource<?>> toSee = new ArrayDeque<>();
			toSee.push(unit);
			while (!toSee.isEmpty()) {
				FlexoResource<?> r = toSee.pop();
				if (seen.add(r)) {
					if (resourceManager.hasPendingDependencies(r)) {
						return true;
					}
					for (FlexoResource<?> d : walkedDependencies.getOrDefault(r, Collections.emptyList())) {
						toSee.push(d);
					}
				}
			}
			return false;
		}

		/**
		 * The technologies needed by a unit and by every walked unit it imports, directly or not: what its step may load
		 */
		Set<TechnologyAdapter<?>> closure(FlexoResource<?> unit) {
			Set<TechnologyAdapter<?>> returned = new LinkedHashSet<>();
			Set<FlexoResource<?>> seen = new HashSet<>();
			Deque<FlexoResource<?>> toSee = new ArrayDeque<>();
			toSee.push(unit);
			while (!toSee.isEmpty()) {
				FlexoResource<?> r = toSee.pop();
				if (seen.add(r)) {
					returned.addAll(ownNeeds.getOrDefault(r, Collections.emptySet()));
					for (FlexoResource<?> d : walkedDependencies.getOrDefault(r, Collections.emptyList())) {
						toSee.push(d);
					}
				}
			}
			return returned;
		}
	}

	/**
	 * Return the resource this plan loads
	 */
	public FlexoResource<?> getResource() {
		return resource;
	}

	/**
	 * Return the technology adapters to activate, not active when the plan was computed, in the order the steps need them
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
	 * Return the technologies, among {@link #getTechnologyAdapters()}, that the step loading supplied resource needs
	 */
	public Set<TechnologyAdapter<?>> getTechnologyAdaptersNeededBy(FlexoResource<?> resourceToLoad) {
		return Collections.unmodifiableSet(needs.getOrDefault(resourceToLoad, Collections.emptySet()));
	}

	/**
	 * Activate the technology adapters and load the resources of this plan, each as a task followed by the task manager of the resource
	 * (shown in the task panel of the application).<br>
	 * The units are loaded in the current thread, one after the other. The technologies are activated meanwhile by a task of the task
	 * manager, in the order the units need them, and a unit waits only for those it needs. Each activation is run once, by whoever needs
	 * it first: a unit does not wait for an activation the background task has not started (it runs it itself), so that a loading never
	 * waits for a task queued behind it. Without a task manager, everything runs in the current thread.<br>
	 * A loading step that fails stops the loading: the steps left are cancelled and the failure is thrown. A background activation that
	 * fails is logged; the unit that needs it tries again when it loads.
	 *
	 * @return the data of the resource
	 */
	public ResourceData<?> execute() throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
		if (resource.isLoaded()) {
			return resource.getLoadedResourceData();
		}
		FlexoServiceManager serviceManager = resource.getServiceManager();
		FlexoTaskManager taskManager = serviceManager != null ? serviceManager.getTaskManager() : null;

		List<FlexoTask> pending = Collections.synchronizedList(new ArrayList<>());
		Map<TechnologyAdapter<?>, ActivationStep> activations = new LinkedHashMap<>();
		for (TechnologyAdapter<?> ta : technologyAdapters) {
			activations.put(ta, new ActivationStep(ta, title(serviceManager, "activating_technology_($0)", ta.getName())));
		}
		List<LoadingStep> loadings = new ArrayList<>();
		for (FlexoResource<?> r : resources) {
			loadings.add(new LoadingStep(r, title(serviceManager, "loading_resource_($0)", r.getName())));
		}
		follow(taskManager, new ArrayList<>(activations.values()));
		pending.addAll(loadings);
		follow(taskManager, loadings);

		AtomicBoolean stopped = new AtomicBoolean(false);
		if (taskManager != null && !activations.isEmpty()) {
			taskManager.scheduleExecution(new BackgroundActivations(new ArrayList<>(activations.values()), stopped, taskManager));
		}
		try {
			// The units the resource depends on, each once the technologies it needs are active
			LoadingStep root = loadings.get(loadings.size() - 1);
			for (LoadingStep step : new ArrayList<>(loadings.subList(0, loadings.size() - 1))) {
				activate(needs.get(step.resource), activations, pending, taskManager);
				run(step, pending, taskManager);
			}
			// All of them before the resource itself, then what they brought (a dependency that was pending, maybe needing other
			// technologies), activated here
			activate(activations.keySet(), activations, pending, taskManager);
			List<LoadingStep> added = new ArrayList<>();
			ResourceLoadingPlan again = compute(resource);
			while (true) {
				List<ActivationStep> more = new ArrayList<>();
				for (TechnologyAdapter<?> ta : again.technologyAdapters) {
					if (!activations.containsKey(ta)) {
						ActivationStep step = new ActivationStep(ta, title(serviceManager, "activating_technology_($0)", ta.getName()));
						activations.put(ta, step);
						more.add(step);
					}
				}
				for (FlexoResource<?> r : again.resources) {
					if (r != resource && loadings.stream().noneMatch(step -> step.resource == r)
							&& added.stream().noneMatch(step -> step.resource == r)) {
						LoadingStep step = new LoadingStep(r, title(serviceManager, "loading_resource_($0)", r.getName()));
						added.add(step);
						pending.add(step);
						follow(taskManager, Collections.singletonList(step));
					}
				}
				if (more.isEmpty()) {
					break;
				}
				follow(taskManager, more);
				for (ActivationStep step : more) {
					activate(Collections.singleton(step.technologyAdapter), activations, pending, taskManager);
				}
				again = compute(resource);
			}
			for (LoadingStep step : added) {
				run(step, pending, taskManager);
			}
			run(root, pending, taskManager);
		} finally {
			// What was not run because a step failed, or because the loading was interrupted
			stopped.set(true);
			for (ActivationStep step : activations.values()) {
				if (step.claim()) {
					step.done.countDown();
					step.abandon();
					if (taskManager != null) {
						taskManager.unfollowTask(step);
					}
				}
			}
			for (FlexoTask step : new ArrayList<>(pending)) {
				step.abandon();
				if (taskManager != null) {
					taskManager.unfollowTask(step);
				}
			}
		}
		return resource.getLoadedResourceData();
	}

	/**
	 * Make sure supplied technologies are active: each one is activated here when nobody started it yet, or awaited when the background
	 * task is activating it
	 */
	private static void activate(Set<TechnologyAdapter<?>> technologies, Map<TechnologyAdapter<?>, ActivationStep> activations,
			List<FlexoTask> pending, FlexoTaskManager taskManager)
			throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
		if (technologies == null) {
			return;
		}
		for (Map.Entry<TechnologyAdapter<?>, ActivationStep> entry : activations.entrySet()) {
			if (!technologies.contains(entry.getKey())) {
				continue;
			}
			ActivationStep step = entry.getValue();
			if (step.claim()) {
				step.runClaimed(pending, taskManager);
			}
			else {
				step.awaitDone();
			}
		}
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
			// This step will not run: it is given up here, not left in the task panel (an activation claimed by this thread is in no
			// list the caller cleans)
			pending.remove(step);
			step.abandon();
			if (taskManager != null) {
				taskManager.unfollowTask(step);
			}
			throw new ResourceLoadingCancelledException();
		}
		pending.remove(step);
		// A milestone of the work running the step, which shows its progress from one step to the next
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

	/**
	 * The activation of a technology, run once, by the first one that claims it: the background task, or the loading of a unit that
	 * needs it
	 */
	private static class ActivationStep extends PlanStep {
		private final TechnologyAdapter<?> technologyAdapter;
		private final AtomicBoolean claimed = new AtomicBoolean(false);
		private final CountDownLatch done = new CountDownLatch(1);

		ActivationStep(TechnologyAdapter<?> technologyAdapter, String title) {
			super("ActivateTechnologyAdapter", title);
			this.technologyAdapter = technologyAdapter;
		}

		/**
		 * Return true for the one that will run this activation
		 */
		boolean claim() {
			return claimed.compareAndSet(false, true);
		}

		/**
		 * Run this activation, claimed by the current thread
		 */
		void runClaimed(List<FlexoTask> pending, FlexoTaskManager taskManager)
				throws ResourceLoadingCancelledException, FileNotFoundException, OpenflexoRuntimeException {
			try {
				ResourceLoadingPlan.run(this, pending, taskManager);
			} finally {
				done.countDown();
			}
		}

		/**
		 * Wait for the end of this activation, claimed by another thread: an interruption of this thread stops the loading
		 */
		void awaitDone() throws ResourceLoadingCancelledException {
			try {
				done.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ResourceLoadingCancelledException();
			}
		}

		@Override
		public boolean perform() {
			if (technologyAdapter.isActivated()) {
				return false;
			}
			logger.info("Activating " + technologyAdapter.getName() + " before loading");
			activateNow(technologyAdapter);
			return true;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static void activateNow(TechnologyAdapter technologyAdapter) {
			technologyAdapter.getTechnologyAdapterService().activateTechnologyAdapter(technologyAdapter, true);
		}

		@Override
		public String getProgressHistoryKey() {
			return "ActivateTechnologyAdapter." + technologyAdapter.getIdentifier();
		}
	}

	/**
	 * The task of the task manager that runs, in the background and in order, the activations nobody claimed yet. It is not shown: the
	 * activations it runs are
	 */
	private static class BackgroundActivations extends FlexoTask {
		private final List<ActivationStep> activations;
		private final AtomicBoolean stopped;
		private final FlexoTaskManager taskManager;

		BackgroundActivations(List<ActivationStep> activations, AtomicBoolean stopped, FlexoTaskManager taskManager) {
			super("ActivateTechnologyAdapters", "ActivateTechnologyAdapters");
			this.activations = activations;
			this.stopped = stopped;
			this.taskManager = taskManager;
		}

		@Override
		public boolean isShownInTaskPanel() {
			return false;
		}

		@Override
		public void performTask() {
			for (ActivationStep step : activations) {
				if (stopped.get()) {
					return;
				}
				if (step.claim()) {
					try {
						step.runClaimed(new ArrayList<>(), taskManager);
					} catch (Exception e) {
						// The unit that needs it tries again when it loads, and reports the failure
						logger.warning("Could not activate " + step.technologyAdapter.getName() + " in the background: " + e);
					}
				}
			}
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
