/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexoutils, a component of the software infrastructure 
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

import java.util.logging.Logger;

import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

public class FMLLoggingFilter extends PropertyChangedSupportDefaultImplementation {

	static final Logger LOGGER = Logger.getLogger(FMLLoggingFilter.class.getPackage().getName());

	public String filterName;
	public FilterType type = FilterType.Highlight;
	public MessageFilterType messageFilterType = MessageFilterType.Contains;
	public String filteredContent;
	public FMLConsole.LogLevel filteredLevel = null;
	public String filteredSourceContent;
	public DateFilterType dateFilterType = null;
	public String filteredDate1;
	public String filteredDate2;
	public String filteredThread;
	public String filteredStacktrace;
	public int startSequence = -1;
	public int endSequence = -1;

	public static enum FilterType {
		OnlyKeep, Highlight, Dismiss
	}

	public static enum MessageFilterType {
		Contains, StartsWith, EndsWith
	}

	public static enum DateFilterType {
		Before, After, Between
	}

	public FMLLoggingFilter(String filterName) {
		this.filterName = filterName;
	}

	public boolean getHasFilteredMessage() {
		return messageFilterType != null;
	}

	public void setHasFilteredMessage(boolean aFlag) {
		if (aFlag) {
			messageFilterType = MessageFilterType.Contains;
		}
		else {
			messageFilterType = null;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredMessage", !aFlag, aFlag);
	}

	public boolean getHasFilteredLevel() {
		return filteredLevel != null;
	}

	public void setHasFilteredLevel(boolean aFlag) {
		if (aFlag) {
			filteredLevel = FMLConsole.LogLevel.INFO;
		}
		else {
			filteredLevel = null;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredLevel", !aFlag, aFlag);
	}

	public boolean getHasFilteredDate() {
		return dateFilterType != null;
	}

	public void setHasFilteredDate(boolean aFlag) {
		if (aFlag) {
			dateFilterType = DateFilterType.After;
		}
		else {
			dateFilterType = null;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredDate", !aFlag, aFlag);
	}

	public boolean getHasFilteredThread() {
		return filteredThread != null;
	}

	public void setHasFilteredThread(boolean aFlag) {
		if (aFlag) {
			filteredThread = "10";
		}
		else {
			filteredThread = null;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredThread", !aFlag, aFlag);
	}

	public boolean getHasFilteredStacktrace() {
		return filteredStacktrace != null;
	}

	public void setHasFilteredStacktrace(boolean aFlag) {
		if (aFlag) {
			filteredStacktrace = "Searched content";
		}
		else {
			filteredStacktrace = null;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredStacktrace", !aFlag, aFlag);
	}

	public boolean getHasFilteredSequence() {
		return startSequence > -1;
	}

	public void setHasFilteredSequence(boolean aFlag) {
		if (aFlag) {
			startSequence = 0;
			endSequence = 0;
		}
		else {
			startSequence = -1;
			endSequence = -1;
		}
		getPropertyChangeSupport().firePropertyChange("hasFilteredSequence", !aFlag, aFlag);
	}

	private boolean messageMatches(FMLLogRecord record) {
		if (StringUtils.isEmpty(record.message)) {
			return true;
		}
		switch (messageFilterType) {
			case Contains:
				if (record.message.contains(filteredContent)) {
					return true;
				}
				break;
			case StartsWith:
				if (record.message.startsWith(filteredContent)) {
					return true;
				}
				break;
			case EndsWith:
				if (record.message.endsWith(filteredContent)) {
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	private boolean levelMatches(FMLLogRecord record) {
		return record.level == filteredLevel;
	}

	private boolean threadMatches(FMLLogRecord record) {
		return record.threadAsString().equals(filteredThread);
	}

	private boolean stacktraceMatches(FMLLogRecord record) {
		return record.getStackTraceAsString().contains(filteredStacktrace);
	}

	private boolean sequenceMatches(FMLLogRecord record) {
		return record.sequence >= startSequence && record.sequence <= endSequence;
	}

	private static boolean dateMatches(FMLLogRecord record) {
		LOGGER.warning("Not implemented ");
		return true;
	}

	public boolean filterDoesApply(FMLLogRecord record) {
		if (getHasFilteredMessage()) {
			if (!messageMatches(record)) {
				return false;
			}
		}
		if (getHasFilteredLevel()) {
			if (!levelMatches(record)) {
				return false;
			}
		}
		if (getHasFilteredDate()) {
			if (!dateMatches(record)) {
				return false;
			}
		}
		if (getHasFilteredSequence()) {
			if (!sequenceMatches(record)) {
				return false;
			}

		}
		if (getHasFilteredStacktrace()) {
			if (!stacktraceMatches(record)) {
				return false;
			}
		}
		if (getHasFilteredThread()) {
			if (!threadMatches(record)) {
				return false;
			}
		}
		return true;
	}

	public String getFilterDescription() {
		StringBuffer returned = new StringBuffer();
		if (getHasFilteredMessage()) {
			returned.append("message " + messageFilterType + " " + filteredContent + " ");
		}
		if (getHasFilteredLevel()) {
			returned.append("level=" + filteredLevel + " ");
		}
		if (getHasFilteredDate()) {
			returned.append("date " + dateFilterType + " " + filteredDate1
					+ (dateFilterType == DateFilterType.Between ? " and " + filteredDate2 : "") + " ");
		}
		if (getHasFilteredStacktrace()) {
			returned.append("stacktrace contains " + filteredStacktrace + " ");
		}
		if (getHasFilteredThread()) {
			returned.append("thread=" + filteredThread + " ");
		}
		if (getHasFilteredSequence()) {
			returned.append("sequence between " + startSequence + " and " + endSequence + " ");
		}
		return returned.toString();
	}
}
