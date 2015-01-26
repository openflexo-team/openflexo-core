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

package org.openflexo.fib;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.Flexo;
import org.openflexo.fib.editor.FIBAbstractEditor;
import org.openflexo.fib.utils.FlexoLoggingViewer;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.logging.FlexoLoggingManager;

public class LoggingViewerEDITOR extends FIBAbstractEditor {

	private static final Logger logger = FlexoLogger.getLogger(LoggingViewerEDITOR.class.getPackage().getName());

	@Override
	public Object[] getData() {
		return makeArray(new FlexoLoggingViewer(FlexoLoggingManager.instance()));
	}

	@Override
	public File getFIBFile() {
		return FlexoLoggingViewer.LOGGING_VIEWER_FIB;
	}

	public static void main(String[] args) {
		final FlexoLoggingManager loggingManager = Flexo.initializeLoggingManager();
		loggingManager.setKeepLogTrace(true);
		loggingManager.setMaxLogCount(-1);
		main(LoggingViewerEDITOR.class);
	}
}
