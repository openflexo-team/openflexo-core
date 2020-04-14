/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.FileIODelegate.FileHasBeenWrittenOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.WillWriteFileOnDiskNotification;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;

public class MoveVirtualModelToDirectory extends FlexoAction<MoveVirtualModelToDirectory, VirtualModel, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(MoveVirtualModelToDirectory.class.getPackage().getName());

	public static FlexoActionFactory<MoveVirtualModelToDirectory, VirtualModel, FMLObject> actionType = new FlexoActionFactory<MoveVirtualModelToDirectory, VirtualModel, FMLObject>(
			"move_to_directory", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public MoveVirtualModelToDirectory makeNewAction(VirtualModel focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new MoveVirtualModelToDirectory(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(MoveVirtualModelToDirectory.actionType, VirtualModel.class);
	}

	private RepositoryFolder<VirtualModelResource, ?> newFolder;
	// private VirtualModelResource movedResource;

	MoveVirtualModelToDirectory(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	/*private List<VirtualModelResource> getAllVirtualModelResources(VirtualModelResource vm) {
		List<VirtualModelResource> returned = new ArrayList<>();
		buildAllVirtualModelResources(vm, returned);
		return returned;
	}
	
	private void buildAllVirtualModelResources(VirtualModelResource vm, List<VirtualModelResource> l) {
		l.add(vm);
		for (VirtualModelResource virtualModelResource : vm.getContainedVirtualModelResources()) {
			buildAllVirtualModelResources(virtualModelResource, l);
		}
	}*/

	/*	@Override
		protected void doAction(Object context)
				throws NotImplementedException, InvalidParameterException, InconsistentFlexoConceptHierarchyException {
	
			System.out.println("Move VM to " + getNewFolder());
	
			File vmDirectory = ((DirectoryBasedIODelegate) getFocusedObject().getResource().getIODelegate()).getDirectory();
	
			FlexoResourceCenter<?> oldResourceCenter = getFocusedObject().getResource().getResourceCenter();
			VirtualModelResourceFactory resourceFactory = getFMLTechnologyAdapter().getVirtualModelResourceFactory();
	
			List<VirtualModelResource> allVMResources = getAllVirtualModelResources((VirtualModelResource) getFocusedObject().getResource());
	
			for (VirtualModelResource virtualModelResource : allVMResources) {
				resourceFactory.unregisterResource(virtualModelResource, oldResourceCenter);
			}
	
			try {
				File newDirectory = new File((File) getNewFolder().getSerializationArtefact(), vmDirectory.getName());
	
				getServiceManager().notify(null, new WillWriteFileOnDiskNotification(vmDirectory));
				getServiceManager().notify(null, new WillWriteFileOnDiskNotification(newDirectory));
	
				Files.move(vmDirectory.toPath(), newDirectory.toPath());
	
				// Delete the resources
				for (VirtualModelResource virtualModelResource : allVMResources) {
					virtualModelResource.delete();
				}
	
				FlexoResourceCenter<File> newResourceCenter = (FlexoResourceCenter<File>) getNewFolder().getResourceRepository()
						.getResourceCenter();
				movedResource = resourceFactory.retrieveResource(newDirectory, newResourceCenter);
	
				getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(vmDirectory));
				getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(newDirectory));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
	
		}*/

	@Override
	protected void doAction(Object context) {

		System.out.println("Move VM to " + getNewFolder());

		File oldDirectory = ((DirectoryBasedIODelegate) getFocusedObject().getResource().getIODelegate()).getDirectory();
		File newDirectory = new File((File) getNewFolder().getSerializationArtefact(), oldDirectory.getName());

		FlexoResourceCenter<?> oldResourceCenter = getFocusedObject().getResource().getResourceCenter();
		VirtualModelResourceFactory resourceFactory = getFMLTechnologyAdapter().getVirtualModelResourceFactory();

		List<MovedResourceInfo> allVMResourceInfos = getAllVirtualModelResourceInfos(
				(VirtualModelResource) getFocusedObject().getResource(), oldDirectory, newDirectory);

		RepositoryFolder<FlexoResource<?>, ?> oldFolder = getFocusedObject().getResource().getResourceCenter()
				.getRepositoryFolder(getFocusedObject().getResource());
		oldFolder.removeFromResources(getFocusedObject().getResource());

		for (MovedResourceInfo movedResourceInfo : allVMResourceInfos) {
			resourceFactory.unregisterResource(movedResourceInfo.resource, oldResourceCenter);
		}

		try {

			getServiceManager().notify(null, new WillWriteFileOnDiskNotification(oldDirectory));
			getServiceManager().notify(null, new WillWriteFileOnDiskNotification(newDirectory));

			Files.move(oldDirectory.toPath(), newDirectory.toPath());

			// moved the resources
			for (MovedResourceInfo movedResourceInfo : allVMResourceInfos) {
				((DirectoryBasedIODelegate) movedResourceInfo.resource.getIODelegate()).moveToDirectory(movedResourceInfo.newfile);
			}

			FlexoResourceCenter<File> newResourceCenter = (FlexoResourceCenter<File>) getNewFolder().getResourceRepository()
					.getResourceCenter();
			// movedResource = resourceFactory.retrieveResource(newDirectory, newResourceCenter);

			for (MovedResourceInfo movedResourceInfo : allVMResourceInfos) {
				resourceFactory.registerResource(movedResourceInfo.resource, newResourceCenter, false);
				// System.out.println("Resource: " + movedResourceInfo.resource.getName() + " container="
				// + movedResourceInfo.resource.getContainer() + " contents=" + movedResourceInfo.resource.getContents());
			}

			getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(oldDirectory));
			getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(newDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	class MovedResourceInfo {
		VirtualModelResource resource;
		File oldFile;
		File newfile;

		public MovedResourceInfo(VirtualModelResource resource, File oldFile, File newfile) {
			super();
			this.resource = resource;
			this.oldFile = oldFile;
			this.newfile = newfile;
		}
	}

	private List<MovedResourceInfo> getAllVirtualModelResourceInfos(VirtualModelResource vm, File origin, File destination) {
		List<MovedResourceInfo> returned = new ArrayList<>();
		buildAllVirtualModelResourceInfos(vm, returned, origin, destination);
		return returned;
	}

	private void buildAllVirtualModelResourceInfos(VirtualModelResource vm, List<MovedResourceInfo> l, File origin, File destination) {
		l.add(new MovedResourceInfo(vm, origin, destination));
		for (VirtualModelResource virtualModelResource : vm.getContainedVirtualModelResources()) {
			String containedBaseName = virtualModelResource.getName();
			if (!containedBaseName.endsWith(VirtualModelResourceFactory.FML_SUFFIX)) {
				containedBaseName = containedBaseName + VirtualModelResourceFactory.FML_SUFFIX;
			}
			File containedOrigin = new File(origin, containedBaseName);
			File containedDestination = new File(destination, containedBaseName);
			buildAllVirtualModelResourceInfos(virtualModelResource, l, containedOrigin, containedDestination);
		}
	}

	/*public VirtualModelResource getMovedResource() {
		return movedResource;
	}*/

	public RepositoryFolder<VirtualModelResource, ?> getNewFolder() {
		return newFolder;
	}

	public void setNewFolder(RepositoryFolder<VirtualModelResource, ?> newFolder) {
		if ((newFolder == null && this.newFolder != null) || (newFolder != null && !newFolder.equals(this.newFolder))) {
			RepositoryFolder<VirtualModelResource, ?> oldValue = this.newFolder;
			this.newFolder = newFolder;
			getPropertyChangeSupport().firePropertyChange("newFolder", oldValue, newFolder);
		}
	}

	@Override
	public boolean isValid() {
		RepositoryFolder currentFolder = getFocusedObject().getResource().getResourceCenter()
				.getRepositoryFolder(getFocusedObject().getResource());

		if (getNewFolder() == null) {
			return false;
		}
		else if (currentFolder.isFatherOf(getNewFolder())) {
			return false;
		}
		return true;
	}

}
