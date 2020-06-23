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

package org.openflexo.prefs;

import java.lang.management.ManagementFactory;

import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationData;
import org.openflexo.ApplicationVersion;
import org.openflexo.FlexoCst;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.ToolBox;

/**
 * This class represents the whole preferences hierarchy
 * 
 * @author sguerin
 */
@ModelEntity
@ImplementationClass(FlexoPreferences.FlexoPreferencesImpl.class)
@XMLElement
@Preferences(
		shortName = "Preferences",
		longName = "Openflexo Preferences",
		FIBPanel = "Fib/Prefs/OpenflexoPreferences.fib",
		smallIcon = "Icons/Flexo/OpenflexoNoText_16.png",
		bigIcon = "Icons/Flexo/OpenflexoNoText_64.png")
public interface FlexoPreferences extends PreferencesContainer, ResourceData<FlexoPreferences> {

	public ApplicationData getApplicationData();

	public String softwareVersion();

	public String javaVersion();

	public String getPlatform();

	public String getHeapMemory();

	public static abstract class FlexoPreferencesImpl extends PreferencesContainerImpl implements FlexoPreferences {

		@Override
		public ApplicationData getApplicationData() {
			if (getServiceManager() instanceof ApplicationContext) {
				return ((ApplicationContext) getServiceManager()).getApplicationData();
			}
			return null;
		}

		@Override
		public String softwareVersion() {
			return FlexoCst.BUSINESS_APPLICATION_VERSION + " (build " + ApplicationVersion.BUILD_ID + ")";
		}

		@Override
		public String javaVersion() {
			return System.getProperty("java.version");
		}

		@Override
		public String getPlatform() {
			return ToolBox.getPLATFORM();
		}

		@Override
		public String getHeapMemory() {
			return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / (1024 * 1024) + "Mb";
		}

	}
}
