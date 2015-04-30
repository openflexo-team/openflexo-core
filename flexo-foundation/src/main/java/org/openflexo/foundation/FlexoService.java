/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation;

/**
 * Implements a service (an object with operational state) in the Openflexo architecture. A {@link FlexoService} is responsible for a
 * particular task, and works in conjunction with other services within a {@link FlexoServiceManager} which receives and broadcast all
 * {@link ServiceNotification} to all registered services.
 * 
 * @author sylvain
 * 
 */
public interface FlexoService {

	/**
	 * Called by the {@link FlexoServiceManager} to register the service manager
	 * 
	 * @param serviceManager
	 */
	public void register(FlexoServiceManager serviceManager);

	/**
	 * Return the {@link FlexoServiceManager} where this {@link FlexoService} is registered. If not registered return null. This is the
	 * implemenation responsability to register itself the service manager
	 * 
	 * @return
	 */
	public FlexoServiceManager getServiceManager();

	/**
	 * Called after registration, after all services have been notified that this service has been registered
	 */
	public void initialize();

	/**
	 * Called to stop the service
	 */
	public void stop();

	/**
	 * Receives a new {@link ServiceNotification} broadcasted from the {@link FlexoServiceManager}
	 * 
	 * @param caller
	 * @param notification
	 */
	public void receiveNotification(FlexoService caller, ServiceNotification notification);

	/**
	 * A notification broadcasted by the {@link FlexoServiceManager}
	 * 
	 * @author sylvain
	 * 
	 */
	public static interface ServiceNotification {

	}
}
