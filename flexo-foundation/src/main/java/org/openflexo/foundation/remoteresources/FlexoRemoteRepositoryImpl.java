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

import java.util.ArrayList;
import java.util.List;

import org.openflexo.toolbox.ImageIconResource;


public class FlexoRemoteRepositoryImpl implements FlexoRemoteRepository{

	private String name;
	
	private String uri;
	
	private List<FlexoBundle> flexoBundles;
	
	public FlexoRemoteRepositoryImpl(String name, String uri){
		this.name = name;
		this.uri = uri;
		initialize();
	}
	private void initialize(){
		flexoBundles = new ArrayList<FlexoBundle>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUri() {
		return uri;
	}

	public void setUri(String anUri) {
		this.uri = anUri;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public String getProvider() {
		return "NoProvider";
	}

	@Override
	public List<FlexoBundle> getFlexoBundles() {
		if(flexoBundles == null){
			flexoBundles = new ArrayList<FlexoBundle>();
		}
		return flexoBundles;
	}

	@Override
	public ImageIconResource getIcon() {
		return null;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	

}
	
