/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.components.widget;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.project.InteractiveProjectLoader;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select a {@link FlexoProject}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBProjectSelector extends FIBFlexoObjectSelector<FlexoProject> {

	static final Logger logger = Logger.getLogger(FIBProjectSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/ProjectSelector.fib");

	private InteractiveProjectLoader projectLoader;

	public FIBProjectSelector(FlexoProject editedObject) {
		super(editedObject);
		getTextField().setColumns(25);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@Override
	public Class<FlexoProject> getRepresentedType() {
		return FlexoProject.class;
	}

	@Override
	public String renderedString(FlexoProject editedObject) {
		if (editedObject != null) {
			return editedObject.getProjectName();
		}
		return "";
	}

	public InteractiveProjectLoader getProjectLoader() {
		return projectLoader;
	}

	public void setProjectLoader(InteractiveProjectLoader projectLoader) {
		InteractiveProjectLoader old = this.projectLoader;
		this.projectLoader = projectLoader;
		getPropertyChangeSupport().firePropertyChange("projectLoader", old, projectLoader);
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not instantiated in EDIT mode)
	/*
	public static void main(String[] args) {
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				FlexoProject project = Mockito.mock(FlexoProject.class);
				Mockito.when(project.getProjectName()).thenReturn("Root project");
				FlexoProject subProject1 = Mockito.mock(FlexoProject.class);
				Mockito.when(subProject1.getProjectName()).thenReturn("Sub project 1");
				FlexoProject subProject2 = Mockito.mock(FlexoProject.class);
				Mockito.when(subProject2.getProjectName()).thenReturn("Sub project 2");
				ProjectData projectData = Mockito.mock(ProjectData.class);
				Mockito.when(project.getProjectData()).thenReturn(projectData);
				PamelaModelFactory factory;
				try {
					factory = new PamelaModelFactory(FlexoProjectReference.class);
					FlexoProjectReference ref1 = factory.newInstance(FlexoProjectReference.class);
					ref1.init(subProject1);
					FlexoProjectReference ref2 = factory.newInstance(FlexoProjectReference.class);
					ref1.init(subProject2);
					Mockito.when(projectData.getImportedProjects()).thenReturn(Arrays.asList(ref1, ref2));
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}
	
				ProjectLoader projectLoader = Mockito.mock(ProjectLoader.class);
				Mockito.when(projectLoader.getRootProjects()).thenReturn(Arrays.asList(project));
				mockPropertyChangeSupport(projectLoader);
				mockPropertyChangeSupport(project);
				mockPropertyChangeSupport(subProject1);
				mockPropertyChangeSupport(subProject2);
				FIBProjectSelector selector = new FIBProjectSelector(null);
				selector.setProjectLoader(projectLoader);
				return makeArray(selector);
			}
	
			private void mockPropertyChangeSupport(HasPropertyChangeSupport changeSupport) {
				Mockito.when(changeSupport.getPropertyChangeSupport()).thenReturn(new PropertyChangeSupport(changeSupport));
			}
	
			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}
	
			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController(component);
			}
		};
		editor.launch();
	}
	*/
}
