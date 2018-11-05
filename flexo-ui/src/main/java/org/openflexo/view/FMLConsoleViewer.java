/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Gina-swing, a component of the software infrastructure 
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

package org.openflexo.view;

import java.awt.Color;
import java.awt.Window;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.foundation.fml.rt.logging.FMLLogRecord;
import org.openflexo.foundation.fml.rt.logging.FMLLoggingFilter;
import org.openflexo.foundation.fml.rt.logging.FMLLoggingFilter.FilterType;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.icon.ImageIconResource;
import org.openflexo.icon.UtilsIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

public class FMLConsoleViewer extends PropertyChangedSupportDefaultImplementation {

	public static ResourceLocator rl = ResourceLocator.getResourceLocator();

	static final Logger LOGGER = Logger.getLogger(FMLConsoleViewer.class.getPackage().getName());

	public static final Resource CONSOLE_VIEWER_FIB_NAME = ResourceLocator.locateResource("Fib/FMLConsoleViewer.fib");

	public static final ImageIcon FILTER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Search.png"));

	private FMLConsole console;

	public Vector<FMLLoggingFilter> filters = new Vector<>();
	public String searchedText;

	public boolean displayLogLevel = false;
	public boolean displayFlexoConceptInstance = true;
	public boolean displayBehaviour = true;
	public boolean displaySequence = true;
	public boolean displayDate = true;
	public boolean displayMillis = true;
	public boolean displayThread = true;

	private static FMLConsoleViewer instance;
	private static JFIBDialog<FMLConsoleViewer> dialog;

	public static void showConsoleViewer(FMLConsole console, FIBLibrary fibLibrary, Window parent) {
		System.out.println("showConsoleViewer for " + console);
		FIBComponent consoleViewerComponent = fibLibrary.retrieveFIBComponent(CONSOLE_VIEWER_FIB_NAME, true);
		if (instance == null || dialog == null) {
			instance = new FMLConsoleViewer(console);
			dialog = JFIBDialog.instanciateAndShowDialog("FML Console", consoleViewerComponent, instance, parent, false,
					FlexoLocalization.getMainLocalizer());
		}
		else {
			instance.setConsole(console);
			dialog.showDialog();
		}
	}

	public FMLConsoleViewer(FMLConsole console) {
		this.console = console;
	}

	public FMLConsole getConsole() {
		return console;
	}

	public void setConsole(FMLConsole console) {
		if ((console == null && this.console != null) || (console != null && !console.equals(this.console))) {
			FMLConsole oldValue = this.console;
			this.console = console;
			getPropertyChangeSupport().firePropertyChange("console", oldValue, console);
		}
	}

	public List<FMLLogRecord> getRecords() {
		return console.getRecords();
	}

	public Icon getIconForFilter(FMLLoggingFilter filter) {
		return FILTER_ICON;
	}

	public Icon getIconForLogRecord(FMLLogRecord record) {
		if (record.level == FMLConsole.LogLevel.WARNING) {
			return UtilsIconLibrary.WARNING_ICON;
		}
		if (record.level == FMLConsole.LogLevel.SEVERE) {
			return UtilsIconLibrary.ERROR_ICON;
		}
		return null;
	}

	public Color getColorForLogRecord(FMLLogRecord record) {
		if (record.level == FMLConsole.LogLevel.INFO) {
			return Color.BLACK;
		}
		else if (record.level == FMLConsole.LogLevel.WARNING) {
			return Color.RED;
		}
		else if (record.level == FMLConsole.LogLevel.SEVERE) {
			return Color.PINK;
		}
		return Color.GRAY;
	}

	public Color getBgColorForLogRecord(FMLLogRecord record) {
		if (getConsole().filtersApplied()) {
			for (FMLLoggingFilter f : filters) {
				if (f.type == FilterType.Highlight && f.filterDoesApply(record)) {
					return Color.YELLOW;
				}
			}
		}
		return null;
	}

	public boolean getIsInfiniteNumberOfLogs() {
		return console.getMaxLogCount() == -1;
	}

	public void setIsInfiniteNumberOfLogs(boolean isInfinite) {
		if (isInfinite) {
			console.setMaxLogCount(-1);
		}
		else {
			console.setMaxLogCount(500);
		}
		getPropertyChangeSupport().firePropertyChange("isInfiniteNumberOfLogs", !isInfinite, isInfinite);
	}

	public void clear() {
		console.clearRecords();
	}

	public void refresh() {
		getPropertyChangeSupport().firePropertyChange("records", null, getRecords());
	}

	public void printStackTrace(FMLLogRecord record) {
		if (record == null) {
			return;
		}
		System.err.println("Stack trace for '" + record.message + "':");
		StringTokenizer st = new StringTokenizer(record.getStackTraceAsString(), StringUtils.LINE_SEPARATOR);
		while (st.hasMoreTokens()) {
			System.err.println("\t" + st.nextToken());
		}
	}

	public FMLLoggingFilter createFilter() {
		FMLLoggingFilter newFilter = new FMLLoggingFilter("New filter");
		filters.add(newFilter);
		return newFilter;
	}

	public void deleteFilter(FMLLoggingFilter filter) {
		filters.remove(filter);
	}

	public void applyFilters() {
		getConsole().applyFilters(filters);
	}

	public void dismissFilters() {
		getConsole().dismissFilters();
	}

	public void searchText() {
		if (StringUtils.isNotEmpty(searchedText)) {
			getConsole().searchText(searchedText);
		}
	}

	public void dismissSearchText() {
		getConsole().dismissSearchText();
	}

}
