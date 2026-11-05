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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.toolbox.FileUtils;

/**
 * The milestones reached by one execution of a {@link FlexoTask}, with the time elapsed since the start of the task when each was
 * reached, and the total duration of the execution.<br>
 *
 * The history of the previous execution of a task is what makes its progress bar linear: when a milestone is reached again, the bar
 * is set at the fraction of the total duration that this milestone represented the previous time, and between two milestones the
 * bar moves with time (see {@link FlexoTask#getProgressFraction()}).<br>
 *
 * A history is stored in the application data directory, in {@value #DIRECTORY}/&lt;key&gt;.properties.
 *
 * @author sylvain
 */
public class ProgressHistory {

	private static final Logger logger = Logger.getLogger(ProgressHistory.class.getPackage().getName());

	public static final String DIRECTORY = "ProgressHistory";

	private static final String TOTAL = "total";
	private static final String COUNT = "count";
	private static final String NAME = ".name";
	private static final String TIME = ".time";

	/**
	 * A milestone of an execution: its name (the step name given to {@link Progress#progress(String)}, possibly null) and the time
	 * elapsed since the start of the task, in milliseconds
	 */
	public static class Milestone {
		private final String name;
		private final long time;

		public Milestone(String name, long time) {
			this.name = name;
			this.time = time;
		}

		public String getName() {
			return name;
		}

		public long getTime() {
			return time;
		}

		@Override
		public String toString() {
			return name + "@" + time + "ms";
		}
	}

	private final List<Milestone> milestones;
	private final long totalDuration;

	public ProgressHistory(List<Milestone> milestones, long totalDuration) {
		this.milestones = Collections.unmodifiableList(new ArrayList<>(milestones));
		this.totalDuration = totalDuration;
	}

	public List<Milestone> getMilestones() {
		return milestones;
	}

	public long getTotalDuration() {
		return totalDuration;
	}

	/**
	 * Return the fraction of the total duration at which milestone of supplied index was reached (1.0 past the last milestone)
	 */
	public double getFraction(int index) {
		if (index < 0 || totalDuration <= 0) {
			return 0;
		}
		if (index >= milestones.size()) {
			return 1;
		}
		return Math.min(1.0, (double) milestones.get(index).getTime() / totalDuration);
	}

	/**
	 * Return the time at which milestone of supplied index was reached (the total duration past the last milestone, 0 before the first
	 * one)
	 */
	public long getTime(int index) {
		if (index < 0) {
			return 0;
		}
		if (index >= milestones.size()) {
			return totalDuration;
		}
		return milestones.get(index).getTime();
	}

	/**
	 * Return the index of the first milestone after <code>fromIndex</code> having supplied name, or -1 when there is none
	 */
	public int indexOf(String name, int fromIndex) {
		for (int i = Math.max(0, fromIndex); i < milestones.size(); i++) {
			String n = milestones.get(i).getName();
			if (name == null ? n == null : name.equals(n)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return "ProgressHistory " + totalDuration + "ms " + milestones;
	}

	/**
	 * Turn a history key into a file name
	 */
	private static String fileName(String key) {
		String name = key.replaceAll("[^A-Za-z0-9._-]", "_");
		if (name.length() > 150) {
			// A key may hold a path: keep the file name short, and distinct thanks to the hash of the whole key
			name = name.substring(0, 100) + "_" + Integer.toHexString(key.hashCode()) + "_" + name.substring(name.length() - 40);
		}
		return name + ".properties";
	}

	private static File getUserFile(String key) {
		return new File(new File(FileUtils.getApplicationDataDirectory(), DIRECTORY), fileName(key));
	}

	/**
	 * Load the history stored for supplied key
	 *
	 * @return the history, or null when there is none
	 */
	public static ProgressHistory load(String key) {
		File file = getUserFile(key);
		if (file.exists()) {
			try (InputStream in = new FileInputStream(file)) {
				return read(in);
			} catch (IOException | RuntimeException e) {
				logger.warning("Could not read progress history " + file + ": " + e);
			}
		}
		return null;
	}

	private static ProgressHistory read(InputStream in) throws IOException {
		Properties p = new Properties();
		p.load(in);
		int count = Integer.parseInt(p.getProperty(COUNT, "0"));
		List<Milestone> milestones = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			milestones.add(new Milestone(p.getProperty(i + NAME), Long.parseLong(p.getProperty(i + TIME, "0"))));
		}
		return new ProgressHistory(milestones, Long.parseLong(p.getProperty(TOTAL, "0")));
	}

	/**
	 * Store this history for supplied key, in the application data directory
	 */
	public void store(String key) {
		File file = getUserFile(key);
		Properties p = new Properties();
		p.setProperty(TOTAL, Long.toString(totalDuration));
		p.setProperty(COUNT, Integer.toString(milestones.size()));
		for (int i = 0; i < milestones.size(); i++) {
			if (milestones.get(i).getName() != null) {
				p.setProperty(i + NAME, milestones.get(i).getName());
			}
			p.setProperty(i + TIME, Long.toString(milestones.get(i).getTime()));
		}
		file.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(file)) {
			p.store(out, "Milestones of the last execution of " + key + " (times in ms since the start of the task)");
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Could not store progress history " + file + ": " + e);
			}
		}
	}

}
