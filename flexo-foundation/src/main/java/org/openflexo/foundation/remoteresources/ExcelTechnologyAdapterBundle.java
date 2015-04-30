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

package org.openflexo.foundation.remoteresources;

import java.net.MalformedURLException;
import java.net.URL;

import org.openflexo.toolbox.FlexoVersion;

public class ExcelTechnologyAdapterBundle extends DefaultFlexoBundle{

	public ExcelTechnologyAdapterBundle(String baseUri){
		try {
			String url1 = "openflexo-release/org/openflexo/excelconnector/1.7.0-beta/excelconnector-1.7.0-beta.jar";
			String url2 = "openflexo-release/org/openflexo/excelconnector-ui/1.7.0-beta/excelconnector-ui-1.7.0-beta.jar";
			getURLs().add(new URL(baseUri+url1));
			getURLs().add(new URL(baseUri+url2));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public FlexoBundleType getBundleType() {
		return FlexoBundleType.TECHNOLOGY_ADAPTER;
	}

	@Override
	public FlexoVersion getVersion() {
		return new FlexoVersion("1.7.0");
	}

	@Override
	public String getSimpleName() {
		return "Excel Technology Adaptor";
	}
	
}
