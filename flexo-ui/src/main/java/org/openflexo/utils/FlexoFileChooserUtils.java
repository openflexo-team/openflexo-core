/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.utils;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;

public class FlexoFileChooserUtils {

	/*public static class FlexoPaletteFileFilter extends FileFilter {
	
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return false;
		}
	
		@Override
		public String getDescription() {
			return FlexoLocalization.localizedForKey("flexo_palettes");
		}
	
	}*/

	/*public static class FlexoPaletteFileView extends FileView {
	
		protected FlexoPaletteFileView() {
	
		}
	
		@Override
		public Boolean isTraversable(File f) {
			if (f == null || !f.isDirectory()) {
				return Boolean.FALSE;
			}
			if (f.getName().toLowerCase().endsWith(".iepalette")) {
				return Boolean.FALSE;
			}
			File[] files = f.listFiles(new java.io.FileFilter() {
	
				@Override
				public boolean accept(File file) {
					return !file.isDirectory() && file.getName().toLowerCase().endsWith(".woxml");
				}
			});
			if (files != null && files.length > 0) {
				return Boolean.FALSE;
			}
			return super.isTraversable(f);
		}
	
		@Override
		public Icon getIcon(File f) {
			if (f.getName().toLowerCase().endsWith(".iepalette")) {
				return FilesIconLibrary.SMALL_FOLDER_ICON;
			} else if (f.isDirectory()) {
				File[] files = f.listFiles(new java.io.FileFilter() {
	
					@Override
					public boolean accept(File file) {
						return !file.isDirectory() && file.getName().toLowerCase().endsWith(".woxml");
					}
				});
				if (files != null && files.length > 0) {
					return FilesIconLibrary.SMALL_FOLDER_ICON;
				} else {
					return super.getIcon(f);
				}
			} else {
				return super.getIcon(f);
			}
		}
	}*/

	public static class FlexoProjectFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			return FlexoLocalization.getMainLocalizer().localizedForKey("flexo_projects");
		}

	}

	public static class FlexoProjectFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			// System.out.println("Est ce que c'est bon ? " + dir + " name=" + name);
			if (new File(dir, name).isDirectory() && name.toLowerCase().endsWith(".prj")) {
				// System.out.println("oui");
				return true;
			}
			// System.out.println("non");
			return false;
		}
	}

	/**
	 * @author gpolet
	 * 
	 */
	public static class FlexoProjectFileView extends FileView {

		protected FlexoProjectFileView() {

		}

		/**
		 * Overrides isTraversable
		 * 
		 * @see javax.swing.filechooser.FileView#isTraversable(java.io.File)
		 */
		@Override
		public Boolean isTraversable(File f) {
			System.out.println("isTraversable: " + f);
			if (f == null || !f.isDirectory()) {
				return Boolean.FALSE;
			}
			if (f.getName().toLowerCase().endsWith(".prj")) {
				return Boolean.FALSE;
			}
			else {
				return super.isTraversable(f);
			}
		}

		/**
		 * Overrides getIcon
		 * 
		 * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
		 */
		@Override
		public Icon getIcon(File f) {
			if (f.getName().toLowerCase().endsWith(".prj")) {
				return IconLibrary.OPENFLEXO_NOTEXT_16;
			}
			else {
				return super.getIcon(f);
			}
		}
	}

	public static final FileView PROJECT_FILE_VIEW = new FlexoProjectFileView();

	public static final FileFilter PROJECT_FILE_FILTER = new FlexoProjectFileFilter();

	public static final FilenameFilter PROJECT_FILE_NAME_FILTER = new FlexoProjectFilenameFilter();

	// public static final FileView PALETTE_FILE_VIEW = new FlexoPaletteFileView();

	// public static final FileFilter PALETTE_FILE_FILTER = new FlexoPaletteFileFilter();

}
