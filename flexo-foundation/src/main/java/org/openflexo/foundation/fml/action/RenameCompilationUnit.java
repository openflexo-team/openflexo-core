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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

public class RenameCompilationUnit extends FlexoAction<RenameCompilationUnit, FMLCompilationUnit, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(RenameCompilationUnit.class.getPackage().getName());

	public static FlexoActionFactory<RenameCompilationUnit, FMLCompilationUnit, FMLObject> actionType = new FlexoActionFactory<RenameCompilationUnit, FMLCompilationUnit, FMLObject>(
			"rename", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public RenameCompilationUnit makeNewAction(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new RenameCompilationUnit(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(RenameCompilationUnit.actionType, FMLCompilationUnit.class);
	}

	private String newCompilationUnitName;
	private String newCompilationUnitURI;
	private String newCompilationUnitDescription;
	private boolean defaultURI = true;

	RenameCompilationUnit(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		newCompilationUnitName = focusedObject.getName();
		newCompilationUnitDescription = focusedObject.getDescription();
		if (focusedObject.getResource() != null && !focusedObject.getResource().computeDefaultURI().equals(focusedObject.getURI())) {
			newCompilationUnitURI = focusedObject.getURI();
			defaultURI = false;
		}
	}

	@Override
	protected void doAction(Object context) throws InvalidNameException {

		System.out.println("Rename VM to " + getNewCompilationUnitName());

		getFocusedObject().setName(getNewCompilationUnitName());
		getFocusedObject().setURI(getNewCompilationUnitURI());
		getFocusedObject().setDescription(getNewCompilationUnitDescription());

		try {
			getFocusedObject().getResource().save();
		} catch (SaveResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	public String getNewCompilationUnitName() {
		return newCompilationUnitName;
	}

	public void setNewCompilationUnitName(String newViewPointName) {
		this.newCompilationUnitName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newCompilationUnitName", null, newViewPointName);
		getPropertyChangeSupport().firePropertyChange("newCompilationUnitURI", null, getNewCompilationUnitURI());
	}

	public String getNewCompilationUnitDescription() {
		return newCompilationUnitDescription;
	}

	public void setNewCompilationUnitDescription(String newCompilationUnitDescription) {
		this.newCompilationUnitDescription = newCompilationUnitDescription;
		getPropertyChangeSupport().firePropertyChange("newCompilationUnitDescription", null, newCompilationUnitDescription);
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewCompilationUnitName());
	}

	public boolean getDefaultURI() {
		return defaultURI;
	}

	public void setDefaultURI(boolean defaultURI) {
		System.out.println("set default URI: " + defaultURI);
		if (defaultURI == true) {
			this.defaultURI = true;
			setNewCompilationUnitURI(null);
		}
		else {
			this.defaultURI = false;
			setNewCompilationUnitURI(computeDefaultURI());
		}
		getPropertyChangeSupport().firePropertyChange("defaultURI", !defaultURI, defaultURI);
	}

	private String computeDefaultURI() {
		String baseURI;
		if (getFocusedObject().getVirtualModel().getOwningVirtualModel() != null) {
			baseURI = getFocusedObject().getVirtualModel().getOwningVirtualModel().getURI();
		}
		else {
			RepositoryFolder currentFolder = getFocusedObject().getResource().getResourceCenter()
					.getRepositoryFolder(getFocusedObject().getResource());
			baseURI = currentFolder.getDefaultBaseURI();
		}
		if (!baseURI.endsWith("/")) {
			baseURI = baseURI + "/";
		}
		return baseURI + getBaseName() + CompilationUnitResourceFactory.FML_SUFFIX;
	}

	public String getNewCompilationUnitURI() {
		if (newCompilationUnitURI == null) {
			return computeDefaultURI();
		}

		return newCompilationUnitURI;
	}

	public void setNewCompilationUnitURI(String newCompilationUnitURI) {
		this.newCompilationUnitURI = newCompilationUnitURI;
		getPropertyChangeSupport().firePropertyChange("newCompilationUnitURI", null, newCompilationUnitURI);

	}

	public VirtualModelLibrary getVirtualModelLibrary() {
		return getServiceManager().getVirtualModelLibrary();
	}

	public boolean isNewCompilationUnitNameValid() {
		if (StringUtils.isEmpty(getNewCompilationUnitName())) {
			return false;
		}
		return true;
	}

	public boolean isNewCompilationUnitURIValid() {
		if (StringUtils.isEmpty(getNewCompilationUnitURI())) {
			return false;
		}
		try {
			new URL(getNewCompilationUnitURI());
		} catch (MalformedURLException e) {
			return false;
		}
		if (getVirtualModelLibrary() == null) {
			return false;
		}
		if (getVirtualModelLibrary().getCompilationUnitResource(getNewCompilationUnitURI()) != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewCompilationUnitNameValid()) {
			return false;
		}
		if (!isNewCompilationUnitURIValid()) {
			return false;
		}
		return true;
	}

}
