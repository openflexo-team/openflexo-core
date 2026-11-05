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

package org.openflexo.foundation.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;
import org.openflexo.foundation.task.ProgressHistory.Milestone;
import org.openflexo.toolbox.FileUtils;

/**
 * Checks that a {@link FlexoTask} having a progress history key replays the milestones of its previous execution: its progress never
 * goes back, reaches at a milestone the fraction it had the previous time, and the execution is stored for the next one.
 */
public class TestProgressHistory {

	private final String key = "TestProgressHistory-" + System.nanoTime();

	private File historyFile() {
		return new File(new File(FileUtils.getApplicationDataDirectory(), ProgressHistory.DIRECTORY), key + ".properties");
	}

	@After
	public void deleteHistory() {
		historyFile().delete();
	}

	/** A task reaching milestones a, b, c, recording its progress just after each */
	private class MilestoneTask extends FlexoTask {

		private final long pause;
		private final List<Double> fractions = new ArrayList<>();

		MilestoneTask(long pause) {
			super("MilestoneTask", "Milestones");
			this.pause = pause;
		}

		@Override
		public String getProgressHistoryKey() {
			return key;
		}

		@Override
		public long getDefaultExpectedDuration() {
			return 1000;
		}

		@Override
		public void performTask() throws InterruptedException {
			for (String name : Arrays.asList("a", "b", "c")) {
				Thread.sleep(pause);
				Progress.progress(name);
				fractions.add(getProgressFraction());
			}
			Thread.sleep(pause);
		}
	}

	private MilestoneTask run(long pause) {
		FlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance(1);
		MilestoneTask task = new MilestoneTask(pause);
		taskManager.scheduleExecution(task);
		taskManager.waitTask(task);
		taskManager.shutdownAndWait();
		assertEquals(TaskStatus.FINISHED, task.getTaskStatus());
		return task;
	}

	private static void assertNeverDecreases(List<Double> fractions) {
		for (int i = 1; i < fractions.size(); i++) {
			assertTrue("progress went back: " + fractions, fractions.get(i) >= fractions.get(i - 1));
		}
	}

	@Test
	public void testStoreAndLoad() {
		assertNull(ProgressHistory.load(key));
		new ProgressHistory(Arrays.asList(new Milestone("a", 100), new Milestone(null, 250), new Milestone("é = x", 300)), 400).store(key);
		ProgressHistory history = ProgressHistory.load(key);
		assertNotNull(history);
		assertEquals(400, history.getTotalDuration());
		assertEquals(3, history.getMilestones().size());
		assertNull(history.getMilestones().get(1).getName());
		assertEquals("é = x", history.getMilestones().get(2).getName());
		assertEquals(0.75, history.getFraction(2), 1e-9);
		assertEquals(1, history.indexOf(null, 0));
		assertEquals(-1, history.indexOf("a", 1));
	}

	@Test
	public void testReplayPreviousExecution() {
		// First execution: no history, the progress tends towards 1 with time, without reaching it
		MilestoneTask first = run(100);
		assertNeverDecreases(first.fractions);
		assertTrue(first.fractions.get(2) < 0.99 + 1e-9);
		assertEquals(1.0, first.getProgressFraction(), 1e-9);

		ProgressHistory history = ProgressHistory.load(key);
		assertNotNull("the execution was not stored", history);
		assertEquals(3, history.getMilestones().size());
		assertEquals("b", history.getMilestones().get(1).getName());

		// Second execution, twice as slow: at each milestone, the progress is the fraction measured the first time
		MilestoneTask second = run(200);
		assertNeverDecreases(second.fractions);
		for (int i = 0; i < 3; i++) {
			assertEquals("milestone " + i + " " + second.fractions, history.getFraction(i), second.fractions.get(i), 0.05);
		}
	}

	@Test
	public void testCountedStepsWithoutKey() {
		FlexoTask task = new FlexoTask("Counted", "Counted") {
			@Override
			public void performTask() {
			}
		};
		assertTrue(!task.hasTimedProgress());
		task.setExpectedProgressSteps(4);
		task.progress();
		assertEquals(0.25, task.getProgressFraction(), 1e-9);
		// Announcing the steps again resets the counter: the progress does not go back
		task.setExpectedProgressSteps(10);
		assertEquals(0.25, task.getProgressFraction(), 1e-9);
	}
}
