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

package org.openflexo.foundation.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.toolbox.FileUtils;

/**
 * This is the very first implementation of the new ResourceManager
 * 
 * @author sylvain
 * 
 */
public class ResourceManager extends FlexoServiceImpl implements FlexoService {

	protected static final Logger logger = Logger.getLogger(ResourceManager.class.getPackage().getName());

	private final List<FlexoResource<?>> resources;

	private final List<File> filesToDelete;

	public static ResourceManager createInstance() {
		return new ResourceManager();
	}

	private ResourceManager() {
		// Not now: will be performed by the ServiceManager
		// initialize();
		resources = new ArrayList<FlexoResource<?>>();
		filesToDelete = new ArrayList<File>();
	}

	@Override
	public void initialize() {
		logger.info("Initialized ResourceManager...");
	}

	public void registerResource(FlexoResource<?> resource) {

		if (resource.getResourceCenter() == null) {
			logger.warning("Resource belonging to no ResourceCenter: " + resource);
			Thread.dumpStack();
		}

		if (!resources.contains(resource)) {
			resources.add(resource);
			getServiceManager().notify(this, new ResourceRegistered(resource, null));
		}
		else {
			logger.info("Resource already registered: " + resource);
		}
		if (resource.getURI() == null) {
			logger.info("Resource with null URI: " + resource);
			Thread.dumpStack();
		}
	}

	public void unregisterResource(FlexoResource<?> resource) {
		resources.remove(resource);
		getServiceManager().notify(this, new ResourceUnregistered(resource, null));
	}

	public List<FlexoResource<?>> getRegisteredResources() {
		return resources;
	}

	public List<FlexoResource<?>> getRegisteredFileResources() {
		return (List<FlexoResource<?>>) getRegisteredResources(FlexoResource.class);
	}

	public <R extends FlexoResource<?>> List<? extends R> getRegisteredResources(Class<R> resourceClass) {
		List<R> returned = new ArrayList<R>();
		for (FlexoResource<?> r : getRegisteredResources()) {
			if (resourceClass.isAssignableFrom(r.getClass())) {
				returned.add((R) r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getLoadedResources() {
		List<FlexoResource<?>> returned = new ArrayList<FlexoResource<?>>();
		for (FlexoResource<?> r : resources) {
			if (r.isLoaded()) {
				returned.add(r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getUnsavedResources() {
		List<FlexoResource<?>> returned = new ArrayList<FlexoResource<?>>();
		for (FlexoResource<?> r : resources) {
			if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
				returned.add(r);
			}
			if (r.isDeleted()) {
				returned.add(r);
			}
		}
		return returned;
	}

	public FlexoResource<?> getResource(String resourceURI) {

		System.out.println("On cherche la resource " + resourceURI);
		System.out.println("on a ca: ");

		for (FlexoResource r : resources) {
			System.out.println(r.getURI());
		}

		if (StringUtils.isEmpty(resourceURI)) {
			return null;
		}
		for (FlexoResource r : resources) {
			if (resourceURI.equals(r.getURI())) {
				return r;
			}
		}
		return null;
	}

	public void addToFilesToDelete(File f) {
		filesToDelete.add(f);
	}

	public void removeFromFilesToDelete(File f) {
		filesToDelete.remove(f);
	}

	public void deleteFilesToBeDeleted() {
		for (File f : filesToDelete) {
			try {
				if (FileUtils.recursiveDeleteFile(f)) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Successfully deleted " + f.getAbsolutePath());
						// filesToDelete.remove(f);
					}
				}
				else if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			}
		}
		filesToDelete.clear();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
		if (notification instanceof ResourceCenterRemoved) {
			// In this case, we MUST unregister all resources contained in removed ResourceCenter
			FlexoResourceCenter<?> removedRC = ((ResourceCenterRemoved) notification).getRemovedResourceCenter();

			System.out.println("REMOVED RC:");
			for (FlexoResource<?> r : removedRC.getAllResources(null)) {
				System.out.println("Remove " + r.getURI());
				unregisterResource(r);
			}

			for (FlexoResource<?> r : resources) {
				System.out.println("RC: " + r.getResourceCenter() + " r: " + r);
				if (r.getResourceCenter() == removedRC) {
					System.out.println("Tiens faudrait desenregistrer la resource: " + r);
				}
			}
		}
	}
}
