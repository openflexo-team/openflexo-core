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

package org.openflexo.foundation.fml.rt.logging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.LogLevel;
import org.openflexo.foundation.fml.rt.logging.FMLLoggingFilter.FilterType;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Represent the console related to the execution of FML inside a {@link FlexoEditor} (related to a project)
 * 
 * @author sylvain
 * 
 */
public class FMLConsole extends PropertyChangedSupportDefaultImplementation {

	private FlexoEditor editor;
	private LogLevel monitoredLogLevel = LogLevel.INFO;
	private boolean keepLogTrace = true;
	private int maxLogCount = -1; // -1 means infinite

	private final LinkedList<FMLLogRecord> allRecords;
	private final ArrayList<FMLLogRecord> filteredRecords = new ArrayList<FMLLogRecord>();
	private List<FMLLogRecord> records;

	private int totalLogs = 0;
	private int totalWarningLogs = 0;
	private int totalSevereLogs = 0;

	private int logCount = 0;
	private int warningCount = 0;
	private int severeCount = 0;

	private boolean filtersApplied = false;
	private boolean textSearchApplied = false;

	private FMLLoggingFormatter formatter;

	public FMLConsole(FlexoEditor editor) {
		this.editor = editor;
		allRecords = new LinkedList<FMLLogRecord>();
		records = allRecords;
		formatter = new FMLLoggingFormatter();
	}

	public FlexoEditor getFlexoEditor() {
		return editor;
	}

	/**
	 * Receive aLogString as debug in console
	 * 
	 * @param aLogString
	 */
	public void debug(String aLogString, FlexoConceptInstance fci, FlexoBehaviour behaviour) {
		FMLLogRecord logRecord = new FMLLogRecord(aLogString, LogLevel.DEBUG, fci, behaviour, this);
		add(logRecord);
		System.out.println(formatter.format(logRecord));
	}

	/**
	 * Send supplied logString to log console, with supplied log level
	 * 
	 * @param aLogString
	 * @param logLevel
	 */
	public void log(String aLogString, LogLevel logLevel, FlexoConceptInstance fci, FlexoBehaviour behaviour) {

		if (getMonitoredLogLevel().ordinal() >= logLevel.ordinal()) {
			FMLLogRecord logRecord = new FMLLogRecord(aLogString, logLevel, fci, behaviour, this);
			add(logRecord);
			System.out.println(formatter.format(logRecord));
		}
	}

	public LogLevel getMonitoredLogLevel() {
		return monitoredLogLevel;
	}

	public void setMonitoredLogLevel(LogLevel monitoredLogLevel) {
		if ((monitoredLogLevel == null && this.monitoredLogLevel != null)
				|| (monitoredLogLevel != null && !monitoredLogLevel.equals(this.monitoredLogLevel))) {
			LogLevel oldValue = this.monitoredLogLevel;
			this.monitoredLogLevel = monitoredLogLevel;
			getPropertyChangeSupport().firePropertyChange("monitoredLogLevel", oldValue, monitoredLogLevel);
		}
	}

	private boolean showDebug = true;

	public boolean isShowDebug() {
		return showDebug;
	}

	public void setShowDebug(boolean showDebug) {
		if (showDebug != this.showDebug) {
			boolean oldValue = this.showDebug;
			this.showDebug = showDebug;
			getPropertyChangeSupport().firePropertyChange("showDebug", oldValue, showDebug);
		}
	}

	public boolean getKeepLogTrace() {
		return keepLogTrace;
	}

	public void setKeepLogTrace(boolean keepLogTrace) {
		if (keepLogTrace != this.keepLogTrace) {
			boolean oldValue = this.keepLogTrace;
			this.keepLogTrace = keepLogTrace;
			getPropertyChangeSupport().firePropertyChange("keepLogTrace", oldValue, keepLogTrace);
		}
	}

	public int getMaxLogCount() {
		return maxLogCount;
	}

	public void setMaxLogCount(int maxLogCount) {
		if (maxLogCount != this.maxLogCount) {
			int oldValue = this.maxLogCount;
			this.maxLogCount = maxLogCount;
			getPropertyChangeSupport().firePropertyChange("maxLogCount", oldValue, maxLogCount);
		}
	}

	private boolean isNotifying = false;

	private void add(FMLLogRecord record) {
		synchronized (allRecords) {
			if (getMaxLogCount() > -1 && allRecords.size() > getMaxLogCount()) {
				allRecords.remove(0);
			}
			allRecords.add(record);
			if (record.level == LogLevel.WARNING) {
				totalWarningLogs++;
				getPropertyChangeSupport().firePropertyChange("warningLogs", null, record);
			}
			if (record.level == LogLevel.SEVERE) {
				totalSevereLogs++;
				getPropertyChangeSupport().firePropertyChange("severeLogs", null, record);
			}
			totalLogs++;
			if (!isNotifying) {
				isNotifying = true;
				getPropertyChangeSupport().firePropertyChange("records", null, record);
				getPropertyChangeSupport().firePropertyChange("totalLogs", null, record);
				getPropertyChangeSupport().firePropertyChange("logCount", null, record);
				isNotifying = false;
			}
		}
	}

	public FMLLogRecord elementAt(int row) {
		return allRecords.get(row);
	}

	public List<FMLLogRecord> getRecords() {
		return records;
	}

	public void setRecords(List<FMLLogRecord> records) {
		this.records = records;
	}

	public void addToRecords(FMLLogRecord record) {
		records.add(record);
	}

	public void removeFromRecords(FMLLogRecord record) {
		records.remove(record);
	}

	public void clearRecords() {
		logCount = 0;
		warningCount = 0;
		severeCount = 0;
		totalLogs = 0;
		totalWarningLogs = 0;
		totalSevereLogs = 0;
		allRecords.clear();
		dismissFilters();
		notifyFilteringChange();
	}

	public int getTotalLogs() {
		return totalLogs;
	}

	public int getWarningLogs() {
		return totalWarningLogs;
	}

	public int getSevereLogs() {
		return totalSevereLogs;
	}

	public int getLogCount() {
		if (!filtersApplied && !textSearchApplied) {
			return totalLogs;
		}
		return logCount;
	}

	public int getWarningCount() {
		if (!filtersApplied && !textSearchApplied) {
			return totalWarningLogs;
		}
		return warningCount;
	}

	public int getSevereCount() {
		if (!filtersApplied && !textSearchApplied) {
			return totalSevereLogs;
		}
		return severeCount;
	}

	public void applyFilters(List<FMLLoggingFilter> filters) {
		logCount = 0;
		warningCount = 0;
		severeCount = 0;
		filtersApplied = true;
		filteredRecords.clear();
		boolean onlyKeep = false;
		for (FMLLoggingFilter f : filters) {
			if (f.type == FilterType.OnlyKeep) {
				onlyKeep = true;
			}
		}
		for (FMLLogRecord r : allRecords) {
			boolean keepRecord = !onlyKeep;
			for (FMLLoggingFilter f : filters) {
				if (f.filterDoesApply(r)) {
					if (f.type == FilterType.OnlyKeep) {
						keepRecord = true;
					}
				}
			}
			for (FMLLoggingFilter f : filters) {
				if (f.filterDoesApply(r)) {
					if (f.type == FilterType.Dismiss) {
						keepRecord = false;
					}
				}
			}
			if (keepRecord) {
				filteredRecords.add(r);
				logCount++;
				if (r.level == LogLevel.WARNING) {
					warningCount++;
				}
				else if (r.level == LogLevel.SEVERE) {
					severeCount++;
				}
			}
		}
		records = filteredRecords;
		notifyFilteringChange();
	}

	public void dismissFilters() {
		filtersApplied = false;
		records = allRecords;
		notifyFilteringChange();
	}

	public void searchText(String someText) {
		logCount = 0;
		warningCount = 0;
		severeCount = 0;
		textSearchApplied = true;
		records = new ArrayList<FMLLogRecord>();
		FMLLoggingFilter f = new FMLLoggingFilter("search");
		f.setHasFilteredMessage(true);
		f.filteredContent = someText;
		for (FMLLogRecord r : filtersApplied() ? filteredRecords : allRecords) {
			if (f.filterDoesApply(r)) {
				records.add(r);
				logCount++;
				if (r.level == LogLevel.WARNING) {
					warningCount++;
				}
				else if (r.level == LogLevel.SEVERE) {
					severeCount++;
				}
			}
		}
		notifyFilteringChange();
	}

	public void dismissSearchText() {
		textSearchApplied = false;
		if (filtersApplied()) {
			records = filteredRecords;
			notifyFilteringChange();
		}
		else {
			records = allRecords;
			notifyFilteringChange();
		}
	}

	private void notifyFilteringChange() {
		getPropertyChangeSupport().firePropertyChange("logCount", -1, logCount);
		getPropertyChangeSupport().firePropertyChange("warningCount", -1, warningCount);
		getPropertyChangeSupport().firePropertyChange("severeCount", -1, severeCount);
		getPropertyChangeSupport().firePropertyChange("records", null, records);
		getPropertyChangeSupport().firePropertyChange("filtersApplied", false, true);
		getPropertyChangeSupport().firePropertyChange("textSearchApplied", false, true);
	}

	public boolean filtersApplied() {
		return filtersApplied;
	}

	public boolean textSearchApplied() {
		return textSearchApplied;
	}

}
