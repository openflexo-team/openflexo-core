/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Implements a timer task which recursively watch a directory looking for changes on file system
 * 
 * @author sylvain
 * 
 */
public abstract class DirectoryWatcher extends TimerTask {

	protected static final Logger logger = Logger.getLogger(DirectoryWatcher.class.getPackage().getName());

	private final NodeDirectoryWatcher rootDirectoryWatcher;

	private static class NodeDirectoryWatcher {

		private final DirectoryWatcher watcher;
		private final File directory;
		private final Map<File, Long> lastModified = new HashMap<File, Long>();
		private final Map<File, NodeDirectoryWatcher> subNodes = new HashMap<File, NodeDirectoryWatcher>();

		private NodeDirectoryWatcher(File directory, DirectoryWatcher watcher, boolean notifyAdding) {
			// System.out.println("Init NodeDirectoryWatcher on " + directory);
			this.directory = directory;
			this.watcher = watcher;
			for (File f : directory.listFiles()) {
				lastModified.put(f, f.lastModified());
				if (f.isDirectory()) {
					subNodes.put(f, new NodeDirectoryWatcher(f, watcher, notifyAdding));
				}
				if (notifyAdding) {
					watcher.fileAdded(f);
				}
			}
		}

		private void watch() {
			Set<File> checkedFiles = new HashSet<File>();

			// scan the files and check for modification/addition
			for (File f : directory.listFiles()) {
				Long current = lastModified.get(f);
				checkedFiles.add(f);
				if (current == null) {
					// new file
					lastModified.put(f, f.lastModified());
					watcher.fileAdded(f);
					if (f.isDirectory()) {
						subNodes.put(f, new NodeDirectoryWatcher(f, watcher, true));
					}
				} else if (current.longValue() != f.lastModified()) {
					// modified file
					lastModified.put(f, f.lastModified());
					watcher.fileModified(f);
				}
			}

			// now check for deleted files
			Set<File> ref = new HashMap<File, Long>(lastModified).keySet();
			ref.removeAll(checkedFiles);
			Iterator<File> it = ref.iterator();
			while (it.hasNext()) {
				File deletedFile = it.next();
				lastModified.remove(deletedFile);
				if (subNodes.get(deletedFile) != null) {
					subNodes.get(deletedFile).delete();
				}
				subNodes.remove(deletedFile);
				watcher.fileDeleted(deletedFile);
			}

			for (NodeDirectoryWatcher w : subNodes.values()) {
				w.watch();
			}
		}

		private void delete() {
			for (File f : lastModified.keySet()) {
				watcher.fileDeleted(f);
			}
			for (NodeDirectoryWatcher w : subNodes.values()) {
				w.delete();
			}
		}
	}

	public DirectoryWatcher(File directory) {
		super();
		rootDirectoryWatcher = new NodeDirectoryWatcher(directory, this, false);
		logger.info("Started DirectoryWatcher on " + directory + " ...");
	}

	@Override
	public final void run() {
		rootDirectoryWatcher.watch();
	}

	protected abstract void fileModified(File file);

	protected abstract void fileAdded(File file);

	protected abstract void fileDeleted(File file);

	public static void main(String[] args) {
		TimerTask task = new DirectoryWatcher(new File("/Users/sylvain/Temp")) {
			@Override
			protected void fileModified(File file) {
				System.out.println("File MODIFIED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}

			@Override
			protected void fileAdded(File file) {
				System.out.println("File ADDED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}

			@Override
			protected void fileDeleted(File file) {
				System.out.println("File DELETED " + file.getName() + " in " + file.getParentFile().getAbsolutePath());
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, new Date(), 1000);

	}
}
