/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

package org.openflexo.drm.helpset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.java.help.search.Indexer;

public class JHIndexer {

	private static final Logger logger = Logger.getLogger(DRMHelpSet.class.getPackage().getName());

	private static final String JAVA_HELP_SEARCH = "JavaHelpSearch";

	private final File _helpsetDirectory;
	private final Vector<String> indexerFileList = new Vector<>();

	public JHIndexer(File helpsetDirectory) {
		_helpsetDirectory = helpsetDirectory;
	}

	// Use only one indexer in JVM, otherwise
	// produce anything useful but bunches of “ConfigFile and/or IndexBuilder not set”-exceptions
	private static Indexer ixr = new Indexer();

	/**
	 * create a search database for full text searching
	 */
	public void generate() {
		try {
			File dbDir = new File(_helpsetDirectory.getAbsolutePath() + File.separator + JAVA_HELP_SEARCH);
			if (dbDir.exists()) {
				dbDir.delete();
			}
			File confFile = writeConfigFile(_helpsetDirectory.getAbsolutePath());
			ixr.compile(getIndexerArguments(dbDir, confFile));
			confFile.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * create a configuration file for the Indexer call
	 */
	private File writeConfigFile(String prjDir) {
		String lineSeparator = System.getProperty("line.separator");
		File file = new File(prjDir + File.separator + "ixrConf.txt");
		if (file.exists()) {
			file.delete();
		}
		String[] files = getIndexerFiles(prjDir);
		try (OutputStream fw = new FileOutputStream(file)) {
			String attribute = "IndexRemove " + prjDir + File.separator + lineSeparator;
			attribute = attribute.replace('\\', '/');
			fw.write(attribute.getBytes());
			for (int i = 0; i < files.length; i++) {
				attribute = "File " + files[i] + lineSeparator;
				fw.write(attribute.getBytes());
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * create a list of files and directories that are targets for a full text search
	 */
	private String[] getIndexerFiles(String dir) {

		File[] files = new File(dir).listFiles();
		String fName = null;
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				fName = files[i].getAbsolutePath();
				if (files[i].isDirectory()) {
					getIndexerFiles(files[i].getAbsolutePath());
				}
				else if (fName.endsWith(".htm") || fName.endsWith(".html")) {
					if (!fName.endsWith("index.htm") && !fName.endsWith("toc.htm") && !fName.endsWith("images")
							&& !fName.endsWith("JavaHelpSearch")) {
						String added = fName.replace('\\', '/');
						// logger.info("Added: "+added);
						indexerFileList.addElement(added);
					}
				}
			}
		}
		return indexerFileList.toArray(new String[0]);
	}

	/**
	 * create a list of options for the Indexer call
	 */
	private static String[] getIndexerArguments(File dbDir, File cFile) {
		Vector<String> list = new Vector<>(0);
		String arg = null;
		list.addElement("-c");
		arg = cFile.getAbsolutePath();
		list.addElement(arg.replace('\\', '/'));
		list.addElement("-db");
		arg = dbDir.getAbsolutePath() + File.separator;
		list.addElement(arg.replace('\\', '/'));
		if (logger.isLoggable(Level.FINER)) {
			list.addElement("-verbose");
		}
		return list.toArray(new String[0]);
	}
}
