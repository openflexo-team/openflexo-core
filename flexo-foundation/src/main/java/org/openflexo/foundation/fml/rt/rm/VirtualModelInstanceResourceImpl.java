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

package org.openflexo.foundation.fml.rt.rm;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;

/**
 * Default implementation for {@link VirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class VirtualModelInstanceResourceImpl extends AbstractVirtualModelInstanceResourceImpl<VirtualModelInstance, VirtualModel>
		implements VirtualModelInstanceResource {

	static final Logger logger = Logger.getLogger(VirtualModelInstanceResourceImpl.class.getPackage().getName());

	@Override
	public Class<VirtualModelInstance> getResourceDataClass() {
		return VirtualModelInstance.class;
	}

	@Override
	public String computeDefaultURI() {
		if (getContainer() != null) {
			return getContainer().getURI() + "/" + (getName().endsWith(VirtualModelInstanceResourceFactory.VIRTUAL_MODEL_INSTANCE_SUFFIX)
					? getName() : (getName() + VirtualModelInstanceResourceFactory.VIRTUAL_MODEL_INSTANCE_SUFFIX));
		}
		return null;
	}

	private String virtualModelURI;

	@Override
	public String getVirtualModelURI() {
		if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getURI();
		}
		return virtualModelURI;
	}

	@Override
	public void setVirtualModelURI(String virtualModelURI) {
		this.virtualModelURI = virtualModelURI;
	}

	@Override
	public boolean delete(Object... context) {
		// gets service manager before deleting otherwise the service manager is null
		FlexoServiceManager serviceManager = getServiceManager();
		if (super.delete(context)) {
			if (getIODelegate().getSerializationArtefact() instanceof File) {
				serviceManager.getResourceManager().addToFilesToDelete((File) getIODelegate().getSerializationArtefact());
				return true;
			}
		}
		return false;
	}

}
